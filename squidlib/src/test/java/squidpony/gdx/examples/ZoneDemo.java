package squidpony.gdx.examples;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import squidpony.GwtCompatibility;
import squidpony.panel.IColoredString;
import squidpony.squidai.ZOI;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.AnimatedEntity;
import squidpony.squidgrid.gui.gdx.DefaultResources;
import squidpony.squidgrid.gui.gdx.GDXMarkup;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidColorCenter;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.gui.gdx.SquidLayers;
import squidpony.squidgrid.gui.gdx.TextCellFactory;
import squidpony.squidgrid.gui.gdx.TextPanel;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.SerpentMapGenerator;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.RNG;

public class ZoneDemo extends ApplicationAdapter {
    SpriteBatch batch;

    private RNG rng;
    private SquidLayers display;
    private DungeonGenerator dungeonGen;
    private char[][] bareDungeon, lineDungeon;
    private int[][] lights;
    private Color[][] bgColors;
    private Color[] influenceColors;
    private ZOI zoi;
    private short[][] packedInfluences;
    private int width, height;
    private int cellWidth, cellHeight;
    private Coord[] centers, shiftedCenters;
    private AnimatedEntity[] centerEntities;
    private SquidInput input;
    private static final Color bgColor = SColor.DARK_SLATE_GRAY, textColor = SColor.SLATE_GRAY;
    private Stage stage;
    private SquidColorCenter colorCenter;
    @Override
    public void create () {
        batch = new SpriteBatch();
        width = 80;
        height = 50;
        cellWidth = 16;
        cellHeight = 16;
        TextCellFactory tcf = DefaultResources.getStretchableFont().addSwap('.', ' ');
        display = new SquidLayers(width, height, cellWidth, cellHeight, tcf);
        display.setAnimationDuration(0.05f);
        display.setTextSize(cellWidth, cellHeight + 1);
        stage = new Stage(new ScreenViewport(), batch);

        rng = new RNG(0x7ECCBABBL);

        dungeonGen = new DungeonGenerator(width, height, rng);
//        dungeonGen.addWater(10);
        //dungeonGen.addDoors(15, true);
        SerpentMapGenerator serpent = new SerpentMapGenerator(width, height, rng, 0.4);
        serpent.putBoxRoomCarvers(2);
        serpent.putWalledBoxRoomCarvers(2);
        serpent.putWalledRoundRoomCarvers(2);
        serpent.putCaveCarvers(4);
        bareDungeon = dungeonGen.generate(serpent.generate());
        //bareDungeon = DungeonUtility.closeDoors(bareDungeon);

        //lineDungeon = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(bareDungeon));
        lineDungeon = DungeonUtility.hashesToLines(bareDungeon);

        //ArrayList<Coord> temp = PoissonDisk.sampleMap(bareDungeon, 8.0f, rng, '#', '+', '/');

        //centers = temp.toArray(new Coord[temp.size()]);
        //shiftedCenters = temp.toArray(new Coord[temp.size()]);

        centers = CoordPacker.apartPacked(CoordPacker.pack(bareDungeon, '.'), 8);
        shiftedCenters = GwtCompatibility.cloneCoords(centers);
        colorCenter = DefaultResources.getSCC();
        influenceColors = new Color[centers.length];
        centerEntities = new AnimatedEntity[centers.length];
        for (int i = 0; i < centers.length; i++) {
            float hue = i * 1.0f / centers.length, sat = rng.nextFloat() * 0.3f + 0.7f,
                    val = rng.nextFloat() * 0.4f + 0.6f;
            influenceColors[i] = colorCenter.getHSV(hue, sat, val);

            centerEntities[i] = display.animateActor(centers[i].x, centers[i].y, '@',
                    colorCenter.getHSV(hue, sat - 0.3f, val - 0.4f), false); //, true);
        }
        zoi = new ZOI(centers, bareDungeon, Radius.DIAMOND);
        packedInfluences = zoi.calculate();

        bgColors = new Color[width][height];
        recolorZones();
        lights = DungeonUtility.generateLightnessModifiers(bareDungeon);

        // just quit if we get a Q.
        input = new SquidInput(new SquidInput.KeyHandler() {

        	private TextPanel<Color> current;

            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key)
                {
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE:
                    {
                        Gdx.app.exit();
					    break;
                    }
				case 'M': /* Convenient when switching US/French layouts as I do (smelC) */
				case '?': {
					if (current == null) {
						current = new TextPanel<Color>(new GDXMarkup(), DefaultResources.getLargeFont());
						current.backgroundColor = colorCenter.get(30, 30, 30);
						final List<IColoredString<Color>> text = new ArrayList<>();
						IColoredString<Color> buf = IColoredString.Impl.create();
						buf.append("SquidLib ", colorCenter.get(255, 0, 0));
						buf.append("is brought to you by Tommy Ettinger, Eben Howard, smelC, and others",
								null);
						text.add(buf);
						/* Jump line */
						text.add(IColoredString.Impl.<Color> create());
						buf = IColoredString.Impl.create();
						buf.append("If you wanna contribute, visit ", null);
						buf.append("https://github.com/SquidPony/SquidLib", colorCenter.get(29, 0, 253));
						text.add(buf);
						final float screenWidth = Gdx.graphics.getWidth();
						final float screenHeight = Gdx.graphics.getHeight();
						/*
						 * To have scrollbars, we would need to provide textures
						 */
						final float panelWidth = screenWidth / 2;
						final float panelHeight = screenHeight / 2;
						final ScrollPane sp = current.getScrollPane();
						current.init(panelWidth, panelHeight, text);
						final float x = (screenWidth - panelWidth) / 2;
						final float y = (screenHeight - panelHeight) / 2;
						sp.setPosition(x, y);
						stage.setKeyboardFocus(sp);
						stage.setScrollFocus(sp);
						stage.addActor(sp);
					} else {
						current.dispose();
						stage.getActors().removeValue(current.getScrollPane(), true);
						stage.setKeyboardFocus(null);
						stage.setScrollFocus(null);
						current = null;
					}
					break;
				}
                }
            }
        });
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        // and then add display, our one visual component, to the list of things that act in Stage.
        display.setPosition(0, 0);
        stage.addActor(display);
        Gdx.input.setInputProcessor(input);
    }

    public void move() {
        for (int i = 0; i < centers.length; i++) {
            AnimatedEntity ae = centerEntities[i];
            Direction[] dirs = new Direction[4];
            rng.shuffle(Direction.CARDINALS, dirs);
            for (int j = 0; j < dirs.length; j++) {
                int newX = ae.gridX + dirs[j].deltaX, newY = ae.gridY + dirs[j].deltaY;
                if (bareDungeon[newX][newY] != '#' &&
                        Radius.DIAMOND.radius(centers[i].x, centers[i].y,
                                newX, newY) <= 4.0) {
                    display.slide(ae, newX, newY);
                    shiftedCenters[i] = Coord.get(newX, newY);
                    break;
                }
            }
        }
        //recolorZones();
        //phase = Phase.MOVE_ANIM;

    }
    public void recolorZones()
    {
        zoi = new ZOI(shiftedCenters, bareDungeon, Radius.DIAMOND);
        packedInfluences = zoi.calculate();
        Coord c;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                c = Coord.get(x, y);
                int[] inf = zoi.nearestInfluences(packedInfluences, c);
                if(inf.length == 0) {
                    bgColors[x][y] = bgColor;
                }
                else if(inf.length == 1)
                {
                    bgColors[x][y] = influenceColors[inf[0]];
                }
                else
                {
                    float hue = colorCenter.getHue(influenceColors[inf[0]]),
                            sat = colorCenter.getSaturation(influenceColors[inf[0]]),
                            val = colorCenter.getValue(influenceColors[inf[0]]),
                            tempHue;
                    if(hue < 0.5) hue += 1f;
                    for (int i = 1; i < inf.length; i++) {
                        tempHue = colorCenter.getHue(influenceColors[inf[i]]);
                        if(tempHue < 0.5) tempHue += 1f;
                        hue += tempHue;
                        sat += colorCenter.getSaturation(influenceColors[inf[i]]);
                        val += colorCenter.getValue(influenceColors[inf[i]]);
                    }
                    bgColors[x][y] = colorCenter.getHSV((hue / inf.length) % 1.0f, sat / inf.length, val / inf.length);
                }
            }
        }
    }

    private void postMove() {
        recolorZones();
    }
    public void putMap()
    {
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                display.put(i, j, lineDungeon[i][j], textColor, bgColors[i][j], lights[i][j]);
                //display.put(i * 2, j, lineDungeon[i * 2][j], textColor, bgColors[i][j], lights[i][j]);
                //display.put(i * 2 + 1, j, lineDungeon[i * 2 + 1][j], textColor, bgColors[i][j], lights[i][j]);
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

        stage.act();

        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        // if we are waiting for the player's input and get input, process it.
        if(input.hasNext()) {
            input.next();
        }

        if(!display.hasActiveAnimations()) {
            move();
            postMove();
            //secondsWithoutAnimation += Gdx.graphics.getDeltaTime();
            //if (secondsWithoutAnimation >= 0.05f) {
            //}
        }
/*
            secondsWithoutAnimation += Gdx.graphics.getDeltaTime();
            if (secondsWithoutAnimation >= 0.01f) {
                secondsWithoutAnimation = 0f;
                switch (phase) {
                    case WAIT_ANIM: {
                        move();
                    }
                    break;
                    case MOVE_ANIM: {
                        postMove();
                        move();
                    }
                }
            }
        }*/

        // if we do have an animation running, then how many frames have passed with no animation needs resetting
        /*
        else
        {
            secondsWithoutAnimation = 0;
        }
        */

        // stage has its own batch and must be explicitly told to draw(). this also causes it to act().
        stage.draw();
        // display does not draw all AnimatedEntities by default.
        batch.begin();
        for(AnimatedEntity mon : display.getAnimatedEntities(2)) {
            display.drawActor(batch, 1.0f, mon);
        }
        // batch must end if it began.
        batch.end();


    }
}

