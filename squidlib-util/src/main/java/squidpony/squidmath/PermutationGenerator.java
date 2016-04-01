// ============================================================================
//   Copyright 2006-2012 Daniel W. Dyer
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// ============================================================================
package squidpony.squidmath;

import java.util.*;

/**
 * Permutation generator for generating all permutations for all sets up to
 * 20 elements in size.  While 20 may seem a low limit, bear in mind that
 * the number of permutations of a set of size n is n!  For a set of 21
 * items, the number of permutations is bigger than can be stored in Java's
 * 64-bit long integer data type.  Therefore it seems unlikely that you
 * could ever generate, let alone process, all of the permutations in a
 * reasonable time frame.  For this reason the implementation is optimised for
 * sets of size 20 or less (this affords better performance by allowing primitive
 * numeric types to be used for calculations rather than
 * {@link java.math.BigInteger}).
 * <br>
 * Originally part of the <a href="http://maths.uncommons.org/">Uncommon Maths software package</a>.
 * @param <T> The type of element that the permutation will consist of.
 * @author Daniel Dyer (modified from the original version written by Michael
 * Gilleland of Merriam Park Software -
 * <a href="http://www.merriampark.com/perm.htm">http://www.merriampark.com/perm.htm</a>).
 * @see CombinationGenerator
 */
public class PermutationGenerator<T> implements Iterable<List<T>>
{
    private final T[] elements;
    private final int[] permutationIndices;
    private long remainingPermutations;
    private long totalPermutations;


    /**
     * Permutation generator that generates all possible orderings of
     * the elements in the specified set.
     * @param elements The elements to permute; will be modified, so this should be copied beforehand
     */
    public PermutationGenerator(T[] elements)
    {
        if (elements.length > 20)
        {
            throw new IllegalArgumentException("Size must be less than or equal to 20.");
        }
        this.elements = elements;
        permutationIndices = new int[elements.length];
        totalPermutations = MathExtras.factorial(elements.length);
        reset();
    }


    /**
     * Permutation generator that generates all possible orderings of
     * the elements in the specified set.
     * @param elements The elements to permute.
     * @param filler An array of T with the same length as elements; needed because GWT can't create a generic array.
     */
    @SuppressWarnings("unchecked")
    public PermutationGenerator(Collection<T> elements, T[] filler)
    {
        this(elements.toArray(filler));
    }


    /**
     * Resets the generator state.
     */
    public final void reset()
    {
        for (int i = 0; i < permutationIndices.length; i++)
        {
            permutationIndices[i] = i;
        }
        remainingPermutations = totalPermutations;
    }


    /**
     * Returns the number of permutations not yet generated.
     * @return The number of unique permutations still to be generated.
     */
    public long getRemainingPermutations()
    {
        return remainingPermutations;
    }


    /**
     * Returns the total number of unique permutations that can be
     * generated for the given set of elements.
     * @return The total number of permutations.
     */
    public long getTotalPermutations()
    {
        return totalPermutations;
    }


    /**
     * Are there more permutations that have not yet been returned?
     * @return true if there are more permutations, false otherwise.
     */
    public boolean hasMore()
    {
        return remainingPermutations > 0;
    }


    /**
     * Generate the next permutation and return an array containing
     * the elements in the appropriate order.  This overloaded method
     * allows the caller to provide an array that will be used and returned.
     * The purpose of this is to improve performance when iterating over
     * permutations. This method allows a single array instance to be reused.
     * @param destination Provides an array to use to create the
     * permutation.  The specified array must be the same length as a
     * permutation.  This is the array that will be returned, once
     * it has been filled with the elements in the appropriate order.
     * @return The next permutation as an array.
     */
    public T[] nextPermutationAsArray(T[] destination)
    {
        if (destination.length != elements.length)
        {
            throw new IllegalArgumentException("Destination array must be the same length as permutations.");
        }
        generateNextPermutationIndices();
        // Generate actual permutation.
        for (int i = 0; i < permutationIndices.length; i++)
        {
            destination[i] = elements[permutationIndices[i]];
        }
        return destination;
    }


    /**
     * Generate the next permutation and return a list containing
     * the elements in the appropriate order.
     * @see #nextPermutationAsList(List)
     * @return The next permutation as a list.
     */
    public List<T> nextPermutationAsList()
    {
        List<T> permutation = new ArrayList<T>(elements.length);
        return nextPermutationAsList(permutation);
    }


    /**
     * Generate the next permutation and return a list containing
     * the elements in the appropriate order.  This overloaded method
     * allows the caller to provide a list that will be used and returned.
     * The purpose of this is to improve performance when iterating over
     * permutations.  If the {@link #nextPermutationAsList()} method is
     * used it will create a new list every time.  When iterating over
     * permutations this will result in lots of short-lived objects that
     * have to be garbage collected.  This method allows a single list
     * instance to be reused in such circumstances.
     * @param destination Provides a list to use to create the
     * permutation.  This is the list that will be returned, once
     * it has been filled with the elements in the appropriate order.
     * @return The next permutation as a list.
     */
    public List<T> nextPermutationAsList(List<T> destination)
    {
        generateNextPermutationIndices();
        // Generate actual permutation.
        destination.clear();
        for (int i : permutationIndices)
        {
            destination.add(elements[i]);
        }
        return destination;
    }

