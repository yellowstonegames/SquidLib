package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import squidpony.annotation.Beta;

/**
 * A single-Actor container that should stretch the container to fill the dimensions of the container.
 * It's unclear if this actually does anything yet; there are issues testing it that may or may not be related to this.
 * Created by Tommy Ettinger on 12/29/2016.
 */
@Beta
public class StretchContainer extends WidgetGroup {
    public WrapperViewport viewport;
    protected float prefWidth, prefHeight;
    public class WrapperViewport extends ScalingViewport
    {
        public WrapperViewport (float worldWidth, float worldHeight) {
            this(worldWidth, worldHeight, new OrthographicCamera());
        }
        public WrapperViewport (float worldWidth, float worldHeight, Camera camera) {
            super(Scaling.stretch, worldWidth, worldHeight, camera);
        }

        @Override
        public void update (int screenWidth, int screenHeight, boolean centerCamera) {
            Vector2 scaled = Scaling.stretch.apply(getWorldWidth(), getWorldHeight(),
                    getWidth(), getHeight());
            int viewportWidth = MathUtils.round(scaled.x);
            int viewportHeight = MathUtils.round(scaled.y);
            // Center.
            setScreenBounds(MathUtils.round(getX()), MathUtils.round(getY()), viewportWidth, viewportHeight);
            apply(true);
        }

        public Scaling getScaling () {
            return Scaling.stretch;
        }
    }
    public StretchContainer(Actor... actors)
    {
        if(actors == null || actors.length == 0)
        {
            viewport = new WrapperViewport(1, 1);
        }
        else {
            viewport = new WrapperViewport(prefWidth = actors[0].getWidth(), prefHeight = actors[0].getHeight());
            setSize(prefWidth, prefHeight);
            addActor(actors[0]);

        }
        setFillParent(true);
    }


    @Override
    public float getMinWidth() {
        return 0;
    }

    @Override
    public float getMinHeight() {
        return 0;
    }

    @Override
    public float getPrefWidth() {
        return prefWidth;
    }

    @Override
    public float getPrefHeight() {
        return prefHeight;
    }

    @Override
    public void invalidate() {
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        super.invalidate();
    }
}
