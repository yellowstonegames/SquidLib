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
import squidpony.ArrayTools;
import squidpony.squidgrid.gui.gdx.ICellVisible.Basic;
import squidpony.squidgrid.mapping.WildMap;
import squidpony.squidgrid.mapping.WorldMapGenerator;
import squidpony.squidmath.IntPointHash;
import squidpony.squidmath.SilkRNG;

import java.util.HashMap;
import java.util.Map;

import static squidpony.squidgrid.gui.gdx.SColor.*;
import static squidpony.squidgrid.gui.gdx.WorldMapView.*;

/**
 * Created by Tommy Ettinger on 9/6/2019.
 */
public class WildMapView {
    protected int width, height;
    protected float[][] colorMap;
    public WildMap wildMap;
    
    public Map<String, ? extends ICellVisible> viewer;
    
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float[][] getColorMap() {
        return colorMap;
    }
    
    public WildMap getWildMap() {
        return wildMap;
    }

    public void setWildMap(WildMap wildMap) {
        this.wildMap = wildMap;
        if(this.width != wildMap.width || this.height != wildMap.height)
        {
            width = wildMap.width;
            height = wildMap.height;
            colorMap = new float[width][height];
        }
    }
    
    public static HashMap<String, ? extends ICellVisible> defaultViewer()
    {
        HashMap<String, ICellVisible> viewer = new HashMap<>(128);

        viewer.put("snow path", new Basic('.', ALICE_BLUE.toEditedFloat(0.0f, -0.2f, -0.15f)));
        viewer.put("dirt path", new Basic('.', CLOVE_BROWN.toEditedFloat(-0.005f, -0.275f, 0.17f)));
        viewer.put("sand path", new Basic('.', CW_PALE_ORANGE.toEditedFloat(0.05f, -0.17f, -0.075f)));
        viewer.put("grass path", new Basic('.', AURORA_DUSTY_GREEN.toEditedFloat(0.0f, -0.15f, -0.1f)));
        viewer.put("stone path", new Basic('.', AURORA_CHIPPED_GRANITE.toEditedFloat(-0.09f, -0.05f, 0.1f)));
        viewer.put("wooden bridge", new Basic(':', BRUSHWOOD_DYED.toEditedFloat(0.0f, -0.275f, 0.05f)));

        viewer.put("ice ledge", new Basic('¬', SColor.toEditedFloat(PALE_CORNFLOWER_BLUE, 0.0f, -0.1f, 0.1f)));
        viewer.put("dirt ledge", new Basic('¬', CLOVE_BROWN.toEditedFloat(-0.005f, -0.175f, -0.18f)));
        viewer.put("sand ledge", new Basic('¬', CW_PALE_ORANGE.toEditedFloat(0.05f, -0.15f, -0.125f)));
        viewer.put("grass ledge", new Basic('¬', AURORA_DUSTY_GREEN.toEditedFloat(0.0f, -0.025f, -0.45f)));
        viewer.put("stone ledge", new Basic('¬', AURORA_CHIPPED_GRANITE.toEditedFloat(-0.07f, -0.1f, -0.25f)));

        viewer.put("snow", new Basic('…', ALICE_BLUE));
        viewer.put("ice", new Basic('-', SColor.lightenFloat(PALE_CORNFLOWER_BLUE, 0.3f)));
        viewer.put("dirt", new Basic('·', CLOVE_BROWN.toEditedFloat(-0.005f, -0.075f, 0.02f)));
        viewer.put("pebbles", new Basic('…', AURORA_WET_STONE.toEditedFloat(0.0f, 0.0f, 0.0f)));
        viewer.put("dry grass", new Basic('\'', CW_FADED_BROWN.toEditedFloat(0.06f, 0.05f, 0.05f)));
        viewer.put("fresh water", new Basic('~', AURORA_BLUE_EYE));
        viewer.put("salt water", new Basic('≈', AURORA_PRUSSIAN_BLUE));
        viewer.put("sand", new Basic('…', CW_PALE_ORANGE.toEditedFloat(0.05f, -0.05f, 0.075f)));
        viewer.put("leaves", new Basic('…', CHINESE_TEA_YELLOW.toEditedFloat(0.02f, -0.025f, 0.0f)));
        viewer.put("grass", new Basic('"', AURORA_DUSTY_GREEN.toEditedFloat(0.0f, 0.075f, -0.25f)));
        viewer.put("mud", new Basic(',', DB_EARTH.toEditedFloat(0.03f, -0.15f, -0.03f)));
        viewer.put("moss", new Basic('˝', AURORA_FERN_GREEN.toEditedFloat(0f, 0.0f, 0.0f)));
        viewer.put("rubble", new Basic('‰', AURORA_CHIPPED_GRANITE.toEditedFloat(-0.07f, 0.0f, -0.05f)));
        viewer.put("empty space", new Basic('_', DB_INK));
        viewer.put("snow mound", new Basic('∆', ALICE_BLUE.toEditedFloat(0f, 0.05f, -0.1f)));
        viewer.put("icy divot", new Basic('°', ALICE_BLUE.toEditedFloat(0.05f, 0.075f, 0.06f)));
        viewer.put("powder snowdrift", new Basic('¨', ALICE_BLUE.toEditedFloat(0.0f, 0.0f, -0.07f)));
        viewer.put("hillock", new Basic('∆', CW_DRAB_BROWN.toEditedFloat(0.1f, -0.05f, 0.25f)));
        viewer.put("animal burrow", new Basic('¸', AURORA_ARMY_GREEN.toEditedFloat(0.05f, 0.0f, -0.05f)));
        viewer.put("small bush 1", new Basic('♣', AURORA_AVOCADO.toEditedFloat(-0.055f, -0.025f, -0.225f)));
        viewer.put("large bush 1", new Basic('♣', AURORA_FOREST_GLEN.toEditedFloat(-0.055f, -0.125f, -0.225f)));
        viewer.put("evergreen tree 1", new Basic('♠', PINE_GREEN.toEditedFloat(-0.13f, -0.03f, -0.05f)));
        viewer.put("evergreen tree 2", new Basic('♠', AURORA_EUCALYPTUS.toEditedFloat(-0.035f, -0.045f, -0.75f)));
        viewer.put("small cactus 1", new Basic('‡', AURORA_FROG_GREEN.toEditedFloat(0.035f, 0.065f, -0.06f)));
        viewer.put("large cactus 1", new Basic('‡', AURORA_MARSH.toEditedFloat(0.04f, 0.11f, -0.03f)));
        viewer.put("succulent 1", new Basic('§', CW_FLUSH_JADE.toEditedFloat(-0.045f, -0.1f, 0.0f)));
        viewer.put("seashell 1", new Basic('ˋ', CW_LIGHT_APRICOT.toEditedFloat(0.0f, -0.095f, 0.07f)));
        viewer.put("seashell 2", new Basic('ˋ', CW_PALE_RED.toEditedFloat(0.0f, -0.2f, 0.1f)));
        viewer.put("seashell 3", new Basic('ˋ', CW_PALE_YELLOW.toEditedFloat(0.0f, 0.02f, 0.05f)));
        viewer.put("seashell 4", new Basic('ˋ', CW_PALE_VIOLET.toEditedFloat(0.0f, -0.080f, 0.11f)));
        viewer.put("driftwood", new Basic('¿', AURORA_DRIFTWOOD.toEditedFloat(0.0f, -0.25f, 0.04f)));
        viewer.put("boulder", new Basic('●', AURORA_SLOW_CREEK.toEditedFloat(0.0f, -0.01f, 0.0f)));
        viewer.put("deciduous tree 1", new Basic('¥', AURORA_AVOCADO.toEditedFloat(-0.065f, 0.0f, -0.3f)));
        viewer.put("small bush 2", new Basic('♣', AURORA_WOODLANDS.toEditedFloat(-0.045f, -0.05f, -0.025f)));
        viewer.put("deciduous tree 2", new Basic('¥', AURORA_IVY_GREEN.toEditedFloat(-0.02f, 0.0f, 0.0f)));
        viewer.put("deciduous tree 3", new Basic('¥', AURORA_ASPARAGUS.toEditedFloat(-0.015f, 0.055f, 0.02f)));
        viewer.put("large bush 2", new Basic('♣', AURORA_VIRIDIAN.toEditedFloat(-0.03f, -0.05f, 0.03f)));
        viewer.put("tropical tree 1", new Basic('¶', AURORA_FLORAL_FOAM.toEditedFloat(-0.05f, 0.025f, 0.075f)));
        viewer.put("tropical tree 2", new Basic('¶', AURORA_MAIDENHAIR_FERN.toEditedFloat(0.0f, 0.0f, 0.02f)));
        viewer.put("large bush 3", new Basic('♣', AURORA_KELLY_GREEN.toEditedFloat(0.0f, 0.025f, 0.02f)));
        viewer.put("tropical tree 3", new Basic('¶', AURORA_SOFT_TEAL.toEditedFloat(-0.15f, -0.07f, -0.03f)));
        viewer.put("tropical tree 4", new Basic('¶', AURORA_PRASE.toEditedFloat(-0.04f, -0.02f, -0.02f)));
        return viewer;
    }

