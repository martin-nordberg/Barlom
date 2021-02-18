//
// (C) Copyright 2017-2021 Martin E. Nordberg III
// Apache 2.0 License
//

package barlom.util.revisions

import org.junit.jupiter.api.Test
import kotlin.test.*

//---------------------------------------------------------------------------------------------------------------------

/**
 * Tests of class VLinkedList.
 */
internal class VLinkedListTests {

    @Test
    fun `A versioned linked list grows and shrinks with old versions preserved`() {

        val revHistory = RevisionHistory("Rev #0")

        var list0: VLinkedList<Int>? = null

        // empty starting point
        revHistory.update(2) {
            list0 = VLinkedList()
            "Rev #1"
        }

        val list: VLinkedList<Int> = list0!!

        assertEquals(1, revHistory.lastRevision.revisionNumber)

        fun checkRev1() = revHistory.review(1) {
            assertEquals(0, list.size)
            assertEquals(0, list.asList().size)
            assertTrue(list.isEmpty())
            assertFalse(list.isNotEmpty())
            assertFalse(list.remove(40))
            assertNull(list.find { it == 7 })
            assertEquals(-1, list.indexOf(2))
            assertEquals(-1, list.lastIndexOf(2))
            assertFailsWith(IndexOutOfBoundsException::class) {
                list[-1]
            }
            assertFailsWith(IndexOutOfBoundsException::class) {
                list[0]
            }
            assertFailsWith(IndexOutOfBoundsException::class) {
                list[-1] = 1
            }
            assertFailsWith(IndexOutOfBoundsException::class) {
                list[0] = 4
            }
        }

        checkRev1()

        // add one item
        revHistory.update(2) {
            list.add(10)
            "Rev #2"
        }

        assertEquals(2, revHistory.lastRevision.revisionNumber)

        fun checkRev2() = revHistory.review(2) {
            assertEquals(1, list.size)
            assertEquals(1, list.asList().size)
            assertTrue(list.asList().contains(10))
            assertTrue(list.contains(10))
            assertFalse(list.isEmpty())
            assertTrue(list.isNotEmpty())
        }

        checkRev2()
        checkRev1()

        // add two more items
        revHistory.update(2) {
            list.add(20)
            list.add(30)
            "Rev #3"
        }

        assertEquals(3, revHistory.lastRevision.revisionNumber)

        fun checkRev3() = revHistory.review(3) {
            assertEquals(3, list.size)
            assertEquals(3, list.asList().size)
            assertTrue(list.asList().contains(10))
            assertTrue(list.asList().contains(20))
            assertTrue(list.asList().contains(30))
            assertTrue(list.contains(10))
            assertTrue(list.contains(20))
            assertTrue(list.contains(30))
            assertFalse(list.isEmpty())
            assertTrue(list.isNotEmpty())
        }

        checkRev3()
        checkRev2()
        checkRev1()

        // remove one; add one new and one duplicate
        var removed = false
        var notRemoved = false
        revHistory.update(2) {
            removed = list.remove(20)
            list.add(30)
            list.add(40)
            notRemoved = !list.remove(21)
            "Rev #4"
        }

        assertEquals(4, revHistory.lastRevision.revisionNumber)
        assertTrue(removed)
        assertTrue(notRemoved)

        fun checkRev4() = revHistory.review(4) {

            assertEquals(4, list.size)
            assertEquals(4, list.asList().size)
            assertTrue(list.asList().contains(10))
            assertFalse(list.asList().contains(20))
            assertTrue(list.asList().contains(30))
            assertTrue(list.asList().contains(40))
            assertTrue(list.contains(10))
            assertFalse(list.contains(20))
            assertTrue(list.contains(30))
            assertTrue(list.contains(40))
            assertTrue(list.contains({ it == 10 }))
            assertFalse(list.contains({ it == 20 }))
            assertTrue(list.contains({ it == 30 }))
            assertTrue(list.contains({ it == 40 }))
            assertFalse(list.isEmpty())
            assertTrue(list.isNotEmpty())

            assertEquals(40, list[0])
            assertEquals(30, list[1])
            assertEquals(30, list[2])
            assertEquals(10, list[3])

            val list2 = list.asList()

            assertEquals(4, list2.size)
            assertEquals(40, list2[0])
            assertEquals(30, list2[1])
            assertEquals(30, list2[2])
            assertEquals(10, list2[3])

            val slist = list.sortedBy { item -> item }

            assertEquals(4, slist.size)
            assertEquals(10, slist[0])
            assertEquals(30, slist[1])
            assertEquals(30, slist[2])
            assertEquals(40, slist[3])

            var count = 0
            list.forEach { count += 1 }

            assertEquals(4, count)

            count = 0
            list.forEachWhile { item ->
                if (item >= 30) {
                    count += 1; true
                } else false
            }

            assertEquals(3, count)

            val list3 = list.map { item -> 10 * item }

            assertEquals(4, list3.size)
            assertEquals(400, list3[0])
            assertEquals(300, list3[1])
            assertEquals(300, list3[2])
            assertEquals(100, list3[3])

            assertEquals(0, list.indexOf(40))
            assertEquals(1, list.indexOf(30))
            assertEquals(3, list.indexOf(10))
            assertEquals(-1, list.indexOf(11))
            assertEquals(0, list.lastIndexOf(40))
            assertEquals(2, list.lastIndexOf(30))
            assertEquals(3, list.lastIndexOf(10))
            assertEquals(-1, list.lastIndexOf(11))

        }

        checkRev4()
        checkRev3()
        checkRev2()
        checkRev1()

        // remove the first item
        revHistory.update(2) {

            list.remove(40)
            "Rev #5"

        }

        assertEquals(5, revHistory.lastRevision.revisionNumber)

        fun checkRev5() = revHistory.review(5) {

            assertEquals(3, list.size)
            assertEquals(3, list.asList().size)
            assertTrue(list.asList().contains(10))
            assertFalse(list.asList().contains(20))
            assertTrue(list.asList().contains(30))
            assertFalse(list.asList().contains(40))
            assertTrue(list.contains(10))
            assertFalse(list.contains(20))
            assertTrue(list.contains(30))
            assertFalse(list.contains(40))
            assertFalse(list.isEmpty())
            assertTrue(list.isNotEmpty())

            val list2 = list.asList()

            assertEquals(3, list2.size)
            assertEquals(30, list2[0])
            assertEquals(30, list2[1])
            assertEquals(10, list2[2])

            var count = 0
            list.forEach { count += it }

            assertEquals(70, count)

            count = 0
            list.forEachWhile { item ->
                if (item != 30) {
                    count += 1; true
                } else false
            }

            assertEquals(0, count)

            val list3 = list.map { item -> 10 * item }

            assertEquals(3, list3.size)
            assertEquals(300, list3[0])
            assertEquals(300, list3[1])
            assertEquals(100, list3[2])

        }

        checkRev5()
        checkRev4()
        checkRev3()
        checkRev2()
        checkRev1()

        // remove everything
        revHistory.update(2) {
            list.clear()
            "Rev #6"
        }

        assertEquals(6, revHistory.lastRevision.revisionNumber)

        revHistory.review {
            assertEquals(0, list.size)
            assertEquals(0, list.asList().size)
        }

        checkRev5()
        checkRev4()
        checkRev3()
        checkRev2()
        checkRev1()

    }


