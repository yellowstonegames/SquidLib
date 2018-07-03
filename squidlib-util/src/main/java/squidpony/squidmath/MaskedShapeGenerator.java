package squidpony.squidmath;

/**
 * Generates procedural shapes based on a mask that determines what values can exist at a location.
 * Currently, this generates spaceship-like shapes, which <a href="https://i.imgur.com/O4q1a2I.png">look like this</a>.
 * The technique used here is derived from <a href="https://github.com/zfedoran/pixel-sprite-generator">this repo</a>,
 * which is an adaptation of
 * <a href="http://web.archive.org/web/20080228054410/http://www.davebollinger.com/works/pixelspaceships/"> Dave
 * Bollinger's work</a>.
 * Created by Tommy Ettinger on 10/12/2017.
 */
public class MaskedShapeGenerator {
    public static final int[][] spaceship = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 1, 2, 2, 2, 1, 1, 0},
            {0, 1, 3, 3, 3, 3, 2, 2, 2, 3, 1, 0}
    };
    public static final GreasedRegion potentialBody = new GreasedRegion(spaceship, 1), // can be body or empty
            potentialSolid = new GreasedRegion(spaceship, 2, 4), // can be solid or body, never empty
            alwaysSolid = new GreasedRegion(spaceship, 3); // must be solid
    public LinnormRNG randomness = new LinnormRNG(123456789L);
    public RNG rng = new RNG(randomness);
    public final GreasedRegion randomRegion = new GreasedRegion(randomness, 6, 12);
    private final GreasedRegion workingBody = new GreasedRegion(6, 12),
            workingSolid = new GreasedRegion(6, 12),
            workingShade = new GreasedRegion(6, 12),
            workingShine = new GreasedRegion(6, 12);

    public MaskedShapeGenerator()
    {
    }

    /**
     * Returns a modified version of changing where 0 represents empty space, 1 represents border, and 2 represents
     * "body." Only a 12x12 area will be changed by this call, with its minimum x and y determined by xPos and yPos.
     * The seed will change each time this runs, producing different shapes each time.
     * The technique used here is derived from https://github.com/zfedoran/pixel-sprite-generator .
     * @param changing an int array that will be altered if possible
     * @param xPos the minimum x to modify; the maximum will be xPos + 12, exclusive
     * @param yPos the minimum y to modify; the maximum will be yPos + 12, exclusive
     * @return changing, after modifications
     */
    public int[][] generateInto(int[][] changing, int xPos, int yPos)
    {
        int w = workingBody.width, h = workingBody.height, ys = (h + 63) >> 6;
        if(changing.length < w * 2 || changing[0].length < h
                || xPos + w * 2 >= changing.length || yPos + h >= changing[0].length)
            return changing;
        randomRegion.refill(rng, 0.75, w, h);
        workingSolid.remake(potentialSolid);
        workingBody.remake(potentialBody).or(potentialSolid).andNot(alwaysSolid).and(randomRegion);
        workingSolid.andNot(workingBody).or(randomRegion.remake(workingBody).fringe());
        for (int x = 0, o = w*2-1; x < w; x++, o--) {
            for (int y = 0; y < h; y++) {
                changing[xPos + x][yPos + y] = ((workingBody.data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 2 : 0)
                        | ((workingSolid.data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1 : 0);
            }
            System.arraycopy(changing[xPos + x], yPos, changing[xPos + o], yPos, h);
        }
        return changing;
    }

    /**
     * Returns an int array (12x12) where 0 represents empty space, 1 represents border, and 2 represents "body."
     * The seed will change each time this runs, producing different shapes each time.
     * The technique used here is derived from https://github.com/zfedoran/pixel-sprite-generator .
     * @return an int array with the randomly generated shape.
     */
    public int[][] generate()
    {
        return generateInto(new int[workingSolid.width][workingSolid.height], 0, 0);
    }
    /**
     * Returns an int array (12x12) where 0 represents empty space, 1 represents border, and 2 represents "body."
     * Will use the specified seed for this generation.
     * The technique used here is derived from https://github.com/zfedoran/pixel-sprite-generator .
     * @param seed a long to use as the seed for this random shape.
     * @return an int array with the randomly generated shape.
     */
    public int[][] generate(long seed)
    {
        randomness.setState(seed);
        return generateInto(new int[workingSolid.width][workingSolid.height], 0, 0);
    }

    /**
     * Returns a modified version of changing where 0 represents empty space, 1 represents border, 2 represents shaded
     * "body,", 3 represents normal body, and 4 represents lit body. Only a 12x12 area will be changed by this call,
     * with its minimum x and y determined by xPos and yPos.
     * The seed will change each time this runs, producing different shapes each time.
     * The technique used here is derived from https://github.com/zfedoran/pixel-sprite-generator .
     * @param changing an int array that will be altered if possible
     * @param xPos the minimum x to modify; the maximum will be xPos + 12, exclusive
     * @param yPos the minimum y to modify; the maximum will be yPos + 12, exclusive
     * @return changing, after modifications
     */
    public int[][] generateIntoShaded(int[][] changing, int xPos, int yPos)
    {
        int w = workingBody.width, h = workingBody.height, ys = (h + 63) >> 6;
        if(changing.length < w * 2 || changing[0].length < h
                || xPos + w * 2 >= changing.length || yPos + h >= changing[0].length)
            return changing;
        randomRegion.refill(rng, 0.75, w, h);
        workingSolid.remake(potentialSolid);
        workingBody.remake(potentialBody).or(potentialSolid).andNot(alwaysSolid).and(randomRegion);
        workingSolid.andNot(workingBody).or(randomRegion.remake(workingBody).fringe());
        workingShade.remake(workingBody).neighborDown().not().and(workingBody);
        workingShine.remake(workingBody).neighborUp().not().and(workingBody).andNot(workingShade);
        workingBody.andNot(workingShade).andNot(workingShine);
        for (int x = 0, o = w*2-1; x < w; x++, o--) {
            for (int y = 0; y < h; y++) {
                changing[xPos + x][yPos + y] = ((workingShine.data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 4 : 0)
                        | ((workingBody.data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 3 : 0)
                        | ((workingShade.data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 2 : 0)
                        | ((workingSolid.data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1 : 0);
            }
            System.arraycopy(changing[xPos + x], yPos, changing[xPos + o], yPos, h);
        }
        return changing;
    }

    /**
     * Returns a modified version of changing where 0 represents empty space, 1 represents border, 2 represents shaded
     * "body,", 3 represents normal body, and 4 represents lit body. Only a 12x12 area will be changed by this call,
     * with its minimum x and y determined by xPos and yPos. Ensures that borders drawn around the shape cover all cells
     * that are 8-way adjacent to any cells in the shape.
     * The seed will change each time this runs, producing different shapes each time.
     * The technique used here is derived from https://github.com/zfedoran/pixel-sprite-generator .
     * @param changing an int array that will be altered if possible
     * @param xPos the minimum x to modify; the maximum will be xPos + 12, exclusive
     * @param yPos the minimum y to modify; the maximum will be yPos + 12, exclusive
     * @return changing, after modifications
     */
    public int[][] generateIntoShaded8way(int[][] changing, int xPos, int yPos)
    {
        int w = workingBody.width, h = workingBody.height, ys = (h + 63) >> 6;
        if(changing.length < w * 2 || changing[0].length < h
                || xPos + w * 2 >= changing.length || yPos + h >= changing[0].length)
            return changing;
        randomRegion.refill(rng, 0.75, w, h);
        workingSolid.remake(potentialSolid);
        workingBody.remake(potentialBody).or(potentialSolid).andNot(alwaysSolid).and(randomRegion);
        workingSolid.andNot(workingBody).or(randomRegion.remake(workingBody).fringe8way());
        workingShade.remake(workingBody).neighborDown().not().and(workingBody);
        workingShine.remake(workingBody).neighborUp().not().and(workingBody).andNot(workingShade);
        workingBody.andNot(workingShade).andNot(workingShine);
        for (int x = 0, o = w*2-1; x < w; x++, o--) {
            for (int y = 0; y < h; y++) {
                changing[xPos + x][yPos + y] = ((workingShine.data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 4 : 0)
                        | ((workingBody.data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 3 : 0)
                        | ((workingShade.data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 2 : 0)
                        | ((workingSolid.data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1 : 0);
            }
            System.arraycopy(changing[xPos + x], yPos, changing[xPos + o], yPos, h);
        }
        return changing;
    }

}
