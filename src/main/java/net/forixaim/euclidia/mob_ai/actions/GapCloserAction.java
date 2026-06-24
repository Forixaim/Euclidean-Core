package net.forixaim.euclidia.mob_ai.actions;

import net.forixaim.euclidia.mob_ai.core.AIController;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class GapCloserAction implements IAction {
    private final AnimationManager.AnimationAccessor<? extends StaticAnimation> dashAnimationId;
    private final double stopDistance;
    private LivingEntity target;

    public GapCloserAction(AnimationManager.AnimationAccessor<? extends StaticAnimation> dashAnimationId, double stopDistance) {
        this.dashAnimationId = dashAnimationId;
        this.stopDistance = stopDistance;
    }

    @Override
    public void start(LivingEntityPatch<?> entityPatch, AIController controller) {
        this.target = entityPatch.getTarget();
        entityPatch.playAnimationSynchronized(dashAnimationId, 0);
    }

    @Override
    public void tick(LivingEntityPatch<?> entityPatch, AIController controller) {
        if (target == null) return;

        LivingEntity boss = entityPatch.getOriginal();
        
        entityPatch.rotateTo(target, 0.0f, true);

        Vec3 dashDirection = target.position().subtract(boss.position()).normalize();
        if (boss.position().distanceTo(target.position()) > stopDistance) {
            boss.setDeltaMovement(dashDirection.x * 0.4, boss.getDeltaMovement().y, dashDirection.z * 0.4);
        }
    }

    @Override
    public void stop(LivingEntityPatch<?> entityPatch, AIController controller) {
        entityPatch.getOriginal().setDeltaMovement(0, entityPatch.getOriginal().getDeltaMovement().y, 0);
    }

    @Override
    public boolean continuous() {
        return true;
    }

    @Override
    public boolean interruptible(LivingEntityPatch<?> entityPatch, AIController controller) {
        return false;
    }
}