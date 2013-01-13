package squidpony.squidgrid.fov;

/**
 * Basic radius strategy implementations.
 *
 * @author Eben Howard - http://squidpony.com - eben@squidpony.com
 */
public enum BasicRadiusStrategy implements RadiusStrategy {

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
    CIRCLE;

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
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        float radius = 0f;
        switch (this) {
            case SQUARE:
                radius = Math.max(dx, dy);//radius is longest axial distance
                break;
            case DIAMOND:
                radius = dx + dy;//radius is the manhattan distance
                break;
            case CIRCLE:
                radius = (float) Math.sqrt(dx * dx + dy * dy);//standard circular radius
        }
        return radius;
    }
}
