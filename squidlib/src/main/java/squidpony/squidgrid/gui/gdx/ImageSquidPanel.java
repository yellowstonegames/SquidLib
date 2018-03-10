package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.IntMap;
import squidpony.IColorCenter;
import squidpony.StringKit;
import squidpony.panel.ISquidPanel;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Displays text and images in a grid pattern, like SquidPanel, but will automatically render certain chars as images.
 * Supports basic animations, such as sliding, wiggling, or fading the contents of a cell.
 * <br>
 * Grid width and height settings are in terms of number of cells. Cell width and height are in terms of number of
 * pixels (when there is no stretching taking place due to viewport or window size).
 * <br>
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger
 */
public class ImageSquidPanel extends SquidPanel {

    public IntMap<TextureRegion> imageMap;

    /**
     * Creates a bare-bones panel with all default values for text rendering.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     */
    public ImageSquidPanel(int gridWidth, int gridHeight) {
        this(gridWidth, gridHeight, new TextCellFactory().defaultSquareFont());
    }

    /**
     * Creates a panel with the given grid and cell size. Uses a default square font.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param cellWidth the number of horizontal pixels in each cell
     * @param cellHeight the number of vertical pixels in each cell
     */
    public ImageSquidPanel(int gridWidth, int gridHeight, int cellWidth, int cellHeight) {
        this(gridWidth, gridHeight, new TextCellFactory().defaultSquareFont().width(cellWidth).height(cellHeight).initBySize());
    }

    /**
     * Builds a panel with the given grid size and all other parameters determined by the factory. Even if sprite images
     * are being used, a TextCellFactory is still needed to perform sizing and other utility functions.
     *
     * If the TextCellFactory has not yet been initialized, then it will be sized at 12x12 px per cell. If it is null
     * then a default one will be created and initialized.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param factory the factory to use for cell rendering
     */
    public ImageSquidPanel(int gridWidth, int gridHeight, TextCellFactory factory) {
        this(gridWidth, gridHeight, factory, DefaultResources.getSCC());
    }

    /**
     * Builds a panel with the given grid size and all other parameters determined by the factory. Even if sprite images
     * are being used, a TextCellFactory is still needed to perform sizing and other utility functions.
     *
     * If the TextCellFactory has not yet been initialized, then it will be sized at 12x12 px per cell. If it is null
     * then a default one will be created and initialized.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param factory the factory to use for cell rendering
     * @param center
     * 			The color center to use. Can be {@code null}, but then must be set later on with
     *          {@link #setColorCenter(IColorCenter)}.
     */
    public ImageSquidPanel(int gridWidth, int gridHeight, TextCellFactory factory, IColorCenter<Color> center) {
        this(gridWidth, gridHeight, factory, center, 0f, 0f);
    }

