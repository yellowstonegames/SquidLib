package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
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
 * usage in {@link SparseLayers}.
 * <br>
 * Created by Tommy Ettinger on 4/6/2016.
 */
public class SparseLightingDemo2 extends ApplicationAdapter{
    public static final int gridWidth = 80, gridHeight = 40, cellWidth = 11, cellHeight = 20;
//★
    private SparseLayers layers;
    private char[][] map, displayedMap;
    private Color[][] fgColors, bgColors;
    private StatefulRNG rng;
    private Stage stage;
    private FilterBatch batch;
    private ArrayList<Color> colors, mColors;
    private int colorIndex;
    private Coord[] points;
    private int[] offsets;
    private TextCellFactory.Glyph[] markers;
    private double[][] resMap;
    private float ctr;
    private LightingHandler lighting;
//    private final double[][] tempLit = new double[gridWidth][gridHeight];
//    private final float[][][] colorful = SColor.blankColoredLighting(gridWidth, gridHeight),
//            tempColorful = new float[2][gridWidth][gridHeight];
    @Override
    public void create() {
        super.create();
        rng = new StatefulRNG(0x9876543210L);

        layers = new SparseLayers(gridWidth, gridHeight, cellWidth, cellHeight,
                //DefaultResources.getStretchableCodeFont());
                DefaultResources.getCrispLeanFont());
        //new TextCellFactory().fontDistanceField("SourceCodePro-Medium-distance.fnt", "SourceCodePro-Medium-distance.png"));
                //DefaultResources.getStretchableFont());
        //layers.setTextSize(cellWidth + 1, cellHeight + 2);
        //colors = DefaultResources.getSCC().rainbow(0.2f, 1.0f, 144);
        colors = DefaultResources.getSCC().rainbow(0.85f, 1.0f, 512);
        mColors = DefaultResources.getSCC().loopingGradient(SColor.BLACK, SColor.WHITE, 523);
        //colors.addAll(DefaultResources.getSCC().zigzagGradient(Color.MAGENTA, Color.RED, 200));
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
        fgColors = MapUtility.generateDefaultColors(map);
        bgColors = MapUtility.generateDefaultBGColors(map);
        resMap = DungeonUtility.generateResistances(map);
        lighting = new LightingHandler(resMap, SColor.FLOAT_BLACK, Radius.CIRCLE, Double.POSITIVE_INFINITY);
        GreasedRegion packed = new GreasedRegion(gen.getBareDungeon(), '.');
        points = packed.randomScatter(rng, 7, 32).asCoords();
        offsets = new int[points.length];
        markers = new TextCellFactory.Glyph[points.length];
        Coord pt;
        for(int c = 0; c < points.length; c++)
        {
            offsets[c] = (Integer.reverse(c + 1) >>> 23); // similar to VanDerCorputQRNG.determine2()
            pt = points[c];
            lighting.addLight(pt, new Radiance(rng.between(5, 11), colors.get((colorIndex + offsets[c]) & 511).toFloatBits(), 0f, rng.nextFloat()+0.1f));
//            FOV.reuseFOV(resMap, tempLit, pt.x, pt.y, 8.5);
//            SColor.colorLightingInto(tempColorful, tempLit, colors.get((colorIndex + offsets[c]) & 511).toFloatBits());
//            SColor.mixColoredLighting(colorful, tempColorful);
            markers[c] = layers.glyph('★', SColor.FLOAT_WHITE, pt.x, pt.y);
        }

        lighting.updateAll();
        batch = new FilterBatch(16383);
        stage = new Stage(new StretchViewport(gridWidth * cellWidth, gridHeight * cellHeight), batch);
        stage.addActor(layers);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stage.getViewport().update(width, height, false);
    }

    private final Direction[] dirs = new Direction[4];

    @Override
    public void render() {
        super.render();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        ctr += Gdx.graphics.getDeltaTime();
        if(ctr > 1.5) {
            ctr -= 1.5;
            Coord alter;
            for (int i = 0; i < points.length; i++) {
                Coord pt = points[i];
                rng.shuffle(Direction.CARDINALS, dirs);
                for (Direction d : dirs) {
                    alter = pt.translate(d);
                    if (map[alter.x][alter.y] == '.') {
                        lighting.moveLight(pt, alter);
                        pt = alter;
                        points[i] = pt;
                        layers.slide(markers[i], layers.gridX(markers[i].getX()), layers.gridY(markers[i].getY()), alter.x, alter.y, 0.25f, null);
                        break;
                    }
                }
            }
        }
        lighting.updateAll();
        layers.put(displayedMap, fgColors, bgColors);
        lighting.draw(layers);
        //layers.setLightingColor(colors.get(colorIndex = (colorIndex + 1) % colors.size()));
//        for (int x = 0; x < gridWidth; x++) {
//            for (int y = 0; y < gridHeight; y++) {
//                layers.put(x, y, displayedMap[x][y], fgColors[x][y],
//                        bgColors[x][y], colorful[0][x][y], colorful[1][x][y]);
//            }
//        }
        stage.getViewport().apply(false);
        stage.draw();
        stage.act();
        int aeLen = layers.glyphs.size();
        batch.begin();
        for (int i = 0; i < aeLen; i++) {
            layers.glyphs.get(i).draw(batch, 1f);
        }
        batch.end();
    }

    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Test: Lighting");
        config.useVsync(false);
        config.setWindowedMode(gridWidth * cellWidth, gridHeight * cellHeight);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new SparseLightingDemo2(), config);
    }
}
