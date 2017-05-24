package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import squidpony.ArrayTools;
import squidpony.annotation.Beta;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.SeededNoise;

import java.util.List;

/**
 * Created by Tommy Ettinger on 5/24/2017.
 */
@Beta
public abstract class PanelEffect extends TemporalAction{
    public SquidPanel target;
    public float completion = 0.0f;
    public GreasedRegion validCells;

    protected PanelEffect(SquidPanel targeting)
    {
        target = targeting;
        validCells = new GreasedRegion(targeting.gridWidth(), targeting.gridHeight()).allOn();
    }
    protected PanelEffect(SquidPanel targeting, float duration)
    {
        target = targeting;
        setDuration(duration);
        validCells = new GreasedRegion(targeting.gridWidth(), targeting.gridHeight()).allOn();
    }
    protected PanelEffect(SquidPanel targeting, float duration, GreasedRegion valid)
    {
        target = targeting;
        setDuration(duration);
        validCells = valid;
    }
    @Beta
    public static class ExplosionEffect extends PanelEffect
    {
        public Coord center;
        public int radius = 2;
        public float[] colors = {
                SColor.INTERNATIONAL_ORANGE.toFloatBits(),
                SColor.FLORAL_LEAF.toFloatBits(),
                SColor.LEMON.toFloatBits(),
                SColor.LEMON_CHIFFON.toFloatBits(),
                SColor.floatGet(0xFF6600BB),  // SColor.SAFETY_ORANGE
                SColor.floatGet(0x59565299),  // SColor.DB_SOOT
                SColor.floatGet(0x59565200)}; // SColor.DB_SOOT
        public double[][] resMap, lightMap;
        public List<Coord> affected;
        public ExplosionEffect(SquidPanel targeting, Coord center, int radius)
        {
            this(targeting, 1f, center, radius);
        }
        public ExplosionEffect(SquidPanel targeting, float duration, Coord center, int radius)
        {
            super(targeting, duration);
            this.center = center;
            this.radius = radius;
            resMap = new double[validCells.width][validCells.height];
            lightMap = new double[validCells.width][validCells.height];
            FOV.reuseFOV(resMap, lightMap, center.x, center.y, radius);
            affected = Radius.inCircle(center.x, center.y, radius, false, validCells.width, validCells.height);
        }
        public ExplosionEffect(SquidPanel targeting, float duration, GreasedRegion valid, Coord center, int radius)
        {
            super(targeting, duration, valid);
            this.center = center;
            this.radius = radius;
            resMap = ArrayTools.fill(1.0, validCells.width, validCells.height);
            validCells.writeDoublesInto(resMap, 0.0);
            lightMap = new double[validCells.width][validCells.height];
            FOV.reuseFOV(resMap, lightMap, center.x, center.y, radius);
            affected = Radius.inCircle(center.x, center.y, radius, false, validCells.width, validCells.height);
        }
        /**
         * Called each frame.
         *
         * @param percent The percentage of completion for this action, growing from 0 to 1 over the duration. If
         *                {@link #setReverse(boolean) reversed}, this will shrink from 1 to 0.
         */
        @Override
        protected void update(float percent) {
            int len = affected.size();
            Coord c;
            float f, color;
            int idx, seed = System.identityHashCode(this);
            for (int i = 0; i < len; i++) {
                c = affected.get(i);
                if(lightMap[c.x][c.y] <= 0.0 || 0.6 * (lightMap[c.x][c.y] + percent) < 0.25)
                    continue;
                f = (float)SeededNoise.noise(c.x * 0.5, c.y * 0.5, percent * 3, seed)
                        * 0.125f + 0.125f + percent * 0.75f;
                idx = (int) (f * colors.length);
                if(idx >= colors.length - 1)
                    color = SColor.lerpFloatColors(colors[colors.length-1], 0f, (f * colors.length) % 1f);
                else
                    color = SColor.lerpFloatColors(colors[idx], colors[idx+1], (f * colors.length) % 1f);
                target.put(c.x, c.y, color);
            }
        }
    }
}
