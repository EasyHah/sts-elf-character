package elfprototype.cards;

import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import elfprototype.mechanics.FlipHandAction;

/** 0 cost. Light: erode 1, draw 1 (2). Cursed: erode 2 (+draw 1 when upgraded). magic = erode, magic2 = draw. */
public class CurseWhisper extends DualFaceCard {
    public static final String SIMPLE_ID = "CurseWhisper";

    public CurseWhisper() {
        super(SIMPLE_ID, 0, CardType.SKILL, CardRarity.BASIC,
                CardTarget.SELF, FaceStats.of().magic(1, 0).magic2(1, 1),
                CardTarget.SELF, FaceStats.of().magic(2, 0).magic2(0, 1));
        setSeal(Seal.of(Seal.Kind.BLOCK, 2, 1));
    }

    @Override
    protected void useLight(AbstractPlayer p, AbstractMonster m) {
        addToBot(FlipHandAction.erode(magicNumber));
        addToBot(new DrawCardAction(p, face().magic2));
    }

    @Override
    protected void useCursed(AbstractPlayer p, AbstractMonster m) {
        addToBot(FlipHandAction.erode(magicNumber));
        if (face().magic2 > 0) addToBot(new DrawCardAction(p, face().magic2));
    }
}
