SquidLib is a Java library that provides a full featured toolbox for working with turn based games in Swing and libGDX.
--  

SquidLib is used for Wyrm, Epigon, Attack the Geth, Assault Fish, and other projects.
    

Current Features:
--
###Ease Of Use
-   Standard GUI notation of (x,y) locations within the grid
-   Uses Swing components
-   Any Font can be used
-   Images may be used alongside characters in same panel
--      Characters can be used as a drop-in fallback mechanism!
-   Specify Grid and Font size multiple ways
--      Set number of cells in the grid and Font to be used
---        Size of cell is adjusted to optimally fit the Font size given
--      Set size of the cell, number of cells in the grid, and Font to be used
---         Font is dynamically resized to fit optimally within the cell
-   Font size and style can be changed on the fly
-   Multiple grids of different configurations can be used simultaneously in the same display
-   Multiple grids of different configurations can be overlayed allowing for transparency effects
-   Basic Swing animation support

###Highly Flexible
-   Can create multiple overlapping layers
--      Basic foreground & background color differences per cell
--      Multiple layers can be used to have multiple characters in a single cell
--      Multiple different sized layers can be used for sub-cell effects
--      Overlays can be used to show animation effects without disturbing the display
--      Overlays can be used to show potential Area of Effects or Ranges

###Lots of Color
-   SColor class extends Color and can be used as a drop-in replacement for any awt.Color needs
-   SColor chooser included to visually select named and adjusted colors
-   Over 500 named colors
-   Automatic color caching minimizes memory overhead
-   Can get a list of colors that are a gradient between two colors
-   Can perform LIBTCOD style "dark", "light", and "desaturate" commands on any color
-   Can get an arbitrary amount of blend between two colors

###Roguelike Specific Toolkit
-   Robust Field of View and Line of Sight system
-       Includes multiple options to fit the desired level of permissiveness for FOV and LOS.
-   Sound propagation system that can be used like Line of Sight, but for sounds that echo and pass through walls
-   Dijkstra Maps and A* can be used for pathfinding and other purposes.
-       DijkstraMap provides ways to get NPCs to flee, for ranged attackers to get to the right distance, and more
-       Several classes support multi-cell creatures, including DijkstraMap
-   Spill class implements randomized flood-fill, useful for spreading gases and other fluids

###Dungeon Generation Toolkit
-   Full-featured Herringbone Wang Tile dungeon generator
-       Herringbone Wang Tiles can produce less predictable, more varied dungeon layouts than BSP or other methods
-       Add water, doors, and traps to a dungeon by specifying the percentage of valid cells to affect
-       Many different styles of dungeon, from simple rectangular rooms and hallways to sinuous circular caverns
-   Alternate dungeon generators available, such as the one used by the original Rogue
-   Convert dungeons that use '#' for walls to use box drawing characters; this can also be used for graphical walls

###Fully Documented API
-   Each named color has a sample of its appearance in the Javadoc against multiple backgrounds
-   HTML browsing of the API shows these samples
-   Pop-up javadoc in NetBeans and Eclipse show these samples
-   Demos of all functionality included

###Math Toolkit
-   Custom extension(s) of Random allows drop-in replacement with added features
-       In addition to the usual Mersenne Twister, there's a XorShift128+ RNG and a SplitMix64 RNG (called LightRNG)
-       DharmaRNG can be used to make more or less "lucky" RNGs that skew towards high or low results
-       SobolQRNG produces deterministic results that may seem random, but tend to be more evenly distributed
-   Able to find Bresenham Lines for 2D and 3D coordinates.
--      Also can use Wu or Elias Lines (antialiased Bresenham Lines)
-   Perlin noise implementation
  
Download JARs from the Releases tab or use Maven Central to download with
```
<dependency>
  <groupId>com.squidpony</groupId>
  <artifactId>squidlib</artifactId>
  <version>2.3.0</version>
</dependency>
```

GitHub repository: https://github.com/SquidPony/SquidLib

Blog updates: http://squidpony.com/not-games/squidlib/

Developed by Eben Howard - howard@squidpony.com
