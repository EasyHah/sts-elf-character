package elfprototype.character;

import com.badlogic.gdx.graphics.Color;

/**
 * The elf's card color. Kept out of ElfCharacter on purpose: the mod constructor registers the color
 * before the game is initialized, and touching ElfCharacter there would load AbstractPlayer /
 * AbstractCreature, whose static initializer needs Settings and crashes (NullPointerException).
 */
public final class ElfColor {
    public static final Color CARD = new Color(0.36f, 0.62f, 0.38f, 1f);

    private ElfColor() {
    }
}
