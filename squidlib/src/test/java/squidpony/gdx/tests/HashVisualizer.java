package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.IFilter;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.RandomnessSource;
import squidpony.squidmath.ThunderRNG;

import java.util.Arrays;

/**
 * Created by Tommy Ettinger on 8/20/2016.
 */
public class HashVisualizer extends ApplicationAdapter {
    private SpriteBatch batch;
    private SquidColorCenter colorFactory;
    private SquidPanel display, overlay;
    private int width, height;
    private int cellWidth, cellHeight;
    private SquidInput input;
    private static final SColor bgColor = SColor.BLACK;
    private Stage stage;
    private Viewport view;
    private int hashMode = 0;
    private CrossHash.Sip sipA;
    private CrossHash.Storm stormA, stormB, stormC;
    private int testType = 0;
    private RandomnessSource random;

    @Override
    public void create () {
        batch = new SpriteBatch();
        width = 512;
        height = 512;
        cellWidth = 1;
        cellHeight = 1;
        display = new SquidPanel(width, height, 1, 1);
        overlay = new SquidPanel(16, 8, DefaultResources.getStretchableFont().width(32).height(64).initBySize());
        IFilter<Color> filter0 = new Filters.PaletteFilter(
                new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,                           0f, 0.125f, 0.25f, 0.375f, 0.5f, 0.625f, 0.75f, 0.875f, 1f},
                new float[]{0f, 0.125f, 0.25f, 0.375f, 0.5f, 0.625f, 0.75f, 0.875f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,},
                new float[]{0.5f, 0.5625f, 0.625f, 0.6875f, 0.75f, 0.8125f, 0.875f, 0.9375f, 1f, 0.5f, 0.5625f, 0.625f, 0.6875f, 0.75f, 0.8125f, 0.875f, 0.9375f, 1f},
                new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f,}),
        filter1 = new Filters.PaletteFilter(SColor.YELLOW_GREEN_SERIES),// new Filters.PaletteFilter(SColor.BLUE_VIOLET_SERIES),
        filter2 = new Filters.PaletteFilter(new SColor[]{SColor.TREE_PEONY, SColor.NAVAJO_WHITE, SColor.BELLFLOWER, SColor.CAPE_JASMINE, SColor.CELADON, SColor.DAWN, SColor.TEAL}),
        filter3 = new Filters.GrayscaleFilter(),// new Filters.PaletteFilter(SColor.BLUE_VIOLET_SERIES),
        filter4 = new Filters.PaletteFilter(new SColor[]{SColor.NAVAJO_WHITE, SColor.CAPE_JASMINE, SColor.LEMON_CHIFFON, SColor.PEACH_YELLOW});
        colorFactory = new SquidColorCenter(filter4);
        sipA = new CrossHash.Sip();
        stormA = new CrossHash.Storm();
        stormB= new CrossHash.Storm(0xBEEFF00DCAFECABAL);
        stormC = new CrossHash.Storm(16L);
        random = new ThunderRNG(0xBEEFCAFEF00DCABAL, 0x1337BABECAFECABAL);
        view = new ScreenViewport();
        stage = new Stage(view, batch);

        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key)
                {
                    case SquidInput.ENTER:
                        hashMode++;
                        hashMode %= 28;
                        //Gdx.graphics.requestRendering();
                        break;
                    case 'S':
                    case 's':
                        testType = (testType + 1) % 3;
                        //Gdx.graphics.requestRendering();
                        break;
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE:
                    {
                        Gdx.app.exit();
                    }
                }
            }
        });
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(input);
        // and then add display, our one visual component, to the list of things that act in Stage.
        display.setPosition(0, 0);
        overlay.setPosition(0, 0);
        Stack stk = new Stack(display, overlay);
        stage.addActor(stk);
        stk.layout();

        //Gdx.graphics.setContinuousRendering(false);
        //Gdx.graphics.requestRendering();
    }
    public void putMap()
    {
        display.erase();
        overlay.erase();
        int[] coordinates = new int[2], coordinate = new int[1];
        long code;
        switch (testType) {
            case 1: {
                switch (hashMode) {
                    case 0:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 1:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormA.hash(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 2:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormB.hash(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 3:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormC.hash(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 4:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 5:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 6:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 7:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 8:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormA.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 9:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormB.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 10:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormC.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 11:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 12:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 13:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 14:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 15:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormA.hash(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 16:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormB.hash(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 17:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormC.hash(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 18:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 19:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 20:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 21:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 22:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormA.hash64(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 23:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormB.hash64(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 24:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = stormC.hash64(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 25:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormA.hash64(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 26:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormB.hash64(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 27:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = stormC.hash64(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                }
            }
            break;
            case 0: {
                switch (hashMode) {
                    case 0:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 1:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 2:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = sipA.hash(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 3:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 4:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 5:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 6:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = sipA.hash(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 7:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 8:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 9:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = sipA.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 10:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash64(coordinates) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 11:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 12:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = sipA.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 13:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash64(coordinate) << 8 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 14:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = Arrays.hashCode(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 15:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 16:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = sipA.hash(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 17:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 18:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = Arrays.hashCode(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 19:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 20:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = sipA.hash(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 21:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 22:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.hash64(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 23:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = sipA.hash64(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 24:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = x;
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = y;
                                code = CrossHash.Lightning.hash64(coordinates) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 25:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.hash64(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 26:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = sipA.hash64(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 27:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = (x << 9) | y;
                                code = CrossHash.Lightning.hash64(coordinate) & 0xFFFFFF00 | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                }
            }
            break;
            default:
            {
                switch (hashMode) {
                    case 0:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + random.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + random.next(2)) >>> 2) << 3);
                                code = Arrays.hashCode(coordinates) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 1:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + random.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + random.next(2)) >>> 2) << 3);
                                code = CrossHash.hash(coordinates) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 2:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + random.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + random.next(2)) >>> 2) << 3);
                                code = sipA.hash(coordinates) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 3:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + random.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + random.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash(coordinates) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 4:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + random.next(2)) >>> 2) << 9) | ((y + random.next(2)) >>> 2));
                                code = Arrays.hashCode(coordinate) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 5:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + random.next(2)) >>> 2) << 9) | ((y + random.next(2)) >>> 2));
                                code = CrossHash.hash(coordinate) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 6:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + random.next(2)) >>> 2) << 9) | ((y + random.next(2)) >>> 2));
                                code = sipA.hash(coordinate) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 7:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + random.next(2)) >>> 2) << 9) | ((y + random.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash(coordinate) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 8:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + random.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + random.next(2)) >>> 2) << 3);
                                code = CrossHash.hash64(coordinates) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 9:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + random.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + random.next(2)) >>> 2) << 3);
                                code = sipA.hash64(coordinates) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 10:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + random.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + random.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash64(coordinates) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 11:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + random.next(2)) >>> 2) << 9) | ((y + random.next(2)) >>> 2));
                                code = CrossHash.hash64(coordinate) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 12:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + random.next(2)) >>> 2) << 9) | ((y + random.next(2)) >>> 2));
                                code = sipA.hash64(coordinate) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 13:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + random.next(2)) >>> 2) << 9) | ((y + random.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash64(coordinate) & 7L; code = 0xFF00L * (code & 1L) | 0xFF0000L * ((code & 2L) >> 1) | 0xFF000000L * ((code & 4L) >> 2) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 14:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + random.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + random.next(2)) >>> 2) << 3);
                                code = Arrays.hashCode(coordinates) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 15:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + random.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + random.next(2)) >>> 2) << 3);
                                code = CrossHash.hash(coordinates) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 16:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + random.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + random.next(2)) >>> 2) << 3);
                                code = sipA.hash(coordinates) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 17:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + random.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + random.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash(coordinates) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 18:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + random.next(2)) >>> 2) << 9) | ((y + random.next(2)) >>> 2));
                                code = Arrays.hashCode(coordinate) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 19:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + random.next(2)) >>> 2) << 9) | ((y + random.next(2)) >>> 2));
                                code = CrossHash.hash(coordinate) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 20:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + random.next(2)) >>> 2) << 9) | ((y + random.next(2)) >>> 2));
                                code = sipA.hash(coordinate) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 21:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + random.next(2)) >>> 2) << 9) | ((y + random.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash(coordinate) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 22:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + random.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + random.next(2)) >>> 2) << 3);
                                code = CrossHash.hash64(coordinates) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 23:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + random.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + random.next(2)) >>> 2) << 3);
                                code = sipA.hash64(coordinates) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        break;
                    case 24:
                        for (int x = 0; x < width; x++) {
                            coordinates[0] = (((x + random.next(2)) >>> 2) << 3);
                            for (int y = 0; y < height; y++) {
                                coordinates[1] = (((y + random.next(2)) >>> 2) << 3);
                                code = CrossHash.Lightning.hash64(coordinates) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 25:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + random.next(2)) >>> 2) << 9) | ((y + random.next(2)) >>> 2));
                                code = CrossHash.hash64(coordinate) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 26:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + random.next(2)) >>> 2) << 9) | ((y + random.next(2)) >>> 2));
                                code = sipA.hash64(coordinate) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                    case 27:
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                coordinate[0] = ((((x + random.next(2)) >>> 2) << 9) | ((y + random.next(2)) >>> 2));
                                code = CrossHash.Lightning.hash64(coordinate) & 1792L; code = 0xFF00L * ((code & 256L) >>> 8) | 0xFF0000L * ((code & 512L) >> 9) | 0xFF000000L * ((code & 1024L) >> 10) | 255L;
                                display.put(x, y, colorFactory.get(code));
                            }
                        }
                        //overlay.put(4, 4, String.valueOf(random.next(2)), SColor.MIDORI);
                        break;
                }
            }
        }

    }
    @Override
    public void render () {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(bgColor.r / 255.0f, bgColor.g / 255.0f, bgColor.b / 255.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // not sure if this is always needed...
        Gdx.gl.glEnable(GL20.GL_BLEND);
        view.apply(true);
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        // if the user clicked, we have a list of moves to perform.

        // if we are waiting for the player's input and get input, process it.
        if(input.hasNext()) {
            input.next();
        }

        // stage has its own batch and must be explicitly told to draw(). this also causes it to act().
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        this.width = width;
        this.height = height;
        view.update(width, height, true);
        //display = new SquidPanel(this.width, this.height, cellWidth, cellHeight);
        //Gdx.graphics.requestRendering();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Test: Hash Visualization";
        config.width = 512;
        config.height = 512;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new HashVisualizer(), config);
    }
}
