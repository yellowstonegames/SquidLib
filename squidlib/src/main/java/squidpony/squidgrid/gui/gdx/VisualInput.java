package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.CharArray;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.util.Map;
import java.util.TreeMap;

/**
 * Acts like SquidInput but displays available keys as visible buttons if the device has no physical keyboard.
 * Only relevant if you want keyboard input to always be available, so this doesn't implement the SquidInput
 * constructors that don't handle any keys (the buttons this shows will act like keys even on Android and iOS).
 * <br>
 * This doesn't currently work well when used with a SquidPanel or SquidLayers that pans a camera over an area
 * that is larger than one screen (you only need to worry about this in "standard" SquidLib usage if you use
 * the {@link SquidPanel#SquidPanel(int, int, TextCellFactory, squidpony.IColorCenter, float, float, char[][])}) or
 * {@link SquidLayers#SquidLayers(int, int, int, int, TextCellFactory, SquidColorCenter, SquidColorCenter, char[][])}
 * constructors with a larger 2D char array than the given gridWidth and/or gridHeight). It works fine if the
 * area that can be seen on one screen is as big as it gets, such as in the normal text-based mode of games like
 * Rogue and Brogue. It can also work if you manually handle movement over a larger area without using libGDX
 * Viewport and Camera code to accomplish that, since this class needs to handle the Viewport to some extent.
 * <br>
 * Created by Tommy Ettinger on 4/15/2016.
 */
public class VisualInput extends SquidInput {

