package squidpony.gdx.examples.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import squidpony.gdx.examples.CoveredPathDemo;

public class CoveredPathDemoLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "SquidLib GDX Covered Pathfinding Demo";
		config.width = 60 * 6;
		config.height = 50 * 12;
		config.addIcon("Tentacle-16.png", Files.FileType.Classpath);
		config.addIcon("Tentacle-32.png", Files.FileType.Classpath);
		config.addIcon("Tentacle-128.png", Files.FileType.Classpath);
		new LwjglApplication(new CoveredPathDemo(), config);
	}
}
