//
// (C) Copyright 2017-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.util

import java.net.NetworkInterface
import java.net.SocketException
import java.util.concurrent.atomic.AtomicLong

//---------------------------------------------------------------------------------------------------------------------

/**
 * Cross-platform implementation of UUID loosely based on OpenJDK UUID.
 */
class Uuid(

    /** The most significant 64 bits of this UUID. */
    private val mostSignificantBits: Long,

    /** The least significant 64 bits of this UUID. */
    private val leastSignificantBits: Long

) : Comparable<Uuid> {

    /**
     * Compares this UUID with the specified UUID. The first of two UUIDs is greater than the second if the most
     * significant field in which the UUIDs differ is greater for the first UUID.
     * @param  other `UUID` to which this `UUID` is to be compared
     * @return  -1, 0 or 1 as this `UUID` is less than, equal to, or greater than `other`
     */
    override operator fun compareTo(other: Uuid): Int {

        // The ordering is intentionally set up so that the UUIDs
        // can simply be numerically compared as two numbers
        return when {
            this.mostSignificantBits < other.mostSignificantBits -> -1
            this.mostSignificantBits > other.mostSignificantBits -> 1
            this.leastSignificantBits < other.leastSignificantBits -> -1
            this.leastSignificantBits > other.leastSignificantBits -> 1
            else -> 0
        }

    }

    /**
     * Compares this object to the specified object.  The result is `true` if and only if the argument is not `null`, is a `UUID`
     * object, has the same variant, and contains the same value, bit for bit,
     * as this `UUID`.
     * @param  other The object to be compared
     * @return  `true` if the objects are the same; `false` otherwise
     */
    override fun equals(other: Any?): Boolean {

        if (null == other || other !is Uuid) {
            return false
        }

        return mostSignificantBits == other.mostSignificantBits && leastSignificantBits == other.leastSignificantBits

    }

    fun hasNextInReservedBlock(): Boolean {

        return this.mostSignificantBits and 0x000000FF00000000L < 0xFF00000000

    }

    fun nextInReservedBlock(): Uuid {

        check(hasNextInReservedBlock()) { "Reserved block of UUIDs exhausted." }

        return Uuid(this.mostSignificantBits + 0x100000000L, this.leastSignificantBits)

    }

    /**
     * Returns a hash code for this `UUID`.
     * @return  A hash code value for this `UUID`
     */
    override fun hashCode(): Int {
        val hilo = mostSignificantBits xor leastSignificantBits
        return (hilo shr 32).toInt() xor hilo.toInt()
    }

    /**
     * Returns a `String` object representing this `UUID`.
     *
     * The UUID string representation is as described by this BNF:
     * UUID                   = <time_low> "-" <time_mid> "-"
     * <time_high_and_version> "-"
     * <variant_and_sequence> "-"
     * <node>
     * time_low               = 4*<hexOctet>
     * time_mid               = 2*<hexOctet>
     * time_high_and_version  = 2*<hexOctet>
     * variant_and_sequence   = 2*<hexOctet>
     * node                   = 6*<hexOctet>
     * hexOctet               = <hexDigit><hexDigit>
     * hexDigit               =
     * "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
     * | "a" | "b" | "c" | "d" | "e" | "f"
     * | "A" | "B" | "C" | "D" | "E" | "F"
     * @return  A string representation of this `UUID`
     */
    override fun toString(): String {

        /** Returns val represented by the specified number of hex digits.  */
        fun digits(value: Long, digits: Int): String {
            val hi = 1L shl digits * 4
            return longToHexString(hi or (value and hi - 1)).substring(1)
        }

        return digits(mostSignificantBits shr 32, 8) + "-" +
                digits(mostSignificantBits shr 16, 4) + "-" +
                digits(mostSignificantBits, 4) + "-" +
                digits(leastSignificantBits shr 48, 4) + "-" +
                digits(leastSignificantBits, 12)

    }


    companion object {

        /**
         * Creates a `UUID` from the string standard representation as described in the [.toString] method.
         * @param uuidStr A string that specifies a `UUID`.
         * @return  A `UUID` with the specified value.
         * @throws  IllegalArgumentException If name does not conform to the string representation as
         *          described in [.toString]
         */
        fun fromString(uuidStr: String): Uuid {

            val components = uuidStr.split("-".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()

            if (components.size != 5) {
                throw IllegalArgumentException("Invalid UUID string: " + uuidStr)
            }

            var mostSigBits = components[0].toLong(16)
            mostSigBits = mostSigBits shl 16
            mostSigBits = mostSigBits or components[1].toLong(16)
            mostSigBits = mostSigBits shl 16
            mostSigBits = mostSigBits or components[2].toLong(16)

            var leastSigBits = components[3].toLong(16)
            leastSigBits = leastSigBits shl 48
            leastSigBits = leastSigBits or components[4].toLong(16)

            return Uuid(mostSigBits, leastSigBits)

        }

        /**
         * Makes a version 1 UUID.
         * @return the new UUID.
         */
        fun make(): Uuid {
            return Uuid(getNextTimeAndVersion(false), CLOCK_SEQ_AND_NODE)
        }

        /**
         * Makes a UUID with a block of 256 sequential UUIDs reserved. The next UUID returned by this generator will be
         * different in its first three bytes, so a remote client can safely create up to 256 UUIDs from the given one by
         * incrementing only the fourth byte. The fourth byte will be 0x00.
         * @return the new UUID.
         */
        fun makeWithReservedBlock(): Uuid {
            return Uuid(getNextTimeAndVersion(true), CLOCK_SEQ_AND_NODE)
        }

        ////

        /**
         * Converts a long to a hex string.
         */
        private fun longToHexString(value: Long): String {
            return java.lang.Long.toHexString(value)
        }


        /**
         * Computes a clock sequence and node value.
         *
         * @return the low-order 64 bits of the UUID
         */
        private fun determineClockSeqAndNode(): Long {

            // node
            val macAddress: ByteArray = determineMacAddress()
            var result = 0xFFL and macAddress[5].toLong()
            result = result or (0xFFL and macAddress[4].toLong() shl 8)
            result = result or (0xFFL and macAddress[3].toLong() shl 16)
            result = result or (0xFFL and macAddress[2].toLong() shl 24)
            result = result or (0xFFL and macAddress[1].toLong() shl 32)
            result = result or (0xFFL and macAddress[0].toLong() shl 40)

            // clock sequence - TODO: currently random; store & retrieve a value instead
            result = result or ((Math.random() * 0x3FFF.toDouble()).toLong() shl 48)

            // reserved bits
            result = result or Long.MIN_VALUE

            return result
        }

        /**
         * Determines the MAC address of the host.
         *
         * @return six bytes of the MAC address
         */
        private fun determineMacAddress(): ByteArray {
            try {
                val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                if (networkInterfaces != null) {
                    while (networkInterfaces.hasMoreElements()) {
                        val networkInterface = networkInterfaces.nextElement()
                        val result = networkInterface.hardwareAddress
                        if (result != null && result.size == 6 && result[1] != 0xff.toByte()) {
                            return result
                        }
                    }
                }
            } catch (ex: SocketException) {
                // ignore
            }
            return byteArrayOf(0.toByte(), 1.toByte(), 2.toByte(), 3.toByte(), 4.toByte(), 5.toByte())
        }

        /**
         * Computes the next time field from the current system time plus any counter increment needed.
         *
         * @param reservedTimeBlock If true, ensure that the next time returned after this one will be 256 * 100ns later
         *
         * @return the high 64 bits of the next UUID (version 1)
         */
        private fun getNextTimeAndVersion(reservedTimeBlock: Boolean): Long {

            // retrieve system time (UTC)
            val timeMs = System.currentTimeMillis()

            // convert to 100ns units
            var time100ns = timeMs * 10000L

            // convert to UUID time (from Gregorian start)
            time100ns += 0x01B21DD213814000L

            // get the next time value to use, ensuring uniqueness and reserving 256 values when required
            while (true) {
                // for blocks of UUIDs use 00 as last byte after rounding up
                if (reservedTimeBlock) {
                    time100ns += 0xFFL
                    time100ns = time100ns and -0x100L
                }
                val last: Long = prevTime100ns.get()
                if (time100ns > last) {
                    if (reservedTimeBlock) {
                        if (prevTime100ns.compareAndSet(
                                last,
                                time100ns + 0xFFL
                            )
                        ) {
                            break
                        }
                    } else if (prevTime100ns.compareAndSet(last, time100ns)) {
                        break
                    }
                } else {
                    // go around again with a time bigger than last
                    time100ns = last + 1L
                }
            }

            // time low
            var result = time100ns shl 32

            // time mid
            result = result or (time100ns and 0xFFFF00000000L shr 16)

            // time hi and version 1
            result = result or (time100ns shr 48 and 0x0FFFL)

            // version 1
            result = result or 0x1000L
            return result
        }

        /**
         * The clock sequence and node value.
         */
        private val CLOCK_SEQ_AND_NODE: Long = determineClockSeqAndNode()

        /**
         * The last used time value. Tracks time but with atomic increments when needed to avoid duplicates.
         */
        private val prevTime100ns = AtomicLong(Long.MIN_VALUE)

    }

}

//---------------------------------------------------------------------------------------------------------------------
