package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.SnapshotArray;
import squidpony.ArrayTools;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.StatefulRNG;

/**
 * Created by Tommy Ettinger on 7/28/2017.
 */
public class SparseLayers extends Actor {
    public final int width, height;
    public float[][] backgrounds;
    public Array<SparseTextMap> layers;
    protected IntIntMap mapping;
    public TextCellFactory font;
    public float xOffset, yOffset;
    protected int animationCount = 0;
    private SnapshotArray<TextCellFactory.Glyph> glyphs;


    public SparseLayers(int width, int height)
    {
        this(width, height, 10, 16, DefaultResources.getStretchableFont());
    }

    public SparseLayers(int width, int height, float cellWidth, float cellHeight)
    {
        this(width, height, cellWidth, cellHeight, DefaultResources.getStretchableFont());
    }

    public SparseLayers(int width, int height, float cellWidth, float cellHeight, TextCellFactory font) {
        this(width, height, cellWidth, cellHeight, font, 0f, 0f);
    }

    public SparseLayers(int width, int height, float cellWidth, float cellHeight, TextCellFactory font, float xOffset, float yOffset) {
        this.width = MathUtils.clamp(width, 1, 65535);
        this.height = MathUtils.clamp(height, 1, 65535);
        backgrounds = ArrayTools.fill(0f, this.width, this.height);
        layers = new Array<>(true, 4, SparseTextMap.class);
        this.font = font.width(cellWidth).height(cellHeight).initBySize();
        layers.add(new SparseTextMap(width * height >> 2));
        mapping = new IntIntMap(4);
        mapping.put(0, 0);
        glyphs = new SnapshotArray<>(true, 16, TextCellFactory.Glyph.class);
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        setBounds(xOffset, yOffset,
                this.font.actualCellWidth * this.width, this.font.actualCellHeight * this.height);
    }

    public void put(int x, int y, char c, Color foreground, Color background)
    {
        put(x, y, c, foreground == null ? 0f : foreground.toFloatBits(),
                background == null ? 0f : background.toFloatBits());
    }

    public void put(int x, int y, char c, float foreground, float background)
    {
        if(x < 0 || x >= width || y < 0 || y >= height)
            return;
        if(background != 0f)
            backgrounds[x][y] = background;
        if(foreground != 0f)
            layers.items[0].place(x, y, c, foreground);
    }

    public void put(int x, int y, char c, Color foreground, Color background, int layer)
    {
        put(x, y, c, foreground == null ? 0f : foreground.toFloatBits(),
                background == null ? 0f : background.toFloatBits(), layer);

    }

    public void put(int x, int y, char c, float foreground, float background, int layer)
    {
        if(x < 0 || x >= width || y < 0 || y >= height || layer < 0)
            return;
        if(background != 0f)
            backgrounds[x][y] = background;
        layer = mapping.get(layer, layer);
        if(foreground != 0f)
        {
            if(layer >= layers.size)
            {
                mapping.put(layer, layers.size);
                SparseTextMap stm = new SparseTextMap(width * height >> 4);
                stm.place(x, y, c, foreground);
                layers.add(stm);
            }
            else
            {
                layers.get(layer).place(x, y, c, foreground);
            }
        }
    }
    public void print(int x, int y, String text, Color foreground, Color background) {
        print(x, y, text, foreground == null ? 0f : foreground.toFloatBits(),
                background == null ? 0f : background.toFloatBits());
    }

    public void print(int x, int y, String text, float foreground, float background)
    {
        int len = Math.min(text.length(), width - x);
        for (int i = 0; i < len; i++) {
            put(x + i, y, text.charAt(i), foreground, background);
        }
    }

