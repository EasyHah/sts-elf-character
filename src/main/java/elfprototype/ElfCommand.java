package elfprototype;

import basemod.DevConsole;
import basemod.devcommands.ConsoleCommand;
import elfprototype.skin.ElfSkin;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * BaseMod dev console (open with the ` key):
 *   elf status | elf on | elf off | elf play <action> | elf reset | elf debug | elf arrows
 *   elf scale <float> | elf target <PlayerClass>
 *   elf cursed   toggles "cursed card in hand" (intermediate idle) until the card layer drives it
 */
public class ElfCommand extends ConsoleCommand {
    private static final String[] SUBS = {"status", "on", "off", "play", "reset", "debug", "arrows", "cursed", "scale", "target", "help"};

    public ElfCommand() {
        minExtraTokens = 0;
        maxExtraTokens = 2;
        simpleCheck = false;
        requiresPlayer = false;
    }

    @Override
    protected void execute(String[] tokens, int depth) {
        String sub = tokens.length > depth + 1 ? tokens[depth + 1].toLowerCase() : "help";
        String arg = tokens.length > depth + 2 ? tokens[depth + 2] : null;
        switch (sub) {
            case "status":
                DevConsole.log(ElfSkin.status());
                break;
            case "on":
            case "off":
                ElfConfig.setBool(ElfConfig.ENABLED, sub.equals("on"));
                DevConsole.log("elf skin " + (sub.equals("on") ? "ENABLED" : "DISABLED (vanilla body)"));
                break;
            case "play":
                if (arg == null) {
                    DevConsole.log("usage: elf play <" + String.join("|", ElfSkin.ACTIONS) + ">");
                } else {
                    DevConsole.log(ElfSkin.debugPlay(arg.toLowerCase()));
                }
                break;
            case "reset":
                ElfSkin.debugReset();
                DevConsole.log("reset to idle (death lock cleared; a still-dead player re-locks next frame)");
                break;
            case "debug":
                ElfConfig.setBool(ElfConfig.DEBUG_OVERLAY, !ElfConfig.debugOverlay());
                DevConsole.log("debug overlay " + (ElfConfig.debugOverlay() ? "on" : "off"));
                break;
            case "cursed":
                boolean on = !ElfSkin.isCursedInHand();
                ElfSkin.setCursedInHand(on);
                DevConsole.log("cursed in hand " + (on ? "on (intermediate idle)" : "off (elf idle)"));
                break;
            case "arrows":
                ElfConfig.setBool(ElfConfig.ARROWS, !ElfConfig.arrows());
                DevConsole.log("arrows " + (ElfConfig.arrows() ? "on" : "off"));
                break;
            case "scale":
                try {
                    float v = Float.parseFloat(arg);
                    if (v <= 0.05f || v >= 10f) throw new NumberFormatException();
                    ElfConfig.setFloat(ElfConfig.SCALE, v);
                    DevConsole.log("scale = " + v);
                } catch (Exception e) {
                    DevConsole.log("usage: elf scale <0.05..10>");
                }
                break;
            case "target":
                if (arg == null) {
                    DevConsole.log("usage: elf target <IRONCLAD|THE_SILENT|DEFECT|WATCHER>");
                } else {
                    ElfConfig.setString(ElfConfig.TARGET, arg.toUpperCase());
                    DevConsole.log("target = " + arg.toUpperCase());
                }
                break;
            default:
                DevConsole.log("elf " + String.join("|", SUBS));
        }
    }

    @Override
    protected ArrayList<String> extraOptions(String[] tokens, int depth) {
        if (tokens.length > depth + 1 && tokens[depth + 1].equalsIgnoreCase("play")) {
            if (tokens.length > depth + 2 && Arrays.asList(ElfSkin.ACTIONS).contains(tokens[depth + 2].toLowerCase())) {
                complete = true;
            }
            return new ArrayList<>(Arrays.asList(ElfSkin.ACTIONS));
        }
        if (tokens.length > depth + 1 && tokens[depth + 1].equalsIgnoreCase("target")) {
            return new ArrayList<>(Arrays.asList("IRONCLAD", "THE_SILENT", "DEFECT", "WATCHER"));
        }
        if (tokens.length > depth + 1 && Arrays.asList(SUBS).contains(tokens[depth + 1].toLowerCase())) {
            complete = true;
        }
        return new ArrayList<>(Arrays.asList(SUBS));
    }
}
