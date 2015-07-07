package squidpony.squidgrid.gui;

import squidpony.SColor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * A helper class to make using multiple SquidPanels easier.
 *
 * Created by Tommy Ettinger on 7/6/2015.
 */
public class SquidLayers extends JLayeredPane {
    protected int width, height;
    protected SquidPanel backgroundPanel, lightnessPanel, foregroundPanel;
    int[][] bgIndices, lightnesses, coloredLightIndices;
    protected ArrayList<SquidPanel> additionalPanels;
    protected TextCellFactory textFactory;
    protected ArrayList<Color> palette, lightingPalette;

    public SquidLayers()
    {
        super();
    }
    public  SquidLayers(int gridWidth, int gridHeight)
    {
        palette = new ArrayList<Color>(64);
        palette.add(SColor.DARK_SLATE_GRAY);
        palette.add(SColor.IVORY);
        palette.add(SColor.SLATE_GRAY);
        palette.add(SColor.SILVER_GREY);
        palette.add(SColor.RUST);
        palette.add(SColor.WATER);
        palette.add(SColor.INTERNATIONAL_ORANGE);

        lightingPalette = new ArrayList<Color>(512);
        lightingPalette.add(0, SColor.TRANSPARENT);
        for(int i = 1; i < 256; i++)
        {
            lightingPalette.add(256 + i, new SColor(255, 255, 255, i));
            lightingPalette.add(256 - i, new SColor(0, 0, 0, i));
        }
        lightingPalette.add(256, SColor.TRANSPARENT);

        width = gridWidth;
        height = gridHeight;
        bgIndices = new int[width][height];
        lightnesses = new int[width][height];
        coloredLightIndices = new int[width][height];
        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                lightnesses[x][y] = 256;
                coloredLightIndices[x][y] = 1;
            }
        }

        textFactory = new TextCellFactory().width(16).height(16);

        backgroundPanel = new SquidPanel(gridWidth, gridHeight);
        lightnessPanel = new SquidPanel(gridWidth, gridHeight);
        foregroundPanel = new SquidPanel(gridWidth, gridHeight);

        backgroundPanel.refresh();
        lightnessPanel.refresh();
        foregroundPanel.refresh();

        additionalPanels = new ArrayList<SquidPanel>();

        this.setLayer(backgroundPanel, 0);
        this.setLayer(lightnessPanel, 1);
        this.setLayer(foregroundPanel, 3);
    }

    public  SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight)
    {
        palette = new ArrayList<Color>(64);
        palette.add(SColor.DARK_SLATE_GRAY);
        palette.add(SColor.IVORY);
        palette.add(SColor.SLATE_GRAY);
        palette.add(SColor.SILVER_GREY);
        palette.add(SColor.RUST);
        palette.add(SColor.WATER);
        palette.add(SColor.INTERNATIONAL_ORANGE);

        lightingPalette = new ArrayList<Color>(512);
        lightingPalette.add(0, SColor.TRANSPARENT);
        for(int i = 1; i < 256; i++)
        {
            lightingPalette.add(256 + i, new SColor(255, 255, 255, i));
            lightingPalette.add(256 - i, new SColor(0, 0, 0, i));
        }
        lightingPalette.add(256, SColor.TRANSPARENT);

        width = gridWidth;
        height = gridHeight;

        bgIndices = new int[width][height];
        lightnesses = new int[width][height];
        coloredLightIndices = new int[width][height];
        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                lightnesses[x][y] = 256;
                coloredLightIndices[x][y] = 1;
            }
        }

        textFactory = new TextCellFactory().width(cellWidth).height(cellHeight);

        backgroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);
        lightnessPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);
        foregroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);

        backgroundPanel.refresh();
        lightnessPanel.refresh();
        foregroundPanel.refresh();

        additionalPanels = new ArrayList<SquidPanel>();

        this.setLayer(backgroundPanel, 0);
        this.setLayer(lightnessPanel, 1);
        this.setLayer(foregroundPanel, 3);
    }
    public  SquidLayers(int gridWidth, int gridHeight, int cellWidth, int cellHeight, Font font)
    {
        palette = new ArrayList<Color>(64);
        palette.add(SColor.DARK_SLATE_GRAY);
        palette.add(SColor.IVORY);
        palette.add(SColor.SLATE_GRAY);
        palette.add(SColor.SILVER_GREY);
        palette.add(SColor.RUST);
        palette.add(SColor.WATER);
        palette.add(SColor.INTERNATIONAL_ORANGE);

        lightingPalette = new ArrayList<Color>(512);
        lightingPalette.add(0, SColor.TRANSPARENT);
        for(int i = 1; i < 256; i++)
        {
            lightingPalette.add(256 + i, new SColor(255, 255, 255, i));
            lightingPalette.add(256 - i, new SColor(0, 0, 0, i));
        }
        lightingPalette.add(256, SColor.TRANSPARENT);

        width = gridWidth;
        height = gridHeight;

        bgIndices = new int[width][height];
        lightnesses = new int[width][height];
        coloredLightIndices = new int[width][height];
        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                lightnesses[x][y] = 256;
                coloredLightIndices[x][y] = 1;
            }
        }

        textFactory = new TextCellFactory().font(font).width(cellWidth).height(cellHeight);

        backgroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);
        lightnessPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);
        foregroundPanel = new SquidPanel(gridWidth, gridHeight, textFactory, null);

        backgroundPanel.refresh();
        lightnessPanel.refresh();
        foregroundPanel.refresh();

        additionalPanels = new ArrayList<SquidPanel>();

        this.setLayer(backgroundPanel, 0);
        this.setLayer(lightnessPanel, 1);
        this.setLayer(foregroundPanel, 3);
    }

}
