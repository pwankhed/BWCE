package com.ceva.eai.bwce.generic.cfw.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ceva.eai.bwce.generic.cfw.utils.executor.ExecutorServiceFactory;

/**
 * <p>
 * This class allow to read from an <code>InputStream</code> the data who has
 * been written to an <code>OutputStream</code> (performs an
 * <code>OutputStream</code> -> <code>InputStream</code> conversion).
 * </p>
 * <p>
 * More detailed it is an <code>OutputStream</code> that, when extended, allows
 * to read the data written to it from the <code>InputStream</code> inside the
 * method {@linkplain #doRead(InputStream)}.
 * </p>
 * <p>
 * To use this class you must extend it and implement the method
 * {@linkplain #doRead(InputStream)}. Inside this method place the logic that
 * needs to read the data from the <code>InputStream</code>. Then the data can
 * be written to this class that implements <code>OutputStream</code>. When
 * {@linkplain #close()} method is called on the outer <code>OutputStream</code>
 * an EOF is generated in the <code>InputStream</code> passed in the
 * {@linkplain #doRead(InputStream)}.
 * </p>
 * <p>
 * The {@linkplain #doRead(InputStream)} call executes in another thread, so
 * there is no warranty on when it will start and when it will end. Special care
 * must be taken in passing variables to it: all the arguments must be final and
 * inside {@linkplain #doRead(InputStream)} you shouldn't change the variables
 * of the outer class.
 * </p>
 * <p>
 * Any Exception threw inside the {@linkplain #doRead(InputStream)} method is
 * propagated to the outer <code>OutputStream</code> on the next
 * <code>write</code> operation.
 * </p>
 * <p>
 * The method {@link #getResult()} suspend the outer thread and wait for the
 * read from the internal stream is over. It returns when the
 * <code>doRead()</code> terminates and has produced its result.
 * </p>
 * <p>
 * Some sample code:
 * </p>
 * <code>
 * <pre>
 * OutputStreamToInputStream&lt;String&gt; oStream2IStream =
 * new OutputStreamToInputStream&lt;String&gt;() {
 * 	protected String doRead(final InputStream istream) throws Exception {
 * 		// Users of this class should place all the code that need to read data
 *      // from the InputStream in this method. Data available through the
 *      // InputStream passed as a parameter is the data that is written to the
 * 		// OutputStream oStream2IStream through its write() methods.
 * 		final String result = IOUtils.toString(istream);
 *      //"result" is here only for example of how returning data to the external OutputStream. 
 *      //Real implementations might return "null" if they doesn't need to return a value.
 * 		return result + &quot; was processed.&quot;;
 * 	}
 * };
 * try {
 * 	// some data is written to the OutputStream, will be passed to the method
 * 	// doRead(InputStream i) above and after close() is called the results
 * 	// will be available through the getResults() method.
 * 	oStream2IStream.write(&quot;test&quot;.getBytes());
 * } finally {
 * 	// don't miss the close (or a thread would not terminate correctly).
 * 	oStream2IStream.close();
 * }
 * String result = oStream2IStream.getResult();
 * //result now contains the string &quot;test was processed.&quot;
 * </pre></code>
 * 
 * @param <T> Type returned by the method {@link #getResults()} after the thread
 *            has finished.
 */
public abstract class OutputStreamToInputStream<T> extends PipedOutputStream {

	public static final int SKIP_BUFFER_SIZE = 8192;
	public static final int DEFAULT_PIPE_SIZE = 4096;

	/**
	 * This class executes in the second thread.
	 * 
	 * @author G.Contini
	 */
	private final class DataConsumer implements Callable<T> {

