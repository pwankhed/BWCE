package com.ceva.eai.bwce.generic.cfw.utils.io;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ceva.eai.bwce.generic.cfw.utils.executor.ExecutorServiceFactory;

/**
 * <p>
 * This class allow to read the data written to an Writer from an Reader.
 * </p>
 * <p>
 * To use this class you must subclass it and implement the abstract method
 * {@linkplain #produce(Writer)}. The data who is produced inside this function
 * can be written to the sink <code>Writer</code> passed as a parameter. Later
 * it can be read back from from the <code>ReaderFromWriter</code> class (whose
 * ancestor is <code>java.io.Reader</code> ).
 * </p>
 * 
 * <pre>
 * final String dataId=//id of some data.
 * final ReaderFromWriter&lt;String&gt; rfw
 *                          = new ReaderFromWriter&lt;String&gt;() {
 *   &#064;Override
 *   public String produce(final Writer dataSink) throws Exception {
 *      //call your application function who produces the data here
 *      //WARNING: we're in another thread here, so this method shouldn't
 *      //write any class field or make assumptions on the state of the class.
 *      return produceMydata(dataId,dataSink)
 *   }
 * };
 *  try {
 *    //now you can read from the Reader the data that was written to the
 *    //dataSink Writer
 *     char[] read=IOUtils.toCharArray(rfw);
 *     //Use data here
 *   } catch (final IOException e) {
 *     //Handle exception here
 *   } finally {
 *   rfw.close();
 * }
 * //You can get the result of produceMyData after the stream has been closed.
 * String resultOfProduction = isos.getResult();
 * </pre>
 * <p>
 * This class encapsulates a pipe and a <code>Thread</code>, hiding the
 * complexity of using them. It is possible to select different strategies for
 * allocating the internal thread or even specify the
 * {@linkplain ExecutorService} for thread execution.
 * </p>
 * 
 * @param <T> Optional result returned by the function
 *            {@linkplain #produce(Writer)} after the data has been written. It
 *            can be obtained calling the {@linkplain #getResult()}
 * @see ExecutionModel $
 */
public abstract class ReaderFromWriter<T> extends Reader {

	public static final int SKIP_BUFFER_SIZE = 8192;
	public static final int DEFAULT_PIPE_SIZE = 4096;

	/**
	 * This inner class run in another thread and calls the {@link #produce(Writer)}
	 * method.
	 */
	private final class DataProducer implements Callable<T> {

		private final String name;

		private final Writer writer;

		DataProducer(final String threadName, final Writer writer) {
			this.writer = writer;
			this.name = threadName;
		}

		@Override
		public T call() throws Exception {
			final String threadName = getName();
			T result;
			ReaderFromWriter.ACTIVE_THREAD_NAMES.add(threadName);
			ReaderFromWriter.LOG.debug("thread [" + threadName + "] started.");
			try {
				result = produce(this.writer);
			} finally {
				closeStream();
				ReaderFromWriter.ACTIVE_THREAD_NAMES.remove(threadName);
				ReaderFromWriter.LOG.debug("thread [" + threadName + "] closed.");
			}
			return result;
		}

		private void closeStream() {
			try {
				this.writer.close();
			} catch (final IOException e) {
				if ((e.getMessage() != null) && (e.getMessage().indexOf("closed") > 0)) {
					ReaderFromWriter.LOG.debug("Stream already closed");
				} else {
					ReaderFromWriter.LOG.error("IOException closing Writer" + " Thread might be locked", e);
				}
			} catch (final Throwable t) {
				ReaderFromWriter.LOG.error("Error closing Reader" + " Thread might be locked", t);
			}
		}

		String getName() {
			return this.name;
		}

	}

	/**
	 * Extends PipedReader to allow set the default buffer size.
	 */
	private final class MyPipedReader extends PipedReader {

		MyPipedReader(final int bufferSize) {
			super(bufferSize);
		}
	}

	/**
	 * Collection for debugging purpose containing names of threads (name is
	 * calculated from the instantiation line. See <code>getCaller()</code>.
	 */
	private static final List<String> ACTIVE_THREAD_NAMES = Collections.synchronizedList(new ArrayList<String>());

	/**
	 * The default pipe buffer size for the newly created pipes.
	 */
	private static int defaultPipeSize = DEFAULT_PIPE_SIZE;
	private static final Logger LOG = LoggerFactory.getLogger(ReaderFromWriter.DataProducer.class);

