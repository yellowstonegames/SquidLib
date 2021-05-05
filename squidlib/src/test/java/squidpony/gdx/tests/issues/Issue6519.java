package squidpony.gdx.tests.issues;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import squidpony.squidmath.Coord;

public class Issue6519 extends ApplicationAdapter {
    public static class GraphicalTerm {
        private static final FileHandle GNUnicode = Gdx.files.classpath("7-12-serif.fnt");
        private static final FileHandle GNUnicode_images = Gdx.files.classpath("7-12-serif.png");
        static BitmapFont font;
        public final int Height, Width;
        public final int Top;

        public final char[][] Buffer;
        public final Color[][][] ColorBuffer;

        private int x,y;

        public GraphicalTerm( int height, int width, int top ){
            this.Top   = height * 12;
            this.Height = height;
            this.Width = width;
            this.Buffer = new char[width][height];
            this.ColorBuffer = new Color[width][height][2];
            for(int i = 0; i < this.Width; i++){
                for( int j = 0; j < this.Height; j++){
                    this.Buffer[i][j] = ' ';
                    this.ColorBuffer[i][j][0] = Color.WHITE;
                    this.ColorBuffer[i][j][1] = Color.WHITE;
                }
            }

            char render = 33;
            for(int i = 0; i < this.Width; i++){
                render ++;
                if (render > 137) {
                    render = 33;
                }
                for( int j = 0; j < this.Height; j++){
                    this.ColorBuffer[i][j][1] = colarray[(i+j)%colarray.length];
                    this.Buffer[i][j] = render;
                }
            }
        }

        public int GetHeight() {
            return this.Height;
        }

        public int GetWidth() {
            return this.Width;
        }

        public Coord GetCurrentPos() {
            return Coord.get(this.x,this.y);
        }

        public boolean SetPos(int x, int y) {
            this.x = MathUtils.clamp(x, 0, this.Width - 1);
            this.y = MathUtils.clamp(y, 0, this.Height - 1);
            return true;
        }

        public boolean SetChar(int x, int y, char c) {
            this.Buffer[x][y] = c;
            return true;
        }

        public void Create(){
            font = new BitmapFont(GNUnicode, GNUnicode_images, false);
            font.setUseIntegerPositions(false);
            font.setFixedWidthGlyphs("");
        }

        public void Render(Batch b){
            for(int i = 0; i < this.Width; i++){
                for( int j = 0; j < this.Height; j++){
                    font.setColor(ColorBuffer[i][j][1]);
                    font.draw(b, Character.toString(this.Buffer[i][j]), i*10, this.Top-(font.getLineHeight()*j));
                }
            }
        }

        public static final Color[] colarray =
                {
                        new Color(0, 0, 1, 1),
                        new Color(0, 0, 0.5f, 1),
                        new Color(0x4169e1ff),
                        new Color(0x708090ff),
                        new Color(0x87ceebff),
                        new Color(0, 1, 1, 1),
                        new Color(0, 0.5f, 0.5f, 1),
                        new Color(0x00ff00ff),
                        new Color(0x7fff00ff),
                        new Color(0x32cd32ff),
                        new Color(0x228b22ff),
                        new Color(0x6b8e23ff),
                        new Color(0xffff00ff),
                        new Color(0xffd700ff),
                        new Color(0xdaa520ff),
                        new Color(0xffa500ff),
                        new Color(0x8b4513ff),
                        new Color(0xd2b48cff),
                        new Color(0xb22222ff),
                        new Color(0xff0000ff),
                        new Color(0xff341cff),
                        new Color(0xff7f50ff),
                        new Color(0xfa8072ff),
                        new Color(0xff69b4ff),
                        new Color(1, 0, 1, 1),
                        new Color(0xa020f0ff),
                        new Color(0xee82eeff),
                        new Color(0xb03060ff),
                };
    }

    ScreenViewport viewport;
    SpriteBatch batch;
    GraphicalTerm term;
    @Override
    public void create() {
        term = new GraphicalTerm(40, 80, 2);
        term.Create();
        viewport = new ScreenViewport();
        batch = new SpriteBatch();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public void render() {
        batch.begin();
        term.Render(batch);
        batch.end();
    }

    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Issue 6519 Test");
        config.useVsync(true);
        config.setWindowedMode(640, 480);
        new Lwjgl3Application(new Issue6519(), config);
    }

}
