package me.celestialfault.sweatbridge

import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Utility which provides a method to determine an amount of time to wait before attempting an action again,
 * getting exponentially longer each time.
 *
 * @param base The base amount of time to wait; the first use of [ExponentialBackoff.delay] will always be this long
 * @param max  The max amount of invocations before this backoff reaches its maximum delay
 */
// TODO convert to using Duration?
@Suppress("MemberVisibilityCanBePrivate")
class ExponentialBackoff(private val base: Long = 500L, private val max: Int = 10) {
	var count: Int = 0
		private set
	private var last: Long = 0

	/**
	 * A [Double] representing an exact amount of milliseconds to wait before attempting the requested action again.
	 *
	 * @see [delay]
	 */
	val exactDelay: Double
		get() {
			val current = System.currentTimeMillis()
			val resetAfter: Long = (base * (2.toDouble()).pow(7.5)).toLong()
			if(last + resetAfter < current) {
				count = 0
			}
			last = current
			return base * min((++count).toDouble(), max.toDouble()).pow(2.0)
		}

	/**
	 * Get a [Long] representing how long you should wait before attempting a failed action again.
	 *
	 * The returned delay is determined by `base * min(count, max)^2`, with `count` resetting after `base * 2^7.5`.
	 *
	 * The returned delay will be slightly randomized within the last 20% of the delay time; if you want the exact
	 * length of time to wait, use [exactDelay] instead.
	 */
	val delay: Long
		get() {
			val delay = exactDelay / 1000
			return (Random.nextDouble(delay * 0.8, delay) * 1000).toLong()
		}
}
