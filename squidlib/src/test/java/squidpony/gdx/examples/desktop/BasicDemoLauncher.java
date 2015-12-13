package squidpony.gdx.examples.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import squidpony.gdx.examples.BasicDemo;

public class BasicDemoLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "SquidLib GDX Basic Demo";
		config.width = 80 * 12;
		config.height = 25 * 24;
        config.addIcon("Tentacle-16.png", Files.FileType.Classpath);
        config.addIcon("Tentacle-32.png", Files.FileType.Classpath);
        config.addIcon("Tentacle-128.png", Files.FileType.Classpath);
		new LwjglApplication(new BasicDemo(), config);
	}
}
