package squidpony.squidmath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Meant to take a fixed-size set of items and produce a shuffled stream of them such that an element is never chosen in
 * quick succession; that is, there should always be a gap between the same item's occurrences. This is an Iterable of
 * T, not a Collection, because it can iterate without stopping, infinitely, unless you break out of a foreach loop that
 * iterates through one of these, or call the iterator's next() method only a limited number of times. Collections have
 * a size that can be checked, but Iterables can be infinite (and in this case, this one is).
 * Created by Tommy Ettinger on 5/21/2016.
 * @param <T> the type of items to iterate over; ideally, the items are unique
 */
public class GapShuffler<T> implements Iterable<T>, Serializable {
    private static final long serialVersionUID = 1277543974688106290L;
    public RNG rng;
    private ArrayList<T> elements;
    private int size, index;
    private int[][] indexSections;
    private GapShuffler() {}

    /**
     * Constructor that takes any Collection of T, shuffles it with an unseeded RNG, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in elements is unique, but that is not necessary and does not affect how this works.
     * @param elements a Collection of T that will not be modified
     */
    public GapShuffler(Collection<T> elements)
    {
        rng = new RNG(new LongPeriodRNG());
        this.elements = rng.shuffle(elements);
        size = this.elements.size();
        double sz2 = size;
        index = 0;
        int portionSize = Math.min(20, Math.max(1, size / 2));
        int minSection = Math.min(5, size / 2 + 1);
        while (size % portionSize < minSection && portionSize > 2)
            portionSize--;
        indexSections = new int[(int)Math.ceil(sz2 / portionSize)][];
        for (int i = 0; i < indexSections.length - 1; i++) {
            indexSections[i] = PermutationGenerator.decodePermutation(
                    rng.nextLong(PermutationGenerator.getTotalPermutations(portionSize)), portionSize, i * portionSize);
            sz2 -= portionSize;
        }
        indexSections[indexSections.length - 1] = PermutationGenerator.decodePermutation(
                rng.nextLong(PermutationGenerator.getTotalPermutations((int)sz2)),
                (int)sz2, (indexSections.length - 1) * portionSize);
    }

    /**
     * Constructor that takes any Collection of T, shuffles it with the given RNG, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in elements is unique, but that is not necessary and does not affect how this works. The
     * rng parameter is copied so externally using it won't change the order this produces its values; the rng field is
     * used whenever the iterator needs to re-shuffle the internal ordering of elements. I suggest that the RNG should
     * use LongPeriodRNG as its RandomnessSource, since it is in general a good choice for shuffling, but since this
     * class mostly delegates its unique-shuffling code to PermutationGenerator and looks up at most 20 elements'
     * permutation at once (allowing it to use a single random long to generate the permutation), there probably won't
     * be problems if you use any other RandomnessSource.
     * @param elements a Collection of T that will not be modified
     * @param rng an RNG that can be pre-seeded; will be copied and not used directly
     */
    public GapShuffler(Collection<T> elements, RNG rng)
    {
        this.rng = rng.copy();
        this.elements = rng.shuffle(elements);
        size = this.elements.size();
        double sz2 = size;
        index = 0;
        int portionSize = Math.min(20, Math.max(1, size / 2));
        int minSection = Math.min(5, size / 2 + 1);
        while (size % portionSize < minSection && portionSize > 2)
            portionSize--;
        indexSections = new int[(int)Math.ceil(sz2 / portionSize)][];
        for (int i = 0; i < indexSections.length - 1; i++) {
            indexSections[i] = PermutationGenerator.decodePermutation(
                    rng.nextLong(PermutationGenerator.getTotalPermutations(portionSize)), portionSize, i * portionSize);
            sz2 -= portionSize;
        }
        indexSections[indexSections.length - 1] = PermutationGenerator.decodePermutation(
                rng.nextLong(PermutationGenerator.getTotalPermutations((int)sz2)),
                (int)sz2, (indexSections.length - 1) * portionSize);
    }

    /**
     * Gets the next element of the infinite sequence of T this shuffles through. The same as calling next() on an
     * iterator returned by this class' iterator() method.
     * @return the next element in the infinite sequence
     */
    public T getNext() {
        if(index >= size)
        {
            index = 0;
            int build = 0, inner, rotated;
            for (int i = 0; i < indexSections.length; i++) {
                if(indexSections.length <= 2)
                    rotated = (indexSections.length + 2 - i) % indexSections.length;
                else
                    rotated = (indexSections.length + 1 - i) % indexSections.length;
                inner = indexSections[rotated].length;
                indexSections[rotated] =
                        PermutationGenerator.decodePermutation(
                                rng.nextLong(PermutationGenerator.getTotalPermutations(inner)),
                                inner,
                                build);
                build += inner;
            }
        }
        int ilen = indexSections[0].length, ii = index / ilen, ij = index - ilen * ii;
        ++index;
        return elements.get(indexSections[ii][ij]);
    }
    /**
     * Returns an <b>infinite</b> iterator over elements of type {@code T}. You should be prepared to break out of any
     * for loops that use this once you've gotten enough elements! The remove() method is not supported by this iterator
     * and hasNext() will always return true.
     *
     * @return an infinite Iterator over elements of type T.
     */
    @Override
    public Iterator<T> iterator() {
        return new GapIterator();
    }

    private class GapIterator implements Iterator<T>, Serializable
    {
        private static final long serialVersionUID = 3167045364623458470L;
        private int innerIndex;
        private RNG innerRNG;
        GapIterator() {
            innerIndex = 0;
            innerRNG = rng.copy();
        }
        /**
         * Returns {@code true} if the iteration has more elements.
         * This is always the case for this Iterator.
         *
         * @return {@code true} always
         */
        @Override
        public boolean hasNext() {
            return true;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         */
        @Override
        public T next() {
            if(innerIndex >= size)
            {
                innerIndex = 0;
                int build = 0, inner, rotated;
                for (int i = 0; i < indexSections.length; i++) {

                    if(indexSections.length <= 2)
                        rotated = (indexSections.length + 2 - i) % indexSections.length;
                    else
                        rotated = (indexSections.length + 1 - i) % indexSections.length;
                    inner = indexSections[rotated].length;
                    indexSections[rotated] =
                            PermutationGenerator.decodePermutation(
                                    innerRNG.nextLong(PermutationGenerator.getTotalPermutations(inner)),
                                    inner,
                                    build);
                    build += inner;
                }
            }
            int ilen = indexSections[0].length, ii = innerIndex / ilen, ij = innerIndex - ilen * ii;
            ++innerIndex;
            return elements.get(indexSections[ii][ij]);
        }

        /**
         * Not supported.
         *
         * @throws UnsupportedOperationException always throws this exception
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() is not supported");
        }
    }
}
