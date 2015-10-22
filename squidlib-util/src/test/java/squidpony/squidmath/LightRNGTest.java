package squidpony.squidmath;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.categories.Categories.ExcludeCategory;

/**
 * test to check if {@link LightRNG} returns predictable results
 * 
 * @author David Becker
 *
 */
public class LightRNGTest {
	LightRNG l = new LightRNG();

	@Test
	public void testNextInt() {
		LightRNG rng = new LightRNG(1L);
		assertEquals(1, rng.next(1));
		assertEquals(3, rng.next(2));
		assertEquals(6, rng.next(3));
		assertEquals(11, rng.next(4));
		assertEquals(1, rng.next(1));
		assertEquals(-1877671296, rng.nextInt());
		rng.setState(rng.state * 11234L);
		assertEquals(101, rng.nextInt(10, 230));
	}

	@Test
	public void testNextLong() {
		LightRNG rng = new LightRNG(2L);
        assertEquals(-7541218347953203506L, rng.nextLong());
        assertEquals(-4627371582388691390L, rng.nextLong());
		assertEquals(75L, rng.nextLong(100));
		assertEquals(-4327252827158612380L, rng.nextLong());
		assertEquals(5747796768693156649L, rng.nextLong());
		assertEquals(6394052312532759219L, rng.nextLong());
		assertEquals(5L, rng.nextLong(1, 10));
        assertEquals(77L, rng.nextLong(100));
        assertEquals(19L, rng.nextLong(100));
        assertEquals(66L, rng.nextLong(100));
        assertEquals(14L, rng.nextLong(100));
        assertEquals(7L, rng.nextLong(100));
    }

	@Test
	public void testNextDouble() {
		l.state = 2L;
		assertEquals(0.756575637666822, l.nextDouble(), 1.0 / 1000.0);
		assertEquals(25.855257359302918, l.nextDouble(100), 1.0 / 1000.0);
	}

	@Test
	public void testNextFloat() {
		l.setSeed(1L);
		l.skip(5L);
		assertEquals(0.0820694, l.nextFloat(), 1.0 / 1000.0);
		assertEquals(0.21186286, l.nextFloat(), 1.0 / 1000.0);
		assertEquals(0.15438014, l.nextFloat(), 1.0 / 1000.0);
		assertEquals(0.4931283, l.nextFloat(), 1.0 / 1000.0);
		assertEquals(0.38048685, l.nextFloat(), 1.0 / 1000.0);
		assertEquals(0.33714873, l.nextFloat(), 1.0 / 1000.0);
	}

	@Test
	public void testNextBoolean() {
		l.setSeed(1L);
		assertEquals(1L, l.getState());
		assertTrue(l.nextBoolean());
		assertTrue(l.nextBoolean());
		assertFalse(l.nextBoolean());
		assertTrue(l.nextBoolean());
		assertTrue(l.nextBoolean());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNextIntException() {
		l.nextInt(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNextIntException2() {
		l.nextInt(2, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNextLongException() {
		l.nextLong(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNextLongException2() {
		l.nextLong(2, 1);
	}

}
