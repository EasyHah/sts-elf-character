package elfprototype.skin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.megacrit.cardcrawl.vfx.AbstractGameEffect;

/** Purely visual arrow from the bow to the target. Damage is resolved by the card's own actions. */
public class ElfArrowEffect extends AbstractGameEffect {
    private static final float DURATION = 0.18f;
    private static Texture img; // shared, lives for the whole process

    private final float sx, sy, tx, ty;
    private final float drawScale;

    public ElfArrowEffect(float sx, float sy, float tx, float ty, float drawScale) {
        this.sx = sx;
        this.sy = sy;
        this.tx = tx;
        this.ty = ty;
        this.drawScale = drawScale;
        this.duration = DURATION;
        this.startingDuration = DURATION;
        this.rotation = MathUtils.atan2(ty - sy, tx - sx) * MathUtils.radiansToDegrees;
        this.color = Color.WHITE.cpy();
        if (img == null) {
            img = new Texture(Gdx.files.internal(ElfSkin.ARROW_IMG));
            img.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
    }

    @Override
    public void update() {
        duration -= Gdx.graphics.getDeltaTime();
        if (duration <= 0f) isDone = true;
    }

    @Override
    public void render(SpriteBatch sb) {
        float t = 1f - Math.max(0f, duration) / startingDuration;
        float x = MathUtils.lerp(sx, tx, t);
        float y = MathUtils.lerp(sy, ty, t);
        int w = img.getWidth(), h = img.getHeight();
        // Arrow art points right; rotation is about the image center.
        Color prev = sb.getColor().cpy();
        sb.setColor(color);
        sb.draw(img, x - w / 2f, y - h / 2f, w / 2f, h / 2f, w, h, drawScale, drawScale, rotation,
                0, 0, w, h, false, false);
        sb.setColor(prev);
    }

    @Override
    public void dispose() {
        // texture is shared and intentionally kept
    }
}
