package squidpony.gdx.examples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import squidpony.squidai.DijkstraMap;
import squidpony.squidai.Technique;
import squidpony.squidai.Threat;
import squidpony.squidgrid.FOVCache;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.SerpentMapGenerator;
import squidpony.squidmath.*;

import java.util.*;

public class CoveredPathDemo extends ApplicationAdapter {
    private enum Phase {MOVE_ANIM, ATTACK_ANIM}
    private static class Creature
    {
        public AnimatedEntity entity;
        public int health;
        public DijkstraMap dijkstra;
        public ArrayList<Coord> previousPositions;

        public Creature(AnimatedEntity ae, int hp, DijkstraMap dijkstraMap)
        {
            entity = ae;
            health = hp;
            dijkstra = dijkstraMap;
            previousPositions = new ArrayList<>(16);
        }
    }
    SpriteBatch batch;

    private Phase phase = Phase.ATTACK_ANIM;
    private RNG rng;
    private LightRNG lrng;
    private SquidLayers display;
    private DungeonGenerator dungeonGen;
    private char[][] bareDungeon, lineDungeon, decoDungeon;
    private double[][] res;
    private int[][] colors, bgColors, lights;
    private int width, height;
    private int cellWidth, cellHeight;
    private int numMonsters = 25;
    private Radius radiusKind = Radius.DIAMOND;

    private SquidInput input;
    private static final Color bgColor = SColor.DARK_SLATE_GRAY;
    private ArrayList<Creature> teamRed, teamBlue;
    private OrderedSet<Coord> redPlaces, bluePlaces;
    private OrderedSet<Threat> redThreats, blueThreats;
    private DijkstraMap getToRed, getToBlue;
    private Stage stage;
    private int framesWithoutAnimation = 0, moveLength = 6;
    private ArrayList<Coord> awaitedMoves;
    private int scheduledMoves = 0, whichIdx = 0;
    private boolean blueTurn = false;
    private double frames = 0.0;
    boolean late = false;

