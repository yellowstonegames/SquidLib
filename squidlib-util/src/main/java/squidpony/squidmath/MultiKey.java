package squidpony.squidmath;

import squidpony.annotation.Beta;

import java.util.Iterator;

/**
 * Created by Tommy Ettinger on 10/23/2016.
 */
@Beta
public interface MultiKey {
    int keyCount();
    int size();
    @Beta
    class TwoKey<A, B> implements MultiKey
    {
        protected Arrangement<A> keysA;
        protected Arrangement<B> keysB;
        public TwoKey()
        {
            keysA = new Arrangement<>();
            keysB = new Arrangement<>();
        }
        public TwoKey(int expected)
        {
            keysA = new Arrangement<>(expected);
            keysB = new Arrangement<>(expected);
        }
        public TwoKey(int expected, float f)
        {
            keysA = new Arrangement<>(expected, f);
            keysB = new Arrangement<>(expected, f);
        }
        public TwoKey(Iterable<A> aKeys, Iterable<B> bKeys)
        {
            keysA = new Arrangement<>();
            keysB = new Arrangement<>();
            putAll(aKeys, bKeys);
        }

        public int getIndexA(Object key)
        {
            return keysA.getInt(key);
        }
        public int getIndexB(Object key)
        {
            return keysB.getInt(key);
        }

        public A getAAt(int index)
        {
            return keysA.keyAt(index);
        }
        public B getBAt(int index)
        {
            return keysB.keyAt(index);
        }

        public B getBFromA(Object key)
        {
            return keysB.keyAt(keysA.getInt(key));
        }
        public A getAFromB(Object key)
        {
            return keysA.keyAt(keysB.getInt(key));
        }

        /**
         * Changes an existing A key, {@code past}, to another A key, {@code future}, if past exists in this MultiKey
         * and future does not yet exist in this MultiKey. This will retain past's point in the ordering for future, so
         * the associated other key(s) will still be associated in the same way.
         * @param past an A key, that must exist in this MultiKey's A keys, and will be changed
         * @param future an A key, that cannot currently exist in this MultiKey's A keys, but will if this succeeds
         * @return this for chaining
         */
        public TwoKey<A, B> alterA(A past, A future)
        {
            if(keysA.containsKey(past) && !keysA.containsKey(future))
                keysA.alter(past, future);
            return this;
        }

        /**
         * Changes an existing B key, {@code past}, to another B key, {@code future}, if past exists in this MultiKey
         * and future does not yet exist in this MultiKey. This will retain past's point in the ordering for future, so
         * the associated other key(s) will still be associated in the same way.
         * @param past a B key, that must exist in this MultiKey's B keys, and will be changed
         * @param future a B key, that cannot currently exist in this MultiKey's B keys, but will if this succeeds
         * @return this for chaining
         */
        public TwoKey<A, B> alterB(B past, B future)
        {
            if(keysB.containsKey(past) && !keysB.containsKey(future))
                keysB.alter(past, future);
            return this;
        }

        /**
         * Changes the A key at {@code index} to another A key, {@code future}, if index is valid and future does not
         * yet exist in this MultiKey. The position in the ordering for future will be the same as index, and the same
         * as the key this replaced, if this succeeds, so the other key(s) at that position will still be associated in
         * the same way.
         * @param index a position in the ordering to change; must be at least 0 and less than {@link #size()}
         * @param future an A key, that cannot currently exist in this MultiKey's A keys, but will if this succeeds
         * @return this for chaining
         */
        public TwoKey<A, B> alterAAt(int index, A future)
        {
            if(!keysA.containsKey(future) && index >= 0 && index < keysA.size)
                keysA.alter(keysA.keyAt(index), future);
            return this;
        }


        /**
         * Changes the B key at {@code index} to another B key, {@code future}, if index is valid and future does not
         * yet exist in this MultiKey. The position in the ordering for future will be the same as index, and the same
         * as the key this replaced, if this succeeds, so the other key(s) at that position will still be associated in
         * the same way.
         * @param index a position in the ordering to change; must be at least 0 and less than {@link #size()}
         * @param future a B key, that cannot currently exist in this MultiKey's B keys, but will if this succeeds
         * @return this for chaining
         */
        public TwoKey<A, B> alterBAt(int index, B future)
        {
            if(!keysB.containsKey(future) && index >= 0 && index < keysB.size)
                keysB.alter(keysB.keyAt(index), future);
            return this;
        }
        /**
         * Adds an A key and a B key at the same point in the ordering to this MultiKey. Neither parameter can be
         * present in this collection before this is called. If you want to change or update an existing key, use
         * {@link #alterA(Object, Object)} or {@link #alterB(Object, Object)}.
         * @param a an A value to add; cannot already be present
         * @param b a B value to add; cannot already be present
         * @return this for chaining
         */
        public TwoKey<A, B> put(A a, B b)
        {
            if(!keysA.containsKey(a) && !keysB.containsKey(b))
            {
                keysA.add(a);
                keysB.add(b);
            }
            return this;
        }

