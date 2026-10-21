package elfprototype.mechanics;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.combat.FlashAtkImgEffect;

/**
 * One hit, then a callback that knows whether the hit landed (unblocked damage) and whether it killed.
 * Kill = like the vanilla Feed card: dying, not half-dead, not a minion.
 */
public class DamageThenAction extends AbstractGameAction {
    public interface After {
        void run(boolean killed, int unblocked);
    }

    private final DamageInfo info;
    private final After after;

    public DamageThenAction(AbstractCreature target, DamageInfo info, AttackEffect fx, After after) {
        this.target = target;
        this.info = info;
        this.attackEffect = fx;
        this.after = after;
        this.actionType = ActionType.DAMAGE;
        this.duration = 0.1f;
    }

    @Override
    public void update() {
        if (duration == 0.1f && target != null && !target.isDeadOrEscaped()) {
            AbstractDungeon.effectList.add(new FlashAtkImgEffect(target.hb.cX, target.hb.cY, attackEffect));
            target.damage(info);
            boolean killed = (target.isDying || target.currentHealth <= 0) && !target.halfDead
                    && !target.hasPower("Minion");
            after.run(killed, target.lastDamageTaken);
            if (AbstractDungeon.getCurrRoom().monsters.areMonstersBasicallyDead()) {
                AbstractDungeon.actionManager.clearPostCombatActions();
            }
        }
        tickDuration();
    }
}
