package squidpony.squidgrid.shape;

import java.util.ArrayList;
import squidpony.annotation.Beta;
import squidpony.squidutility.SCollections;

/**
 * Creates various shapes from rectangular tiles.
 *
 * @author Tommy Ettinger
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class ShapeGenerator {

    /**
     * Returns a TiledShape that is constructed out of randomly chosen pieces placed in a
     * herringbone manner.
     *
     * The provided sets of pieces must have pieces of the same dimension, with the height of the
     * vertical tiles matching the width of the horizontal tiles and the width of the vertical tiles
     * matching the height of the horizontal tiles. The height of the vertical tiles must be as
     * large or larger than the width of the vertical tiles.
     *
     * There do not need to be the same number of tiles in each set, but there must be at least one
     * tile in each.
     *
     * @param width the desired width of the returned TiledShape
     * @param height the desired height of the returned TiledShape
     * @param verticalTiles the pool of vertical tiles to randomly chose from
     * @param horizontalTiles the pool of horizontal tiles to randomly chose from
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

        return result;
    }

    /**
     * Returns a TiledShape that is constructed out of randomly chosen pieces placed in a regular
     * column and row manner.
     *
     * The provided list of tiles must all have exactly the same dimensions.
     *
     * @param width the desired width of the returned TiledShape
     * @param height the desired height of the returned TiledShape
     * @param tiles the pool of tiles to randomly choose from
     * @return
     */
    public static TiledShape buildStackBond(int width, int height, ArrayList<TiledShape> tiles) {
        TiledShape result = new TiledShape(width, height);

        int tileWidth = tiles.get(0).getWidth();//TODO - replace this with robust validator
        int tileHeight = tiles.get(0).getHeight();

        for (int x = 0; x < width; x += tileWidth) {
            for (int y = 0; y < height; y += tileHeight) {
                TiledShape tile = SCollections.getRandomElement(tiles);
                result.merge(tile, x, y);
            }
        }

        return result;
    }

    /**
     * Returns a TiledShape that is constructed out of randomly chosen pieces placed in a series of
     * rows or columns with the provided offset for each subsequent row or column. End result looks
     * like brickwork when the offset is applied per row.
     *
     * The provided list of tiles must all have exactly the same dimensions.
     *
     * @param width the desired width of the returned TiledShape
     * @param height the desired height of the returned TiledShape
     * @param tiles the pool of tiles to randomly choose from
     * @param offsetHorizontally true if each row should be offset from the previous (as in
     * brickwork) or false if the offset should be applied inside the columns
     * @param offset the distance to offset following row or column elements
     * @return
     */
    public static TiledShape buildBrick(int width, int height, ArrayList<TiledShape> tiles, boolean offsetHorizontally, int offset) {
        TiledShape result = new TiledShape(width, height);

        int tileWidth = tiles.get(0).getWidth();//TODO - replace this with robust validator
        int tileHeight = tiles.get(0).getHeight();

        int currentOffset;
        if (offsetHorizontally) {
            currentOffset = tileWidth;
            for (int y = 0; y < height; y += tileHeight) {
                for (int x = -tileWidth + currentOffset; x < width; x += tileWidth) {
                    TiledShape tile = SCollections.getRandomElement(tiles);
                    result.merge(tile, x, y);
                }
                currentOffset += offset;
                currentOffset %= tileWidth;
            }
        } else {
            currentOffset = tileHeight;
            for (int x = 0; x < width; x += tileWidth) {
                for (int y = -tileHeight + currentOffset; y < height; y += tileHeight) {
                    TiledShape tile = SCollections.getRandomElement(tiles);
                    result.merge(tile, x, y);
                }
                currentOffset += offset;
                currentOffset %= tileHeight;
            }
        }

        return result;
    }

    /**
     * Returns a TiledShape that is constructed out of randomly chosen pieces placed in a concentric
     * pattern known as running bond.
     *
     * The provided tiles must have the same dimensions, with the vertical tiles having the same
     * height as the width of the horizontal tiles and the horizontal tiles having the same height
     * as the width of the vertical tiles.
     *
     * There do not need to be the same number of tiles in the two lists, but each list must contain
     * at least one tile.
     *
     * Returns null if width and height are not both greater than zero.
     *
     * @param width the desired width of the returned TiledShape
     * @param height the desired height of the returned TiledShape
     * @param verticalTiles the pool of tiles to randomly choose vertical tiles from
     * @param horizontalTiles the pool of tiles to randomly choose horizontal tiles from
     * @return
     */
    public static TiledShape buildRunningBond(int width, int height, ArrayList<TiledShape> verticalTiles, ArrayList<TiledShape> horizontalTiles) {
        if (width <= 0 || height <= 0) {
            return null;
        }
        TiledShape result = new TiledShape(width, height);

        int tileWidth = verticalTiles.get(0).getWidth();//TODO - replace this with robust validator
        int tileHeight = verticalTiles.get(0).getHeight();

        int endY = height - tileWidth;
        int ySpacers = (int) (endY / tileHeight);//get number of spacer tiles
        endY = tileWidth + ySpacers * tileHeight;

        int endX = width - tileWidth;
        int xSpacers = (int) (endX / tileHeight);//get number of spacer tiles
        endX = tileWidth + xSpacers * tileHeight;

        if (width > tileWidth || height > tileHeight) {
            TiledShape tile = buildRunningBond(xSpacers * tileHeight, ySpacers * tileHeight, verticalTiles, horizontalTiles);
            if (tile != null) {
                result.merge(tile, tileWidth, tileWidth);
            }
        }

        //do horizontal first so right-hand edge can overwrite extra bits
        for (int x = tileWidth; x < width; x += tileHeight) {
            TiledShape tile = SCollections.getRandomElement(horizontalTiles);
            result.merge(tile, x, 0);
            tile = SCollections.getRandomElement(horizontalTiles);
            result.merge(tile, x, endY);
        }

        for (int y = 0; y < height; y += tileHeight) {
            TiledShape tile = SCollections.getRandomElement(verticalTiles);
            result.merge(tile, 0, y);
//            if (width - endX <= tileWidth) {
            tile = SCollections.getRandomElement(verticalTiles);
            result.merge(tile, endX, y);
//            }
        }

        return result;
    }

    private ShapeGenerator() {
    }
}
