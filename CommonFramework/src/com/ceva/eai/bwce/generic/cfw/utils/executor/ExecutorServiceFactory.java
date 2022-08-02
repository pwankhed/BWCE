package com.ceva.eai.bwce.generic.cfw.utils.executor;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible of instantiating the right executor given an
 * ExecutionModel.
 */
public final class ExecutorServiceFactory {

	public enum ExecutionModel {
		/**
		 * <p>
		 * Only one thread is shared by all instances (slow).
		 * </p>
		 */
		SINGLE_THREAD,
		/**
		 * <p>
		 * Threads are taken from a static pool.
		 * </p>
		 * <p>
		 * Some slow thread might lock up the pool and other processes might be slowed
		 * down.
		 * </p>
		 * 
		 * @see java.util.concurrent.ThreadPoolExecutor
		 */

		STATIC_THREAD_POOL,
		/**
		 * <p>
		 * One thread per instance of class. Slow but each instance can work in
		 * isolation. Also if some thread is not correctly closed there might be threads
		 * leaks.
		 * </p>
		 */
		THREAD_PER_INSTANCE
	}

	/**
	 * Executor that stops after a single execution. Used when a thread per instance
	 * strategy is requested.
	 */
	private static class OneShotThreadExecutor extends AbstractExecutorService {
		private final ExecutorService exec = Executors.newSingleThreadExecutor();

		@Override
		public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
			return this.exec.awaitTermination(timeout, unit);
		}

		@Override
		public void execute(final Runnable command) {
			this.exec.execute(command);
			shutdown();
		}

		@Override
		public boolean isShutdown() {
			return this.exec.isShutdown();
		}

		@Override
		public boolean isTerminated() {
			return this.exec.isTerminated();
		}

		@Override
		public void shutdown() {
			this.exec.shutdown();
		}

		@Override
		public List<Runnable> shutdownNow() {
			return this.exec.shutdownNow();
		}

		@Override
		public <T> Future<T> submit(final Callable<T> task) {
			final Future<T> result = this.exec.submit(task);
			shutdown();
			return result;
		}

	}

	/*
	 * Default should be 0 otherwise there are problems stopping application
	 * servers.
	 */
	private static ExecutorService executor = new ThreadPoolExecutor(0, 20, 5, TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>(500));

	private static final ExecutorService SINGLE_EXECUTOR = Executors.newSingleThreadExecutor();

	/**
	 * <p>
	 * Getter for the field <code>executor</code>.
	 * </p>
	 *
	 * @param tmodel a {@link com.gc.iotools.stream.base.ExecutionModel} object.
	 * @return a {@link java.util.concurrent.ExecutorService} object.
	 */
	public static ExecutorService getExecutor(final ExecutionModel tmodel) {
		final ExecutorService result;
		switch (tmodel) {
		case THREAD_PER_INSTANCE:
			result = new OneShotThreadExecutor();
			break;
		case STATIC_THREAD_POOL:
			result = ExecutorServiceFactory.executor;
			break;
		case SINGLE_THREAD:
			result = ExecutorServiceFactory.SINGLE_EXECUTOR;
			break;

		default:
			throw new UnsupportedOperationException("ExecutionModel [" + tmodel + "] not supported");
		}
		return result;
	}

	/**
	 * <p>
	 * Call this method to initialize the <code>ExecutorService</code> that is used
	 * in <code>STATIC_THREAD_POOL</code> execution mode.
	 * </p>
	 *
	 * @see ExecutionModel#STATIC_THREAD_POOL
	 * @see #setDefaultThreadPoolExecutor(ExecutorService)
	 */
	public static void init() {
		setDefaultThreadPoolExecutor(
				new ThreadPoolExecutor(0, 20, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(500)));
	}

	/**
	 * <p>
	 * Sets the default ExecutorService returned when this class is invoked with
	 * {@link ExecutionModel#STATIC_THREAD_POOL}.
	 * </p>
	 * <p>
	 * It can also be used to initialize the class (for instance for use into a web
	 * application).
	 * </p>
	 *
	 * @param executor ExecutorService for the STATIC_THREAD_POOL model.
	 */
	public static void setDefaultThreadPoolExecutor(final ExecutorService executor) {
		ExecutorServiceFactory.executor = executor;
	}

	/**
	 * <p>
	 * Call this method to finalize the execution queue.
	 * </p>
	 * <p>
	 * It is mandatory when you use this library in a container, otherwise the
	 * container doesn't terminate gracefully (for instance you can call it in a web
	 * application context listener <code>ContextListener.shutdown()</code> ).
	 * </p>
	 */
	public static void shutDown() {
		executor.shutdown();
	}

	/**
	 * Users should not instantiate this class directly.
	 */
	private ExecutorServiceFactory() {
		// don't instantiate
	}
}
