/* blacken - a library for Roguelike games
 * Copyright © 2012 Steven Black <yam655@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.googlecode.blacken.dungeon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * A simple implementation of a {@link Containerlike} object.
 *
 * <p>This holds things, with an optional size limit, and an optional (sub)type
 * verifier.
 *
 * @param <T> type that is contained
 * @author Steven Black
 */
public class SimpleContainer<T> implements Containerlike<T> {
    private ThingTypeCheck verifier = null;
    private int sizeLimit = -1;
    private List<T> storage = new ArrayList<>();

    /**
     * Create a new container with no verifier and no size limit.
     */
    public SimpleContainer() {
    }
    /**
     * Create a new container with no size limit, but with a verifier.
     * @param verifier
     */
    public SimpleContainer(ThingTypeCheck<T> verifier) {
        this.verifier = verifier;
    }
    /**
     * Create a new container with both a verifier and a size limit
     * @param verifier
     * @param limit
     */
    public SimpleContainer(ThingTypeCheck<T> verifier, int limit) {
        this.verifier = verifier;
        if (limit >= 0) {
            this.sizeLimit = limit;
        }
    }

    @Override
    public boolean canFit(T thing) {
        if (this.verifier != null) {
            if (!this.verifier.isSufficient(thing)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Collection<T> findSimilar(ThingTypeCheck<T>judge) {
        List<T> ret = new ArrayList<>();
        if (judge == null) {
            throw new NullPointerException("Need a judge.");
        }
        for (T thing : this) {
            if (judge.isSufficient(thing)) {
                ret.add(thing);
            }
        }
        return ret;
    }

    @Override
    public Collection<T> removeUnfit() {
        if (this.verifier == null) {
            return Collections.emptyList();
        }
        Collection<T> unfit = new ArrayList<>();
        ListIterator<T> iter = this.storage.listIterator();
        while(iter.hasNext()) {
            T thing = iter.next();
            if (!verifier.isSufficient(thing)) {
                unfit.add(thing);
                iter.remove();
            }
        }
        return unfit;
    }
    @Override
    public boolean hasUnfit() {
        if (this.verifier == null) {
            return false;
        }
        boolean ret = false;
        for (T thing : storage) {
            if (!verifier.isSufficient(thing)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    @Override
    public void setVerifier(ThingTypeCheck<T> verifier) {
        this.verifier = verifier;
    }

    @Override
    public ThingTypeCheck<T> getVerifier() {
        return verifier;
    }

    @Override
    public boolean hasSizeLimit() {
        return sizeLimit >= 0;
    }

    @Override
    public int getSizeLimit() {
        if (sizeLimit < 0) {
            return -1;
        }
        return sizeLimit;
    }

    @Override
    public int setSizeLimit(int limit) throws IllegalStateException {
        if (sizeLimit == limit) {
            return sizeLimit;
        }
        int oldLimit = sizeLimit;
        if (limit < 0) {
            sizeLimit = -1;
        } else if (limit < this.size()) {
            throw new IllegalStateException("Too late. It is already too big.");
        } else {
            sizeLimit = limit;
        }
        return oldLimit;
    }

    @Override
    public boolean add(T o) {
        if (o == null) {
            throw new NullPointerException("parameter cannot be null");
        }
        // throw a class cast exception sooner rather than later.
        T thing = (T)o;
        if (sizeLimit >= 0 && storage.size() >= sizeLimit) {
            throw new IllegalStateException("Already full");
        } else if (this.canFit(thing)) {
            return this.storage.add(thing);
        } else {
            throw new IllegalStateException("Can't fit that there.");
        }
    }

    @Override
    public boolean offer(T o) {
        if (o == null) {
            throw new NullPointerException("parameter cannot be null");
        }
        // throw a class cast exception sooner rather than later.
        T thing = (T)o;
        if (this.canFit(thing)) {
            this.storage.add(thing);
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return this.storage.size();
    }

    @Override
    public boolean isEmpty() {
        return this.storage.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.storage.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return storage.iterator();
    }

    @Override
    public T[] toArray() {
        return (T[])storage.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return storage.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return storage.remove(o);
    }

    @Override
    public boolean containsAll(Collection c) {
        return storage.containsAll(c);
    }

    @Override
    public boolean addAll(Collection c) {
        for (Object object : c) {
            storage.add((T)object);
        }
        return storage.addAll(c);
    }

    @Override
    public boolean removeAll(Collection c) {
        return storage.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection c) {
        return storage.retainAll(c);
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SimpleContainer<T> other = (SimpleContainer<T>) obj;
        if (!Objects.equals(this.verifier, other.verifier)) {
            return false;
        }
        if (this.sizeLimit != other.sizeLimit) {
            return false;
        }
        if (!Objects.equals(this.storage, other.storage)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.verifier);
        hash = 59 * hash + this.sizeLimit;
        hash = 59 * hash + this.storage.hashCode();
        return hash;
    }

}
