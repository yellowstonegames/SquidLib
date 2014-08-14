package squidpony.squidgrid.generation;

/**
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class SolarnusTest {
    public static void main(String... args){
        SolarnusDungeon dungeon = new SolarnusDungeon();
        dungeon.CreateDungeon(20, 80, 5);
        dungeon.ShowDungeon();
    }
}