    @Test
    fun `A versioned linked list supports indexed operations and iteration`() {

        val revHistory = RevisionHistory("Rev #0")

        var list0: VLinkedList<Int>? = null

        // empty starting point
        revHistory.update(2) {
            list0 = VLinkedList()
            "Rev #1"
        }

        val list: VLinkedList<Int> = list0!!

        // add several items
        revHistory.update(2) {
            list.add(90)
            list.add(80)
            list.add(70)
            list.add(60)
            list.add(50)
            list.add(40)
            list.add(30)
            list.add(20)
            list.add(10)
            list.add(0)
            "Rev #2"
        }

        revHistory.review {
            assertEquals(10, list.size)
            assertEquals(0, list[0])
            assertEquals(10, list[1])
            assertEquals(90, list[9])
        }

        // remove several items
        revHistory.update(2) {
            assertEquals(0, list.removeAt(0))
            assertEquals(10, list.removeAt(0))
            assertEquals(40, list.removeAt(2))
            assertEquals(90, list.removeAt(6))
            assertFailsWith(IndexOutOfBoundsException::class) {
                list.removeAt(6)
            }
            assertFailsWith(IndexOutOfBoundsException::class) {
                list.removeAt(-1)
            }
            "Rev #3"
        }

        revHistory.review {
            assertEquals(6, list.size)
            assertEquals(20, list[0])
            assertEquals(30, list[1])
            assertEquals(50, list[2])
            assertEquals(60, list[3])
            assertEquals(70, list[4])
            assertEquals(80, list[5])
            assertFailsWith(IndexOutOfBoundsException::class) {
                list[-1]
            }
            assertFailsWith(IndexOutOfBoundsException::class) {
                list[6]
            }
        }

        // replace several items
        revHistory.update(2) {
            assertEquals(20, list.set(0, 25))
            assertEquals(30, list.set(1, 35))
            assertEquals(70, list.set(4, 75))
            assertEquals(80, list.set(5, 85))
            assertFailsWith(IndexOutOfBoundsException::class) {
                list.set(6, 95)
            }
            "Rev #4"
        }

        revHistory.review {
            assertEquals(6, list.size)
            assertEquals(25, list[0])
            assertEquals(35, list[1])
            assertEquals(50, list[2])
            assertEquals(60, list[3])
            assertEquals(75, list[4])
            assertEquals(85, list[5])
        }

        revHistory.review {

            val i = list.iterator()

            assertTrue(i.hasNext())
            assertEquals(25, i.next())
            assertTrue(i.hasNext())
            assertEquals(35, i.next())
            assertTrue(i.hasNext())
            assertEquals(50, i.next())
            assertTrue(i.hasNext())
            assertEquals(60, i.next())
            assertTrue(i.hasNext())
            assertEquals(75, i.next())
            assertTrue(i.hasNext())
            assertEquals(85, i.next())

            assertFalse(i.hasNext())

        }

        revHistory.review {

            val x = list.find {
                it == 75
            }

            assertEquals(75, x)

            val y = list.find {
                it == 40
            }

            assertNull(y)

        }

        revHistory.review {

            var count = 0

            list.forEach { count += 1 }

            assertEquals(6, count)

        }

        revHistory.review {

            var count = 0

            list.forEachWhile {
                if (it == 60) {
                    false
                } else {
                    count += 1
                    true
                }
            }

            assertEquals(3, count)

        }

    }


