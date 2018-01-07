package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import squidpony.panel.IColoredString;
import squidpony.squidgrid.gui.gdx.*;

/**
 * Created by Tommy Ettinger on 7/24/2017.
 */
public class TinyTest extends ApplicationAdapter {
    private SpriteBatch batch;
    private SquidLayers layers;
    SquidMessageBox smb;
    TextCellFactory font;
    @Override
    public void create() {
        batch = new SpriteBatch();
        font = DefaultResources.getSlabFamily().width(10).height(21).initBySize();
        layers = new SquidLayers(40, 25);
        smb = new SquidMessageBox(40, 25, font);
        smb.setTextSize(smb.cellWidth() * 1.1f, smb.cellHeight() * 1.1f);
        smb.appendWrappingMessage("ABCDEFGHIJ0123456789abcdefghij01234567");
        smb.appendWrappingMessage("ABCDEFGHIJ0123456789abcdefghij012345678");
        smb.appendWrappingMessage("ABCDEFGHIJ0123456789 abcdefghij01234567");
        smb.appendWrappingMessage("alpha beta gamma delta epsilon zeta...");
        smb.appendWrappingMessage("alpha beta gamma delta epsilon zeta eta");
        smb.appendWrappingMessage(new IColoredString.Impl<Color>("ABCDEFGHIJ0123456789abcdefghij01234567", SColor.RED));
        smb.appendWrappingMessage(new IColoredString.Impl<Color>("ABCDEFGHIJ0123456789abcdefghij012345678", SColor.GREEN));
        smb.appendWrappingMessage(new IColoredString.Impl<Color>("ABCDEFGHIJ0123456789 abcdefghij01234567", SColor.CW_AZURE));
        smb.appendWrappingMessage(GDXMarkup.instance.colorString("[CW Red]alpha[] [CW Apricot]beta [][CW Yellow]gamma [CW Jade]delta [CW Blue]epsilon [CW Purple]zeta[]..."));
        smb.appendWrappingMessage(GDXMarkup.instance.colorString("[CW Red]alpha[] [CW Apricot]beta [][CW Yellow]gamma [CW Jade]delta [CW Blue]epsilon [CW Purple]zeta[] eta"));
    }

    @Override
    public void render() {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //layers.put(10, 10, '@');

        batch.begin();
        smb.draw(batch, 1f);
        batch.end();
    }

    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "SquidLib Demo: Tiny Test";
        config.width = 400;
        config.height = 525;
        config.addIcon("Tentacle-16.png", Files.FileType.Internal);
        config.addIcon("Tentacle-32.png", Files.FileType.Internal);
        config.addIcon("Tentacle-64.png", Files.FileType.Internal);
        config.addIcon("Tentacle-128.png", Files.FileType.Internal);
        new LwjglApplication(new TinyTest(), config);
    }

}