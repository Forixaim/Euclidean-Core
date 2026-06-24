package net.forixaim.euclidia.mob_ai.actions;

import net.forixaim.euclidia.mob_ai.core.AIController;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.function.Predicate;

public abstract class AbstractTimedAction implements IAction {
    protected final int duration;
    protected final Predicate<LivingEntityPatch<?>> condition;

    public AbstractTimedAction(int duration)
    {
        this.duration = duration;
        this.condition = c -> false;
    }

    public AbstractTimedAction(int duration, Predicate<LivingEntityPatch<?>> condition) {
        this.duration = duration;
        this.condition = condition;
    }

    @Override
    public void tick(LivingEntityPatch<?> entity, AIController controller) {
        if (controller.getTicksSinceActionStarted() > duration || condition.test(entity)) {
            controller.stopAction();
        }
    }

    @Override
    public boolean continuous() { return true; }

    @Override
    public boolean interruptible(LivingEntityPatch<?> entity, AIController controller) { return true; }
}