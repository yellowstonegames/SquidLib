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
		String archiveContents = Gdx.files.local("archives/Thesaurus-" + ARCHIVE_VERSION + ".txt").readString("UTF-8");
		String archiveContentsAlt = Gdx.files.local("Thesaurus-" + ARCHIVE_VERSION + ".alt.txt").readString("UTF-8");
		Thesaurus thesaurus = new Thesaurus(0L);
		thesaurus.addArchivedCategories(archiveContents);
		String archive = Thesaurus.archiveCategories();
		String archiveAlt = Thesaurus.archiveCategoriesAlternate();
		Gdx.files.local("Thesaurus-" + ARCHIVE_VERSION + ".txt").writeString(archive, false, "UTF-8");
		Gdx.files.local("Thesaurus-" + ARCHIVE_VERSION + ".alt.txt").writeString(archiveAlt, false, "UTF-8");
		System.out.println(
				archiveContents.equals(archive));
		thesaurus = new Thesaurus(0L);
		thesaurus.addArchivedCategories(archiveContents);
		Thesaurus thesaurus2 = new Thesaurus(0L);
		thesaurus2.addArchivedCategoriesAlternate(archiveContentsAlt);
		String testing, testing2;
		System.out.println((testing = thesaurus.makeFlowerName()) + "  \t  " + (testing.equals(testing2 = thesaurus2.makeFlowerName()) ? "== " : "!= ") + testing2);
		System.out.println((testing = thesaurus.makeFruitName() ) + "  \t  " + (testing.equals(testing2 = thesaurus2.makeFruitName()) ? "== " : "!= ") + testing2);
		System.out.println((testing = thesaurus.makeNutName()   ) + "  \t  " + (testing.equals(testing2 = thesaurus2.makeNutName()) ? "== " : "!= ") + testing2);
		System.out.println((testing = thesaurus.makePlantName() ) + "  \t  " + (testing.equals(testing2 = thesaurus2.makePlantName()) ? "== " : "!= ") + testing2);
		System.out.println((testing = thesaurus.makeNationName()) + "  \t  " + (testing.equals(testing2 = thesaurus2.makeNationName()) ? "== " : "!= ") + testing2);
		System.out.println((testing = thesaurus.makeNationName()) + "  \t  " + (testing.equals(testing2 = thesaurus2.makeNationName()) ? "== " : "!= ") + testing2);
		System.out.println((testing = thesaurus.makeNationName()) + "  \t  " + (testing.equals(testing2 = thesaurus2.makeNationName()) ? "== " : "!= ") + testing2);
		System.out.println((testing = thesaurus.makeNationName()) + "  \t  " + (testing.equals(testing2 = thesaurus2.makeNationName()) ? "== " : "!= ") + testing2);
		Gdx.app.exit();
	}
	public static void main(String[] args)
	{
		HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
		new HeadlessApplication(new ThesaurusArchiver(), config);
	}

}
