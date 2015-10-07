SquidLib is a Java library that provides a full featured toolbox for working with turn based games in Swing and libGDX.
--  

SquidLib is used for Wyrm, Epigon, Attack the Geth, Assault Fish, and other projects.

[![Join the slow-paced chat at https://gitter.im/SquidPony/SquidLib](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/SquidPony/SquidLib)

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
  -   Also used for a world map generator in MetsaMapFactory

###Actively Developed
- Started in 2011 by SquidPony (Eben Howard), SquidLib has since picked up contributions from a number of developers around the world
- Development has accelerated recently as more people started adding code, with Tommy Ettinger working on things that aren't included in most other roguelike libraries, smelc and David Becker each contributing quite a few pull requests that help stability, performance, and code clarity, and still more developers helping by reporting and commenting on issues
- SquidLib 2.9.1 is pretty good right now
- SquidLib 3.0.0 will be better! Features already added or in development, and expected to be in 3.0.0, include:
  - Use only the features you need; if you want the wide assortment of roguelike logic utilities, but don't want the text-based rendering (maybe because you're making a graphical game), you could include squidlib-util but nothing else.
  - Android support (and likely iOS via RoboVM), for both the logic utilities and text display
  - Better pathfinding for unusual monsters (you can tell it that a fish won't choose to leave water, a fire elemental will never choose to enter water, and an eccentric mystic won't enter doorways, for example)
    - The types of terrain are fully extensible to meet most games' pathfinding needs
  - FOV precomputation/caching/compression to make even large maps (up to 256x256) able to avoid overhead on numerous FOV calls (as well as some AOE calculations that use FOV)
    - Uncompressed FOV maps are extremely memory-hungry; a 256x256 dungeon with a simple 2D boolean array per cell, to track what cells each cell can see... uses more than 4GB of RAM
      - Yes, that's more RAM than any Java program can use with a 32-bit Java version; there *is* a better way
    - With the right compression techniques, memory usage can be reduced tremendously; preliminary testing predicts 20-50 MB for a full map with multiple FOV radii, and some games can expect even less.
    - You can even get information from compressed FOV maps without having to decompress them
  - More attention paid to performance
    - Still, users of SquidLib shouldn't have to give up clear or safe code to benefit from what the library does internally
    - A major refactoring of code that used java.awt.Point produced the Coord class, which is immutable, never needs to be constructed more than once (each is cached, except in very rare cases), and should never need garbage collection either
  - Better documentation, we're really trying here
- But, 3.0.0 will be a major release, and so should be expected to *break* API backwards compatibility
  - Any minor releases after 3.0.0 and before 4.0.0 should be expected to *keep* API backwards compatibility, unless a feature is broken or unusable
  - The most significant change in 3.0.0 will be the removal of the Swing-based rendering and full transition to the similar, but much faster and more responsive, libGDX renderer
    - This should also enable SquidLib to be used for rendering on Android/iOS and not only the desktop platforms Swing is limited to
    - It also should allow some other changes to take place, from split-screen text panels and overlaid, translucent, partially-offset panels, to HDR color and the more elaborate visual effects that allows
  - There will be complete documentation on how to set up a project for people just starting with SquidLib and/or libGDX
    - If you already use Maven, Gradle, SBT, Leiningen, or some other dependency manager, upgrading should be easier.
    - If you don't, you should, but there's no requirement to start using one

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
