SquidLib is a Java library that provides a diverse toolbox for working with procedural generation in games using libGDX.
--  

What Can It Do?
---
SquidLib is a very full-featured library, and is partly targeted at making traditional roguelikes and similar genres.
It can handle basic to moderately-complex dungeon generation, including rigid mazes (`GrowingTreeMazeGenerator`),
flowing/natural cave systems (`FlowingCaveGenerator`), dense systems of inter-connected rooms (`DungeonGenerator`'s
default setting), rooms that connect via thin corridor in a cyclical pattern (`SerpentMapGenerator`), mixes of cave,
room, and corridor (also `SerpentMapGenerator`, with more options using `SectionDungeonGenerator`), and more. It
represents dungeons universally as a `char[][]`, consistently using x-then-y indexing throughout the library, and
various other parts of SquidLib can understand `char[][]` input. Since we target the JVM, we have Unicode for our
`char`s, and dungeons often are drawn with box-drawing characters for walls. Within a dungeon or cave, the lighting
system can be complex, so there's code to find the Field of Vision for a character that can also be used to find the
level of brightness for light cast into many tiles. You can mix these fields of vision (`FOV`) based on what lit cells
are in Line of Sight (`LOS`); SquidLib provides LOS algorithms to get the actual line and can also find the total set of
cells that are at all within line of sight, even if out of lighting range. The dungeons can be generated from repeatable
random number generators (`RNG`), so if the same seed is provided to an RNG, you get the same procedurally generated
content, even on different runs, machines, OSes or platforms.

There's a lot of procedural content generation here. There's simple name-generation roughly matching the style of a list
of names in `WeightedLetterNamegen`, and then there's the sizable and just-about unique `FakeLanguageGen`, which
imitates the linguistic properties of real and/or fictional languages but can also generate those languages procedurally
(with no list of starting material) or hybridize two or more languages it already knows. For English text, there's
`Thesaurus`, which can choose various synonyms for specified categories of words, `Messaging`/`ProceduralMessaging`,
which is useful for transforming coded statements into short, game-appropriate sentences like "You smash the goblin for
7 damage!" or "The goblin slices you for 3 damage!", and general utilities in `StringKit`, like word-wrapping and
handling case. SquidLib can generate wild area maps using `WildMap`, all the way to continent or world maps with
`WorldMapGenerator`. Handling complex map situations is made more feasible using the incredibly-powerful but oddly-named
`GreasedRegion` class; it allows manipulating regions of a map not just as unrelated sets of points, but as areas that
can be expanded, retracted, limited to only their outer surface, randomly filtered, flood-filled within the bounds of
another region, and so on. GreasedRegion has a `spill()` method, but there's also a `Spill` (and `MultiSpill`) class,
all of which handle randomized flood-fill to loosely simulate a fluid expanding through an area. There's code for
continuous noise, including Classic Perlin Noise (`ClassicNoise` and a mode in `FastNoise`), Simplex Noise
(`SeededNoise` and a mode in `FastNoise`), and more unusual kinds like Foam Noise (`FoamNoise`, `PhantomNoise`, and
again, a mode in `FastNoise`); this is often used for making water animate in games, or to make grass sway slightly.

