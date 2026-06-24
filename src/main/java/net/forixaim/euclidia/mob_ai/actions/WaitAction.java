package net.forixaim.euclidia.mob_ai.actions;

import net.forixaim.euclidia.mob_ai.core.AIController;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.function.Predicate;

public class WaitAction extends AbstractTimedAction
{

    public WaitAction(int duration) {
        super(duration);
    }

    public WaitAction(int duration, Predicate<LivingEntityPatch<?>> condition) {
        super(duration, condition);
    }

    @Override
    public void start(LivingEntityPatch<?> entity, AIController controller)
    {

    }

    @Override
    public void stop(LivingEntityPatch<?> entity, AIController controller)
    {
    }
}
