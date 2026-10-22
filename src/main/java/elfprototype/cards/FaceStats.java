package elfprototype.cards;

/**
 * Numbers of one card face (light or cursed). Pure data, no game classes, so it can be unit tested.
 * magic is the face's !M! value; with hits() it is the hit count of a multi-hit face (extra hits from powers are
 * added on top at runtime). magic2 is written into the description as {M2}. cost -1 = use the card's cost.
 */
public final class FaceStats {
    public final int damage;
    public final int block;
    public final int magic;
    public final int magic2;
    public final int cost;
    /** Damage hits all enemies (multiDamage). */
    public final boolean aoe;
    /** magic is the number of hits. */
    public final boolean hits;
    public final boolean exhaust;
    final int damageUp;
    final int blockUp;
    final int magicUp;
    final int magic2Up;
    final int costUp;

    private FaceStats(int damage, int block, int magic, int magic2, int cost, boolean aoe, boolean hits,
                      boolean exhaust, int damageUp, int blockUp, int magicUp, int magic2Up, int costUp) {
        this.damage = damage;
        this.block = block;
        this.magic = magic;
        this.magic2 = magic2;
        this.cost = cost;
        this.aoe = aoe;
        this.hits = hits;
        this.exhaust = exhaust;
        this.damageUp = damageUp;
        this.blockUp = blockUp;
        this.magicUp = magicUp;
        this.magic2Up = magic2Up;
        this.costUp = costUp;
    }

    public static FaceStats of() {
        return new FaceStats(-1, -1, -1, 0, -1, false, false, false, 0, 0, 0, 0, 0);
    }

    public FaceStats damage(int base, int up) {
        return new FaceStats(base, block, magic, magic2, cost, aoe, hits, exhaust, up, blockUp, magicUp, magic2Up, costUp);
    }

    public FaceStats block(int base, int up) {
        return new FaceStats(damage, base, magic, magic2, cost, aoe, hits, exhaust, damageUp, up, magicUp, magic2Up, costUp);
    }

    public FaceStats magic(int base, int up) {
        return new FaceStats(damage, block, base, magic2, cost, aoe, hits, exhaust, damageUp, blockUp, up, magic2Up, costUp);
    }

    public FaceStats magic2(int base, int up) {
        return new FaceStats(damage, block, magic, base, cost, aoe, hits, exhaust, damageUp, blockUp, magicUp, up, costUp);
    }

    /** Multi-hit face: magic = hit count. */
    public FaceStats hits(int base, int up) {
        return new FaceStats(damage, block, base, magic2, cost, aoe, true, exhaust, damageUp, blockUp, up, magic2Up, costUp);
    }

    /** Face-specific cost; costUp is the change on upgrade (e.g. -1). */
    public FaceStats cost(int base, int up) {
        return new FaceStats(damage, block, magic, magic2, base, aoe, hits, exhaust, damageUp, blockUp, magicUp, magic2Up, up);
    }

    public FaceStats aoe() {
        return new FaceStats(damage, block, magic, magic2, cost, true, hits, exhaust, damageUp, blockUp, magicUp, magic2Up, costUp);
    }

    public FaceStats exhaust() {
        return new FaceStats(damage, block, magic, magic2, cost, aoe, hits, true, damageUp, blockUp, magicUp, magic2Up, costUp);
    }

    /** Values after the card's single upgrade. Unused fields (-1) stay unused. */
    public FaceStats upgraded() {
        return new FaceStats(bump(damage, damageUp), bump(block, blockUp), bump(magic, magicUp), magic2 + magic2Up,
                cost < 0 ? cost : Math.max(0, cost + costUp), aoe, hits, exhaust, 0, 0, 0, 0, 0);
    }

    public boolean damageUpgrades() {
        return damage >= 0 && damageUp != 0;
    }

    public boolean blockUpgrades() {
        return block >= 0 && blockUp != 0;
    }

    public boolean magicUpgrades() {
        return magic >= 0 && magicUp != 0;
    }

    /**
     * Rule: a cursed face costs at least 1. A cursed face returns to hand after it is played, so with 0-cost cursed
     * faces two cards that erode each other loop forever for free (two Skims, Curse Whisper + Mirror, ...).
     * With the floor every loop spends energy, and an infinite needs energy-back pieces (Moon Awakening, ...).
     */
    public static final int MIN_CURSED_COST = 1;

    /** Cost actually charged for a face; X-costs (negative) are left alone. */
    public static int faceCost(int cost, boolean cursed) {
        return cursed && cost >= 0 ? Math.max(MIN_CURSED_COST, cost) : cost;
    }

    private static int bump(int base, int up) {
        return base < 0 ? base : base + up;
    }
}
