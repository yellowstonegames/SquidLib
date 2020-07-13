/*
MIT License

Copyright (c) 2020 earlygrey

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package squidpony.squidai.graph;

/*
 ***********************************************************************
 *
 * NOTE: I've edited this code a little, original source here:
 * https://keithschwarz.com/interesting/code/?dir=fibonacci-heap
 * I've eg removed any safety checks
 *
 * File: FibonacciHeap.java
 * Author: Keith Schwarz (htiek@cs.stanford.edu)
 *
 * An implementation of a priority queue backed by a Fibonacci heap,
 * as described by Fredman and Tarjan.  Fibonacci heaps are interesting
 * theoretically because they have asymptotically good runtime guarantees
 * for many operations.  In particular, insert, peek, and decrease-key all
 * run in amortized O(1) time.  dequeueMin and delete each run in amortized
 * O(lg n) time.  This allows algorithms that rely heavily on decrease-key
 * to gain significant performance boosts.  For example, Dijkstra's algorithm
 * for single-source shortest paths can be shown to run in O(m + n lg n) using
 * a Fibonacci heap, compared to O(m lg n) using a standard binary or binomial
 * heap.
 *
 * Internally, a Fibonacci heap is represented as a circular, doubly-linked
 * list of trees obeying the min-heap property.  Each node stores pointers
 * to its parent (if any) and some arbitrary child.  Additionally, every
 * node stores its degree (the number of children it has) and whether it
 * is a "marked" node.  Finally, each Fibonacci heap stores a pointer to
 * the tree with the minimum value.
 *
 * To insert a node into a Fibonacci heap, a singleton tree is created and
 * merged into the rest of the trees.  The merge operation works by simply
 * splicing together the doubly-linked lists of the two trees, then updating
 * the min pointer to be the smaller of the minima of the two heaps.  Peeking
 * at the smallest element can therefore be accomplished by just looking at
 * the min element.  All of these operations complete in O(1) time.
 *
 * The tricky operations are dequeueMin and decreaseKey.  dequeueMin works
 * by removing the root of the tree containing the smallest element, then
 * merging its children with the topmost roots.  Then, the roots are scanned
 * and merged so that there is only one tree of each degree in the root list.
 * This works by maintaining a dynamic array of trees, each initially null,
 * pointing to the roots of trees of each dimension.  The list is then scanned
 * and this array is populated.  Whenever a conflict is discovered, the
 * appropriate trees are merged together until no more conflicts exist.  The
 * resulting trees are then put into the root list.  A clever analysis using
 * the potential method can be used to show that the amortized cost of this
 * operation is O(lg n), see "Introduction to Algorithms, Second Edition" by
 * Cormen, Rivest, Leiserson, and Stein for more details.
 *
 * The other hard operation is decreaseKey, which works as follows.  First, we
 * update the key of the node to be the new value.  If this leaves the node
 * smaller than its parent, we're done.  Otherwise, we cut the node from its
 * parent, add it as a root, and then mark its parent.  If the parent was
 * already marked, we cut that node as well, recursively mark its parent,
 * and continue this process.  This can be shown to run in O(1) amortized time
 * using yet another clever potential function.  Finally, given this function,
 * we can implement delete by decreasing a key to -\infty, then calling
 * dequeueMin to extract it.
 */

import java.util.ArrayList;
import java.util.NoSuchElementException;


/**
 * A class representing a Fibonacci heap.
 *
 * @author Keith Schwarz (htiek@cs.stanford.edu)
 */
public final class FibonacciHeap<V> {
    /* In order for all of the Fibonacci heap operations to complete in O(1),
     * clients need to have O(1) access to any element in the heap.  We make
     * this work by having each insertion operation produce a handle to the
     * node in the tree.  In actuality, this handle is the node itself, but
     * we guard against external modification by marking the internal fields
     * private.
     */
    public static final class Entry<V> {
        private int degree = 0;       // Number of children
        private boolean isMarked = false; // Whether this node is marked

        private Entry<V> next;   // Next and previous elements in the list
        private Entry<V> prev;

        private Entry<V> parent; // Parent in the tree, if any.

        private Entry<V> child;  // Child node, if any.

        protected V item;     // Element being stored here
        private double value; // Its priority

        /**
         * Returns the element represented by this heap entry.
         *
         * @return The element represented by this heap entry.
         */
        public V getItem() {
            return item;
        }
        /**
         * Sets the element associated with this heap entry.
         *
         * @param item The element to associate with this heap entry.
         */
        public void setItem(V item) {
            this.item = item;
        }

        /**
         * Returns the priority of this element.
         *
         * @return The priority of this element.
         */
        public double getValue() {
            return value;
        }

