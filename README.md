SquidLib is a Java library that provides a full featured toolbox for working with turn based games in Swing and libGDX.
==  

SquidLib is used for Wyrm, Epigon, Attack the Geth, and other projects.
--  

Current Features:
--
###Ease Of Use
-	Standard GUI notation of (x,y) locations within the grid.
-	Uses Swing or libGDX
-	Any Font can be used
-	Images may be used alongside characters in same panel
--		Characters can be used as a drop-in fallback mechanism!
-	Specify Grid and Font size multiple ways
--		Set number of cells in the grid and Font to be used
---			Size of cell is adjusted to optimally fit the Font size given
--		Set size of the cell, number of cells in the grid, and Font to be used
---			Font is dynamically resized to fit optimally within the cell
-	Font size and style can be changed on the fly
-	Standard JComponent that meets JavaBean requirements for use with NetBeans built-in GUI Builder
-	Multiple grids of different configurations can be used simultaneously in the same display
-	Basic Swing animation support

###Lots of Color
-	Foreground and Background can be set individually on a per-cell basis
-	SColor class extends Color and can be used as a drop-in replacement for any awt.Color needs
-	SColor chooser included to visually select named and adjusted colors
-	Over 500 named colors
-	Automatic color caching minimizes memory overhead
-	Can get a list of colors that are a gradient between two colors
-	Can perform LIBTCOD style "dark", "light", and "desaturate" commands on any color
-	Can get an arbitrary amount of blend between two colors

###Roguelike Specific Toolkit
-	Robust Field of View and Line of Sight system

###Fully Documented API
-	Each named color has a sample of its appearance in the Javadoc against multiple backgrounds
-	HTML browsing of the API shows these samples
-	Pop-up javadoc in NetBeans and Eclipse show these samples
-	Demos of all functionality included

###Math Toolkit
-	Custom extension of Random allows drop-in replacement with added features
-	Able to find Bresenham Line for 2D and 3D coordinates.
  
  
GitHub repository: https://github.com/SquidPony/SquidLib

Blog updates: http://squidpony.com/not-games/squidlib/

Developed by Eben Howard - howard@squidpony.com