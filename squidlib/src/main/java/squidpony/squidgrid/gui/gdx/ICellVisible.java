package squidpony.squidgrid.gui.gdx;

/**
 * A basic interface for any kind of object that has a symbolic representation and a color, here represented by a char
 * and a packed float color. This is useful for generic classes that don't force user code to extend a specific class to
 * be shown in some way.
 * <br>
 * Created by Tommy Ettinger on 10/17/2019.
 */
public interface ICellVisible {
    char getSymbol();
    float getPackedColor();
}