        /**
         * Constructs a new Entry<V> that holds the given element with the indicated
         * priority.
         *
         * @param elem The element stored in this node.
         * @param value The priority of this element.
         */
        private Entry(V elem, double value) {
            next = prev = this;
            item = elem;
            this.value = value;
        }
    }

    /* Pointer to the minimum element in the heap. */
    private Entry<V> min = null;

    /* Cached size of the heap, so we don't have to recompute this explicitly. */
    private int size = 0;

    public void clear() {
        min = null;
    }

    /**
     * Inserts the specified element into the Fibonacci heap with the specified
     * priority.  Its priority must be a valid double, so you cannot set the
     * priority to NaN.
     *
     * @param value The value to insert.
     * @param priority Its priority, which must be valid.
     * @return An Entry<V> representing that element in the tree.
     */
    public Entry<V> enqueue(V value, double priority) {
        //checkPriority(priority);

        /* Create the entry object, which is a circularly-linked list of length
         * one.
         */
        Entry<V> result = new Entry<V>(value, priority);

        /* Merge this singleton list with the tree list. */
        min = mergeLists(min, result);

        /* Increase the size of the heap; we just added something. */
        ++size;

        /* Return the reference to the new element. */
        return result;
    }

    /**
     * Returns an Entry<V> object corresponding to the minimum element of the
     * Fibonacci heap, throwing a NoSuchElementException if the heap is
     * empty.
     *
     * @return The smallest element of the heap.
     * @throws NoSuchElementException If the heap is empty.
     */
    public Entry<V> min() {
        //if (isEmpty())
          //  throw new NoSuchElementException("Heap is empty.");
        return min;
    }

    /**
     * Returns whether the heap is empty.
     *
     * @return Whether the heap is empty.
     */
    public boolean isEmpty() {
        return min == null;
    }

    /**
     * Returns the number of elements in the heap.
     *
     * @return The number of elements in the heap.
     */
    public int size() {
        return size;
    }

    /**
     * Given two Fibonacci heaps, returns a new Fibonacci heap that contains
     * all of the elements of the two heaps.  Each of the input heaps is
     * destructively modified by having all its elements removed.  You can
     * continue to use those heaps, but be aware that they will be empty
     * after this call completes.
     *
     * @param one The first Fibonacci heap to merge.
     * @param two The second Fibonacci heap to merge.
     * @return A new FibonacciHeap containing all of the elements of both
     *         heaps.
     */
    public static <V> FibonacciHeap<V> merge(FibonacciHeap<V> one, FibonacciHeap<V> two) {
        /* Create a new FibonacciHeap to hold the result. */
        FibonacciHeap<V> result = new FibonacciHeap<>();

        /* Merge the two Fibonacci heap root lists together.  This helper function
         * also computes the min of the two lists, so we can store the result in
         * the mMin field of the new heap.
         */
        result.min = mergeLists(one.min, two.min);

        /* The size of the new heap is the sum of the sizes of the input heaps. */
        result.size = one.size + two.size;

        /* Clear the old heaps. */
        one.size = two.size = 0;
        one.min = null;
        two.min = null;

        /* Return the newly-merged heap. */
        return result;
    }

