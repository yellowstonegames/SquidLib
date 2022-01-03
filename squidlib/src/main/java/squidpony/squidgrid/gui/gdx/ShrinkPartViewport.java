/******************************************************************************
 Copyright 2011 See AUTHORS file.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

/** A viewport that scales the world using {@link Scaling#stretch} on a sub-region of the screen.
 * Does not keep the aspect ratio, the world is scaled to take up the requested region of the screen.
 * @author Daniel Holderbaum
 * @author Nathan Sweet
 * Created by Tommy Ettinger on 4/16/2016.
 */
public class ShrinkPartViewport extends ScalingViewport {
    public float barWidth, barHeight;
    /** Creates a new viewport using a new {@link OrthographicCamera}. */
    public ShrinkPartViewport (float worldWidth, float worldHeight, float barWidth) {
        this(worldWidth, worldHeight, barWidth, new OrthographicCamera());
    }
    public ShrinkPartViewport (float worldWidth, float worldHeight, float barWidth, float barHeight) {
        this(worldWidth, worldHeight, barWidth, barHeight, new OrthographicCamera());
    }

    public ShrinkPartViewport (float worldWidth, float worldHeight, float barWidth, Camera camera) {
        this(worldWidth, worldHeight, barWidth, 0f, camera);
    }

    public ShrinkPartViewport (float worldWidth, float worldHeight, float barWidth, float barHeight, Camera camera) {
        super(Scaling.stretch, worldWidth, worldHeight, camera);
        this.barWidth = barWidth;
        this.barHeight = barHeight;
    }

    @Override
    public void update (int screenWidth, int screenHeight, boolean centerCamera) {
        Vector2 scaled = Scaling.stretch.apply(getWorldWidth(), getWorldHeight(),
                screenWidth - barWidth * 2, screenHeight - barHeight * 2);
        int viewportWidth = Math.round(scaled.x);
        int viewportHeight = Math.round(scaled.y);
        // Center.
        setScreenBounds((screenWidth - viewportWidth) >> 1, (screenHeight - viewportHeight) >> 1, viewportWidth, viewportHeight);
        apply(true);
    }

    public Scaling getScaling () {
        return Scaling.stretch;
    }
}
