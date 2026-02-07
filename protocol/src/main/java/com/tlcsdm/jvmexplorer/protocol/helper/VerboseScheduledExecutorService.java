package com.tlcsdm.jvmexplorer.protocol.helper;

import com.esotericsoftware.minlog.Log;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// By default, an executor service will swallow exceptions. It's pretty annoying when debugging if you ignore the Future
// so this will log every exception.
public class VerboseScheduledExecutorService implements ScheduledExecutorService {

	private final ScheduledExecutorService executor;

	public VerboseScheduledExecutorService(ScheduledExecutorService executor) {
		this.executor = executor;
	}

	@Override
	public void shutdown() {
		this.executor.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return this.executor.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return this.executor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return this.executor.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return this.executor.awaitTermination(timeout, unit);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return this.executor.submit(new VerboseCallable<>(task));
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return this.executor.submit(new VerboseRunnable(task), result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return this.executor.submit(new VerboseRunnable(task));
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return this.executor.invokeAll(wrapCallables(tasks));
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return this.executor.invokeAll(wrapCallables(tasks), timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return this.executor.invokeAny(wrapCallables(tasks));
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return this.executor.invokeAny(wrapCallables(tasks), timeout, unit);
	}

	private <T> Collection<VerboseCallable<T>> wrapCallables(Collection<? extends Callable<T>> tasks) {
		return tasks.stream().map(VerboseCallable::new).collect(java.util.stream.Collectors.toList());
	}

	@Override
	public void execute(Runnable command) {
		this.executor.execute(new VerboseRunnable(command));
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return this.executor.schedule(new VerboseRunnable(command), delay, unit);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		return this.executor.schedule(new VerboseCallable<>(callable), delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return this.executor.scheduleAtFixedRate(new VerboseRunnable(command), initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return this.executor.scheduleWithFixedDelay(new VerboseRunnable(command), initialDelay, delay, unit);
	}

		private static class VerboseRunnable implements Runnable {
		private final Runnable runnable;

		public VerboseRunnable(Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		public void run() {
			try {
				runnable.run();
			}
			catch (Throwable throwable) {

				// Bug with either RichTextFX, or ReactFX, not sure. But the async stream used for highlighting
				// tries to do some stuff off the JavaFX thread
				final StackTraceElement[] stackTrace = throwable.getStackTrace();
				if (stackTrace != null && stackTrace.length >= 2 && stackTrace[1].getClassName()
				                                                                 .equals("javafx.concurrent.Task")
				    && stackTrace[1].getMethodName().equals("addEventHandler")) {
					// Return, don't print or anything. We (unfortunately) know this happens.
					return;
				}

				Log.warn("Exception thrown in executor task", throwable);
				throw throwable;
			}
		}
	}

	private static class VerboseCallable<V> implements Callable<V> {
		private final Callable<V> callable;

		public VerboseCallable(Callable<V> callable) {
			this.callable = callable;
		}

		@Override
		public V call() throws Exception {
			try {
				return callable.call();
			}
			catch (Throwable throwable) {
				Log.warn("Exception thrown in executor callable task", throwable);
				throw throwable;
			}
		}
	}

}
