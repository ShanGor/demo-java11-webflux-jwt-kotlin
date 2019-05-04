import java.math.MathContext
import groovy.time.TimeCategory

import java.math.RoundingMode

/**
 * Decimal Operator overloading. To make all response to be big decimal
 */

def round(final Number num, final int precision, final RoundingMode roundingMode=RoundingMode.HALF_UP) {
  return (num as BigDecimal).setScale(precision, roundingMode)
}

public class DecimalCatetory {
  public static BigDecimal round(final BigDecimal left, final int precision, final RoundingMode roundingMode=RoundingMode.HALF_UP) {
    return left.setScale(precision, roundingMode)
  }

  public static BigDecimal plus(final Double left, final BigDecimal right) {
    return (left as BigDecimal).add(right)
  }

  public static BigDecimal plus(final BigDecimal left, final Double right) {
    return left.add(right as BigDecimal)
  }

  public static BigDecimal minus(final Double left, final BigDecimal right) {
    return right.negate().add(left as BigDecimal)
  }

  public static BigDecimal minus(final BigDecimal left, final Double right) {
    return left.add((right as BigDecimal).negate())
  }

  public static BigDecimal multiply(final Double left, final BigDecimal right) {
    return new BigDecimal(left).multiply(right)
  }

  public static BigDecimal multiply(final BigDecimal left, final Double right) {
    return left.multiply(right as BigDecimal)
  }

  public static BigDecimal div(final Double left, final BigDecimal right) {
    return (left as BigDecimal).divide(right, MathContext.DECIMAL128)
  }

  public static BigDecimal div(final BigDecimal left, final Double right) {
    return left.divide((right as BigDecimal), MathContext.DECIMAL128)
  }

  public static BigDecimal power(final Number left, final Number right) {
    return Math.pow(left as Double, right as Double) as BigDecimal
  }

  public static BigDecimal plus(final Double left, final BigInteger right) {
    return (left as BigDecimal).add(right as BigDecimal)
  }

  public static BigDecimal plus(final BigInteger left, final Double right) {
    return (left as BigDecimal).add(right as BigDecimal)
  }

  public static BigDecimal minus(final Double left, final BigInteger right) {
    return (right as BigDecimal).negate().add(left as BigDecimal)
  }

  public static BigDecimal minus(final BigInteger left, final Double right) {
    return (left as BigDecimal).add((right as BigDecimal).negate())
  }

  public static BigDecimal multiply(final Double left, final BigInteger right) {
    return new BigDecimal(left).multiply(right as BigDecimal)
  }

  public static BigDecimal multiply(final BigInteger left, final Double right) {
    return (left as BigDecimal).multiply(right as BigDecimal)
  }

  public static BigDecimal div(final Double left, final BigInteger right) {
    return (left as BigDecimal).divide(right as BigDecimal, MathContext.DECIMAL128)
  }

  public static BigDecimal div(final BigInteger left, final Double right) {
    return (left as BigDecimal).divide((right as BigDecimal), MathContext.DECIMAL128)
  }
}

Double x=10.2
assert((x+1.0g).class == java.lang.Double)
assert((x-1.0g).class == java.lang.Double)
assert((1.0g-x).class == java.lang.Double)
assert((x*1.0g).class == java.lang.Double)
assert((x/1.0g).class == java.lang.Double)
assert((x ** 2.0g).class == java.lang.Double)
assert((2.0g ** 2).class == java.math.BigDecimal)
println("Asserts for original successfully")

use(DecimalCatetory, TimeCategory) {
  start = new Date()
  // Test BigDecimal with double
  assert((x + 2.0).class == java.math.BigDecimal)
  assert((3.0 + x).class == java.math.BigDecimal)

  assert((3.0g - x).class == java.math.BigDecimal)
  assert((x - 3.0).class == java.math.BigDecimal)

  assert((3.0g * x).class == java.math.BigDecimal)
  assert((x * 3.0).class == java.math.BigDecimal)

  assert((3.0g / x).class == java.math.BigDecimal)
  assert((x / 3.0).class == java.math.BigDecimal)

  x = 0.3
  assert((3.0g ** x).class == java.math.BigDecimal)
  assert((x ** 3.0).class == java.math.BigDecimal)
  assert((3.0g ** (new Integer(2))).class == java.math.BigDecimal)

  // Test BigInteger with double
  assert((1g + x).class == java.math.BigDecimal)
  assert((x + 1g).class == java.math.BigDecimal)
  assert((1g - x).class == java.math.BigDecimal)
  assert((x - 1g).class == java.math.BigDecimal)
  assert((1g * x).class == java.math.BigDecimal)
  assert((x * 1g).class == java.math.BigDecimal)
  assert((1g / x).class == java.math.BigDecimal)
  assert((x / 1g).class == java.math.BigDecimal)
  assert((1g ** x).class == java.math.BigDecimal)
  assert((x ** 1g).class == java.math.BigDecimal)

  // Test BigInteger Values
  x = 102.3245
  assert((1g + x).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("103.325"))
  assert((x + 1g).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("103.325"))
  assert((1g - x).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("-101.325"))
  assert((x - 1g).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("101.325"))
  assert((2g * x).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("204.649"))
  assert((x * 2g).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("204.649"))
  assert((1g / x).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("0.010"))
  assert((x / 2g).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("51.162"))
  assert((1g ** x).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("1"))
  assert((x ** 1g).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("102.325"))
  assert((x ** 2g).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("10470.303"))


  // Test BigDecimal Values
  x = 102.3245
  assert((1.0g + x).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("103.325"))
  assert((x + 1.0g).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("103.325"))
  assert((1.0g - x).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("-101.325"))
  assert((x - 1.0g).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("101.325"))
  assert((2.0g * x).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("204.649"))
  assert((x * 2.0g).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("204.649"))
  assert((1.0g / x).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("0.010"))
  assert((x / 2.0g).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("51.162"))
  assert((1.0g ** x).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("1"))
  assert((x ** 1.0g).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("102.325"))
  assert((x ** 2.0g).setScale(3, BigDecimal.ROUND_HALF_UP) == new BigDecimal("10470.303"))


  assert round(11.234, 2) == new BigDecimal("11.23")

  end = new Date()
  println (end - start)
}

println("All asserts for DecimalCatetory successfully!")
