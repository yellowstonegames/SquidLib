# Extras for SquidLib

If you set up your project manually (without SquidSetup), it may be missing almost all of its default resources.
This can also happen if you add `squidlib` (the text-based display part of the library) as a dependency to an
existing project, without getting the default resources put into place by SquidSetup (this is especially likely
if you use a non-Java JVM language with its own build tool, like Scala's `sbt` or Clojure's `lein`).

This folder has the default resources that SquidLib previously shipped with, and now uses as test resources. This
means the tests and demos in SquidLib still run, but users don't need 22 MB of resources if they intend to use
their own. You only need the files from this folder your game uses; if a file is missing that you use, it is a
run-time error, usually very early on (when the first screen using a font is displayed, for instance).

If you use a bitmap font, you need both the .png and .fnt file with the same or similar base filename, like
`Mandrill-6x16.fnt` and `Mandrill-6x16.png`. The stretchable bitmap fonts in DefaultResources don't load an
external shader file (it's stored as a String in lib code), so they only need the .fnt and .png for the appropriate
font. Images are probably only going to be used as icons, but if you want to use the tentacle images, feel free to
use the .png files for any purpose you have for them. SquidSetup adds extra platform-specific icons for loading
screens on Android and iOS; these can, of course, be replaced if you use SquidSetup, but they aren't distributed
here since they were never in SquidLib-proper.

Whatever you pick, you should put the files in your project's assets folder, which is usually in the Android
project, but can also be in the core project if there isn't an Android project. You shouldn't have a resources
folder inside the assets folder; the files themselves should be directly under `assets/` .

You can delete or move any files you know you don't use so they don't increase jar or APK size, though it is
recommended that you check and make sure your program hasn't changed what font it uses (or had some other change)
each time you remove files.
