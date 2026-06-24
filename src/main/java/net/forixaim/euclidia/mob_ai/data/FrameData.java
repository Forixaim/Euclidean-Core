package net.forixaim.euclidia.mob_ai.data;

import net.forixaim.euclidia.mixin.AttackAnimationTracker;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Optional;

/**
 * An immutable capsule representing an entity's exact positioning 
 * on an Epic Fight animation timeline for a specific tick.
 */
public record FrameData(
    float startup,
    float lastActive,
    float endlag
) {
    /**
     * Determines the conceptual fighting game phase of the animation.
     */
    public OpponentSnapshot.AnimationPhase getPhase(float elapsedTime) {
        if (elapsedTime < startup) {
            return OpponentSnapshot.AnimationPhase.STARTUP;
        } else if (elapsedTime >= startup && elapsedTime <= lastActive) {
            return OpponentSnapshot.AnimationPhase.ACTIVE; // The weapon hitbox is hot!
        } else {
            return OpponentSnapshot.AnimationPhase.ENDLAG; // Vulnerable recovery frames
        }
    }

    public static Optional<FrameData> getFrameData(LivingEntityPatch<?> opponent)
    {
        if (opponent instanceof AttackAnimationTracker tracker && tracker.euclidia$getAttackAnimation() != null && tracker.euclidia$getAttackAnimation().get() instanceof AttackAnimation animation)
        {
            float startup = animation.getTransitionTime() + animation.phases[0].preDelay;
            float lastActive = animation.getTransitionTime() + animation.phases[animation.phases.length - 1].contact;
            float endlag = animation.getTransitionTime() + animation.phases[animation.phases.length - 1].recovery;
            return Optional.of(new FrameData(startup, lastActive, endlag));
        }
        return Optional.empty();
    }

    /**
     * Calculates exactly how many remaining ticks a target is stuck in recovery.
     * Returns 0 if they are not in their endlag window.
     */
    public float getRemainingEndlag(float elapsedTime) {
        if (getPhase(elapsedTime) == OpponentSnapshot.AnimationPhase.ENDLAG) {
            return endlag - elapsedTime;
        }
        return 0.0f;
    }
}