	/**
	 * This method can be used for debugging purposes to get a list of the currently
	 * active threads.
	 * 
	 * @return Array containing names of the threads currently active.
	 */
	public static final String[] getActiveThreadNames() {
		final String[] result;
		synchronized (ReaderFromWriter.ACTIVE_THREAD_NAMES) {
			result = ReaderFromWriter.ACTIVE_THREAD_NAMES.toArray(new String[0]);
		}
		return result;
	}

	/**
	 * Set the size for the pipe circular buffer for the newly created
	 * <code>ReaderFromWriter</code>. Default is 4096 bytes.
	 * 
	 * @since 1.2.7
	 * @param defaultPipeSize the default pipe buffer size in bytes.
	 */
	public static void setDefaultPipeSize(final int defaultPipeSize) {
		ReaderFromWriter.defaultPipeSize = defaultPipeSize;
	}

	private boolean closeCalled = false;
	private Future<T> futureResult = null;
	private final boolean joinOnClose;
	private final PipedReader pipedReader;
	private final String callerId;

	protected final ExecutorService executorService;

	/**
	 * <p>
	 * It creates a <code>ReaderFromWriter</code> with a THREAD_PER_INSTANCE thread
	 * strategy.
	 * </p>
	 * 
	 * @see ExecutionModel#THREAD_PER_INSTANCE
	 */
	public ReaderFromWriter() {
		this(ExecutorServiceFactory.ExecutionModel.THREAD_PER_INSTANCE);
	}

	/**
	 * <p>
	 * It creates a <code>ReaderFromWriter</code> and let the user choose the thread
	 * allocation strategy he likes.
	 * </p>
	 * <p>
	 * This class executes the produce method in a thread created internally.
	 * </p>
	 * 
	 * @since 1.2.7
	 * @see ExecutionModel
	 * @param executionModel Defines how the internal thread is allocated.
	 * @param joinOnClose    If <code>true</code> the {@linkplain #close()} method
	 *                       will also wait for the internal thread to finish.
	 */
	public ReaderFromWriter(final boolean joinOnClose, final ExecutorServiceFactory.ExecutionModel executionModel) {
		this(joinOnClose, ExecutorServiceFactory.getExecutor(executionModel));
	}

	/**
	 * <p>
	 * It creates a <code>ReaderFromWriter</code> and let the user specify the
	 * ExecutorService that will execute the {@linkplain #produce(Writer)} method.
	 * </p>
	 * 
	 * @since 1.2.7
	 * @see ExecutorService
	 * @param executor    Defines the ExecutorService that will allocate the the
	 *                    internal thread.
	 * @param joinOnClose If <code>true</code> the {@linkplain #close()} method will
	 *                    also wait for the internal thread to finish.
	 */
	public ReaderFromWriter(final boolean joinOnClose, final ExecutorService executor) {
		this(joinOnClose, executor, defaultPipeSize);
	}

	/**
	 * <p>
	 * It creates a <code>ReaderFromWriter</code> and let the user specify the
	 * <code>ExecutorService</code> that will execute the
	 * {@linkplain #produce(Writer)} method and the pipe buffer size.
	 * </p>
	 * <p>
	 * Using this method the default size is ignored.
	 * </p>
	 * 
	 * @since 1.2.7
	 * @see ExecutorService
	 * @param executor       Defines the ExecutorService that will allocate the the
	 *                       internal thread.
	 * @param joinOnClose    If <code>true</code> the {@linkplain #close()} method
	 *                       will also wait for the internal thread to finish.
	 * @param pipeBufferSize The size of the pipe buffer to allocate.
	 */
	public ReaderFromWriter(final boolean joinOnClose, final ExecutorService executor, final int pipeBufferSize) {
		this.joinOnClose = joinOnClose;
		this.pipedReader = new MyPipedReader(pipeBufferSize);
		this.executorService = executor;
		this.callerId = Thread.currentThread().getName();

	}

	private void checkInitialized() {
		if (futureResult == null) {
			PipedWriter pipedWriter = null;
			try {
				pipedWriter = new PipedWriter(this.pipedReader) {
					private boolean writerCloseCalled = false;

					// duplicate close hangs the pipeStream see issue #36
					@Override
					public void close() throws IOException {
						synchronized (this) {
							if (writerCloseCalled) {
								return;
							}
							writerCloseCalled = true;
						}
						super.close();
					}
				};
			} catch (final IOException e) {
				throw new RuntimeException("Error during pipe creaton", e);
			}
			final Callable<T> executingCallable = new DataProducer(callerId, pipedWriter);
			this.futureResult = this.executorService.submit(executingCallable);
			ReaderFromWriter.LOG.debug("thread created by[{}] queued for start.", this.callerId);
		}
	}

