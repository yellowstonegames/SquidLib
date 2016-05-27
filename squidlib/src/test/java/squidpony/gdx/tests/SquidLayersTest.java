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
import squidpony.squidgrid.gui.gdx.DefaultResources;
import squidpony.squidgrid.gui.gdx.SquidLayers;
import squidpony.squidgrid.gui.gdx.TextCellFactory;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.OrganicMapGenerator;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;

/**
 * Created by Tommy Ettinger on 4/6/2016.
 */
public class SquidLayersTest extends ApplicationAdapter{
    int gridWidth, gridHeight, cellWidth, cellHeight;
    SquidLayers layers;
    char[][] map, displayedMap;
    int[][] indicesFG, indicesBG, lightness;
    FOV fov;
    TextCellFactory tcf;
    StatefulRNG rng;
    Stage stage;
    SpriteBatch batch;
    ArrayList<Color> colors;
    int colorIndex = 0;
    ArrayList<Coord> points;
    double[][] resMap;
    float ctr = 0;
    @Override
    public void create() {
        super.create();
        rng = new StatefulRNG(0x9876543210L);
        gridWidth = 120;
        gridHeight = 50;
        cellWidth = 8;
        cellHeight = 17;
        layers = new SquidLayers(gridWidth, gridHeight, cellWidth, cellHeight,
                DefaultResources.getStretchableFont());
        layers.setTextSize(cellWidth, cellHeight+1);
        //colors = DefaultResources.getSCC().rainbow(0.2f, 1.0f, 144);
        colors = DefaultResources.getSCC().zigzagGradient(Color.RED, Color.PINK, 200);
        colors.addAll(DefaultResources.getSCC().zigzagGradient(Color.PINK, Color.RED, 200));
        layers.setLightingColor(colors.get(colorIndex));
        fov = new FOV(FOV.SHADOW);
        //PacMazeGenerator maze = new PacMazeGenerator(gridWidth, gridHeight, rng);
        OrganicMapGenerator org = new OrganicMapGenerator(gridWidth, gridHeight, rng);
        DungeonGenerator gen = new DungeonGenerator(gridWidth, gridHeight, rng);
        map = gen.generate(org.generate());
        displayedMap = DungeonUtility.hashesToLines(map);
        indicesBG = DungeonUtility.generateBGPaletteIndices(map);
        indicesFG = DungeonUtility.generatePaletteIndices(map);
        resMap = DungeonUtility.generateResistances(map);
        short[] packed = CoordPacker.pack(gen.getBareDungeon(), '.');
        points = CoordPacker.randomPortion(packed, 10, rng);
        lightness = new int[gridWidth][gridHeight];
        double[][] lit;
        for(Coord pt : points)
        {
            lit = fov.calculateFOV(resMap, pt.x, pt.y, 11, Radius.CIRCLE);
            for (int x = 0; x < gridWidth; x++) {
                for (int y = 0; y < gridHeight; y++) {
                    if(lit[x][y] > 0.0)
                        lightness[x][y] += (int)(lit[x][y] * 200);
                }
            }
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
    }

    @Override
    public void render() {
        super.render();
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        ctr += Gdx.graphics.getDeltaTime();
        if(ctr > 0.1)
            layers.setLightingColor(colors.get(colorIndex = (colorIndex + 1) % colors.size()));
        if(ctr > 0.2) {
            ctr -= 0.2;
            lightness = new int[gridWidth][gridHeight];
            double[][] lit;
            Direction[] dirs = new Direction[4];
            Coord alter;
            for (int i = 0; i < points.size(); i++) {
                Coord pt = points.get(i);
                rng.shuffle(Direction.CARDINALS, dirs);
                for (Direction d : dirs) {
                    alter = pt.translate(d);
                    if (map[alter.x][alter.y] == '.') {
                        pt = alter;
                        points.set(i, pt);
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
        stage.draw();

    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Test: SquidLayers";
        config.width = 120 * 8;
        config.height = 50 * 17;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new SquidLayersTest(), config);
    }
}