		@SuppressWarnings("deprecation")
		@Override
		public synchronized T call() throws Exception {
			T processResult;
			try {
				// avoid the internal class close the stream.
				final CloseShieldInputStream istream = new CloseShieldInputStream(OutputStreamToInputStream.this.inputstream);
				processResult = doRead(istream);
			} catch (final Exception e) {
				OutputStreamToInputStream.this.abort = true;
				throw e;
			} finally {
				// empty the internal inputstream so the outer thread doesn't
				// lock
				emptyInputStream();
				OutputStreamToInputStream.this.inputstream.close();
			}
			return processResult;
		}

		private void emptyInputStream() {
			try {
				final byte[] buffer = new byte[SKIP_BUFFER_SIZE];
				while (OutputStreamToInputStream.this.inputstream.read(buffer) >= 0) {
					;
					// empty block: just throw bytes away
				}
			} catch (final IOException e) {
				if ((e.getMessage() != null) && (e.getMessage().indexOf("closed") > 0)) {
					OutputStreamToInputStream.LOG.debug("Stream already closed");

				} else {
					OutputStreamToInputStream.LOG
							.error("IOException while empty InputStream a " + "thread can be locked", e);
				}
			} catch (final Throwable e) {
				OutputStreamToInputStream.LOG.error("IOException while empty InputStream a " + "thread can be locked",
						e);
			}
		}
	}

	/**
	 * Extends PipedInputStream to allow set the default buffer size.
	 */
	private final class MyPipedInputStream extends PipedInputStream {

		MyPipedInputStream(final int bufferSize) {
			super.buffer = new byte[bufferSize];
		}
	}

	// Default timeout in milliseconds.
	private static final int DEFAULT_TIMEOUT = 15 * 60 * 1000;

	/**
	 * The default pipe buffer size for the newly created pipes.
	 */
	private static int defaultPipeSize = DEFAULT_PIPE_SIZE;

	private static final Logger LOG = LoggerFactory.getLogger(OutputStreamToInputStream.class);

	/**
	 * Set the size for the pipe circular buffer. This setting has effect for the
	 * newly created <code>OutputStreamToInputStream</code>. Default is 4096 bytes.
	 * 
	 * @since 1.2.3
	 * @param defaultPipeSize The default pipe buffer size in bytes.
	 */
	public static void setDefaultPipeSize(final int defaultPipeSize) {
		OutputStreamToInputStream.defaultPipeSize = defaultPipeSize;
	}

	private boolean abort = false;
	private boolean closeCalled = false;
	private final ExecutorService executorService;
	private final InputStream inputstream;
	private final boolean joinOnClose;
	private Future<T> writingResult = null;

	/**
	 * <p>
	 * Creates a new <code>OutputStreamToInputStream</code>. It uses the default
	 * {@link ExecutionModel#THREAD_PER_INSTANCE} thread instantiation strategy.
	 * This means that a new thread is created for every instance of
	 * <code>OutputStreamToInputStream</code>.
	 * </p>
	 * <p>
	 * When the {@linkplain #close()} method is called this class wait for the
	 * internal thread to terminate.
	 * </p>
	 * 
	 * @throws java.lang.IllegalStateException Exception thrown if pipe can't be
	 *                                         created.
	 */
	public OutputStreamToInputStream() {
		this(true, ExecutorServiceFactory.ExecutionModel.THREAD_PER_INSTANCE);
	}

	/**
	 * <p>
	 * Creates a new <code>OutputStreamToInputStream</code>. It uses the default
	 * {@link ExecutionModel#THREAD_PER_INSTANCE} thread instantiation strategy.
	 * This means that a new thread is created for every instance of
	 * <code>OutputStreamToInputStream</code>.
	 * </p>
	 * <p>
	 * If <code>startImmediately</code> is <code>true</code> the internal thread
	 * will start before the constructor completes. This is the best way if you're
	 * doing anonymous subclassing. While if you do explicit sublcassing you should
	 * set this parameter to false to allow the constructor of the superclass to
	 * complete before the threads are started.
	 * </p>
	 * <p>
	 * When the {@linkplain #close()} method is called this class wait for the
	 * internal thread to terminate.
	 * </p>
	 * 
	 * @since 1.2.13
	 * @throws java.lang.IllegalStateException Exception thrown if pipe can't be
	 *                                         created.
	 */
	public OutputStreamToInputStream(final boolean startImmediately) {
		this(startImmediately, true,
				ExecutorServiceFactory.getExecutor(ExecutorServiceFactory.ExecutionModel.THREAD_PER_INSTANCE),
				defaultPipeSize);
	}

