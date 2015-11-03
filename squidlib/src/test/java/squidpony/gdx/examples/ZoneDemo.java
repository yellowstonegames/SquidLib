package squidpony.gdx.examples;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import squidpony.squidai.ZOI;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.SerpentMapGenerator;
import squidpony.squidmath.Coord;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.PoissonDisk;
import squidpony.squidmath.RNG;

import java.util.ArrayList;

public class ZoneDemo extends ApplicationAdapter {
    private enum Phase {MOVE_ANIM, WAIT_ANIM}
    SpriteBatch batch;

    private Phase phase = Phase.WAIT_ANIM;
    private RNG rng;
    private LightRNG lrng;
    private SquidLayers display;
    private DungeonGenerator dungeonGen;
    private char[][] bareDungeon, lineDungeon;
    private double[][] res;
    private int[][] lights;
    private Color[][] bgColors;
    private Color[] influenceColors;
    private ZOI zoi;
    private short[][] packedInfluences;
    private int width, height;
    private int cellWidth, cellHeight;
    private Coord[] centers, shiftedCenters;
    private AnimatedEntity[] centerEntities;
    private SquidInput input;
    private static final Color bgColor = SColor.DARK_SLATE_GRAY, textColor = SColor.SLATE_GRAY;
    private Stage stage;
    private int framesWithoutAnimation = 0;
    private ArrayList<Coord> awaitedMoves;
    private SquidColorCenter colorCenter;
    @Override
    public void create () {
        batch = new SpriteBatch();
        width = 80;
        height = 60;
        cellWidth = 6;
        cellHeight = 12;
        display = new SquidLayers(width * 2, height, cellWidth, cellHeight, DefaultResources.narrowName);
        display.setAnimationDuration(0.15f);
        display.addExtraLayer();
        stage = new Stage(new ScreenViewport(), batch);

        lrng = new LightRNG(0xBADBEEF);
        rng = new RNG(lrng);

        dungeonGen = new DungeonGenerator(width, height, rng);
//        dungeonGen.addWater(10);
        //dungeonGen.addDoors(15, true);
        SerpentMapGenerator serpent = new SerpentMapGenerator(width, height, rng);
        serpent.putBoxRoomCarvers(4);
        serpent.putRoundRoomCarvers(2);
        serpent.putCaveCarvers(4);
        bareDungeon = dungeonGen.generate(serpent.generate());
        bareDungeon = DungeonUtility.closeDoors(bareDungeon);
        lineDungeon = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(bareDungeon));

        ArrayList<Coord> temp = PoissonDisk.sampleMap(bareDungeon, 8.0f, rng, '#', '+', '/');
        centers = temp.toArray(new Coord[temp.size()]);
        shiftedCenters = temp.toArray(new Coord[temp.size()]);

        colorCenter = DefaultResources.getSCC();
        influenceColors = new Color[centers.length];
        centerEntities = new AnimatedEntity[centers.length];
        for (int i = 0; i < centers.length; i++) {
            float hue = i * 1.0f / centers.length, sat = rng.nextFloat() * 0.5f + 0.5f,
                    val = rng.nextFloat() * 0.3f + 0.7f;
            influenceColors[i] = colorCenter.getHSV(hue, sat, val);

            centerEntities[i] = display.animateActor(centers[i].x, centers[i].y, '@',
                    colorCenter.getHSV(hue, sat - 0.2f, val - 0.4f), true);
        }
        zoi = new ZOI(centers, bareDungeon, Radius.DIAMOND);
        packedInfluences = zoi.calculate();

        bgColors = new Color[width][height];
        recolorZones();
        lights = DungeonUtility.generateLightnessModifiers(bareDungeon);

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

    public void move() {
        for (int i = 0; i < centers.length; i++) {
            AnimatedEntity ae = centerEntities[i];
            Direction[] dirs = rng.shuffle(Direction.CARDINALS);
            for (int j = 0; j < dirs.length; j++) {
                int newX = ae.gridX + dirs[j].deltaX, newY = ae.gridY + dirs[j].deltaY;
                if (bareDungeon[newX][newY] != '#' &&
                        Radius.DIAMOND.radius(centers[i].x, centers[i].y,
                                newX, newY) <= 4.0) {
                    display.slide(ae, newX, newY);
                    shiftedCenters[i] = Coord.get(newX, newY);
                    break;
                }
            }
        }
        phase = Phase.MOVE_ANIM;

    }
    public void recolorZones()
    {
        zoi = new ZOI(shiftedCenters, bareDungeon, Radius.DIAMOND);
        packedInfluences = zoi.calculate();
        Coord c;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                c = Coord.get(x, y);
                int[] inf = zoi.nearestInfluences(packedInfluences, c);
                if(inf.length == 0) {
                    bgColors[x][y] = bgColor;
                }
                else if(inf.length == 1)
                {
                    bgColors[x][y] = influenceColors[inf[0]];
                }
                else
                {
                    float hue = 0f, sat = 0f, val = 0f;
                    for (int i = 0; i < inf.length; i++) {
                        hue += colorCenter.getHue(influenceColors[inf[i]]);
                        sat += colorCenter.getSaturation(influenceColors[inf[i]]);
                        val += colorCenter.getValue(influenceColors[inf[i]]);
                    }
                    bgColors[x][y] = colorCenter.getHSV(hue / inf.length, sat / inf.length, val / inf.length);
                }
            }
        }
    }

    private void postMove() {
        recolorZones();
        phase = Phase.WAIT_ANIM;
    }
    public void putMap()
    {
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                display.put(i * 2, j, lineDungeon[i * 2][j], textColor, bgColors[i][j], lights[i][j]);
                display.put(i * 2 + 1, j, lineDungeon[i * 2 + 1][j], textColor, bgColors[i][j], lights[i][j]);
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

        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        // if we are waiting for the player's input and get input, process it.
        if(input.hasNext()) {
            input.next();
        }
        if(!display.hasActiveAnimations()) {
            ++framesWithoutAnimation;
            if (framesWithoutAnimation >= 5) {
                framesWithoutAnimation = 0;
                switch (phase) {
                    case WAIT_ANIM: {
                        move();
                    }
                    break;
                    case MOVE_ANIM: {
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

        // disolay does not draw all AnimatedEntities by default.
        batch.begin();
        for(AnimatedEntity mon : display.getAnimatedEntities(2)) {
            display.drawActor(batch, 1.0f, mon);
        }
        /*
        for(AnimatedEntity mon : teamBlue.keySet()) {
                display.drawActor(batch, 1.0f, mon);
        }*/
        // batch must end if it began.
        batch.end();
    }
}

