package net.forixaim.euclidia.mob_ai.actions;

import net.forixaim.euclidia.mob_ai.core.AIController;
import net.forixaim.euclidia.mob_ai.data.BossSnapshot;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public interface IAction
{
    default boolean isAttack() { return false; }
    default boolean isGuardBreak() { return false; }
    default boolean isCommandGrab() { return false; }
    default boolean isEvasive() { return false; }
    default boolean isParry() { return false; }
    default boolean isGapCloser() { return false; }
    default boolean isFastCounter() { return false; }
    default boolean isAggressiveChase() { return false; }
    default boolean isRecklessTrade() { return false; }
    default boolean isPoiseProtected() { return false; }
    default boolean isPositioningOnly() { return false; }
    default boolean isZoning() { return false; }

    default boolean canExecute(AIController controller, BossSnapshot snapshot) {
        return true;
    }

    void start(LivingEntityPatch<?> entity, AIController controller);

    void tick(LivingEntityPatch<?> entity, AIController controller);

    void stop(LivingEntityPatch<?> entity, AIController controller);

    boolean continuous();

    boolean interruptible(LivingEntityPatch<?> entity, AIController controller);
}