    /**
     * Dequeues and returns the minimum element of the Fibonacci heap.  If the
     * heap is empty, this throws a NoSuchElementException.
     *
     * @return The smallest element of the Fibonacci heap.
     * @throws NoSuchElementException If the heap is empty.
     */
    public Entry<V> dequeueMin() {
        /* Check for whether we're empty. */
        //if (isEmpty())
        //    throw new NoSuchElementException("Heap is empty.");

        /* Otherwise, we're about to lose an element, so decrement the number of
         * entries in this heap.
         */
        --size;

        /* Grab the minimum element so we know what to return. */
        Entry<V> minElem = min;

        /* Now, we need to get rid of this element from the list of roots.  There
         * are two cases to consider.  First, if this is the only element in the
         * list of roots, we set the list of roots to be null by clearing mMin.
         * Otherwise, if it's not null, then we write the elements next to the
         * min element around the min element to remove it, then arbitrarily
         * reassign the min.
         */
        if (min.next == min) { // Case one
            min = null;
        }
        else { // Case two
            min.prev.next = min.next;
            min.next.prev = min.prev;
            min = min.next; // Arbitrary element of the root list.
        }

        /* Next, clear the parent fields of all of the min element's children,
         * since they're about to become roots.  Because the elements are
         * stored in a circular list, the traversal is a bit complex.
         */
        if (minElem.child != null) {
            /* Keep track of the first visited node. */
            Entry<V> curr = minElem.child;
            do {
                curr.parent = null;

                /* Walk to the next node, then stop if this is the node we
                 * started at.
                 */
                curr = curr.next;
            } while (curr != minElem.child);
        }

        /* Next, splice the children of the root node into the topmost list,
         * then set mMin to point somewhere in that list.
         */
        min = mergeLists(min, minElem.child);

        /* If there are no entries left, we're done. */
        if (min == null) return minElem;

        /* Next, we need to coalsce all of the roots so that there is only one
         * tree of each degree.  To track trees of each size, we allocate an
         * ArrayList where the entry at position i is either null or the
         * unique tree of degree i.
         */
        ArrayList<Entry<V>> treeTable = new ArrayList<>();

        /* We need to traverse the entire list, but since we're going to be
         * messing around with it we have to be careful not to break our
         * traversal order mid-stream.  One major challenge is how to detect
         * whether we're visiting the same node twice.  To do this, we'll
         * spent a bit of overhead adding all of the nodes to a list, and
         * then will visit each element of this list in order.
         */
        ArrayList<Entry<V>> toVisit = new ArrayList<>();

        /* To add everything, we'll iterate across the elements until we
         * find the first element twice.  We check this by looping while the
         * list is empty or while the current element isn't the first element
         * of that list.
         */
        for (Entry<V> curr = min; toVisit.isEmpty() || toVisit.get(0) != curr; curr = curr.next) {
            toVisit.add(curr);
        }

        /* Traverse this list and perform the appropriate unioning steps. */
        int n = toVisit.size();
        for (int i = 0; i < n; i++) {
            Entry<V> curr = toVisit.get(i);
            /* Keep merging until a match arises. */
            while (true) {
                /* Ensure that the list is long enough to hold an element of this
                 * degree.
                 */
                while (curr.degree >= treeTable.size())
                    treeTable.add(null);

                /* If nothing's here, we're can record that this tree has this size
                 * and are done processing.
                 */
                if (treeTable.get(curr.degree) == null) {
                    treeTable.set(curr.degree, curr);
                    break;
                }

                /* Otherwise, merge with what's there. */
                Entry<V> other = treeTable.get(curr.degree);
                treeTable.set(curr.degree, null); // Clear the slot

                /* Determine which of the two trees has the smaller root, storing
                 * the two tree accordingly.
                 */
                Entry<V> min = (other.value < curr.value)? other : curr;
                Entry<V> max = (other.value < curr.value)? curr  : other;

                /* Break max out of the root list, then merge it into min's child
                 * list.
                 */
                max.next.prev = max.prev;
                max.prev.next = max.next;

                /* Make it a singleton so that we can merge it. */
                max.next = max.prev = max;
                min.child = mergeLists(min.child, max);

                /* Reparent max appropriately. */
                max.parent = min;

                /* Clear max's mark, since it can now lose another child. */
                max.isMarked = false;

                /* Increase min's degree; it now has another child. */
                ++min.degree;

                /* Continue merging this tree. */
                curr = min;
            }

            /* Update the global min based on this node.  Note that we compare
             * for <= instead of < here.  That's because if we just did a
             * reparent operation that merged two different trees of equal
             * priority, we need to make sure that the min pointer points to
             * the root-level one.
             */
            if (curr.value <= min.value) min = curr;
        }
        return minElem;
    }

    /**
     * Decreases the key of the specified element to the new priority.  If the
     * new priority is greater than the old priority, this function throws an
     * IllegalArgumentException.  The new priority must be a finite double,
     * so you cannot set the priority to be NaN, or +/- infinity.  Doing
     * so also throws an IllegalArgumentException.
     *
     * It is assumed that the entry belongs in this heap.  For efficiency
     * reasons, this is not checked at runtime.
     *
     * @param entry The element whose priority should be decreased.
     * @param newPriority The new priority to associate with this entry.
     * @throws IllegalArgumentException If the new priority exceeds the old
     *         priority, or if the argument is not a finite double.
     */
    public void decreaseKey(Entry<V> entry, double newPriority) {
        //checkPriority(newPriority);
        //if (newPriority > entry.mPriority)
         //   throw new IllegalArgumentException("New priority exceeds old.");

        /* Forward this to a helper function. */
        decreaseKeyUnchecked(entry, newPriority);
    }

    /**
     * Deletes this Entry<V> from the Fibonacci heap that contains it.
     *
     * It is assumed that the entry belongs in this heap.  For efficiency
     * reasons, this is not checked at runtime.
     *
     * @param entry The entry to delete.
     */
    public void delete(Entry<V> entry) {
        /* Use decreaseKey to drop the entry's key to -infinity.  This will
         * guarantee that the entry is cut and set to the global minimum.
         */
        decreaseKeyUnchecked(entry, Double.NEGATIVE_INFINITY);

        /* Call dequeueMin to remove it. */
        dequeueMin();
    }