    public WildMapView()
    {
        this(null);
    }
    public WildMapView(WildMap wildMap)
    {
        if(wildMap == null)
        {
            this.wildMap = new WildMap();
        }
        else
        {
            this.wildMap = wildMap;
        }
        this.viewer = defaultViewer();
        width = this.wildMap.width;
        height = this.wildMap.height;
        colorMap = new float[width][height];
        initialize();
    }
    public WildMapView(WildMap wildMap, Map<String, ? extends ICellVisible> viewer)
    {
        if(wildMap == null) { // default to forest map, biome 21; ignore given viewer, it may be null
            this.wildMap = new WildMap();
            this.viewer = defaultViewer();
        }
        else
        {
            this.wildMap = wildMap;
            this.viewer = viewer == null ? defaultViewer() : viewer;
        }
        width = this.wildMap.width;
        height = this.wildMap.height;
        colorMap = new float[width][height];
        initialize();
    }
    
    public WildMapView(long seed, int width, int height, int biome)
    {
        this(new WildMap(width, height, biome, new SilkRNG(seed)));
    }
    
    protected float[] biomeColors = {
            desertColor,
            savannaColor,
            tropicalRainforestColor,
            grasslandColor,
            woodlandColor,
            seasonalForestColor,
            temperateRainforestColor,
            borealForestColor,
            tundraColor,
            iceColor,
            beachColor,
            rockyColor,
            shallowColor,
            deepColor,
            emptyColor
    };
    public final float[] BIOME_COLOR_TABLE = new float[61], BIOME_DARK_COLOR_TABLE = new float[61];

