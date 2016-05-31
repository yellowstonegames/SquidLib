package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.SpatialMap;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.SquidID;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;

/**
 * Created by Tommy Ettinger on 4/6/2016.
 */
public class IconsTest extends ApplicationAdapter{
    int gridWidth, gridHeight, cellWidth, cellHeight;
    SquidLayers layers;
    char[][] map, displayedMap;
    int[][] indicesFG, indicesBG;
    StatefulRNG rng;
    Stage stage;
    SpriteBatch batch;
    ArrayList<Color> colors;
    Coord[] points;
    double[][] resMap;
    float ctr = 0;
    TextureAtlas atlas;
    SpatialMap<SquidID, AnimatedEntity> things;
    Array<TextureAtlas.AtlasRegion> regions;
    int totalRegions;
    long seed;
    @Override
    public void create() {
        super.create();
        rng = new StatefulRNG(0x9876543210L);
        atlas = DefaultResources.getIconAtlas();
        regions = atlas.getRegions();
        totalRegions = regions.size;
        gridWidth = 50;
        gridHeight = 25;
        cellWidth = 32;
        cellHeight = 32;
        layers = new SquidLayers(gridWidth, gridHeight, cellWidth, cellHeight,
                DefaultResources.getStretchableSquareFont());
        layers.setTextSize(cellWidth, cellHeight+1);
        //colors = DefaultResources.getSCC().rainbow(0.2f, 1.0f, 144);
        /*
        colors = DefaultResources.getSCC().zigzagGradient(Color.DARK_GRAY, Color.LIGHT_GRAY, 200);
        colors.addAll(DefaultResources.getSCC().zigzagGradient(Color.LIGHT_GRAY, Color.DARK_GRAY, 200));
        */
        colors = DefaultResources.getSCC().rainbow(100);
        layers.setLightingColor(Color.WHITE);
        //PacMazeGenerator maze = new PacMazeGenerator(gridWidth, gridHeight, rng);
        //OrganicMapGenerator org = new OrganicMapGenerator(gridWidth, gridHeight, rng);
        DungeonGenerator gen = new DungeonGenerator(gridWidth, gridHeight, rng);
        map = gen.generate();
        displayedMap = DungeonUtility.hashesToLines(map);
        indicesBG = DungeonUtility.generateBGPaletteIndices(map);
        indicesFG = DungeonUtility.generatePaletteIndices(map);
        resMap = DungeonUtility.generateResistances(map);
        short[] packed = CoordPacker.pack(gen.getBareDungeon(), '.');
        points = CoordPacker.fractionPacked(packed, 7);

        seed = rng.getState();
        things = new SpatialMap<SquidID, AnimatedEntity>(points.length);
        AnimatedEntity ent;
        for (int i = 0; i < points.length; i++) {
            ent = layers.animateActor(points[i].x, points[i].y, regions.get(rng.nextInt(totalRegions)),
                    i, colors);
            things.add(points[i], new SquidID(), ent);
            ent.actor.setUserObject(i);
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
    public void resume() {
        super.resume();
        atlas = DefaultResources.getIconAtlas();
        regions = atlas.getRegions();
        totalRegions = regions.size;
        rng.setState(seed);
        things = new SpatialMap<SquidID, AnimatedEntity>(points.length);
        AnimatedEntity ent;
        for (int i = 0; i < points.length; i++) {
            ent = layers.animateActor(points[i].x, points[i].y, regions.get(rng.nextInt(totalRegions)),
                    i, colors);
            things.add(points[i], new SquidID(), ent);
            ent.actor.setUserObject(i);
        }
    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void render() {
        super.render();
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        ctr += Gdx.graphics.getDeltaTime();
        if(ctr > 0.05) {
            Coord pt;
            AnimatedEntity ent;
            for (int i = 0; i < points.length; i++) {
                pt = points[i];
                ent = things.get(pt);
                if(ent == null || ent.actor == null)
                    continue;
                ent.actor.setUserObject(((Integer) (ent.actor.getUserObject()) + 1) % colors.size());
                ent.actor.setColor(colors.get((Integer) ent.actor.getUserObject()));
            }
        }
        SquidPanel panel = layers.getForegroundLayer();
        if(ctr > 0.4) {
            ctr -= 0.4;
            Direction[] dirs = new Direction[4];
            Coord alter, pt;
            AnimatedEntity ent;
            for (int i = 0; i < points.length; i++) {
                pt = points[i];
                rng.shuffle(Direction.CARDINALS, dirs);
                for (Direction d : dirs) {
                    alter = pt.translate(d);
                    if (map[alter.x][alter.y] == '.' && !things.containsPosition(alter)) {
                        points[i] = alter;
                        ent = things.get(pt);
                        ent.gridX = alter.x;
                        ent.gridY = alter.y;
                        ent.actor.setPosition(panel.adjustX(ent.gridX, false), panel.adjustY(ent.gridY));
                        things.positionalModify(pt, ent);
                        things.move(pt, alter);
                        break;
                    }
                }
            }

        }

        layers.put(0, 0, displayedMap, indicesFG, indicesBG);
        stage.draw();
        batch.begin();
        for(AnimatedEntity ae : things)
        {
            layers.drawActor(batch, 1f, ae);
        }
        batch.end();

    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Test: Icons";
        config.width = 60 * 24;
        config.height = 40 * 24;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new IconsTest(), config);
    }
}