    @Test
    fun `A versioned linked list works when retries are needed`() {

        val revHistory = RevisionHistory("Rev #0")

        var list0: VLinkedList<Int>? = null

        // empty starting point
        revHistory.update(2) {
            list0 = VLinkedList()
            "Rev #1"
        }

        val list: VLinkedList<Int> = list0!!

        assertEquals(1, revHistory.lastRevision.revisionNumber)

        fun checkRev1() = revHistory.review(1) {
            assertEquals(0, list.size)
            assertEquals(0, list.asList().size)
            assertTrue(list.isEmpty())
            assertFalse(list.isNotEmpty())
        }

        checkRev1()

        var retries = 2

        // add some items with thread conflict
        revHistory.update(2) {

            list.add(30)

            retries -= 1

            if (retries > 0) {

                val otherThread = Thread {

                    revHistory.update(2) {
                        list.add(10)
                        list.add(20)
                        "Rev #2"
                    }

                }

                otherThread.start()
                otherThread.join()

            }

            list.add(40)
            "Rev #3"

        }

        assertEquals(0, retries)
        assertEquals(3, revHistory.lastRevision.revisionNumber)

        revHistory.review(3) {
            assertEquals(4, list.size)
            assertEquals(40, list[0])
            assertEquals(30, list[1])
            assertEquals(20, list[2])
            assertEquals(10, list[3])
        }

        revHistory.review(2) {
            assertEquals(2, list.size)
            assertEquals(20, list[0])
            assertEquals(10, list[1])
        }

        checkRev1()


    }

}

//---------------------------------------------------------------------------------------------------------------------
