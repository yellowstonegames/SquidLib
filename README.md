SquidLib is a Java library that provides a full featured toolbox for working with turn based games in Swing and libGDX.
--  

SquidLib is used for Wyrm, Epigon, Attack the Geth, Assault Fish, [Dungeon Mercenary](http://www.schplaf.org/hgames/), and other projects.

[![Join the slow-paced chat at https://gitter.im/SquidPony/SquidLib](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/SquidPony/SquidLib)

Documentation:
---
Jars of javadocs are distributed with each release via Maven Central, and with the current latest via JitPack. You can
get the docs and source of the latest version, 3.0.0-b6, in two parts for each; squidlib-util (the core of the library,
and also the largest part) has its
[docs here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-util/3.0.0-b6/squidlib-util-3.0.0-b6-javadoc.jar),
and [source here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-util/3.0.0-b6/squidlib-util-3.0.0-b6-sources.jar),
while squidlib (the display part of the library, named the way it is because depending on squidlib should also pull in
squidlib-util to make it a "one-stop shop" dependency) has its
[docs here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib/3.0.0-b6/squidlib-3.0.0-b6-javadoc.jar),
and [source here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib/3.0.0-b6/squidlib-3.0.0-b6-sources.jar).
You can browse the javadocs of the most recent commit
[here for squidlib-util](https://jitpack.io/com/github/SquidPony/SquidLib/squidlib-util/-SNAPSHOT/javadoc/) and
[here for squidlib](https://jitpack.io/com/github/SquidPony/SquidLib/squidlib/-SNAPSHOT/javadoc/), though JitPack will often
need to rebuild the docs if there was a recent commit, which may take (according to their FAQ) up to 15 minutes; refresh
if it isn't loading or looks wrong.

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
-   Multiple grids of different configurations can be overlaid allowing for transparency effects
  -   A convenience class, SquidLayers provides foreground and background setting with this
  -   SquidLayers also allows background brightness changes (such as from torchlight) with just an int argument
    -   This works by keeping a partly-transparent layer of black or white for darkening or lightening
-   Basic Swing animation support
-   More robust libGDX animation support, with much better performance than Swing animations
-   Starting with 3.0.0 beta 5, there is support for variable-width fonts in message boxes, which can be much more legible
  -   More UI controls may be able to support variable-width fonts in the future

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
-   Starting with 3.0.0 beta 2, you can alter colors automatically using Filters
-   Starting with 3.0.0 beta 5, lots of options are available for generating gradients, including with Filters, and gradients that wrap around like a rainbow

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
-   Big improvements in 3.0.0 beta 5, including...
  -   SectionDungeonGenerator, which can put different dungeon features in different areas (grass or moss may grow in caves but not rooms, for instance)
  -   More maze generators and improvements to existing ones, including an arcade-reminiscent PacMazeGenerator
  -   ModularMapGenerator can produce repeated sections of map, like one common style of room, for military or sci-fi themed areas
  -   DenseRoomMapGenerator produces a cramped, over-filled section of purely rectangular rooms, with periodic breaks for doors.
  -   OrganicMapGenerator makes nice caves using properties of Perlin noise to produce open cave areas and the WobblyLine line drawing class to connect them.

###SquidAI Pathfinding code
-   Dijkstra Maps and A* can be used for pathfinding and other purposes.
  -   DijkstraMap provides support for getting to a target, avoiding paths that would make you stop in an invalid cell.
  -   DijkstraMap supports fleeing monsters, optionally sharing one "flee map" for all monsters fleeing the hero.
  -   DijkstraMap can be given a Technique that contains a minimum and maximum range, and an Area of Effect, and it will pathfind to a relatively good place to use that technique.
    -   There are many kinds of Area of Effect (AOE) provided, and given the right information, they can calculate the best place to position that AOE to hit as many targets as possible (not an easy task, but it gets calculated quickly).
  -   DijkstraMap can partially scan an area, stopping once it reaches a given distance.
  -   Several classes support multi-cell creatures, including DijkstraMap
  -   DijkstraMap is currently recommended for pathfinding because it has been optimized much more heavily than AStarSearch
    -   AStarSearch is expected to receive some optimization in the near future, but this isn't in beta 5.
   
###Fully Documented API
-   Each named color has a sample of its appearance in the Javadoc against multiple backgrounds
  -   This can be harder to access in IntelliJ, so the Colors class has constants that IDEA can preview easily
-   HTML browsing of the API shows these samples
-   Pop-up javadoc in NetBeans and Eclipse show these samples
-   Demos of all functionality included
-   EverythingDemo shows off lots of features and is fully documented; a good place to start
-   SquidAIDemo has two AI teams fight each other with large area-of-effect attacks.
-   Several other demos are smaller and meant to test individual features, like CoveredPathDemo to test DijkstraMap's pathfinding while avoiding ranged attackers.
-   SquidSetup produces a sample project with a heavily-documented basic example to get started.

###Math Toolkit
-   Custom extension(s) of Random allows drop-in replacement with added features
  -   In addition to the common Mersenne Twister (now deprecated), there's...
    -   XorRNG, which is a XorShift128+ RNG
    -   XoRoRNG, which is a xoroshiro128+ RNG using a very recently-published algorithm
      -   XoRoRNG is very fast and has good properties for heavy usage
    -   LightRNG, which is a SplitMix64 RNG
      -   LightRNG can skip ahead or behind in its generated sequence, and it's the fastest of all the RNGs here, in a virtual tie with XoRoRNG
    -   LongPeriodRNG, which is a XorShift1024* RNG (it preferred to replace MersenneTwister for applications like shuffling large sequences, and is much faster)
    -   PermutedRNG is fairly fast (not quite as fast as LightRNG), but has potential statistical advantages.
  -   DharmaRNG can be used to make more or less "lucky" RNGs that skew towards high or low results
  -   So can EditRNG, but EditRNG also allows tweaking the "centrality" of the numbers it generates, and has an easier-to-understand expected average (recommended for luck alteration in RNGs)
  -   SobolQRNG produces deterministic results that may seem random, but tend to be more evenly distributed
  -   DeckRNG should be less random during any particular span of random numbers, since it "shuffles" 16 numbers, from low to high, and draws them in a random order.
-   Able to find Bresenham Lines for 2D and 3D coordinates.
  -   Also can use Wu or Elias Lines (antialiased Bresenham Lines)
  -   Also several other line drawing algorithms, including one that only makes orthogonal movements, another with options to make wider lines, and another that wiggles in random directions and makes a randomized line toward a goal.
-   Perlin noise implementation
  -   Used to make Brogue-style "moving" water that works by altering the background lightness
  -   Also used for a world map generator in MetsaMapFactory
-   Lots of code for dealing with oddly-shaped or non-contiguous regions in CoordPacker and some other classes
  -   RegionMap can use such regions as keys in a Map-like data structure, and find all regions that overlap with a point
  -   CoordPacker is heavily used internally, but shouldn't need to be used in most code that uses SquidLib

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
    - This turns out to be mostly unneeded on its own, since SquidLib's FOV performs well enough to recompute FOV maps quickly, but led to development of more useful techniques for dungeon generation via CoordPacker
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
- And a fourth beta, called 3.0.0-b5 because b4 had a single-character-wrong bug in the configuration that prevented its use.
  - HTML works for sure as a target
    - Lots of things needed changing to get this in a solid state, but now we can be confident SquidLib works with GWT to generate HTML pages
  - Things are split up more now and the dependencies on SquidLib should add less to application size
    - You can download pre-made assets, like fonts, separately [from here](https://github.com/tommyettinger/SquidSetup/releases/tag/v3.0.0-LATEST) and delete them from your project if unused
  - Advancements on the UI front
    - VisualInput allows on-screen commands to take the place of a keyboard on Android (and iOS, once libGDX is able to compile to it again)
    - TextPanel allows a variable-width font to be used in a message screen, not limited to the grid; this can be much nicer to read
      - LinesPanel does this too
    - ColorChangeLabel can be useful for creatures that have an uncertain color or have multiple colors in games where color is related to other gameplay elements
      - It doesn't count as an animation, but continues to change color on its own without interrupting existing animations
  - Multiple new dungeon generation tools
    - The Placement class gives the ability to analyze rooms to find corners, large flat walls (for weapon racks, blackboards, etc.), or the approximate centers of rooms (useful to place thrones, shrines, or other important places)
    - For places where RoomFinder is too slow for a large map, there's RectangleRoomFinder, which can find many of the needed features to place some things without doing as detailed of an analysis of the dungeon
    - New dungeon generators mentioned earlier, including SectionDungeonGenerator, ModularMapGenerator, PacMazeGenerator, OrganicMapGenerator, DenseRoomMapGenerator, LanesMapGenerator, etc.
  - Code ported from Uncommon Maths (and expanded) allows iterating through permutations or combinations of a small collection, and random access of a permutation when given a number index
  - FakeLanguageGen has had multiple updates and supports more stylistic elements to add into languages, including adding or removing accented glyphs or mimicking the feature of some languages where a word may be redoubled (which would be virtually impossible to generate using FakeLanguageGen's technique without specific support for "hindsight" into syllables generated earlier).
    - LanguageCipher can use FakeLanguageGen to produce deterministic "translations" of a source text into a fake language, and then allows deciphering with a vocabulary that the player character might learn over time
  - Only a few places in SquidLib make heavy use of regular expressions, but there's no support for the standard library's regex package on GWT for targeting HTML
    - This is why the RegExodus project was formed, and SquidLib depends on this small lib to allow FakeLanguageGen, LanguageCipher, and Dice to work on HTML
- Now another beta, 3.0.0-b6.
  - Primarily a bugfix release, but also has some new features
  - MimicFill is a port of [ConvChain](https://github.com/mxgmn/ConvChain) and allows a square boolean 2D array to be used as a "stylistic basis" for filling an unlimited region with an imitation of the original square's style
  - Thesaurus allows replacement of words with their synonyms for procedural text, or replacement of special keywords with one of a list of words in a thematic field, like "plural nouns referring to empires, kingdoms, dynasties, or similar large nations"
  - More color stuff, including saturation modifying methods in IColorCenter and all its implementations, plus an odd gradient type that varies more in SquidColorCenter
  - Improvements to FakeLanguageGen, including more languages it can imitate and better handling of modifiers
  - Improvements to LanguageCipher as well, allowing library users to choose how much to cache, or to make it so a single fake word virtually-never can be generated as the translation for multiple source words
  - LinesPanel is simpler, allows line wrapping, and a choice of vertical direction for line layout
  - Over 2000 icons from http://game-icons.net added to the optional assets download, with a distance field effect applied so they can be used along with "stretchable" fonts, and all this put in a texture atlas for efficient lookup
  - Various other small improvements, like RNG and RandomnessSource allowing all their variants to be copied with `copy()`, GapShuffler being added (used in Thesaurus
    to ensure the same word doesn't show up twice in rapid succession, which repeated loops through sequences shuffled with `RNG.shuffle()` can't guarantee), and Maker
    being added to help construction of cumbersome objects, like a LinkedHashMap, with one method call
- In the upcoming next release, but not yet in a beta release (only in the master revision):
  - Some breaking changes, though they should be easy to make un-broken in your code.
    - The JRE's LinkedHashMap and LinkedHashSet were used heavily in SquidLib (mainly to ensure RNG seeds keep the same behavior across Java versions and platforms).
    - These have been replaced with OrderedMap and OrderedSet, but the OrderedX classes have a superset of the LinkedHashX classes, so replacing is as simple as a find and
      replace that swaps "LinkedHash" for "Ordered" and including squidlib-util's OrderedMap and/or OrderedSet classes (libGDX has unrelated classes with those names).
    - But why? Well, OrderedMap and OrderedSet allow the ordering to be altered, so they can be shuffled, plus elements can be random-accessed by index in constant time,
      and a useful feature is being able to generate a random ordering with `RNG.randomOrdering()` and use it with `reorder()` on multiple OrderedMap or OrderedSet
      collections of the same size, producing the same "shuffle" for each one.
      - Some other features are being added to the Ordered collections as they are needed, but they already do everything a LinkedHashMap or LinkedHashSet does (except
        access-ordering instead of insertion-ordering in LinkedHashMaps, which is not commonly needed), and pass essentially the same tests the LinkedHashX classes do.
  - Hopefully-significant speedups to region encoding!
    - CoordPacker is nice to use, mostly, but if used too often it can get slow, especially on larger maps. The need for a better way of handling small amounts of
      encoded regions quickly without needing tightly controlled memory usage (the initial reason CoordPacker was made) has led to GreasedRegion.
    - GreasedRegion is an un-compressed bitset-like region encoding that already implements equivalents to much of CoordPacker's API but can be *hundreds of times faster*.
    - It's a little less lean on memory usage, hence the "fat-filled" name, but it's also like greased lightning as to speed.
    - It still hasn't been integrated into the rest of SquidLib, so it isn't clear how much speedup can be gained, but it should really help Placement and SectionDungeonGenerator.
  - More FakeLanguageGen improvements; now you can generate purely random languages.
  - DetailedMimic is like MimicFill, and is a port of [SynTex](https://github.com/mxgmn/SynTex) but can handle color or
    char information, with the former useful for texture generation and the latter for map style imitation.
  - Improvements to the long-neglected Bresenham line drawing class, letting 2D lines use 2D Coords (and pool them accordingly, helping performance).
  - Some extra support for DijkstraMap, including checks for out-of-map points in more places and the ability to configure what makes diagonals pass-able.
  - Several bugfixes, including to one annoying 9-month-old bug with a specific FOV type.
- But, 3.0.0's final release will be major, and so should be expected to *break* API backwards compatibility
  - Any minor releases after 3.0.0 and before 4.0.0 should be expected to *keep* API backwards compatibility, unless a feature is broken or unusable
  - The most significant change in 3.0.0 will be the removal of the Swing-based rendering and full transition to the similar, but much faster and more responsive, libGDX renderer
  - 3.0.0-b1 is the last release to contain Swing. If you're porting code that used an earlier version of SquidLib and need Swing for some reason, you may want to stay with the largely-compatible 2.9.1 instead of the very-different 3.0.0-b1.
    - This should also enable SquidLib to be used for rendering on Android/iOS and not only the desktop platforms Swing is limited to
  - There is now a tool that sets up a project for people who want an easy way to handle the dependencies of SquidLib and/or libGDX
    - We now have SquidSetup to automatically handle the setup of a new project that uses SquidLib 3.0.0-b6, including fetching dependencies automatically and setting up a project that potentially targets both desktop and Android, potentially HTML, and ~~possibly iOS as well~~ iOS is currently in a bit of a chaotic state with RoboVM's changes, and isn't recommended.
    - If you already use Maven, Gradle, SBT, Leiningen, or some other dependency manager, upgrading should be easier to the 3.0.0 series
    - If you don't, you should, and SquidSetup should handle the hard parts for you.
  - If you use SquidLib's latest version as of April 12, the assets have been moved out of the `squidlib` jar, making the download size smaller, but a freshly-updated SquidSetup has all the latest assets
    and will put them in the correct assets folder (as before) without duplicating them.
    - If you don't use SquidSetup, you can download any assets you need from the assets/ folder of this GitHub repo, or get all the assets in a .zip or .tar.gz file from SquidSetup's release page for the [latest SquidSetup](https://github.com/tommyettinger/SquidSetup/releases/tag/v3.0.0-LATEST).
  - [This commit on June 19, 2016](https://github.com/SquidPony/SquidLib/commit/22c770b37b3635c6beacadda6ef71e07c9a55a8e)
    changed usages of LinkedHashMap and LinkedHashSet to OrderedMap and OrderedSet in the squidpony.squidmath package.
    If you use SquidLib versions before that commit, then update to after that commit (after beta 6), you should expect
    some find/replace needed throughout your code. The APIs for LinkedHashSet and OrderedSet are identical except
    for the additions OrderedSet makes, and almost the same is true for LinkedHashMap (no possible access-ordering like
    in LinkedHashMap; only insertion-ordering or user-specified-ordering are possible in OrderedMap, though the last
    isn't possible in LinkedHashMap).

Download
--

Download JARs for older versions from the Releases tab, use Maven Central to download the latest version with your choice of features, or simply use SquidSetup to make a new project configured the way libGDX prefers to work, and copy in any code you might already have.

Ideally, if you're just starting out you should use SquidSetup. Beta 6 has been added to SquidSetup.
This is [the most recent, beta 6, release of the setup tool](https://github.com/SquidPony/SquidLib/releases/tag/v3.0.0-b6);
that version is no longer discouraged like beta 3 was.
This is [the new snapshot setup tool](https://github.com/tommyettinger/SquidSetup/releases/tag/v3.0.0-LATEST), which is
good if you already understand Gradle, but especially if you want to test new features/fixes as they come in. It
currently isn't named in the most accurate way; it actually downloads a fixed version from JitPack.io, but you can
change that version to the latest commit if you want to use more recent code between releases.

Work is under way to support SquidLib as a third-party extension for [gdx-setup](https://github.com/czyzby/gdx-setup),
an alternative to the current official libGDX setup that aims to have more features and update more readily. This may
soon replace SquidSetup. The gdx-setup project also allows demo code to be more easily supplied, and some demos are in
progress for SquidLib.

More information is available on the wiki here on Github, at the page on [Project Setup](https://github.com/SquidPony/SquidLib/wiki/Project-Setup).

GitHub repository: https://github.com/SquidPony/SquidLib

Blog updates: http://squidpony.com/not-games/squidlib/

Created by Eben Howard - howard@squidpony.com  
Currently developed by Tommy Ettinger - tommy.ettinger@gmail.com

Additional work has been greatly appreciated by a team of contributors. smelC and David Becker have each done excellent work in improving and modernizing SquidLib in all sorts of ways.
In particular, David Becker needs thanks for handling some very tough work with Maven configuration and encouraging more unit tests (which have caught quite a few bugs),
and smelC has found all sorts of ways to give back to SquidLib as he has worked on [Dungeon Mercenary](http://www.schplaf.org/hgames/), including doing most of the work for the HTML target,
cleaning up and improving the handling of colors, emphasizing more flexible ways to work with display (such as zooming the screen on mobile), and supporting non-monospaced fonts in the display.
Don't be shy about posting issues! Many of SquidLib's biggest and best changes have been motivated by issues posted by users, including the port to Android!
