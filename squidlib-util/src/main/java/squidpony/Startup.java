package squidpony;

import squidpony.squidmath.StatefulRNG;

/**
 * All static variables that can have their value change are isolated here. IF YOU TARGET ANDROID, THIS MATTERS TO YOU!
 * This class will work behind-the-scenes on desktop and will only be used internally, but on Android (and probably iOS
 * via RoboVM), you need to take an additional step to ensure the variables in this class do not linger into the next
 * application startup. This is simple: when the application resumes from a paused or inactive state, call
 * {@code Startup.init()}, or if you want to save and load static state (such as when a game must use ) {}
 * Created by Tommy Ettinger on 9/15/2015.
 */
public class Startup {
    /**
     *
     */
    public static StatefulRNG terrainRNG;
}
