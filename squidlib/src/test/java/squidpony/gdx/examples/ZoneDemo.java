package squidpony.gdx.examples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import squidpony.ColoredStringList;
import squidpony.squidai.ZOI;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.SerpentMapGenerator;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
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
    private float[] influenceH, influenceS, influenceV;
    private ZOI zoi;
    private short[][] packedInfluences;
    private int width, height, screenWidth, screenHeight;
    private int cellWidth, cellHeight;
    private Coord[] centers, shiftedCenters;
    private AnimatedEntity[] centerEntities;
    private SquidInput input;
    private static final Color bgColor = SColor.DARK_SLATE_GRAY, textColor = SColor.SLATE_GRAY;
    private Stage stage;
    private SquidColorCenter colorCenter;
    private TextPanel<Color> current;
    @Override
    public void create () {
        batch = new SpriteBatch();
        width = 90;
        height = 40;

        cellWidth = 18;
        cellHeight = 18;
        TextCellFactory tcf = DefaultResources.getStretchableHeavySquareFont().addSwap('.', ' ');
        display = new SquidLayers(width, height, cellWidth, cellHeight, tcf);
        //display.setTextSize(cellWidth, cellHeight);
        screenWidth = width * cellWidth;
        screenHeight = height * cellHeight;
        display.setAnimationDuration(0.2f);
        display.setTextSize(cellWidth, cellHeight);
        stage = new Stage(new StretchViewport(screenWidth, screenHeight), batch);

        rng = new RNG(0xBABABADAAAAAAAL);

        dungeonGen = new DungeonGenerator(width, height, rng);
//        dungeonGen.addWater(10);
        //dungeonGen.addDoors(15, true);
        SerpentMapGenerator serpent = new SerpentMapGenerator(width, height, rng, 0.2);
        serpent.putBoxRoomCarvers(1);
        serpent.putWalledBoxRoomCarvers(3);
        serpent.putWalledRoundRoomCarvers(4);
        serpent.putCaveCarvers(2);
        /*
        MixedGenerator mixed = new MixedGenerator(width, height, rng);
        mixed.putWalledBoxRoomCarvers(9);
        mixed.putWalledRoundRoomCarvers(6);
        mixed.putCaveCarvers(2);
        */
        //OrganicMapGenerator organic = new OrganicMapGenerator(0.55, 0.65, width, height, rng);
        bareDungeon = dungeonGen.generate(serpent.generate());

        //bareDungeon = DungeonUtility.closeDoors(bareDungeon);

        //lineDungeon = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(bareDungeon));
        lineDungeon = DungeonUtility.hashesToLines(bareDungeon);

        //ArrayList<Coord> temp = PoissonDisk.sampleMap(bareDungeon, 8.0f, rng, '#', '+', '/');

        //centers = temp.toArray(new Coord[temp.size()]);
        //shiftedCenters = temp.toArray(new Coord[temp.size()]);

        GreasedRegion g = new GreasedRegion(bareDungeon, '.').randomScatter(rng, 7);
        centers = g.asCoords();
        shiftedCenters = g.asCoords();
        colorCenter = DefaultResources.getSCC();
        influenceH = new float[centers.length];
        influenceS = new float[centers.length];
        influenceV = new float[centers.length];
        influenceColors = new Color[centers.length];
        centerEntities = new AnimatedEntity[centers.length];
        for (int i = 0; i < centers.length; i++) {
            float hue = i * 1.0f / centers.length, sat = rng.nextFloat() * 0.2f + 0.8f,
                    val = rng.nextFloat() * 0.3f + 0.7f;
            influenceH[i] = hue;
            influenceS[i] = sat;
            influenceV[i] = val;
            influenceColors[i] = colorCenter.getHSV(hue, sat, val);
            centerEntities[i] = display.animateActor(centers[i].x, centers[i].y, '@',
                    colorCenter.getHSV(hue, sat - 0.3f, val - 0.4f), false); //, true);
        }
        zoi = new ZOI(centers, bareDungeon, Radius.DIAMOND);
        packedInfluences = zoi.calculate();

        bgColors = new Color[width][height];
        recolorZones();
        lights = MapUtility.generateLightnessModifiers(bareDungeon);

        // just quit if we get a Q.
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
					    break;
                    }
				case 'M': /* Convenient when switching US/French layouts as I do (smelC) */
				case '?': {
                    if (current == null)
                        buildCurrentTextPanel(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                    else
                        disposeCurrentTextPanel();
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
        int inf0;
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
                    inf0 = inf[0];
                    float hue = influenceH[inf0] + 1f,
                            sat = influenceS[inf0],
                            val = influenceV[inf0];
                    //if(hue < 0.5) hue += 1f;
                    for (int i = 1; i < inf.length; i++) {
                        //if(tempHue < 0.5) tempHue += 1f;
                        hue += influenceH[inf[i]] + 1f;
                        sat += influenceS[inf[i]];
                        val += influenceV[inf[i]];
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
        display.putString(2, 0, String.valueOf(Gdx.graphics.getFramesPerSecond()));
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
        }

        // stage has its own batch and must be explicitly told to draw().
        stage.getViewport().apply(true);
        stage.draw();
        // display does not draw all AnimatedEntities by default.
        batch.begin();
        for(AnimatedEntity mon : display.getAnimatedEntities(2)) {
            display.drawActor(batch, 1.0f, mon);
        }
        // batch must end if it began.
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height, true);
    }

    private void buildCurrentTextPanel(int newWidth, int newHeight) {
        current = new TextPanel<Color>(GDXMarkup.instance, //DefaultResources.getLargeFont());
                //new TextCellFactory().fontDistanceField("Gentium-distance.fnt", "Gentium-distance.png")
                //new TextCellFactory().fontDistanceField("Noto-Sans-distance.fnt", "Noto-Sans-distance.png")
                //        .setSmoothingMultiplier(0.4f).height(30).width(7)
                //DefaultResources.getSlabFamily().width(11).height(24));
                DefaultResources.getStretchablePrintFont());
        current.backgroundColor = colorCenter.get(30, 30, 30);
        final ColoredStringList<Color> text = new ColoredStringList<Color>();
        //text.addColoredText("SquidLib ", colorCenter.get(255, 0, 0));
        text.addText(GDXMarkup.instance.colorString("[CW Pale Indigo]SquidLib[] is brought to you by Tommy Ettinger, Eben Howard, smelC, and others"));
        /* Jump a line */
        text.addTextOnNewLine(GDXMarkup.instance.colorString("If you wanna contribute, visit " +
                "[CW Bright Sapphire]https://github.com/SquidPony/SquidLib[]"));
        /* // useful during debugging
        char[] big = new char[50];
        Arrays.fill(big, 'A');
        buf.append(new String(big), Color.RED);
        text.add(buf);
        Arrays.fill(big, 'B');
        text.add(IColoredString.Impl.<Color> create(new String(big), Color.GREEN));
        Arrays.fill(big, 'C');
        text.add(IColoredString.Impl.<Color> create(new String(big), Color.BLUE));
        Arrays.fill(big, 'D');
        text.add(IColoredString.Impl.<Color> create(new String(big), Color.YELLOW));
        */
        /*
    	 * To have scrollbars, we would need to provide textures
    	 */
        final float panelWidth = screenWidth / 2f;
        final float panelHeight = screenHeight / 2f;
        final ScrollPane sp = current.getScrollPane();
        current.init(panelWidth, panelHeight, text);
        final float x = (screenWidth - panelWidth) / 2f;
        final float y = (screenHeight - panelHeight) / 2f;
        sp.setPosition(x, y);
        stage.setKeyboardFocus(sp);
        stage.setScrollFocus(sp);
        stage.addActor(sp);
    }

    private void disposeCurrentTextPanel() {
        current.dispose();
        stage.getActors().removeValue(current.getScrollPane(), true);
        stage.setKeyboardFocus(null);
        stage.setScrollFocus(null);
        current = null;
    }
}