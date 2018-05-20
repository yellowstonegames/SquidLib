# Key Remapping Preference files

This directory includes a key remapping tool that can be given to users so they can adjust what key event is produced
when they press a key. It probably is limited to keyboards that can enter the ASCII character set, but doesn't constrain
the layout. A user who prefers DVORAK or AZERTY over QWERTY can still enter things like the `hjkl` vi-keys, or `wasd`
traditional QWERTY direction keys, with keys that fit a similar shape on their different layout. The tool is
`KeyRemapTool.jar` in this directory; to use it, mouse over the key you want to produce, and press the key or key
combination you want to use instead. The custom remap will show up to the right of the key it produces, and you can
click the Alt, Ctrl, and Shift buttons at the very bottom to cause the keys you produce to be combined with Alt, Ctrl,
and/or Shift. If Shift is pressed, it changes some key results to different chars, like `a` to `A` and `7` to `&`; this
matches the behavior of SquidInput. You can press a key such as `j` when moused-over that same key (`j`) to delete any
custom remapping (effectively mapping it to what it normally is).

When you make any changes in the KeyRemapTool, a file is written in the same folder called keymap.preferences.
SquidInput will automatically check for and read a file with this name if it is present in the same folder as a
SquidLib-using JAR (if you want to try it with tests, put it in the root folder of all the SquidLib modules, in the
parent of this directory). Any remapping specified in KeyRemapTool will then be used by every SquidInput instance in
that JAR, unless loaded remapping was specifically removed (via `SquidInput.clearMapping()`). If you want to copy an
existing set of preferences, you only need to copy that file. If you find something doesn't work and want to go back to
the defaults, just move or delete `keymap.preferences`. If a `keymap.preferences` file is already in the same folder as
`KeyRemapTool.jar`, it will act as saved preferences that will automatically be loaded when you start the JAR. 

## Existing keymaps

One `keymap.preferences` file is already here, in the subdirectory `vi-keys`. It is meant for computers without a numpad
that need to play a game that was written with a numpad in mind. It maps hjkl to the numpad directions left, down, up,
and right (`hjkl = numpad 4286`), as well as yubn to numpad directions up-left, up-right, down-left, and down-right,
(`yubn = numpad 7913`), and `.` to numpad "center" (`. = numpad 5`). You can try this on most "graphical" tests in
SquidLib by copying that file into the topmost directory (it contains the folders `squidlib`, `squidlib-util`, and
`squidlib-extra`), or with a JAR of your own by copying `keymap.preferences` next to your JAR.
