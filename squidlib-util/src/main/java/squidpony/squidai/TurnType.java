package squidpony.squidai;

/**
 * Enum to determine how a Being can act on its turn.
 * Created by Tommy Ettinger on 11/7/2015.
 */

public enum TurnType
{
    /**
     * This Being can either move up to its normal move distance or act, but not both.
     */
    ACT_OR_MOVE,

    /**
     * This Being can move up to its normal move distance and then act if it can, in that order.
     */
    MOVE_THEN_ACT,

    /**
     * This Being can move up to its normal move distance and act in the same turn, and can do either first.
     */
    MOVE_AND_ACT_MIXED
}
