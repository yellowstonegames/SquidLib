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
import com.badlogic.gdx.utils.viewport.StretchViewport;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.FlowingCaveGenerator;
import squidpony.squidgrid.mapping.SectionDungeonGenerator;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;

/**
 * Tests colored lighting using various methods in {@link FOV}, {@link SColor}, and {@link MapUtility}, along with their
 * usage in {@link SquidLayers}.
 * <br>
 * Created by Tommy Ettinger on 4/6/2016.
 */
public class LightingTest extends ApplicationAdapter{
    public static final int gridWidth = 80, gridHeight = 40, cellWidth = 11, cellHeight = 20;

    private SquidLayers layers;
    private char[][] map, displayedMap;
    private float[][] fgColors, bgColors;
    private StatefulRNG rng;
    private Stage stage;
    private SpriteBatch batch;
    private ArrayList<Color> colors, mColors;
    private int colorIndex = 0;
    private Coord[] points;
    private int[] offsets;
    private AnimatedEntity[] markers;
    private double[][] resMap;
    private float ctr = 0;
    private final double[][] tempLit = new double[gridWidth][gridHeight];
    private final float[][][] colorful = SColor.blankColoredLighting(gridWidth, gridHeight),
            tempColorful = new float[2][gridWidth][gridHeight];
    @Override
    public void create() {
        super.create();
        rng = new StatefulRNG(0x9876543210L);

        layers = new SquidLayers(gridWidth, gridHeight, cellWidth, cellHeight,
                //DefaultResources.getStretchableCodeFont());
                DefaultResources.getCrispLeanFont());
        //new TextCellFactory().fontDistanceField("SourceCodePro-Medium-distance.fnt", "SourceCodePro-Medium-distance.png"));
                //DefaultResources.getStretchableFont());
        layers.setTextSize(cellWidth + 1, cellHeight + 2);
        //colors = DefaultResources.getSCC().rainbow(0.2f, 1.0f, 144);
        colors = DefaultResources.getSCC().rainbow(0.85f, 1.0f, 512);
        mColors = DefaultResources.getSCC().loopingGradient(SColor.BLACK, SColor.WHITE, 523);
        //colors.addAll(DefaultResources.getSCC().zigzagGradient(Color.MAGENTA, Color.RED, 200));
        layers.setLightingColor(SColor.WHITE);
        //PacMazeGenerator maze = new PacMazeGenerator(gridWidth, gridHeight, rng);
        //OrganicMapGenerator org = new OrganicMapGenerator(gridWidth, gridHeight, rng);
//        SerpentMapGenerator org = new SerpentMapGenerator(gridWidth, gridHeight, rng, 0.1);
//        org.putBoxRoomCarvers(3);
//        org.putRoundRoomCarvers(1);
//        org.putCaveCarvers(1);
        SectionDungeonGenerator gen = new SectionDungeonGenerator(gridWidth, gridHeight, rng);
//        gen.addMaze(10);
//        gen.addBoulders(0, 8);
        FlowingCaveGenerator org = new FlowingCaveGenerator(gridWidth, gridHeight, TilesetType.ROUND_ROOMS_DIAGONAL_CORRIDORS, rng);
        map = org.generate();
        map = gen.generate(map, org.getEnvironment());
        displayedMap = DungeonUtility.hashesToLines(map, true);
        SColor.LIMITED_PALETTE[0] = SColor.DB_GRAPHITE;
        SColor.LIMITED_PALETTE[2] = SColor.DB_CAPPUCCINO;
        fgColors = MapUtility.generateDefaultColorsFloat(map);
        bgColors = MapUtility.generateDefaultBGColorsFloat(map);
        resMap = DungeonUtility.generateResistances(map);
        GreasedRegion packed = new GreasedRegion(gen.getBareDungeon(), '.');
        points = packed.randomScatter(rng, 7, 32).asCoords();
        offsets = new int[points.length];
        markers = new AnimatedEntity[points.length];
        Coord pt;
        for(int c = 0; c < points.length; c++)
        {
            offsets[c] = (Integer.reverse(c + 1) >>> 23); // similar to VanDerCorputQRNG.determine2()
            pt = points[c];
            FOV.reuseFOV(resMap, tempLit, pt.x, pt.y, 8.5);
            SColor.colorLightingInto(tempColorful, tempLit, colors.get((colorIndex + offsets[c]) & 511).toFloatBits());
            SColor.mixColoredLighting(colorful, tempColorful);
            markers[c] = layers.directionMarker(pt.x, pt.y, mColors, 4f, 2, false);
        }

        batch = new SpriteBatch();
        stage = new Stage(new StretchViewport(gridWidth * cellWidth, gridHeight * cellHeight), batch);
        stage.addActor(layers);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height, false);
    }

    @Override
    public void render() {
        super.render();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        ctr += Gdx.graphics.getDeltaTime();
        if(ctr > 0.6) {
            ctr -= 0.6;
            SColor.eraseColoredLighting(colorful);
            Direction[] dirs = new Direction[4];
            Coord alter;
            for (int i = 0; i < points.length; i++) {
                Coord pt = points[i];
                rng.shuffle(Direction.CARDINALS, dirs);
                for (Direction d : dirs) {
                    alter = pt.translate(d);
                    if (map[alter.x][alter.y] == '.') {
                        pt = alter;
                        points[i] = pt;
                        markers[i].setDirection(d);
                        layers.slide(markers[i], alter.x, alter.y, 2, 0.25f);
                        break;
                    }
                }
                FOV.reuseFOV(resMap, tempLit, pt.x, pt.y, 8.5);
                SColor.colorLightingInto(tempColorful, tempLit, colors.get((colorIndex + offsets[i]) & 511).toFloatBits());
                SColor.mixColoredLighting(colorful, tempColorful);
            }
        }
        //layers.setLightingColor(colors.get(colorIndex = (colorIndex + 1) % colors.size()));
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                layers.put(x, y, displayedMap[x][y], fgColors[x][y],
                        bgColors[x][y], colorful[0][x][y], colorful[1][x][y]);
            }
        }
        stage.getViewport().apply(false);
        stage.draw();
        stage.act();
        int aeLen = layers.getForegroundLayer().animatedEntities.size();
        batch.begin();
        for (int i = 0; i < aeLen; i++) {
            layers.drawActor(batch, 1f, layers.getForegroundLayer().animatedEntities.getAt(i), 2);
        }
        batch.end();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Test: Lighting";
        config.width = gridWidth * cellWidth;
        config.height = gridHeight * cellHeight;
        config.foregroundFPS = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new LightingTest(), config);
    }
}
