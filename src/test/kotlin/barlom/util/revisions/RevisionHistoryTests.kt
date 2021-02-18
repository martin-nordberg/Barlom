//
// (C) Copyright 2017-2018 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.util.revisions

import org.junit.jupiter.api.Test
import kotlin.test.*

//---------------------------------------------------------------------------------------------------------------------

/**
 * Tests of class RevisionHistory.
 */
internal class RevisionHistoryTests {

    @Test
    fun `A revision history supports transactions`() {

        val revHistory = RevisionHistory("Rev #0")

        var s0: Sample? = null

        revHistory.update(2) {
            s0 = Sample()
            "Rev #1"
        }

        val s = s0!!

        assertEquals("Rev #1", revHistory.lastRevision.description)
        assertEquals(1, revHistory.lastRevision.revisionNumber)
        assertEquals("Rev #0", revHistory.lastRevision.priorRevision?.description)
        assertEquals(0L, revHistory.lastRevision.priorRevision?.revisionNumber)
        assertNull(revHistory.lastRevision.priorRevision?.priorRevision)

        fun checkRev1() = revHistory.review(1) {
            assertEquals(1, s.value.get())
            assertEquals("one", s.name.get())
            assertFalse(s.isEven.get())
        }

        checkRev1()

        revHistory.update(2) {
            s.value.set(2)
            s.name.set("two")
            s.isEven.set(true)
            "Rev #2"
        }

        assertEquals("Rev #2", revHistory.lastRevision.description)
        assertEquals(2, revHistory.lastRevision.revisionNumber)
        assertEquals("Rev #1", revHistory.lastRevision.priorRevision?.description)

        fun checkRev2() = revHistory.review(2) {
            assertEquals(2, s.value.get())
            assertEquals("two", s.name.get())
            assertTrue(s.isEven.get())
        }

        checkRev2()
        checkRev1()

        revHistory.update(2) {
            s.value.increment()
            s.name.set("three")
            s.isEven.set(false)
            "Rev #3"
        }

        assertEquals("Rev #3", revHistory.lastRevision.description)
        assertEquals(3, revHistory.lastRevision.revisionNumber)
        assertEquals("Rev #2", revHistory.lastRevision.priorRevision?.description)

        revHistory.review {
            assertEquals(3, s.value.get())
            assertEquals("three", s.name.get())
            assertFalse(s.isEven.get())
        }

        checkRev2()
        checkRev1()

        assertFailsWith(IllegalArgumentException::class) {
            revHistory.review(-1) {}
        }

        assertFailsWith(IllegalArgumentException::class) {
            revHistory.review(4) {}
        }

    }

    @Test
    fun `A revision history supports retries with conflict resolution`() {

        val revHistory = RevisionHistory("Rev #0")

        var s0: Sample? = null

        revHistory.update(2) {
            s0 = Sample()
            "Rev #1"
        }

        val s = s0!!

        assertEquals("Rev #1", revHistory.lastRevision.description)
        assertEquals(1, revHistory.lastRevision.revisionNumber)
        assertEquals("Rev #0", revHistory.lastRevision.priorRevision?.description)

        fun checkRev1() = revHistory.review(1) {
            assertEquals(1, s.value.get())
        }

        checkRev1()

        var retries = 2

        revHistory.update(2) {

            retries -= 1

            if (retries > 0) {

                val otherThread = Thread {

                    revHistory.update(2) {
                        s.value.set(201)
                        s.name.set("two-oh-one")
                        s.isEven.set(false)
                        "Rev #201"
                    }

                }

                otherThread.start()
                otherThread.join()

            }

            s.value.get()

            s.value.set(2)
            s.name.set("two")
            s.isEven.set(true)

            "Rev #2"

        }

        assertEquals(0, retries)

        assertEquals("Rev #2", revHistory.lastRevision.description)
        assertEquals(3, revHistory.lastRevision.revisionNumber)
        assertEquals("Rev #201", revHistory.lastRevision.priorRevision?.description)
        assertEquals("Rev #1", revHistory.lastRevision.priorRevision?.priorRevision?.description)

        fun checkRev2() = revHistory.review(2) {
            assertEquals(201, s.value.get())
            assertEquals("two-oh-one", s.name.get())
            assertFalse(s.isEven.get())
        }

        fun checkRev3() = revHistory.review(3) {
            assertEquals(2, s.value.get())
            assertEquals("two", s.name.get())
            assertTrue(s.isEven.get())
        }

        checkRev1()
        checkRev2()
        checkRev3()

        retries = 2

        revHistory.update(2) {

            s.value.set(3)
            s.name.set("three")
            s.isEven.set(false)

            retries -= 1

            if (retries > 0) {

                val otherThread = Thread {

                    revHistory.update(2) {
                        s.value.set(302)
                        s.name.set("three-oh-two")
                        s.isEven.set(true)
                        "Rev #302"
                    }

                }

                otherThread.start()
                otherThread.join()

            }

            s.name.get()

            "Rev #3"

        }

        assertEquals(0, retries)

        assertEquals("Rev #3", revHistory.lastRevision.description)
        assertEquals(5, revHistory.lastRevision.revisionNumber)
        assertEquals("Rev #302", revHistory.lastRevision.priorRevision?.description)
        assertEquals("Rev #2", revHistory.lastRevision.priorRevision?.priorRevision?.description)

        revHistory.review {
            assertEquals(3, s.value.get())
            assertEquals("three", s.name.get())
            assertFalse(s.isEven.get())
        }

        revHistory.review(4) {
            assertEquals(302, s.value.get())
            assertEquals("three-oh-two", s.name.get())
            assertTrue(s.isEven.get())
        }

        checkRev1()
        checkRev2()
        checkRev3()

    }


