package squidpony.gdx.examples.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import squidpony.gdx.examples.ImageDemo;

public class ImageDemoLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "SquidLib GDX Image Demo";
		config.width = 30 * 2 * 18;
		config.height = 20 * 36;
		new LwjglApplication(new ImageDemo(), config);
	}
}
