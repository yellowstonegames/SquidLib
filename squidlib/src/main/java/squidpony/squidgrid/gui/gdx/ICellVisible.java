/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
    /**
     * @return a char that can be used to represent this ICellVisible on a grid
     */
    char getSymbol();

    /**
     * @return a packed float color as produced by {@link Color#toFloatBits()} that this ICellVisible will be shown with
     */
    float getPackedColor();

    /**
     * A bare-bones implementation of ICellVisible that always allows its symbol and color to be changed.
     */
    class Basic implements ICellVisible, Serializable {
        private static final long serialVersionUID = 1L;
        public char symbol;
        public float packedColor;

        public Basic()
        {
            this('.', -0x1.0p125f); // float is equal to SColor.FLOAT_BLACK
        }
        public Basic(char symbol, Color color)
        {
            this.symbol = symbol;
            this.packedColor = color.toFloatBits();
        }

        public Basic(char symbol, float color)
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

    /**
     * A implementation of ICellVisible that extends {@link ICellVisible.Basic} to also carry a String name.
     */
    class Named extends Basic implements ICellVisible, Serializable {
        private static final long serialVersionUID = 1L;
        public String name;

        public Named()
        {
            this('.', -0x1.0p125f, "floor"); // float is equal to SColor.FLOAT_BLACK
        }
        public Named(char symbol, Color color, String name)
        {
            super(symbol, color);
            this.name = name;
        }

        public Named(char symbol, float color, String name)
        {
            super(symbol, color);
            this.name = name;
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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
