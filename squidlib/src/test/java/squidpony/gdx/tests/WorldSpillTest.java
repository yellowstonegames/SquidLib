package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.squidgrid.gui.gdx.DefaultResources;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidLayers;
import squidpony.squidgrid.mapping.SpillWorldMap;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.StatefulRNG;

/**
 * Created by Tommy Ettinger on 4/6/2016.
 */
public class WorldSpillTest extends ApplicationAdapter{
    public static final int gridWidth = 192, gridHeight = 128, cellWidth = 4, cellHeight = 4;

    SquidLayers layers;
    char[][] map, displayedMap;
    int[][] indicesBG;
    SpillWorldMap swm;
    GreasedRegion land;
    StatefulRNG rng;
    Stage stage;
    SpriteBatch batch;
    @Override
    public void create() {
        super.create();
        rng = new StatefulRNG(0x9876543210L);
        swm = new SpillWorldMap(gridWidth, gridHeight, FakeLanguageGen.FANTASY_NAME.word(rng, true));
        displayedMap = ArrayTools.fill(' ', gridWidth, gridHeight);
        map = swm.generate(0, false, true, 0.0, 1.4);
        land = new GreasedRegion(swm.heightMap, 0, 0xffff);
        indicesBG = land.writeIntsInto(ArrayTools.fill(27, gridWidth, gridHeight), 20);
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                swm.heightMap[x][y] = swm.heightMap[x][y] * 3 - 150;
            }
        }
        layers = new SquidLayers(gridWidth, gridHeight, cellWidth, cellHeight,
                DefaultResources.getStretchableDejaVuFont());
        layers.setLightingColor(SColor.WHITE);
        batch = new SpriteBatch();
        stage = new Stage(new StretchViewport(gridWidth * cellWidth, gridHeight * cellHeight), batch);
        stage.addActor(layers);
        stage.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                super.keyDown(event, keycode);
                refresh();
                return true;
            }
        });
        Gdx.input.setInputProcessor(stage);
    }

    public void refresh()
    {
        swm = new SpillWorldMap(gridWidth, gridHeight, FakeLanguageGen.FANTASY_NAME.word(rng, true));
        map = swm.generate(0, false, true, 0.0, 1.4);
        land.clear().refill(swm.heightMap, 0, 0xffff);
        ArrayTools.fill(indicesBG, 27);
        land.writeIntsInto(indicesBG, 20);
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                swm.heightMap[x][y] = swm.heightMap[x][y] * 3 - 150;
            }
        }
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
        layers.put(0, 0, displayedMap, indicesBG, indicesBG, swm.heightMap);
        stage.getViewport().apply(false);
        stage.draw();
        stage.act();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Test: SquidLayers";
        config.width = gridWidth * cellWidth;
        config.height = gridHeight * cellHeight;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new WorldSpillTest(), config);
    }
}
