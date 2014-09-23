package squidpony.squidgrid.mapping.shape;

import java.util.Arrays;
import squidpony.annotation.Beta;

/**
 * This class represent the edge (or corner) of a Wang Tile.
 *
 * All tiles need to have sizes that are multiples of the length in this class.
 * The length itself is how much edge will be matched, so tiles larger than this
 * will be broken into logical sub-edges for matching.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class WangEdge {

    public static int length = 1;//minimum (yet trivial) size of a tile is 1x1
    
    public boolean[] edge;

    public WangEdge(boolean[] edge) {
        this.edge = edge;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Arrays.hashCode(this.edge);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WangEdge other = (WangEdge) obj;
        if (!Arrays.equals(this.edge, other.edge)) {
            return false;
        }
        return true;
    }
}
