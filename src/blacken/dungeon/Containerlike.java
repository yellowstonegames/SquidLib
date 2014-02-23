/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.googlecode.blacken.dungeon;

import java.util.Collection;
import java.util.Iterator;

/**
 * A generic container-like interface supporting fuzzy typing.
 *
 * @param <T> the type of item contained
 * @author Steven Black
 * @since Blacken 1.1
 */
public interface Containerlike<T> extends Collection<T> {
    /**
     * Can this contain the <code>thing</code>?
     * @param thing
     * @return false when incompatible type or container full; true otherwise
     */
    public boolean canFit(T thing);
    /**
     * Request similar items from this container.
     * @param judge determiner for what is similar
     * @return
     */
    public Collection<T> findSimilar(ThingTypeCheck<T> judge);
    /**
     * Set the typing verifier
     *
     * <p>Use <code>null</code> to disable the typing verifier.</code>
     *
     * @param judge determiner for what can fit in this container
     */
    public void setVerifier(ThingTypeCheck<T> judge);
    /**
     * Get the typing verifier
     * @return null if none specified
     */
    public ThingTypeCheck<T> getVerifier();
    /**
     * Is a size limit in effect?
     * @return
     */
    public boolean hasSizeLimit();
    /**
     * Get the current size limit
     * @return -1 if none; otherwise size limit
     */
    public int getSizeLimit();
    /**
     * Set the size limit.
     * 
     * @param limit new size limit (-1 to disable)
     * @return old size limit (-1 if none)
     * @throws IllegalStateException already contains too many items
     */
    public int setSizeLimit(int limit) throws IllegalStateException;

    @Override
    public boolean add(T e);
    /**
     * Potentially add to the container.
     * @param e
     * @return true if added; false if not added
     */
    public boolean offer(T e);
    /**
     * Remove all contained items that fail our current verifier.
     * 
     * <p>If there is no verifier set this will return the empty list.
     * 
     * @return the unfit items
     */
    public Collection<T> removeUnfit();

    /**
     * Are there any items that would fail the current verifier?
     *
     * <p>Important note: You want to either call this function or the
     * {@link #removeUnfit()} function -- never both. This function is
     * primarily useful in exception-throwing cases. If you're going to remove
     * the unfit items then just do it -- otherwise you risk scanning the
     * contained items twice.
     *
     * @return true if there are unfit items; false otherwise
    */
    public boolean hasUnfit();

    @Override
    public int size();
    @Override
    public boolean isEmpty();
    @Override
    public boolean contains(Object o);
    @Override
    public Iterator<T> iterator();
    @Override
    public T[] toArray();
    @Override
    public <T> T[] toArray(T[] a);
    @Override
    public boolean remove(Object o);
    @Override
    public boolean containsAll(Collection<?> c);
    @Override
    public boolean addAll(Collection<? extends T> c);
    @Override
    public boolean removeAll(Collection<?> c);
    @Override
    public boolean retainAll(Collection<?> c);
    @Override
    public void clear();

}
