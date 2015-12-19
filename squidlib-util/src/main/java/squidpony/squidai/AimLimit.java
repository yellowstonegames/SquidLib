package squidpony.squidai;

/**
 * Enum used for common targeting limitations (or lack thereof, in the case of AimLimit.FREE ). AimLimit.ORTHOGONAL will
 * limit single targets or the centers/aimed-at-cells of AOE effects to cells directly, north, south, east or west of
 * the user. AimLimit.DIAGONAL does the same but for northeast, southeast, southwest, or northwest. AIMLIMIT.EIGHT_WAY
 * limits the same things, but is less restrictive, allowing all cells AimLimits.ORTHOGONAL does as well as all cells
 * AimLimits.DIAGONAL allows. AimLimit.FREE allows all cells within any range limit an ability may have.
 * Created by Tommy Ettinger on 12/17/2015.
 */
public enum AimLimit {
    FREE,
    ORTHOGONAL,
    DIAGONAL,
    EIGHT_WAY
}