    /**
     * Utility function which, given two pointers into disjoint circularly-
     * linked lists, merges the two lists together into one circularly-linked
     * list in O(1) time.  Because the lists may be empty, the return value
     * is the only pointer that's guaranteed to be to an element of the
     * resulting list.
     *
     * This function assumes that one and two are the minimum elements of the
     * lists they are in, and returns a pointer to whichever is smaller.  If
     * this condition does not hold, the return value is some arbitrary pointer
     * into the doubly-linked list.
     *
     * @param one A pointer into one of the two linked lists.
     * @param two A pointer into the other of the two linked lists.
     * @return A pointer to the smallest element of the resulting list.
     */
    private static <V> Entry<V> mergeLists(Entry<V> one, Entry<V> two) {
        /* There are four cases depending on whether the lists are null or not.
         * We consider each separately.
         */
        if (two == null) { // Either both null are, resulting list is null, or only two is, so result is one.
            return one;
        }
        else if (one == null) { // One is null, result is two.
            return two;
        }
        else { // Both non-null; actually do the splice.
            /* This is actually not as easy as it seems.  The idea is that we'll
             * have two lists that look like this:
             *
             * +----+     +----+     +----+
             * |    |--N->|one |--N->|    |
             * |    |<-P--|    |<-P--|    |
             * +----+     +----+     +----+
             *
             *
             * +----+     +----+     +----+
             * |    |--N->|two |--N->|    |
             * |    |<-P--|    |<-P--|    |
             * +----+     +----+     +----+
             *
             * And we want to relink everything to get
             *
             * +----+     +----+     +----+---+
             * |    |--N->|one |     |    |   |
             * |    |<-P--|    |     |    |<+ |
             * +----+     +----+<-\  +----+ | |
             *                  \  P        | |
             *                   N  \       N |
             * +----+     +----+  \->+----+ | |
             * |    |--N->|two |     |    | | |
             * |    |<-P--|    |     |    | | P
             * +----+     +----+     +----+ | |
             *              ^ |             | |
             *              | +-------------+ |
             *              +-----------------+
             *
             */
            Entry<V> oneNext = one.next; // Cache this since we're about to overwrite it.
            one.next = two.next;
            one.next.prev = one;
            two.next = oneNext;
            two.next.prev = two;

            /* Return a pointer to whichever's smaller. */
            return one.value < two.value ? one : two;
        }
    }

    /**
     * Decreases the key of a entry in the tree without doing any checking to ensure
     * that the new priority is valid.
     *
     * @param entry The entry whose key should be decreased.
     * @param priority The entry's new priority.
     */
    private void decreaseKeyUnchecked(Entry<V> entry, double priority) {
        /* First, change the entry's priority. */
        entry.value = priority;

        /* If the entry no longer has a higher priority than its parent, cut it.
         * Note that this also means that if we try to run a delete operation
         * that decreases the key to -infinity, it's guaranteed to cut the entry
         * from its parent.
         */
        if (entry.parent != null && entry.value <= entry.parent.value)
            cutNode(entry);

        /* If our new value is the new min, mark it as such.  Note that if we
         * ended up decreasing the key in a way that ties the current minimum
         * priority, this will change the min accordingly.
         */
        if (entry.value <= min.value)
            min = entry;
    }

    /**
     * Cuts a entry from its parent.  If the parent was already marked, recursively
     * cuts that entry from its parent as well.
     *
     * @param entry The entry to cut from its parent.
     */
    private void cutNode(Entry<V> entry) {
        /* Begin by clearing the entry's mark, since we just cut it. */
        entry.isMarked = false;

        /* Base case: If the entry has no parent, we're done. */
        if (entry.parent == null) return;

        /* Rewire the entry's siblings around it, if it has any siblings. */
        if (entry.next != entry) { // Has siblings
            entry.next.prev = entry.prev;
            entry.prev.next = entry.next;
        }

        /* If the entry is the one identified by its parent as its child,
         * we need to rewrite that pointer to point to some arbitrary other
         * child.
         */
        if (entry.parent.child == entry) {
            /* If there are any other children, pick one of them arbitrarily. */
            if (entry.next != entry) {
                entry.parent.child = entry.next;
            }
            /* Otherwise, there aren't any children left and we should clear the
             * pointer and drop the entry's degree.
             */
            else {
                entry.parent.child = null;
            }
        }

        /* Decrease the degree of the parent, since it just lost a child. */
        --entry.parent.degree;

        /* Splice this tree into the root list by converting it to a singleton
         * and invoking the merge subroutine.
         */
        entry.prev = entry.next = entry;
        min = mergeLists(min, entry);

        /* Mark the parent and recursively cut it if it's already been
         * marked.
         */
        if (entry.parent.isMarked)
            cutNode(entry.parent);
        else
            entry.parent.isMarked = true;

        /* Clear the relocated entry's parent; it's now a root. */
        entry.parent = null;
    }
}
