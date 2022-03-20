package components;

import util.AssetPool;

import java.util.ArrayList;
import java.util.List;

public class AnimationState {

    public String title;
    public List<Frame> frames = new ArrayList<>();
    public boolean doLoop = false;

    // default white sprite
    private static Sprite defaultSprite = new Sprite();
    private transient float timeTracker = 0.0f;
    private transient int currentSprite = 0;

    public void refreshTextures() {
        for (Frame frame : frames) {
            frame.sprite.setTexture(AssetPool.getTexture(frame.sprite.getTexture().getFilePath()));
        }
    }

    public void addFrame(Sprite sprite, float frameTime) {
        frames.add(new Frame(sprite, frameTime));
    }

    public void setLoop(boolean doLoop) {
        this.doLoop = doLoop;
    }

    public void update(float dt) {
        if (currentSprite < frames.size()) {
            timeTracker -= dt;
            if (timeTracker <= 0) {
                // if we need to move to the next frame, then move to the next frame
                if (currentSprite != frames.size() - 1 || doLoop) {
                    currentSprite = (currentSprite + 1) % frames.size();
                }
                timeTracker = frames.get(currentSprite).frameTime;
            }
        }
    }

    public Sprite getCurrentSprite() {
        if (currentSprite < frames.size()) {
            return frames.get(currentSprite).sprite;
        }
        return defaultSprite;
    }
}