    private FOVCache cache;
    @Override
    public void create () {
        batch = new SpriteBatch();
        width = 60;
        height = 50;
        cellWidth = 6;
        cellHeight = 12;
        display = new SquidLayers(width, height, cellWidth, cellHeight, DefaultResources.narrowName);
        display.setAnimationDuration(0.09f);
        display.addExtraLayer();
        stage = new Stage(new ScreenViewport(), batch);

        lrng = new LightRNG(0x1337BEEF);
        rng = new RNG(lrng);

        dungeonGen = new DungeonGenerator(width, height, rng);
//        dungeonGen.addWater(10);
        //dungeonGen.addDoors(15, true);
        dungeonGen.addBoulders(25);
        SerpentMapGenerator serpent = new SerpentMapGenerator(width, height, rng);
        serpent.putWalledBoxRoomCarvers(1);
        // change the TilesetType to lots of different choices to see what dungeon works best.
        dungeonGen.generate(serpent.generate());
        bareDungeon = dungeonGen.getBareDungeon();
        decoDungeon = dungeonGen.getDungeon();
        cache = new FOVCache(bareDungeon, 9, radiusKind);
        lineDungeon = DungeonUtility.hashesToLines(decoDungeon);
        // it's more efficient to get random floors from a packed set containing only (compressed) floor positions.
        short[] placement = CoordPacker.pack(bareDungeon, '.');

        teamRed = new ArrayList<>(numMonsters);
        teamBlue = new ArrayList<>(numMonsters);

        redPlaces = new OrderedSet<>(numMonsters);
        bluePlaces = new OrderedSet<>(numMonsters);

        redThreats = new OrderedSet<>(numMonsters);
        blueThreats = new OrderedSet<>(numMonsters);
        for(int i = 0; i < numMonsters; i++)
        {
            Coord monPos = dungeonGen.utility.randomCell(placement);
            placement = CoordPacker.removePacked(placement, monPos.x, monPos.y);

            teamRed.add(new Creature(display.animateActor(monPos.x, monPos.y, "9", 11), 9,
                    new DijkstraMap(bareDungeon, DijkstraMap.Measurement.MANHATTAN, rng)));
            redPlaces.add(monPos);
            redThreats.add(new Threat(monPos, 0, 1));

            Coord monPosBlue = dungeonGen.utility.randomCell(placement);
            placement = CoordPacker.removePacked(placement, monPosBlue.x, monPosBlue.y);

            teamBlue.add(new Creature(display.animateActor(monPosBlue.x, monPosBlue.y, "9", 25), 9,
                    new DijkstraMap(bareDungeon, DijkstraMap.Measurement.MANHATTAN, rng)));
            bluePlaces.add(monPosBlue);
            blueThreats.add(new Threat(monPosBlue, 3, 5));
        }
        res = DungeonUtility.generateResistances(bareDungeon);

        getToRed = new DijkstraMap(bareDungeon, DijkstraMap.Measurement.MANHATTAN);
        getToRed.rng = rng;
        getToBlue = new DijkstraMap(bareDungeon, DijkstraMap.Measurement.MANHATTAN);
        getToBlue.rng = rng;

        awaitedMoves = new ArrayList<>(10);
        colors = DungeonUtility.generatePaletteIndices(decoDungeon);
        bgColors = DungeonUtility.generateBGPaletteIndices(decoDungeon);
        lights = DungeonUtility.generateLightnessModifiers(decoDungeon, frames);

        // just quit if we get a Q.
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
                    break;
                    default:
                        scheduledMoves++;
                }
            }
        });
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        // and then add display, our one visual component, to the list of things that act in Stage.
        display.setPosition(0, 0);
        stage.addActor(display);
        cache.awaitCache();
        Gdx.input.setInputProcessor(input);


    }

    /**
     * Move a unit toward a good position to attack, but don't attack in this method.
     * @param idx the index of the unit in the appropriate ordered Map.
     */
    private void startMove(int idx) {
//        if(health <= 0) return;
        int i = 0, myMin, myMax;
        DijkstraMap whichDijkstra = null;
        Technique whichTech;
        Set<Coord> whichFoes, whichAllies;
        OrderedSet<Threat> whichThreats;
        AnimatedEntity ae = null;
        int health = 0;
        Coord user = null;
        if(blueTurn) {
            whichFoes = redPlaces;
            whichAllies = bluePlaces;
            whichThreats = redThreats;
            myMin = 3;
            myMax = 5;

            Creature entry = teamBlue.get(idx);
            ae = entry.entity;
            health = entry.health;
            whichDijkstra = entry.dijkstra;
            user = Coord.get(ae.gridX, ae.gridY);
            entry.previousPositions.add(user);
        }
        else {
            whichFoes = bluePlaces;
            whichAllies = redPlaces;
            whichThreats = blueThreats;
            myMin = 0;
            myMax = 1;
            Creature entry = teamRed.get(idx);
            ae = entry.entity;
            health = entry.health;
            whichDijkstra = entry.dijkstra;
            user = Coord.get(ae.gridX, ae.gridY);
            entry.previousPositions.add(user);
        }
        if(whichDijkstra == null || health <= 0) {
            phase = Phase.MOVE_ANIM;
            return;
        }
        whichAllies.remove(user);
        /*for(Coord p : whichFoes)
        {
            AnimatedEntity foe = display.getAnimatedEntityByCell(p.x, p.y);
            if(los.isReachable(res, user.x, user.y, p.x, p.y) && foe != null && whichEnemyTeam.get(foe) != null && whichEnemyTeam.get(foe) > 0)
            {
                visibleTargets.add(p);
            }
        }*/
        ArrayList<Coord> path = whichDijkstra.findCoveredAttackPath(moveLength, myMin, myMax, 1.0, cache, true, whichFoes,
                whichAllies, whichThreats, user, whichFoes.toArray(new Coord[whichFoes.size()]));
        /*
        System.out.println("User at (" + user.x + "," + user.y + ") using " +
                whichTech.name);
        */
        /*
        boolean anyFound = false;

        for (int yy = 0; yy < height; yy++) {
            for (int xx = 0; xx < width; xx++) {
                System.out.print((whichDijkstra.targetMap[xx][yy] == null) ? "." : "@");
                anyFound = (whichDijkstra.targetMap[xx][yy] != null) ? true : anyFound;
            }
            System.out.println();
        }*/
        if(path.isEmpty())
            path.add(user);
        awaitedMoves = new ArrayList<>(path);
    }

    public void move(AnimatedEntity ae, int newX, int newY) {
        display.slide(ae, newX, newY);
        phase = Phase.MOVE_ANIM;

    }

    // check if a monster's movement would overlap with another monster.
    @SuppressWarnings("unused")
	private boolean checkOverlap(AnimatedEntity ae, int x, int y)
    {
        for(Creature mon : teamRed)
        {
            if(mon.entity.gridX == x && mon.entity.gridY == y && !mon.entity.equals(ae))
                return true;
        }
        for(Creature mon : teamBlue)
        {
            if(mon.entity.gridX == x && mon.entity.gridY == y && !mon.entity.equals(ae))
                return true;
        }
        return false;
    }

    private void postMove(int idx) {

        int i = 0, myMax, myMin;
        OrderedSet<Coord> whichFoes, whichAllies, visibleTargets = new OrderedSet<>(8);
        AnimatedEntity ae = null;
        int health = 0;
        Coord user = null;
        DijkstraMap dijkstra = null;
        ArrayList<Coord> previous = null;
        Color whichTint = Color.WHITE;
        ArrayList<Creature> whichEnemyTeam;
        OrderedSet<Threat> myThreats, enemyThreats;
        if (blueTurn) {
            whichFoes = redPlaces;
            whichAllies = bluePlaces;
            whichTint = Color.CYAN;
            whichEnemyTeam = teamRed;
            myThreats = blueThreats;
            enemyThreats = redThreats;
            myMin = 3;
            myMax = 5;
            Creature entry = teamBlue.get(idx);
            ae = entry.entity;
            health = entry.health;
            dijkstra = entry.dijkstra;
            previous = entry.previousPositions;
            user = Coord.get(ae.gridX, ae.gridY);
        } else {
            whichFoes = bluePlaces;
            whichAllies = redPlaces;
            whichTint = Color.RED;
            whichEnemyTeam = teamBlue;
            myThreats = redThreats;
            enemyThreats = blueThreats;
            myMin = 0;
            myMax = 1;

            Creature entry = teamRed.get(idx);
            ae = entry.entity;
            health = entry.health;
            dijkstra = entry.dijkstra;
            previous = entry.previousPositions;
            user = Coord.get(ae.gridX, ae.gridY);
        }
        myThreats.getAt(idx).position = user;

        if (health <= 0 || dijkstra == null) {
            myThreats.getAt(idx).reach.maxDistance = 0;
            myThreats.getAt(idx).reach.minDistance = 0;
            phase = Phase.ATTACK_ANIM;
            return;
        }
        dijkstra.deteriorate(previous);
        for(Creature creature : whichEnemyTeam)
        {
            if(cache.queryLOS(user.x, user.y, creature.entity.gridX, creature.entity.gridY) &&
                    creature.health > 0 &&
                    radiusKind.radius(user.x, user.y, creature.entity.gridX, creature.entity.gridY) <= myMax &&
                    radiusKind.radius(user.x, user.y, creature.entity.gridX, creature.entity.gridY) >= myMin)
            {
                visibleTargets.add(Coord.get(creature.entity.gridX, creature.entity.gridY));
            }
        }
        Coord targetCell = null;
        for(Coord vt : visibleTargets)
        {
            targetCell = vt;
            break;
        }

        if(targetCell != null) {
            whichTint.a = 0.5f;
            int successfulKill = -1, ix = 0;
            display.tint(targetCell.x, targetCell.y, whichTint, 0, display.getAnimationDuration());
            for (Creature mon : whichEnemyTeam) {
                if (mon.entity.gridX == targetCell.x && mon.entity.gridY == targetCell.y) {
                    int currentHealth = Math.max(mon.health - 3, 0);
                    mon.health = currentHealth;
                    if (currentHealth <= 0) {
                        successfulKill = ix;
                    }
                    mon.entity.setText(Integer.toString(currentHealth));
                }
                ix++;
            }
            if (successfulKill >= 0 && successfulKill < enemyThreats.size()) {
                Threat succ = enemyThreats.getAt(successfulKill);
                enemyThreats.remove(succ);
                Coord deadPos = succ.position;
                AnimatedEntity deadAE = whichEnemyTeam.get(successfulKill).entity;
                display.removeAnimatedEntity(deadAE);
                whichFoes.remove(deadPos);
            }

        }
        /*
        else
        {

            System.out.println("NO ATTACK POSITION: User at (" + user.x + "," + user.y + ") using " +
                    whichTech.name);

            display.tint(user.x * 2    , user.y, highlightColor, 0, display.getAnimationDuration() * 3);
            display.tint(user.x * 2 + 1, user.y, highlightColor, 0, display.getAnimationDuration() * 3);
        }
        */
        whichAllies.add(user);

        phase = Phase.ATTACK_ANIM;
    }
    public void putMap()
    {
        lights = DungeonUtility.generateLightnessModifiers(decoDungeon, frames);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                display.put(i, j, lineDungeon[i][j], colors[i][j], bgColors[i][j], lights[i][j]);
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
        frames  += Gdx.graphics.getDeltaTime() * 15;
        stage.act();
        boolean blueWins = false, redWins = false;
        for(Creature blueHealth : teamBlue)
        {
            if(blueHealth.health > 0) {
                redWins = false;
                break;
            }
            redWins = true;
        }
        for(Creature redHealth : teamRed)
        {
            if(redHealth.health > 0) {
                blueWins = false;
                break;
            }
            blueWins = true;
        }
        if (blueWins) {
            // still need to display the map, then write over it with a message.
            putMap();
            display.putBoxedString(width / 2 - 11, height / 2 - 1, "  BLUE TEAM WINS!  ");
            display.putBoxedString(width / 2 - 11, height / 2 + 5, "     q to quit.    ");

            // because we return early, we still need to draw.
            stage.draw();
            // q still needs to quit.
            if(input.hasNext())
                input.next();
            return;
        }
        else if(redWins)
        {
            putMap();
            display.putBoxedString(width / 2 - 11, height / 2 - 1, "   RED TEAM WINS!  ");
            display.putBoxedString(width / 2 - 11, height / 2 + 5, "     q to quit.    ");

            // because we return early, we still need to draw.
            stage.draw();
            // q still needs to quit.
            if(input.hasNext())
                input.next();
            return;
        }
        int i = 0;
        AnimatedEntity ae = null;
        if(blueTurn) {
            Creature entry = teamBlue.get(whichIdx);
            ae = entry.entity;

        }
        else {
            Creature entry = teamRed.get(whichIdx);
            ae = entry.entity;
        }

        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        // if we are waiting for the player's input and get input, process it.
        if(input.hasNext()) {
            input.next();
        }
        if(!awaitedMoves.isEmpty())
        {
            if(ae == null) {
                awaitedMoves.clear();
            }
            // extremely similar to the block below that also checks if animations are done
            // this doesn't check for input, but instead processes and removes Points from awaitedMoves.
            else if(!display.hasActiveAnimations()) {
                ++framesWithoutAnimation;
                if (framesWithoutAnimation >= 3) {
                    framesWithoutAnimation = 0;
                    Coord m = awaitedMoves.remove(0);
                    move(ae, m.x, m.y);
                }
            }
        }
        // if the previous blocks didn't happen, and there are no active animations, then either change the phase
        // (because with no animations running the last phase must have ended), or start a new animation soon.
        else if(!display.hasActiveAnimations()) {
            ++framesWithoutAnimation;
            if (framesWithoutAnimation >= 6) {// && scheduledMoves > 0) {
                //System.out.println("frames: " + framesWithoutAnimation + "scheduled: " + scheduledMoves);
                framesWithoutAnimation = 0;
                switch (phase) {
                    case ATTACK_ANIM: {
                        phase = Phase.MOVE_ANIM;
                        blueTurn = !blueTurn;
                        if(!blueTurn)
                        {
                            if(whichIdx + 1 >= numMonsters)
                                late = true;
                            whichIdx = (whichIdx + 1) % numMonsters;
                        }
                        startMove(whichIdx);
                    }
                    break;
                    case MOVE_ANIM: {
                        //scheduledMoves = Math.max(scheduledMoves - 1, 0);
                        postMove(whichIdx);
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

        // disolay does not draw all AnimatedEntities by default.
        batch.begin();
        for(AnimatedEntity mon : display.getAnimatedEntities(0)) {
            display.drawActor(batch, 1.0f, mon, 0);
        }
        for(AnimatedEntity mon : display.getAnimatedEntities(2)) {
            display.drawActor(batch, 1.0f, mon, 2);
        }
        /*
        for(AnimatedEntity mon : teamBlue.keySet()) {
                display.drawActor(batch, 1.0f, mon);
        }*/
        // batch must end if it began.
        batch.end();
    }
}

