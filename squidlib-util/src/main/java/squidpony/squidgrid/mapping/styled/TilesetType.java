package squidpony.squidgrid.mapping.styled;

/**
 * An enumeration of all the kinds of dungeon that DungeonGen
 * knows how to draw already. Each value has a Javadoc description.
 * Created by Tommy Ettinger on 3/10/2015.
 */
public enum TilesetType {
    /**
     * A generally useful kind of dungeon for ruins or underground manufactured areas.
     */
    DEFAULT_DUNGEON,
    /**
     * A good general kind of cave, with long distances between merging paths.
     */
    CAVES_LIMIT_CONNECTIVITY,
    /**
     * Only usable if using Chebyshev distances; many connections are diagonal-only.
     */
    CAVES_TINY_CORRIDORS,
    /**
     * Most parts of the cave are large and open, but tiny corridors link them, providing hiding places.
     */
    CORNER_CAVES,
    /**
     * Very basic demo dungeon.
     */
    HORIZONTAL_CORRIDORS_A,
    /**
     * Slightly less basic demo dungeon.
     */
    HORIZONTAL_CORRIDORS_B,
    /**
     * A bit more complexity in this demo dungeon.
     */
    HORIZONTAL_CORRIDORS_C,
    /**
     * A reference implementation of where you can place walls; mostly floor.
     */
    LIMIT_CONNECTIVITY_FAT,
    /**
     * A reference implementation of where you can place walls; mostly wall.
     */
    LIMITED_CONNECTIVITY,
    /**
     * A generally good maze; MAZE_A and MAZE_B should both be interchangeable, but not on the same map.
     */
    MAZE_A,
    /**
     * A generally good maze; MAZE_A and MAZE_B should both be interchangeable, but not on the same map.
     */
    MAZE_B,
    /**
     * A map that's less dungeon-like than the others, with lots of open space.
     */
    OPEN_AREAS,
    /**
     * An excellent natural cave style that looks worn down haphazardly, as by burrowing creatures or deep rivers.
     */
    REFERENCE_CAVES,
    /**
     * Mostly open, kinda weird.
     */
    ROOMS_AND_CORRIDORS_A,
    /**
     * Mostly open, but with long corridors that should be a good fit for ranged combat.
     */
    ROOMS_AND_CORRIDORS_B,
    /**
     * A nice old-school roguelike map, with thin corridors and rectangular rooms.
     */
    ROOMS_LIMIT_CONNECTIVITY,
    /**
     * A thing of beauty. Good for maps that need to seem otherworldly or unusual, like heavenly planes.
     */
    ROUND_ROOMS_DIAGONAL_CORRIDORS,
    /**
     * A more open cave, but portions of this may seem artificial. Consider alternating with REFERENCE_CAVES .
     */
    SIMPLE_CAVES,
    /**
     * Kinda... not the best map. Very predictable.
     */
    SQUARE_ROOMS_WITH_RANDOM_RECTS
}

