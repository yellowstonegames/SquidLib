package squidpony.gdx.examples.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import squidpony.gdx.examples.ZoneDemo;

public class ZoneDemoLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "SquidLib GDX Zone Demo";
		config.width = 90 * 18;
		config.height = 40 * 18;
		config.addIcon("Tentacle-16.png", Files.FileType.Classpath);
		config.addIcon("Tentacle-32.png", Files.FileType.Classpath);
		config.addIcon("Tentacle-128.png", Files.FileType.Classpath);
		config.vSyncEnabled = false;
        config.foregroundFPS = 0;
		new LwjglApplication(new ZoneDemo(), config);
	}
}
