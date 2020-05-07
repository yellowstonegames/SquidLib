package squidpony.squidmath;

import squidpony.Maker;

import java.io.Serializable;
import java.util.*;

/**
 * Meant to take a fixed-size set of items and produce a shuffled stream of them such that an element is never chosen in
 * quick succession; that is, there should always be a gap between the same item's occurrences. This is an Iterable of
 * T, not a Collection, because it can iterate without stopping, infinitely, unless you break out of a foreach loop that
 * iterates through one of these, or call the iterator's next() method only a limited number of times. Collections have
 * a size that can be checked, but Iterables can be infinite (and in this case, this one is).
 * Created by Tommy Ettinger on 5/21/2016.
 * @param <T> the type of items to iterate over; ideally, the items are unique
 */
public class GapShuffler<T> implements Iterator<T>, Iterable<T>, Serializable {
    private static final long serialVersionUID = 1L;
    public IRNG rng;
    private ArrayList<T> elements;
    private int index;
    private GapShuffler() {}

    public GapShuffler(T single)
    {
        rng = new RNG();
        elements = new ArrayList<>(1);
        elements.add(single);
        index = 0;
    }
    /**
     * Constructor that takes any Collection of T, shuffles it with an unseeded RNG, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in elements is unique, but that is not necessary and does not affect how this works.
     * @param elements a Collection of T that will not be modified
     */
    public GapShuffler(Collection<T> elements)
    {
        this(elements, new RNG(new LongPeriodRNG()));

    }

    /**
     * Constructor that takes any Collection of T, shuffles it with an unseeded RNG, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in elements is unique, but that is not necessary and does not affect how this works.
     * @param elements a Collection of T that will not be modified
     */
    public GapShuffler(Collection<T> elements, String seed)
    {
        this(elements, new RNG(new LongPeriodRNG(seed)));
    }

    /**
     * Constructor that takes any Collection of T, shuffles it with the given RNG, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in items is unique, but that is not necessary and does not affect how this works. The
     * rng parameter is copied so externally using it won't change the order this produces its values; the rng field is
     * used whenever the iterator needs to re-shuffle the internal ordering of items. I suggest that the IRNG should
     * use {@link LongPeriodRNG} as its RandomnessSource, since it is in general a good choice for shuffling, or
     * {@link XoshiroStarPhi32RNG} for GWT or other 32-bit platforms, but the choice is unlikely to matter in practice.
     * @param items a Collection of T that will not be modified
     * @param rng an IRNG that can be pre-seeded; will be copied and not used directly
     */
    public GapShuffler(Collection<T> items, IRNG rng)
    {
        this.rng = rng.copy();
        elements = this.rng.shuffle(items);
        index = 0;
    }

    /**
     * Constructor that takes any Collection of T, shuffles it with the given RNG, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in items is unique, but that is not necessary and does not affect how this works. The
     * rng parameter will be copied if {@code shareRNG} is true, otherwise the reference will be shared (which could
     * make the results of this GapShuffler depend on outside code, though it will always maintain a gap between
     * identical elements if the elements are unique). I suggest that the IRNG should use {@link LongPeriodRNG} as its
     * RandomnessSource, since it is in general a good choice for shuffling, or {@link XoshiroStarPhi32RNG} for GWT or
     * other 32-bit platforms, but the choice is unlikely to matter in practice. Any {@link IStatefulRNG} should work in
     * most cases, like {@link GWTRNG}, {@link StatefulRNG}, or {@link SilkRNG}. If you encounter unusually-low-quality
     * shuffles, try a different starting seed, and if that doesn't work, try SilkRNG (it has a different internal
     * structure than the other types; it could yield better results at the very start).
     * @param items a Collection of T that will not be modified
     * @param rng an IRNG that can be pre-seeded; will be copied and not used directly
     * @param shareRNG if false, {@code rng} will be copied and no reference will be kept; if true, {@code rng} will be shared with the outside code
     */
    public GapShuffler(Collection<T> items, IRNG rng, boolean shareRNG)
    {
        this.rng = shareRNG ? rng : rng.copy();
        elements = this.rng.shuffle(items);
        index = 0;
    }

    /**
     * Constructor that takes any Collection of T, shuffles it with an unseeded RNG, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in elements is unique, but that is not necessary and does not affect how this works.
     * @param elements a Collection of T that will not be modified
     */
    public GapShuffler(T[] elements)
    {
        this(elements, new RNG(new LongPeriodRNG()));
    }

    /**
     * Constructor that takes any Collection of T, shuffles it with an unseeded RNG, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in elements is unique, but that is not necessary and does not affect how this works.
     * @param elements a Collection of T that will not be modified
     */
    public GapShuffler(T[] elements, CharSequence seed)
    {
        this(elements, new RNG(new LongPeriodRNG(seed)));
    }

