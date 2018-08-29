package squidpony.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SparseLayers;
import squidpony.squidgrid.gui.gdx.TextCellFactory;

/**
 * Created by Tommy Ettinger on 8/28/2018.
 */
public class SparseLayersExperiment extends SparseLayers {
    public Pixmap bg;
    public Texture tex;
    public SparseLayersExperiment(int gridWidth, int gridHeight) {
        super(gridWidth, gridHeight);
        bg = new Pixmap(this.gridWidth, this.gridHeight, Pixmap.Format.RGBA8888);
        tex = new Texture(bg, Pixmap.Format.RGBA8888, false);
    }

    public SparseLayersExperiment(int gridWidth, int gridHeight, float cellWidth, float cellHeight) {
        super(gridWidth, gridHeight, cellWidth, cellHeight);
        bg = new Pixmap(this.gridWidth, this.gridHeight, Pixmap.Format.RGBA8888);
        tex = new Texture(bg, Pixmap.Format.RGBA8888, false);
    }

    public SparseLayersExperiment(int gridWidth, int gridHeight, float cellWidth, float cellHeight, TextCellFactory font) {
        super(gridWidth, gridHeight, cellWidth, cellHeight, font);
        bg = new Pixmap(this.gridWidth, this.gridHeight, Pixmap.Format.RGBA8888);
        tex = new Texture(bg, Pixmap.Format.RGBA8888, false);
    }

    public SparseLayersExperiment(int gridWidth, int gridHeight, float cellWidth, float cellHeight, TextCellFactory font, float xOffset, float yOffset) {
        super(gridWidth, gridHeight, cellWidth, cellHeight, font, xOffset, yOffset);
        bg = new Pixmap(this.gridWidth, this.gridHeight, Pixmap.Format.RGBA8888);
        tex = new Texture(bg, Pixmap.Format.RGBA8888, false);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float xo = getX(), yo = getY(), yOff = yo + 1f + gridHeight * font.actualCellHeight, gxo, gyo;
        font.draw(bg, backgrounds);
        tex.draw(bg, 0, 0);
        batch.setColor(SColor.FLOAT_WHITE);
        batch.draw(tex, xo, yo, gridWidth * font.actualCellWidth, gridHeight * font.actualCellHeight);
        int len = layers.size();
        Frustum frustum = null;
        Stage stage = getStage();
        if(stage != null) {
            Viewport viewport = stage.getViewport();
            if(viewport != null)
            {
                Camera camera = viewport.getCamera();
                if(camera != null)
                {
                    if(
                            camera.frustum != null &&
                                    (!camera.frustum.boundsInFrustum(xo, yOff - font.actualCellHeight - 1f, 0f, font.actualCellWidth, font.actualCellHeight, 0f) ||
                                            !camera.frustum.boundsInFrustum(xo + font.actualCellWidth * (gridWidth-1), yo, 0f, font.actualCellWidth, font.actualCellHeight, 0f))
                    )
                        frustum = camera.frustum;
                }
            }
        }
        font.configureShader(batch);
        if(frustum == null) {
            for (int i = 0; i < len; i++) {
                layers.get(i).draw(batch, font, xo, yOff);
            }

        }
        else
        {
            for (int i = 0; i < len; i++) {
                layers.get(i).draw(batch, font, frustum, xo, yOff);
            }
        }

        int x, y;
        for (int i = 0; i < glyphs.size(); i++) {
            TextCellFactory.Glyph glyph = glyphs.get(i);
            if(glyph == null)
                continue;
            glyph.act(Gdx.graphics.getDeltaTime());
            if(
                    !glyph.isVisible() ||
                            (x = Math.round((gxo = glyph.getX() - xo) / font.actualCellWidth)) < 0 || x >= gridWidth ||
                            (y = Math.round((gyo = glyph.getY() - yo)  / -font.actualCellHeight + gridHeight)) < 0 || y >= gridHeight ||
                            backgrounds[x][y] == 0f || (frustum != null && !frustum.boundsInFrustum(gxo, gyo, 0f, font.actualCellWidth, font.actualCellHeight, 0f)))
                continue;
            glyph.draw(batch, 1f);
        }
    }
}