Once you have inhabitants in your map, whatever it is, you probably want to have them act at least a little
intelligently, and so there's various pathfinding classes here. `DijkstraMap` produces one of these
[beloved roguelike concepts](http://roguebasin.roguelikedevelopment.org/index.php?title=Dijkstra_Maps_Visualized) and
can support not just simply getting next to the player from far away, but also pathfinding toward a target while keeping
a minimum distance, fleeing away from a target, pathfinding to the closest of several potential targets, reusing certain
parts of pathfinding to make later paths nearly instantaneous to find, etc. The `squidpony.squidai.graph` package draws
on the very fast code from [simple-graphs](https://github.com/earlygrey/simple-graphs) to handle A-Star pathfinding to
one target, and various other common graph algorithms like topological sorting.

Displaying the map and all the contents of the world you carefully craft really is something that's different in every
game. You can use `squidlib-util` on its own and render your game with [libGDX](https://github.com/libgdx/libgdx),
[Arc](https://github.com/Anuken/Arc), some other choice for graphics, or you can use the text-based display code in the
`squidlib` module. This could be just for prototyping, or for your final game, it is up to you. This text-based display
is usually used via `SparseLayers`, which can render a grid of text layered over other text and that layered over a
background, any of which can be partially transparent. It can also render chars out of grid alignment for movement
between grid cells, or bump/wiggle effects. Fonts are usually in the `TextCellFactory` class, which is misleadingly
named because it doesn't actually handle cells, nor is it a factory... But it does handle the nice and smooth
"stretchable" SDF fonts, and the newer "crisp" MSDF fonts, which are provided in various pre-made forms in
`DefaultResources`. There's a ton of pre-defined colors in `SColor` along with ways to manipulate them. You can make use
of SColor with `Radiance`, which handles those FOV-based lights mentioned above, and can also make the lights flicker
and/or strobe. You can also filter colors before they are drawn using `Filters` or `FloatFilters`, which store various
handy predefined effects that you can add to.

Where Is It Used?
---

SquidLib is used for Wyrm, [Epigon](https://github.com/SquidPony/Epigon), Attack the Geth, Assault Fish,
[Dungeon Mercenary](http://www.schplaf.org/hgames/), [Cave Cops](https://github.com/tommyettinger/CaveCops), and other
projects.

You can see [a small example](http://tommyettinger.github.io/SquidLib-Demos/tsar/index.html) online; it is part of the
[SquidLib-Demos](https://github.com/tommyettinger/SquidLib-Demos) collection. It uses `squidlib-util` to generate
dungeon maps, handle pathfinding for enemies (who chase you after they see you) and the player (if you click, it
uses pathfinding to move the player to the clicked spot), calculate field of vision, and produce gibberish text that
looks like Russian (but isn't). It uses `squidlib` (the display module for text-based games) to show the generated maps
in a traditional roguelike style with `@` for the player and `Я` for enemy guards, blend colors for torchlight effects,
and smoothly slide the player and enemies around when they move. A much more polished use of SquidLib is
[Dungeon Mercenary](http://www.schplaf.org/hgames/), by smelC; it is a full-fledged game, not just a demo.

You don't need to use a text-based display to use many features of this library; there's a demo of this (not yet a game)
in [ColorGuard](https://github.com/tommyettinger/ColorGuard), which uses `squidlib-util` to handle random numbers, world
map generation, and text generation in fictional languages. SquidLib-Demos has some demos that only use `squidlib-util`
and libGDX; [one can be seen here](https://tommyettinger.github.io/SquidLib-Demos/dawnlike/index.html),
[source here](https://github.com/tommyettinger/SquidLib-Demos/tree/master/DawnlikeDemo), and another with
a simpler graphics style [can be seen here](https://tommyettinger.github.io/SquidLib-Demos/graphical/),
[source here](https://github.com/tommyettinger/SquidLib-Demos/tree/master/GraphicalDemo).

There's a fairly-active [Discord server for SquidLib](https://discord.gg/aQtmqXt); many questions for SquidLib
also apply to libGDX, so if you're on Discord I'd recommend also joining the [libGDX Discord server](https://discord.gg/7c6Wg8H).
There's also a [SquidLib IRC channel on Freenode](http://webchat.freenode.net/?channels=%23squidlib&uio=MT1mYWxzZSY5PXRydWUmMTE9MjU242);
it is usually nearly empty and silent, so prefer Discord if at all possible.

Documentation:
---
Jars of javadocs are distributed with each release via Maven Central, and with the current latest via JitPack. You can
get the docs and source of the latest version, 3.0.0, in two parts for each; squidlib-util (the core of the library,
and also the largest part) has its
[library jar here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-util/3.0.0/squidlib-util-3.0.0.jar),
[docs here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-util/3.0.0/squidlib-util-3.0.0-javadoc.jar),
and [source here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-util/3.0.0/squidlib-util-3.0.0-sources.jar),
while squidlib (the display part of the library, named the way it is because depending on squidlib should also pull in
squidlib-util to make it a "one-stop shop" dependency) has its
[library jar here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib/3.0.0/squidlib-3.0.0.jar),
[docs here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib/3.0.0/squidlib-3.0.0-javadoc.jar),
and [source here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib/3.0.0/squidlib-3.0.0-sources.jar).
The completely-optional squidlib-extra module (primarily used for serialization; relies on libGDX but doesn't use it for display) has its
[library jar here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-extra/3.0.0/squidlib-extra-3.0.0.jar),
[docs here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-extra/3.0.0/squidlib-extra-3.0.0-javadoc.jar),
and [source here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-extra/3.0.0/squidlib-extra-3.0.0-sources.jar).

You can browse the **JavaDocs** of a recent commit (possibly newer than 3.0.0, but no older) here:
  - [Docs for squidlib-util](http://squidpony.github.io/SquidLib/squidlib-util/apidocs/index.html)
  - [Docs for squidlib](http://squidpony.github.io/SquidLib/squidlib/apidocs/index.html)
  - [Docs for squidlib-extra](http://squidpony.github.io/SquidLib/squidlib-extra/apidocs/index.html)
The docs here are updated whenever the project is rebuilt fully, which only coincides with releases occasionally.

Download
--

Download JARs for older versions from the Releases tab, use Maven Central to download the latest stable release (or
JitPack to download any commit, typically the most recent one) with your choice of features (display or none, with or
without squidlib-extra), or simply use [SquidSetup](https://github.com/tommyettinger/SquidSetup/) to make a new project
configured the way libGDX prefers to work (including SquidLib's assets), and copy in any code you might already have.

Ideally, if you're starting out you should use [SquidSetup](https://github.com/tommyettinger/SquidSetup/). This is based
on [czyzby's gdx-setup tool](https://github.com/czyzby/gdx-setup), an alternative to the current official libGDX setup
that aims to have more features and update more readily. A demo is present for SquidLib, selected by default when making
a project with SquidSetup (typically if the demo becomes out-of-date, SquidSetup is updated to fix the demo). The
recommended release is [v3.0.0-JITPACK](https://github.com/tommyettinger/SquidSetup/releases/tag/v3.0.0-JITPACK), which
will use Gradle 6.5.1 for the project (and that avoids numerous bugs in older Gradle versions) and will automatically
depend on the latest SquidLib version at the time the project is generated.

If you use a dependency manager already and don't need a new project, you can use these dependencies for Maven projects:

Core of SquidLib:
```
<dependency>
    <groupId>com.squidpony</groupId>
    <artifactId>squidlib-util</artifactId>
    <version>3.0.0</version>
</dependency>
```

Optional Text-Based Display (depends on libGDX 1.9.11)
```
<dependency>
    <groupId>com.squidpony</groupId>
    <artifactId>squidlib</artifactId>
    <version>3.0.0</version>
</dependency>
```

Optional Serialization Support (depends on libGDX 1.9.11)
```
<dependency>
    <groupId>com.squidpony</groupId>
    <artifactId>squidlib-extra</artifactId>
    <version>3.0.0</version>
</dependency>
```

Or these dependencies for Gradle:

Core of SquidLib:
```
api 'com.squidpony:squidlib-util:3.0.0'
```

Optional Text-Based Display
```
api 'com.squidpony:squidlib:3.0.0'
```

Optional Serialization Support
```
api 'com.squidpony:squidlib-extra:3.0.0'
```

If you want the latest version of SquidLib, which uses libGDX 1.9.11 and either GWT 2.8.2 or GWT 2.9.0 (if you use GWT),
you can use JitPack to build the latest commit on-demand. It needs an additional repository, which is this for Maven:

```
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

Or this for Gradle, which if you used SquidSetup, would be in the repositories block inside subprojects:
```
maven { url "https://jitpack.io" }
```

Then the dependencies would be this for Maven (the first is needed, the others are optional); replace `b7f13176ef`
with any short commit from GitHub:

```
	<dependency>
	    <groupId>com.github.SquidPony.SquidLib</groupId>
	    <artifactId>squidlib-util</artifactId>
	    <version>b7f13176ef</version>
	</dependency>
	<dependency>
	    <groupId>com.github.SquidPony.SquidLib</groupId>
	    <artifactId>squidlib</artifactId>
	    <version>b7f13176ef</version>
	</dependency>
	<dependency>
	    <groupId>com.github.SquidPony.SquidLib</groupId>
	    <artifactId>squidlib-extra</artifactId>
	    <version>b7f13176ef</version>
	</dependency>
```

Or this for Gradle (the first two are needed, the others are optional); replace `b7f13176ef` with any short commit from GitHub:
```
    api "com.github.SquidPony.SquidLib:squidlib-util:b7f13176ef"
    api "com.github.SquidPony.SquidLib:squidlib:b7f13176ef"
    api "com.github.SquidPony.SquidLib:squidlib-extra:b7f13176ef"
```

If you use GWT, you should probably use SquidSetup to configure the project, since there's a lot of places where
Maven or Gradle with GWT gets tricky. GWT needs the sources for all dependencies, but also needs the sources for
regexodus 0.1.10 (because `squidlib-util` needs it) and possibly anim8-gdx 0.1.6 (because `squidlib` needs it; anim8-gdx
is only needed if you depend on `squidlib`). The sources dependencies look like this for Gradle (I haven't really tried
with Maven):
```
    implementation "com.github.SquidPony.SquidLib:squidlib-util:b7f13176ef:sources"
    // above depends on this:
    implementation "com.github.tommyettinger:regexodus:0.1.10:sources"

    implementation "com.github.SquidPony.SquidLib:squidlib:b7f13176ef:sources"
    // above depends on this:
    implementation "com.github.tommyettinger:anim8-gdx:0.1.6:sources"

    implementation "com.github.SquidPony.SquidLib:squidlib-extra:b7f13176ef:sources"
}
```

If you want GWT 2.9.0 support, just use SquidSetup, otherwise you'll be fine with GWT 2.8.2.

There's also the GWT "inherits" configuration for your application.
These should be present once in GdxDefinition.gwt.xml if you use GWT:

```
    <inherits name="squidlib-util" />
```

If you use the display module, you also need

```
    <inherits name="squidlib" />
```

And if you use squidlib-extra, you also need

```
    <inherits name="squidlib-extra" />
```

Credits
---

GitHub repository: [https://github.com/SquidPony/SquidLib](https://github.com/SquidPony/SquidLib)

Blog updates: [http://squidpony.com/not-games/squidlib/](http://squidpony.com/not-games/squidlib/) (possibly down permanently; server mishaps)

Created by Eben Howard - howard@squidpony.com  
Currently developed by Tommy Ettinger - tommy.ettinger@gmail.com

Additional work has been greatly appreciated by a team of contributors. smelC and David Becker have each done excellent work in improving and modernizing SquidLib in all sorts of ways.
In particular, David Becker needs thanks for handling some very tough work with Maven configuration and encouraging more unit tests (which have caught quite a few bugs),
and smelC has found all sorts of ways to give back to SquidLib as he has worked on [Dungeon Mercenary](http://www.schplaf.org/hgames/), including doing most of the work for the HTML target,
cleaning up and improving the handling of colors, emphasizing more flexible ways to work with display (such as zooming the screen on mobile), and supporting non-monospaced fonts in the display.
Don't be shy about posting issues! Many of SquidLib's biggest and best changes have been motivated by issues posted by users, including the port to Android!
