/* blacken - a library for Roguelike games
 * Copyright © 2010-2012 Steven Black <yam655@gmail.com>
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

package com.googlecode.blacken.bsp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.googlecode.blacken.core.Random;
import com.googlecode.blacken.grid.BoxRegion;
import com.googlecode.blacken.grid.BoxRegionIterator;
import com.googlecode.blacken.grid.Point;
import com.googlecode.blacken.grid.Positionable;
import com.googlecode.blacken.grid.RegionIterator;
import com.googlecode.blacken.grid.Regionlike;
import com.googlecode.blacken.grid.SimpleSize;
import com.googlecode.blacken.grid.Sizable;

/**
 * An implementation of Binary Screen Partitioning Trees, useful for quick and good dungeon generation.
 *
 * @param <R> room object contained in tree
 * @author XLambda
 * @since Blacken 1.1
 */

public class BSPTree<R> implements Regionlike {

    private int x;
    private int y;
    private int width;
    private int height;
    private int level = 0;
    private int position = -1;
    private boolean horizontal = false;
    private BSPTree leftChild = null;
    private BSPTree rightChild = null;
    private BSPTree parent = null;
    private R contained = null;

    /**
     * Creates a new single-node tree which can be split to refine it.
     *
     * @param bounds the tree's bounds
     */
    public BSPTree(Regionlike bounds) {
        this.x = bounds.getX();
        this.y = bounds.getY();
        this.width = bounds.getWidth();
        this.height = bounds.getHeight();
    }

    /**
     * Creates a new single-node tree which can be split to refine it.
     *
     * @param height the tree's height
     * @param width the tree's width
     * @param y the tree's y ordinate
     * @param x the tree's x ordinate
     */
    public BSPTree(int height, int width, int y, int x) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Assign datum to the leaf node.
     * @param contained
     */
    public void setContained(R contained) {
        if (!isLeaf()) {
            throw new IllegalStateException("Only leaf nodes can contain data");
        }
        this.contained = contained;
    }
    /**
     * Get the leaf node's contained data.
     * @return
     */
    public R getContained() {
        return this.contained;
    }

