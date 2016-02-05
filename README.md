SquidLib is a Java library that provides a full featured toolbox for working with turn based games in Swing and libGDX.
--  

SquidLib is used for Wyrm, Epigon, Attack the Geth, Assault Fish, [Dungeon Mercenary](http://www.schplaf.org/hgames/), and other projects.

[![Join the slow-paced chat at https://gitter.im/SquidPony/SquidLib](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/SquidPony/SquidLib)

Current Features:
--
###Ease Of Use
-   Standard GUI notation of (x,y) locations within the grid
-   Uses Swing components or the scene2d.ui classes from libGDX
  -   Only one of either Swing or LibGDX is required to use to display a grid; using libGDX should work on desktop and Android, plus HTML with some caveats, as well as probably iOS via RoboVM (untested).
-   Any Font can be used
  -   This means TTF or OTF fonts in Swing
  -   For libGDX, it means bitmap fonts created in the AngelCode format, which can be generated from libGDX's Hiero tool using TTF or OTF fonts as input
-   Images may be used alongside characters in same panel
  -  Characters can be used as a drop-in fallback mechanism!
-   Specify Grid and Font size multiple ways
  -   Set number of cells in the grid and Font to be used
    -   Size of cell is adjusted to optimally fit the Font size given
  -   Set size of the cell, number of cells in the grid, and Font to be used
    -   Font is dynamically resized to fit optimally within the cell
-   Font size and style can be changed on the fly
-   Several fonts provided as resources, some narrow, some square, for unicode line drawing to work out-of-the-box
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
-   Starting with 3.0.0 beta 2, you can alter colors automatically using Filters.

###Roguelike Specific Toolkit
-   Robust Field of View and Line of Sight system
  -  Includes multiple options to fit the desired level of permissiveness for FOV and LOS
  -  Can handle directional FOV by simply specifying an angle and a span in degrees to cover with the FOV
-   Sound propagation system that can be used like Line of Sight, but for sounds that echo and pass through walls
-   Spill class implements randomized flood-fill, useful for spreading gases and other fluids
-   Splash is an easy-to-use implementation of randomized flood-fill for when you don't need all of Spill's features or bloat; it's new in 3.0.0 beta 3.

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
- MixedGenerator can produce maps that combine cave areas with artificial areas, starting in 3.0.0 beta 2.
  - A winding, snake-like path can be produced by SerpentMapGenerator, and has been adapted to generate multi-level dungeons with SerpentDeepMapGenerator. Both of these use MixedGenerator, and so can mix natural rock with worked stone.

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
-   SquidSetup produces a sample project with a heavily-documented basic example to get started.

###Math Toolkit
-   Custom extension(s) of Random allows drop-in replacement with added features
  -   In addition to the usual Mersenne Twister, there's a XorShift128+ RNG and a SplitMix64 RNG (called LightRNG)
    -   LightRNG can skip ahead or behind in its generated sequence, and it's the fastest of all the RNGs here.
  -   DharmaRNG can be used to make more or less "lucky" RNGs that skew towards high or low results
  -   SobolQRNG produces deterministic results that may seem random, but tend to be more evenly distributed
  -   DeckRNG should be less random during any particular span of random numbers, since it "shuffles" 16 numbers, from low to high, and draws them in a random order.
  -   PermutedRNG is fairly fast (not quite as fast as LightRNG), but has potential statistical advantages.
-   Able to find Bresenham Lines for 2D and 3D coordinates.
  -   Also can use Wu or Elias Lines (antialiased Bresenham Lines)
-   Perlin noise implementation
  -   Used to make Brogue-style "moving" water that works by altering the background lightness
  -   Also used for a world map generator in MetsaMapFactory

###Actively Developed
- Started in 2011 by SquidPony (Eben Howard), SquidLib has since picked up contributions from a number of developers around the world
- Development has accelerated recently as more people started adding code, with Tommy Ettinger working on things that aren't included in most other roguelike libraries, smelc and David Becker each contributing quite a few pull requests that help stability, performance, and code clarity, and still more developers helping by reporting and commenting on issues
- SquidLib 2.9.1 is pretty good
- SquidLib 3.0.0 will be better!
- SquidLib 3.0.0 now has a second beta! See the info below.
- Features already added in the beta include:
  - Use only the features you need; if you want the wide assortment of roguelike logic utilities, but don't want the text-based rendering (maybe because you're making a graphical game), you could include squidlib-util but nothing else.
  - Android support (and likely iOS via RoboVM), for both the logic utilities and text display
  - Better pathfinding for unusual monsters (you can tell it that a fish won't choose to leave water, a fire elemental will never choose to enter water, and an eccentric mystic won't enter doorways, for example)
    - The types of terrain are fully extensible to meet most games' pathfinding needs
  - FOV precomputation/caching/compression to make even large maps (up to 256x256) able to avoid overhead on numerous FOV calls (as well as some AOE calculations that use FOV)
    - Uncompressed FOV maps are extremely memory-hungry; a 256x256 dungeon with a simple 2D boolean array per cell, to track what cells each cell can see... uses more than 4GB of RAM
      - Yes, that's more RAM than any Java program can use with a 32-bit Java version; there *is* a better way
    - With the right compression techniques, memory usage can be reduced tremendously; preliminary testing predicts 20-50 MB for a full map with multiple FOV radii, and some games can expect even less.
    - Full FOV can be precomputed on multiple threads without users of the library needing to delve deep into concurrent code. The API is simple: generate a map before you use it, create an FOVCache for the unreached map, call `cacheAll()` on the FOVCache, and call `awaitCache()` later when the map needs to be used. No `java.lang.Thread` needed in your code!
    - You can even get information from compressed FOV maps without having to decompress them
      - The technique this uses is similar to that used by [JavaEWAH](https://github.com/lemire/javaewah), but ours is modified for data that is typically in a contiguous area of 2D space. JavaEWAH has gone through much more rigorous tests than SquidLib, and the RLE-like compression scheme has shown itself to be "best-in-class" for encoding large binary sequences (which is how we treat a compressed FOV map).
  - More attention paid to performance
    - Still, users of SquidLib shouldn't have to give up clear or safe code to benefit from what the library does internally
    - A major refactoring of code that used java.awt.Point produced the Coord class, which is immutable, never needs to be constructed more than once (each is cached, except in very rare cases), and should never need garbage collection either
  - Better documentation, we're really trying here
- Features not in the first beta but present in the second include:
  - More focus on colors in rendering.
    - Various "ColorCenter"s, such as `SquidColorCenter`, allow you to cache the Colors you fetch.
      - But SquidColorCenter goes further and allows you to filter colors with special effects like sepia tone, hue-shifting, or even psychedelic moving waves of bright color that change every frame
    - Swappable palettes are in consideration, and are partially implemented in some classes
    - HDR colors were considered but proved too cumbersome for users when the higher range wouldn't often be used
  - Some optimizations to FOVCache, particularly for memory usage while it is caching.
  - More and better dungeon generation techniques
    - DungeonGenerator allows users to request grass, mid-room boulders, or islands dotting large stretches of deep water to ensure a safe path
    - MixedGenerator takes points, draws rooms around them, then connects them with corridors, caves, or a mix of the two
    - SerpentMapGenerator uses MixedGenerator but tries to ensure a winding, snake-like path that must visit many rooms/caves
    - SymmetryDungeonGenerator also uses MixedGenerator but makes a yin-yang pattern of identical interlocking halves of a dungeon.
    - SerpentDeepMapGenerator works like SerpentMapGenerator, but requires passage up and down stairs to get to certain areas
  - More options for pathfinding
    - WaypointPathfinder precalculates paths between all doorways or other wide-to-narrow transitions, and can quickly fetch a path that it already knows.
    - DijkstraMap has a few more features, including pathfinding that tries to stay behind cover or out of sight.
- There's now a third beta!
  - Distance field fonts! These resize very smoothly and should drastically reduce the number of fonts needed to implement zooming or adapt to multiple screen sizes.
  - There's "imitation foreign language" generation in FakeLanguageGen, including the ability to mix styles of generated language or scripts.
  - There's also some early random monster description generation.
  - You can now analyze a map with RoomFinder after it's been generated to find likely rooms and corridors in it, for various uses.
  - You can associate regions from CoordPacker with values (something that can't be done with HashMap normally), and query a point to find overlapping regions that contain that point.
  - SpatialMap provides a common convenience by allowing values to be indexed by a key or by a Coord position, and updates all 3 of these as one entry.
  - IColoredString has lots of new useful features, including justified and wrapped text, on top of its existing multi-colored text.
  - SquidMessageBox provides a simple scrolling message box that be used as a scene2d.ui widget, and can display IColoredString data.
- But, 3.0.0's final release will be major, and so should be expected to *break* API backwards compatibility
  - Any minor releases after 3.0.0 and before 4.0.0 should be expected to *keep* API backwards compatibility, unless a feature is broken or unusable
  - The most significant change in 3.0.0 will be the removal of the Swing-based rendering and full transition to the similar, but much faster and more responsive, libGDX renderer
  - 3.0.0-b1 is the last release to contain Swing. If you're porting code that used an earlier version of SquidLib and need Swing for some reason, you may want to stay with the largely-compatible 2.9.1 instead of the very-different 3.0.0-b1.
    - This should also enable SquidLib to be used for rendering on Android/iOS and not only the desktop platforms Swing is limited to
  - There is now a tool that sets up a project for people who want an easy way to handle the dependencies of SquidLib and/or libGDX
    - We now have SquidSetup to automatically handle the setup of a new project that uses SquidLib 3.0.0-b3, including fetching dependencies automatically and setting up a project that potentially targets both desktop and Android, potentially HTML, and possibly iOS as well.
    - If you already use Maven, Gradle, SBT, Leiningen, or some other dependency manager, upgrading should be easier to the 3.0.0 series
    - If you don't, you should, and SquidSetup should handle the hard parts for you.

Download
--

Download JARs for older versions from the Releases tab, use Maven Central to download the latest version with your choice of features, or simply use SquidSetup to make a new project configured the way libGDX prefers to work, and copy in any code you might already have.

Ideally, if you're just starting out you should use SquidSetup. This is [the most recent release](https://github.com/SquidPony/SquidLib/releases/tag/v3.0.0-b3). If your older code did not use a dependency manager like Maven or Gradle, you will have a hard time updating without SquidSetup, so the recommended approach in that case is to make a new project in a folder of your choice, copy all of your old code over into the new project, and if you're satisfied, then copy it back to replace your old project with the reorganized and updated code. If you're updating an existing project that does use a dependency manager, you may not want to create a whole new starting point and copy over your old code, though that can still be a good option. You can instead update your dependencies. All projects need `squidlib-util`, which contains the core logic that anything that uses SquidLib needs, but if you depend on the text-based display module then it will be downloaded automatically as part of depending on the display module. If you use the logic module on its own and handle graphics yourself, you should update your existing Maven, Gradle, etc. project with the [Dependency Information here](http://search.maven.org/#artifactdetails%7Ccom.squidpony%7Csquidlib-util%7C3.0.0-b2%7Cjar). If your project used text-based display in SquidLib 3.0.0-b1, then you need to change a few things.
  - If you used Swing before, you will need to change to use LibGDX. The API is relatively similar, but the typical folder layout is not. You should probably either stick with b1 or earlier, or use SquidSetup.
  - If you used libGDX before, the name of the dependency changed from `squidlib-gdx` in 3.0.0-b1, to just `squidlib` in 3.0.0-b2 and 3.0.0-b3. It depends on `squidlib-util` and the core LibGDX library (`gdx`), but needs to be told in this version what platform it targets (this avoids distributing the large LWJGL library, which is used only on desktop, to Android or other platforms that it just weighs down). 

This last step is slightly more involved than before, and is part of the rationale for making SquidSetup. Using Maven for a desktop project, you would use this dependency in the desktop pom.xml:
```
<dependency>
  <groupId>com.squidpony</groupId>
  <artifactId>squidlib</artifactId>
  <version>3.0.0-b3</version>
</dependency>
<dependency>
    <groupId>com.badlogicgames.gdx</groupId>
    <artifactId>gdx-backend-lwjgl</artifactId>
    <version>1.9.1</version>
</dependency>
<dependency>
    <groupId>com.badlogicgames.gdx</groupId>
    <artifactId>gdx-platform</artifactId>
    <version>1.9.1</version>
    <classifier>natives-desktop</classifier>
</dependency>
```
Using Maven for an Android project, you would use these dependencies in the Android pom.xml:
```
<dependency>
  <groupId>com.squidpony</groupId>
  <artifactId>squidlib</artifactId>
  <version>3.0.0-b3</version>
</dependency>
<dependency>
    <groupId>com.badlogicgames.gdx</groupId>
    <artifactId>gdx-backend-android</artifactId>
    <version>1.9.1</version>
</dependency>
<dependency>
    <groupId>com.badlogicgames.gdx</groupId>
    <artifactId>gdx-platform</artifactId>
    <version>1.9.1</version>
    <classifier>natives-armeabi</classifier>
</dependency>
<dependency>
    <groupId>com.badlogicgames.gdx</groupId>
    <artifactId>gdx-platform</artifactId>
    <version>1.9.1</version>
    <classifier>natives-armeabi-v7a</classifier>
</dependency>
<dependency>
    <groupId>com.badlogicgames.gdx</groupId>
    <artifactId>gdx-platform</artifactId>
    <version>1.9.1</version>
    <classifier>natives-x86</classifier>
</dependency>
```

HTML support is new in 3.0.0-b3, and you should really use SquidSetup to try it out; a lot of resources are needed in specific places, and I don't know of a good way to get them laid out without using a setup tool.

If you really, truly want to use Swing, you need to use 2.9.1 or 3.0.0-b1.  This is the dependency for SquidLib using the Swing renderer and version 3.0.0-b1, which has known bugs that are fixed in newer versions, but otherwise should "work" on Windows, Mac OS X, and Linux:
```
<dependency>
  <groupId>com.squidpony</groupId>
  <artifactId>squidlib</artifactId>
  <version>3.0.0-b1</version>
</dependency>
```

There's also the last stable release in the 2.x series, which optionally depends on libGDX but will not work on mobile devices:
```
<dependency>
  <groupId>com.squidpony</groupId>
  <artifactId>squidlib</artifactId>
  <version>2.9.1</version>
</dependency>
```

If you want to use the LibGDX code in 2.9.1 (anything in a package with "gdx" in it, in that release), you need to depend on (an earlier version of) libGDX.
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
(This is not a recent version of LibGDX, and it may work with more recent versions, but no guarantees can be made. `squidlib` 3.0.0-b3 depends on libGDX 1.9.1, `squidlib` 3.0.0-b2 depends on libGDX 1.7.1, and they have similar behavior about dependency handling. `squidlib-gdx` 3.0.0-b1 handles its dependencies on its own, so you don't need this step, except because of that behavior it will always download the desktop dependencies, even when they do nothing on the current platform.)

GitHub repository: https://github.com/SquidPony/SquidLib

Blog updates: http://squidpony.com/not-games/squidlib/

Created by Eben Howard - howard@squidpony.com  
Currently developed by Tommy Ettinger - tommy.ettinger@gmail.com

Additional work has been greatly appreciated by a team of contributors. smelC and David Becker have each done excellent work in improving and modernizing SquidLib in all sorts of ways. Don't be shy about posting issues! Many of SquidLib's biggest and best changes have been motivated by issues posted by users, including the port to Android!
