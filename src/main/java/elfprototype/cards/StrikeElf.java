package elfprototype.cards;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

/** Light: deal 6 (9). Cursed: deal 4 (5) twice. */
public class StrikeElf extends DualFaceCard {
    public static final String SIMPLE_ID = "Strike_Elf";

    public StrikeElf() {
        super(SIMPLE_ID, 1, CardType.ATTACK, CardRarity.BASIC,
                CardTarget.ENEMY, FaceStats.of().damage(6, 3),
                CardTarget.ENEMY, FaceStats.of().damage(4, 1).hits(2, 0));
        setSeal(Seal.of(Seal.Kind.DAMAGE_RANDOM, 3, 1));
        tags.add(CardTags.STRIKE);
        tags.add(CardTags.STARTER_STRIKE);
    }

    @Override
    protected void useLight(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn),
                AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
    }

    @Override
    protected void useCursed(AbstractPlayer p, AbstractMonster m) {
        for (int i = 0; i < magicNumber; i++) {
            addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn),
                    i % 2 == 0 ? AbstractGameAction.AttackEffect.SLASH_HORIZONTAL
                            : AbstractGameAction.AttackEffect.SLASH_VERTICAL));
        }
    }
}
