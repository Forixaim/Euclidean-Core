package net.forixaim.euclidia.mob_ai.actions;

import net.forixaim.euclidia.mob_ai.core.AIController;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class FaceOpponentAction implements IAction
{

    @Override
    public void start(LivingEntityPatch<?> entity, AIController controller)
    {
        Entity target = controller.getBossBrain().selectPrimaryTarget(controller.getBossBrain().getBossBias());
        entity.rotateTo(target, 360f, false);
    }

    @Override
    public void tick(LivingEntityPatch<?> entity, AIController controller)
    {

    }

    @Override
    public void stop(LivingEntityPatch<?> entity, AIController controller)
    {

    }

    @Override
    public boolean continuous()
    {
        return false;
    }

    @Override
    public boolean interruptible(LivingEntityPatch<?> entity, AIController controller)
    {
        return true;
    }
}