    public void initialize()
    {
        initialize(0f, 0f, 0f, 1f);
    }

    public void initialize(float hue, float saturation, float brightness, float contrast)
    {
        float b, diff;
        for (int i = 0; i < 60; i++) {
            b = BIOME_TABLE[i];
            diff = (b % 1.0f - 0.48f) * 0.27f * contrast;
            BIOME_COLOR_TABLE[i] = b = SColor.toEditedFloat(diff >= 0
                    ? SColor.lightenFloat(biomeColors[(int)b], diff)
                    : SColor.darkenFloat(biomeColors[(int)b], -diff), hue, saturation, brightness, 0f);
            BIOME_DARK_COLOR_TABLE[i] = SColor.darkenFloat(b, 0.08f);
        }
        BIOME_COLOR_TABLE[60] = BIOME_DARK_COLOR_TABLE[60] = emptyColor;
    }

    /**
     * Initializes the colors to use for each biome (these are almost always mixed with other biome colors in practice).
     * Each parameter may be null to use the default for an Earth-like world; otherwise it should be a libGDX
     * {@link Color} or some subclass, like {@link SColor}. All non-null parameters should probably be fully opaque,
     * except {@code emptyColor}, which is only used for world maps that show empty space (like a globe, as produced by
     * {@link WorldMapGenerator.RotatingSpaceMap}).
     * @param desertColor hot, dry, barren land; may be sandy, but many real-world deserts don't have much sand
     * @param savannaColor hot, mostly-dry land with some parched vegetation; also called scrub or chaparral
     * @param tropicalRainforestColor hot, extremely wet forests with dense rich vegetation
     * @param grasslandColor prairies that are dry and usually wind-swept, but not especially hot or cold
     * @param woodlandColor part-way between a prairie and a forest; not especially hot or cold
     * @param seasonalForestColor forest that becomes barren in winter (deciduous trees); not especially hot or cold
     * @param temperateRainforestColor forest that tends to be slightly warm but very wet
     * @param borealForestColor forest that tends to be cold and very wet
     * @param tundraColor very cold plains that still have some low-lying vegetation; also called taiga 
     * @param iceColor cold barren land covered in permafrost; also used for rivers and lakes that are frozen
     * @param beachColor sandy or otherwise light-colored shorelines; here, these are more common in warmer places
     * @param rockyColor rocky or otherwise rugged shorelines; here, these are more common in colder places
     * @param shallowColor the color of very shallow water; will be mixed with {@code deepColor} to get most ocean colors
     * @param deepColor the color of very deep water; will be mixed with {@code shallowColor} to get most ocean colors
     * @param emptyColor the color used for empty space off the edge of the world map; may be transparent
     */
    public void initialize(
            Color desertColor,
            Color savannaColor,
            Color tropicalRainforestColor,
            Color grasslandColor,
            Color woodlandColor,
            Color seasonalForestColor,
            Color temperateRainforestColor,
            Color borealForestColor,
            Color tundraColor,
            Color iceColor,
            Color beachColor,
            Color rockyColor,
            Color shallowColor,
            Color deepColor,
            Color emptyColor
    )
    {
        biomeColors[ 0] = desertColor == null ? WorldMapView.desertColor : desertColor.toFloatBits();
        biomeColors[ 1] = savannaColor == null ? WorldMapView.savannaColor : savannaColor.toFloatBits();
        biomeColors[ 2] = tropicalRainforestColor == null ? WorldMapView.tropicalRainforestColor : tropicalRainforestColor.toFloatBits();
        biomeColors[ 3] = grasslandColor == null ? WorldMapView.grasslandColor : grasslandColor.toFloatBits();
        biomeColors[ 4] = woodlandColor == null ? WorldMapView.woodlandColor : woodlandColor.toFloatBits();
        biomeColors[ 5] = seasonalForestColor == null ? WorldMapView.seasonalForestColor : seasonalForestColor.toFloatBits();
        biomeColors[ 6] = temperateRainforestColor == null ? WorldMapView.temperateRainforestColor : temperateRainforestColor.toFloatBits();
        biomeColors[ 7] = borealForestColor == null ? WorldMapView.borealForestColor : borealForestColor.toFloatBits();
        biomeColors[ 8] = tundraColor == null ? WorldMapView.tundraColor : tundraColor.toFloatBits();
        biomeColors[ 9] = iceColor == null ? WorldMapView.iceColor : iceColor.toFloatBits();
        biomeColors[10] = beachColor == null ? WorldMapView.beachColor : beachColor.toFloatBits();
        biomeColors[11] = rockyColor == null ? WorldMapView.rockyColor : rockyColor.toFloatBits();
        biomeColors[12] = shallowColor == null ? WorldMapView.shallowColor : shallowColor.toFloatBits();
        biomeColors[13] = deepColor == null ? WorldMapView.deepColor : deepColor.toFloatBits();
        biomeColors[14] = emptyColor == null ? WorldMapView.emptyColor : emptyColor.toFloatBits();
        float b, diff;
        for (int i = 0; i < 60; i++) {
            b = BIOME_TABLE[i];
            diff = (b % 1.0f - 0.48f) * 0.27f;
            BIOME_COLOR_TABLE[i] = b = diff >= 0
                    ? SColor.lightenFloat(biomeColors[(int)b], diff)
                    : SColor.darkenFloat(biomeColors[(int)b], -diff);
            BIOME_DARK_COLOR_TABLE[i] = SColor.darkenFloat(b, 0.08f);
        }
        BIOME_COLOR_TABLE[60] = BIOME_DARK_COLOR_TABLE[60] = biomeColors[14];
        biomeColors[ 0] = WorldMapView.desertColor;
        biomeColors[ 1] = WorldMapView.savannaColor;
        biomeColors[ 2] = WorldMapView.tropicalRainforestColor;
        biomeColors[ 3] = WorldMapView.grasslandColor;
        biomeColors[ 4] = WorldMapView.woodlandColor;
        biomeColors[ 5] = WorldMapView.seasonalForestColor;
        biomeColors[ 6] = WorldMapView.temperateRainforestColor;
        biomeColors[ 7] = WorldMapView.borealForestColor;
        biomeColors[ 8] = WorldMapView.tundraColor;
        biomeColors[ 9] = WorldMapView.iceColor;
        biomeColors[10] = WorldMapView.beachColor;
        biomeColors[11] = WorldMapView.rockyColor;
        biomeColors[12] = WorldMapView.shallowColor;
        biomeColors[13] = WorldMapView.deepColor;
        biomeColors[14] = WorldMapView.emptyColor;
    }


