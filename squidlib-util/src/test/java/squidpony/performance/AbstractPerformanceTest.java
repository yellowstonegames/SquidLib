package squidpony.performance;

import squidpony.squidmath.LightRNG;
import squidpony.squidmath.StatefulRNG;
import squidpony.squidmath.StatefulRandomness;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * super class for performance related tests.<br>
 * 
 * @author David Becker
 *
 */
abstract class AbstractPerformanceTest {
	// we want predictable outcome for our test
	protected final StatefulRandomness SOURCE = new LightRNG(0x1337BEEF);
	protected final StatefulRNG RNG = new StatefulRNG(SOURCE);

	protected static final int NUM_THREADS = 1;
	protected static final int NUM_TASKS = 32;

	protected final List<AbstractPerformanceUnit> tasks = new ArrayList<>();

	/**
	 * this methods should be implemented by children to create one single unit
	 * of work that should later be scheduled
	 * 
	 * @return work unit
	 */
	protected abstract AbstractPerformanceUnit createWorkUnit();

	/**
	 * creates the list of threads that should be invoked later.
	 */
	protected void createThreadList() {
		for (int i = 0; i < NUM_TASKS; i++) {
			tasks.add(createWorkUnit());
		}
	}

	/**
	 * invokes the prepared background threads and outputs the time that each
	 * thread took to {@link System#out}
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void invokeThreads() throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
		System.out.println("invoking " + NUM_TASKS + " tasks on " + NUM_THREADS + " threads");
		final List<Future<Long>> invoke = executor.invokeAll(tasks);
		AtomicLong atom = new AtomicLong(0);
		for (Future<Long> future : invoke) {
			try {
				atom.getAndAdd(future.get(120, TimeUnit.SECONDS));
			} catch (TimeoutException e) {
				System.out.println("Task timed out after 120 seconds!");
			}
		}
		System.out.println("Task took " + atom.doubleValue() / NUM_TASKS + " ms on average");
		executor.shutdown();
	}

}
