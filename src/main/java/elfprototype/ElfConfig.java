package elfprototype;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;

import java.util.Properties;

/** Persisted test switches (SpireConfig; the actual file path is logged at startup). */
public final class ElfConfig {
    public static final String ENABLED = "enabled";
    public static final String TARGET = "targetCharacter";
    public static final String SCALE = "scale";
    public static final String DEBUG_OVERLAY = "debugOverlay";
    public static final String ARROWS = "arrows";

    private static SpireConfig config;

    private ElfConfig() {
    }

    static void load() {
        Properties d = new Properties();
        d.setProperty(ENABLED, "true");
        d.setProperty(TARGET, "IRONCLAD");
        d.setProperty(SCALE, "1.0");
        d.setProperty(DEBUG_OVERLAY, "false");
        d.setProperty(ARROWS, "true");
        try {
            config = new SpireConfig(ElfPrototypeMod.MOD_ID, "ElfPrototypeConfig", d);
            ElfPrototypeMod.logger.info("[ElfPrototype] config file: "
                    + SpireConfig.makeFilePath(ElfPrototypeMod.MOD_ID, "ElfPrototypeConfig"));
        } catch (Exception e) {
            ElfPrototypeMod.logger.error("[ElfPrototype] config load failed, using defaults", e);
        }
    }

    public static boolean enabled() {
        return config == null || config.getBool(ENABLED);
    }

    public static String target() {
        return config == null ? "IRONCLAD" : config.getString(TARGET);
    }

    public static float scale() {
        if (config == null) return 1f;
        try {
            float s = config.getFloat(SCALE);
            return s > 0.05f && s < 10f ? s : 1f;
        } catch (NumberFormatException e) {
            return 1f;
        }
    }

    public static boolean debugOverlay() {
        return config != null && config.getBool(DEBUG_OVERLAY);
    }

    public static boolean arrows() {
        return config == null || config.getBool(ARROWS);
    }

    public static void setBool(String key, boolean v) {
        if (config == null) return;
        config.setBool(key, v);
        save();
    }

    public static void setString(String key, String v) {
        if (config == null) return;
        config.setString(key, v);
        save();
    }

    public static void setFloat(String key, float v) {
        if (config == null) return;
        config.setFloat(key, v);
        save();
    }

    private static void save() {
        try {
            config.save();
        } catch (Exception e) {
            ElfPrototypeMod.logger.error("[ElfPrototype] config save failed", e);
        }
    }
}
