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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidColorCenter;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.gui.gdx.SquidPanel;
import squidpony.squidgrid.mapping.MetsaMapFactory;
import squidpony.squidmath.Coord;

import java.util.List;

public class MetsaWorldMapDemo extends ApplicationAdapter {
    private MetsaMapFactory mapFactory;

    private SpriteBatch batch;
    private SquidColorCenter colorFactory;
    private SquidPanel display;
    private int width, height;
    private int cellWidth, cellHeight;
    private SquidInput input;
    private static final SColor bgColor = SColor.DARK_SLATE_GRAY;
    private Stage stage;
    private Viewport view;

    private final SColor CITY_COLOR = new SColor(0xFF9944);

    //COLORORDER
/*
     0 = deepsea
     1 = beach
     2 = low
     3 = high
     4 = mountain
     5 = snowcap
     6 = lowsea
     */
    private final SColor[] colors = new SColor[]{SColor.DENIM, SColor.PEACH, SColor.PALE_YOUNG_GREEN_ONION,
            SColor.FOREST_GREEN, SColor.SLATE_GRAY, SColor.ALICE_BLUE, SColor.AZUL};
    private final SColor[] polarcolors = colors;
    //            new SColor[]{SColor.DARK_SLATE_GRAY, SColor.SCHOOL_BUS_YELLOW, SColor.YELLOW_GREEN,
//        SColor.GREEN_BAMBOO, SColorFactory.lighter(SColor.LIGHT_BLUE_SILK), SColor.ALICE_BLUE, SColor.AZUL};
    private final SColor[] desertcolors = colors;
//            new SColor[]{SColor.DARK_SLATE_GRAY, SColor.SCHOOL_BUS_YELLOW, SColor.YELLOW_GREEN,
//        SColor.GREEN_BAMBOO, SColorFactory.lighter(SColor.LIGHT_BLUE_SILK), SColor.ALICE_BLUE, SColor.AZUL};

    private double highn = 0;
    private int[][] biomeMap;
    private double[][] map;
    private List<Coord> cities;
    private void remake()
    {
        mapFactory = new MetsaMapFactory(width, height);
        map = mapFactory.getHeightMap();
        biomeMap = mapFactory.makeBiomeMap();
        mapFactory.makeWeightedMap();
        highn = mapFactory.getMaxPeak();
        cities = mapFactory.getCities();

    }
    @Override
    public void create () {
        batch = new SpriteBatch();
        width = 240;
        height = 120;
        cellWidth = 5;
        cellHeight = 5;
        display = new SquidPanel(width, height, cellWidth, cellHeight);
        colorFactory = new SquidColorCenter();
        remake();
        view = new ScreenViewport();
        stage = new Stage(view, batch);

        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key)
                {
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE:
                    {
                        Gdx.app.exit();
                    }
                    break;
                    default:
                        remake();
                        break;
                }
            }
        });
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(input);
        // and then add display, our one visual component, to the list of things that act in Stage.
        display.setPosition(0, 0);
        stage.addActor(display);

        Gdx.graphics.setContinuousRendering(false);
        Gdx.graphics.requestRendering();
    }
    public void putMap()
    {
        display.clear();
        double n;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                n = map[x][y];
                SColor[] curcolor = colors;
                if (biomeMap[x][y] == 1 || biomeMap[x][y] == 3) {
                    curcolor = polarcolors;
                }
                if (biomeMap[x][y] == 2) {
                    curcolor = desertcolors;
                }
                Color color = curcolor[6];
                if (n > MetsaMapFactory.SEA_LEVEL) {
                    color = curcolor[1];
                }
                if (n > MetsaMapFactory.BEACH_LEVEL) {
                    color = curcolor[2];
                }
                if (n > MetsaMapFactory.PLAINS_LEVEL) {
                    color = curcolor[3];
                }
                if (n > MetsaMapFactory.MOUNTAIN_LEVEL) {
                    color = curcolor[4];
                }
                if (n > MetsaMapFactory.SNOW_LEVEL) {
                    color = curcolor[5];
                }

                //Polar ice
                if (n < MetsaMapFactory.DEEP_SEA_LEVEL) {
                    if (biomeMap[x][y] == 3) {
                        color = polarcolors[0];
                    } else {
                        color = colors[0];
                    }
                }

//                use alpha to blend
                if (n > 0) {
                    color = colorFactory.lerp(color, SColor.ALICE_BLUE, (float)Math.pow(n / highn, 2) / 2f);//high stuff gets lighter
                    color = colorFactory.lerp(color, SColor.DARK_BLUE_DYE, (float)(0.2 - n * n));//low stuff gets darker

                    int shadow = mapFactory.getShadow(x, y, map);
                    if (n > MetsaMapFactory.SNOW_LEVEL && (biomeMap[x][y] == 1 || biomeMap[x][y] == 3)) {//SNOWAREA VOLCANO CASE
                        if (shadow == -1) {//shadow side INVERSE
//                            color = SColorFactory.blend(color, new SColor(0, 0, 90), 0.2);
                            color = colorFactory.lerp(color, SColor.DENIM, (float)(0.2 * n / 2));
                        }
                        if (shadow == 1) {//sun side INVERSE
//                            color = SColorFactory.blend(color, new SColor(255, 255, 0), 0.1);
                            color = colorFactory.lerp(color, SColor.BRASS, (float)(0.1 * n / 2));
                        }
                    } else {
                        if (shadow == 1) { //shadow side
//                            color = SColorFactory.blend(color, new SColor(0, 0, 90), 0.2);
                            color = colorFactory.lerp(color, SColor.ONANDO, (float)(0.2 * n / 2));
                        }
                        if (shadow == -1) {//sun side
//                            color = SColorFactory.blend(color, new SColor(220, 220, 100), 0.2);
                            color = colorFactory.lerp(color, SColor.YELLOW, (float)(0.2 * n / 2));
                        }
                    }
                }
                display.put(x, y, color);
            }
        }

        for (Coord city : cities) {
            display.put(city.x, city.y, '@', CITY_COLOR);
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
        this.width = width / cellWidth;
        this.height = height / cellHeight;
        view.update(width, height, true);
        mapFactory.regenerateHeightMap(this.width, this.height);
        map = mapFactory.getHeightMap();
        biomeMap = mapFactory.makeBiomeMap();
        mapFactory.makeWeightedMap();
        highn = mapFactory.getMaxPeak();
        cities = mapFactory.getCities();
        display = new SquidPanel(this.width, this.height, cellWidth, cellHeight);
        display.setPosition(0,0);
        stage.clear();
        stage.addActor(display);
        Gdx.graphics.requestRendering();
	}
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib GDX World Map Demo";
        config.width = 1200;
        config.height = 600;
        config.addIcon("Tentacle-16.png", Files.FileType.Classpath);
        config.addIcon("Tentacle-32.png", Files.FileType.Classpath);
        config.addIcon("Tentacle-128.png", Files.FileType.Classpath);
        new LwjglApplication(new MetsaWorldMapDemo(), config);
    }

}
