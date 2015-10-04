package squidpony.performance;

import java.util.concurrent.Callable;

/**
 * 
 * single unit of work intended to be used in a performance related test.</br>
 * provides a wrapper around {@link Callable} to return the time that the test
 * took to complete in ms
 * 
 * @author David Becker
 *
 */
abstract class AbstractPerformanceUnit implements Callable<Long> {

	@Override
	public Long call() throws Exception {
		final long timerStart = System.currentTimeMillis();
		doWork();
		return System.currentTimeMillis() - timerStart;
	}

	/**
	 * this method is intended to be overridden by clients to do the actual work
	 * of the test
	 */
	protected abstract void doWork();

}
