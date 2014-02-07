package squidpony.squidgrid.util;

/**
 * Basic radius strategy implementations.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public enum BasicRadiusStrategy implements RadiusStrategy3D {

    /**
     * In an unobstructed area the FOV would be a square.
     *
     * This is the shape that would represent movement radius in an 8-way
     * movement scheme with no additional cost for diagonal movement.
     */
    SQUARE,
    /**
     * In an unobstructed area the FOV would be a diamond.
     *
     * This is the shape that would represent movement radius in a 4-way
     * movement scheme.
     */
    DIAMOND,
    /**
     * In an unobstructed area the FOV would be a circle.
     *
     * This is the shape that would represent movement radius in an 8-way
     * movement scheme with all movement cost the same based on distance from
     * the source
     */
    CIRCLE,
    /**
     * In an unobstructed area the FOV would be a cube.
     *
     * This is the shape that would represent movement radius in an 8-way
     * movement scheme with no additional cost for diagonal movement.
     */
    CUBE,
    /**
     * In an unobstructed area the FOV would be a octahedron.
     *
     * This is the shape that would represent movement radius in a 4-way
     * movement scheme.
     */
    OCTAHEDRON,
    /**
     * In an unobstructed area the FOV would be a sphere.
     *
     * This is the shape that would represent movement radius in an 8-way
     * movement scheme with all movement cost the same based on distance from
     * the source
     */
    SPHERE;

    @Override
    public float radius(int startx, int starty, int startz, int endx, int endy, int endz) {
        return radius((float) startx, (float) starty, (float) startz, (float) endx, (float) endy, (float) endz);
    }

    @Override
    public float radius(float startx, float starty, float startz, float endx, float endy, float endz) {
        float dx = Math.abs(startx - endx);
        float dy = Math.abs(starty - endy);
        float dz = Math.abs(startz - endz);
        return radius(dx, dy, dz);
    }

    @Override
    public float radius(int dx, int dy, int dz) {
        return radius((float) dx, (float) dy, (float) dz);
    }

    @Override
    public float radius(float dx, float dy, float dz) {
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        dz = Math.abs(dz);
        float radius = 0f;
        switch (this) {
            case SQUARE:
            case CUBE:
                radius = Math.max(dx, Math.max(dy, dz));//radius is longest axial distance
                break;
            case DIAMOND:
            case OCTAHEDRON:
                radius = dx + dy + dz;//radius is the manhattan distance
                break;
            case CIRCLE:
            case SPHERE:
                radius = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);//standard spherical radius
        }
        return radius;
    }

    @Override
    public float radius(int startx, int starty, int endx, int endy) {
        return radius((float) startx, (float) starty, (float) endx, (float) endy);
    }

    @Override
    public float radius(float startx, float starty, float endx, float endy) {
        float dx = Math.abs(startx - endx);
        float dy = Math.abs(starty - endy);
        return radius(dx, dy);
    }

    @Override
    public float radius(int dx, int dy) {
        return radius((float) dx, (float) dy);
    }

    @Override
    public float radius(float dx, float dy) {
        return radius(dx, dy, 0f);
    }
}