    /**
     * Builds a panel with the given grid size and all other parameters determined by the factory. Even if sprite images
     * are being used, a TextCellFactory is still needed to perform sizing and other utility functions.
     *
     * If the TextCellFactory has not yet been initialized, then it will be sized at 12x12 px per cell. If it is null
     * then a default one will be created and initialized.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param factory the factory to use for cell rendering
     * @param center
     * 			The color center to use. Can be {@code null}, but then must be set later on with
     *          {@link #setColorCenter(IColorCenter)}.
     */
    public ImageSquidPanel(int gridWidth, int gridHeight, TextCellFactory factory, IColorCenter<Color> center,
                           float xOffset, float yOffset) {
        this(gridWidth, gridHeight, factory, center, xOffset, yOffset, null);
    }
    /**
     * Builds a panel with the given grid size and all other parameters determined by the factory. Even if sprite images
     * are being used, a TextCellFactory is still needed to perform sizing and other utility functions. Importantly,
     * this constructor takes a 2D char array argument that can be sized differently than the displayed area. The
     * displayed area is gridWidth by gridHeight in cells, but the actualMap argument can be much larger, and only a
     * portion will be displayed at a time. This requires some special work with the Camera and Viewports to get working
     * correctly; in the squidlib module's examples, EverythingDemo may be a good place to see how this can be done.
     * You can pass null for actualMap, which will simply create a char array to use internally that is exactly
     * gridWidth by gridHeight, in cells.
     * <br>
     * If the TextCellFactory has not yet been initialized, then it will be sized at 12x12 px per cell. If it is null
     * then a default one will be created and initialized. The xOffset and yOffset arguments are measured in pixels or
     * whatever sub-cell unit of measure your game uses (world coordinates, in libGDX parlance), and change where the
     * SquidPanel starts drawing by simply adding to the initial x and y coordinates. 0 and 0 are usually fine.
     *
     * @param gridWidth the number of cells horizontally
     * @param gridHeight the number of cells vertically
     * @param factory the factory to use for cell rendering
     * @param center
     * 			The color center to use. Can be {@code null}, but then must be set later on with
     *          {@link #setColorCenter(IColorCenter)}.
     * @param xOffset the x offset to start rendering at, in pixels (or some other sub-cell measurement your game uses)
     * @param yOffset the y offset to start rendering at, in pixels (or some other sub-cell measurement your game uses)
     * @param actualMap will often be a different size than gridWidth by gridHeight, which enables camera scrolling
     */
    public ImageSquidPanel(int gridWidth, int gridHeight, TextCellFactory factory, IColorCenter<Color> center,
                           float xOffset, float yOffset, char[][] actualMap) {
        super(gridWidth, gridHeight, factory, center, xOffset, yOffset, actualMap);
        imageMap = new IntMap<>(128);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        textFactory.configureShader(batch);
        int inc = onlyRenderEven ? 2 : 1, widthInc = inc * cellWidth, heightInc = inc * cellHeight;
        TextureRegion tr;
        float screenX = xOffset - (gridOffsetX <= 0 ? 0 : cellWidth) + getX(),
                screenY_base = 1f + yOffset + (gridOffsetY <= 0 ? 0 : cellHeight) + gridHeight * cellHeight + getY(), screenY;
        char c;
        for (int x = Math.max(0, gridOffsetX-1), xx = (gridOffsetX <= 0) ? 0 : -1; xx <= gridWidth && x < contents.length; x += inc, xx += inc, screenX += widthInc) {
            screenY = screenY_base;
            for (int y = Math.max(0, gridOffsetY-1), yy = (gridOffsetY <= 0) ? 0 : -1; yy <= gridHeight && y < contents[x].length; y += inc, yy += inc, screenY -= heightInc) {
                c = contents[x][y];
                tr = imageMap.get(c);
                if(tr == null)
                    textFactory.draw(batch, c, colors[x][y], screenX, screenY);
                else
                    textFactory.draw(batch, tr, colors[x][y], screenX, screenY);
            }
        }
        super.draw(batch, parentAlpha);
        int len = animatedEntities.size();
        for (int i = 0; i < len; i++) {

            animatedEntities.getAt(i).actor.act(Gdx.graphics.getDeltaTime());
        }
        len = autoActors.size();
        Actor a;
        for (int i = 0; i < len; i++) {
            a = autoActors.getAt(i);
            if(a == null) continue;
            drawActor(batch, parentAlpha, a);
            a.act(Gdx.graphics.getDeltaTime());
        }
    }

    /**
     * Draws one AnimatedEntity, specifically the Actor it contains. Batch must be between start() and end()
     * @param batch Must have start() called already but not stop() yet during this frame.
     * @param parentAlpha This can be assumed to be 1.0f if you don't know it
     * @param ae The AnimatedEntity to draw; the position to draw ae is stored inside it.
     */
    public void drawActor(Batch batch, float parentAlpha, AnimatedEntity ae)
    {
        drawActor(batch, parentAlpha, ae.actor);
    }

    /**
     * Draws one AnimatedEntity, specifically the Actor it contains. Batch must be between start() and end()
     * @param batch Must have start() called already but not stop() yet during this frame.
     * @param parentAlpha This can be assumed to be 1.0f if you don't know it
     * @param ac The Actor to draw; the position to draw ac is modified and reset based on some fields of this object
     */
    public void drawActor(Batch batch, float parentAlpha, Actor ac)
    {
        float prevX = ac.getX(), prevY = ac.getY();
        ac.setPosition(prevX - gridOffsetX * cellWidth, prevY + gridOffsetY * cellHeight);
        ac.draw(batch, parentAlpha);
        ac.setPosition(prevX, prevY);
    }