    @Test
    fun `A revision history survives a task failure`() {

        val revHistory = RevisionHistory("Rev #0")

        var s0: Sample? = null

        revHistory.update(2) {
            s0 = Sample()
            "Rev #1"
        }

        val s = s0!!

        assertEquals("Rev #1", revHistory.lastRevision.description)
        assertEquals(1, revHistory.lastRevision.revisionNumber)
        assertEquals("Rev #0", revHistory.lastRevision.priorRevision?.description)
        assertEquals(0L, revHistory.lastRevision.priorRevision?.revisionNumber)
        assertNull(revHistory.lastRevision.priorRevision?.priorRevision)

        fun checkRev1() = revHistory.review(1) {
            assertEquals(1, s.value.get())
            assertEquals("one", s.name.get())
            assertFalse(s.isEven.get())
        }

        checkRev1()

        assertFailsWith(Exception::class) {

            revHistory.update(2) {
                s.value.set(2)
                s.name.set("two")
                s.isEven.set(true)
                throw Exception("Testing")
            }

        }

        assertEquals("Rev #1", revHistory.lastRevision.description)
        assertEquals(1, revHistory.lastRevision.revisionNumber)
        assertEquals("Rev #0", revHistory.lastRevision.priorRevision?.description)
        assertEquals(0L, revHistory.lastRevision.priorRevision?.revisionNumber)
        assertNull(revHistory.lastRevision.priorRevision?.priorRevision)

        checkRev1()

    }


    @Test
    fun `A revision limits transaction retries`() {

        val revHistory = RevisionHistory("Rev #0")

        var s0: Sample? = null

        revHistory.update(2) {
            s0 = Sample()
            assertEquals(revHistory, RevisionHistory.currentlyInUse)
            "Rev #1"
        }

        assertFailsWith(IllegalStateException::class) {
            RevisionHistory.transactionOfCurrentThread
        }

        val s = s0!!

        assertEquals("Rev #1", revHistory.lastRevision.description)
        assertEquals(1, revHistory.lastRevision.revisionNumber)
        assertEquals("Rev #0", revHistory.lastRevision.priorRevision?.description)
        assertEquals(0L, revHistory.lastRevision.priorRevision?.revisionNumber)
        assertNull(revHistory.lastRevision.priorRevision?.priorRevision)

        fun checkRev1() = revHistory.review(1) {
            assertFailsWith(IllegalStateException::class) {
                RevisionHistory.currentlyInUse
            }
            assertFailsWith(IllegalStateException::class) {
                revHistory.review {}
            }
            assertEquals(1, s.value.get())
            assertEquals("one", s.name.get())
            assertFalse(s.isEven.get())
        }

        checkRev1()

        var count = 0

        assertFailsWith(MaximumRetriesExceededException::class) {

            revHistory.update(2) {
                s.value.set(2)
                s.name.set("two")
                s.isEven.set(true)
                count += 1
                throw WriteConflictException()
            }

        }

        assertEquals(3, count)
        assertEquals("Rev #1", revHistory.lastRevision.description)
        assertEquals(1, revHistory.lastRevision.revisionNumber)
        assertEquals("Rev #0", revHistory.lastRevision.priorRevision?.description)
        assertEquals(0L, revHistory.lastRevision.priorRevision?.revisionNumber)
        assertNull(revHistory.lastRevision.priorRevision?.priorRevision)

        checkRev1()

    }


    class Sample {

        val value = V(1)
        val name = V("one")
        val isEven = V(false)

    }

}

//---------------------------------------------------------------------------------------------------------------------

