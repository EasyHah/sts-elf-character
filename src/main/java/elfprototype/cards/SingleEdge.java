package elfprototype.cards;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import elfprototype.mechanics.Lumen;

/** Light: deal 7 (10); if a card was flipped this turn, draw 1. Cursed: deal 3 (4) three times. */
public class SingleEdge extends DualFaceCard {
    public static final String SIMPLE_ID = "SingleEdge";

    public SingleEdge() {
        super(SIMPLE_ID, 1, CardType.ATTACK, CardRarity.BASIC,
                CardTarget.ENEMY, FaceStats.of().damage(7, 3),
                CardTarget.ENEMY, FaceStats.of().damage(3, 1).hits(3, 0));
        setSeal(Seal.of(Seal.Kind.NEXT_ATTACK, 3, 1));
    }

    @Override
    protected void useLight(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn),
                AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
        if (Lumen.chime() > 0) addToBot(new DrawCardAction(p, 1));
    }

    @Override
    protected void useCursed(AbstractPlayer p, AbstractMonster m) {
        for (int i = 0; i < magicNumber; i++) {
            addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn),
                    AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
        }
    }

    @Override
    public void triggerOnGlowCheck() {
        glowColor = !isCursed() && Lumen.chime() > 0 ? GOLD_BORDER_GLOW_COLOR.cpy() : BLUE_BORDER_GLOW_COLOR.cpy();
    }
}
