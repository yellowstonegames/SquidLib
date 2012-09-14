===SquidLib is a Java library that provides a full featured toolbox for working with turn based games in Swing.===

SquidLib is used for [[Wyrm]] and [[Epigon]].

===Current Features===
*Ease Of Use
**Standard GUI notation of (x,y) locations within the grid.
**Uses Swing
**Any Font can be used
**Specify Grid and Font size multiple ways
***Set number of cells in the grid and Font to be used
****Size of cell is adjusted to optimally fit the Font size given
***Set size of the cell, number of cells in the grid, and Font to be used
****Font is dynamically resized to fit optimally within the cell
**Font size and style can be changed on the fly
**Standard JComponent that meets JavaBean requirements for use with NetBeans built-in GUI Builder
**Multiple grids of different configurations can be used simultaneously in the same display
*Lots of Color
**Foreground and Background can be set individually on a per-cell basis
**SColor class extends Color and can be used as a drop-in replacement for any awt.Color needs
**Over 500 named colors
**Automatic color caching minimizes memory overhead
**Can get a list of colors that are a gradient between two colors
**Can perform LIBTCOD style "dark", "light", and "desaturate" commands on any color
**Can get an arbitrary amount of blend between two colors
*Full Featured API
**Each named color has a sample of its appearance in the Javadoc against multiple backgrounds
***HTML browsing of the API shows these samples
***Pop-up javadoc in NetBeans and Eclipse show these samples
**Demos of all functionality included
**Demos of tying in to mouse and keyboard Swing events included
*Math Toolkit
**Custom extension of Random allows drop-in replacement with added features
**Able to find Bresenham Line for 2D and 3D coordinates.

===Features in Nightly Build===
*Animations
**Slide, bump, wiggle, etc.
*Sound
**mp3, ogg, wav, etc.



GitHub repository: https://github.com/SquidPony/SquidLib

Blog updates: http://squidpony.com/not-games/squidlib/

Developed by [[Deej|Eben Howard]]