    /**
     * Generate the indices into the elements array for the next permutation.  The
     * algorithm is from Kenneth H. Rosen, Discrete Mathematics and its Applications,
     * 2nd edition (NY: McGraw-Hill, 1991), p. 284)
     */
    private void generateNextPermutationIndices()
    {
        if (remainingPermutations == 0)
        {
            throw new IllegalStateException("There are no permutations remaining.  " +
                                            "Generator must be reset to continue using.");
        }
        else if (remainingPermutations < totalPermutations)
        {
            // Find largest index j with permutationIndices[j] < permutationIndices[j + 1]
            int j = permutationIndices.length - 2;
            while (permutationIndices[j] > permutationIndices[j + 1])
            {
                j--;
            }

            // Find index k such that permutationIndices[k] is smallest integer greater than
            // permutationIndices[j] to the right of permutationIndices[j].
            int k = permutationIndices.length - 1;
            while (permutationIndices[j] > permutationIndices[k])
            {
                k--;
            }

            // Interchange permutation indices.
            int temp = permutationIndices[k];
            permutationIndices[k] = permutationIndices[j];
            permutationIndices[j] = temp;

            // Put tail end of permutation after jth position in increasing order.
            int r = permutationIndices.length - 1;
            int s = j + 1;

            while (r > s)
            {
                temp = permutationIndices[s];
                permutationIndices[s] = permutationIndices[r];
                permutationIndices[r] = temp;
                r--;
                s++;
            }
        }
        --remainingPermutations;
    }

    private int[] getPermutationShift(T[] perm) {
        int[] sh = new int[perm.length];
        boolean[] taken = new boolean[perm.length];

        for (int i = 0; i < perm.length - 1; i++) {
            int ctr = -1;
            for (int j = 0; j < perm.length; j++) {
                if (!taken[j])
                    ctr++;
                if (perm[j] == elements[i]) {
                    taken[j] = true;
                    sh[i] = ctr;
                    break;
                }
            }
        }
        return sh;
    }

    private int[] getPermutationShift(List<T> perm) {
        int length = perm.size();
        int[] sh = new int[length];
        boolean[] taken = new boolean[length];

        for (int i = 0; i < length - 1; i++) {
            int ctr = -1;
            for (int j = 0; j < length; j++) {
                if (!taken[j])
                    ctr++;
                if (perm.get(j) == elements[i]) {
                    taken[j] = true;
                    sh[i] = ctr;
                    break;
                }
            }
        }
        return sh;
    }

    /**
     * Given an array of T that constitutes a permutation of the elements this was constructed with, finds the specific
     * index of the permutation given a factoradic numbering scheme (not used by the rest of this class, except the
     * decodePermutation() method). The index can be passed to decodePermutation to reproduce the permutation passed to
     * this, or modified and then passed to decodePermutation(). Determines equality by identity, not by .equals(), so
     * that equivalent values that have different references/identities can appear in the permuted elements.
     * <br>
     * Credit goes to user Joren on StackOverflow, http://stackoverflow.com/a/1506337
     * @param perm an array of T that must be a valid permutation of this object's elements
     * @return an encoded number that can be used to reconstruct the permutation when passed to decodePermutation()
     */
    public long encodePermutation(T[] perm)
    {
        long e = 0;
        if(perm == null || perm.length != elements.length)
            return e;
        int[] shift = getPermutationShift(perm);
        for (int i = 1; i < shift.length; i++) {
            e += shift[i] * MathExtras.factorialsStart[i];
        }
        return e;
    }

    /**
     * Given a List of T that constitutes a permutation of the elements this was constructed with, finds the specific
     * index of the permutation given a factoradic numbering scheme (not used by the rest of this class, except the
     * decodePermutation() method). The index can be passed to decodePermutation to reproduce the permutation passed to
     * this, or modified and then passed to decodePermutation(). Determines equality by identity, not by .equals(), so
     * that equivalent values that have different references/identities can appear in the permuted elements.
     * <br>
     * Credit goes to user Joren on StackOverflow, http://stackoverflow.com/a/1506337
     * @param perm a List of T that must be a valid permutation of this object's elements
     * @return an encoded number that can be used to reconstruct the permutation when passed to decodePermutation()
     */
    public long encodePermutation(List<T> perm)
    {
        long e = 0;
        if(perm == null || perm.size() != elements.length)
            return e;
        int[] shift = getPermutationShift(perm);
        for (int i = 1; i < shift.length; i++) {
            e += shift[i] * MathExtras.factorialsStart[i];
        }
        return e;
    }

