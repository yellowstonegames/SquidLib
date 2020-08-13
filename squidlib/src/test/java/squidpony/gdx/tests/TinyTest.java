package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import squidpony.panel.IColoredString;
import squidpony.squidgrid.gui.gdx.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tommy Ettinger on 7/24/2017.
 */
public class TinyTest extends ApplicationAdapter {
    private FilterBatch batch;
    private Stage stage;
    private SparseLayers layers;
    private TextCellFactory font;
//    public static final int GRID_WIDTH = 40, GRID_HEIGHT = 25, CELL_WIDTH = 7, CELL_HEIGHT = 12;
    public static final int GRID_WIDTH = 40, GRID_HEIGHT = 14, CELL_WIDTH = 11, CELL_HEIGHT = 21;
    @Override
    public void create() {
        batch = new FilterBatch();
        stage = new Stage(new StretchViewport(GRID_WIDTH * CELL_WIDTH, GRID_HEIGHT * CELL_HEIGHT), batch);
        font = DefaultResources.getCrispSlabFamily();
//        font = new TextCellFactory().font(DefaultResources.getSevenTwelveFont());
        layers = new SparseLayers(GRID_WIDTH, GRID_HEIGHT, CELL_WIDTH, CELL_HEIGHT, font);
        List<IColoredString<Color>> messages = new ArrayList<>(20);
        GDXMarkup.instance.colorString("[Aurora Fusion Red]Baal, [*]Agares, [/]Vassago[*], Samigina[/], Marbas, [*]Valefor, [/]Amon[*], Barbatos... [*]Heed this![*]").wrap(40, messages);
        GDXMarkup.instance.colorString("ABCDEFGHIJ0123456789abcdefghij01234567" ).wrap(40, messages);
        GDXMarkup.instance.colorString("ABCDEFGHIJ0123456789abcdefghij012345678").wrap(40, messages);
        GDXMarkup.instance.colorString("ABCDEFGHIJ0123456789 abcdefghij01234567").wrap(40, messages);
        GDXMarkup.instance.colorString("alpha beta gamma delta epsilon zeta..." ).wrap(40, messages);
        GDXMarkup.instance.colorString("alpha beta gamma delta epsilon zeta eta").wrap(40, messages);
        GDXMarkup.instance.colorString("[Red]ABCDEFGHIJ0123456789abcdefghij01234567").wrap(40, messages);
        GDXMarkup.instance.colorString("[Green]ABCDEFGHIJ0123456789abcdefghij012345678").wrap(40, messages);
        GDXMarkup.instance.colorString("[CW Azure]ABCDEFGHIJ0123456789 abcdefghij01234567").wrap(40, messages);
        GDXMarkup.instance.colorString("[CW Red (lighter)]alpha[] [CW Apricot (lighter)]beta [][CW Yellow (lighter)]gamma [CW Jade (lighter)]delta [CW Blue (lighter)]epsilon [CW Purple (lighter)]zeta[]...").wrap(40, messages);
        GDXMarkup.instance.colorString("[CW Red]alpha[] [CW Apricot]beta [][CW Yellow]gamma [CW Jade]delta [CW Blue]epsilon [CW Purple]zeta[] eta").wrap(40, messages);
        GDXMarkup.instance.colorString("[CW Red (darker)]alpha[] [CW Apricot (darker)]beta [][CW Yellow (darker)]gamma [CW Jade (darker)]delta [CW Blue (darker)]epsilon [CW Purple (darker)]zeta[] eta!").wrap(40, messages);
        for (int i = 0; i < GRID_HEIGHT && i < messages.size(); i++) {
            layers.put(0, i, messages.get(i));
        }
        stage.addActor(layers);
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Tiny Test";
        config.width = GRID_WIDTH * CELL_WIDTH;
        config.height = GRID_HEIGHT * CELL_HEIGHT;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-64.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new TinyTest(), config);
    }

}
