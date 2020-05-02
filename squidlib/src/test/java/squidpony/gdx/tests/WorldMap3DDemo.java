package squidpony.gdx.tests;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * All credit for this demo has to go to mgsx, who wrote it in a 30-minute gist ( https://gist.github.com/mgsx-dev/28be3c3e4f63be55e4298209c95334a6 )
 * to boldly go where no SquidLib demo has gone before.
 */
public class WorldMap3DDemo extends Game {

	private FitViewport viewport;
	private PerspectiveCamera camera;
	private CameraInputController cameraController;
	private ModelBatch batch;
	private Environment env;
	private ModelInstance modelInstance;
	private Texture texture;
	private ColorAttribute ambientLight;
	private DirectionalLight sunLight;
	private DirectionalLight cameraLight;

	@Override
	public void create() {
		// camera
		camera = new PerspectiveCamera();
		camera.fieldOfView = 50f;
		camera.near = .01f;
		camera.far = 100f;
		camera.up.set(Vector3.Y);
		camera.position.set(10, 10, 10);
		camera.lookAt(Vector3.Zero);
		camera.update();
		viewport = new FitViewport(16f, 9f, camera);
		cameraController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(cameraController);
		
		// environment
		batch = new ModelBatch();
		env = new Environment();
		env.set(ambientLight = new ColorAttribute(ColorAttribute.AmbientLight, Color.DARK_GRAY));
		env.add(sunLight = new DirectionalLight().set(Color.WHITE, new Vector3(1, -3, 1)));
		env.add(cameraLight = new DirectionalLight().set(Color.WHITE, new Vector3(1, 1, 1)));
		
		// material
		Material material = new Material();
		material.set(ColorAttribute.createDiffuse(Color.WHITE));
		texture = new Texture(Gdx.files.internal("special/EquirectangularWorldMap.png"));
		material.set(TextureAttribute.createDiffuse(texture));
		
		// model
		Model model = createSphere(material);
		modelInstance = new ModelInstance(model);
	}
	
	private Model createSphere(Material material){
		float sphereSize = 10f;
		int divisionsU = 32;
		int divisionsV = 16;
		return new ModelBuilder().createSphere(sphereSize, sphereSize, sphereSize, divisionsU, divisionsV, material, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
	}
	
	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}
	
	@Override
	public void render() {
		cameraController.update();
		
		ambientLight.color.set(Color.WHITE).mul(.2f);
		sunLight.color.set(Color.WHITE).mul(.8f);
		cameraLight.color.set(Color.WHITE).mul(.5f);
		cameraLight.direction.set(camera.direction);
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		batch.begin(camera);
		batch.render(modelInstance, env);
		batch.end();
	}
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "SquidLib Demo: Detailed World Map";
		config.width = 960;
		config.height = 540;
		config.foregroundFPS = 60;
		config.vSyncEnabled = true;
		//config.fullscreen = true;
		config.backgroundFPS = -1;
		config.addIcon("Tentacle-16.png", Files.FileType.Internal);
		config.addIcon("Tentacle-32.png", Files.FileType.Internal);
		config.addIcon("Tentacle-128.png", Files.FileType.Internal);
		new LwjglApplication(new WorldMap3DDemo(), config);
	}

}
