package squidpony.squidgrid.layout;

import squidpony.squidgrid.generation.TiledShape;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import squidpony.annotation.Beta;
import squidpony.squidmath.RNG;
import squidpony.squidutility.SCollections;

/**
 * Creates a herringbone layout from rectangular tiles.
 *
 * @author Tommy Ettinger
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class HerringboneDungeon {

    private static final RNG rng = new RNG();

    public static void main(String... args) throws IOException {
        ArrayList<TiledShape> verts = new ArrayList<>();
        ArrayList<TiledShape> horzs = new ArrayList<>();

        verts.add(new TiledShape(ImageIO.read(new File("./assets/tiles/herringbone small vertical test.png"))));
        horzs.add(new TiledShape(ImageIO.read(new File("./assets/tiles/herringbone small horizontal test.png"))));

        buildHerringboneShape(400, 100, verts, horzs);
    }

    /**
     * Returns a TiledShape that is constructed out of randomly chosen pieces placed in a herringbone manner.
     * 
     * The provided sets of pieces must have pieces of the same dimension, with the height of the vertical
     * tiles matching the width of the horizontal tiles and the width of the vertical tiles matching the height of the
     * horizontal tiles. The height of the vertical tiles must be as large or larger than the width of the vertical tiles.
     * 
     * There do not need to be the same number of tiles in each set, but there must be at least one tile in each.
     * 
     * @param width the desired width of the returned TiledShape
     * @param height the desired height of the returned TiledShape
     * @param verticalTiles
     * @param horizontalTiles
     * @return 
     */
    public static TiledShape buildHerringboneShape(int width, int height, ArrayList<TiledShape> verticalTiles, ArrayList<TiledShape> horizontalTiles) {
        TiledShape result = new TiledShape(width, height);

        int tileWidth = verticalTiles.get(0).getWidth();//TODO - replace this with robust validator
        int tileHeight = verticalTiles.get(0).getHeight();

        int startX = 0;
        int startY = 0;
        while (startX < width) {
            int x = startX;
            int y = -startY;
            while (y < height) {
                TiledShape tile = SCollections.getRandomElement(verticalTiles);
                result.merge(tile, x, y);
                x += tileWidth;

                tile = SCollections.getRandomElement(horizontalTiles);
                result.merge(tile, x, y);
                y += tileWidth;
            }
            startX += tileHeight + tileWidth;
            startY += (tileHeight - tileWidth);
            startY %= 2 * tileHeight;
        }

        startX = tileHeight;
        startY = tileHeight;
        while (startY < height) {
            int x = -startX;
            int y = startY;
            while (y < height) {
                TiledShape tile = SCollections.getRandomElement(verticalTiles);
                result.merge(tile, x, y);
                x += tileWidth;

                tile = SCollections.getRandomElement(horizontalTiles);
                result.merge(tile, x, y);
                y += tileWidth;
            }
            startY += tileHeight + tileWidth;
            startX += (tileHeight - tileWidth);
            startX %= 2 * tileHeight;
        }

        System.out.println(result);

        return result;
    }
}