    /**
     * This mostly exists to help with debugging.
     *
     * @param height
     * @param width
     * @param y
     * @param x
     * @param level
     * @param position
     * @param horizontal
     */
    protected BSPTree(int height, int width, int y, int x, int level, int position, boolean horizontal) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.level = level;
        this.position = position;
        this.horizontal = horizontal;
        this.leftChild = null;
        this.rightChild = null;
        this.parent = null;
    }

    /**
     *  Constructor of BSPTree.
     *  Creates a new sub-tree for an existing BSPTree.
     *  Do not use for splitting a tree.
     *  @param parent the father node
     *  @param left true to create a left child, false to create a right child
     */

    public BSPTree(BSPTree parent, boolean left) {
        if(parent.isHorizontal()) {
            this.x = parent.getX();
            this.width = parent.getWidth();
            if(left) {
                this.y = parent.getY();
                this.height = parent.getSplitPosition() - this.y;
            } else {
                this.y = parent.getSplitPosition();
                this.height = parent.getY() + parent.getHeight() - parent.getSplitPosition();
            }
        } else {
            this.y = parent.getY();
            this.height = parent.getHeight();
            if(left) {
                this.x = parent.getX();
                this.width = parent.getSplitPosition() - this.x;
            } else {
                this.x = parent.getSplitPosition();
                this.width = parent.getX() + parent.getWidth() - parent.getSplitPosition();
            }
        }
        this.level = parent.getLevel() + 1;
        this.parent = parent;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    /**
     * Returns the node's level.
     *
     * Zero is the root node.
     *
     * @return the node's level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the place in which the node is split. 
     * 
     * See {@link #isHorizontal()} for the orientation of the split.
     *
     * @return the place in which the node is split.
     */
    public int getSplitPosition() {
        return position;
    }


    /**
     *  Returns the node's orientation when split.
     *  @return true if horizontal, false if vertical
     */
    public boolean isHorizontal() {
        return horizontal;
    }

    /**
     *  @return the node's left child
     */
    public BSPTree getLeftChild() {
        return leftChild;
    }

    /**
     *  @return the node's right child
     */
    public BSPTree getRightChild() {
        return rightChild;
    }

    /**
     *  @return the node's parent
     */
    public BSPTree getParent() {
        return parent;
    }

    /**
     * @return the node's father
     * @deprecated use {@link #getParent()} instead.
     */
    @Deprecated
    public BSPTree getFather() {
        return parent;
    }

    /**
     *  Checks whether the node is a leaf.
     *  @return true, if the node is a leaf, false otherwise
     */
    public boolean isLeaf() {
        if(leftChild == null && rightChild == null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean contains(int py, int px) {
        return (px >= x && py >= y && px < x+width && py < y+height);
    }

    /**
     *  Finds the leaf of the tree containing the given coordinates.
     *  @param px the x ordinate
     *  @param py the y ordinate
     *  @return the leaf containing the coordinates, or null if the tree does not contain the coordinates.
     */
    public BSPTree findNode(int py, int px) {
        if(!contains(px,py)) {
            return null;
        }
        if(!isLeaf()) {
            if(leftChild.contains(px,py)) {
                return leftChild.findNode(px,py);
            } else if(rightChild.contains(px,py)) {
                return rightChild.findNode(px,py);
            }
        }
        return this;
    }

    /**
     * Find the top-down order of the tree. (libtcod calls it "preorder")
     *
     * <p>If you want to use a callback with this, send in a custom Collection
     * that overrides <code>Collection.add(Object)</code>. No other Collection
     * functions are used on the <code>nodelist</code> object.</p>
     *
     * @param nodelist returns new collection if null
     *  @return List of the tree's nodes ordered top-down.
     */
    public Collection<BSPTree> findTopOrder(Collection<BSPTree> nodelist) {
        if(nodelist == null) {
            nodelist = new LinkedList<>();
        }
        nodelist.add(this);
        if(leftChild != null) {
            leftChild.findTopOrder(nodelist);
        }
        if(rightChild !=null) {
            rightChild.findTopOrder(nodelist);
        }
        return nodelist;
    }

    /**
     *  Traverses the tree in preorder.
     *
     * <p>We're not really "traversing" the tree so much as just returning all
     * the desired nodes. We're changing the name of the function to clarify
     * that. We will keep this function indefinitely to help with folks
     * familiar with libtcod.</p>
     *
     * @param nodelist the target list. If null is passed, a new list will be created.
     * @return a List of the tree's nodes in preorder.
     * @deprecated Use {@link #findInorder(java.util.Collection)} instead.
     */
    public List<BSPTree> traversePreorder(List<BSPTree> nodelist) {
        return (List)findTopOrder(nodelist);
    }

    /**
     * Find the order for the tree. (libtcod calls this "inorder")
     *
     * <p>This is neither a top-down nor a bottom-up order.</p>
     *
     * <p>If you want to use a callback with this, send in a custom Collection
     * that overrides <code>Collection.add(Object)</code>. No other Collection
     * functions are used on the <code>nodelist</code> object.</p>
     *
     * @param nodelist returns new collection if null
     *  @return a List of the tree's nodes in inorder
     */
    public Collection<BSPTree> findInOrder(Collection<BSPTree> nodelist) {
        if(nodelist == null) {
            nodelist = new ArrayList<>();
        }
        if(leftChild != null) {
            leftChild.findInOrder(nodelist);
        }
        nodelist.add(this);
        if(rightChild != null) {
            rightChild.findInOrder(nodelist);
        }
        return nodelist;
    }

    /**
     * Traverses the tree in inorder.
     *
     * <p>We're not really "traversing" the tree so much as just returning all
     * the desired nodes. We're changing the name of the function to clarify
     * that. We will keep this function indefinitely to help with folks
     * familiar with libtcod.</p>
     *
     * @param nodelist the target list. If null is passed, a new list will be created.
     * @deprecated Use {@link #findInorder(java.util.Collection)} instead.
     * @return a List of the tree's nodes in inorder
     */
    public List<BSPTree> traverseInorder(List<BSPTree> nodelist) {
        return (List)findInOrder(nodelist);
    }

    /**
     * Find the bottom-up order of the tree. (libtcod calls it "preorder")
     *
     * <p>If you want to use a callback with this, send in a custom Collection
     * that overrides <code>Collection.add(Object)</code>. No other Collection
     * functions are used on the <code>nodelist</code> object.</p>
     *
     * @param nodelist returns new collection if null
     * @return a List of the tree's nodes in postorder
     */
    public Collection<BSPTree> findBottomOrder(Collection<BSPTree> nodelist) {
        if(nodelist == null) {
            nodelist = new ArrayList<>();
        }
        if(leftChild != null) {
            leftChild.findBottomOrder(nodelist);
        }
        if(rightChild != null) {
            rightChild.findBottomOrder(nodelist);
        }
        nodelist.add(this);
        return nodelist;
    }

    /**
     * Traverses the tree in postorder.
     *
     * <p>We're not really "traversing" the tree so much as just returning all
     * the desired nodes. We're changing the name of the function to clarify
     * that. We will keep this function indefinitely to help with folks
     * familiar with libtcod.</p>
     *
     * @param nodelist the target list. If null is passed, a new list will be created.
     * @deprecated use {@link #findBottomOrder(java.util.Collection) instead.
     * @return a List of the tree's nodes in postorder
     */
    public List<BSPTree> traversePostorder(List<BSPTree> nodelist) {
        return (List)findBottomOrder(nodelist);
    }

    /**
     * Find all the leaf nodes for this section of the tree.
     * @param nodelist
     * @return all the 'contained' objects
     */
    public Collection<BSPTree> findLeaves(Collection<BSPTree> nodelist) {
        // This demonstrates tree traversal using Collection-overriding
        // Java IDEs will fill out the skeleton, so this was trivial to implement.
        class GetLeaves implements Collection<BSPTree> {
            Collection<BSPTree> collector;
            GetLeaves(Collection<BSPTree> collector) {
                this.collector = collector;
            }
            @Override
            public boolean add(BSPTree e) {
                if (e.isLeaf()) {
                    collector.add(e);
                }
                return true;
            }

            @Override
            public int size() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override
            public boolean isEmpty() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override
            public boolean contains(Object o) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override
            public Iterator<BSPTree> iterator() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override
            public Object[] toArray() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override
            public <T> T[] toArray(T[] a) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean addAll(Collection<? extends BSPTree> c) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
        Collection<BSPTree> ret = nodelist;
        if (ret == null) {
            ret = new ArrayList<>();
        }
        findLevelOrder(new GetLeaves(ret));
        return ret;
    }

    /**
     * Find all the 'contained' items under this section of the tree.
     * @param c collection of the 'ontained' objects
     * @return all the 'contained' objects
     */
    public Collection<R> findContained(Collection<R> c) {
        // This demonstrates tree traversal using Collection-overriding
        // Java IDEs will fill out the skeleton, so this was trivial to implement.
        class GetContained implements Collection<BSPTree> {
            Collection<R> collector;
            GetContained(Collection<R> collector) {
                this.collector = collector;
            }
            @Override
            public boolean add(BSPTree e) {
                R contained = (R)e.getContained();
                if (contained != null) {
                    collector.add(contained);
                }
                return true;
            }

            @Override
            public int size() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override
            public boolean isEmpty() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override
            public boolean contains(Object o) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override
            public Iterator<BSPTree> iterator() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override
            public Object[] toArray() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override
            public <T> T[] toArray(T[] a) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean addAll(Collection<? extends BSPTree> c) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
        Collection<R> ret = c;
        if (ret == null) {
            ret = new ArrayList<>();
        }
        findLevelOrder(new GetContained(ret));
        return ret;
    }

    /**
     *  Find the level order for the tree.
     *
     * <p>If you want to use a callback with this, send in a custom Collection
     * that overrides <code>Collection.add(Object)</code>. No other Collection
     * functions are used on the <code>nodelist</code> object.</p>
     *
     * <p>Implementation note: This is currently the most efficient way to get
     * the tree's nodes. If that's your only goal, use this function.</p>
     *
     *  @param nodelist the target list. If null is passed, a new list will be created.
     *  @return a List of the tree's nodes in level order
     */
    public Collection<BSPTree> findLevelOrder(Collection<BSPTree> nodelist) {
        if(nodelist == null) {
            nodelist = new ArrayList<>();
        }
        List<BSPTree> q = new ArrayList<>();
        q.add(this);
        while(!q.isEmpty()) {
            BSPTree currentNode = q.remove(0);
            nodelist.add(currentNode);
            if(!currentNode.isLeaf()) {
                q.add(currentNode.getLeftChild());
                q.add(currentNode.getRightChild());
            }
        }
        return nodelist;
    }

    /**
     *  Traverses the tree in level order.
     *
     * <p>We're not really "traversing" the tree so much as just returning all
     * the desired nodes. We're changing the name of the function to clarify
     * that. We will keep this function indefinitely to help with folks
     * familiar with libtcod.</p>
     *
     *  @param nodelist the target list. If null is passed, a new list will be created.
     *  @return a List of the tree's nodes in level order
     * @deprecated Use {@link #findLevelOrder(java.util.Collection)} instead.
     */
    public List<BSPTree> traverseLevelOrder(List<BSPTree> nodelist) {
        return (List)findLevelOrder(nodelist);
    }

    /**
     *  Traverses the tree in inverted level order.
     *
     * <p>If you want to use a callback with this, send in a custom Collection
     * that overrides <code>Collection.addAll(Collection)</code> or
     * <code>Collection.add(Object)</code>. No other Collection
     * functions are used on the <code>nodelist</code> object. (We first try
     * the <code>addAll</code> function, and if it throws an exception we
     * use separate <code>add</code> commands.)</p>
     *
     *  @param nodelist the target list. If null is passed, a new list will be generated.
     *  @return a List of the tree's nodes in inverted level order
     */
    public Collection<BSPTree> findInvertedLevelOrder(Collection<BSPTree> nodelist) {
        List<BSPTree> first = new ArrayList<>();
        Collection<BSPTree> ret = nodelist;
        if (ret == null) {
            ret = new ArrayList<>();
        }
        findLevelOrder(first);
        Collections.reverse(first);
        // If we didn't want a simpler override interface, we would use addAll()
        try {
            nodelist.addAll(first);
        } catch(RuntimeException e) {
            for (BSPTree node : first) {
                ret.add(node);
            }
        }
        return ret;
    }

    /**
     *  Traverses the tree in inverted level order.
     *
     * <p>We're not really "traversing" the tree so much as just returning all
     * the desired nodes. We're changing the name of the function to clarify
     * that. We will keep this function indefinitely to help with folks
     * familiar with libtcod.</p>
     *
     *  @param nodelist the target list. If null is passed, a new list will be generated.
     *  @return a List of the tree's nodes in inverted level order
     */
    public List<BSPTree> traverseInvertedLevelOrder(List<BSPTree> nodelist) {
        return (List)findInvertedLevelOrder(nodelist);
    }

    /**
     *  Splits the tree by adding two children to the node.
     *  @param horizontal orientation of the split
     *  @param position place in which the tree is to be split
     *
     */
    public void splitOnce(boolean horizontal, int position) {
        if (this.leftChild != null) {
            return;
        }
        this.horizontal = horizontal;
        this.position = position;
        BSPTree lc = new BSPTree(this, true);
        BSPTree rc = new BSPTree(this, false);
        this.leftChild = lc;
        this.rightChild = rc;
    }

    /**
     * Splits the tree recursively, based on size and recursion depth 
     * constraints.
     *
     * <p>A node will only be split if the resulting subnodes are at least
     * minVSize x minHSize large.</p>
     *
     * <p>If a split node does not fit the maxVRatio, the split  orientation
     * will be changed to achieve a ratio smaller than maxHRatio</p>
     *
     *  @param rng If null uses the default instance
     *  @param recursionDepth Due to size constraints this might no be reached.
     *  @param minVSize the minumum height of a node
     *  @param minHSize the minimum width of a node
     *  @param maxVRatio the maximum height/width ratio 
     *  @param maxHRatio the maximum width/height ratio
     */
    public void splitRecursive(Random rng, int recursionDepth,
            int minVSize, int minHSize, double maxVRatio, double maxHRatio) {
        if(recursionDepth == 0 || width < 2*minHSize || height < 2*minVSize) {
            return;
        }
        boolean horiz;
        if(rng == null) {
            rng = Random.getInstance();
        }
        if(height < 2*minVSize || width > height * maxHRatio) {
            horiz = false;
        } else if (width < 2*minHSize || height > width * maxVRatio) {
            horiz = true;
        } else {
            horiz = rng.nextBoolean();
        }
        int pos;
        if(horiz) {
            pos = rng.nextInt(y+minVSize,y+height-minVSize);
        } else {
            pos = rng.nextInt(x+minHSize,x+width-minHSize);
        }
        if (this.leftChild == null && this.rightChild == null) {
            splitOnce(horiz, pos);
        }
        leftChild.splitRecursive(rng, recursionDepth-1, minVSize, minHSize, maxVRatio, maxHRatio);
        rightChild.splitRecursive(rng, recursionDepth-1, minVSize, minHSize, maxVRatio, maxHRatio);
    }

    /**
     * Splits the tree recursively, based on size and recursion depth
     * constraints.
     *
     * <p>A node will only be split if the resulting subnodes are at least
     * minVSize x minHSize large.</p>
     *
     * <p>If a split node does not fit the maxVRatio, the split  orientation
     * will be changed to achieve a ratio smaller than maxHRatio</p>
     *
     *  @param rng If null uses the default instance
     *  @param recursionDepth Due to size constraints this might no be reached.
     *  @param minVSize the minumum height of a node
     *  @param minHSize the minimum width of a node
     */
    public void splitRecursive(Random rng, int recursionDepth,
            int minVSize, int minHSize) {
        splitRecursive(rng, recursionDepth, minVSize, minHSize,
                3000, 4000);
    }

    /**
     * Splits the tree recursively, based on size and recursion depth
     * constraints.
     *
     * <p>A node will only be split if the resulting subnodes are at least
     * minVSize x minHSize large.</p>
     *
     * <p>If a split node does not fit the maxVRatio, the split  orientation
     * will be changed to achieve a ratio smaller than maxHRatio</p>
     *
     * <p>This is an integer-math version of 
     * {@link #splitRecursive(com.googlecode.blacken.core.Random, int, int, int, double, double)}</p>
     *
     *  @param rng If null uses the default instance
     *  @param recursionDepth Due to size constraints this might no be reached.
     *  @param minVSize the minumum height of a node
     *  @param minHSize the minimum width of a node
     *  @param maxVRatio the maximum height/width ratio (times 1000)
     *  @param maxHRatio the maximum width/height ratio (times 1000)
     */
    public void splitRecursive(Random rng, int recursionDepth,
            int minVSize, int minHSize, int maxVRatio, int maxHRatio) {
        if(recursionDepth == 0 || width < 2*minHSize || height < 2*minVSize) {
            return;
        }
        boolean horiz;
        if(rng == null) {
            rng = Random.getInstance();
        }
        if(height < 2*minVSize || width > height * maxHRatio / 1000) {
            horiz = false;
        } else if (width < 2*minHSize || height > width * maxVRatio / 1000) {
            horiz = true;
        } else {
            horiz = rng.nextBoolean();
        }
        int pos;
        if(horiz) {
            pos = rng.nextInt(y+minVSize,y+height-minVSize);
        } else {
            pos = rng.nextInt(x+minHSize,x+width-minHSize);
        }
        if (this.leftChild == null && this.rightChild == null) {
            splitOnce(horiz, pos);
        }
        leftChild.splitRecursive(rng, recursionDepth-1, minVSize, minHSize, maxVRatio, maxHRatio);
        rightChild.splitRecursive(rng, recursionDepth-1, minVSize, minHSize, maxVRatio, maxHRatio);
    }

    /**
     *  Resizes the tree and all its subtrees, without changing the splitting orientation and position.
     *  This should only be called on the tree to enlarge it - shrinking may cause splits to be outside the repective node. Leafs can of course be shrunk safely, but should not be enlarged beyond their original size, otherwise a coordinate may be contained in more than one leaf.
     *  @param x the tree's new x ordinate
     *  @param y the tree's new y ordinate
     *  @param width the tree's new width
     *  @param height the tree's new height
     */
    @Override
    public void setBounds(int y, int x, int height, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        if(!this.isLeaf()) {
            if(isHorizontal()) {
                leftChild.setBounds(y, x, position-y, width);
                rightChild.setBounds(position, x, y+height-position, width);
            } else {
                leftChild.setBounds(y, x, height, position-x);
                rightChild.setBounds(y, position, height, x+width-position);
            }
        }
    }
    @Override
    public void setBounds(Regionlike r) {
        this.setBounds(r.getY(), r.getX(), r.getHeight(), r.getWidth());
    }

    @Override
    public boolean contains(int height, int width, int y1, int x1) {
        return BoxRegion.contains(this, height, width, y1, x1);
    }

    @Override
    public boolean contains(Positionable p) {
        return this.contains(p.getY(), p.getX());
    }

    @Override
    public boolean contains(Regionlike r) {
        return BoxRegion.contains(this, r);
    }

    @Override
    public Regionlike getBounds() {
        return new BoxRegion(height, width, y, x);
    }

    @Override
    public RegionIterator getEdgeIterator() {
        RegionIterator ret = new BoxRegionIterator(this, true, false);
        return ret;
    }

    @Override
    public RegionIterator getInsideIterator() {
        RegionIterator ret = new BoxRegionIterator(this, false, false);
        return ret;
    }

    @Override
    public RegionIterator getNotOutsideIterator() {
        RegionIterator ret = new BoxRegionIterator(this, false, true);
        return ret;
    }

    @Override
    public boolean intersects(int height, int width, int y1, int x1) {
        return BoxRegion.intersects(this, height, width, y1, x1);
    }

    @Override
    public boolean intersects(Regionlike room) {
        return BoxRegion.intersects(this, room);
    }

    @Override
    public Positionable getPosition() {
        return new Point(y, x);
    }

    @Override
    public void setX(int x) {
        throw new UnsupportedOperationException("BSP Trees are not movable");
    }

    @Override
    public void setY(int y) {
        throw new UnsupportedOperationException("BSP Trees are not movable");
    }

    @Override
    public void setPosition(int y, int x) {
        throw new UnsupportedOperationException("BSP Trees are not movable");
    }

    @Override
    public void setPosition(Positionable point) {
        throw new UnsupportedOperationException("BSP Trees are not movable");
    }

    @Override
    public Sizable getSize() {
        return new SimpleSize(height, width);
    }

    @Override
    public void setHeight(int height) {
        throw new UnsupportedOperationException("BSP Trees are not individually resizable");
    }

    @Override
    public void setWidth(int width) {
        throw new UnsupportedOperationException("BSP Trees are not individually resizable");
    }

    @Override
    public void setSize(int height, int width) {
        throw new UnsupportedOperationException("BSP Trees are not individually resizable");
    }

    @Override
    public void setSize(Sizable size) {
        throw new UnsupportedOperationException("BSP Trees are not individually resizable");
    }

    @Override
    public String toString() {
        String right = "-";
        String left = "-";
        if (rightChild != null) {
            right = rightChild.toString();
        }
        if (leftChild != null) {
            left = leftChild.toString();
        }
        return String.format("{Level: %s; Position: %s,%s; Size: %s,%s; "
                + "Split: %s; %s; Level: %s;\n Right:%s;\n Left:%s}",
                level, getY(), getX(), getHeight(), getWidth(), position,
                (horizontal ? "Horizontal" : "Vertical"), level, right, left);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BSPTree other = (BSPTree) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        if (this.width != other.width) {
            return false;
        }
        if (this.height != other.height) {
            return false;
        }
        if (this.level != other.level) {
            return false;
        }
        if (this.position != other.position) {
            return false;
        }
        if (this.horizontal != other.horizontal) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + this.x;
        hash = 37 * hash + this.y;
        hash = 37 * hash + this.width;
        hash = 37 * hash + this.height;
        hash = 37 * hash + this.level;
        hash = 37 * hash + this.position;
        hash = 37 * hash + (this.horizontal ? 1 : 0);
        return hash;
    }
}
