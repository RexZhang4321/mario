package components;

import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.util.*;

public class StateMachine extends Component {
    // map of the StateTrigger to the state title of the destination AnimationState
    public Map<StateTrigger, String> stateTransfers = new HashMap<>();

    private Map<String, AnimationState> stateMap = new HashMap<>();
    private transient AnimationState currentState = null;
    private String defaultStateTitle = "";

    @Override
    public void start() {
        for (AnimationState state : stateMap.values()) {
            if (state.title.equals(defaultStateTitle)) {
                currentState = state;
                break;
            }
        }
    }

    @Override
    public void update(float dt) {
        updateInternal(dt);
    }

    @Override
    public void editorUpdate(float dt) {
        updateInternal(dt);
    }

    @Override
    public void imGui() {
        int index = 0;
        for (AnimationState state : stateMap.values()) {
            ImString title = new ImString(state.title);
            ImGui.inputText("State: ", title);
            state.title = title.get();

            ImBoolean doLoop = new ImBoolean(state.doLoop);
            ImGui.checkbox("Does Loop? ", doLoop);
            state.doLoop = doLoop.get();
            for (Frame frame : state.frames) {
                float[] tmp = new float[1];
                tmp[0] = frame.frameTime;
                ImGui.dragFloat("Frame(" + index + ") Time: ", tmp, 0.01f);
                frame.frameTime = tmp[0];
                index++;
            }
        }
    }

    private void updateInternal(float dt) {
        if (currentState != null) {
            currentState.update(dt);
            SpriteRenderer spriteRenderer = gameObject.getComponent(SpriteRenderer.class);
            if (spriteRenderer != null) {
                spriteRenderer.setSprite(currentState.getCurrentSprite());
            }
        }
    }

    public void refreshTextures() {
        for (AnimationState state : stateMap.values()) {
            state.refreshTextures();
        }
    }

    public void setDefaultStateTitle(String title) {
        for (AnimationState state : stateMap.values()) {
            if (state.title.equals(title)) {
                defaultStateTitle = title;
                if (currentState == null) {
                    currentState = state;
                    return;
                }
            }
        }
        System.out.println("Unable to find state " + title + " in set default state title");
    }

    public void addStateTrigger(String from, String to, String onTrigger) {
        stateTransfers.put(new StateTrigger(from, onTrigger), to);
    }

    public void addState(AnimationState state) {
        stateMap.put(state.title, state);
    }

    public void trigger(String trigger) {
        Optional<AnimationState> maybeAnimationState = stateTransfers.entrySet()
                .stream()
                .filter(entry -> entry.getKey().trigger.equals(trigger) && entry.getKey().stateTitle.equals(currentState.title))
                .findFirst()
                .map(entry -> stateMap.get(entry.getValue()));
        if (maybeAnimationState.isPresent()) {
            currentState = maybeAnimationState.get();
        } else {
            System.out.println("Unable to find trigger: " + trigger + ". Current state: " + currentState.title);
        }
    }

    private class StateTrigger {
        public String stateTitle;
        public String trigger;

        public StateTrigger() {
        }

        public StateTrigger(String stateTitle, String trigger) {
            this.stateTitle = stateTitle;
            this.trigger = trigger;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StateTrigger that = (StateTrigger) o;
            return Objects.equals(stateTitle, that.stateTitle) && Objects.equals(trigger, that.trigger);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stateTitle, trigger);
        }
    }
}
