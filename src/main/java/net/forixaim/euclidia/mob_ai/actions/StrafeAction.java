package net.forixaim.euclidia.mob_ai.actions;

import net.forixaim.euclidia.mob_ai.core.AIController;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.function.Predicate;

public class StrafeAction extends AbstractTimedAction {
    private final boolean circleLeft;

    public StrafeAction(int duration, boolean circleLeft) {
        super(duration);
        this.circleLeft = circleLeft;
    }

    public StrafeAction(int duration, Predicate<LivingEntityPatch<?>> condition, boolean circleLeft) {
        super(duration, condition);
        this.circleLeft = circleLeft;
    }


    @Override
    public void start(LivingEntityPatch<?> entity, AIController controller) {

    }

    @Override
    public void tick(LivingEntityPatch<?> entity, AIController controller) {
        // 1. Life Cycle Termination check
        if (controller.getTicksSinceActionStarted() >= this.duration) {
            controller.stopAction();
            return;
        }

        LivingEntity boss = entity.getOriginal();
        LivingEntity target = entity.getTarget();
        if (target == null || !target.isAlive()) {
            controller.stopAction();
            return;
        }

        Vec3 toTarget = target.position().subtract(boss.position());
        Vec3 directionToTarget = toTarget.normalize();
        
        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 lateralVector = directionToTarget.cross(worldUp).normalize();
        
        if (!circleLeft) {
            lateralVector = lateralVector.scale(-1);
        }

        float strafeSpeedMultiplier = 0.22f;
        boss.setDeltaMovement(lateralVector.scale(strafeSpeedMultiplier));

        boss.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
    }

    @Override
    public void stop(LivingEntityPatch<?> entity, AIController controller) {

    }
}