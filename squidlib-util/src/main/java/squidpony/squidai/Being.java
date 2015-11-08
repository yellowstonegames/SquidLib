package squidpony.squidai;

import squidpony.squidmath.Coord;

import java.util.List;

/**
 * A standardized way to describe the quantity and kinds of actions that a specific creature may take on their turn, as
 * well as some other key properties such as location and size. The only criterion that defines a Being is that it must
 * be independently target-able, so if a creature such as a long snake can be attacked at the head, 3 body positions, or
 * the tip of its tail, then it would be 5 Beings (see below for how to handle this). Beings also handle actions in a
 * way that can be understood by the ActionEconomy class, which depends on the behavior of Beings to help make AI
 * decisions.
 * <br>
 * Returning to the five-Being long snake example, you could connect the five segments with the head Being given a
 * reference to the connected body Being, and vice versa, with setConnected(), and segments behind that each given
 * connections to the segment in front of them. The connected Beings can be queried with getConnected(), but many games
 * will not use this feature and can implement setConnected as a no-op and getConnected() as returning null. Methods in
 * SquidLib that use connections in Beings will check for null when using getConnected(), and treat it as an empty List.
 * Created by Tommy Ettinger on 11/7/2015.
 */
public interface Being {
    /**
     * Returns the size of the Being, which can be passed to multi-cell pathfinding methods of DijkstraMap, but is
     * usually 1. A size of 2 means the creature takes up a 2x2 square, 3 means a 3x3 square, etc. If a creature is non-
     * square, use LinkedBeing to connect square parts of it.
     * @return usually 1, can be 2 or more if the creature has a larger square side length
     */
    int getSize();

    /**
     * The minimum-x, minimum-y Coord occupied by this creature's square area, which is usually just the square the
     * creature occupies for size-1 creatures that fit in a 1x1 square.
     * @return a Coord corresponding to the position in this creature's square area with the lowest x and lowest y
     */
    Coord getPosition();

    /**
     * Get the maximum distance this Being can move in one normal move. Some games may allow Beings to give up actions
     * to move more than once; that does not change the result this returns, and a Being that can move 3 squares and act
     * or 6 squares without acting would return 3, because that is its normal move distance.
     * @return the maximum distance this can move in one "normal" movement, including modifiers but not multiple moves.
     */
    int getMoveDistance();

    /**
     * If enemies are aware of this Being and its capabilities, they may try to stay outside the area it threatens.
     * This is typically 1 for Beings with hand-to-hand weapons like swords or claws, but can be higher for Beings with
     * bows, guns, thrown weapons, and so on. Beings with special attacks in the form of Techniques may want to
     * calculate their threat distance by the range of the Technique plus the radius or equivalent of any AOE centered
     * on a Coord at that range (the case for BurstAOE and BlastAOE). If a Being cannot attack, this should return 0.
     * @return 0 if the unit cannot threaten its enemies; otherwise, the maximum distance this unit can do damage at
     */
    int getMaxThreatDistance();

    /**
     * If enemies are aware of this Being and its capabilities, they may try to stay outside the area it threatens,
     * which in some cases may be closer to a Being with long-range weapons. This is typically 0 for Beings with
     * hand-to-hand weapons like swords or claws (making them always able to threaten up to their max threat distance),
     * but can be higher for Beings with bows, guns, missiles, and so on, though it should be less than their max threat
     * distance. Beings with special attacks in the form of Techniques may want to calculate their minimum threat
     * distance by the minimum range of the Technique minus the radius or equivalent of any AOE centered on a Coord at
     * that range (the case for BurstAOE and BlastAOE), minimum 0. If a Being cannot attack, this should return 0.
     * @return 0 if the unit has no restrictions on threat distance, otherwise, the minimum distance this unit can do
     * damage at
     */
    int getMinThreatDistance();

    /**
     * Gets the behavior this Being uses to determine what actions and movement it can perform on a turn.
     * This may return TurnType.ACT_OR_MOVE, which is common in roguelikes and means a Being cannot both move and act in
     * a turn, TurnType.MOVE_THEN_ACT, which is more common in wargames and means a Being must do any movement before it
     * acts, but can do both in one turn, or TurnType.MOVE_AND_ACT_MIXED, which is common in some RPGs and means a Being
     * can move and then act, or act and then move, as it sees fit.
     * @return a TurnType enum that defines how this Being combines action and movement
     */
    TurnType getTurnType();

    /**
     * Gets the List of all Beings whose movement and actions are linked to this one. The exact behavior should be up to
     * the implementor, i.e. some games may want linked Beings to share health, while others may want independently-
     * target-able Beings to have independent health bars.
     * @return a List of Beings that this is connected to; may be null if the game does not use connections
     */
    List<Being> getConnected();

    /**
     * Sets the List of Beings whose movement and actions are connected to this one. The exact behavior should be up to
     * the implementor, i.e. some games may want linked Beings to share health, while others may want independently-
     * target-able Beings to have independent health bars.
     * @param beings the List of beings to set as the connected Beings list; may be null
     */
    void setConnected(List<Being> beings);
}