    public SquidPanel left, right;
    protected TextCellFactory tcfLeft, tcfRight;
    protected TreeMap<Character, String> availableKeys;
    protected SquidMouse mouseLeft, mouseRight;
    private int sectionWidth, sectionHeight;
    private float screenWidth = -1, screenHeight = -1;
    protected boolean initialized = false;
    public Color color = Color.WHITE;
    protected CharArray clicks;
    public boolean eightWay = true, forceButtons = false;
    public Stage stage;
    protected ShrinkPartViewport spv;
    private void ready(TextCellFactory font)
    {
        initialized = true;
        sectionWidth = Gdx.graphics.getWidth() / 8;
        sectionHeight = Gdx.graphics.getHeight();

        tcfLeft = font.copy().width(sectionWidth / 4).height(sectionHeight / 16).initBySize();
        tcfRight = font.copy().width(sectionWidth / 12).height(sectionHeight / 24).initBySize();

        left = new SquidPanel(4, 16, tcfLeft);
        if(eightWay) {
            left.put(0, 7, new char[][]{
                    new char[]{'\\', '-', '/'},
                    new char[]{'|', 'O', '|'},
                    new char[]{'/', '-', '\\'},
            }, color);
        }
        else
        {
            left.put(0, 7, new char[][]{
                    new char[]{' ', SquidInput.LEFT_ARROW, ' '},
                    new char[]{SquidInput.UP_ARROW, 'O', SquidInput.DOWN_ARROW},
                    new char[]{' ', SquidInput.RIGHT_ARROW, ' '},
            }, color);        }
        right = new SquidPanel(12, 24, tcfRight, null, Gdx.graphics.getWidth() - sectionWidth, 0);

        mouseLeft = new SquidMouse(left.cellWidth(), left.cellHeight(), left.gridWidth, left.gridHeight,
                0, 0, new InputAdapter()
        {
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(screenX < 3 && screenY >= 5 && screenY <= 7 && left.contents[screenX][screenY + 2] != 0 &&
                        left.contents[screenX][screenY + 2] != ' ')
                {
                    switch ((screenY - 5) * 3 + screenX)
                    {
                        case 0: queue.add(SquidInput.UP_LEFT_ARROW);
                            break;
                        case 1: queue.add(SquidInput.UP_ARROW);
                            break;
                        case 2: queue.add(SquidInput.UP_RIGHT_ARROW);
                            break;
                        case 3: queue.add(SquidInput.LEFT_ARROW);
                            break;
                        case 4: queue.add(SquidInput.CENTER_ARROW);
                            break;
                        case 5: queue.add(SquidInput.RIGHT_ARROW);
                            break;
                        case 6: queue.add(SquidInput.DOWN_LEFT_ARROW);
                            break;
                        case 7: queue.add(SquidInput.DOWN_ARROW);
                            break;
                        case 8: queue.add(SquidInput.DOWN_RIGHT_ARROW);
                            break;
                        default:
                            return false;
                    }
                    queue.add('\u0000');
                    return true;
                }
                else
                    return false;
            }
        });
        //if(mouse != null)
          //  mouse.setOffsetX(-sectionWidth);
        stage = new Stage(new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        stage.addActor(left);
        stage.addActor(right);
    }
    private void fillActions()
    {
        if(!initialized)
            return;
        int y = 0;
        clicks = new CharArray(right.getGridHeight());
        for(Map.Entry<Character, String> kv : availableKeys.entrySet())
        {
            switch (kv.getKey())
            {
                case SquidInput.UP_LEFT_ARROW:
                case SquidInput.UP_ARROW:
                case SquidInput.UP_RIGHT_ARROW:
                case SquidInput.LEFT_ARROW:
                case SquidInput.CENTER_ARROW:
                case SquidInput.RIGHT_ARROW:
                case SquidInput.DOWN_LEFT_ARROW:
                case SquidInput.DOWN_ARROW:
                case SquidInput.DOWN_RIGHT_ARROW:
                    break;
                default:
                    right.put(1, y, kv.getValue(), color);
                    clicks.add(kv.getKey());
                    y++;
            }
            if(y > right.getGridHeight())
                break;
        }
        mouseRight = new SquidMouse(right.cellWidth(), right.cellHeight(), right.gridWidth(), right.gridHeight(),
                Gdx.graphics.getWidth() - sectionWidth, Math.round(sectionHeight - right.getHeight()),
                new InputAdapter()
        {
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(screenY < clicks.size)
                {
                    queue.add(clicks.get(screenY));
                    queue.add('\u0000');
                    return true;
                }
                return false;
            }
        });
    }
    /**
     * Convenience method that does essentially the same thing as init(Map&lt;Character, String&gt;).
     * This assumes that each String passed to this is an action, and the first letter of a String is the character that
     * a keypress would generate to perform the action named by the String. For example, calling this with
     * {@code init("fire", "Fortify")} would register "fire" under 'f' and "Fortify" under 'F', displaying the Strings
     * instead of the characters if possible. The first char of each String should be unique in the arguments.
     * <br>
     * This also initializes the displayed buttons if there is no hardware keyboard available. This uses the color and
     * eightWay fields to determine what color the buttons will be drawn with and if directions should be 8-way or
     * 4-way, respectively. These fields should be set before calling init() if you don't want the defaults, which are
     * white buttons and 8-way directions. The font with this overload of init is set to Inconsolata-LGC-Custom, which
     * is normally available via {@link DefaultResources#getStretchableFont()} and requires the .fnt file and .png file
     * listed in the documentation for that method. If you don't have those files, but have some other font (preferably
     * some kind of distance field font, since this resizes the font significantly), you can use
     * {@link #init(TextCellFactory, String...)} or {@link #init(TextCellFactory, Map)} to have this use your font.
     * @param enabled an array or vararg of Strings that name actions; the first char of each String should be unique
     */
    public void init(String... enabled) {
        init(DefaultResources.getStretchableFont(), enabled);
    }
    /**
     * Convenience method that does essentially the same thing as init(Map&lt;Character, String&gt;).
     * This assumes that each String passed to this is an action, and the first letter of a String is the character that
     * a keypress would generate to perform the action named by the String. For example, calling this with
     * {@code init("fire", "Fortify")} would register "fire" under 'f' and "Fortify" under 'F', displaying the Strings
     * instead of the characters if possible. The first char of each String should be unique in the arguments.
     * <br>
     * This also initializes the displayed buttons if there is no hardware keyboard available. This uses the color and
     * eightWay fields to determine what color the buttons will be drawn with and if directions should be 8-way or
     * 4-way, respectively. These fields should be set before calling init() if you don't want the defaults, which are
     * white buttons and 8-way directions.
     * @param font the TextCellFactory to use for the buttons, usually a distance field font from DefaultResources
     * @param enabled an array or vararg of Strings that name actions; the first char of each String should be unique
     */
    public void init(TextCellFactory font, String... enabled)
    {
        if(!forceButtons && Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard))
            return;
        ready(font);
        availableKeys = new TreeMap<>();
        if(enabled == null)
            return;
        for (int i = 0; i < enabled.length; i++) {
            if(enabled[i] != null && !enabled[i].isEmpty())
                availableKeys.put(enabled[i].charAt(0), enabled[i]);
        }
        fillActions();
    }
    /**
     * For each char and String in available, registers each keyboard key (as a char, such as 'A' or
     * SquidInput.LEFT_ARROW) with a String that names the action that key is used to perform, and makes these Strings
     * available as buttons on the right side of the screen if on a device with no hardware keyboard. Arrows will be
     * provided on the left side of the screen for directions.
     * <br>
     * This also initializes the displayed buttons if there is no hardware keyboard available. This uses the color and
     * eightWay fields to determine what color the buttons will be drawn with and if directions should be 8-way or
     * 4-way, respectively. These fields should be set before calling init() if you don't want the defaults, which are
     * white buttons and 8-way directions. The font with this overload of init is set to Inconsolata-LGC-Custom, which
     * is normally available via {@link DefaultResources#getStretchableFont()} and requires the .fnt file and .png file
     * listed in the documentation for that method. If you don't have those files, but have some other font (preferably
     * some kind of distance field font, since this resizes the font significantly), you can use
     * {@link #init(TextCellFactory, String...)} or {@link #init(TextCellFactory, Map)} to have this use your font.
     * @param available a Map of Character keys representing keyboard keys and Strings for the actions they trigger
     */
    public void init(Map<Character, String> available)
    {
        init(DefaultResources.getStretchableFont(), available);
    }
    /**
     * For each char and String in available, registers each keyboard key (as a char, such as 'A' or
     * SquidInput.LEFT_ARROW) with a String that names the action that key is used to perform, and makes these Strings
     * available as buttons on the right side of the screen if on a device with no hardware keyboard. Arrows will be
     * provided on the left side of the screen for directions.
     * <br>
     * This uses the color and eightWay fields to determine what color the buttons will be drawn with and if directions
     * should be 8-way or 4-way, respectively. These fields should be set before calling init() if you don't want the
     * defaults, which are white buttons and 8-way directions.
     * @param font the TextCellFactory to use for the buttons, usually a distance field font from DefaultResources
     * @param available a Map of Character keys representing keyboard keys and Strings for the actions they trigger
     */
    public void init(TextCellFactory font, Map<Character, String> available)
    {
        if(!forceButtons && Gdx.input.isPeripheralAvailable(Input.Peripheral.HardwareKeyboard))
            return;
        ready(font);
        if(available != null) {
            availableKeys = new TreeMap<>(available);
            fillActions();
        }
    }
    private VisualInput() {

    }

    public VisualInput(KeyHandler keyHandler) {
        super(keyHandler);
    }

    public VisualInput(KeyHandler keyHandler, boolean ignoreInput) {
        super(keyHandler, ignoreInput);
    }

    public VisualInput(KeyHandler keyHandler, SquidMouse mouse) {
        super(keyHandler, mouse);
    }

    public VisualInput(KeyHandler keyHandler, SquidMouse mouse, boolean ignoreInput) {
        super(keyHandler, mouse, ignoreInput);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (initialized && mouseLeft.onGrid(screenX, screenY))
            return mouseLeft.touchDown(screenX, screenY, pointer, button);
        else if (initialized && mouseRight.onGrid(screenX, screenY))
            return mouseRight.touchDown(screenX, screenY, pointer, button);
        if(spv != null) {
            screenX *= spv.getScreenWidth() / (spv.getScreenWidth() - spv.barWidth);
        }
        return (!initialized || (mouse != null && mouse.onGrid(screenX, screenY))) && super.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (initialized && mouseLeft.onGrid(screenX, screenY))
            return mouseLeft.touchUp(screenX, screenY, pointer, button);
        else if (initialized && mouseRight.onGrid(screenX, screenY))
            return mouseRight.touchUp(screenX, screenY, pointer, button);
        if(spv != null) {
            screenX *= spv.getScreenWidth() / (spv.getScreenWidth() - spv.barWidth);
        }

        return (!initialized || (mouse != null && mouse.onGrid(screenX, screenY))) && super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (initialized && mouseLeft.onGrid(screenX, screenY))
            return mouseLeft.touchDragged(screenX, screenY, pointer);
        else if (initialized && mouseRight.onGrid(screenX, screenY))
            return mouseRight.touchDragged(screenX, screenY, pointer);
        if(spv != null) {
            screenX *= spv.getScreenWidth() / (spv.getScreenWidth() - spv.barWidth);
        }
        return (!initialized || (mouse != null && mouse.onGrid(screenX, screenY))) && super.touchDragged(screenX, screenY, pointer);

    }
    @Override
    public boolean mouseMoved (int screenX, int screenY) {
        if(spv != null) {
            screenX *= spv.getScreenWidth() / (spv.getScreenWidth() - spv.barWidth);
        }

        if(ignoreInput || mouse == null || !mouse.onGrid(screenX, screenY)) return false;
        return mouse.mouseMoved(screenX, screenY);
    }
    public void reinitialize(float cellWidth, float cellHeight, float gridWidth, float gridHeight,
                             int offsetX, int offsetY, float screenWidth, float screenHeight)
    {
        if(!initialized)
        {
            if(mouse != null) 
                mouse.reinitialize(cellWidth, cellHeight, gridWidth, gridHeight, offsetX, offsetY);
            return;
        }
        if(this.screenWidth > 0)
            sectionWidth *= screenWidth / this.screenWidth;
        else
            sectionWidth *= screenWidth / Gdx.graphics.getWidth();
        if(this.screenHeight > 0)
            sectionHeight *= screenHeight / this.screenHeight;
        else
            sectionHeight *= screenHeight / Gdx.graphics.getHeight();
        cellWidth /= screenWidth / (screenWidth - sectionWidth * 0.75f);
        float leftWidth = screenWidth / 32f, rightWidth = screenWidth / 96f,
                leftHeight = screenHeight / 12f, rightHeight = screenHeight / 24f;
        if(mouse != null) 
            mouse.reinitialize(cellWidth, cellHeight, gridWidth, gridHeight,
                offsetX - MathUtils.round((screenWidth * 0.125f) * (screenWidth / (screenWidth - sectionWidth)) + cellWidth * 0.5f), offsetY);
        mouseLeft.reinitialize(leftWidth, leftHeight, 4, 16, offsetX, offsetY);
        mouseRight.reinitialize(rightWidth, rightHeight, 12, 24,
                MathUtils.ceil(offsetX - (screenWidth - sectionWidth)),
                MathUtils.round(offsetY - rightHeight * 0.5f + (right.getGridHeight() * rightHeight - screenHeight)));
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        if(spv != null)
            spv.barWidth = sectionWidth;
    }
    public ShrinkPartViewport resizeInnerStage(Stage insides)
    {
        if(!initialized)
            return null;
        /*
        insides.getViewport().setWorldWidth(insides.getViewport().getWorldWidth() - screenWidth * 2);
        insides.getViewport().setScreenX(screenWidth);
        insides.getViewport().setScreenY(0);
        */
        spv = new ShrinkPartViewport(insides.getWidth(), insides.getHeight(), sectionWidth);
        insides.setViewport(spv);
        return spv;
    }

    public int getSectionWidth() {
        return sectionWidth;
    }

    public int getSectionHeight() {
        return sectionHeight;
    }

    public void update(int width, int height, boolean centerCamera) {
        if(initialized)
        {
            stage.getViewport().update(width, height, centerCamera);
        }
    }

    public void show() {
        if(initialized) {
            stage.getViewport().apply(true);
            stage.draw();
            stage.act();
        }
    }
}
