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
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.*;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;

/**
 * Created by Tommy Ettinger on 4/6/2016.
 */
public class SquidLayersTest extends ApplicationAdapter{
    public static final int gridWidth = 80, gridHeight = 30, cellWidth = 20, cellHeight = 20;

    SquidLayers layers;
    char[][] map, displayedMap;
    int[][] indicesFG, indicesBG, lightness;
    FOV fov;
    TextCellFactory tcf;
    StatefulRNG rng;
    Stage stage;
    SpriteBatch batch;
    ArrayList<Color> colors, mColors;
    int colorIndex = 0;
    Coord[] points;
    AnimatedEntity[] markers;
    double[][] resMap;
    float ctr = 0;
    @Override
    public void create() {
        super.create();
        rng = new StatefulRNG(0x9876543210L);

        layers = new SquidLayers(gridWidth, gridHeight, cellWidth, cellHeight,
                //DefaultResources.getStretchableCodeFont());
                DefaultResources.getStretchableDejaVuFont());
        //new TextCellFactory().fontDistanceField("SourceCodePro-Medium-distance.fnt", "SourceCodePro-Medium-distance.png"));
                //DefaultResources.getStretchableFont());
        layers.setTextSize(cellWidth + 1, cellHeight + 4);
        //colors = DefaultResources.getSCC().rainbow(0.2f, 1.0f, 144);
        colors = DefaultResources.getSCC().loopingGradient(SColor.ATOMIC_TANGERINE, SColor.CRIMSON, 100);
        mColors = DefaultResources.getSCC().loopingGradient(SColor.SKY_BLUE, SColor.MAGIC_MINT, 123);
        //colors.addAll(DefaultResources.getSCC().zigzagGradient(Color.MAGENTA, Color.RED, 200));
        layers.setLightingColor(colors.get(colorIndex));
        fov = new FOV(FOV.SHADOW);
        //PacMazeGenerator maze = new PacMazeGenerator(gridWidth, gridHeight, rng);
        //OrganicMapGenerator org = new OrganicMapGenerator(gridWidth, gridHeight, rng);
        SerpentMapGenerator org = new SerpentMapGenerator(gridWidth, gridHeight, rng, 0.1);
        org.putBoxRoomCarvers(3);
        org.putRoundRoomCarvers(1);
        org.putCaveCarvers(1);
        SectionDungeonGenerator gen = new SectionDungeonGenerator(gridWidth, gridHeight, rng);
        gen.addMaze(10);
        gen.addBoulders(0, 8);
        map = org.generate();
        map = gen.generate(map, org.getEnvironment());
        displayedMap = DungeonUtility.hashesToLines(map, true);
        indicesBG = DungeonUtility.generateBGPaletteIndices(map);
        indicesFG = DungeonUtility.generatePaletteIndices(map);
        resMap = DungeonUtility.generateResistances(map);
        GreasedRegion packed = new GreasedRegion(gen.getBareDungeon(), '.');
        points = packed.randomPortion(rng, 10);
        markers = new AnimatedEntity[points.length];
        lightness = new int[gridWidth][gridHeight];
        double[][] lit;
        Coord pt;
        for(int c = 0; c < points.length; c++)
        {
            pt = points[c];
            lit = fov.calculateFOV(resMap, pt.x, pt.y, 11, Radius.CIRCLE);
            for (int x = 0; x < gridWidth; x++) {
                for (int y = 0; y < gridHeight; y++) {
                    if(lit[x][y] > 0.0)
                        lightness[x][y] += (int)(lit[x][y] * 200);
                }
            }
            markers[c] = layers.directionMarker(pt.x, pt.y, mColors, 4f, 2, false);
        }
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                lightness[x][y] -= 40;
            }
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
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        ctr += Gdx.graphics.getDeltaTime();
        if(ctr > 0.3)
            layers.setLightingColor(colors.get(colorIndex = (colorIndex + 1) % colors.size()));
        if(ctr > 0.6) {
            ctr -= 0.6;
            lightness = new int[gridWidth][gridHeight];
            double[][] lit;
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
                lit = fov.calculateFOV(resMap, pt.x, pt.y, 7, Radius.CIRCLE);
                for (int x = 0; x < gridWidth; x++) {
                    for (int y = 0; y < gridHeight; y++) {
                        if (lit[x][y] > 0.0)
                            lightness[x][y] += (int) (lit[x][y] * 200);
                    }
                }
            }
            for (int x = 0; x < gridWidth; x++) {
                for (int y = 0; y < gridHeight; y++) {
                    lightness[x][y] -= 40;
                }
            }
        }

        layers.put(0, 0, displayedMap, indicesFG, indicesBG, lightness);
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
        config.title = "SquidLib Test: SquidLayers";
        config.width = gridWidth * cellWidth;
        config.height = gridHeight * cellHeight;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new SquidLayersTest(), config);
    }
}