	/**
	 * <p>
	 * It creates a <code>ReaderFromWriter</code> and let the user choose the thread
	 * allocation strategy he likes.
	 * </p>
	 * <p>
	 * This class executes the produce method in a thread created internally.
	 * </p>
	 * 
	 * @see ExecutionModel
	 * @param executionModel Defines how the internal thread is allocated.
	 */
	public ReaderFromWriter(final ExecutorServiceFactory.ExecutionModel executionModel) {
		this(false, executionModel);
	}

	/**
	 * <p>
	 * It creates a <code>ReaderFromWriter</code> and let the user specify the
	 * ExecutorService that will execute the {@linkplain #produce(Writer)} method.
	 * </p>
	 * 
	 * @see ExecutorService
	 * @param executor Defines the ExecutorService that will allocate the the
	 *                 internal thread.
	 */
	public ReaderFromWriter(final ExecutorService executor) {
		this(false, executor);
	}

	private void checkException() throws IOException {
		try {
			this.futureResult.get();
		} catch (final ExecutionException e) {
			final Throwable t = e.getCause();
			final IOException e1 = new IOException("Exception producing data");
			e1.initCause(t);
			throw e1;
		} catch (final InterruptedException e) {
			final IOException e1 = new IOException("Thread interrupted");
			e1.initCause(e);
			throw e1;

		}
	}

	/** {@inheritDoc} */
	@Override
	public final void close() throws IOException {
		checkInitialized();
		if (!this.closeCalled) {
			this.closeCalled = true;
			this.pipedReader.close();
			if (this.joinOnClose) {
				try {
					getResult();
				} catch (final Exception e) {
					final IOException e1 = new IOException("The internal stream threw exception");
					e1.initCause(e);
					throw e1;
				}
			}
		}
	}

	/**
	 * <p>
	 * Returns the object that was previously returned by the
	 * {@linkplain #produce(Writer)} method. It performs all the synchronization
	 * operations needed to read the result and waits for the internal thread to
	 * terminate.
	 * </p>
	 * <p>
	 * This method must be called after the method {@linkplain #close()}, otherwise
	 * an IllegalStateException is thrown.
	 * </p>
	 * 
	 * @since 1.2.7
	 * @return Object that was returned by the {@linkplain #produce(Writer)} method.
	 * @throws java.lang.Exception             If the {@linkplain #produce(Writer)}
	 *                                         method threw an java.lang.Exception
	 *                                         this method will throw again the same
	 *                                         exception.
	 * @throws java.lang.IllegalStateException If the {@linkplain #close()} method
	 *                                         hasn't been called yet.
	 */
	public T getResult() throws Exception {
		if (!this.closeCalled) {
			throw new IllegalStateException("getResult() called before close()." + "This method can be called only "
					+ "after the stream has been closed.");
		}
		T result;
		try {
			result = this.futureResult.get();
		} catch (final ExecutionException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof Exception) {
				throw (Exception) cause;
			}
			throw e;
		}
		return result;
	}

	/**
	 * <p>
	 * This method must be implemented by the user of this class to produce the data
	 * that must be read from the external <code>Reader</code>.
	 * </p>
	 * <p>
	 * Special care must be paid passing arguments to this method or setting global
	 * fields because it is executed in another thread.
	 * </p>
	 * <p>
	 * The right way to set a field variable is to return a value in the
	 * <code>produce</code>and retrieve it in the getResult().
	 * </p>
	 * 
	 * @return The implementing class can use this to return a result of data
	 *         production. The result will be available through the method
	 *         {@linkplain #getResult()}.
	 * @param sink the implementing class should write its data to this stream.
	 * @throws java.lang.Exception the exception eventually thrown by the
	 *                             implementing class is returned by the
	 *                             {@linkplain #read()} methods.
	 * @see #getResult()
	 */
	protected abstract T produce(final Writer sink) throws Exception;

	/** {@inheritDoc} */
	@Override
	public final int read() throws IOException {
		checkInitialized();
		final int result = this.pipedReader.read();
		if (result < 0) {
			checkException();
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public final int read(final char[] b, final int off, final int len) throws IOException {
		checkInitialized();
		final int result = this.pipedReader.read(b, off, len);
		if (result < 0) {
			checkException();
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public long skip(long n) throws IOException {
		checkInitialized();
		return super.skip(n);
	}

}
