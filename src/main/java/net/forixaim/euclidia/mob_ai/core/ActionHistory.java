package net.forixaim.euclidia.mob_ai.core;

import com.google.common.collect.ImmutableList;
import net.forixaim.euclidia.mob_ai.actions.IAction;
import net.forixaim.euclidia.mob_ai.actions.PlayAnimationAction;
import net.minecraft.core.Holder;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ActionHistory
{
    private final Queue<Holder<IAction>> actions = new ConcurrentLinkedQueue<>();

    public void add(Holder<IAction> action)
    {
        actions.add(action);
        if (actions.size() > 8)
        {
            actions.poll();
        }
    }

    public Holder<IAction> findByClass(Class<?> actionClass)
    {
        return this.getSnapshotView().stream().filter(action -> actionClass.isInstance(action.value())).findFirst().orElse(null);
    }

    public Holder<IAction> containsAnimation(AnimationManager.AnimationAccessor<? extends StaticAnimation> animationAccessor)
    {
        return this.getSnapshotView().stream().filter(action -> action instanceof PlayAnimationAction animationAction  && animationAction.getAnimationAccessor() == animationAccessor).findFirst().orElse(null);
    }

    /**
     * Calculates an anti-spam penalty based on the action's presence and
     * position within the rolling 8-move history window.
     */
    public float getPenaltyFor(IAction action) {
        if (this.actions.isEmpty()) return 0.0f;

        float totalPenalty = 0.0f;
        int totalElements = this.actions.size();
        List<Holder<IAction>> historicalList = this.getSnapshotView();

        for (int i = 0; i < totalElements; i++) {
            Holder<IAction> historicalAction = historicalList.get(i);

            if (historicalAction.value() == action) {
                int stepsAgo = (totalElements - 1) - i;
                float recencyMultiplier = 1.0f / (stepsAgo + 1);
                totalPenalty += 1.5f * recencyMultiplier;
            }
        }

        return totalPenalty;
    }

    /**
     * Calculates how predictable the boss is by counting how many times
     * consecutive actions belong to the same execution class.
     * Returns a normalized value from 0.0 (unpredictable) to 1.0 (pure spam).
     */
    public float calculateSequenceRepetitionScore() {
        if (actions.size() < 2) return 0.0f;

        int repetitions = 0;
        Holder<IAction> lastAction = null;

        for (Holder<IAction> current : actions) {
            if (lastAction != null && current.value() == lastAction.value()) {
                repetitions++;
            }
            lastAction = current;
        }

        return (float) repetitions / 7.0f;
    }

    public List<Holder<IAction>> getSnapshotView() {
        return ImmutableList.copyOf(actions);
    }
}