	/**
	 * <p>
	 * Creates a new <code>OutputStreamToInputStream</code>. It let the user specify
	 * the thread instantiation service and what will happen upon the invocation of
	 * <code>close()</code> method.
	 * </p>
	 * <p>
	 * If <code>startImmediately</code> is <code>true</code> the internal thread
	 * will start before the constructor completes. This is the best way if you're
	 * doing anonymous subclassing. While if you do explicit sublcassing you should
	 * set this parameter to false to allow the constructor of the superclass to
	 * complete before the threads are started.
	 * </p>
	 * If <code>joinOnClose</code> is <code>true</code> when the
	 * <code>close()</code> method is invoked this class will wait for the internal
	 * thread to terminate.
	 * </p>
	 * <p>
	 * It also let the user specify the size of the pipe buffer to allocate.
	 * </p>
	 * 
	 * @since 1.2.13
	 * @param startImmediately if <code>true</code> the internal thread will start
	 *                         immediately after this constructor completes.
	 * @param joinOnClose      if <code>true</code> the internal thread will be
	 *                         joined when close is invoked.
	 * @param executorService  Service for executing the internal thread.
	 * @param pipeBufferSize   The size of the pipe buffer to allocate.
	 * @throws java.lang.IllegalStateException Exception thrown if pipe can't be
	 *                                         created.
	 */
	public OutputStreamToInputStream(final boolean startImmediately, final boolean joinOnClose,
			final ExecutorService executorService, final int pipeBufferSize) {
		if (executorService == null) {
			throw new IllegalArgumentException("executor service can't be null");
		}
		final String callerId = Thread.currentThread().getName();
		final PipedInputStream pipedIS = new MyPipedInputStream(pipeBufferSize);
		try {
			pipedIS.connect(this);
		} catch (final IOException e) {
			throw new IllegalStateException("Error during pipe creaton", e);
		}
		this.joinOnClose = joinOnClose;
		this.inputstream = pipedIS;
		this.executorService = executorService;
		LOG.debug("invoked by[{}] queued for start.", callerId);
		if (startImmediately) {
			initializeIfNecessary();
		}
	}

	/**
	 * <p>
	 * Creates a new <code>OutputStreamToInputStream</code>. It let the user specify
	 * the thread instantiation strategy and what will happen upon the invocation of
	 * <code>close()</code> method.
	 * </p>
	 * <p>
	 * If <code>joinOnClose</code> is <code>true</code> when the
	 * <code>close()</code> method is invoked this class will wait for the internal
	 * thread to terminate.
	 * </p>
	 * 
	 * @see ExecutionModel
	 * @param joinOnClose    if <code>true</code> the internal thread will be joined
	 *                       when close is invoked.
	 * @param executionModel The strategy for allocating threads.
	 * @throws java.lang.IllegalStateException Exception thrown if pipe can't be
	 *                                         created.
	 */
	public OutputStreamToInputStream(final boolean joinOnClose,
			final ExecutorServiceFactory.ExecutionModel executionModel) {
		this(joinOnClose, ExecutorServiceFactory.getExecutor(executionModel));
	}

