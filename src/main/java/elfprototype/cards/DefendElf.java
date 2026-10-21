package elfprototype.cards;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Light: gain 5 (8) block. Cursed: gain 3 (5) block, deal 3 (4) to ALL enemies. */
public class DefendElf extends DualFaceCard {
    public static final String SIMPLE_ID = "Defend_Elf";

    public DefendElf() {
        super(SIMPLE_ID, 1, CardType.SKILL, CardRarity.BASIC,
                CardTarget.SELF, FaceStats.of().block(5, 3),
                CardTarget.ALL_ENEMY, FaceStats.of().block(3, 2).damage(3, 1).aoe());
        tags.add(CardTags.STARTER_DEFEND);
    }

    @Override
    protected void useLight(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, block));
    }

    @Override
    protected void useCursed(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, block));
        addToBot(new DamageAllEnemiesAction(p, multiDamage, damageTypeForTurn,
                AbstractGameAction.AttackEffect.SLASH_HEAVY));
    }
}
