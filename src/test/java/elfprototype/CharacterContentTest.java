package elfprototype;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import elfprototype.cards.FaceStats;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Game-free checks of all elf content. Card / power / relic ids are read from the Java sources
 * (the classes need a running game to instantiate), then matched against strings and art in the resources.
 */
public class CharacterContentTest {
    private static final String ROOT = "elfprototype/";
    private static final Path SRC = Paths.get("src", "main", "java", "elfprototype");
    private static final String[] LANGS = {"zhs", "eng"};

    private static String source(String... parts) throws IOException {
        return new String(Files.readAllBytes(SRC.resolve(Paths.get("", parts))), StandardCharsets.UTF_8);
    }

    private static String allSources(String dir) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Stream<Path> s = Files.list(SRC.resolve(dir))) {
            for (Path p : (Iterable<Path>) s::iterator) sb.append(new String(Files.readAllBytes(p), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    /** simple id -> card type folder, for every DualFaceCard / ElfPowerCard constructor call. */
    private static Map<String, String> cards() throws IOException {
        Map<String, String> out = new LinkedHashMap<>();
        String cards = allSources("cards");
        Matcher dual = Pattern.compile("super\\(\"(\\w+)\", \\d+, CardType\\.(ATTACK|SKILL)").matcher(cards);
        while (dual.find()) out.put(dual.group(1), dual.group(2).toLowerCase());
        Matcher starter = Pattern.compile("SIMPLE_ID = \"(\\w+)\";[\\s\\S]*?CardType\\.(ATTACK|SKILL)").matcher(cards);
        while (starter.find()) out.put(starter.group(1), starter.group(2).toLowerCase());
        Matcher power = Pattern.compile("super\\(\"(\\w+)\", \\d+, CardRarity\\.").matcher(cards);
        while (power.find()) out.put(power.group(1), "power");
        return out;
    }

    private static List<String[]> powers() throws IOException {
        List<String[]> out = new ArrayList<>();
        Matcher m = Pattern.compile("super\\((?:cursed \\? \"(\\w+)\" : \"(\\w+)\"|\"(\\w+)\"), \"(\\w+)\"")
                .matcher(allSources("powers"));
        while (m.find()) {
            if (m.group(3) != null) out.add(new String[]{m.group(3), m.group(4)});
            else {
                out.add(new String[]{m.group(1), m.group(4)});
                out.add(new String[]{m.group(2), m.group(4)});
            }
        }
        return out;
    }

    private static List<String> relics() throws IOException {
        List<String> out = new ArrayList<>();
        out.add("CrackedSeal");
        Matcher m = Pattern.compile("super\\(\"(\\w+)\", RelicTier\\.").matcher(source("relics", "ElfRelics.java"));
        while (m.find()) out.add(m.group(1));
        return out;
    }

    private static JsonElement json(String path) throws Exception {
        return new JsonParser().parse(ElfAnimatorTest.read(ROOT + path));
    }

    private void assertResource(String path) {
        assertNotNull("missing " + path, getClass().getClassLoader().getResource(ROOT + path));
    }

    @Test
    public void upgradeAppliesDeltasOnce() {
        FaceStats f = FaceStats.of().damage(4, 1).hits(2, 0);
        FaceStats u = f.upgraded();
        assertEquals(5, u.damage);
        assertEquals(2, u.magic);
        assertTrue(u.hits);
        assertEquals(-1, u.block); // unused stays unused
        assertTrue(f.damageUpgrades());
        assertFalse(f.magicUpgrades());
        assertEquals(5, u.upgraded().damage); // deltas are consumed by the first upgrade
    }

    @Test
    public void flagsAndFaceCostSurviveUpgrade() {
        FaceStats f = FaceStats.of().magic(2, 0).magic2(0, 1).block(3, 2).damage(3, 1).aoe().exhaust().cost(2, -1);
        FaceStats u = f.upgraded();
        assertEquals(1, u.magic2);
        assertEquals(5, u.block);
        assertEquals(4, u.damage);
        assertTrue(u.aoe);
        assertTrue(u.exhaust);
        assertEquals(1, u.cost);
        assertEquals(-1, FaceStats.of().upgraded().cost); // no face cost stays "use the card's cost"
        assertEquals(0, FaceStats.of().cost(0, -1).upgraded().cost); // never negative
    }

    /** Cursed faces return to hand, so a 0-cost cursed face would let two eroding cards loop for free. */
    @Test
    public void cursedFacesCostAtLeastOne() {
        assertEquals(1, FaceStats.faceCost(0, true));
        assertEquals(2, FaceStats.faceCost(2, true));
        assertEquals(0, FaceStats.faceCost(0, false)); // light faces may cost 0
        assertEquals(-1, FaceStats.faceCost(-1, true)); // X cost untouched
    }

    @Test
    public void allCardsAreRegisteredAndMatchTheDesign() throws Exception {
        Map<String, String> cards = cards();
        assertEquals("starter 4 + pool 68", 72, cards.size());
        long powers = cards.values().stream().filter("power"::equals).count();
        assertEquals(13, powers);
        String registry = source("cards", "ElfCards.java");
        for (String id : cards.keySet()) {
            String cls = id.equals("Strike_Elf") ? "StrikeElf" : id.equals("Defend_Elf") ? "DefendElf" : id;
            assertTrue("not registered in ElfCards: " + id, registry.contains("." + cls + "()") || registry.contains("new " + cls + "()"));
        }
    }

    @Test
    public void everyCardHasTextInBothLanguages() throws Exception {
        Map<String, String> cards = cards();
        for (String lang : LANGS) {
            JsonObject strings = json("localization/" + lang + "/CardStrings.json").getAsJsonObject();
            assertEquals(lang, cards.size(), strings.entrySet().size());
            for (Map.Entry<String, String> e : cards.entrySet()) {
                JsonObject c = strings.getAsJsonObject("elfprototype:" + e.getKey());
                assertNotNull(lang + " missing " + e.getKey(), c);
                assertFalse(c.get("NAME").getAsString().isEmpty());
                assertFalse(c.get("DESCRIPTION").getAsString().isEmpty());
                if (!e.getValue().equals("power")) {
                    JsonArray ext = c.getAsJsonArray("EXTENDED_DESCRIPTION");
                    assertTrue(lang + " " + e.getKey() + " needs cursed name + text", ext != null && ext.size() >= 2);
                }
            }
        }
    }

    @Test
    public void everyCardHasArt() throws Exception {
        for (Map.Entry<String, String> e : cards().entrySet()) {
            String base = "images/cards/" + e.getValue() + "/" + e.getKey();
            String[] suffixes = e.getValue().equals("power") ? new String[]{"", "_p"}
                    : new String[]{"", "_p", "_cursed", "_cursed_p"};
            for (String s : suffixes) assertResource(base + s + ".png");
        }
    }

    @Test
    public void everyPowerHasTextAndIcon() throws Exception {
        List<String[]> powers = powers();
        assertEquals("15 simple + Nock / Echo light and cursed", 19, powers.size());
        for (String lang : LANGS) {
            JsonObject strings = json("localization/" + lang + "/PowerStrings.json").getAsJsonObject();
            assertEquals(3, strings.getAsJsonObject("elfprototype:Chime").getAsJsonArray("DESCRIPTIONS").size());
            for (String[] p : powers) {
                JsonObject s = strings.getAsJsonObject("elfprototype:" + p[0]);
                assertNotNull(lang + " missing power " + p[0], s);
                assertTrue(s.getAsJsonArray("DESCRIPTIONS").size() >= 1);
            }
        }
        for (String[] p : powers) {
            assertResource("images/powers/" + p[1] + ".png");
            assertResource("images/powers/large/" + p[1] + ".png");
        }
        assertResource("images/powers/Chime.png");
    }

    @Test
    public void everyRelicHasTextAndImages() throws Exception {
        List<String> relics = relics();
        assertEquals(10, relics.size());
        for (String lang : LANGS) {
            JsonObject strings = json("localization/" + lang + "/RelicStrings.json").getAsJsonObject();
            for (String r : relics) assertNotNull(lang + " missing relic " + r, strings.get("elfprototype:" + r));
        }
        for (String r : relics) {
            assertResource("images/relics/" + r + ".png");
            assertResource("images/relics/" + r + "Outline.png");
            assertResource("images/relics/large/" + r + ".png");
        }
    }

    /**
     * The mod constructor runs before the game is initialized. Touching a class that extends a game creature
     * (ElfCharacter -> AbstractPlayer -> AbstractCreature) there crashes the launch with a NullPointerException
     * in AbstractCreature's static initializer, so the constructor may only use the safe classes listed here.
     */
    @Test
    public void modConstructorTouchesNoGameCreatureClass() throws Exception {
        String src = source("ElfPrototypeMod.java");
        int start = src.indexOf("public ElfPrototypeMod() {");
        assertTrue(start >= 0);
        String body = src.substring(start, src.indexOf("\n    }\n", start));
        for (String forbidden : new String[]{"ElfCharacter", "ElfCards", "ElfRelics", "CrackedSeal", "ChimePower",
                "DualFaceCard", "ElfSkin"}) {
            assertFalse("mod constructor must not touch " + forbidden + " (game not initialized yet)",
                    body.contains(forbidden));
        }
    }

    @Test
    public void uiCharacterAndKeywordText() throws Exception {
        for (String lang : LANGS) {
            assertTrue(json("localization/" + lang + "/UIStrings.json").getAsJsonObject()
                    .getAsJsonObject("elfprototype:Lumen").getAsJsonArray("TEXT").size() >= 7); // FlipHand, tooltip, pick prompts
            JsonObject ch = json("localization/" + lang + "/CharacterStrings.json").getAsJsonObject()
                    .getAsJsonObject("elfprototype:Elf");
            assertEquals(2, ch.getAsJsonArray("NAMES").size());
            assertEquals(3, ch.getAsJsonArray("TEXT").size());
            assertEquals(5, json("localization/" + lang + "/Keywords.json").getAsJsonArray().size());
        }
        for (String p : new String[]{"character/select/button.png", "character/select/portrait.png",
                "character/energyorb/cover.png", "character/cardback_cursed/bg_attack_p.png",
                "character/cardback_cursed/bg_skill_p.png", "character/cardback_cursed/bg_power_p.png",
                "character/cardback_cursed/energy_orb_p.png"}) {
            assertResource("images/" + p);
        }
    }
}