	/**
	 * <p>
	 * Creates a new <code>OutputStreamToInputStream</code>. It let the user specify
	 * the thread instantiation service and what will happen upon the invocation of
	 * <code>close()</code> method.
	 * </p>
	 * <p>
	 * If <code>joinOnClose</code> is <code>true</code> when the
	 * <code>close()</code> method is invoked this class will wait for the internal
	 * thread to terminate.
	 * </p>
	 * 
	 * @since 1.2.6
	 * @param joinOnClose     if <code>true</code> the internal thread will be
	 *                        joined when close is invoked.
	 * @param executorService Service for executing the internal thread.
	 * @throws java.lang.IllegalStateException Exception thrown if pipe can't be
	 *                                         created.
	 */
	public OutputStreamToInputStream(final boolean joinOnClose, final ExecutorService executorService) {
		this(joinOnClose, executorService, defaultPipeSize);
	}

	/**
	 * <p>
	 * Creates a new <code>OutputStreamToInputStream</code>. It let the user specify
	 * the thread instantiation service and what will happen upon the invocation of
	 * <code>close()</code> method.
	 * </p>
	 * <p>
	 * If <code>joinOnClose</code> is <code>true</code> when the
	 * <code>close()</code> method is invoked this class will wait for the internal
	 * thread to terminate.
	 * </p>
	 * <p>
	 * It also let the user specify the size of the pipe buffer to allocate.
	 * </p>
	 * 
	 * @since 1.2.6
	 * @param joinOnClose     if <code>true</code> the internal thread will be
	 *                        joined when close is invoked.
	 * @param executorService Service for executing the internal thread.
	 * @param pipeBufferSize  The size of the pipe buffer to allocate.
	 * @throws java.lang.IllegalStateException Exception thrown if pipe can't be
	 *                                         created.
	 */
	public OutputStreamToInputStream(final boolean joinOnClose, final ExecutorService executorService,
			final int pipeBufferSize) {
		this(false, joinOnClose, executorService, pipeBufferSize);
	}

	/**
	 * <p>
	 * This method is called just before the close method completes, and after the
	 * eventual join with the internal thread.
	 * </p>
	 * <p>
	 * It is an extension point designed for applications that need to perform some
	 * operation when the <code>OutputStream</code> is closed.
	 * </p>
	 * 
	 * @since 1.2.9
	 * @throws IOException threw when the extension point wants to launch an
	 *                     exception.
	 */
	protected void afterClose() throws IOException {
		// extension point;
	}

	/** {@inheritDoc} */
	@Override
	public final void close() throws IOException {
		internalClose(this.joinOnClose, TimeUnit.MILLISECONDS, DEFAULT_TIMEOUT);
	}

	/**
	 * When this method is called the internal thread is always waited for
	 * completion.
	 * 
	 * @param timeout maximum time to wait for the internal thread to finish.
	 * @param tu      Time unit for the timeout.
	 * @throws java.io.IOException Threw if some problem (timeout or internal
	 *                             exception) occurs. see the
	 *                             <code>getCause()</code> method for the
	 *                             explanation.
	 */
	public final void close(final long timeout, final TimeUnit tu) throws IOException {
		internalClose(true, tu, timeout);
	}

	/**
	 * <p>
	 * This method has to be implemented to use this class. It allows to retrieve
	 * the data written to the outer <code>OutputStream</code> from the
	 * <code>InputStream</code> passed as a parameter.
	 * </p>
	 * <p>
	 * Any exception eventually threw inside this method will be propagated to the
	 * external <code>OutputStream</code>. When the next {@linkplain #write(byte[])}
	 * operation is called an <code>IOException</code> will be thrown and the
	 * original exception can be accessed calling the getCause() method on the
	 * IOException. It will also be available by calling the method
	 * {@link #getResults()}.
	 * </p>
	 * 
	 * @param istream The InputStream where the data can be retrieved.
	 * @return Optionally returns a result of the elaboration.
	 * @throws java.lang.Exception If an <code>java.lang.Exception</code> occurs
	 *                             during the elaboration it can be thrown. It will
	 *                             be propagated to the external
	 *                             <code>OutputStream</code> and will be available
	 *                             calling the method {@link #getResults()}.
	 */
	protected abstract T doRead(InputStream istream) throws Exception;

