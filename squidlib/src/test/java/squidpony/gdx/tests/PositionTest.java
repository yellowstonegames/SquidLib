package squidpony.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import squidpony.squidgrid.gui.gdx.*;

/**
 * Created by Tommy Ettinger on 7/24/2017.
 */
public class PositionTest extends ApplicationAdapter {
    public static final int msgWidth = 80, msgHeight = 6, gridWidth = 50, gridHeight = 20,
            statWidth = 30, statHeight = gridHeight, cellWidth = 8, cellHeight = 20;
    public SparseLayers display;
    public SquidPanel statPanel;
    public Stage stage;
    @Override
    public void create() {
        stage = new Stage();
        TextCellFactory tcf = 
                new TextCellFactory().font(DefaultResources.getLessTinyFont());
        tcf.height(cellHeight).width(cellWidth).initBySize();
        display = new SparseLayers(gridWidth, gridHeight, cellWidth, cellHeight, tcf);
        SquidMessageBox msgs = new SquidMessageBox(msgWidth, msgHeight, tcf);
        display.setBounds(0, msgHeight * cellHeight, gridWidth * cellWidth, gridHeight * cellHeight);
        msgs.setBounds(0, 0, msgWidth * cellWidth, msgHeight * cellHeight);
        statPanel = new SquidPanel(statWidth, statHeight, tcf);
        statPanel.setBounds(gridWidth * cellWidth, msgHeight * cellHeight, statWidth * cellWidth, statHeight * cellHeight);
        //display.setTextSize(cellWidth * 1.1f, cellHeight * 1.1f);
        //msgs.setTextSize(cellWidth * 1.1f, cellHeight * 1.1f);
        //statPanel.setTextSize(cellWidth * 1.1f, cellHeight * 1.1f);
        stage.addActor(display);
        stage.addActor(msgs);
        stage.addActor(statPanel);
        Gdx.input.setInputProcessor(stage);
        msgs.appendMessage(GDXMarkup
                .instance
                .colorString("[CW Pale Indigo]Welcome[] to SquidLib!"));
    }

    @Override
    public void render()
    {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        super.render();
        display.put((gridWidth - "Test Screen".length()) / 2, 10, "Test Screen", SColor.MEDIUM_CRIMSON);
        display.put((gridWidth - "[Esc] does nothing here".length()) / 2, 11, "[Esc] does nothing here", SColor.MEDIUM_CRIMSON);
        statPanel.put(0, 0, "Right String");
        stage.draw();
        stage.act();
    }

    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Demo: Position Test");
        config.useVsync(false);
        config.setWindowedMode(msgWidth * cellWidth, (msgHeight + gridHeight) * cellHeight);
        config.setWindowIcon(Files.FileType.Internal, "Tentacle-128.png", "Tentacle-64.png", "Tentacle-32.png", "Tentacle-16.png");
        new Lwjgl3Application(new PositionTest(), config);
    }

}
