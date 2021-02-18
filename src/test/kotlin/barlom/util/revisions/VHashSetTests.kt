//
// (C) Copyright 2017-2018 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.util.revisions

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

//---------------------------------------------------------------------------------------------------------------------

/**
 * Tests of class VHashSet.
 */
internal class VHashSetTests {

    @Test
    fun `A versioned hash set grows and shrinks with old versions preserved`() {

        val revHistory = RevisionHistory("Rev #0")

        var set0: VHashSet<Int>? = null

        // empty starting point
        revHistory.update(2) {
            set0 = VHashSet()
            "Rev #1"
        }

        val set: VHashSet<Int> = set0!!

        assertEquals(1, revHistory.lastRevision.revisionNumber)

        fun checkRev1() = revHistory.review(1) {
            assertEquals(0, set.size)
            assertEquals(0, set.asSet().size)
            assertTrue(set.isEmpty())
            assertFalse(set.isNotEmpty())
        }

        checkRev1()

        // add one item
        revHistory.update(2) {
            set.add(10)
            "Rev #2"
        }

        assertEquals(2, revHistory.lastRevision.revisionNumber)

        fun checkRev2() = revHistory.review(2) {
            assertEquals(1, set.size)
            assertEquals(1, set.asSet().size)
            assertTrue(set.asSet().contains(10))
            assertTrue(set.contains(10))
            assertFalse(set.isEmpty())
            assertTrue(set.isNotEmpty())
        }

        checkRev2()
        checkRev1()

        // add two more items
        revHistory.update(2) {
            set.add(20)
            set.add(30)
            "Rev #3"
        }

        assertEquals(3, revHistory.lastRevision.revisionNumber)

        fun checkRev3() = revHistory.review(3) {
            assertEquals(3, set.size)
            assertEquals(3, set.asSet().size)
            assertTrue(set.asSet().contains(10))
            assertTrue(set.asSet().contains(20))
            assertTrue(set.asSet().contains(30))
            assertTrue(set.contains(10))
            assertTrue(set.contains(20))
            assertTrue(set.contains(30))
            assertFalse(set.isEmpty())
            assertTrue(set.isNotEmpty())
        }

        checkRev3()
        checkRev2()
        checkRev1()

        // remove one; add one new and one duplicate
        revHistory.update(2) {
            set.remove(20)
            set.add(30)
            set.add(40)
            "Rev #4"
        }

        assertEquals(4, revHistory.lastRevision.revisionNumber)

        fun checkRev4() = revHistory.review(4) {

            assertEquals(3, set.size)
            assertTrue(set.contains(10))
            assertFalse(set.contains(20))
            assertTrue(set.contains(30))
            assertTrue(set.contains(40))
            assertFalse(set.isEmpty())
            assertTrue(set.isNotEmpty())

            var count = 0
            set.forEach { count += 1 }

            assertEquals(3, count)

            val set3 = set.map { item -> 10 * item }

            assertEquals(3, set3.size)
            assertTrue(set3.contains(400))
            assertTrue(set3.contains(300))
            assertTrue(set3.contains(100))

        }

        checkRev4()
        checkRev3()
        checkRev2()
        checkRev1()

        // remove the first item
        revHistory.update(2) {

            set.remove(40)
            "Rev #5"

        }

        assertEquals(5, revHistory.lastRevision.revisionNumber)

        fun checkRev5() = revHistory.review(5) {

            assertEquals(2, set.size)
            assertEquals(2, set.asSet().size)
            assertTrue(set.asSet().contains(10))
            assertFalse(set.asSet().contains(20))
            assertTrue(set.asSet().contains(30))
            assertFalse(set.asSet().contains(40))
            assertTrue(set.contains(10))
            assertFalse(set.contains(20))
            assertTrue(set.contains(30))
            assertFalse(set.contains(40))
            assertFalse(set.isEmpty())
            assertTrue(set.isNotEmpty())

            val set2 = set.asSet()

            assertTrue(set2.contains(10))
            assertTrue(set2.contains(30))

            var count = 0
            set.forEach { count += it }

            assertEquals(40, count)

            val set3 = set.map { item -> 10 * item }

            assertEquals(2, set3.size)
            assertTrue(set3.contains(100))
            assertTrue(set3.contains(300))

        }

        checkRev5()
        checkRev4()
        checkRev3()
        checkRev2()
        checkRev1()

        // remove everything
        revHistory.update(2) {
            set.clear()
            "Rev #6"
        }

        assertEquals(6, revHistory.lastRevision.revisionNumber)

        revHistory.review {
            assertEquals(0, set.size)
            assertEquals(0, set.asSet().size)
        }

        checkRev5()
        checkRev4()
        checkRev3()
        checkRev2()
        checkRev1()

    }


    @Test
    fun `A versioned hash set works when retries are needed`() {

        val revHistory = RevisionHistory("Rev #0")

        var set0: VHashSet<Int>? = null

        // empty starting point
        revHistory.update(2) {
            set0 = VHashSet()
            "Rev #1"
        }

        val set: VHashSet<Int> = set0!!

        assertEquals(1, revHistory.lastRevision.revisionNumber)

        fun checkRev1() = revHistory.review(1) {
            assertEquals(0, set.size)
            assertEquals(0, set.asSet().size)
            assertTrue(set.isEmpty())
            assertFalse(set.isNotEmpty())
        }

        checkRev1()

        var retries = 2

        // add some items with thread conflict
        revHistory.update(2) {

            set.add(30)

            retries -= 1

            if (retries > 0) {

                val otherThread = Thread {

                    revHistory.update(2) {
                        set.add(10)
                        set.add(20)
                        "Rev #2"
                    }

                }

                otherThread.start()
                otherThread.join()

            }

            set.add(40)
            "Rev #3"

        }

        assertEquals(0, retries)
        assertEquals(3, revHistory.lastRevision.revisionNumber)

        revHistory.review(3) {
            assertEquals(4, set.size)
            assertTrue(set.contains(10))
            assertTrue(set.contains(20))
            assertTrue(set.contains(30))
            assertTrue(set.contains(40))
        }

        revHistory.review(2) {
            assertEquals(2, set.size)
            assertTrue(set.contains(10))
            assertTrue(set.contains(20))
        }

        checkRev1()


    }

}

//---------------------------------------------------------------------------------------------------------------------
