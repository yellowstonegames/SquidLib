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
package squidpony.squidai.astar.eg;

import squidpony.annotation.Beta;

import java.io.Serializable;

@Beta
public class Connection<V> extends Edge<V> implements Serializable {
    private static final long serialVersionUID = 1L;

    //================================================================================
    // Fields and constants
    //================================================================================

    protected static final float DEFAULT_WEIGHT = 1;

    protected Node<V> a, b;
    protected float weight = DEFAULT_WEIGHT;

    //================================================================================
    // Constructor
    //================================================================================
    
    public Connection() {
    }
    
    public Connection(Node<V> a, Node<V> b) {
        this.a = a;
        this.b = b;
    }
    
    public Connection(Node<V> a, Node<V> b, float weight) {
        this.a = a;
        this.b = b;
        this.weight = weight;
    }

    @Override
    protected void set(Node<V> a, Node<V> b, float weight) {
        this.a = a;
        this.b = b;
        this.weight = weight;
    }

    //================================================================================
    // Internal methods
    //================================================================================

    @Override
    protected Node<V> getInternalNodeA() {
        return a;
    }

    @Override
    protected Node<V> getInternalNodeB() {
        return b;
    }

    //================================================================================
    // Public methods
    //================================================================================

    @Override
    public V getA() {
        return a.object;
    }

    @Override
    public V getB() {
        return b.object;
    }

    @Override
    public float getWeight() {
        return weight;
    }

    @Override
    public void setWeight(float weight) {
        this.weight = weight;
    }

    public Node<V> getNodeA() {
        return a;
    }

    public Node<V> getNodeB() {
        return b;
    }

    //================================================================================
    // Subclasses
    //================================================================================

    public static class DirectedConnection<V> extends Connection<V> {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Connection edge = (Connection) o;
            // this assumes a and b are non-null when equals() is called.
            return a.equals(edge.a) && b.equals(edge.b);
        }

        @Override
        public int hashCode() {
            return (int) (a.hashCode() * 0xC13FA9A902A6328FL + b.hashCode() * 0x91E10DA5C79E7B1DL >>> 32);
        }

        @Override
        public String toString() {
            return "{" + a + " -> " + b +'}';
        }

    }

    public static class UndirectedConnection<V> extends Connection<V> {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Connection edge = (Connection) o;
            // this assumes a and b are non-null when equals() is called.
            return (a.equals(edge.a) && b.equals(edge.b))
                    || (a.equals(edge.b) && b.equals(edge.a));
        }

        @Override
        public int hashCode() {
            return (int) ((a.hashCode() + b.hashCode()) * 0x9E3779B97F4A7C15L >>> 32);
        }

        @Override
        public String toString() {
            return "{" + a + " <> " + b +'}';
        }
    }

}