    public void generate()
    {
        wildMap.generate();
        float baseColor = BIOME_COLOR_TABLE[wildMap.biome & 1023];
        int h = 1234567890, change, seed = wildMap.rng.nextInt();
        ICellVisible icv;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                change = h += IntPointHash.hashAll(x, y, 
                        seed);
                if((icv = viewer.get(wildMap.floorTypes.get(wildMap.floors[x][y]))) != null) 
                    colorMap[x][y] = SColor.toEditedFloat(icv.getPackedColor(),
                        0x1p-12f * ((h & 0xFF) - 0x9F + (change >>> 8 & 0x3F)),
                        0x1.8p-12f * ((h >>> 8 & 0xFF) - 0xB0 + (change >>> 16 & 0x3F)) - 0.0625f,
                        0x1.3p-12f * ((h >>> 16 & 0xFF) - 0x90 + (change >>> 24 & 0x3F)),
                        0f);
                else 
                    colorMap[x][y] = SColor.toEditedFloat(baseColor,
                        0x1p-12f * ((h & 0xFF) - 0x9F + (change >>> 8 & 0x3F)),
                        0x1.8p-12f * ((h >>> 8 & 0xFF) - 0xB0 + (change >>> 16 & 0x3F)) - 0.0625f,
                        0x1.3p-12f * ((h >>> 16 & 0xFF) - 0x90 + (change >>> 24 & 0x3F)),
                        0f);

            }
        }
    }
    
    public void show(SparseLayers layers)
    {
        ArrayTools.insert(colorMap, layers.backgrounds, 0, 0);
        int c;
        ICellVisible icv;
        for (int x = 0; x < width && x < layers.gridWidth; x++) {
            for (int y = 0; y < height && y < layers.gridHeight; y++) {
                if((c = wildMap.content[x][y]) >= 0 && (icv = viewer.get(wildMap.contentTypes.get(c))) != null)
                    layers.put(x, y, icv.getSymbol(), SColor.contrastLuma(icv.getPackedColor(), colorMap[x][y]));
                else if((icv = viewer.get(wildMap.floorTypes.get(wildMap.floors[x][y]))) != null)
                    layers.put(x, y, icv.getSymbol(), SColor.contrastLuma(icv.getPackedColor(), colorMap[x][y]));
            }
        }
    }
}
