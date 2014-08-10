package squidpony.examples.libgdxExample;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import java.awt.Font;
import squidpony.squidgrid.libgdx.SGPanelGDX;

/**
 * Controls what is on screen at any given time.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class DisplayMaster implements Screen {

    //game world fields
    private boolean runningTurn = false;//indicates that a turn is being calculated and animated
    private Map map;    //
    //UI output fields
    private Table primaryTable, viewTable, defaultActionTable, actionBarTable,
            healthBarTable, equipmentDummyTable, multiAreaTable, multiViewSelectionTable,
            leftTable, rightTable;
    private Stage stage;
    private Skin skin;
    private com.badlogic.gdx.physics.box2d.World boxWorld;
    private TextureAtlas atlas;
    private SoundManager sound = SoundManager.getInstance();
    private SGPanelGDX viewPanel;

    /**
     * Sets the view to the map provided and initializes display of the map, with the provided location as close to
     * center as possible given the size of the provided map and the screen space.
     *
     * The view only needs to be reset if changes to the map have occurred that were not incrementally passed in through
     * an Action or if an entirely new map should be displayed.
     *
     * @param map
     * @param startx the x coordinate that should be visible
     * @param starty the y coordinate that should be visible
     */
    public void setView(Map map, int startx, int starty) {
        if (viewPanel != null) {
            viewPanel.remove();
        }

        viewPanel = new SGPanelGDX();
        int w = map.width;
        int h = map.height;
        viewPanel.initialize(w, h, new Font(Font.SERIF, Font.BOLD, 18));//TODO -- replace hard coded font
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Item tile = map.contents[x][y];
                viewPanel.placeCharacter(x, y, tile.symbol, tile.color);
            }
        }

        viewPanel.refresh();

        viewTable.add(viewPanel);
        viewTable.setHeight(viewPanel.getHeight());
        viewTable.setWidth(viewPanel.getWidth());
        viewTable.setPosition(startx * viewPanel.getCellWidth(), starty * viewPanel.getCellHeight());
    }

    /**
     * Indicates that displayed item at the given start location should be moved to the given end position.
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     */
    public void indicateMovement(int startx, int starty, int endx, int endy) {
        //TODO -- fill out
    }

    /**
     * Animates the given character moving from the starting position to the end position.
     *
     * This is appropriate for something like an arrow that is not meant to stay on the screen once its movement has
     * finished.
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     * @param c
     */
    public void indicateMovement(int startx, int starty, int endx, int endy, char c) {
        //TODO -- fill out
    }

    /**
     * Shows that the player has died and then progresses to the appropriate end game screen.
     */
    public void indicatePlayerDied() {
        //TODO -- show that player died and bring up appropriate screen
    }

    public Map getMap() {
        return map;
    }

    public Table getViewTable() {
        return viewTable;
    }

    public SGPanelGDX getViewPanel() {
        return viewPanel;
    }

    /**
     * Resizes entire game display.
     *
     * @param width
     * @param height
     */
    @Override
    public void resize(int width, int height) {
        //TODO -- implement resize 
    }

    /**
     * Performs any rendering that needs to be done.
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0.0f, 0.0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        if (EnvironmentalVariables.isDebugMode()) {
            Table.drawDebug(stage);
        }
    }

    @Override
    public void show() {
        atlas = new TextureAtlas("assets/images/textures.txt");

        int width = EnvironmentalVariables.getScreenWidth(),
                height = EnvironmentalVariables.getScreenHeight();

        //set up the display tables
        skin = new Skin(Gdx.files.local("./assets/skin/uiskin.json"));

        primaryTable = new Table(skin);
        primaryTable.setFillParent(true);
//        stage = new Stage(width, height, true);
        stage = new Stage(new StretchViewport(width, height));
        Gdx.input.setInputProcessor(stage);
        stage.addActor(primaryTable);

        leftTable = new Table(skin);
        primaryTable.add(leftTable).width(width * 0.6f).height(height);

        viewTable = new Table(skin);

        ScrollPane viewScrollPane = new ScrollPane(viewTable, skin);
        viewScrollPane.setScrollbarsOnTop(false);
        viewScrollPane.setFadeScrollBars(false);
        leftTable.add(viewScrollPane).width(width * 0.6f).height(height * 0.9f).colspan(2);

        leftTable.row().height(height * 0.1f);

        defaultActionTable = new Table(skin);
//        Image image = new Image(atlas.findRegion("default action"));
//        image.setScaling(Scaling.fit);
//        defaultActionTable.add(image);
        leftTable.add(defaultActionTable).width(width * 0.05f);

        actionBarTable = new Table(skin);
//        image = new Image(atlas.findRegion("action bar"));
//        image.setHeight(height * 0.08f);
//        image.setScaling(Scaling.fillY);
//        actionBarTable.add(image);
        viewScrollPane = new ScrollPane(actionBarTable, skin);
        viewScrollPane.setScrollbarsOnTop(false);
        viewScrollPane.setFadeScrollBars(false);
        leftTable.add(viewScrollPane).width(width * 0.55f);
//        leftTable.add(actionBarTable).width(width * 0.55f);

        rightTable = new Table(skin);
        primaryTable.add(rightTable).top().right().width(width * 0.4f).height(height);

        healthBarTable = new Table(skin);
//        image = new Image(atlas.findRegion("health bars"));
//        image.setScaling(Scaling.fit);
//        healthBarTable.add(image);
        rightTable.add(healthBarTable).size(width * 0.4f, height * 0.2f);

        rightTable.row();

        equipmentDummyTable = new Table(skin);
//        image = new Image(atlas.findRegion("equipment dummies"));
//        image.setScaling(Scaling.fit);
//        equipmentDummyTable.add(image);
        rightTable.add(equipmentDummyTable).size(width * 0.4f, height * 0.12f);

        rightTable.row();

        multiViewSelectionTable = new Table(skin);
//        image = new Image(atlas.findRegion("multiview selection"));
//        image.setScaling(Scaling.fit);
//        multiViewSelectionTable.add(image);
        rightTable.add(multiViewSelectionTable).size(width * 0.4f, height * 0.12f);

        rightTable.row();

        multiAreaTable = new Table(skin);
//        image = new Image(atlas.findRegion("multiview"));
//        image.setScaling(Scaling.fit);
//        multiAreaTable.add(image);
        rightTable.add(multiAreaTable).size(width * 0.4f, height * 0.56f);

        //set all tables to debug draw if needed
        if (EnvironmentalVariables.isDebugMode()) {
            primaryTable.debug();
            leftTable.debug();
            rightTable.debug();
            viewTable.debug();
            defaultActionTable.debug();
            actionBarTable.debug();
            healthBarTable.debug();
            equipmentDummyTable.debug();
            multiViewSelectionTable.debug();
            multiAreaTable.debug();
        }
        
        World world = new World();
        setView(world.getDefaultMap(), world.getDefaultMap().width, world.getDefaultMap().height);
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        boxWorld.dispose();
        atlas.dispose();
        sound.dispose();
    }
}
