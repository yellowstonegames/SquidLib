package squidpony.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;
import squidpony.squidmath.RandomnessSource;

/**
 * super class for performance related tests.<br>
 * 
 * @author David Becker
 *
 */
abstract class AbstractPerformanceTest {
	// we want predictable outcome for our test
	protected static final RandomnessSource SOURCE = new LightRNG(0x1337BEEF);
	protected static final RNG RNG = new RNG(SOURCE);

	protected static final int NUM_THREADS = 8;
	protected static final int NUM_TASKS = 100;

	protected final List<AbstractPerformanceUnit> tasks = new ArrayList<AbstractPerformanceUnit>();

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

		for (Future<Long> future : invoke) {
			System.out.println(future.get());
		}
	}

}
