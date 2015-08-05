package squidpony.gdx.examples.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import squidpony.gdx.examples.EverythingDemo;

public class EverythingDemoLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "SquidLib GDX Everything Demo";
		config.width = 80 * 12;
		config.height = 30 * 24;
		new LwjglApplication(new EverythingDemo(), config);
	}
}
