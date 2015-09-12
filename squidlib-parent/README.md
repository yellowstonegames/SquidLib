SquidLib is a Java library that provides a full featured toolbox for working with turn based games in Swing and libGDX.
--  

SquidLib is used for Wyrm, Epigon, Attack the Geth, Assault Fish, and other projects.
    

Current Features:
--
###Ease Of Use
-   Standard GUI notation of (x,y) locations within the grid
-   Uses Swing components or the scene2d.ui classes from libGDX
  -   LibGDX is an optional dependency; if you don't use it, you don't need to include it (unless building SquidLib yourself)
-   Any Font can be used
  -   This means TTF or OTF fonts in Swing
  -   For libGDX, it means bitmap fonts created by the libGDX tool Hiero, which uses TTF or OTF fonts as input
-   Images may be used alongside characters in same panel
  -  (libGDX does not currently support mixing images with characters, but will in a future version) 
  -  Characters can be used as a drop-in fallback mechanism!
-   Specify Grid and Font size multiple ways
  -   Set number of cells in the grid and Font to be used
    -   Size of cell is adjusted to optimally fit the Font size given
  -   Set size of the cell, number of cells in the grid, and Font to be used
    -   Font is dynamically resized to fit optimally within the cell
-   Font size and style can be changed on the fly
-   Several fonts provided as resources, one narrow, one square, for unicode line drawing to work out-of-the-box
-   Multiple grids of different configurations can be used simultaneously in the same display
-   Multiple grids of different configurations can be overlayed allowing for transparency effects
  -   A convenience class, SquidLayers provides foreground and background setting with this
  -   SquidLayers also allows background brightness changes (such as from torchlight) with just an int argument
    -   This works by keeping a partly-transparent layer of black or white for darkening or lightening
-   Basic Swing animation support
-   More robust libGDX animation support, with much better performance than Swing animations

###Highly Flexible
-   Can create multiple overlapping layers
  -   Basic foreground & background color differences per cell
    -  Using a partially-transparent, full-cell tile in an overlay can change the colors below it 
  -   Multiple layers can be used to have multiple characters in a single cell
  -   Multiple different sized layers can be used for sub-cell effects
  -   Overlays can be used to show animation effects without disturbing the display
  -   Overlays can be used to show potential Areas of Effect or Ranges
    -   If using libGDX, there is a tint animation that can be used to highlight an area or cell without using overlays

###Lots of Color
-   SColor class extends Color and can be used as a drop-in replacement for any awt.Color needs
-   SColor chooser included to visually select named and adjusted colors
-   Over 500 named colors
-   Automatic color caching minimizes memory overhead
-   Can get a list of colors that are a gradient between two colors
-   Can perform LIBTCOD style "dark", "light", and "desaturate" commands on any color
-   Can get an arbitrary amount of blend between two colors
-   Colors are also available as standard java.awt.Color constants in the Colors class

###Roguelike Specific Toolkit
-   Robust Field of View and Line of Sight system
  -  Includes multiple options to fit the desired level of permissiveness for FOV and LOS
  -  Can handle directional FOV by simply specifying an angle and a span in degrees to cover with the FOV
-   Sound propagation system that can be used like Line of Sight, but for sounds that echo and pass through walls
-   Spill class implements randomized flood-fill, useful for spreading gases and other fluids

###Dungeon Generation Toolkit
-   Full-featured Herringbone Wang Tile dungeon generator
  -   Herringbone Wang Tiles can produce less predictable, more varied dungeon layouts than BSP or other methods
  -   Add water, doors, and traps to a dungeon by specifying the percentage of valid cells to affect
  -   Many different styles of dungeon, from simple rectangular rooms and hallways to sinuous circular caverns
-   Alternate dungeon generators available, such as the one used by the original Rogue
-   Convert dungeons that use `#` for walls to use box drawing characters; this can also be used for graphical walls
-   Convenience functions/constructors let you use the `char[][]` dungeons to easily build other grid things
  -   DijkstraMap will have walls automatically placed in as obstacles if passed a `char[][]` when it's constucted
  -   FOV resistance maps can be generated automatically by DungeonUtility given a `char[][]`

###SquidAI Pathfinding code
-   Dijkstra Maps and A* can be used for pathfinding and other purposes.
  -   DijkstraMap provides support for getting to a target, avoiding paths that would make you stop in an invalid cell.
  -   DijkstraMap supports fleeing monsters, optionally sharing one "flee map" for all monsters fleeing the hero.
  -   DijkstraMap can be given a Technique that contains a minimum and maximum range, and an Area of Effect, and it will pathfind to a relatively good place to use that technique.
    -   There are many kinds of Area of Effect (AOE) provided, and given the right information, they can calculate the best place to position that AOE to hit as many targets as possible (not an easy task, but it gets calculated quickly).
  -   DijkstraMap can partially scan an area, stopping once it reaches a given distance.
  -   Several classes support multi-cell creatures, including DijkstraMap
   
###Fully Documented API
-   Each named color has a sample of its appearance in the Javadoc against multiple backgrounds
  -   This can be harder to access in IntelliJ, so the Colors class has constants that IDEA can preview easily
-   HTML browsing of the API shows these samples
-   Pop-up javadoc in NetBeans and Eclipse show these samples
-   Demos of all functionality included
-   EverythingDemo shows off lots of features and is fully documented; a good place to start
-   SquidAIDemo has two AI teams fight each other with large area-of-effect attacks.

###Math Toolkit
-   Custom extension(s) of Random allows drop-in replacement with added features
  -   In addition to the usual Mersenne Twister, there's a XorShift128+ RNG and a SplitMix64 RNG (called LightRNG)
    -   LightRNG can skip ahead or behind in its generated sequence.
  -   DharmaRNG can be used to make more or less "lucky" RNGs that skew towards high or low results
  -   SobolQRNG produces deterministic results that may seem random, but tend to be more evenly distributed
-   Able to find Bresenham Lines for 2D and 3D coordinates.
  -   Also can use Wu or Elias Lines (antialiased Bresenham Lines)
-   Perlin noise implementation
  -   Used to make Brogue-style "moving" water that works by altering the background lightness
  
Download JARs from the Releases tab or use Maven Central to download with
```
<dependency>
  <groupId>com.squidpony</groupId>
  <artifactId>squidlib</artifactId>
  <version>2.9.1</version>
</dependency>
```

If you want to use the LibGDX code (anything in a package with "gdx" in it), you need to depend on libGDX.
```
<dependency>
    <groupId>com.badlogicgames.gdx</groupId>
    <artifactId>gdx</artifactId>
    <version>1.6.4</version>
</dependency>
<dependency>
    <groupId>com.badlogicgames.gdx</groupId>
    <artifactId>gdx-backend-lwjgl</artifactId>
    <version>1.6.4</version>
</dependency>
<dependency>
    <groupId>com.badlogicgames.gdx</groupId>
    <artifactId>gdx-platform</artifactId>
    <version>1.6.4</version>
    <classifier>natives-desktop</classifier>
</dependency>
```
(This is not the absolute-most-recent version of LibGDX, and it may work with more recent versions, but no guarantees can be made. SquidLib will update its dependency version when needed, like in the case of the breaking changes between LibGDX 1.5.5 and 1.5.6 that affected text rendering. SquidLib may also update dependencies simply because there's a major release.)

GitHub repository: https://github.com/SquidPony/SquidLib

Blog updates: http://squidpony.com/not-games/squidlib/

Developed by Eben Howard - howard@squidpony.com
