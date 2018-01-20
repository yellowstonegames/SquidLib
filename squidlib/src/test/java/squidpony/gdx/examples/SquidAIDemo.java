package squidpony.gdx.examples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import squidpony.squidai.*;
import squidpony.squidgrid.LOS;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.RNG;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class SquidAIDemo extends ApplicationAdapter {
    private enum Phase {MOVE_ANIM, ATTACK_ANIM}
    SpriteBatch batch;

    private Phase phase = Phase.ATTACK_ANIM;
    private RNG rng;
    private SquidLayers display;
    private DungeonGenerator dungeonGen;
    private char[][] bareDungeon, lineDungeon;
    private double[][] res;
    private Color[][] colors, bgColors;
    private int[][] lights;
    private LOS los;
    private int width, height;
    private int cellWidth, cellHeight;
    private int numMonsters = 16;

    private SquidInput input;
    private static final Color bgColor = SColor.DARK_SLATE_GRAY;
    private Array<AnimatedEntity> teamRed, teamBlue;
    private IntArray redHealth, blueHealth;
    //private OrderedMap<AnimatedEntity, Integer> teamRed, teamBlue;
    private OrderedSet<Coord> redPlaces, bluePlaces;
    private Technique redCone, redCloud, blueBlast, blueBeam;
    private DijkstraMap getToRed, getToBlue;
    private Stage stage;
    private int framesWithoutAnimation = 0, moveLength = 5;
    private ArrayList<Coord> awaitedMoves;
    private int redIdx = 0, blueIdx = 0;
    private boolean blueTurn = false;

    @Override
    public void create () {
        batch = new SpriteBatch();
        width = 40;
        height = 40;
        cellWidth = 6;
        cellHeight = 12;
        display = new SquidLayers(width * 2, height, cellWidth, cellHeight, DefaultResources.narrowName);
        display.setAnimationDuration(0.35f);
        stage = new Stage(new ScreenViewport(), batch);

        rng = new RNG(0x1337BEEF);

        dungeonGen = new DungeonGenerator(width, height, rng);
//        dungeonGen.addWater(10);
        //dungeonGen.addDoors(15, true);

        // change the TilesetType to lots of different choices to see what dungeon works best.
        bareDungeon = dungeonGen.generate(TilesetType.ROUND_ROOMS_DIAGONAL_CORRIDORS);
        bareDungeon = DungeonUtility.closeDoors(bareDungeon);
        lineDungeon = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(bareDungeon));
        // it's more efficient to get random floors from a packed set containing only (compressed) floor positions.
        GreasedRegion placement = new GreasedRegion(bareDungeon, '.');


        teamRed = new Array<>(numMonsters);
        teamBlue = new Array<>(numMonsters);

        redHealth = new IntArray(numMonsters);
        blueHealth = new IntArray(numMonsters);

        redPlaces = new OrderedSet<>(numMonsters);
        bluePlaces = new OrderedSet<>(numMonsters);
        for(int i = 0; i < numMonsters; i++)
        {
            Coord monPos = placement.singleRandom(rng);
            placement.remove(monPos);

            teamRed.add(display.animateActor(monPos.x, monPos.y, "50", SColor.RED, true));
            redHealth.add(50);
            redPlaces.add(monPos);

            Coord monPosBlue = placement.singleRandom(rng);
            placement.remove(monPosBlue);

            teamBlue.add(display.animateActor(monPosBlue.x, monPosBlue.y, "50", SColor.COLUMBIA_BLUE, true));
            blueHealth.add(50);
            bluePlaces.add(monPosBlue);
        }
        // your choice of FOV matters here.
        los = new LOS(LOS.BRESENHAM);
        res = DungeonUtility.generateResistances(bareDungeon);

        ConeAOE cone = new ConeAOE(Coord.get(0, 0), 9, 0, 60, Radius.CIRCLE);
        cone.setMinRange(1);
        cone.setMaxRange(2);
        cone.setMetric(Radius.SQUARE);

        redCone = new Technique("Burning Breath", cone);
        redCone.setMap(bareDungeon);

        BlastAOE blast = new BlastAOE(Coord.get(0, 0), 3, Radius.CIRCLE);
        blast.setMinRange(3);
        blast.setMaxRange(5);
        blast.setMetric(Radius.CIRCLE);

        blueBlast = new Technique("Winter Orb", blast);
        blueBlast.setMap(bareDungeon);

        CloudAOE cloud = new CloudAOE(Coord.get(0, 0), 20, Radius.DIAMOND);
        cloud.setMinRange(4);
        cloud.setMaxRange(7);
        cloud.setMetric(Radius.CIRCLE);

        redCloud = new Technique("Acid Mist", cloud);
        redCloud.setMap(bareDungeon);

        BeamAOE beam = new BeamAOE(Coord.get(0, 0), 0.0, 8, 1, Radius.DIAMOND);
        beam.setMinRange(2);
        beam.setMaxRange(8);
        beam.setMetric(Radius.CIRCLE);
        blueBeam = new Technique("Atomic Death Ray", beam);
        blueBeam.setMap(bareDungeon);


        getToRed = new DijkstraMap(bareDungeon, DijkstraMap.Measurement.EUCLIDEAN);
        getToRed.rng = rng;
        getToBlue = new DijkstraMap(bareDungeon, DijkstraMap.Measurement.EUCLIDEAN);
        getToBlue.rng = rng;

        dijkstraAlert();

        awaitedMoves = new ArrayList<>(10);
        colors = MapUtility.generateDefaultColors(bareDungeon);
        bgColors = MapUtility.generateDefaultBGColors(bareDungeon);
        lights = MapUtility.generateLightnessModifiers(bareDungeon);

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
                }
            }
        });
        // ABSOLUTELY NEEDED TO HANDLE INPUT
        // and then add display, our one visual component, to the list of things that act in Stage.
        display.setPosition(0, 0);
        stage.addActor(display);
        Gdx.input.setInputProcessor(input);


    }

    private void dijkstraAlert()
    {
        /*
        getToBlue.clearGoals();
        getToBlue.resetMap();
        getToRed.clearGoals();
        getToRed.resetMap();
        ArrayList<AnimatedEntity> redCopy = new ArrayList<AnimatedEntity>(teamRed.size());
        redCopy.addAll(teamRed.keySet());
        BLUE_LOOP:
        for(AnimatedEntity blue : teamBlue.keySet())
        {
            for(AnimatedEntity red : redCopy)
            {
                if(los.isReachable(res, blue.gridX, blue.gridY, red.gridX, red.gridY, Radius.CIRCLE))
                {
                    getToBlue.setGoal(blue.gridX, blue.gridY);
                    getToRed.setGoal(red.gridX, red.gridY);

                    redCopy.remove(red);

                    continue BLUE_LOOP;
                }
            }
        }
        getToBlue.scan(redPlaces);
        getToRed.scan(bluePlaces);
        */
    }

    /**
     * Move a unit toward a good position to attack, but don't attack in this method.
     * @param idx the index of the unit in the appropriate ordered Map.
     */
    private void startMove(int idx) {
//        if(health <= 0) return;
        int i = 0;
        DijkstraMap whichDijkstra;
        Technique whichTech;
        Set<Coord> whichFoes, whichAllies = new OrderedSet<>(8);
        AnimatedEntity ae = null;
        int health = 0;
        Coord user = null;
        if(blueTurn)
        {
            whichDijkstra = getToRed;
            whichTech = (idx % 2 == 0) ? blueBeam : blueBlast;
            whichFoes = redPlaces;
            whichAllies = bluePlaces;
            ae = teamBlue.get(idx);
            health = blueHealth.get(idx);
            if(ae == null || health <= 0) {
                phase = Phase.MOVE_ANIM;
                return;
            }
            user = Coord.get(ae.gridX, ae.gridY);
        }
        else
        {
            whichDijkstra = getToBlue;
            whichTech = (idx % 2 == 0) ? redCloud : redCone;
            whichFoes = bluePlaces;
            whichAllies = redPlaces;
            ae = teamRed.get(idx);
            health = redHealth.get(idx);
            if(ae == null || health <= 0) {
                phase = Phase.MOVE_ANIM;
                return;
            }
            user = Coord.get(ae.gridX, ae.gridY);
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
        ArrayList<Coord> path = whichDijkstra.findTechniquePath(moveLength, whichTech, bareDungeon, null, whichFoes, whichAllies, user, whichFoes);
        if(path.isEmpty())
            path = whichDijkstra.findPath(moveLength, whichFoes, whichAllies, user, whichFoes.toArray(new Coord[whichFoes.size()]));
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
        awaitedMoves = new ArrayList<>(path);
    }

    public void move(AnimatedEntity ae, int newX, int newY) {
        display.slide(ae, newX, newY, 2, 0.075f);
        phase = Phase.MOVE_ANIM;

    }

    // check if a monster's movement would overlap with another monster.
    @SuppressWarnings("unused")
	private boolean checkOverlap(AnimatedEntity ae, int x, int y)
    {
        for(AnimatedEntity mon : teamRed)
        {
            if(mon.gridX == x && mon.gridY == y && !mon.equals(ae))
                return true;
        }
        for(AnimatedEntity mon : teamBlue)
        {
            if(mon.gridX == x && mon.gridY == y && !mon.equals(ae))
                return true;
        }
        return false;
    }

    private void postMove(int idx) {

        int i = 0;
        Technique whichTech;
        Set<Coord> whichFoes, whichAllies, visibleTargets = new OrderedSet<>(8);
        AnimatedEntity ae = null;
        int health = 0;
        Coord user = null;
        Color whichTint = Color.WHITE;
        Array<AnimatedEntity> whichEnemyTeam;
        IntArray whichEnemyHealth;
        OrderedMap<Coord, Double> effects;
        if (blueTurn) {
            whichTech = (idx % 2 == 0) ? blueBeam : blueBlast;
            whichFoes = redPlaces;
            whichAllies = bluePlaces;
            whichTint = Color.CYAN;
            whichEnemyTeam = teamRed;
            whichEnemyHealth = redHealth;
            ae = teamBlue.get(idx);
            health = blueHealth.get(idx);
            if (ae == null || health <= 0) {
                phase = Phase.ATTACK_ANIM;
                return;
            }
            user = Coord.get(ae.gridX, ae.gridY);
        } else {
            whichTech = (idx % 2 == 0) ? redCloud : redCone;
            whichFoes = bluePlaces;
            whichAllies = redPlaces;
            whichTint = Color.RED;
            whichEnemyTeam = teamBlue;
            whichEnemyHealth = blueHealth;
            ae = teamRed.get(idx);
            health = redHealth.get(idx);
            if (ae == null || health <= 0) {
                phase = Phase.ATTACK_ANIM;
                return;
            }
            user = Coord.get(ae.gridX, ae.gridY);
        }
        for(Coord p : whichFoes)
        {
            AnimatedEntity foe = display.getAnimatedEntityByCell(p.x, p.y);
            int foeIdx;
            if(los.isReachable(res, user.x, user.y, p.x, p.y) && foe != null && (foeIdx = whichEnemyTeam.indexOf(foe, true)) >= 0
                    && whichEnemyHealth.get(foeIdx) > 0)
            {
                visibleTargets.add(p);
            }
        }

        OrderedMap<Coord, ArrayList<Coord>> ideal = whichTech.idealLocations(user, visibleTargets, whichAllies);
        Coord targetCell = null;
        if(!ideal.isEmpty()) targetCell = ideal.firstKey();

        if(targetCell != null)
        {
            effects = whichTech.apply(user, targetCell);

            for(Map.Entry<Coord, Double> power : effects.entrySet())
            {
                Double strength = (idx % 2 == 0) ? rng.nextDouble() : power.getValue();
                whichTint.a = strength.floatValue();
                display.tint(power.getKey().x * 2    , power.getKey().y, whichTint, 0, display.getAnimationDuration());
                display.tint(power.getKey().x * 2 + 1, power.getKey().y, whichTint, 0, display.getAnimationDuration());
                AnimatedEntity tgt;
                for(int tgtIdx = 0; tgtIdx < whichEnemyTeam.size; tgtIdx++)
                {
                    tgt = whichEnemyTeam.get(tgtIdx);
                    if(tgt.gridX == power.getKey().x && tgt.gridY == power.getKey().y)
                    {
                        int currentHealth = Math.max(whichEnemyHealth.get(tgtIdx) - (int) (15 * strength), 0);
                        whichEnemyTeam.set(tgtIdx, tgt);
                        whichEnemyHealth.set(tgtIdx, currentHealth);
                        tgt.setText(Integer.toString(currentHealth));
                    }
                }
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
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                display.put(i * 2, j, lineDungeon[i * 2][j], colors[i][j], bgColors[i][j], lights[i][j]);
                display.put(i * 2 + 1, j, lineDungeon[i * 2 + 1][j], colors[i][j], bgColors[i][j], lights[i][j]);
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

        stage.act();
        boolean blueWins = false, redWins = false;
        for(int bh = 0; bh < blueHealth.size; bh++)
        {
            if(blueHealth.get(bh) > 0) {
                redWins = false;
                break;
            }redWins = true;
        }
        for(int rh = 0; rh < redHealth.size; rh++)
        {
            if(redHealth.get(rh) > 0) {
                blueWins = false;
                break;
            }blueWins = true;
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
        int whichIdx = 0;
        if(blueTurn) {
            whichIdx = blueIdx;
            ae = teamBlue.get(blueIdx);
        }
        else
        {
            whichIdx = redIdx;
            ae = teamRed.get(redIdx);
        }

        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        // if we are waiting for the player's input and get input, process it.
        if(input.hasNext()) {
            input.next();
        }
        // if the user clicked, we have a list of moves to perform.
        if(!awaitedMoves.isEmpty())
        {
            if(ae == null) {
                awaitedMoves.clear();
            }
            // extremely similar to the block below that also checks if animations are done
            // this doesn't check for input, but instead processes and removes Points from awaitedMoves.
            else if(!display.hasActiveAnimations()) {
                ++framesWithoutAnimation;
                if (framesWithoutAnimation >= 2) {
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
            if (framesWithoutAnimation >= 2) {
                framesWithoutAnimation = 0;
                switch (phase) {
                    case ATTACK_ANIM: {
                        phase = Phase.MOVE_ANIM;
                        blueTurn = !blueTurn;
                        if(!blueTurn)
                        {
                            whichIdx = (whichIdx + 1) % numMonsters;
                            redIdx = (redIdx + 1) % numMonsters;
                            blueIdx = (blueIdx + 1) % numMonsters;
                        }
                        dijkstraAlert();
                        startMove(whichIdx);
                    }
                    break;
                    case MOVE_ANIM: {
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

        OrderedSet<AnimatedEntity> entities = display.getAnimatedEntities(0);
        // display does not draw all AnimatedEntities by default.
        batch.begin();
        for (int j = 0; j < entities.size(); j++) {
            display.drawActor(batch, 1.0f, entities.getAt(j), 0);
        }
        entities = display.getAnimatedEntities(2);
        for (int j = 0; j < entities.size(); j++) {
            display.drawActor(batch, 1.0f, entities.getAt(j), 2);
        }
        /*
        for(AnimatedEntity mon : teamBlue.keySet()) {
                display.drawActor(batch, 1.0f, mon);
        }*/
        // batch must end if it began.
        batch.end();
    }
}

