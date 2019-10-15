package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.squidgrid.gui.gdx.DefaultResources;
import squidpony.squidgrid.gui.gdx.FilterBatch;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidPanel;
import squidpony.squidgrid.mapping.SpillWorldMap;
import squidpony.squidmath.Coord;
import squidpony.squidmath.StatefulRNG;

/**
 * Created by Tommy Ettinger on 4/6/2016.
 */
public class WorldSpillTest extends ApplicationAdapter{
    public static final int gridWidth = 256, gridHeight = 256, cellWidth = 2, cellHeight = 2;

    SquidPanel display;
    char[][] map, displayedMap;
    SpillWorldMap swm;
    StatefulRNG rng;
    Stage stage;
    private FilterBatch batch;
    float landColor, oceanColor, lightestColor;
    @Override
    public void create() {
        super.create();
        Coord.expandPoolTo(gridWidth, gridHeight);
        rng = new StatefulRNG(0x9876543210L);
        swm = new SpillWorldMap(gridWidth, gridHeight, FakeLanguageGen.FANTASY_NAME.word(rng, true));
        displayedMap = ArrayTools.fill(' ', gridWidth, gridHeight);
        map = swm.generate(0, false, true, 0.0, 1.4);
        landColor = SColor.LIME_GREEN.toFloatBits();
        oceanColor = SColor.CERULEAN.toFloatBits();
        lightestColor = SColor.CW_ALMOST_WHITE.toFloatBits();

        display = new SquidPanel(gridWidth, gridHeight,
                DefaultResources.getStretchableDejaVuFont().width(cellWidth).height(cellHeight).initBySize());
        display.setLightingColor(SColor.WHITE);
        batch = new FilterBatch();
        stage = new Stage(new StretchViewport(gridWidth * cellWidth, gridHeight * cellHeight), batch);
        stage.addActor(display);
        stage.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                super.keyDown(event, keycode);
                refresh();
                return true;
            }
        });
        refresh();
        Gdx.input.setInputProcessor(stage);
    }

    public void refresh()
    {
        swm = new SpillWorldMap(gridWidth, gridHeight, FakeLanguageGen.FANTASY_NAME.word(rng, true));
        map = swm.generate(0, false, true, 0.0, 0.06);
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                display.colors[x][y] =
                        (swm.heightMap[x][y] >= 0) ? SColor.lerpFloatColors(landColor, lightestColor,
                                Math.min((swm.heightMap[x][y] * 3 + 106f) / (gridHeight + gridWidth), 1f))
                : SColor.lerpFloatColors(oceanColor, lightestColor, 0.1f);
            }
        }
        ArrayTools.fill(display.contents, '\0');

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