	/** {@inheritDoc} */
	@Override
	public final void flush() throws IOException {
		initializeIfNecessary();
		if (this.abort) {
			// internal thread is already aborting. wait for short time.
			internalClose(true, TimeUnit.SECONDS, 1);
		} else {
			super.flush();
		}
	}

	/**
	 * <p>
	 * This method returns the result of the method {@link #doRead(InputStream)} and
	 * ensure the previous method is over.
	 * </p>
	 * <p>
	 * This method suspend the calling thread and waits for the function
	 * {@link #doRead(InputStream)} to finish. It returns when the
	 * <code>doRead()</code> terminates and has produced its result.
	 * </p>
	 * <p>
	 * It must be called after the method {@link #close()} otherwise a
	 * <code>IllegalStateException</code> is thrown.
	 * </p>
	 * 
	 * @exception InterruptedException Thrown when the thread is interrupted.
	 * @exception Exception            Thrown if the method
	 *                                 {@linkplain #doRead(InputStream)} threw an
	 *                                 Exception.
	 * 
	 * @return the object returned from the doRead() method.
	 */
	public final T getResult() throws Exception {
		initializeIfNecessary();
		if (!this.closeCalled) {
			this.closeCalled = true;
			super.close();
		}
		T result;
		try {
			result = this.writingResult.get();
		} catch (ExecutionException e) {
			if (e.getCause() instanceof Exception) {
				throw (Exception) e.getCause();
			} else {
				throw e;
			}
		}
		return result;
	}

	private void initializeIfNecessary() {
		if (this.writingResult == null) {
			final DataConsumer executingProcess = new DataConsumer();
			this.writingResult = this.executorService.submit(executingProcess);
		}
	}

	private void internalClose(final boolean join, final TimeUnit timeUnit, final long timeout) throws IOException {
		if (!this.closeCalled) {
			initializeIfNecessary();
			this.closeCalled = true;
			super.close();
			if (join) {
				// waiting for thread to finish..
				try {
					this.writingResult.get(timeout, timeUnit);
				} catch (final ExecutionException e) {
					final IOException e1 = new IOException(
							"The doRead() threw exception. Use " + "getCause() for details.");
					e1.initCause(e.getCause());
					throw e1;
				} catch (final InterruptedException e) {
					final IOException e1 = new IOException("Waiting of the thread has been interrupted");
					e1.initCause(e);
					throw e1;
				} catch (final TimeoutException e) {
					if (!this.writingResult.isDone()) {
						this.writingResult.cancel(true);
					}
					final IOException e1 = new IOException("Waiting for the internal "
							+ "thread to finish took more than [" + timeout + "] " + timeUnit);
					e1.initCause(e);
					throw e1;
				}
			}
			afterClose();
		}
	}

	/** {@inheritDoc} */
	@Override
	public final void write(final byte[] bytes) throws IOException {
		initializeIfNecessary();
		if (this.abort) {
			// internal thread is already aborting. wait for short time.
			internalClose(true, TimeUnit.SECONDS, 1);
		} else {
			super.write(bytes);
		}
	}

	/** {@inheritDoc} */
	@Override
	public final void write(final byte[] bytes, final int offset, final int length) throws IOException {
		initializeIfNecessary();
		if (this.abort) {
			// internal thread is already aborting. wait for short time.
			internalClose(true, TimeUnit.SECONDS, 1);
		} else {
			super.write(bytes, offset, length);
		}
	}

	/** {@inheritDoc} */
	@Override
	public final void write(final int bytetowr) throws IOException {
		initializeIfNecessary();
		if (this.abort) {
			// internal thread is already aborting. wait for short time.
			internalClose(true, TimeUnit.SECONDS, 1);
		} else {
			super.write(bytetowr);
		}
	}
}