    /**
     * Constructor that takes any Collection of T, shuffles it with the given RNG, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in items is unique, but that is not necessary and does not affect how this works. The
     * rng parameter is copied so externally using it won't change the order this produces its values; the rng field is
     * used whenever the iterator needs to re-shuffle the internal ordering of items. I suggest that the IRNG should
     * use {@link LongPeriodRNG} as its RandomnessSource, since it is in general a good choice for shuffling, or
     * {@link XoshiroStarPhi32RNG} for GWT or other 32-bit platforms, but the choice is unlikely to matter in practice.
     * @param items a Collection of T that will not be modified
     * @param rng an IRNG that can be pre-seeded; will be copied and not used directly
     */
    public GapShuffler(T[] items, IRNG rng)
    {
        this.rng = rng.copy();
        elements = Maker.makeList(items);
        this.rng.shuffleInPlace(elements);
        index = 0;
    }

    /**
     * Constructor that takes any Collection of T, shuffles it with the given RNG, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have at least one "gap" element between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in items is unique, but that is not necessary and does not affect how this works. The
     * rng parameter will be copied if {@code shareRNG} is false, otherwise the reference will be shared (which could
     * make the results of this GapShuffler depend on outside code, though it will always maintain a gap between
     * identical elements if the elements are unique). I suggest that the IRNG should use {@link LongPeriodRNG} as its
     * RandomnessSource, since it is in general a good choice for shuffling, or {@link XoshiroStarPhi32RNG} for GWT or
     * other 32-bit platforms, but the choice is unlikely to matter in practice. Any {@link IStatefulRNG} should work in
     * most cases, like {@link GWTRNG}, {@link StatefulRNG}, or {@link SilkRNG}. If you encounter unusually-low-quality
     * shuffles, try a different starting seed, and if that doesn't work, try SilkRNG (it has a different internal
     * structure than the other types; it could yield better results at the very start).
     * @param items a Collection of T that will not be modified
     * @param rng an IRNG that can be pre-seeded; will be copied and not used directly
     * @param shareRNG if false, {@code rng} will be copied and no reference will be kept; if true, {@code rng} will be shared with the outside code
     */
    public GapShuffler(T[] items, IRNG rng, boolean shareRNG)
    {
        this.rng = shareRNG ? rng : rng.copy();
        elements = Maker.makeList(items);
        this.rng.shuffleInPlace(elements);
        index = 0;
    }

    /**
     * Gets the next element of the infinite sequence of T this shuffles through. This class can be used as an
     * Iterator or Iterable of type T.
     * @return the next element in the infinite sequence
     */
    public T next() {
        int size = elements.size();
        if(size == 1)
        {
            return elements.get(0);
        }
        if(index >= size)
        {
            final int n = size - 1;
            for (int i = n; i > 1; i--) {
                Collections.swap(elements, rng.nextSignedInt(i), i - 1);
            }
            Collections.swap(elements, 1 + rng.nextSignedInt(n), n);
            index = 0;
        }
        return elements.get(index++);
    }
    /**
     * Returns {@code true} if the iteration has more elements.
     * This is always the case for GapShuffler.
     *
     * @return {@code true} always
     */
    @Override
    public boolean hasNext() {
        return true;
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

    /**
     * Returns an <b>infinite</b> iterator over elements of type {@code T}; the returned iterator is this object.
     * You should be prepared to break out of any for loops that use this once you've gotten enough elements!
     * The remove() method is not supported by this iterator and hasNext() will always return true.
     *
     * @return an infinite Iterator over elements of type T.
     */
    @Override
    public Iterator<T> iterator() {
        return this;
    }

    public IRNG getRNG() {
        return rng;
    }


    /**
     * Sets the IRNG this uses to shuffle the order of elements, always copying the given IRNG before using it. Always
     * reshuffles the order, which may eliminate a gap that should have been present, so treat the sequence before and
     * after like separate GapShuffler objects.
     * @param rng an IRNG, such as {@link RNG}, {@link GWTRNG}, {@link StatefulRNG}, and so on; always copied
     */
    public void setRNG(IRNG rng) {
        setRNG(rng, false);
    }

    /**
     * Sets the IRNG this uses to shuffle the order of elements, optionally sharing a reference between outside code and
     * the internal rng (when {@code shareRNG} is true). Always reshuffles the order, which may eliminate a gap that
     * should have been present, so treat the sequence before and after like separate GapShuffler objects.
     * @param rng an IRNG, such as {@link RNG}, {@link GWTRNG}, {@link StatefulRNG}, and so on
     * @param shareRNG if false, {@code rng} will be copied and no reference will be kept; if true, {@code rng} will be shared with the outside code
     */
    public void setRNG(IRNG rng, boolean shareRNG) {
        this.rng = shareRNG ? rng : rng.copy();
        this.rng.shuffleInPlace(elements);
    }

    /**
     * The internal items used here are private, but you can still use this method to put a shallow copy of them into
     * some other Collection. If the type {@code T} is mutable, changes to individual items will be reflected in this
     * GapShuffler, so be careful in that case (if T is immutable, like if it is String, then there's nothing to need to
     * be careful about). This copies each item in the GapShuffler's sequence once, in no particular order, but it may
     * give a prediction of what items this will return in the future (or has already returned).
     * @param coll a Collection that will have each of the possible items this can produce added into it, in no particular order
     */
    public void fillInto(Collection<T> coll) {
        coll.addAll(elements);
    }
    
}
