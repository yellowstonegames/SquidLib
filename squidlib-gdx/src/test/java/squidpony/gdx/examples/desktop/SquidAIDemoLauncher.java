package squidpony.gdx.examples.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import squidpony.gdx.examples.SquidAIDemo;

public class SquidAIDemoLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "SquidLib GDX AI Demo";
		config.width = 40 * 2 * 6;
		config.height = 40 * 12;
		config.addIcon("Tentacle-16.png", Files.FileType.Classpath);
		config.addIcon("Tentacle-32.png", Files.FileType.Classpath);
		config.addIcon("Tentacle-128.png", Files.FileType.Classpath);
		new LwjglApplication(new SquidAIDemo(), config);
	}
}
