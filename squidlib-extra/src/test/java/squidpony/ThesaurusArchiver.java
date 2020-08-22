package squidpony;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

/**
 * Created by Tommy Ettinger on 8/21/2020.
 */
public class ThesaurusArchiver extends ApplicationAdapter {
	public static final String ARCHIVE_VERSION = "3-0-0";
	@Override
	public void create() {
		String archive = Thesaurus.archiveCategories();
		Gdx.files.local("Thesaurus-" + ARCHIVE_VERSION + ".txt").writeString(archive, false, "UTF-8");
		String archive2 = Gdx.files.local("Thesaurus-" + ARCHIVE_VERSION + ".txt").readString("UTF-8");
		System.out.println(
				archive2.equals(archive));
		Thesaurus thesaurus = new Thesaurus(0L);
		thesaurus.addArchivedCategories(archive2);
		Thesaurus thesaurus2 = new Thesaurus(0L);
		thesaurus2.addKnownCategories();
		String testing;
		System.out.println((testing = thesaurus.makeFlowerName()) + "  \t  " + testing.equals(thesaurus2.makeFlowerName()));
		System.out.println((testing = thesaurus.makeFruitName() ) + "  \t  " + testing.equals(thesaurus2.makeFruitName()));
		System.out.println((testing = thesaurus.makeNutName()   ) + "  \t  " + testing.equals(thesaurus2.makeNutName()));
		System.out.println((testing = thesaurus.makePlantName() ) + "  \t  " + testing.equals(thesaurus2.makePlantName()));
		System.out.println((testing = thesaurus.makeNationName()) + "  \t  " + testing.equals(thesaurus2.makeNationName()));
		System.out.println((testing = thesaurus.makeNationName()) + "  \t  " + testing.equals(thesaurus2.makeNationName()));
		System.out.println((testing = thesaurus.makeNationName()) + "  \t  " + testing.equals(thesaurus2.makeNationName()));
		System.out.println((testing = thesaurus.makeNationName()) + "  \t  " + testing.equals(thesaurus2.makeNationName()));
		Gdx.app.exit();
	}
	public static void main(String[] args)
	{
		HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
		new HeadlessApplication(new ThesaurusArchiver(), config);
	}

}
