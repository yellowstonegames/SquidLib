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

SquidLib is used for Wyrm, [Epigon](https://github.com/yellowstonegames/SquidLib/Epigon), Attack the Geth, Assault Fish,
[Dungeon Mercenary](http://www.schplaf.org/hgames/), [Cave Cops](https://github.com/tommyettinger/CaveCops), and other
projects.

You can see [a small example](http://yellowstonegames.github.io/SquidLib-Demos/tsar/index.html) online; it is part of the
[SquidLib-Demos](https://github.com/yellowstonegames/SquidLib-Demos) collection. It uses `squidlib-util` to generate
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
[source here](https://github.com/yellowstonegames/SquidLib-Demos/tree/master/DawnlikeDemo), and another with
a simpler graphics style [can be seen here](https://tommyettinger.github.io/SquidLib-Demos/graphical/),
[source here](https://github.com/yellowstonegames/SquidLib-Demos/tree/master/GraphicalDemo).

There's a fairly-active [Discord server for SquidLib](https://discord.gg/aQtmqXt); many questions for SquidLib
also apply to libGDX, so if you're on Discord I'd recommend also joining the [libGDX Discord server](https://discord.gg/7c6Wg8H).
There's also a [SquidLib IRC channel on Freenode](http://webchat.freenode.net/?channels=%23squidlib&uio=MT1mYWxzZSY5PXRydWUmMTE9MjU242);
it is usually nearly empty and silent, so prefer Discord if at all possible.

Documentation:
---
Jars of javadocs are distributed with each release via Maven Central, and with the current latest via JitPack. You can
get the docs and source of the latest version, 3.0.6, in two parts for each; squidlib-util (the core of the library,
and also the largest part) has its
[library jar here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-util/3.0.6/squidlib-util-3.0.6.jar),
[docs here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-util/3.0.6/squidlib-util-3.0.6-javadoc.jar),
and [source here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-util/3.0.6/squidlib-util-3.0.6-sources.jar),
while squidlib (the display part of the library, named the way it is because depending on squidlib should also pull in
squidlib-util to make it a "one-stop shop" dependency) has its
[library jar here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib/3.0.6/squidlib-3.0.6.jar),
[docs here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib/3.0.6/squidlib-3.0.6-javadoc.jar),
and [source here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib/3.0.6/squidlib-3.0.6-sources.jar).
The completely-optional squidlib-extra module (primarily used for serialization; relies on libGDX but doesn't use it for display) has its
[library jar here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-extra/3.0.6/squidlib-extra-3.0.6.jar),
[docs here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-extra/3.0.6/squidlib-extra-3.0.6-javadoc.jar),
and [source here](http://search.maven.org/remotecontent?filepath=com/squidpony/squidlib-extra/3.0.6/squidlib-extra-3.0.6-sources.jar).

You can browse the **JavaDocs** of a recent commit (possibly newer than 3.0.6, but no older) here:
  - [Docs for squidlib-util](http://yellowstonegames.github.io/SquidLib/squidlib-util/apidocs/index.html)
  - [Docs for squidlib](http://yellowstonegames.github.io/SquidLib/squidlib/apidocs/index.html)
  - [Docs for squidlib-extra](http://yellowstonegames.github.io/SquidLib/squidlib-extra/apidocs/index.html)

You can browse the **JavaDocs** of the stable 3.0.0 release here:
  - [Docs for squidlib-util](http://yellowstonegames.github.io/SquidLib/v3-0-0/squidlib-util/apidocs/index.html)
  - [Docs for squidlib](http://yellowstonegames.github.io/SquidLib/v3-0-0/squidlib/apidocs/index.html)
  - [Docs for squidlib-extra](http://yellowstonegames.github.io/SquidLib/v3-0-0/squidlib-extra/apidocs/index.html)

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
recommended release is [v3.0.3](https://github.com/tommyettinger/SquidSetup/releases/tag/v3.0.3), which uses SquidLib
3.0.3 and Gradle 6.7, and as such is compatible with Java versions from 8 to 15. There's also
[v3.0.0-JITPACK](https://github.com/tommyettinger/SquidSetup/releases/tag/v3.0.0-JITPACK), which will use Gradle 6.5.1
for the project and will automatically depend on the latest SquidLib version at the time the project is generated. The
Jitpack release is sometimes out-of-date if assets change or dependencies update.

If you use a dependency manager already and don't need a new project, you can use these dependencies for Maven projects:

Core of SquidLib:
```
<dependency>
    <groupId>com.squidpony</groupId>
    <artifactId>squidlib-util</artifactId>
    <version>3.0.6</version>
</dependency>
```

Optional Text-Based Display (depends on libGDX 1.11.0 and anim8-gdx 0.3.13)
```
<dependency>
    <groupId>com.squidpony</groupId>
    <artifactId>squidlib</artifactId>
    <version>3.0.6</version>
</dependency>
```

Optional Serialization Support (depends on libGDX 1.11.0)
```
<dependency>
    <groupId>com.squidpony</groupId>
    <artifactId>squidlib-extra</artifactId>
    <version>3.0.6</version>
</dependency>
```

Or these dependencies for Gradle:

Core of SquidLib:
```
api 'com.squidpony:squidlib-util:3.0.6'
```

Optional Text-Based Display
```
api 'com.squidpony:squidlib:3.0.6'
```

Optional Serialization Support
```
api 'com.squidpony:squidlib-extra:3.0.6'
```

If you want the latest version of SquidLib, which uses libGDX 1.11.0 and either GWT 2.8.2 or GWT 2.9.0 (if you use GWT),
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

Then the dependencies would be this for Maven (the first is needed, the others are optional); replace `f8f2590cc6`
with any short commit from GitHub:

```
	<dependency>
	    <groupId>com.github.yellowstonegames.SquidLib</groupId>
	    <artifactId>squidlib-util</artifactId>
	    <version>f8f2590cc6</version>
	</dependency>
	<dependency>
	    <groupId>com.github.yellowstonegames.SquidLib</groupId>
	    <artifactId>squidlib</artifactId>
	    <version>f8f2590cc6</version>
	</dependency>
	<dependency>
	    <groupId>com.github.yellowstonegames.SquidLib</groupId>
	    <artifactId>squidlib-extra</artifactId>
	    <version>f8f2590cc6</version>
	</dependency>
```

Or this for Gradle (the first two are needed, the others are optional); replace `f8f2590cc6` with any short commit from GitHub:
```
    api "com.github.yellowstonegames.SquidLib:squidlib-util:f8f2590cc6"
    api "com.github.yellowstonegames.SquidLib:squidlib:f8f2590cc6"
    api "com.github.yellowstonegames.SquidLib:squidlib-extra:f8f2590cc6"
```

If you use GWT, you should probably use SquidSetup to configure the project, since there's a lot of places where
Maven or Gradle with GWT gets tricky. GWT needs the sources for all dependencies, but also needs the sources for
regexodus 0.1.15 (because `squidlib-util` needs it) and possibly anim8-gdx 0.3.13 (because `squidlib` needs it; anim8-gdx
is only needed if you depend on `squidlib`). The sources dependencies look like this for Gradle (I haven't really tried
with Maven):
```
    implementation "com.github.yellowstonegames.SquidLib:squidlib-util:f8f2590cc6:sources"
    // above depends on this:
    implementation "com.github.tommyettinger:regexodus:0.1.15:sources"

    implementation "com.github.yellowstonegames.SquidLib:squidlib:f8f2590cc6:sources"
    // above depends on this:
    implementation "com.github.tommyettinger:anim8-gdx:0.3.13:sources"

    implementation "com.github.yellowstonegames.SquidLib:squidlib-extra:f8f2590cc6:sources"
}
```

If you want GWT 2.9.0 support, just use SquidSetup, otherwise you'll be fine with GWT 2.8.2.

There's also the GWT "inherits" configuration for your application.
These should be present once in GdxDefinition.gwt.xml if you use GWT:

```
    <inherits name="squidpony.squidlib-util" />
```

If you use the display module, you also need

```
    <inherits name="squidpony.squidlib" />
```

And if you use squidlib-extra, you also need

```
    <inherits name="squidpony.squidlib-extra" />
```

Building
---

Many users will only want to pull out some specific part of SquidLib and copy it into their game. This is quite
understandable, given the size of SquidLib, and isn't discouraged at all. To do this, though, people often need to build
and run the tests/demos in SquidLib to get an idea of what a piece of code does. This is also important if you want to
submit a pull request to SquidLib.

SquidLib currently uses Gradle to build. It had used Maven for years, but building on JitPack started to take longer and
longer with Maven, while it is quite quick with Gradle -- builds would often time out at 20 minutes with Maven, while
Gradle hasn't timed out yet with JitPack. The Maven build scripts can still be handy if you want to use Maven elsewhere,
but for here, the Gradle scripts do a better job. If you use IntelliJ IDEA or possibly Android Studio, you can import
the build.gradle file in the root of the parent project directory, and you may need to "Reload all Gradle Projects" by
clicking the circling arrows in the upper corner of the Gradle sidebar (available by clicking "Gradle" on the right side
of the IDEA window, or automatically done in Android Studio). You may need to configure the project to use a recent JVM,
Java 11 or newer. Java 17 is recommended; Java 20 and up aren't supported because SquidLib is still compatible with Java
7, and starting in 20, Java isn't able to compile any code with Java 7 compatibility. No other steps should be needed.
In earlier versions
of SquidLib, some special configuration was needed in IDEA specifically to set the `emu` folder as Excluded, but this
should be done automatically by Gradle now. If you use Eclipse, the project should import OK if you use BuildShip Gradle
(the default in current Eclipse, as far as I know). It may need the `emu` folder in `squidlib-util` to be excluded from
the build, or it may be fine as-is; I don't use Eclipse. If you use NetBeans, good luck.

Once imported, you can run tests via their main methods, which usually are available by the whole class in the
`squidlib` display module, or if they are actual JUnit tests, you can run them individually or as a group.
Many tests in `squidlib-util` look up the `squidpony.examples.TestConfiguration.PRINTING` variable to determine if they
should print or not; this can be turned off to avoid the flood of output from some tests, but it usually defaults to on.

What's Next
---

The road to 3.0.0 was long and winding; libGDX support was added midway between the 2.x and 3.x releases, and by 3.0.0,
libGDX has established itself as the way to go (and definitely not Swing). Keeping backwards compatibility is a primary
focus of the 3.x series now that it's stable. But looking ahead, to the 4.x version, we
can benefit from large breaking changes that aim to simplify usage of the library. That's where
[SquidSquad](https://github.com/yellowstonegames/SquidSquad) comes in. It's SquidLib, but split up substantially more to
address a feature that's been requested for years; you won't need dependencies on unrelated parts of the library, and
hopefully you will be able to depend on just the parts you use. SquidSquad is also simplified in some ways; its data
structures are shared with [jdkgdxds](https://github.com/tommyettinger/jdkgdxds), making it easier to share a dependency
on common data structures, and it doesn't have an RNG class of its own (it relies on the EnhancedRandom classes from
[juniper](https://github.com/tommyettinger/juniper), which is like using StatefulRNG here).

SquidSquad just has its first alpha release, `4.0.0-alpha1`, and it should be usable now, even though docs are lacking.
Most features in SquidLib are present in SquidSquad, with a few exceptions that should hopefully be filled soon:
Techniques haven't been ported, MultiSpill isn't exactly present in the same way (Spill incorporates some of it), and
the String serialization code in ObText, StringConvert, and Converters won't be ported. Some bugfixes have
already been backported from SquidSquad to SquidLib; a long-standing bug in SerpentMapGenerator, for instance, was fixed
in SquidSquad and that fix brought here. SquidSquad's scope is meant to be a little smaller, and to be spread across
more libraries if possible. Since releasing 3.0.0 took about 5 years, I hope SquidSquad becomes stable more quickly!

Credits
---

GitHub repository: [https://github.com/yellowstonegames/SquidLib](https://github.com/yellowstonegames/SquidLib)

Blog updates: [http://squidpony.com/not-games/squidlib/](http://squidpony.com/not-games/squidlib/) (possibly down permanently; server mishaps)

Created by Eben Howard - howard@squidpony.com
Currently developed by Tommy Ettinger - tommy.ettinger@gmail.com

Additional work has been greatly appreciated by a team of contributors. smelC and David Becker have each done excellent work in improving and modernizing SquidLib in all sorts of ways.
In particular, David Becker needs thanks for handling some very tough work with Maven configuration and encouraging more unit tests (which have caught quite a few bugs),
and smelC has found all sorts of ways to give back to SquidLib as he has worked on [Dungeon Mercenary](http://www.schplaf.org/hgames/), including doing most of the work for the HTML target,
cleaning up and improving the handling of colors, emphasizing more flexible ways to work with display (such as zooming the screen on mobile), and supporting non-monospaced fonts in the display.
Don't be shy about posting issues! Many of SquidLib's biggest and best changes have been motivated by issues posted by users, including the port to Android!
