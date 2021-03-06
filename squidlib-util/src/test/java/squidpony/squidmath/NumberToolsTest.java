package squidpony.squidmath;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NumberToolsTest {
  
  public static float TOLERANCE = 0x1p-12f; // 1.0 / 4096.0
  public static float WEAK_TOLERANCE = 0x1.2p-10f; // 1.125 / 1024.0
  public static float DEGREE_TOLERANCE = 0.06294702339083752f; // WEAK_TOLERANCE converted to degrees

  public static double atan2_Math(double y, double x){
    final double a = Math.atan2(y, x) * (0.5 / Math.PI) + 1.0;
    return a - (int) a;
  }

  private static void assertEqualsWrapping(double expected, double actual, double delta){
    Assert.assertTrue((expected != expected) == (actual != actual) || Math.abs(expected - actual) < delta || Math.abs(expected - actual - 1.0) < delta);
  }
  
  @Test
  public void testAtan2() {
    assertEqualsWrapping(atan2_Math(Float.NaN, Float.NaN), NumberTools.atan2_(Float.NaN, Float.NaN), TOLERANCE);
    assertEqualsWrapping(atan2_Math(-0x1.b5ap+124f, Float.NaN), NumberTools.atan2_(-0x1.b5ap+124f, Float.NaN), TOLERANCE);
    assertEqualsWrapping(atan2_Math(-0x1p-129f, -0x1p-131f), NumberTools.atan2_(-0x1p-129f, -0x1p-131f), TOLERANCE);
    assertEqualsWrapping(atan2_Math(-0x1p-129f, -0x1.4p-129f), NumberTools.atan2_(-0x1p-129f, -0x1.4p-129f), TOLERANCE);
    assertEqualsWrapping(atan2_Math(-0x1p-527, -0x1.003fp+228), NumberTools.atan2_(-0x1p-527, -0x1.003fp+228), TOLERANCE);
    assertEqualsWrapping(atan2_Math(-0x1p-512, -0x1p-760), NumberTools.atan2_(-0x1p-512, -0x1p-760), TOLERANCE);
    assertEqualsWrapping(atan2_Math(-0x1p-512, 0x1.003fp+8), NumberTools.atan2_(-0x1p-512, 0x1.003fp+8), TOLERANCE);
    assertEqualsWrapping(atan2_Math(-0x1p-512, 0x1.003fp-1016), NumberTools.atan2_(-0x1p-512, 0x1.003fp-1016), TOLERANCE);
    assertEqualsWrapping(atan2_Math(-0x1p-146f, 0.0f), NumberTools.atan2_(-0x1p-146f, 0.0f), TOLERANCE);
    assertEqualsWrapping(atan2_Math(0x1.6p-126f, -0x1.4p-129f), NumberTools.atan2_(0x1.6p-126f, -0x1.4p-129f), TOLERANCE);
    assertEqualsWrapping(atan2_Math(0x1p-527, -0x1.003fp+228), NumberTools.atan2_(0x1p-527, -0x1.003fp+228), TOLERANCE);
    assertEqualsWrapping(atan2_Math(0x1p-783, -0x1.003fp-796), NumberTools.atan2_(0x1p-783, -0x1.003fp-796), TOLERANCE);
    assertEqualsWrapping(atan2_Math(0x1p-142f, 0.0f), NumberTools.atan2_(0x1p-142f, 0.0f), TOLERANCE);
    assertEqualsWrapping(atan2_Math(0x0.0000000000001p-1022, 0.0), NumberTools.atan2_(0x0.0000000000001p-1022, 0.0), TOLERANCE);
    assertEqualsWrapping(atan2_Math(0.0f, -0x1.800002p-1f), NumberTools.atan2_(0.0f, -0x1.800002p-1f), TOLERANCE);
    assertEqualsWrapping(atan2_Math(0.0f, 0.0f), NumberTools.atan2_(0.0f, 0.0f), TOLERANCE);
    assertEqualsWrapping(atan2_Math(0.0, 0.0), NumberTools.atan2_(0.0, 0.0), TOLERANCE);
    assertEquals(Math.atan2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), NumberTools.atan2(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), TOLERANCE);
    assertEquals(Math.atan2(Double.NEGATIVE_INFINITY, -0.25), NumberTools.atan2(Double.NEGATIVE_INFINITY, -0.25), TOLERANCE);
    assertEquals(Math.atan2(Double.NEGATIVE_INFINITY, 0.25), NumberTools.atan2(Double.NEGATIVE_INFINITY, 0.25), TOLERANCE);
    assertEquals(Math.atan2(-0x1.8p-1022, Double.NaN), NumberTools.atan2(-0x1.8p-1022, Double.NaN), TOLERANCE);
    assertEquals(Math.atan2(-0x1.3b755p+127f, Float.NaN), NumberTools.atan2(-0x1.3b755p+127f, Float.NaN), TOLERANCE);
    assertEquals(Math.atan2(-0x1p+1022, Double.NEGATIVE_INFINITY), NumberTools.atan2(-0x1p+1022, Double.NEGATIVE_INFINITY), TOLERANCE);
    assertEquals(Math.atan2(-0x1p-149f, -0x1.efffep-103f), NumberTools.atan2(-0x1p-149f, -0x1.efffep-103f), TOLERANCE);
    assertEquals(Math.atan2(-0x1.000008p-95f, -0x1p-95f), NumberTools.atan2(-0x1.000008p-95f, -0x1p-95f), TOLERANCE);
    assertEquals(Math.atan2(-0x1.000008p-95f, 0x1p-95f), NumberTools.atan2(-0x1.000008p-95f, 0x1p-95f), TOLERANCE);
    assertEquals(Math.atan2(0x1p-149f, -0x1.efffep-103f), NumberTools.atan2(0x1p-149f, -0x1.efffep-103f), TOLERANCE);
    assertEquals(Math.atan2(0x1.0000000000001p+1, -0x1.0000000000001p+0), NumberTools.atan2(0x1.0000000000001p+1, -0x1.0000000000001p+0), TOLERANCE);
    assertEquals(Math.atan2(0x1.c83714p-127f, -0x1.70d894p-127f), NumberTools.atan2(0x1.c83714p-127f, -0x1.70d894p-127f), TOLERANCE);
    assertEquals(Math.atan2(0x0.514ffac160359p-1022, -0x0.a316e51ade0c1p-1022), NumberTools.atan2(0x0.514ffac160359p-1022, -0x0.a316e51ade0c1p-1022), TOLERANCE);
    assertEquals(Math.atan2(0x1p-149f, 0.0f), NumberTools.atan2(0x1p-149f, 0.0f), TOLERANCE);
    assertEquals(Math.atan2(0x0.0000000000001p-1022, 0.0), NumberTools.atan2(0x0.0000000000001p-1022, 0.0), TOLERANCE);
    assertEquals(Math.atan2(0.0f, 0.0f), NumberTools.atan2(0.0f, 0.0f), TOLERANCE);
    assertEquals(Math.atan2(0.0, 0.0), NumberTools.atan2(0.0, 0.0), TOLERANCE);
  }
  
  @Test
  public void testDegrees() {
    assertEquals(Float.NEGATIVE_INFINITY, NumberTools.cosDegrees(-0x1.7a573cp+102f), TOLERANCE);
    assertEquals(Math.cos(Math.toRadians(-0x1.f6a936p+1f)), NumberTools.cosDegrees(-0x1.f6a936p+1f), WEAK_TOLERANCE);
    assertEquals(Math.cos(Math.toRadians(0x1.f03bda2455007p-7)), NumberTools.cosDegrees(0x1.f03bda2455007p-7f), WEAK_TOLERANCE);
    for (float f = 0f; f <= 360f; f += 0.01f) {
      assertEquals("Bad sin result at " + f, Math.sin(Math.toRadians(f)), NumberTools.sinDegrees(f), WEAK_TOLERANCE);
      assertEquals("Bad cos result at " + f, Math.cos(Math.toRadians(f)), NumberTools.cosDegrees(f), WEAK_TOLERANCE);
    }
    for (float y = -2f; y < 2f; y+= 0.01f) {
      for (float x = -2f; x < 2f; x+= 0.01f) {
//        if(!MathExtras.approxEquals(Math.toDegrees(Math.atan2(y, x)), NumberTools.atan2Degrees(y, x), DEGREE_TOLERANCE))
//        System.out.printf("Bad atan2Degrees result at x=%f, y=%f: Should be %f, is %f\n", x, y, Math.toDegrees(Math.atan2(y, x)), NumberTools.atan2Degrees(y, x));
        assertEquals("Bad atan2Degrees result at x=" + x + ",y=" + y, Math.toDegrees(Math.atan2(y, x)), NumberTools.atan2Degrees(y, x), DEGREE_TOLERANCE);
      }
    }
  }

  @Test
  public void testRadians() {
    assertEquals(Math.cos(-0x1.f6a936p+1f), NumberTools.cos(-0x1.f6a936p+1f), WEAK_TOLERANCE);
    assertEquals(Math.cos(0x1.f03bda2455007p-7), NumberTools.cos(0x1.f03bda2455007p-7), WEAK_TOLERANCE);
    for (float f = 0f; f <= 6.283185307179586f; f += 0.001f) {
      assertEquals("Bad sin result at " + f, Math.sin(f), NumberTools.sin(f), WEAK_TOLERANCE);
      assertEquals("Bad cos result at " + f, Math.cos(f), NumberTools.cos(f), WEAK_TOLERANCE);
    }

  }

  @Test
  public void testTurns() {
    assertEquals(Math.sin(0.2499999999999 * Math.PI * 2.0), NumberTools.sin_(0.2499999999999), WEAK_TOLERANCE);
    assertEquals(Math.cos(0.2499999999999 * Math.PI * 2.0), NumberTools.cos_(0.2499999999999), WEAK_TOLERANCE);

    assertEquals(Math.sin(-0.2499999999999 * Math.PI * 2.0), NumberTools.sin_(-0.2499999999999), WEAK_TOLERANCE);
    assertEquals(Math.cos(-0.2499999999999 * Math.PI * 2.0), NumberTools.cos_(-0.2499999999999), WEAK_TOLERANCE);
    
    assertEquals(Math.sin(Math.PI * -0.5), NumberTools.sin_(-0.25), WEAK_TOLERANCE);
    assertEquals(Math.cos(Math.PI * -0.5), NumberTools.cos_(-0.25), WEAK_TOLERANCE);
    for (float f = 0f; f <= 1f; f += 0.0001f) {
      assertEquals("Bad sin_ result at " + f, Math.sin(f * Math.PI * 2.0), NumberTools.sin_(f), WEAK_TOLERANCE);
      assertEquals("Bad cos_ result at " + f, Math.cos(f * Math.PI * 2.0), NumberTools.cos_(f), WEAK_TOLERANCE);
    }
  }
  @Test
  public void testInverseTrig() {
//    System.out.println(" 1.000: Should be " + Math.asin(1.0) + ", approx is " + NumberTools.asin(1.0));
//    System.out.println("-1.000: Should be " + Math.asin(-1.0) + ", approx is " + NumberTools.asin(-1.0));
//    System.out.println("-0.998: Should be " + Math.asin(-0.998) + ", approx is " + NumberTools.asin(-0.998));
//    System.out.println(" 0.998: Should be " + Math.asin(0.998) + ", approx is " + NumberTools.asin(0.998));
    for (double f = -1f; f <= 1f; f += 0.0001) {
      assertEquals("Bad asin result at " + f, Math.asin(f), NumberTools.asin(f), TOLERANCE);
      assertEquals("Bad acos result at " + f, Math.acos(f), NumberTools.acos(f), TOLERANCE);
    }
  }
  @Test
  public void testInverseTrigTurns() {
    for (float f = -1f; f <= 1f; f += 0.0001f) {
      assertEquals("Bad asin_ result at " + f, ((Math.asin(f) * 0.15915494309189535 + 1.0) % 1.0), NumberTools.asin_(f), TOLERANCE);
      assertEquals("Bad acos_ result at " + f, ((Math.acos(f) * 0.15915494309189535 + 1.0) % 1.0), NumberTools.acos_(f), TOLERANCE);
    }
  }

  @Test
  public void testSway() {
    assertEquals(Float.POSITIVE_INFINITY, NumberTools.sway(-0x1p+65f), TOLERANCE);
    assertEquals(-0x1.f82238p-1f, NumberTools.sway(0x1.e75b5ep+0f), TOLERANCE);
  }

//  @Test
//  public void testSwayRandomized() {
//    Assert.assertEquals(0x1.2ec44p-1f, NumberTools.swayRandomized(1_840_314_318, 0x1.bffffep+23f), TOLERANCE);
//  }

  @Test
  public void testZigzag() {
    assertEquals(0x1.cp-16f, NumberTools.zigzag(-0x1.bfff9p+1f), TOLERANCE);
    assertEquals(-1.0, NumberTools.zigzag(0x0.0918p-1022), TOLERANCE);
  }
}
