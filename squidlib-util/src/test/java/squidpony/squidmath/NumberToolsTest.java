package squidpony.squidmath;

import org.junit.Assert;
import org.junit.Test;

public class NumberToolsTest {

  @Test
  public void testAtan2() {
    Assert.assertEquals(Float.NaN, NumberTools.atan2_(Float.NaN, Float.NaN), 0.0f);
    Assert.assertEquals(Float.NaN, NumberTools.atan2_(-0x1.b5ap+124f, Float.NaN), 0.0f);
    Assert.assertEquals(0x1.6c086ap-1f, NumberTools.atan2_(-0x1p-129f, -0x1p-131f), 0.0f);
    Assert.assertEquals(0x1.36faf8p-1f, NumberTools.atan2_(-0x1p-129f, -0x1.4p-129f), 0.0f);
    Assert.assertEquals(0.5, NumberTools.atan2_(-0x1p-527, -0x1.003fp+228), 0.0);
    Assert.assertEquals(0.75, NumberTools.atan2_(-0x1p-512, -0x1p-760), 0.0);
    Assert.assertEquals(1.0, NumberTools.atan2_(-0x1p-512, 0x1.003fp+8), 0.0);
    Assert.assertEquals(0.75, NumberTools.atan2_(-0x1p-512, 0x1.003fp-1016), 0.0);
    Assert.assertEquals(0.75f, NumberTools.atan2_(-0x1p-146f, 0.0f), 0.0f);
    Assert.assertEquals(0x1.127126p-2f, NumberTools.atan2_(0x1.6p-126f, -0x1.4p-129f), 0.0f);
    Assert.assertEquals(0.5, NumberTools.atan2_(0x1p-527, -0x1.003fp+228), 0.0);
    Assert.assertEquals(0x1.0005190cf6422p-2, NumberTools.atan2_(0x1p-783, -0x1.003fp-796), 0.0);
    Assert.assertEquals(0.25f, NumberTools.atan2_(0x1p-142f, 0.0f), 0.0f);
    Assert.assertEquals(0.25, NumberTools.atan2_(0x0.0000000000001p-1022, 0.0), 0.0);
    Assert.assertEquals(0.5f, NumberTools.atan2_(0.0f, -0x1.800002p-1f), 0.0f);
    Assert.assertEquals(0.0f, NumberTools.atan2_(0.0f, 0.0f), 0.0f);
    Assert.assertEquals(0.0, NumberTools.atan2_(0.0, 0.0), 0.0);
    Assert.assertEquals(-0x1.921fb5fdd355p+0, NumberTools.atan2(Double.NEGATIVE_INFINITY, -0.25), 0.0);
    Assert.assertEquals(-0x1.921fb5fdd355p+0, NumberTools.atan2(Double.NEGATIVE_INFINITY, 0.25), 0.0);
    Assert.assertEquals(Double.NaN, NumberTools.atan2(-0x1.8p-1022, Double.NaN), 0.0);
    Assert.assertEquals(Float.NaN, NumberTools.atan2(-0x1.3b755p+127f, Float.NaN), 0.0f);
    Assert.assertEquals(-0x1.921fb5fdd355p+1, NumberTools.atan2(-0x1p+1022, Double.NEGATIVE_INFINITY), 0.0);
    Assert.assertEquals(-0x1.921fb6p+1f, NumberTools.atan2(-0x1p-149f, -0x1.efffep-103f), 0.0f);
    Assert.assertEquals(-0x1.2d911ep+1f, NumberTools.atan2(-0x1.000008p-95f, -0x1p-95f), 0.0f);
    Assert.assertEquals(-0x1.923a6p-1f, NumberTools.atan2(-0x1.000008p-95f, 0x1p-95f), 0.0f);
    Assert.assertEquals(0x1.921fb6p+1f, NumberTools.atan2(0x1p-149f, -0x1.efffep-103f), 0.0f);
    Assert.assertEquals(0x1.046925b3c875dp+1, NumberTools.atan2(0x1.0000000000001p+1, -0x1.0000000000001p+0), 0.0);
    Assert.assertEquals(0x1.201656p+1f, NumberTools.atan2(0x1.c83714p-127f, -0x1.70d894p-127f), 0.0f);
    Assert.assertEquals(0x1.56ebbb4a243b7p+1, NumberTools.atan2(0x0.514ffac160359p-1022, -0x0.a316e51ade0c1p-1022), 0.0);
    Assert.assertEquals(0x1.921fb6p+0f, NumberTools.atan2(0x1p-149f, 0.0f), 0.0f);
    Assert.assertEquals(0x1.921fb5fdd355p+0, NumberTools.atan2(0x0.0000000000001p-1022, 0.0), 0.0);
    Assert.assertEquals(0.0f, NumberTools.atan2(0.0f, 0.0f), 0.0f);
    Assert.assertEquals(0.0, NumberTools.atan2(0.0, 0.0), 0.0);
  }
  
