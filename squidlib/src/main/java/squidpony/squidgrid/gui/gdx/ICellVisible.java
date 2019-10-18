package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;

import java.io.Serializable;

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
    
    class Impl implements ICellVisible, Serializable {
        private static final long serialVersionUID = 1L;
        public char symbol;
        public float packedColor;

        public Impl()
        {
            this('.', -0x1.fffffep126f); // float is equal to SColor.FLOAT_WHITE
        }
        public Impl(char symbol, Color color)
        {
            this.symbol = symbol;
            this.packedColor = color.toFloatBits();
        }

        public Impl(char symbol, float color)
        {
            this.symbol = symbol;
            this.packedColor = color;
        }
        
        @Override
        public char getSymbol() {
            return symbol;
        }

        public void setSymbol(char symbol) {
            this.symbol = symbol;
        }

        @Override
        public float getPackedColor() {
            return packedColor;
        }

        public void setPackedColor(float packedColor) {
            this.packedColor = packedColor;
        }
    }
}
