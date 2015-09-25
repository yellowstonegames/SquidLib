package squidpony.squidmath;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.categories.Categories.ExcludeCategory;

import junit.framework.Assert;

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
		assertEquals(0, rng.next(1));
		assertEquals(3, rng.next(2));
		assertEquals(1, rng.next(3));
		assertEquals(11, rng.next(4));
		assertEquals(1, rng.next(1));
		assertEquals(1797366200, rng.nextInt());
		rng.setState(rng.state * 11234L);
		assertEquals(174, rng.nextInt(10, 230));
	}

	@Test
	public void testNextLong() {
		LightRNG rng = new LightRNG(2L);
		assertEquals(1857867792895824164L, rng.nextLong());
		assertEquals(670810732413913090L, rng.nextLong());
		assertEquals(0, rng.nextLong(100));
		assertEquals(5850027601512104100L, rng.nextLong());
		assertEquals(6973757869230453225L, rng.nextLong());
		assertEquals(7841283713040272465L, rng.nextLong());
		assertEquals(2, rng.nextLong(1, 10));
		assertEquals(22, rng.nextLong(100));
		assertEquals(38, rng.nextLong(100));
		assertEquals(58, rng.nextLong(100));
		assertEquals(39, rng.nextLong(100));
		assertEquals(7, rng.nextLong(100));
	}

	@Test
	public void testNextDouble() {
		l.state = 2L;
		assertEquals(0.26476003824658223d, l.nextDouble(), 1 / 1000);
		assertEquals(47.49520291591127, l.nextDouble(100), 1 / 1000);
	}

	@Test
	public void testNextFloat() {
		l.setSeed(1L);
		l.skip(5L);
		assertEquals(0.13137388229370117, l.nextFloat(), 1 / 1000);
		assertEquals(0.21186286211013794, l.nextFloat(), 1 / 1000);
		assertEquals(0.8315069079399109, l.nextFloat(), 1 / 1000);
		assertEquals(0.12255960702896118, l.nextFloat(), 1 / 1000);
		assertEquals(0.3804868459701538, l.nextFloat(), 1 / 1000);
		assertEquals(0.9763892889022827, l.nextFloat(), 1 / 1000);
	}

	@Test
	public void testNextBoolean() {
		l.setSeed(1L);
		assertEquals(1L, l.getState());
		assertFalse(l.nextBoolean());
		assertTrue(l.nextBoolean());
		assertTrue(l.nextBoolean());
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
