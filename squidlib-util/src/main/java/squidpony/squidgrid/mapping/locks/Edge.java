package squidpony.squidgrid.mapping.locks;

/**
 * Links two {@link Room}s.
 * <p>
 * The attached {@link Symbol} is a condition that must be satisfied for the
 * player to pass from one of the linked Rooms to the other via this Edge. It is
 * implemented as a {@link Symbol} rather than a {@link Condition} to simplify
 * the interface to clients of the library so that they don't have to handle the
 * case where multiple Symbols are required to pass through an Edge.
 * <p>
 * An unconditional Edge is one that may always be used to go from one of the
 * linked Rooms to the other.
 */
public class Edge {

    protected int targetRoomId;
    protected int symbol;
   
    /**
     * Creates an unconditional Edge.
     */
    public Edge(int targetRoomId) {
        this(targetRoomId, Symbol.NOTHING);
    }
    
    /**
     * Creates an Edge that requires a particular Symbol to be collected before
     * it may be used by the player to travel between the Rooms.
     * 
     * @param symbol    the symbol that must be obtained
     */
    public Edge(int targetRoomId, int symbol) {
        this.targetRoomId = targetRoomId;
        this.symbol = symbol;
    }
    
    /**
     * @return  whether the Edge is conditional
     */
    public boolean hasSymbol() {
        return symbol != Symbol.NOTHING;
    }
    
    /**
     * @return  the symbol that must be obtained to pass along this edge or null
     *          if there are no required symbols
     */
    public int getSymbol() {
        return symbol;
    }
    
    public void setSymbol(int symbol) {
        this.symbol = symbol;
    }
    
    public int getTargetRoomId() {
        return targetRoomId;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Edge) {
            Edge o = (Edge)other;
            return targetRoomId == o.targetRoomId &&
                    symbol == o.symbol;
        } else {
            return super.equals(other);
        }
    }
    
}