  @Test
  public void testCosDegrees() {
    Assert.assertEquals(Float.NEGATIVE_INFINITY, NumberTools.cosDegrees(-0x1.7a573cp+102f), 0.0f);
  }

  @Test
  public void testRadians() {
    Assert.assertEquals(-0x1.6a61f2p-1f, NumberTools.cos(-0x1.f6a936p+1f), 0.0f);
    Assert.assertEquals(0x1.fff1139353f1cp-1, NumberTools.cos(0x1.f03bda2455007p-7), 0.0);
    for (float f = 0f; f <= 6.283185307179586f; f += 0.001f) {
      Assert.assertEquals("Bad sin result at " + f, Math.sin(f), NumberTools.sin(f), 0.0011);
      Assert.assertEquals("Bad cos result at " + f, Math.cos(f), NumberTools.cos(f), 0.0011);
    }

  }

  @Test
  public void testTurns() {
    Assert.assertEquals(Math.sin(0.2499999999999 * Math.PI * 2.0), NumberTools.sin_(0.2499999999999), 0.0011);
    Assert.assertEquals(Math.cos(0.2499999999999 * Math.PI * 2.0), NumberTools.cos_(0.2499999999999), 0.0011);

    Assert.assertEquals(Math.sin(-0.2499999999999 * Math.PI * 2.0), NumberTools.sin_(-0.2499999999999), 0.0011);
    Assert.assertEquals(Math.cos(-0.2499999999999 * Math.PI * 2.0), NumberTools.cos_(-0.2499999999999), 0.0011);
    
    Assert.assertEquals(Math.sin(Math.PI * -0.5), NumberTools.sin_(-0.25), 0.0011);
    Assert.assertEquals(Math.cos(Math.PI * -0.5), NumberTools.cos_(-0.25), 0.0011);
    for (float f = 0f; f <= 1f; f += 0.0001f) {
      Assert.assertEquals("Bad sin_ result at " + f, Math.sin(f * Math.PI * 2.0), NumberTools.sin_(f), 0.0011);
      Assert.assertEquals("Bad cos_ result at " + f, Math.cos(f * Math.PI * 2.0), NumberTools.cos_(f), 0.0011);
    }
  }
  @Test
  public void testInverseTrig() {
//    System.out.println(" 1.000: Should be " + Math.asin(1.0) + ", approx is " + NumberTools.asin(1.0));
//    System.out.println("-1.000: Should be " + Math.asin(-1.0) + ", approx is " + NumberTools.asin(-1.0));
//    System.out.println("-0.998: Should be " + Math.asin(-0.998) + ", approx is " + NumberTools.asin(-0.998));
//    System.out.println(" 0.998: Should be " + Math.asin(0.998) + ", approx is " + NumberTools.asin(0.998));
    for (double f = -1f; f <= 1f; f += 0.0001) {
      Assert.assertEquals("Bad asin result at " + f, Math.asin(f), NumberTools.asin(f), 0.001);
      Assert.assertEquals("Bad acos result at " + f, Math.acos(f), NumberTools.acos(f), 0.001);
    }
  }
  @Test
  public void testInverseTrigTurns() {
    for (float f = -1f; f <= 1f; f += 0.0001f) {
      Assert.assertEquals("Bad asin_ result at " + f, ((Math.asin(f) * 0.15915494309189535 + 1.0) % 1.0), NumberTools.asin_(f), 0.001);
      Assert.assertEquals("Bad acos_ result at " + f, ((Math.acos(f) * 0.15915494309189535 + 1.0) % 1.0), NumberTools.acos_(f), 0.001);
    }
  }

  @Test
  public void testSway() {
    Assert.assertEquals(Float.POSITIVE_INFINITY, NumberTools.sway(-0x1p+65f), 0.0f);
    Assert.assertEquals(-0x1.f82238p-1f, NumberTools.sway(0x1.e75b5ep+0f), 0.0f);
  }

//  @Test
//  public void testSwayRandomized() {
//    Assert.assertEquals(0x1.2ec44p-1f, NumberTools.swayRandomized(1_840_314_318, 0x1.bffffep+23f), 0.0f);
//  }

  @Test
  public void testZigzag() {
    Assert.assertEquals(0x1.cp-16f, NumberTools.zigzag(-0x1.bfff9p+1f), 0.0f);
    Assert.assertEquals(-1.0, NumberTools.zigzag(0x0.0918p-1022), 0.0);
  }
}
