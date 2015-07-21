package squidpony.gdx.examples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import squidpony.SColor;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.gui.gdx.SquidKey;
import squidpony.squidgrid.gui.gdx.SquidLayers;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import java.awt.Point;
import java.util.HashMap;

public class EverythingDemo extends ApplicationAdapter {
    SpriteBatch batch;

    private RNG rng;
    private LightRNG lrng;
    private SquidLayers display;
    private DungeonGenerator dungeonGen;
    private char[][] bareDungeon, lineDungeon;
    private double[][] res;
    private int[][] colors, bgColors, lights;
    private double[][] fovmap, pathMap;
    private Point player;
    private FOV fov;
    private int width, height;
    private SquidKey keyListener;
    private double counter;
    private boolean[][] seen;
    private int health = 7;
    private static final Color bgColor = SquidLayers.awtColorToGDX(SColor.DARK_SLATE_GRAY);
    private HashMap<Point, Integer> monsters;
    private DijkstraMap getToPlayer;
    private Stage stage;

    @Override
    public void create () {
        batch = new SpriteBatch();
        width = 80;
        height = 30;
        // the font will try to load Rogue-Zodiac.ttf from resources. I (Tommy Ettinger) made it, and it's under the
        // same license as SquidLib.
        display = new SquidLayers(width, height, 12, 24);

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
        player = DungeonUtility.randomFloor(placement);
        placement[player.x][player.y] = '@';
        int numMonsters = 25;
        monsters = new HashMap<Point, Integer>(numMonsters);
        for(int i = 0; i < numMonsters; i++)
        {
            Point monPos = DungeonUtility.randomFloor(placement);
            monsters.put(monPos, 0);
            placement[monPos.x][monPos.y] = 'M';
        }
        fov = new FOV(FOV.RIPPLE_TIGHT);
        getToPlayer = new DijkstraMap(bareDungeon, DijkstraMap.Measurement.MANHATTAN);
        getToPlayer.rng = rng;
        getToPlayer.setGoal(player);
        pathMap = getToPlayer.scan(null);
        res = DungeonUtility.generateResistances(bareDungeon);
        fovmap = fov.calculateFOV(res, player.x, player.y, 8);
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
        if (player.x + xmod >= 0 && player.y + ymod >= 0 && player.x + xmod < width && player.y + ymod < height
                && bareDungeon[player.x + xmod][player.y + ymod] != '#') {
            // '+' is a door.
            if (lineDungeon[player.x + xmod][player.y + ymod] == '+') {
                bareDungeon[player.x + xmod][player.y + ymod] = '/';
                lineDungeon[player.x + xmod][player.y + ymod] = '/';
                // changes to the map mean the resistances for FOV need to be regenerated.
                res = DungeonUtility.generateResistances(bareDungeon);
            } else {
                player.move(player.x + xmod, player.y + ymod);
            }
            if(monsters.containsKey(player))
            {
                // this doesn't remove the player, it removes the monster that just got run over by the player.
                monsters.remove(player);
            }
            // The next two lines are important to avoid monsters treating cells the player WAS in as goals.
            getToPlayer.clearGoals();
            getToPlayer.resetMap();
            // now that goals are cleared, we can mark the current player position as a goal.
            getToPlayer.setGoal(player);
            // this is an important piece of DijkstraMap usage; the argument is a Set of Points for squares that
            // temporarily cannot be moved through (not walls, which are automatically known because the map char[][]
            // was passed to the DijkstraMap constructor, but things like moving creatures and objects).
            pathMap = getToPlayer.scan(monsters.keySet());

            // recalculate FOV, store it in fovmap for the render to use.
            fovmap = fov.calculateFOV(res, player.x, player.y, 8);
            HashMap<Point, Integer> newMons = new HashMap<Point, Integer>(monsters.size());
            // handle monster turns
            for(HashMap.Entry<Point, Integer> mon : monsters.entrySet())
            {
                // monster values are used to store their aggression, 1 for actively stalking the player, 0 for not.
                if(mon.getValue() > 0 || fovmap[mon.getKey().x][mon.getKey().y] > 0.1)
                {
                    // this block is used to ensure that the monster picks the best path, or a random choice if there
                    // is more than one equally good best option.
                    Direction choice = null;
                    double best = 9999.0;
                    for(Direction d : getToPlayer.shuffle(Direction.CARDINALS))
                    {
                        Point tmp = new Point(mon.getKey().x + d.deltaX, mon.getKey().y + d.deltaY);
                        if(pathMap[tmp.x][tmp.y] < best &&
                                !monsters.containsKey(tmp) && !newMons.containsKey(tmp))
                        {
                            // pathMap is a 2D array of doubles where 0 is the goal (the player).
                            // we use best to store which option is closest to the goal.
                            best = pathMap[tmp.x][tmp.y];
                            choice = d;
                        }
                    }
                    if(choice != null)
                    {
                        Point tmp = new Point(mon.getKey().x + choice.deltaX, mon.getKey().y + choice.deltaY);
                        // if we would move into the player, instead damage the player and give newMons the current
                        // position of this monster.
                        if(player.equals(tmp))
                        {
                            health--;
                            newMons.put(mon.getKey(), 1);
                        }
                        // otherwise store the new position in newMons.
                        else
                            newMons.put(tmp, 1);
                    }
                    else
                    {
                        newMons.put(mon.getKey(), 1);
                    }
                }
                else
                {
                    newMons.put(mon.getKey(), mon.getValue());
                }
            }
            monsters = newMons;
        }
    }

    @Override
    public void render () {
        Gdx.gl.glClearColor(bgColor.r / 255.0f, bgColor.g / 255.0f, bgColor.b / 255.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        counter += Gdx.graphics.getDeltaTime() * 15;
        lights = DungeonUtility.generateLightnessModifiers(bareDungeon, counter);

        if (health <= 0) {
            display.putBoxedString(width / 2 - 11, height / 2 - 1, "YOU HAVE BEEN EATEN!");
            display.putBoxedString(width / 2 - 11, height / 2 + 5, "     q to quit.     ");
            return;
        }
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
        // the player doesn't care what was already rendered at its cell on the map.  30 is dark purple.
        display.put(player.x, player.y, Character.forDigit(health, 10), 30);

        for(Point mon : monsters.keySet()) {
            if (fovmap[mon.x][mon.y] > 0.0) {
                display.put(mon.x, mon.y, 'M', 11);
            }
        }
        if(keyListener.hasNext())
            keyListener.next();
//        batch.begin();

        stage.draw();

//        batch.end();
    }
}
