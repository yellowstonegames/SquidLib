package squidpony.gdx.examples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.Coord;
import squidpony.squidmath.RNG;

import java.util.HashMap;

public class ImageDemo extends ApplicationAdapter {
    SpriteBatch batch;

    private RNG rng;
    private SquidLayers display;
    private DungeonGenerator dungeonGen;
    private char[][] bareDungeon, lineDungeon;
    private Color[][] colors, bgColors;
    private int[][] lights;
    private int width, height;
    private int cellWidth, cellHeight;
    private SquidInput input;
    private static final Color bgColor = SColor.DARK_SLATE_GRAY;
    private HashMap<Coord, AnimatedEntity> creatures;
    private Stage stage;
    @Override
    public void create () {
        batch = new SpriteBatch();
        width = 30;
        height = 20;
        cellWidth = 18;
        cellHeight = 36;
        display = new SquidLayers(width * 2, height, cellWidth, cellHeight, DefaultResources.narrowNameExtraLarge);
        display.setAnimationDuration(0.03f);
        stage = new Stage(new ScreenViewport(), batch);

        rng = new RNG(0x1337BEEF);

        dungeonGen = new DungeonGenerator(width, height, rng);
        dungeonGen.addWater(10);
        dungeonGen.addDoors(15, true);

        // change the TilesetType to lots of different choices to see what dungeon works best.
        bareDungeon = dungeonGen.generate(TilesetType.DEFAULT_DUNGEON);
        bareDungeon = DungeonUtility.closeDoors(bareDungeon);
        lineDungeon = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(bareDungeon));
        char[][] placement = DungeonUtility.closeDoors(bareDungeon);
        Coord pl = dungeonGen.utility.randomFloor(placement);
        placement[pl.x][pl.y] = '@';
        int numMonsters = 15;
        creatures = new HashMap<>(numMonsters);
        for(int i = 0; i < numMonsters; i++)
        {
            Coord monPos = dungeonGen.utility.randomFloor(placement);
            placement[monPos.x][monPos.y] = 'M';
            if(rng.nextBoolean())
                creatures.put(monPos, display.animateActor(monPos.x, monPos.y, "M!", SColor.RED, true));
            else
                creatures.put(monPos, display.animateActor(monPos.x, monPos.y, DefaultResources.getTentacle(), true, false));
        }
        colors = MapUtility.generateDefaultColors(bareDungeon);
        bgColors = MapUtility.generateDefaultBGColors(bareDungeon);
        lights = MapUtility.generateLightnessModifiers(bareDungeon, (System.currentTimeMillis() & 0xFFFFFFL) * 0.013);

        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key)
                {
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE:
                    {
                        Gdx.app.exit();
                    }
                }
            }
        });
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(input);
        // and then add display, our one visual component, to the list of things that act in Stage.
        display.setPosition(0, 0);
        stage.addActor(display);

    }
    public void putMap()
    {
        for (int i = 0; i < width * 2; i++) {
            for (int j = 0; j < height; j++) {
                // if we see it now, we remember the cell and show a lit cell based on the fovmap value (between 0.0
                // and 1.0), with 1.0 being almost pure white at +115 lightness and 0.0 being rather dark at -85.
                display.put(i, j, lineDungeon[i][j], colors[i/2][j], bgColors[i/2][j],
                        lights[i/2][j] + 10);
                // if we don't see it now, but did earlier, use a very dark background, but lighter than black.

            }
        }
    }
    @Override
    public void render () {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(bgColor.r / 255.0f, bgColor.g / 255.0f, bgColor.b / 255.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // not sure if this is always needed...
        Gdx.gl.glEnable(GL20.GL_BLEND);

        // this does the standard lighting for walls, floors, etc. but also uses the time to do the Simplex noise thing.
        lights = MapUtility.generateLightnessModifiers(bareDungeon, (System.currentTimeMillis() & 0xFFFFFFL) * 0.013);

        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        // if the user clicked, we have a list of moves to perform.

        // if we are waiting for the player's input and get input, process it.
        if(input.hasNext()) {
            input.next();
        }

        // stage has its own batch and must be explicitly told to draw(). this also causes it to act().
        stage.draw();

        // disolay does not draw all AnimatedEntities by default, since FOV often changes how they need to be drawn.
        batch.begin();
        // the player needs to get drawn every frame, of course.
        for(AnimatedEntity mon : creatures.values()) {
            display.drawActor(batch, 1.0f, mon);
        }
        // batch must end if it began.
        batch.end();
    }

    @Override
	public void resize(int width, int height) {
		super.resize(width, height);
		input.getMouse().reinitialize((float) width / this.width, (float) height / this.height);
	}
}
