package squidpony.gdx.examples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import squidpony.SColor;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.gui.gdx.AnimatedEntity;
import squidpony.squidgrid.gui.gdx.SquidKey;
import squidpony.squidgrid.gui.gdx.SquidLayers;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class EverythingDemo extends ApplicationAdapter {
    private enum Phase {WAIT, PLAYER_ANIM, MONSTER_ANIM}
    SpriteBatch batch;

    private Phase phase = Phase.WAIT;
    private RNG rng;
    private LightRNG lrng;
    private SquidLayers display;
    private DungeonGenerator dungeonGen;
    private char[][] bareDungeon, lineDungeon;
    private double[][] res;
    private int[][] colors, bgColors, lights;
    private double[][] fovmap, pathMap;
    private AnimatedEntity player;
    private FOV fov;
    private int width, height;
    private int cellWidth, cellHeight;
    private SquidKey keyListener;
    private double counter;
    private boolean[][] seen;
    private int health = 7;
    private static final Color bgColor = SquidLayers.awtColorToGDX(SColor.DARK_SLATE_GRAY);
    private HashMap<AnimatedEntity, Integer> monsters;
    private DijkstraMap getToPlayer;
    private Stage stage;
    private int framesWithoutAnimation = 0;
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

        stage = new Stage(new ScreenViewport(), batch);

        counter = 0;
        lrng = new LightRNG(0x1337BEEF);
        rng = new RNG(lrng);

        // this is important if you use a seeded RNG.
//        DungeonUtility.rng = rng;
        dungeonGen = new DungeonGenerator(width, height, rng);
        dungeonGen.addWater(10);
        dungeonGen.addDoors(15, true);

        // change the TilesetType to lots of different choices to see what dungeon works best.
        bareDungeon = dungeonGen.generate(TilesetType.DEFAULT_DUNGEON);
        bareDungeon = DungeonUtility.closeDoors(bareDungeon);
        lineDungeon = DungeonUtility.hashesToLines(bareDungeon);
        char[][] placement = DungeonUtility.closeDoors(bareDungeon);
        Coord pl = DungeonUtility.randomFloor(placement);
        placement[pl.x][pl.y] = '@';
        int numMonsters = 25;
        monsters = new HashMap<AnimatedEntity, Integer>(numMonsters);
        for(int i = 0; i < numMonsters; i++)
        {
            Coord monPos = DungeonUtility.randomFloor(placement);
            placement[monPos.x][monPos.y] = 'M';
            monsters.put(display.animateActor(monPos.x, monPos.y, 'M', 11), 0);

        }
        fov = new FOV(FOV.RIPPLE_TIGHT);
        getToPlayer = new DijkstraMap(bareDungeon, DijkstraMap.Measurement.MANHATTAN);
        getToPlayer.rng = rng;
        getToPlayer.setGoal(pl);
        pathMap = getToPlayer.scan(null);
        res = DungeonUtility.generateResistances(bareDungeon);
        fovmap = fov.calculateFOV(res, pl.x, pl.y, 8);

        player = display.animateActor(pl.x, pl.y, Character.forDigit(health, 10), 30);
        colors = DungeonUtility.generatePaletteIndices(bareDungeon);
        bgColors = DungeonUtility.generateBGPaletteIndices(bareDungeon);
        lights = DungeonUtility.generateLightnessModifiers(bareDungeon, counter);
        seen = new boolean[width][height];

        keyListener = new SquidKey(new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode)
                {
                    case Keys.UP:
                    case Keys.K:
                    case Keys.NUM_8:
                    case Keys.W:
                    {
                        move(0, -1);
                        break;
                    }
                    case Keys.DOWN:
                    case Keys.J:
                    case Keys.NUM_2:
                    case Keys.S:
                    {
                        move(0, 1);
                        break;
                    }
                    case Keys.LEFT:
                    case Keys.H:
                    case Keys.NUM_4:
                    case Keys.A:
                    {
                        move(-1, 0);
                        break;
                    }
                    case Keys.RIGHT:
                    case Keys.L:
                    case Keys.NUM_6:
                    case Keys.D:
                    {
                        move(1, 0);
                        break;
                    }
                    case Keys.Q:
                    case Keys.ESCAPE:
                    {
                        Gdx.app.exit();
                    }
                }
                return false;
            }

            @Override
            public boolean keyUp(int keycode) {
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return false;
            }

            @Override
            public boolean scrolled(int amount) {
                return false;
            }
        });
        Gdx.input.setInputProcessor(keyListener);
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
        int newX = player.gridX + xmod, newY = player.gridY + ymod;
        if (newX >= 0 && newY >= 0 && newX < width && newY < height
                && bareDungeon[newX][newY] != '#') {
            // '+' is a door.
            if (lineDungeon[newX][newY] == '+') {
                bareDungeon[newX][newY] = '/';
                lineDungeon[newX][newY] = '/';
                // changes to the map mean the resistances for FOV need to be regenerated.
                res = DungeonUtility.generateResistances(bareDungeon);
            } else {
//                display.put(player.x, player.y, Character.forDigit(health, 10), 30);
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
            monplaces.add(new Coord(ae.gridX, ae.gridY));
        }
        pathMap = getToPlayer.scan(monplaces);

        // recalculate FOV, store it in fovmap for the render to use.
        fovmap = fov.calculateFOV(res, player.gridX, player.gridY, 8);
        // handle monster turns
        ArrayList<Coord> nextMovePositions = new ArrayList<>(25);
        for(HashMap.Entry<AnimatedEntity, Integer> mon : monsters.entrySet())
        {
            // monster values are used to store their aggression, 1 for actively stalking the player, 0 for not.
            if(mon.getValue() > 0 || fovmap[mon.getKey().gridX][mon.getKey().gridY] > 0.1)
            {
                // this block is used to ensure that the monster picks the best path, or a random choice if there
                // is more than one equally good best option.
                Direction choice = null;
                double best = 9999.0;
                for(Direction d : getToPlayer.shuffle(Direction.CARDINALS))
                {
                    Coord tmp = new Coord(mon.getKey().gridX + d.deltaX, mon.getKey().gridY + d.deltaY);
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
                    Coord tmp = new Coord(mon.getKey().gridX + choice.deltaX, mon.getKey().gridY + choice.deltaY);
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
                        nextMovePositions.add(new Coord(tmp.x, tmp.y));
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
    public void putMap()
    {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // if we see it now, we remember the cell and show a lit cell based on the fovmap value (between 0.0
                // and 1.0), with 1.0 being almost pure white at +150 lightness and 0.0 being rather dark at -100.
                if (fovmap[i][j] > 0.0) {
                    seen[i][j] = true;
                    display.put(i, j, lineDungeon[i][j], colors[i][j], bgColors[i][j],
                            lights[i][j] + (int) (-100 + 250 * fovmap[i][j]));
                    // if we don't see it now, but did earlier, use a very dark background, but lighter than black.
                } else if (seen[i][j]) {
                    display.put(i, j, lineDungeon[i][j], colors[i][j], bgColors[i][j], -140);
                }
            }
        }
    }
    @Override
    public void render () {
        Gdx.gl.glClearColor(bgColor.r / 255.0f, bgColor.g / 255.0f, bgColor.b / 255.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        stage.act(Gdx.graphics.getDeltaTime());
        display.act(Gdx.graphics.getDeltaTime());

        counter += Gdx.graphics.getDeltaTime() * 15;
        lights = DungeonUtility.generateLightnessModifiers(bareDungeon, counter);

        if (health <= 0) {
            putMap();
            display.putBoxedString(width / 2 - 11, height / 2 - 1, "YOU HAVE BEEN EATEN!");
            display.putBoxedString(width / 2 - 11, height / 2 + 5, "     q to quit.     ");

            stage.draw();
            if(keyListener.hasNext())
                keyListener.next();
            return;
        }
        putMap();
        if(keyListener.hasNext() && !display.hasActiveAnimations() && phase == Phase.WAIT) {
            keyListener.next();
        }
        else if(!display.hasActiveAnimations()) {
            ++framesWithoutAnimation;
            if (framesWithoutAnimation > 5) {
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
        else
        {
            framesWithoutAnimation = 0;
        }
//        batch.begin();

        stage.draw();

        batch.begin();
        display.drawActor(batch, 1.0f, player);
        for(AnimatedEntity mon : monsters.keySet()) {
            if (fovmap[mon.gridX][mon.gridY] > 0.0) {
                display.drawActor(batch, 1.0f, mon);
            }
        }
        batch.end();

//        batch.end();
    }
}
