package elfprototype.anim;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the resource pack's custom animation.json (NOT Spine, NOT a LibGDX .atlas).
 * Only the fields needed by the full-frame player are read; atlas/trim data is ignored on purpose.
 */
public final class ClipJson {
    private ClipJson() {
    }

    @SuppressWarnings("deprecation") // the game's bundled Gson predates JsonParser.parseString
    public static ClipData parse(String json) {
        JsonObject o = new JsonParser().parse(json).getAsJsonObject();
        String action = o.get("action").getAsString();
        String form = o.has("form") ? o.get("form").getAsString() : "elf";
        int frameCount = o.get("frame_count").getAsInt();
        float fps = o.get("fps").getAsFloat();
        boolean loop = o.has("loop") && o.get("loop").getAsBoolean();
        String endAction = o.has("end_action") ? o.get("end_action").getAsString() : "idle";

        JsonArray canvas = o.getAsJsonArray("canvas");
        int cw = canvas.get(0).getAsInt();
        int ch = canvas.get(1).getAsInt();

        JsonObject pivot = o.getAsJsonObject("pivot");
        if (pivot.has("origin") && !"top_left".equals(pivot.get("origin").getAsString())) {
            throw new IllegalArgumentException(action + ": unsupported pivot origin " + pivot.get("origin"));
        }
        JsonArray xy = pivot.getAsJsonArray("xy");
        int px = xy.get(0).getAsInt();
        int py = xy.get(1).getAsInt();

        if (o.has("frame_index_base") && o.get("frame_index_base").getAsInt() != 0) {
            throw new IllegalArgumentException(action + ": only frame_index_base 0 is supported");
        }

        List<ClipData.Event> events = new ArrayList<>();
        if (o.has("events")) {
            for (JsonElement e : o.getAsJsonArray("events")) {
                JsonObject eo = e.getAsJsonObject();
                int idx = eo.get("frame_index").getAsInt();
                float t = eo.has("time_seconds") ? eo.get("time_seconds").getAsFloat() : idx / fps;
                events.add(new ClipData.Event(eo.get("name").getAsString(), idx, t));
            }
        }

        // Frames are placed by their "index" field, not by array order.
        String[] files = new String[frameCount];
        for (JsonElement e : o.getAsJsonArray("frames")) {
            JsonObject fo = e.getAsJsonObject();
            int idx = fo.get("index").getAsInt();
            if (idx < 0 || idx >= frameCount) {
                throw new IllegalArgumentException(action + ": frame index out of range " + idx);
            }
            if (files[idx] != null) throw new IllegalArgumentException(action + ": duplicate frame index " + idx);
            files[idx] = fo.get("file").getAsString();
        }
        List<String> frameFiles = new ArrayList<>();
        for (int i = 0; i < frameCount; i++) {
            if (files[i] == null) throw new IllegalArgumentException(action + ": missing frame " + i);
            frameFiles.add(files[i]);
        }
        return new ClipData(action, form, frameCount, fps, loop, endAction, cw, ch, px, py, events, frameFiles);
    }
}