    private int[] factoradicDecode(long e)
    {
        int[] sequence = new int[elements.length];
        int base = 2;

        for (int k = 1; k < elements.length; k++)
        {
            sequence[elements.length - 1 - k] = (int)(e % base);
            e /= base;

            base++; // b[k+1] = b[k] + 1
        }
        return sequence;
    }

    /**
     * Given a long between 0 and the total number of permutations possible (see getTotalPermutations() for how to access
     * this) and an array of T with the same length as the elements this was constructed with, fills the array with the
     * permutation described by the long as a special (factoradic) index into the possible permutations. You can get an
     * index for a specific permutation with encodePermutation() or by generating a random number between 0 and
     * getTotalPermutations(), if you want it randomly.
     * <br>
     * Credit goes to user Joren on StackOverflow, http://stackoverflow.com/a/1506337
     * @param encoded the index encoded as a long
     * @param destination an array of T that must have equivalent length to the elements this was constructed with
     * @return the looked-up permutation, which is the same value destination will be assigned
     */
    public T[] decodePermutation(long encoded, T[] destination)
    {
        if(destination == null)
            return null;
        encoded %= totalPermutations;
        int[] sequence = factoradicDecode(encoded);
        //char[] list = new char[] { 'a', 'b', 'c', 'd', 'e' }; //change for elements

        //char[] permuted = new char[n]; //change for destination
        boolean[] set = new boolean[elements.length];

        for (int i = 0; i < elements.length; i++)
        {
            int s = sequence[i];
            int remainingPosition = 0;
            int index;

            // Find the s'th position in the permuted list that has not been set yet.
            for (index = 0; index < elements.length; index++)
            {
                if (!set[index])
                {
                    if (remainingPosition == s)
                        break;

                    remainingPosition++;
                }
            }

            destination[index] = elements[i];
            set[index] = true;
        }
        return destination;
    }

    /**
     * Given a long between 0 and the total number of permutations possible (see getTotalPermutations() for how to access
     * this), creates a List filled with the permutation described by the long as a special (factoradic) index into the
     * possible permutations. You can get an index for a specific permutation with encodePermutation() or by generating a
     * random number between 0 and getTotalPermutations(), if you want it randomly.
     * <br>
     * Credit goes to user Joren on StackOverflow, http://stackoverflow.com/a/1506337
     * @param encoded the index encoded as a long
     * @return a List of T that corresponds to the permutation at the encoded index
     */
    public List<T> decodePermutation(long encoded)
    {
        ArrayList<T> list = new ArrayList<T>(elements.length);
        Collections.addAll(list, elements);
        return decodePermutation(encoded, list);
    }

    /**
     * Given a long between 0 and the total number of permutations possible (see getTotalPermutations() for how to access
     * this) and a List of T with the same length as the elements this was constructed with, fills the List with the
     * permutation described by the long as a special (factoradic) index into the possible permutations. You can get an
     * index for a specific permutation with encodePermutation() or by generating a random number between 0 and
     * getTotalPermutations(), if you want it randomly.
     * <br>
     * Credit goes to user Joren on StackOverflow, http://stackoverflow.com/a/1506337
     * @param encoded the index encoded as a long
     * @param destination a List of T that must have equivalent size to the elements this was constructed with
     * @return the looked-up permutation, which is the same value destination will be assigned
     */
    public List<T> decodePermutation(long encoded, List<T> destination)
    {
        if(destination == null)
            return null;
        encoded %= totalPermutations;
        int[] sequence = factoradicDecode(encoded);
        //char[] list = new char[] { 'a', 'b', 'c', 'd', 'e' }; //change for elements

        //char[] permuted = new char[n]; //change for destination
        boolean[] set = new boolean[elements.length];

        for (int i = 0; i < elements.length; i++)
        {
            int s = sequence[i];
            int remainingPosition = 0;
            int index;

            // Find the s'th position in the permuted list that has not been set yet.
            for (index = 0; index < elements.length; index++)
            {
                if (!set[index])
                {
                    if (remainingPosition == s)
                        break;

                    remainingPosition++;
                }
            }

            destination.set(index, elements[i]);
            set[index] = true;
        }
        return destination;
    }
    /**
     * <p>Provides a read-only iterator for iterating over the permutations
     * generated by this object.  This method is the implementation of the
     * {@link Iterable} interface that permits instances of this class to be
     * used with the new-style for loop.</p>
     * <p>For example:</p>
     * <pre>
     * List&lt;Integer&gt; elements = Arrays.asList(1, 2, 3);
     * PermutationGenerator&lt;Integer&gt; permutations = new PermutationGenerator(elements);
     * for (List&lt;Integer&gt; p : permutations)
     * {
     *     // Do something with each permutation.
     * }
     * </pre>
     * @return An iterator.
     * @since 1.1
     */
    public Iterator<List<T>> iterator()
    {
        return new Iterator<List<T>>()
        {
            public boolean hasNext()
            {
                return hasMore();
            }


            public List<T> next()
            {
                return nextPermutationAsList();
            }


            public void remove()
            {
                throw new UnsupportedOperationException("Iterator does not support removal.");
            }
        };
    }
}
