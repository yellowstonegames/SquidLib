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
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.DefaultResources;
import squidpony.squidgrid.gui.gdx.SquidLayers;
import squidpony.squidgrid.gui.gdx.TextCellFactory;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.PacMazeGenerator;
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
    @Override
    public void create() {
        super.create();
        rng = new StatefulRNG(0x12349876543210L);
        gridWidth = 70;
        gridHeight = 35;
        cellWidth = 12;
        cellHeight = 19;
        layers = new SquidLayers(gridWidth, gridHeight, cellWidth, cellHeight,
                DefaultResources.getStretchableFont());
        layers.setTextSize(cellWidth, cellHeight+1);
        colors = DefaultResources.getSCC().rainbow(0.2f, 1.0f, 144);
        layers.setLightingColor(colors.get(colorIndex));
        fov = new FOV(FOV.RIPPLE_LOOSE);
        PacMazeGenerator maze = new PacMazeGenerator(gridWidth, gridHeight, rng);
        map = maze.generate();
        displayedMap = DungeonUtility.hashesToLines(map);
        indicesBG = DungeonUtility.generateBGPaletteIndices(map);
        indicesFG = DungeonUtility.generatePaletteIndices(map);
        double[][] resMap = DungeonUtility.generateResistances(map);
        short[] packed = CoordPacker.pack(maze.getMap());
        ArrayList<Coord> points = CoordPacker.randomPortion(packed, 10, rng);
        lightness = new int[gridWidth][gridHeight];
        double[][] lit;
        for(Coord pt : points)
        {
            lit = fov.calculateFOV(resMap, pt.x, pt.y, 7, Radius.CIRCLE);
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
        layers.setLightingColor(colors.get(colorIndex = (colorIndex + 1) % colors.size()));
        layers.put(0, 0, displayedMap, indicesFG, indicesBG, lightness);
        stage.draw();

    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Test: SquidLayers";
        config.width = 70 * 12;
        config.height = 35 * 19;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new SquidLayersTest(), config);
    }
}
