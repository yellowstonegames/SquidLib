package squidpony.squidgrid.mapping.shape;

import java.util.ArrayList;
import squidpony.annotation.Beta;
import squidpony.squidmath.RNG;

/**
 * Creates various shapes from rectangular tiles.
 *
 * @author Tommy Ettinger
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class ShapeGenerator {
    private static final RNG rng = new RNG();

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

        int tileWidth = verticalTiles.get(0).width();//TODO - replace this with robust validator
        int tileHeight = verticalTiles.get(0).height();

        int startX = 0;
        int startY = 0;
        while (startX < width) {
            int x = startX;
            int y = -startY;
            while (y < height) {
                TiledShape tile = rng.getRandomElement(verticalTiles);
                result.overwrite(tile, x, y);
                x += tileWidth;

                tile = rng.getRandomElement(horizontalTiles);
                result.overwrite(tile, x, y);
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
                TiledShape tile = rng.getRandomElement(verticalTiles);
                result.overwrite(tile, x, y);
                x += tileWidth;

                tile = rng.getRandomElement(horizontalTiles);
                result.overwrite(tile, x, y);
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

        int tileWidth = tiles.get(0).width();//TODO - replace this with robust validator
        int tileHeight = tiles.get(0).height();

        for (int x = 0; x < width; x += tileWidth) {
            for (int y = 0; y < height; y += tileHeight) {
                TiledShape tile = rng.getRandomElement(tiles);
                result.overwrite(tile, x, y);
            }
        }

        return result;
    }

    /**
     * Returns a TiledShape that is constructed out of randomly chosen pieces placed in a series of
     * rows with the provided offset for each subsequent row. End result looks like brickwork when
     * the offset is applied per row.
     *
     * The provided list of tiles must all have exactly the same dimensions.
     *
     * @param width the desired width of the returned TiledShape
     * @param height the desired height of the returned TiledShape
     * @param tiles the pool of tiles to randomly choose from
     * @param offset the distance to offset following row
     * @return
     */
    public static TiledShape buildBrick(int width, int height, ArrayList<TiledShape> tiles, int offset) {
        TiledShape result = new TiledShape(width, height);

        int tileWidth = tiles.get(0).width();//TODO - replace this with robust validator
        int tileHeight = tiles.get(0).height();

        int currentOffset = tileHeight;
        for (int x = 0; x < width; x += tileWidth) {
            for (int y = -tileHeight + currentOffset; y < height; y += tileHeight) {
                TiledShape tile = rng.getRandomElement(tiles);
                result.overwrite(tile, x, y);
            }
            currentOffset += offset;
            currentOffset %= tileHeight;
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

        int tileWidth = verticalTiles.get(0).width();//TODO - replace this with robust validator
        int tileHeight = verticalTiles.get(0).height();

        //do horizontal first so right-hand edge can overwrite extra bits
        for (int x = tileWidth; x < width; x += tileHeight) {
            TiledShape tile = rng.getRandomElement(horizontalTiles);
            result.overwrite(tile, x, 0);
            tile = rng.getRandomElement(horizontalTiles);
            result.overwrite(tile, x, height - tileWidth);
        }

        for (int y = 0; y < height; y += tileHeight) {
            TiledShape tile = rng.getRandomElement(verticalTiles);
            result.overwrite(tile, 0, y);
            tile = rng.getRandomElement(verticalTiles);
            result.overwrite(tile, width - tileWidth, y);
        }

        if (width > tileWidth || height > tileHeight) {
            TiledShape tile = buildRunningBond(width - 2 * tileWidth, height - 2 * tileWidth, verticalTiles, horizontalTiles);
            if (tile != null) {
                result.overwrite(tile, tileWidth, tileWidth);
            }
        }

        return result;
    }

    /**
     * Returns a TiledShape that is constructed out of randomly chosen pieces placed in a basket
     * weave pattern.
     *
     * A "regular" basket weave creates square subtiles, alternating between two horizontal tiles
     * together and two vertical tiles together. A non-regular basket weave creates a rectangular
     * subtile structure where two vertical tiles are placed together and then one horizontal tile
     * is placed above or below, alternatingly.
     *
     * The provided tiles must have the same dimensions, with the vertical tiles having the same
     * height as the width of the horizontal tiles and the horizontal tiles having the same height
     * as the width of the vertical tiles.
     *
     * There do not need to be the same number of tiles in the two lists, but each list must contain
     * at least one tile.
     *
     * @param width the desired width of the returned TiledShape
     * @param height the desired height of the returned TiledShape
     * @param verticalTiles the pool of tiles to randomly choose vertical tiles from
     * @param horizontalTiles the pool of tiles to randomly choose horizontal tiles from
     * @param regular true if a regular weave desired, false for a non-regular weave
     * @return
     */
    public static TiledShape buildBasketWeave(int width, int height, ArrayList<TiledShape> verticalTiles, ArrayList<TiledShape> horizontalTiles, boolean regular) {
        TiledShape result = new TiledShape(width, height);

        int tileWidth = verticalTiles.get(0).width();//TODO - replace this with robust validator
        int tileHeight = verticalTiles.get(0).height();
        TiledShape tile;

        boolean alternate = false;
        if (regular) {
            for (int x = 0; x < width; x += tileHeight) {
                for (int y = 0; y < height; y += tileHeight) {
                    if (alternate) {
                        tile = rng.getRandomElement(horizontalTiles);
                        result.overwrite(tile, x, y);
                        tile = rng.getRandomElement(horizontalTiles);
                        result.overwrite(tile, x, y + tileWidth);
                    } else {
                        tile = rng.getRandomElement(verticalTiles);
                        result.overwrite(tile, x, y);
                        tile = rng.getRandomElement(verticalTiles);
                        result.overwrite(tile, x + tileWidth, y);
                    }
                    alternate = !alternate;
                }
            }
        } else {
            for (int x = 0; x < width; x += tileHeight) {
                for (int y = 0; y < height; y += tileHeight + tileWidth) {
                    if (alternate) {
                        tile = rng.getRandomElement(horizontalTiles);
                        result.overwrite(tile, x, y + tileHeight);
                        tile = rng.getRandomElement(verticalTiles);
                        result.overwrite(tile, x, y);
                        tile = rng.getRandomElement(verticalTiles);
                        result.overwrite(tile, x + tileWidth, y);
                    } else {
                        tile = rng.getRandomElement(horizontalTiles);
                        result.overwrite(tile, x, y);
                        tile = rng.getRandomElement(verticalTiles);
                        result.overwrite(tile, x, y + tileWidth);
                        tile = rng.getRandomElement(verticalTiles);
                        result.overwrite(tile, x + tileWidth, y + tileWidth);
                    }
                }
                alternate = !alternate;
            }
        }

        return result;
    }

    /**
     * Returns a TiledShape that is constructed out of randomly chosen pieces placed in a windmill
     * pattern with vertical and horizontal pieces surround a smaller interior piece.
     *
     * The vertical and horizontal tiles do not need to have dimensions related to each other, but
     * all tiles in each individual list must be the same size. The interior tiles must all have a
     * width equal to the width of the horizontal tiles minus the width of the vertical tiles and
     * the height equal to the height of the vertical tiles minus the height of the horizontal
     * tiles.
     *
     * There do not need to be the same number of tiles in the lists, but each list must contain at
     * least one tile.
     *
     * @param width the desired width of the returned TiledShape
     * @param height the desired height of the returned TiledShape
     * @param verticalTiles the pool of tiles to randomly choose vertical tiles from
     * @param horizontalTiles the pool of tiles to randomly choose horizontal tiles from
     * @param interiorTiles the pool of tiles to randomly choose interior tiles from
     * @return
     */
    public static TiledShape buildWindmill(int width, int height, ArrayList<TiledShape> verticalTiles, ArrayList<TiledShape> horizontalTiles, ArrayList<TiledShape> interiorTiles) {
        TiledShape result = new TiledShape(width, height);

        int vertWidth = verticalTiles.get(0).width();//TODO - replace this with robust validator
        int vertHeight = verticalTiles.get(0).height();
        int horzWidth = horizontalTiles.get(0).width();
        int horzHeight = horizontalTiles.get(0).height();
        TiledShape tile;

        for (int x = 0; x < width; x += vertWidth + horzWidth) {
            for (int y = 0; y < height; y += vertHeight + horzHeight) {
                tile = rng.getRandomElement(interiorTiles);
                result.overwrite(tile, x + vertWidth, y + horzHeight);
                tile = rng.getRandomElement(verticalTiles);
                result.overwrite(tile, x, y);
                tile = rng.getRandomElement(horizontalTiles);
                result.overwrite(tile, x + vertWidth, y);
                tile = rng.getRandomElement(horizontalTiles);
                result.overwrite(tile, x, y + vertHeight);
                tile = rng.getRandomElement(verticalTiles);
                result.overwrite(tile, x + horzWidth, y + horzHeight);
            }
        }

        return result;
    }

    private ShapeGenerator() {
    }
}
