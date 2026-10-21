package elfprototype;

import elfprototype.anim.ClipData;
import elfprototype.anim.ClipJson;
import elfprototype.anim.ElfAnimator;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/** Runs against the real animation.json files shipped in src/main/resources. */
public class ElfAnimatorTest {
    private static final String ANIM = "elfprototype/images/character/animations/";
    private static final String ROOT = ANIM + "elf/";
    private static final String[] ACTIONS = {"idle", "attack", "cast", "guard", "hit", "death", "victory"};
    private static final String CURSED_IDLE_DIR = ANIM + "intermediate/idle/";
    private static final String CURSED_ATTACK_DIR = ANIM + "dark/attack/";

    private Map<String, ClipData> clips;
    private ElfAnimator a;
    private final List<String> events = new ArrayList<>();

    static String read(String path) throws Exception {
        try (InputStream in = ElfAnimatorTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull("missing resource " + path, in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    @Before
    public void setUp() throws Exception {
        clips = new LinkedHashMap<>();
        for (String act : ACTIONS) clips.put(act, ClipJson.parse(read(ROOT + act + "/animation.json")));
        clips.put(ElfAnimator.CURSED_IDLE, ClipJson.parse(read(CURSED_IDLE_DIR + "animation.json")));
        clips.put("attack_cursed", ClipJson.parse(read(CURSED_ATTACK_DIR + "animation.json")));
        a = new ElfAnimator(clips);
        a.setListener((c, e) -> events.add(c.action + "." + e.name));
    }

    private void step(float seconds, float dt) {
        int n = Math.round(seconds / dt);
        for (int i = 0; i < n; i++) a.update(dt);
    }

    @Test
    public void metadataMatchesHandoff() throws Exception {
        int total = 0;
        for (String act : ACTIONS) {
            ClipData c = clips.get(act);
            assertEquals("elf", c.form);
            assertEquals(512, c.canvasW);
            assertEquals(512, c.canvasH);
            assertEquals(256, c.pivotX);
            assertEquals(448, c.pivotY);
            assertEquals(24f, c.fps, 0f);
            total += c.frameCount;
            for (String f : c.frameFiles) {
                String p = ROOT + c.action + "/" + f;
                assertNotNull("frame file missing: " + p, getClass().getClassLoader().getResource(p));
            }
        }
        assertEquals(144, total);
        assertTrue(clips.get("idle").loop);
        assertEquals("idle", clips.get("attack").endAction);
        assertTrue(clips.get("death").holdsLastFrame());
        assertEquals(4, clips.get("attack").events.get(0).frameIndex);
        assertEquals(7, clips.get("cast").events.get(0).frameIndex);
    }

    @Test
    public void idleLoopsAt24fpsIndependentOfDisplayRate() {
        step(1.0f, 1f / 144f); // 144 Hz display
        assertEquals("idle", a.currentAction());
        assertEquals(24, a.frameIndex());
        step(1.03f, 1f / 144f);
        assertTrue(a.frameIndex() <= 1); // wrapped after 48 frames / 2 s
        assertEquals("idle", a.currentAction());
    }

    @Test
    public void attackReturnsToIdleAndFiresEventOnce() {
        assertTrue(a.play("attack", ElfAnimator.Source.GAME));
        step(0.15f, 1f / 60f);
        assertTrue(events.isEmpty());
        step(0.05f, 1f / 60f);
        assertEquals(1, events.size());
        step(0.4f, 1f / 60f);
        assertEquals("idle", a.currentAction());
        assertEquals(1, events.size());
    }

    @Test
    public void eventFiresOnceEvenWhenFramesAreSkipped() {
        a.play("attack", ElfAnimator.Source.GAME);
        a.update(0.45f); // one huge step past frame 4
        assertEquals(1, events.size());
        assertEquals("attack", a.currentAction());
        a.update(0.45f);
        assertEquals("idle", a.currentAction());
        assertEquals(1, events.size());
    }

    @Test
    public void restartedAttackFiresAgainPerPlay() {
        a.play("attack", ElfAnimator.Source.GAME);
        step(0.2f, 1f / 60f);
        a.play("attack", ElfAnimator.Source.GAME); // second card before the first finished
        step(0.2f, 1f / 60f);
        assertEquals(2, events.size());
    }

    @Test
    public void consecutiveHitsRestartAndNeverStick() {
        for (int i = 0; i < 5; i++) {
            a.play("hit", ElfAnimator.Source.GAME);
            step(0.1f, 1f / 60f);
            assertEquals("hit", a.currentAction());
        }
        step(0.3f, 1f / 60f);
        assertEquals("idle", a.currentAction());
    }

    @Test
    public void hitInterruptsAttackThenIdle() {
        a.play("attack", ElfAnimator.Source.GAME);
        step(0.1f, 1f / 60f);
        a.play("hit", ElfAnimator.Source.GAME);
        assertEquals("hit", a.currentAction());
        assertEquals(0, a.frameIndex());
        step(0.3f, 1f / 60f);
        assertEquals("idle", a.currentAction());
    }

    @Test
    public void realDeathLocksOnceAndHoldsLastFrame() {
        assertTrue(a.onRealDeath());
        long plays = a.playCount();
        assertFalse(a.onRealDeath()); // only once
        assertEquals(plays, a.playCount());
        step(3f, 1f / 60f);
        assertEquals("death", a.currentAction());
        assertEquals(23, a.frameIndex());
        assertTrue(a.isHoldingLastFrame());
        assertFalse(a.play("hit", ElfAnimator.Source.GAME));
        assertFalse(a.play("idle", ElfAnimator.Source.DEBUG));
        step(3f, 1f / 60f);
        assertEquals(23, a.frameIndex());
    }

    @Test
    public void reviveAndResetClearDeathLock() {
        a.onRealDeath();
        a.onRevived();
        assertFalse(a.isDeathLocked());
        assertEquals("idle", a.currentAction());
        a.onRealDeath();
        a.reset();
        assertFalse(a.isDeathLocked());
        assertTrue(a.play("attack", ElfAnimator.Source.GAME));
    }

    @Test
    public void debugDeathPreviewDoesNotLock() {
        a.play("death", ElfAnimator.Source.DEBUG);
        step(2f, 1f / 60f);
        assertEquals(23, a.frameIndex());
        assertFalse(a.isDeathLocked());
        assertTrue(a.play("attack", ElfAnimator.Source.GAME));
        assertTrue(a.onRealDeath()); // a real death after a preview still triggers
    }

    @Test
    public void victoryFollowsItsMetadataEndAction() {
        a.play("victory", ElfAnimator.Source.GAME);
        step(2f, 1f / 60f);
        ClipData v = clips.get("victory");
        if (v.holdsLastFrame()) {
            assertEquals("victory", a.currentAction());
            assertEquals(v.frameCount - 1, a.frameIndex());
        } else {
            assertEquals(v.endAction, a.currentAction());
        }
    }

    @Test
    public void zeroAndNaNDeltaAreIgnored() {
        a.play("attack", ElfAnimator.Source.GAME);
        a.update(0f);
        a.update(Float.NaN);
        a.update(-1f);
        assertEquals(0, a.frameIndex());
        assertEquals("attack", a.currentAction());
    }

    // ------------------------------------------------------------ cursed (intermediate / dark) clips

    @Test
    public void cursedClipMetadata() {
        ClipData idle = clips.get(ElfAnimator.CURSED_IDLE);
        assertEquals("intermediate", idle.form);
        assertTrue(idle.loop);
        assertEquals(12f, idle.fps, 0f);
        assertEquals(36, idle.frameCount);
        assertEquals(512, idle.canvasW);

        ClipData atk = clips.get("attack_cursed");
        assertEquals("dark", atk.form);
        assertFalse(atk.loop);
        assertEquals("idle", atk.endAction);
        assertEquals(576, atk.canvasW); // right padding keeps the blade tips
        assertEquals(512, atk.canvasH);
        assertEquals(12, atk.frameCount);
        assertEquals(5, atk.events.get(0).frameIndex);

        for (ClipData c : new ClipData[]{idle, atk}) {
            assertEquals(256, c.pivotX); // same foot pivot as the base elf
            assertEquals(448, c.pivotY);
            String dir = c == idle ? CURSED_IDLE_DIR : CURSED_ATTACK_DIR;
            for (String f : c.frameFiles) {
                assertNotNull("frame file missing: " + dir + f, getClass().getClassLoader().getResource(dir + f));
            }
        }
    }

    @Test
    public void cursedIdleSwitchesImmediatelyWhenIdle() {
        a.setCursedIdle(true);
        assertEquals(ElfAnimator.CURSED_IDLE, a.currentAction());
        a.setCursedIdle(false);
        assertEquals("idle", a.currentAction());
    }

    @Test
    public void actionsReturnToTheMatchingIdle() {
        a.setCursedIdle(true);
        a.play("attack", ElfAnimator.Source.GAME);
        step(0.6f, 1f / 60f);
        assertEquals(ElfAnimator.CURSED_IDLE, a.currentAction());

        a.play("attack_cursed", ElfAnimator.Source.GAME);
        step(0.6f, 1f / 60f);
        assertEquals(ElfAnimator.CURSED_IDLE, a.currentAction());

        a.setCursedIdle(false);
        a.play("attack_cursed", ElfAnimator.Source.GAME);
        step(0.6f, 1f / 60f);
        assertEquals("idle", a.currentAction());
    }

    @Test
    public void togglingCursedDoesNotInterruptAnAction() {
        a.play("attack_cursed", ElfAnimator.Source.GAME);
        step(0.1f, 1f / 60f);
        a.setCursedIdle(true);
        assertEquals("attack_cursed", a.currentAction());
        step(0.5f, 1f / 60f);
        assertEquals(ElfAnimator.CURSED_IDLE, a.currentAction());
    }

    @Test
    public void cursedAttackFiresItsHitOnce() {
        a.play("attack_cursed", ElfAnimator.Source.GAME);
        step(0.19f, 1f / 60f);
        assertTrue(events.isEmpty());
        step(0.05f, 1f / 60f);
        assertEquals(java.util.Collections.singletonList("attack_cursed.visual_hit"), events);
    }

    @Test
    public void cursedIdleNeverOverridesDeath() {
        a.setCursedIdle(true);
        a.onRealDeath();
        a.setCursedIdle(false);
        a.setCursedIdle(true);
        assertEquals("death", a.currentAction());
        a.onRevived();
        assertEquals(ElfAnimator.CURSED_IDLE, a.currentAction());
    }
}
