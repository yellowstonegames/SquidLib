package squidpony.performance;

import java.util.concurrent.ExecutionException;

/**
 * runs the predefined performance tests<br/>
 * TODO convert into Mojo to be invoked from Maven
 * 
 * @author David Becker
 *
 */
public class PerfTest {

	// TODO add more tests when appropriate here
	private static final AbstractPerformanceTest[] tests = new AbstractPerformanceTest[] {
			new DijkstraPerformanceTest(), new FOVLOSPerformanceTest() };

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		for (AbstractPerformanceTest test : tests) {
			System.out.println("*** start test " + test.getClass().getSimpleName() + " ***");
			test.invokeThreads();
			System.out.println("*** test finished ***");
		}
	}
}