    public void print(int x, int y, String text, Color foreground, Color background, int layer) {
        print(x, y, text, foreground == null ? 0f : foreground.toFloatBits(),
                background == null ? 0f : background.toFloatBits(), layer);
    }
    public void print(int x, int y, String text, float foreground, float background, int layer) {
        if (x < 0 || x >= width || y < 0 || y >= height || layer < 0)
            return;
        int len = Math.min(text.length(), width - x);
        if(background != 0f) {
            for (int i = 0; i < len; i++) {
                backgrounds[x + i][y] = background;
            }
        }
        if (foreground != 0f)
        {
            layer = mapping.get(layer, layer);
            if (layer >= layers.size) {
                mapping.put(layer, layers.size);
                SparseTextMap stm = new SparseTextMap(width * height >> 4);
                for (int i = 0; i < len; i++) {
                    stm.place(x + i, y, text.charAt(i), foreground);
                }
                layers.add(stm);
            } else {
                SparseTextMap stm = layers.get(layer);
                for (int i = 0; i < len; i++) {
                    stm.place(x + i, y, text.charAt(i), foreground);
                }
            }
        }
    }

    public void changeBackground(int x, int y, Color color)
    {
        changeBackground(x, y, color == null ? 0f : color.toFloatBits());
    }
    public void changeBackground(int x, int y, float color)
    {
        if(x < 0 || x >= width || y < 0 || y >= height)
            return;
        backgrounds[x][y] = color;
    }
    public void erase(int x, int y)
    {
        if(x < 0 || x >= width || y < 0 || y >= height)
            return;
        backgrounds[x][y] = 0f;
        int code = SparseTextMap.encodePosition(x, y);
        for (int i = 0; i < layers.size; i++) {
            layers.get(i).remove(code);
        }
    }
    public void removeChar(int x, int y, int layer)
    {
        layer = mapping.get(layer, -1);
        if(layer >= 0)
        {
            layers.get(layer).remove(SparseTextMap.encodePosition(x, y));
        }
    }

    public void clear()
    {
        ArrayTools.fill(backgrounds, 0f);
        for (int i = 0; i < layers.size; i++) {
            layers.get(i).clear();
        }
    }

    public boolean hasActiveAnimations()
    {
        return animationCount > 0;
    }

    public float worldX(int gridX)
    {
        return xOffset + gridX * font.actualCellWidth;
    }

    public float worldY(int gridY)
    {
        return yOffset + (height - gridY) * font.actualCellHeight;
    }

    public int gridX(float screenX)
    {
        return Math.round((screenX - xOffset) / font.actualCellWidth);
    }

    public int gridY(float screenY)
    {
        return Math.round((yOffset - screenY) / font.actualCellHeight + height);
    }


    public TextCellFactory.Glyph glyph(char shown, float color, int x, int y)
    {
        TextCellFactory.Glyph g =
                font.glyph(shown, color,
                        worldX(x),
                        worldY(y));
        glyphs.add(g);
        return g;
    }

    public TextCellFactory.Glyph glyphFromGrid(int x, int y)
    {
        int code = SparseTextMap.encodePosition(x, y);
        char shown = layers.items[0].getChar(code, ' ');
        float color = layers.items[0].getFloat(code, 0f);
        layers.items[0].remove(code);
        TextCellFactory.Glyph g =
                font.glyph(shown, color, worldX(x), worldY(y));
        glyphs.add(g);
        return g;
    }

    public TextCellFactory.Glyph glyphFromGrid(int x, int y, int layer) {
        layer = mapping.get(layer, -1);
        if (layer >= 0) {
            SparseTextMap l = layers.get(layer);
            int code = SparseTextMap.encodePosition(x, y);
            char shown = l.getChar(code, ' ');
            float color = l.getFloat(code, 0f);
            l.remove(code);
            TextCellFactory.Glyph g =
                    font.glyph(shown, color, worldX(x), worldY(y));
            glyphs.add(g);
            return g;
        }
        else
        {
            return null;
        }
    }

    public void recallToGrid(TextCellFactory.Glyph glyph)
    {
        layers.items[0].place(gridX(glyph.getY()), gridY(glyph.getY()), glyph.shown, glyph.color);
        glyphs.removeValue(glyph, true);
    }

