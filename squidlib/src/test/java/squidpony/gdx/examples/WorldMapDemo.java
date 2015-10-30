package squidpony.gdx.examples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.MetsaMapFactory;
import squidpony.squidmath.Coord;

import java.util.List;

public class WorldMapDemo extends ApplicationAdapter {
    private MetsaMapFactory mapFactory;

    private SpriteBatch batch;
    private SColorFactory colorFactory;
    private SquidLayers display;
    private int width, height;
    private int cellWidth, cellHeight;
    private SquidInput input;
    private static final SColor bgColor = SColor.DARK_SLATE_GRAY;
    private Stage stage;
    //HEIGHT LIMITS

    private double SEALEVEL = 0,
            BEACHLEVEL = 0.05,
            PLAINSLEVEL = 0.3,
            MOUNTAINLEVEL = 0.45,
            SNOWLEVEL = 0.63,
            DEEPSEA = -0.1;

    private final SColor CITY_COLOR = new SColor(0x444);

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
    @Override
    public void create () {
        batch = new SpriteBatch();
        width = 1000;
        height = 600;
        cellWidth = 1;
        cellHeight = 1;
        display = new SquidLayers(width, height, cellWidth, cellHeight);
        colorFactory = new SColorFactory();
        mapFactory = new MetsaMapFactory(width, height);
        map = mapFactory.makeHeightMap();
        biomeMap = mapFactory.makeBiomeMap(map);
        mapFactory.makeWeightedMap(map);
        highn = mapFactory.getMaxPeak();
        cities = mapFactory.getCities();

        stage = new Stage(new ScreenViewport(), batch);

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
                SColor color = curcolor[6];
                if (n > SEALEVEL) {
                    color = curcolor[1];
                }
                if (n > BEACHLEVEL) {
                    color = curcolor[2];
                }
                if (n > PLAINSLEVEL) {
                    color = curcolor[3];
                }
                if (n > MOUNTAINLEVEL) {
                    color = curcolor[4];
                }
                if (n > SNOWLEVEL) {
                    color = curcolor[5];
                }

                //Polar ice
                if (n < DEEPSEA) {
                    if (biomeMap[x][y] == 3) {
                        color = polarcolors[0];
                    } else {
                        color = colors[0];
                    }
                }

//                use alpha to blend
                if (n > 0) {
                    color = colorFactory.blend(color, SColor.ALICE_BLUE, Math.pow(n / highn, 2) / 2);//high stuff gets lighter
                    color = colorFactory.blend(color, SColor.DARK_BLUE_DYE, 0.2 - n * n);//low stuff gets darker

                    int shadow = mapFactory.getShadow(x, y, map);
                    if (n > SNOWLEVEL && (biomeMap[x][y] == 1 || biomeMap[x][y] == 3)) {//SNOWAREA VOLCANO CASE
                        if (shadow == -1) {//shadow side INVERSE
//                            color = SColorFactory.blend(color, new SColor(0, 0, 90), 0.2);
                            color = colorFactory.blend(color, SColor.DENIM, 0.2 * n / 2);
                        }
                        if (shadow == 1) {//sun side INVERSE
//                            color = SColorFactory.blend(color, new SColor(255, 255, 0), 0.1);
                            color = colorFactory.blend(color, SColor.BRASS, 0.1 * n / 2);
                        }
                    } else {
                        if (shadow == 1) { //shadow side
//                            color = SColorFactory.blend(color, new SColor(0, 0, 90), 0.2);
                            color = colorFactory.blend(color, SColor.ONANDO, 0.2 * n / 2);
                        }
                        if (shadow == -1) {//sun side
//                            color = SColorFactory.blend(color, new SColor(220, 220, 100), 0.2);
                            color = colorFactory.blend(color, SColor.YELLOW, 0.2 * n / 2);
                        }
                    }
                }
                display.put(x, y, ' ', color, color);
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
	}
}
