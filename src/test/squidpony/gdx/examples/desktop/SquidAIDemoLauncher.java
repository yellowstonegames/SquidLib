package squidpony.gdx.examples.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import squidpony.gdx.examples.EverythingDemo;
import squidpony.gdx.examples.SquidAIDemo;

public class SquidAIDemoLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "SquidLib GDX AI Demo";
		config.width = 120 * 6;
		config.height = 40 * 12;
		new LwjglApplication(new SquidAIDemo(), config);
	}
}