    public void recallToGrid(TextCellFactory.Glyph glyph, int layer)
    {
        layer = mapping.get(layer, 0);
        layers.get(layer).place(gridX(glyph.getY()), gridY(glyph.getY()), glyph.shown, glyph.color);
        glyphs.removeValue(glyph, true);
    }
    /**
     * Start a bumping animation in the given direction that will last duration seconds.
     * @param glyph
     *              A {@link TextCellFactory.Glyph}, probably produced by
     *              {@link #glyph(char, float, int, int)} or {@link #glyphFromGrid(int, int)}
     * @param direction the direction for the glyph to bump towards
     * @param duration a float, measured in seconds, for how long the animation should last; commonly 0.12f
     */
    public void bump(final TextCellFactory.Glyph glyph, Direction direction, float duration)
    {
        final float x = glyph.getX(),
                y = glyph.getY();
        duration = Math.max(0.015f, duration);
        animationCount++;
        glyph.addAction(Actions.sequence(
                Actions.moveToAligned(x + direction.deltaX * 0.35f * font.actualCellWidth,
                        y - direction.deltaY * 0.35f * font.actualCellHeight,
                        Align.bottomLeft, duration * 0.35F),
                Actions.moveToAligned(x, y, Align.bottomLeft, duration * 0.65F),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        --animationCount;
                    }
                }))));

    }

    /**
     * Slides {@code glyph} from {@code (xstartX,startY)} to {@code (newx, newy)}.
     * Takes a number of seconds equal to duration to complete. This also allows
     * a Runnable to be given as {@code postRunnable} to be run after the
     * slide completes, or null to not run anything after the slide.
     *
     * @param glyph
     *            A {@link TextCellFactory.Glyph}, probably produced by
     *            {@link #glyph(char, float, int, int)} or {@link #glyphFromGrid(int, int)}
     * @param startX
     *            Where to start the slide, horizontally.
     * @param startY
     *            Where to start the slide, vertically.
     * @param newX
     *            Where to end the slide, horizontally.
     * @param newY
     *            Where to end the slide, vertically.
     * @param duration
     *            The animation's duration.
     * @param postRunnable a Runnable to execute after the slide completes; may be null to do nothing.
     */
    public void slide(TextCellFactory.Glyph glyph, final int startX, final int startY, final int newX,
                      final int newY, float duration, /* @Nullable */ Runnable postRunnable) {
        glyph.setPosition(worldX(startX), worldY(startY));
        duration = Math.max(0.015f, duration);
        animationCount++;
        final int nbActions = 2 + (postRunnable == null ? 0 : 1);
        int index = 0;
        final Action[] sequence = new Action[nbActions];
        final float nextX = worldX(newX);
        final float nextY = worldY(newY);
        sequence[index++] = Actions.moveToAligned(nextX, nextY, Align.bottomLeft, duration);
        if(postRunnable != null)
        {
            sequence[index++] = Actions.run(postRunnable);
        }
		/* Do this one last, so that hasActiveAnimations() returns true during 'postRunnables' */
        sequence[index] = Actions.delay(duration, Actions.run(new Runnable() {
            @Override
            public void run() {
                --animationCount;
            }
        }));

        glyph.addAction(Actions.sequence(sequence));
    }

    /**
     * Starts an wiggling animation for the object at the given location for the given duration in seconds.
     *
     * @param glyph
     *            A {@link TextCellFactory.Glyph}, probably produced by
     *            {@link #glyph(char, float, int, int)} or {@link #glyphFromGrid(int, int)}
     * @param duration in seconds, as a float
     */
    public void wiggle(final TextCellFactory.Glyph glyph, float duration) {

        final float x = glyph.getX(), y = glyph.getY(),
                cellWidth = font.actualCellWidth, cellHeight = font.actualCellHeight;
        duration = Math.max(0.015f, duration);
        animationCount++;
        final StatefulRNG gRandom = DefaultResources.getGuiRandom();
        glyph.addAction(Actions.sequence(
                Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(x + (gRandom.nextFloat() - 0.5F) * cellWidth * 0.4f,
                        y + (gRandom.nextFloat() - 0.5F) * cellHeight * 0.4f,
                        Align.bottomLeft, duration * 0.2F),
                Actions.moveToAligned(x, y, Align.bottomLeft, duration * 0.2F),
                Actions.delay(duration, Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        --animationCount;
                    }
                }))));
    }
    /**
     * Tints the background at position x,y so it becomes the given encodedColor, waiting for {@code delay} (in seconds)
     * before performing it, then after the tint is complete it returns the cell to its original color, taking duration
     * seconds. Additionally, enqueue {@code postRunnable} for running after the created action ends.
     * <br>
     * This will only behave correctly if you call {@link Stage#act()} before you call {@link Stage#draw()}, but after
     * any changes to the contents of this SparseLayers. If you change the contents, then draw, and then act, that will
     * draw the contents without the tint this applies, then apply the tint when you call act(), then quickly overwrite
     * the tint in the next frame. That visually appears as nothing happening other than a delay.
     * @param delay how long to wait in seconds before starting the effect
     * @param x the x-coordinate of the cell to tint
     * @param y the y-coordinate of the cell to tint
     * @param encodedColor what to transition the cell's color towards, and then transition back from, as a packed float
     * @param duration how long the total "round-trip" transition should take in seconds
     * @param postRunnable a Runnable to execute after the tint completes; may be null to do nothing.
     */
    public void tint(float delay, final int x, final int y, final float encodedColor, float duration, Runnable postRunnable) {
        if(x < 0 || x >= width || y < 0 || y >= height)
            return;
        duration = Math.max(0.015f, duration);
        animationCount++;
        final float ac = backgrounds[x][y];
        final int nbActions = 3 + (0 < delay ? 1 : 0) + (postRunnable == null ? 0 : 1);
        final Action[] sequence = new Action[nbActions];
        int index = 0;
        if (0 < delay)
            sequence[index++] = Actions.delay(delay);
        sequence[index++] = new TemporalAction(duration * 0.3f) {
            @Override
            protected void update(float percent) {
                backgrounds[x][y] = SColor.lerpFloatColors(ac, encodedColor, percent);
            }
        };
        sequence[index++] = new TemporalAction(duration * 0.7f) {
            @Override
            protected void update(float percent) {
                backgrounds[x][y] = SColor.lerpFloatColors(encodedColor, ac, percent);
            }
        };
        if(postRunnable != null)
        {
            sequence[index++] = Actions.run(postRunnable);
        }
		/* Do this one last, so that hasActiveAnimations() returns true during 'postRunnables' */
        sequence[index] = Actions.delay(duration, Actions.run(new Runnable() {
            @Override
            public void run() {
                --animationCount;
            }
        }));

        addAction(Actions.sequence(sequence));
    }

    /**
     * Create a new Glyph at (startX, startY) using the char shown with the given startColor, and immediately starts
     * changing color to endColor, changing position so it ends on the cell (endX, endY), taking duration seconds to
     * complete before removing the Glyph.
     * <br>
     * Unlike {@link SquidPanel#summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)},
     * this does not rotate the Glyph it produces.
     * @param startX the starting x position in cells
     * @param startY the starting y position in cells
     * @param endX the ending x position in cells
     * @param endY the ending y position in cells
     * @param shown the char to show (the same char throughout the effect)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param duration the duration in seconds for the effect
     */
    public void summon(int startX, int startY, int endX, int endY, char shown,
                       final float startColor, final float endColor, float duration)
    {
        duration = Math.max(0.015f, duration);
        animationCount++;
        final TextCellFactory.Glyph glyph = glyph(shown, startColor, startX, startY);
        glyph.addAction(Actions.sequence(
                Actions.parallel(
                        new TemporalAction(duration) {
                            @Override
                            protected void update(float percent) {
                                glyph.color = SColor.lerpFloatColors(startColor, endColor, percent * 0.95f);
                            }
                        },
                        Actions.moveTo(worldX(endX), worldY(endY), duration)),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        --animationCount;
                        glyphs.removeValue(glyph, true);
                    }
                })));
    }

    /**
     * Create a new Glyph at (startX, startY) in world coordinates (often pixels on the screen) using the char shown
     * with the given startColor, and immediately starts changing color to endColor, changing position so it ends at the
     * world coordinates (endX, endY), taking duration seconds to complete before removing the Glyph.
     * <br>
     * Unlike {@link SquidPanel#summon(float, int, int, int, int, char, Color, Color, boolean, float, float, float)},
     * this does not rotate the Glyph it produces.
     * @param startX the starting x position in world coordinates
     * @param startY the starting y position in world coordinates
     * @param endX the ending x position in world coordinates
     * @param endY the ending y position in world coordinates
     * @param shown the char to show (the same char throughout the effect)
     * @param startColor the starting Color
     * @param endColor the Color to transition to
     * @param duration the duration in seconds for the effect
     */
    public void summon(float startX, float startY, float endX, float endY, char shown,
                       final float startColor, final float endColor, float duration)
    {
        duration = Math.max(0.015f, duration);
        animationCount++;
        final TextCellFactory.Glyph glyph = glyph(shown, startColor, gridX(startX), gridY(startY));
        glyph.addAction(Actions.sequence(
                Actions.parallel(
                        new TemporalAction(duration) {
                            @Override
                            protected void update(float percent) {
                                glyph.color = SColor.lerpFloatColors(startColor, endColor, percent * 0.9f);
                            }
                        },
                        Actions.moveTo(endX, endY, duration)),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        --animationCount;
                        glyphs.removeValue(glyph, true);
                    }
                })));
    }

    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(int, int, int, int, char, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Glyphs that change color and position.
     * The distance is how many cells away to move the created Actors away from (x,y). The measurement determines
     * whether this produces Glyphs in 4 (cardinal) directions for DIAMOND or OCTAHEDRON, or 8 (cardinal and diagonal)
     * directions for any other enum value for Radius; CIRCLE and SPHERE will position the 8 glyphs in a circle, while
     * SQUARE and CUBE will position their 8 glyphs in a square.
     * <br>
     * Unlike {@link SquidPanel#burst(float, int, int, int, boolean, char, Color, Color, boolean, float, float)}, this
     * does not rotate the individual Glyphs it produces.
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param distance how far away, in cells, to move each actor from the center (Chebyshev distance, forming a square)
     * @param measurement a Radius enum that determines if 4 (DIAMOND, OCTAHEDRON) or 8 (anything else) Glyphs are
     *                    created, and the shape they will take
     * @param shown the char to use for Glyphs; should definitely be visible
     * @param startColor the encoded color to start the effect with
     * @param endColor the encoded color to end the effect on
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, int distance, Radius measurement, char shown,
                      float startColor, float endColor, float duration)
    {
        Direction d;
        switch (measurement)
        {
            case SQUARE:
            case CUBE:
                for (int i = 0; i < 8; i++) {
                    d = Direction.CLOCKWISE[i];
                    summon(x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                            shown, startColor, endColor, duration);
                }
                break;
            case CIRCLE:
            case SPHERE:
                float xf = worldX(x), yf = worldY(y);
                for (int i = 0; i < 4; i++) {
                    d = Direction.DIAGONALS[i];
                    summon(xf, yf, xf - d.deltaX * font.actualCellWidth * distance * 0.7071067811865475f, // the constant is 1.0 / Math.sqrt(2.0)
                            yf + d.deltaY * font.actualCellHeight * distance * 0.7071067811865475f,
                            shown, startColor, endColor, duration);
                }
                // break intentionally absent
            default:
                for (int i = 0; i < 4; i++) {
                    d = Direction.CARDINALS_CLOCKWISE[i];
                    summon(x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                            shown, startColor, endColor, duration);
                }
                break;
        }
    }
    /**
     * Convenience method to produce an explosion, splash, or burst effect. Calls
     * {@link #summon(int, int, int, int, char, float, float, float)} repeatedly with
     * different parameters. As with summon(), this creates temporary Glyphs that change color and position.
     * The distance is how many cells away to move the created Actors away from (x,y). The measurement determines
     * whether this produces Glyphs in 4 (cardinal) directions for DIAMOND or OCTAHEDRON, or 8 (cardinal and diagonal)
     * directions for any other enum value for Radius; CIRCLE and SPHERE will position the 8 glyphs in a circle, while
     * SQUARE and CUBE will position their 8 glyphs in a square.
     * <br>
     * Unlike {@link SquidPanel#burst(float, int, int, int, boolean, char, Color, Color, boolean, float, float)}, this
     * does not rotate the individual Glyphs it produces.
     * @param x the starting, center, x-position to create all Actors at
     * @param y the starting, center, y-position to create all Actors at
     * @param distance how far away, in cells, to move each actor from the center (Chebyshev distance, forming a square)
     * @param measurement a Radius enum that determines if 4 (DIAMOND, OCTAHEDRON) or 8 (anything else) Glyphs are
     *                    created, and the shape they will take
     * @param choices a String or other CharSequence containing the chars this can randomly choose
     * @param startColor the encoded color to start the effect with
     * @param endColor the encoded color to end the effect on
     * @param duration how long, in seconds, the effect should last
     */
    public void burst(int x, int y, int distance, Radius measurement, CharSequence choices,
                      float startColor, float endColor, float duration)
    {
        Direction d;
        final int len = choices.length();
        final StatefulRNG rng = DefaultResources.getGuiRandom();
        switch (measurement)
        {
            case SQUARE:
            case CUBE:
                for (int i = 0; i < 8; i++) {
                    d = Direction.CLOCKWISE[i];
                    summon(x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                            choices.charAt(rng.nextIntHasty(len)), startColor, endColor, duration);
                }
                break;
            case CIRCLE:
            case SPHERE:
                float xf = worldX(x), yf = worldY(y);
                for (int i = 0; i < 4; i++) {
                    d = Direction.DIAGONALS[i];
                    summon(xf, yf, xf - d.deltaX * font.actualCellWidth * distance * 0.7071067811865475f,
                            yf + d.deltaY * font.actualCellHeight * distance * 0.7071067811865475f,
                            choices.charAt(rng.nextIntHasty(len)), startColor, endColor, duration);
                }
                // break intentionally absent
            default:
                for (int i = 0; i < 4; i++) {
                    d = Direction.CARDINALS_CLOCKWISE[i];
                    summon(x, y, x - d.deltaX * distance, y + d.deltaY * distance,
                            choices.charAt(rng.nextIntHasty(len)), startColor, endColor, duration);
                }
                break;
        }
    }

    /**
     * Draws the actor. The batch is configured to draw in the parent's coordinate system.
     * {@link Batch#draw(TextureRegion, float, float, float, float, float, float, float, float, float)
     * This draw method} is convenient to draw a rotated and scaled TextureRegion. {@link Batch#begin()} has already been called on
     * the batch. If {@link Batch#end()} is called to draw without the batch then {@link Batch#begin()} must be called before the
     * method returns.
     *
     * @param batch a Batch such as a SpriteBatch that must be between a begin() and end() call; usually done by Stage
     * @param parentAlpha currently ignored
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        font.draw(batch, backgrounds, xOffset, yOffset);
        int len = layers.size;
        float yOff2 = yOffset + 1f + height * font.actualCellHeight;
        for (int i = 0; i < len; i++) {
            layers.get(i).draw(batch, font, xOffset, yOff2);
        }
        TextCellFactory.Glyph[] items = glyphs.begin();
        int x, y;
        for (int i = 0, n = glyphs.size; i < n; i++) {
            TextCellFactory.Glyph glyph = items[i];
            if(glyph == null)
                continue;
            glyph.act(Gdx.graphics.getDeltaTime());
            if(
                    (x = Math.round((glyph.getX() - xOffset) / font.actualCellWidth)) < 0 || x >= width ||
                    (y = Math.round((glyph.getY() - yOffset)  / -font.actualCellHeight + height)) < 0 || y >= height ||
                    backgrounds[x][y] == 0f)
                continue;
            glyph.draw(batch, 1f);
        }
        glyphs.end();
    }
}
