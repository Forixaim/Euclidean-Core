package net.forixaim.euclidia.mob_ai.actions;

import net.forixaim.euclidia.mob_ai.core.AIController;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class IdleAction implements IAction {

    @Override
    public void start(LivingEntityPatch<?> entity, AIController controller) {

    }

    @Override
    public void tick(LivingEntityPatch<?> entity, AIController controller) {

    }

    @Override
    public void stop(LivingEntityPatch<?> entity, AIController controller) {

    }

    @Override
    public boolean continuous() {
        return true;
    }

    @Override
    public boolean interruptible(LivingEntityPatch<?> entity, AIController controller) {
        return true;
    }
}
