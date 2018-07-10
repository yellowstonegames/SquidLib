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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.*;

import java.util.ArrayList;

/**
 * Created by Tommy Ettinger on 4/6/2016.
 */
public class IconsTest extends ApplicationAdapter{
    int gridWidth, gridHeight, cellWidth, cellHeight;
    SquidLayers layers;
    char[][] map, displayedMap;
    Color[][] fgColors, bgColors;
    StatefulRNG rng;
    Stage stage;
    SpriteBatch batch;
    ArrayList<Color> colors;
    double[][] resMap;
    long ctr = 0L, lastUpdate = 0L;
    TextureAtlas atlas;
    OrderedMap<Coord, AnimatedEntity> things;
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
                DefaultResources.getStretchableSlabFont());
        layers.setTextSize(cellWidth * 1.1f, cellHeight * 1.1f);
        layers.setAnimationDuration(0.35f);
        //colors = DefaultResources.getSCC().rainbow(0.2f, 1.0f, 144);
        /*
        colors = DefaultResources.getSCC().zigzagGradient(Color.DARK_GRAY, Color.LIGHT_GRAY, 200);
        colors.addAll(DefaultResources.getSCC().zigzagGradient(Color.LIGHT_GRAY, Color.DARK_GRAY, 200));
        */
        colors = DefaultResources.getSCC().rainbow(64);
        layers.setLightingColor(Color.WHITE);
        //PacMazeGenerator maze = new PacMazeGenerator(gridWidth, gridHeight, rng);
        //OrganicMapGenerator org = new OrganicMapGenerator(gridWidth, gridHeight, rng);
        DungeonGenerator gen = new DungeonGenerator(gridWidth, gridHeight, rng);
        map = gen.generate();
        displayedMap = DungeonUtility.hashesToLines(map);
        fgColors = MapUtility.generateDefaultColors(map);
        bgColors = MapUtility.generateDefaultBGColors(map);
        resMap = DungeonUtility.generateResistances(map);
        Coord[] points = new GreasedRegion(gen.getBareDungeon(), '.').quasiRandomSeparated(0.14);

        seed = rng.getState();
        things = new OrderedMap<>(points.length);
        AnimatedEntity ent;
        for (int i = 0; i < points.length; i++) {
            ent = layers.animateActor(points[i].x, points[i].y, regions.get(rng.nextInt(totalRegions)),
                    colors.get(i & 63));
            things.put(points[i], ent);
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
        AnimatedEntity ent;
        Coord pt;
        for (int i = 0; i < things.size(); i++) {
            pt = things.keyAt(i);
            ent = layers.animateActor(pt.x, pt.y, regions.get(rng.nextInt(totalRegions)),
                    colors.get(i & 63));
            things.put(pt, ent);
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
//        ctr = System.currentTimeMillis();
//        if(ctr > 125 + lastUpdate) {
//            lastUpdate = ctr;
        {
            AnimatedEntity ent;
            boolean hasActiveAnimations = false;
            for (int i = 0; i < things.size(); i++) {
                ent = things.getAt(i);
                if (ent == null || ent.actor == null)
                    continue;
                ent.actor.setColor(colors.get((Integer) (ent.actor.getUserObject()) + (int)(System.nanoTime() >> 26) & 63));
                hasActiveAnimations |= ent.actor.hasActions();
            }

            if (!hasActiveAnimations) 
            {
                Direction[] dirs = new Direction[4];
                Coord alter, pt;
                SquidPanel fg = layers.getForegroundLayer();
                for (int i = 0; i < things.size(); i++) {
                    pt = things.keyAt(i);
                    rng.shuffle(Direction.CARDINALS, dirs);
                    for (int di = 0; di < 4; di++) {
                        alter = pt.translate(dirs[di]);
                        if (map[alter.x][alter.y] == '.' && !things.containsKey(alter)) {
                            ent = things.getAt(i);
                            ent.actor.addAction(Actions.moveTo(fg.adjustX(alter.x, false), fg.adjustY(alter.y), 0.35f));
//                            layers.slide(ent, alter.x, alter.y, 0, 0.35f);
                            things.alter(pt, alter);
                            break;
                        }
                    }
                }

            }

        }
        layers.put(0, 0, displayedMap, fgColors, bgColors);
        stage.draw();
        batch.begin();
        int sz = things.size();
        for (int i = 0; i < sz; i++)
        {
            layers.drawActor(batch, 1f, things.getAt(i));
        }
        batch.end();
        Gdx.graphics.setTitle("Icon Demo running at FPS: " + Gdx.graphics.getFramesPerSecond());

    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Test: Icons";
        config.width = 60 * 24;
        config.height = 40 * 24;
        config.vSyncEnabled = false;
        config.foregroundFPS = 0;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new IconsTest(), config);
    }
}