        /**
         * Puts all unique A and B keys in {@code aKeys} and {@code bKeys} into this MultiKey. If an A in aKeys or a B
         * in bKeys is already present when this would add one, this will not put the A and B keys at that point in the
         * iteration order, and will place the next unique A and B it finds in the arguments at that position instead.
         * @param aKeys an Iterable or Collection of A keys to add; should all be unique (like a Set)
         * @param bKeys an Iterable or Collection of B keys to add; should all be unique (like a Set)
         * @return this for chaining
         */
        public TwoKey<A, B> putAll(Iterable<A> aKeys, Iterable<B> bKeys)
        {
            Iterator<A> aIt = aKeys.iterator();
            Iterator<B> bIt = bKeys.iterator();
            while (aIt.hasNext() && bIt.hasNext())
            {
                put(aIt.next(), bIt.next());
            }
            return this;
        }

        /**
         * Adds an A key and a B key at the given index in the ordering to this MultiKey. Neither a nor b can be
         * present in this collection before this is called. If you want to change or update an existing key, use
         * {@link #alterA(Object, Object)} or {@link #alterB(Object, Object)}. The index this is given should be at
         * least 0 and no greater than {@link #size()}.
         * @param index the point in the ordering to place a and b into; later entries will be shifted forward
         * @param a an A value to add; cannot already be present
         * @param b a B value to add; cannot already be present
         * @return this for chaining
         */
        public TwoKey<A, B> putAt(int index, A a, B b)
        {
            if(!keysA.containsKey(a) && !keysB.containsKey(b))
            {
                keysA.addAt(index, a);
                keysB.addAt(index, b);
            }
            return this;
        }

        /**
         * Removes a given A key, if {@code removing} exists in this MultiKey's A keys, and also removes any keys
         * associated with its point in the ordering.
         * @param removing the A key to remove
         * @return this for chaining
         */
        public TwoKey<A, B> removeA(A removing)
        {
            keysB.removeAt(keysA.removeInt(removing));
            return this;
        }
        /**
         * Removes a given B key, if {@code removing} exists in this MultiKey's B keys, and also removes any keys
         * associated with its point in the ordering.
         * @param removing the B key to remove
         * @return this for chaining
         */
        public TwoKey<A, B> removeB(B removing)
        {
            keysA.removeAt(keysB.removeInt(removing));
            return this;
        }
        /**
         * Removes a given point in the ordering, if {@code index} is at least 0 and less than {@link #size()}.
         * @param index the position in the ordering to remove
         * @return this for chaining
         */
        public TwoKey<A, B> removeAt(int index)
        {
            keysA.removeAt(index);
            keysB.removeAt(index);
            return this;
        }

        /**
         * Reorders this MultiKey using {@code ordering}, which have the same length as this MultiKey's {@link #size()}
         * and can be generated with {@link squidpony.GwtCompatibility#range(int)} (which produces no change to the
         * current ordering), {@link RNG#randomOrdering(int)} (which gives a random ordering, and if applied immediately
         * would be the same as calling {@link #shuffle(RNG)}), or made in some other way. If you already have an
         * ordering and want to undo the change, you can use {@link squidpony.GwtCompatibility#invertOrdering(int[])}.
         * @param ordering an int array or vararg that should contain each int from 0 to {@link #size()} in some order
         * @return this for chaining
         */
        public TwoKey<A, B> reorder(int... ordering)
        {
            keysA.reorder(ordering);
            keysB.reorder(ordering);
            return this;
        }

        /**
         * Generates a random ordering with rng and applies the same ordering to all kinds of keys this has; they will
         * maintain their current association to other keys but their ordering/indices will change.
         * @param rng an RNG to produce the random ordering this will use
         * @return this for chaining
         */
        public TwoKey<A, B> shuffle(RNG rng)
        {
            int[] ordering = rng.randomOrdering(keysA.size);
            keysA.reorder(ordering);
            keysB.reorder(ordering);
            return this;
        }

        /**
         * Creates a new iterator over the A keys this holds. This can be problematic for garbage collection, and it can
         * be better to access items by index (which also lets you access other keys associated with that index) using
         * {@link #getAAt(int)} in a for loop.
         * @return a newly-created iterator over this MultiKey's A keys
         */
        public Iterator<A> iteratorA()
        {
            return keysA.iterator();
        }
        /**
         * Creates a new iterator over the B keys this holds. This can be problematic for garbage collection, and it can
         * be better to access items by index (which also lets you access other keys associated with that index) using
         * {@link #getBAt(int)} in a for loop.
         * @return a newly-created iterator over this MultiKey's B keys
         */
        public Iterator<B> iteratorB()
        {
            return keysB.iterator();
        }

        @Override
        public int keyCount() {
            return 2;
        }
        @Override
        public int size() {
            return keysA.size;
        }

    }
}
