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
//    private SquidMessageBox smb;
    private TextCellFactory font;
    @Override
    public void create() {
        batch = new FilterBatch();
        stage = new Stage(new StretchViewport(40 * 8,  25 * 8), batch);
        font = DefaultResources.getStretchableHeavySquareFont().setSmoothingMultiplier(1.25f);
        layers = new SparseLayers(40, 25, 8, 8, font);
//        smb = new SquidMessageBox(40, 25, font);
        List<IColoredString<Color>> messages = new ArrayList<>(20);
        new IColoredString.Impl<Color>("ABCDEFGHIJ0123456789abcdefghij01234567", SColor.WHITE).wrap(40, messages);
        new IColoredString.Impl<Color>("ABCDEFGHIJ0123456789abcdefghij012345678", SColor.WHITE).wrap(40, messages);
        new IColoredString.Impl<Color>("ABCDEFGHIJ0123456789 abcdefghij01234567", SColor.WHITE).wrap(40, messages);
        new IColoredString.Impl<Color>("alpha beta gamma delta epsilon zeta...", SColor.WHITE).wrap(40, messages);
        new IColoredString.Impl<Color>("alpha beta gamma delta epsilon zeta eta", SColor.WHITE).wrap(40, messages);
        new IColoredString.Impl<Color>("ABCDEFGHIJ0123456789abcdefghij01234567", SColor.RED).wrap(40, messages);
        new IColoredString.Impl<Color>("ABCDEFGHIJ0123456789abcdefghij012345678", SColor.GREEN).wrap(40, messages);
        new IColoredString.Impl<Color>("ABCDEFGHIJ0123456789 abcdefghij01234567", SColor.CW_AZURE).wrap(40, messages);
        GDXMarkup.instance.colorString("[CW Red]alpha[] [CW Apricot]beta [][CW Yellow]gamma [CW Jade]delta [CW Blue]epsilon [CW Purple]zeta[]...").wrap(40, messages);
        GDXMarkup.instance.colorString("[CW Red]alpha[] [CW Apricot]beta [][CW Yellow]gamma [CW Jade]delta [CW Blue]epsilon [CW Purple]zeta[] eta").wrap(40, messages);
        for (int i = 0; i < 25 && i < messages.size(); i++) {
            layers.put(0, i, messages.get(i));
        }
        stage.addActor(layers);
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //layers.put(10, 10, '@');
        stage.draw();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Tiny Test";
        config.width = 40 * 8;
        config.height = 25 * 8;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-64.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new TinyTest(), config);
    }

}
