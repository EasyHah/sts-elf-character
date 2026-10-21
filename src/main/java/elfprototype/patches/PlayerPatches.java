package elfprototype.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import elfprototype.skin.ElfSkin;
import javassist.CannotCompileException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * Hooks verified against desktop-1.0.jar (12-18-2022) bytecode:
 * - AbstractPlayer.render: draws the body via renderPlayerImage(sb) (skeleton), or via
 *   sb.draw(img, ..., flipH, flipV) when atlas == null || renderCorpse (corpse after death).
 *   Both body draws are swapped; health bar, orbs, stance, hitboxes are untouched.
 * - AbstractPlayer.useCard: calls useFastAttackAnimation() for ATTACK cards; one call per card play.
 * - AbstractPlayer.damage: Ironclad/Silent override it and call super; isDead is set only after
 *   Mark of the Bloom / Fairy in a Bottle / Lizard Tail fail.
 */
public class PlayerPatches {
    // Own logger: avoid loading the mod class (and its game imports) during patching.
    private static final Logger LOG = LogManager.getLogger("ElfPrototype");
    private static final String DRAW_SIG = "(Lcom/badlogic/gdx/graphics/Texture;FFFFIIIIZZ)V";

    @SpirePatch(clz = AbstractPlayer.class, method = "render")
    public static class RenderBody {
        @SpireInstrumentPatch
        public static ExprEditor instrument() {
            return new ExprEditor() {
                private int replaced;

                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    String hook = "if (elfprototype.skin.ElfSkin.appliesTo(this)) { elfprototype.skin.ElfSkin.render(this, %s); }"
                            + " else { $_ = $proceed($$); }";
                    // String literal on purpose: AbstractPlayer.class here would load the class before it is patched.
                    if (m.getClassName().equals("com.megacrit.cardcrawl.characters.AbstractPlayer")
                            && m.getMethodName().equals("renderPlayerImage")) {
                        m.replace(String.format(hook, "$1"));
                        LOG.info("[ElfPrototype] patched AbstractPlayer.render -> renderPlayerImage (#" + (++replaced) + ")");
                    } else if (m.getClassName().equals("com.badlogic.gdx.graphics.g2d.SpriteBatch")
                            && m.getMethodName().equals("draw") && m.getSignature().equals(DRAW_SIG)) {
                        m.replace(String.format(hook, "$0"));
                        LOG.info("[ElfPrototype] patched AbstractPlayer.render -> corpse/img draw (#" + (++replaced) + ")");
                    }
                }
            };
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "useCard")
    public static class UseCard {
        @SpirePrefixPatch
        public static void prefix(AbstractPlayer __instance, AbstractCard c, AbstractMonster monster, int energyOnUse) {
            ElfSkin.onCardUsed(__instance, c, monster);
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "damage")
    public static class Damage {
        @SpirePrefixPatch
        public static void prefix(AbstractPlayer __instance, DamageInfo info) {
            ElfSkin.beforeDamage(__instance);
        }

        @SpirePostfixPatch
        public static void postfix(AbstractPlayer __instance, DamageInfo info) {
            ElfSkin.afterDamage(__instance);
        }
    }

    @SpirePatch(clz = AbstractPlayer.class, method = "onVictory")
    public static class Victory {
        @SpirePostfixPatch
        public static void postfix(AbstractPlayer __instance) {
            ElfSkin.onVictory(__instance);
        }
    }
}
