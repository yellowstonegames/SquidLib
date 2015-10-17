package squidpony.gdx.examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import squidpony.panel.IColoredString;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.AnimatedEntity;
import squidpony.squidgrid.gui.gdx.GroupCombinedPanel;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.gui.gdx.SquidLayers;
import squidpony.squidgrid.gui.gdx.SquidMouse;
import squidpony.squidgrid.gui.gdx.SquidPanel;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.Coord;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class EverythingDemo extends ApplicationAdapter {
    private enum Phase {WAIT, PLAYER_ANIM, MONSTER_ANIM}
    SpriteBatch batch;

    private Phase phase = Phase.WAIT;
    private RNG rng;
    private LightRNG lrng;
    private SquidLayers display;
    /** Non-{@code null} iff '?' was pressed before */
    private /*Nullable*/ Actor help;
    private DungeonGenerator dungeonGen;
    private char[][] bareDungeon, lineDungeon;
    private double[][] res;
    private int[][] colors, bgColors, lights;
    private double[][] fovmap, pathMap;
    private AnimatedEntity player;
    private FOV fov;
    /** In number of cells */
    private int width;
    /** In number of cells */
    private int height;
    /** The pixel width of a cell */
    private int cellWidth;
    /** The pixel height of a cell */
    private int cellHeight;
    private SquidInput input;
    private double counter;
    private boolean[][] seen;
    private int health = 7;
    private static final Color bgColor = SColor.DARK_SLATE_GRAY;
    private HashMap<AnimatedEntity, Integer> monsters;
    private DijkstraMap getToPlayer, playerToCursor;
    private Stage stage;
    private int framesWithoutAnimation = 0;
    private Coord cursor;
    private ArrayList<Coord> toCursor;
    private ArrayList<Coord> awaitedMoves;
    @Override
    public void create () {
        batch = new SpriteBatch();
        width = 80;
        height = 30;
        cellWidth = 12;
        cellHeight = 24;
        // the font will try to load Rogue-Zodiac.ttf from resources. I (Tommy Ettinger) made it, and it's under the
        // same license as SquidLib.
        display = new SquidLayers(width, height, cellWidth, cellHeight);
        display.setAnimationDuration(0.03f);
        stage = new Stage(new ScreenViewport(), batch);

        counter = 0;
        lrng = new LightRNG(0x1337BEEF);
        rng = new RNG(lrng);

        dungeonGen = new DungeonGenerator(width, height, rng);
        dungeonGen.addWater(12);
        dungeonGen.addGrass(15);
        dungeonGen.addDoors(15, true);

        // change the TilesetType to lots of different choices to see what dungeon works best.
        bareDungeon = dungeonGen.generate(TilesetType.DEFAULT_DUNGEON);
        bareDungeon = DungeonUtility.closeDoors(bareDungeon);
        lineDungeon = DungeonUtility.hashesToLines(bareDungeon);
        char[][] placement = DungeonUtility.closeDoors(bareDungeon);
        Coord pl = dungeonGen.utility.randomFloor(placement);
        placement[pl.x][pl.y] = '@';
        int numMonsters = 25;
        monsters = new HashMap<AnimatedEntity, Integer>(numMonsters);
        for(int i = 0; i < numMonsters; i++)
        {
            Coord monPos = dungeonGen.utility.randomFloor(placement);
            placement[monPos.x][monPos.y] = 'M';
            monsters.put(display.animateActor(monPos.x, monPos.y, 'M', 11), 0);

        }
        // your choice of FOV matters here.
        fov = new FOV(FOV.RIPPLE_TIGHT);
        getToPlayer = new DijkstraMap(bareDungeon, DijkstraMap.Measurement.CHEBYSHEV);
        getToPlayer.rng = rng;
        getToPlayer.setGoal(pl);
        pathMap = getToPlayer.scan(null);
        res = DungeonUtility.generateResistances(bareDungeon);
        fovmap = fov.calculateFOV(res, pl.x, pl.y, 8, Radius.SQUARE);

        player = display.animateActor(pl.x, pl.y, Character.forDigit(health, 10), 30);
        cursor = Coord.get(-1, -1);
        toCursor = new ArrayList<Coord>(10);
        awaitedMoves = new ArrayList<Coord>(10);
        playerToCursor = new DijkstraMap(bareDungeon, DijkstraMap.Measurement.EUCLIDEAN);
        colors = DungeonUtility.generatePaletteIndices(bareDungeon);
        bgColors = DungeonUtility.generateBGPaletteIndices(bareDungeon);
        lights = DungeonUtility.generateLightnessModifiers(bareDungeon, counter);
        seen = new boolean[width][height];

        // this is a big one.
        // SquidInput can be constructed with a KeyHandler (which just processes specific keypresses), a SquidMouse
        // (which is given an InputProcessor implementation and can handle multiple kinds of mouse move), or both.
        // keyHandler is meant to be able to handle complex, modified key input, typically for games that distinguish
        // between, say, 'q' and 'Q' for 'quaff' and 'Quip' or whatever obtuse combination you choose. The
        // implementation here handles hjklyubn keys for 8-way movement, numpad for 8-way movement, arrow keys for
        // 4-way movement, and wasd for 4-way movement. Shifted letter keys produce capitalized chars when passed to
        // KeyHandler.handle(), but we don't care about that so we just use two case statements with the same body,
        // one for the lower case letter and one for the upper case letter.
        // You can also set up a series of future moves by clicking within FOV range, using mouseMoved to determine the
        // path to the mouse position with a DijkstraMap (called playerToCursor), and using touchUp to actually trigger
        // the event when someone clicks.
        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key)
                {
                    case SquidInput.UP_ARROW:
                    case 'k':
                    case 'w':
                    case 'K':
                    case 'W':
                    {
                        move(0, -1);
                        break;
                    }
                    case SquidInput.DOWN_ARROW:
                    case 'j':
                    case 's':
                    case 'J':
                    case 'S':
                    {
                        move(0, 1);
                        break;
                    }
                    case SquidInput.LEFT_ARROW:
                    case 'h':
                    case 'a':
                    case 'H':
                    case 'A':
                    {
                        move(-1, 0);
                        break;
                    }
                    case SquidInput.RIGHT_ARROW:
                    case 'l':
                    case 'd':
                    case 'L':
                    case 'D':
                    {
                        move(1, 0);
                        break;
                    }

                    case SquidInput.UP_LEFT_ARROW:
                    case 'y':
                    case 'Y':
                    {
                        move(-1, -1);
                        break;
                    }
                    case SquidInput.UP_RIGHT_ARROW:
                    case 'u':
                    case 'U':
                    {
                        move(1, -1);
                        break;
                    }
                    case SquidInput.DOWN_RIGHT_ARROW:
                    case 'n':
                    case 'N':
                    {
                        move(1, 1);
                        break;
                    }
                    case SquidInput.DOWN_LEFT_ARROW:
                    case 'b':
                    case 'B':
                    {
                        move(-1, 1);
                        break;
                    }
                    case '?': {
                    	toggleHelp();
                        break;
                    }
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE:
                    {
                        Gdx.app.exit();
                        break;
                    }
                }
            }
        }, new SquidMouse(cellWidth, cellHeight, new InputAdapter() {

            // if the user clicks within FOV range and there are no awaitedMoves queued up, generate toCursor if it
            // hasn't been generated already by mouseMoved, then copy it over to awaitedMoves.
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(fovmap[screenX][screenY] > 0.0 && awaitedMoves.isEmpty()) {
                    if (toCursor.isEmpty()) {
                        cursor = Coord.get(screenX, screenY);
                        toCursor = playerToCursor.findPath(30, null, null, Coord.get(player.gridX, player.gridY), cursor);
                    }
                    awaitedMoves = new ArrayList<>(toCursor);
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return mouseMoved(screenX, screenY);
            }

            // causes the path to the mouse position to become highlighted (toCursor contains a list of points that
            // receive highlighting). Uses DijkstraMap.findPath() to find the path, which is surprisingly fast.
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                if(!awaitedMoves.isEmpty())
                    return false;
                if(cursor.x == screenX && cursor.y == screenY)
                {
                    return false;
                }
                if(fovmap[screenX][screenY] > 0.0) {
                    cursor = Coord.get(screenX, screenY);
                    toCursor = playerToCursor.findPath(30, null, null, Coord.get(player.gridX, player.gridY), cursor);
                }
                return false;
            }
        }));
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(input);
        // and then add display, our one visual component, to the list of things that act in Stage.
        display.setPosition(0, 0);
        stage.addActor(display);

    }
    /**
     * Move the player or open closed doors, remove any monsters the player bumped, then update the DijkstraMap and
     * have the monsters that can see the player try to approach.
     * In a fully-fledged game, this would not be organized like this, but this is a one-file demo.
     * @param xmod
     * @param ymod
     */
    private void move(int xmod, int ymod) {
    	clearHelp();

        if(health <= 0) return;

        int newX = player.gridX + xmod, newY = player.gridY + ymod;
        if (newX >= 0 && newY >= 0 && newX < width && newY < height
                && bareDungeon[newX][newY] != '#')
        {
            // '+' is a door.
            if (lineDungeon[newX][newY] == '+') {
                bareDungeon[newX][newY] = '/';
                lineDungeon[newX][newY] = '/';
                // changes to the map mean the resistances for FOV need to be regenerated.
                res = DungeonUtility.generateResistances(bareDungeon);
                // recalculate FOV, store it in fovmap for the render to use.
                fovmap = fov.calculateFOV(res, player.gridX, player.gridY, 8, Radius.SQUARE);

            } else {
                // recalculate FOV, store it in fovmap for the render to use.
                fovmap = fov.calculateFOV(res, newX, newY, 8, Radius.SQUARE);
                display.slide(player, newX, newY);

                for(AnimatedEntity ae : monsters.keySet()) {
                    if (newX == ae.gridX && newY == ae.gridY) {
                        monsters.remove(ae);
                        break;
                    }
                }
            }

            phase = Phase.PLAYER_ANIM;
        }
    }

    // check if a monster's movement would overlap with another monster.
    private boolean checkOverlap(AnimatedEntity ae, int x, int y, ArrayList<Coord> futureOccupied)
    {
        for(AnimatedEntity mon : monsters.keySet())
        {
            if(mon.gridX == x && mon.gridY == y && !mon.equals(ae))
                return true;
        }
        for(Coord p : futureOccupied)
        {
            if(x == p.x && y == p.y)
                return true;
        }
        return false;
    }

    private void postMove()
    {

        phase = Phase.MONSTER_ANIM;
        // The next two lines are important to avoid monsters treating cells the player WAS in as goals.
        getToPlayer.clearGoals();
        getToPlayer.resetMap();
        // now that goals are cleared, we can mark the current player position as a goal.
        getToPlayer.setGoal(player.gridX, player.gridY);
        // this is an important piece of DijkstraMap usage; the argument is a Set of Points for squares that
        // temporarily cannot be moved through (not walls, which are automatically known because the map char[][]
        // was passed to the DijkstraMap constructor, but things like moving creatures and objects).
        LinkedHashSet<Coord> monplaces = new LinkedHashSet<>(monsters.size());
        for(AnimatedEntity ae : monsters.keySet())
        {
            monplaces.add(Coord.get(ae.gridX, ae.gridY));
        }
        pathMap = getToPlayer.scan(monplaces);

        // recalculate FOV, store it in fovmap for the render to use.
        fovmap = fov.calculateFOV(res, player.gridX, player.gridY, 8, Radius.SQUARE);
        // handle monster turns
        ArrayList<Coord> nextMovePositions = new ArrayList<>(25);
        for(Map.Entry<AnimatedEntity, Integer> mon : monsters.entrySet())
        {
            // monster values are used to store their aggression, 1 for actively stalking the player, 0 for not.
            if(mon.getValue() > 0 || fovmap[mon.getKey().gridX][mon.getKey().gridY] > 0.1)
            {
                // this block is used to ensure that the monster picks the best path, or a random choice if there
                // is more than one equally good best option.
                Direction choice = null;
                double best = 9999.0;
                for(Direction d : rng.shuffle(Direction.OUTWARDS))
                {
                    Coord tmp = Coord.get(mon.getKey().gridX + d.deltaX, mon.getKey().gridY + d.deltaY);
                    if(pathMap[tmp.x][tmp.y] < best &&
                            !checkOverlap(mon.getKey(), tmp.x, tmp.y, nextMovePositions))
                    {
                        // pathMap is a 2D array of doubles where 0 is the goal (the player).
                        // we use best to store which option is closest to the goal.
                        best = pathMap[tmp.x][tmp.y];
                        choice = d;
                    }
                }
                if(choice != null) {
                    Coord tmp = Coord.get(mon.getKey().gridX + choice.deltaX, mon.getKey().gridY + choice.deltaY);
                    // if we would move into the player, instead damage the player and give newMons the current
                    // position of this monster.
                    if (player.gridX == tmp.x && player.gridY == tmp.y) {
                        display.wiggle(player);
                        health--;
                        player.setText("" + health);
                        monsters.put(mon.getKey(), 1);
                    }
                    // otherwise store the new position in newMons.
                    else {
                        /*if (fovmap[mon.getKey().x][mon.getKey().y] > 0.0) {
                            display.put(mon.getKey().x, mon.getKey().y, 'M', 11);
                        }*/
                        nextMovePositions.add(Coord.get(tmp.x, tmp.y));
                        display.slide(mon.getKey(), tmp.x, tmp.y);
                    }
                }
                else
                {
                    monsters.put(mon.getKey(), 1);
                }
            }
            else
            {
                monsters.put(mon.getKey(), mon.getValue());
            }
        }

    }

    private void toggleHelp() {
        if(help != null)
        {
            clearHelp();
            return;
        }
        final int nbMonsters = monsters.size();

		/* Prepare the String to display */
		final IColoredString<Color> cs = new IColoredString.Impl<Color>();
		cs.append("Still ", null);
		{
			final Color nbColor;
			if (nbMonsters <= 1)
				/* Green */
				nbColor = new Color(0, 1, 0, 1);
			else if (nbMonsters <= 5)
				/* Orange */
				nbColor = new Color(1, 0.5f, 0, 1);
			else
				/* Red */
				nbColor = new Color(1, 0, 0, 1);
			cs.appendInt(nbMonsters, nbColor);
		}
		cs.append(String.format(" monster%s to kill", nbMonsters == 1 ? "" : "s"), null);

		/* The panel's width */
		final int w = cs.length();
		/* The panel's height. */
		final int h = 1;

        final SquidPanel bg = new SquidPanel(w, h, display.getTextFactory());
        final SquidPanel fg = new SquidPanel(w, h, display.getTextFactory());
        final GroupCombinedPanel<Color> gcp = new GroupCombinedPanel<Color>();
		/*
		 * We're setting them late just for the demo, as it avoids giving 'w'
		 * and 'h' at construction time.
		 */
		gcp.setPanels(bg, fg);

		/*
		 * Set the position (the center), using libgdx's 'setPosition'
		 * method, that takes the bottom left corner as input.
		 */
		gcp.setPosition(((width / 2) - (w / 2)) * cellWidth, (height / 2) * cellHeight);

		/* Fill the background with some grey */
		gcp.fillBG(new Color(0.3f, 0.3f, 0.3f, 0.9f));

		/* Now, to set the text we have to follow SquidPanel's convention */
		/* First 0: justify left, second 0: first (and only) line */
		gcp.putFG(0, 0, cs);
		
		help = gcp;

		stage.addActor(gcp);
	}

	private void clearHelp() {
		if (help == null)
			/* Nothing to do */
			return;
		help.clear();
		stage.getActors().removeValue(help, true);
        help = null;
	}

    public void putMap()
    {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // if we see it now, we remember the cell and show a lit cell based on the fovmap value (between 0.0
                // and 1.0), with 1.0 being almost pure white at +115 lightness and 0.0 being rather dark at -85.
                if (fovmap[i][j] > 0.0) {
                    seen[i][j] = true;
                    display.put(i, j, lineDungeon[i][j], colors[i][j], bgColors[i][j],
                            lights[i][j] + (int) (-85 + 200 * fovmap[i][j]));
                    // if we don't see it now, but did earlier, use a very dark background, but lighter than black.
                } else if (seen[i][j]) {
                    display.put(i, j, lineDungeon[i][j], colors[i][j], bgColors[i][j], -140);
                }
            }
        }
        for (Coord pt : toCursor)
        {
            // use a brighter light to trace the path to the cursor, from 170 max lightness to 0 min.
            display.highlight(pt.x, pt.y, lights[pt.x][pt.y] + (int) (0 + 170 * fovmap[pt.x][pt.y]));
        }
    }
    @Override
    public void render () {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(bgColor.r / 255.0f, bgColor.g / 255.0f, bgColor.b / 255.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // not sure if this is always needed...
        Gdx.gl.glEnable(GL20.GL_BLEND);

        // used as the z-axis when generating Simplex noise to make water seem to "move"
        counter += Gdx.graphics.getDeltaTime() * 15;
        // this does the standard lighting for walls, floors, etc. but also uses counter to do the Simplex noise thing.
        lights = DungeonUtility.generateLightnessModifiers(bareDungeon, counter);

        // you done bad. you done real bad.
        if (health <= 0) {
            // still need to display the map, then write over it with a message.
            putMap();
            display.putBoxedString(width / 2 - 11, height / 2 - 1, "YOU HAVE BEEN EATEN!");
            display.putBoxedString(width / 2 - 11, height / 2 + 5, "     q to quit.     ");

            // because we return early, we still need to draw.
            stage.draw();
            // q still needs to quit.
            if(input.hasNext())
                input.next();
            return;
        }
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        // if the user clicked, we have a list of moves to perform.
        if(!awaitedMoves.isEmpty())
        {
            // extremely similar to the block below that also checks if animations are done
            // this doesn't check for input, but instead processes and removes Points from awaitedMoves.
            if(!display.hasActiveAnimations()) {
                ++framesWithoutAnimation;
                if (framesWithoutAnimation >= 3) {
                    framesWithoutAnimation = 0;
                    switch (phase) {
                        case WAIT:
                        case MONSTER_ANIM:
                            Coord m = awaitedMoves.remove(0);
                            toCursor.remove(0);
                            move(m.x - player.gridX, m.y - player.gridY);
                            break;
                        case PLAYER_ANIM:
                            postMove();
                            break;
                    }
                }
            }
        }
        // if we are waiting for the player's input and get input, process it.
        else if(input.hasNext() && !display.hasActiveAnimations() && phase == Phase.WAIT) {
            input.next();
        }
        // if the previous blocks didn't happen, and there are no active animations, then either change the phase
        // (because with no animations running the last phase must have ended), or start a new animation soon.
        else if(!display.hasActiveAnimations()) {
            ++framesWithoutAnimation;
            if (framesWithoutAnimation >= 3) {
                framesWithoutAnimation = 0;
                switch (phase) {
                    case WAIT:
                        break;
                    case MONSTER_ANIM: {
                        phase = Phase.WAIT;
                    }
                    break;
                    case PLAYER_ANIM: {
                        postMove();

                    }
                }
            }
        }
        // if we do have an animation running, then how many frames have passed with no animation needs resetting
        else
        {
            framesWithoutAnimation = 0;
        }

        // stage has its own batch and must be explicitly told to draw(). this also causes it to act().
        stage.draw();

        // disolay does not draw all AnimatedEntities by default, since FOV often changes how they need to be drawn.
        batch.begin();
        // the player needs to get drawn every frame, of course.
        display.drawActor(batch, 1.0f, player);
        for(AnimatedEntity mon : monsters.keySet()) {
            // monsters are only drawn if within FOV.
            if (fovmap[mon.gridX][mon.gridY] > 0.0) {
                display.drawActor(batch, 1.0f, mon);
            }
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
