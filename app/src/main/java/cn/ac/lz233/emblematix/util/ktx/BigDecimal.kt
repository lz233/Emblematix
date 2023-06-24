package cn.ac.lz233.emblematix.util.ktx

import java.math.BigDecimal
import java.math.BigInteger

fun BigDecimal.gcd(denominator: BigDecimal): BigDecimal {
    var b1 = denominator
    var a1 = this
    while (b1 != BigDecimal.ZERO) {
        val t = b1
        b1 = a1 % b1
        a1 = t
    }
    return a1.abs()
}