    @Override
	public void setDefaultForeground(Color defaultForeground) {
        this.defaultForeground = defaultForeground;
    }

	@Override
	public Color getDefaultForegroundColor() {
		return defaultForeground;
	}

    public AnimatedEntity getAnimatedEntityByCell(int x, int y) {
        for(AnimatedEntity ae : animatedEntities)
        {
            if(ae.gridX == x && ae.gridY == y)
                return ae;
        }
        return  null;
    }

    /**
     * Create an AnimatedEntity at position x, y, using the char c in the given color.
     * @param x
     * @param y
     * @param c
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, char c, Color color)
    {
        return animateActor(x, y, false, c, color);
    }

    /**
     * Create an AnimatedEntity at position x, y, using the char c in the given color. If doubleWidth is true, treats
     * the char c as the left char to be placed in a grid of 2-char cells.
     * @param x
     * @param y
     * @param doubleWidth
     * @param c
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, char c, Color color)
    {
        TextureRegion tr = imageMap.get(c);
        if(tr != null)
            return animateActor(x, y, doubleWidth, tr, color, String.valueOf(c));
        Actor a = textFactory.makeActor(c, color);
        a.setName(String.valueOf(c));
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }

    /**
     * Create an AnimatedEntity at position x, y, using the String s in the given color.
     * @param x
     * @param y
     * @param s
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, String s, Color color)
    {
        return animateActor(x, y, false, s, color);
    }

    /**
     * Create an AnimatedEntity at position x, y, using the String s in the given color. If doubleWidth is true, treats
     * the String s as starting in the left cell of a pair to be placed in a grid of 2-char cells.
     * @param x
     * @param y
     * @param doubleWidth
     * @param s
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, String s, Color color)
    {
        Actor a = textFactory.makeActor(s, color);
        a.setName(s);
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }
    /**
     * Create an AnimatedEntity at position x, y, using the String s in the given color. If doubleWidth is true, treats
     * the String s as starting in the left cell of a pair to be placed in a grid of 2-char cells.
     * @param x
     * @param y
     * @param doubleWidth
     * @param s
     * @param colors
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, String s, Collection<Color> colors)
    {
        return animateActor(x, y, doubleWidth, s, colors, 2f);
    }
    /**
     * Create an AnimatedEntity at position x, y, using the String s in the given color. If doubleWidth is true, treats
     * the String s as starting in the left cell of a pair to be placed in a grid of 2-char cells.
     * @param x
     * @param y
     * @param doubleWidth
     * @param s
     * @param colors
     * @param loopTime
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, String s, Collection<Color> colors, float loopTime)
    {
        Actor a = textFactory.makeActor(s, colors, loopTime, doubleWidth);
        a.setName(s);
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }
    /**
     * Create an AnimatedEntity at position x, y, using the char c in the given colors to cycle through. If doubleWidth
     * is true, treats the String s as starting in the left cell of a pair to be placed in a grid of 2-char cells.
     * @param x
     * @param y
     * @param doubleWidth
     * @param c
     * @param colors
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, char c, Collection<Color> colors)
    {
        return animateActor(x, y, doubleWidth, c, colors, 2f);
    }
    /**
     * Create an AnimatedEntity at position x, y, using the char c in the given colors to cycle through. If doubleWidth
     * is true, treats the String s as starting in the left cell of a pair to be placed in a grid of 2-char cells.
     * @param x
     * @param y
     * @param doubleWidth
     * @param c
     * @param colors
     * @param loopTime
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, char c, Collection<Color> colors, float loopTime)
    {
        TextureRegion tr = imageMap.get(c);
        if(tr != null)
            return animateActor(x, y, doubleWidth, tr, colors, loopTime, String.valueOf(c));
        Actor a = textFactory.makeActor(c, colors, loopTime, doubleWidth);
        a.setName(String.valueOf(c));
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }
    /**
     * Create an AnimatedEntity at position x, y, using '^' as its contents, but as an image so it can be rotated.
     * Uses the given colors in a looping pattern, that doesn't count as an animation. If doubleWidth is true, treats
     * the '^' as starting in the middle of a 2-char cell.
     * @param x
     * @param y
     * @param doubleWidth
     * @param colors
     * @param loopTime
     * @return
     */
    public AnimatedEntity directionMarker(int x, int y, boolean doubleWidth, Collection<Color> colors, float loopTime)
    {
        Actor a = textFactory.makeDirectionMarker(colors, loopTime, doubleWidth);
        a.setName("^");
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }
    public AnimatedEntity directionMarker(int x, int y, boolean doubleWidth, Color color)
    {
        Actor a = textFactory.makeDirectionMarker(color);
        a.setName("^");
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }
    /**
     * Create an AnimatedEntity at position x, y, using the char c with a color looked up by index in palette.
     * @param x
     * @param y
     * @param c
     * @param index
     * @param palette
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, char c, int index, ArrayList<Color> palette)
    {
        return animateActor(x, y, c, palette.get(index));
    }

    /**
     * Create an AnimatedEntity at position x, y, using the String s with a color looked up by index in palette.
     * @param x
     * @param y
     * @param s
     * @param index
     * @param palette
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, String s, int index, ArrayList<Color> palette)
    {
        return animateActor(x, y, s, palette.get(index));
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with no color modifications, which will be
     * stretched to fit one cell.
     * @param x
     * @param y
     * @param texture
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, TextureRegion texture)
    {
        return animateActor(x, y, false, texture, Color.WHITE);
        /*
        Actor a = textFactory.makeActor(texture, Color.WHITE);
        a.setName("");
        a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());

        AnimatedEntity ae = new AnimatedEntity(a, x, y);
        animatedEntities.add(ae);
        return ae;
        */
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with the given color, which will be
     * stretched to fit one cell.
     * @param x
     * @param y
     * @param texture
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, TextureRegion texture, Color color)
    {
        return animateActor(x, y, false, texture, color);
        /*
        Actor a = textFactory.makeActor(texture, color);
        a.setName("");
        a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());

        AnimatedEntity ae = new AnimatedEntity(a, x, y);
        animatedEntities.add(ae);
        return ae;
        */
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with no color modifications, which will be
     * stretched to fit one cell, or two cells if doubleWidth is true.
     * @param x
     * @param y
     * @param doubleWidth
     * @param texture
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, TextureRegion texture)
    {
        return animateActor(x, y, doubleWidth, texture, Color.WHITE);
        /*
        Actor a = textFactory.makeActor(texture, Color.WHITE, (doubleWidth ? 2 : 1) * cellWidth, cellHeight);

        a.setName("");
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());

        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
        */
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with the given color, which will be
     * stretched to fit one cell, or two cells if doubleWidth is true.
     * @param x
     * @param y
     * @param doubleWidth
     * @param texture
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, TextureRegion texture, Color color){
        return animateActor(x, y, doubleWidth, texture, color, "");
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with the given color, which will be
     * stretched to fit one cell, or two cells if doubleWidth is true.
     * @param x
     * @param y
     * @param doubleWidth
     * @param texture
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, TextureRegion texture, Color color, String name){
        Actor a = textFactory.makeActor(texture, color, (doubleWidth ? 2 : 1) * cellWidth, cellHeight);
        a.setName(name);
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        /*
        if (doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        */
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }
    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with the given color, which will be
     * stretched to fit one cell, or two cells if doubleWidth is true.
     * @param x
     * @param y
     * @param doubleWidth
     * @param texture
     * @param colors
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, TextureRegion texture, Collection<Color> colors){
        return animateActor(x, y, doubleWidth, texture, colors, 2f);
    }
    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with the given color, which will be
     * stretched to fit one cell, or two cells if doubleWidth is true.
     * @param x
     * @param y
     * @param doubleWidth
     * @param texture
     * @param colors
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, TextureRegion texture, Collection<Color> colors, float loopTime) {
        return animateActor(x, y, doubleWidth, texture, colors, loopTime, "");
    }
    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with the given color, which will be
     * stretched to fit one cell, or two cells if doubleWidth is true.
     * @param x
     * @param y
     * @param doubleWidth
     * @param texture
     * @param colors
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, TextureRegion texture, Collection<Color> colors, float loopTime, String name) {
        Actor a = textFactory.makeActor(texture, colors, loopTime, doubleWidth, (doubleWidth ? 2 : 1) * cellWidth, cellHeight);
        a.setName(name);
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        /*
        if (doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        */
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with no color modifications, which, if and only
     * if stretch is true, will be stretched to fit one cell, or two cells if doubleWidth is true. If stretch is false,
     * this will preserve the existing size of texture.
     * @param x
     * @param y
     * @param doubleWidth
     * @param stretch
     * @param texture
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, boolean stretch, TextureRegion texture)
    {
        Actor a = (stretch)
                ? textFactory.makeActor(texture, Color.WHITE, (doubleWidth ? 2 : 1) * cellWidth, cellHeight)
                : textFactory.makeActor(texture, Color.WHITE, texture.getRegionWidth(), texture.getRegionHeight());
        a.setName("");
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        /*
        if(doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight  - textFactory.getDescent() + getY());
        */
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }

    /**
     * Create an AnimatedEntity at position x, y, using a TextureRegion with the given color, which, if and only
     * if stretch is true, will be stretched to fit one cell, or two cells if doubleWidth is true. If stretch is false,
     * this will preserve the existing size of texture.
     * @param x
     * @param y
     * @param doubleWidth
     * @param stretch
     * @param texture
     * @param color
     * @return
     */
    public AnimatedEntity animateActor(int x, int y, boolean doubleWidth, boolean stretch, TextureRegion texture, Color color) {

        Actor a = (stretch)
                ? textFactory.makeActor(texture, color, (doubleWidth ? 2 : 1) * cellWidth, cellHeight)
                : textFactory.makeActor(texture, color, texture.getRegionWidth(), texture.getRegionHeight());
        a.setName("");
        a.setPosition(adjustX(x, doubleWidth), adjustY(y));
        /*
        if (doubleWidth)
            a.setPosition(x * 2 * cellWidth + getX(), (gridHeight - y - 1) * cellHeight  - textFactory.getDescent() + getY());
        else
            a.setPosition(x * cellWidth + getX(), (gridHeight - y - 1) * cellHeight  - textFactory.getDescent() + getY());
            */
        AnimatedEntity ae = new AnimatedEntity(a, x, y, doubleWidth);
        animatedEntities.add(ae);
        return ae;
    }
    
	@Override
	public ISquidPanel<Color> getBacker() {
		return this;
	}

	/**
	 * Use this method if you use your own {@link IColorCenter} and want this
	 * panel not to allocate its own colors (or fill
	 * {@link DefaultResources#getSCC()} but instead to the provided center.
	 * 
	 * @param scc
	 *            The color center to use. Should not be {@code null}.
	 * @return {@code this}
	 * @throws NullPointerException
	 *             If {@code scc} is {@code null}.
	 */
	@Override
	public ImageSquidPanel setColorCenter(IColorCenter<Color> scc) {
	    super.setColorCenter(scc);
		return this;
	}

    /**
     * Gets a "snapshot" of the data represented by this ImageSquidPanel; stores the dimensions, the char data, and the
     * color data in a way that can be set back to a SquidPanel or ImageSquidPanel using
     * {@link #setFromSnapshot(String, int, int, int, int)} or its overload that takes a StringBuilder. The actual
     * contents of the returned StringBuilder are unlikely to be legible in any way if read as text, and are meant to be
     * concise and stable across versions.
     * <br>
     * NOTE: For this version, the mapping of chars to images is not stored in the snapshot, allowing alternate mappings
     * to be used, such as while graphics are being updated frequently. This also allows the snapshot to be read in from
     * both normal SquidPanels and ImageSquidPanels.
     * @return a StringBuilder representation of this SquidPanel's data that can be passed later to {@link #setFromSnapshot(StringBuilder, int, int, int, int)} or converted to String and passed to its overload
     */
    public StringBuilder getSnapshot()
    {
        return getSnapshot(0, 0, gridWidth, gridHeight);
    }
    /**
     * Gets a "snapshot" of the data represented by this ImageSquidPanel; stores the dimensions, the char data, and the
     * color data in a way that can be set back to a SquidPanel or ImageSquidPanel using
     * {@link #setFromSnapshot(String, int, int, int, int)} or its overload that takes a StringBuilder. The actual
     * contents of the returned StringBuilder are unlikely to be legible in any way if read as text, and are meant to be
     * concise and stable across versions. This overload allows the first x and y position used to be specified, as well
     * as the width and height to use (the actual width and height stored may be different if this SquidPanel's
     * gridWidth and/or gridHeight are smaller than the width and/or height given).
     * <br>
     * NOTE: For this version, the mapping of chars to images is not stored in the snapshot, allowing alternate mappings
     * to be used, such as while graphics are being updated frequently. This also allows the snapshot to be read in from
     * both normal SquidPanels and ImageSquidPanels.
     * @param startX the first x position to use in the snapshot, inclusive
     * @param startY the first y position to use in the snapshot, inclusive
     * @param width how wide the snapshot area should be; x positions from startX to startX + width - 1 will be used
     * @param height how tall the snapshot area should be; y positions from startY to startY + height - 1 will be used
     * @return a StringBuilder representation of this SquidPanel's data that can be passed later to {@link #setFromSnapshot(StringBuilder, int, int, int, int)} or converted to String and passed to its overload
     */
    public StringBuilder getSnapshot(int startX, int startY, int width, int height) {
        width = Math.min(gridWidth - startX, width);
        height = Math.min(gridHeight - startY, height);
        StringBuilder sb = new StringBuilder(width * height * 9 + 12);
        sb.append(width).append('x').append(height).append(':');
        for (int x = startX, i = 0; i < width; x++, i++) {
            sb.append(contents[x], startY, height);
        }
        char[] reuse = new char[8];
        for (int x = startX, i = 0; i < width; x++, i++) {
            for (int y = startY, j = 0; j < height; y++, j++) {
                sb.append(SColor.floatToChars(reuse, colors[x][y]));
            }
        }
        return sb;
    }

    /**
     * Given a "snapshot" from {@link #getSnapshot(int, int, int, int)}, this assigns the chars and colors in this
     * ImageSquidPanel from 0,0 (inclusive) up to the dimensions stored in the snapshot to match the snapshot's data.
     * @param snapshot a StringBuilder in a special format as produced by {@link #getSnapshot(int, int, int, int)}
     * @return this after setting, for chaining
     */
    public ImageSquidPanel setFromSnapshot(StringBuilder snapshot)
    {
        return setFromSnapshot(snapshot, 0, 0, -1, -1);
    }
    /**
     * Given a "snapshot" from {@link #getSnapshot(int, int, int, int)}, this assigns the chars and colors in this
     * ImageSquidPanel from the position given by putX,putY (inclusive) up to the dimensions stored in the snapshot
     * (considering putX and putY as offsets) so they have the values stored in the snapshot.
     * @param snapshot a StringBuilder in a special format as produced by {@link #getSnapshot(int, int, int, int)}
     * @param putX where to start placing the data from the snapshot, x position
     * @param putY where to start placing the data from the snapshot, y position
     * @return this after setting, for chaining
     */
    public ImageSquidPanel setFromSnapshot(StringBuilder snapshot, int putX, int putY)
    {
        return setFromSnapshot(snapshot, putX, putY, -1, -1);
    }
    /**
     * Given a "snapshot" from {@link #getSnapshot(int, int, int, int)}, this assigns the chars and colors in this
     * ImageSquidPanel from the position given by putX,putY (inclusive) to putX+limitWidth,putY+limitHeight (exclusive)
     * so they have the values stored in the snapshot. If limitWidth or limitHeight is negative, this uses the full
     * width and height of the snapshot (stopping early if it would extend past the gridWidth or gridHeight of this
     * ImageSquidPanel).
     * @param snapshot a StringBuilder in a special format as produced by {@link #getSnapshot(int, int, int, int)}
     * @param putX where to start placing the data from the snapshot, x position
     * @param putY where to start placing the data from the snapshot, y position
     * @param limitWidth if negative, uses all of snapshot's width as possible, otherwise restricts the width allowed
     * @param limitHeight if negative, uses all of snapshot's height as possible, otherwise restricts the height allowed
     * @return this after setting, for chaining
     */
    public ImageSquidPanel setFromSnapshot(StringBuilder snapshot, int putX, int putY, int limitWidth, int limitHeight)
    {
        if(putX >= gridWidth || putY >= gridHeight || snapshot == null || snapshot.length() < 4) return this;
        if(putX < 0) putX = 0;
        if(putY < 0) putY = 0;
        int start = snapshot.indexOf(":")+1, width = StringKit.intFromDec(snapshot),
                height = StringKit.intFromDec(snapshot, snapshot.indexOf("x") + 1, start),
                run = start;
        if(limitWidth < 0)
            limitWidth = Math.min(width, gridWidth - putX);
        else
            limitWidth = Math.min(limitWidth, Math.min(width, gridWidth - putX));

        if(limitHeight < 0)
            limitHeight = Math.min(height, gridHeight - putY);
        else
            limitHeight = Math.min(limitHeight, Math.min(height, gridHeight - putY));
        for (int x = putX, i = 0; i < limitWidth; x++, i++, run += height) {
            snapshot.getChars(run, run + limitHeight, contents[x], putY);
        }
        run = start + width * height;
        for (int x = putX, i = 0; i < limitWidth; x++, i++) {
            for (int y = putY, j = 0; j < limitHeight; y++, j++) {
                colors[x][y] = SColor.charsToFloat(snapshot, run);
                run += 8;
            }
        }
        return this;
    }

    /**
     * Given a "snapshot" from {@link #getSnapshot(int, int, int, int)}, this assigns the chars and colors in this
     * ImageSquidPanel from 0,0 (inclusive) up to the dimensions stored in the snapshot to match the snapshot's data.
     * <br>
     * This overload takes a String instead of a StringBuilder for potentially-easier loading from files.
     * @param snapshot a String in a special format as produced by {@link #getSnapshot(int, int, int, int)}
     * @return this after setting, for chaining
     */
    public ImageSquidPanel setFromSnapshot(String snapshot)
    {
        return setFromSnapshot(snapshot, 0, 0, -1, -1);
    }
    /**
     * Given a "snapshot" from {@link #getSnapshot(int, int, int, int)}, this assigns the chars and colors in this
     * ImageSquidPanel from the position given by putX,putY (inclusive) up to the dimensions stored in the snapshot
     * (considering putX and putY as offsets) so they have the values stored in the snapshot.
     * <br>
     * This overload takes a String instead of a StringBuilder for potentially-easier loading from files.
     * @param snapshot a String in a special format as produced by {@link #getSnapshot(int, int, int, int)}
     * @param putX where to start placing the data from the snapshot, x position
     * @param putY where to start placing the data from the snapshot, y position
     * @return this after setting, for chaining
     */
    public ImageSquidPanel setFromSnapshot(String snapshot, int putX, int putY)
    {
        return setFromSnapshot(snapshot, putX, putY, -1, -1);
    }

    /**
     * Given a "snapshot" from {@link #getSnapshot(int, int, int, int)}, this assigns the chars and colors in this
     * ImageSquidPanel from the position given by putX,putY (inclusive) to putX+limitWidth,putY+limitHeight (exclusive)
     * so they have the values stored in the snapshot. If limitWidth or limitHeight is negative, this uses the full
     * width and height of the snapshot (stopping early if it would extend past the gridWidth or gridHeight of this
     * ImageSquidPanel).
     * <br>
     * This overload takes a String instead of a StringBuilder for potentially-easier loading from files.
     * @param snapshot a String in a special format as produced by {@link #getSnapshot(int, int, int, int)}
     * @param putX where to start placing the data from the snapshot, x position
     * @param putY where to start placing the data from the snapshot, y position
     * @param limitWidth if negative, uses all of snapshot's width as possible, otherwise restricts the width allowed
     * @param limitHeight if negative, uses all of snapshot's height as possible, otherwise restricts the height allowed
     * @return this after setting, for chaining
     */

    public ImageSquidPanel setFromSnapshot(String snapshot, int putX, int putY, int limitWidth, int limitHeight)
    {
        if(putX >= gridWidth || putY >= gridHeight || snapshot == null || snapshot.length() < 4) return this;
        if(putX < 0) putX = 0;
        if(putY < 0) putY = 0;
        int start = snapshot.indexOf(":")+1, width = StringKit.intFromDec(snapshot),
                height = StringKit.intFromDec(snapshot, snapshot.indexOf("x") + 1, start),
                run = start;
        if(limitWidth < 0)
            limitWidth = Math.min(width, gridWidth - putX);
        else
            limitWidth = Math.min(limitWidth, Math.min(width, gridWidth - putX));

        if(limitHeight < 0)
            limitHeight = Math.min(height, gridHeight - putY);
        else
            limitHeight = Math.min(limitHeight, Math.min(height, gridHeight - putY));
        for (int x = putX, i = 0; i < limitWidth; x++, i++, run += height) {
            snapshot.getChars(run, run + limitHeight, contents[x], putY);
        }
        run = start + width * height;
        for (int x = putX, i = 0; i < limitWidth; x++, i++) {
            for (int y = putY, j = 0; j < limitHeight; y++, j++) {
                colors[x][y] = SColor.charsToFloat(snapshot, run);
                run += 8;
            }
        }
        return this;
    }

    /**
     * Makes it so when the char swapOut would be drawn, the TextureRegion swapIn is drawn instead.
     * This will apply for as long as this ImageSquidPanel is in use unless swapIn is removed with
     * {@link #removeImageSwap(char)} or directly changing this object's {@link #imageMap}.
     * @param swapOut the char to avoid rendering and replace with an image
     * @param swapIn the image to replace the character with
     */
    public void setImageSwap(final char swapOut, final TextureRegion swapIn)
    {
        if(swapIn != null)
            imageMap.put(swapOut, swapIn);
    }

    /**
     * Removes the char toRemove from the mapping of chars to replace with images, or does nothing if this did not
     * already replace toRemove with an image. This means toRemove will render as itself, if present in the font.
     * @param toRemove the char to render as a glyph instead of a texture
     */
    public void removeImageSwap(final char toRemove)
    {
        imageMap.remove(toRemove);
    }

    /**
     * If there is a TextureRegion that would replace the char toFind when drawn, this will return that TextureRegion,
     * otherwise it returns null.
     * @param toFind the char to find the corresponding TextureRegion that it could be mapped to
     * @return the corresponding TextureRegion if it exists, or null otherwise
     */
    public TextureRegion getImageSwap(final char toFind)
    {
        return imageMap.get(toFind);
    }

    /**
     * Meant for taking easy-to-write chars and generating chars that can map to images, while still allowing the
     * original easy-to-write char to be used as its own non-image char. Given a char that should be from the
     * "relatively common" parts of Unicode (essentially, anything with a codepoint before 0x1000, or with a small risk
     * of collision, anything before 0x8000), this returns a character from the "Private Use Area" of Unicode that will
     * probably be unique for most kinds of input. 4096 possible chars can be returned by this method.
     *
     * As an example, you may want to map the char 'g' to an image of a goblin, but still sometimes show the actual 'g'
     * for some other monster or a variant on goblins you don't have an image for. You could call
     * {@code char goblinChar = ImageSquidPanel.getUnusedChar('g');} and then register goblinChar in the image mapping
     * with {@code myImageSquidPanel.setImageSwap(goblinChar, goblinImage);}, which would allow you to draw 'g' directly
     * by placing a 'g' with put(), or draw goblinImage by placing either goblinChar or
     * {@code ImageSquidPanel.getUnusedChar('g')} with put().
     *
     * @param initial a char that ideally should be from the earlier parts of Unicode (before 0x1000 is ideal)
     * @return a char from Unicode's private use area that is unlikely to be found otherwise or accidentally
     */
    public static char getUnusedChar(final char initial)
    {
        return (char)(((initial & 0x7000) >>> 3 ^ initial) & 0x0FFF | 0xE000);
    }
}
