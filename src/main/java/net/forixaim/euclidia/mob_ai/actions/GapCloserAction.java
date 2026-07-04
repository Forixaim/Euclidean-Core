package net.forixaim.euclidia.mob_ai.actions;

import net.forixaim.euclidia.mob_ai.core.AIController;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
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
    public boolean isGapCloser()
    {
        return true;
    }

    @Override
    public void start(LivingEntityPatch<?> entityPatch, AIController controller) {
        this.target = controller.getBossBrain().selectPrimaryTarget(controller.getBossBrain().getBossBias());
        if (entityPatch.getOriginal() instanceof PathfinderMob finder)
        {
            finder.getNavigation().moveTo(target, 1.0 /* currently a dummy value to test */);
        }
    }

    @Override
    public void tick(LivingEntityPatch<?> entityPatch, AIController controller) {
        if (target == null || !target.isAlive() || target.isRemoved()) {
            controller.stopAction();
            return;
        }

        if (entityPatch.getOriginal().distanceToSqr(target) < Mth.square(stopDistance)) {
            controller.stopAction();
            return;
        }

        if (entityPatch.getOriginal() instanceof PathfinderMob finder)
        {
            if (finder.hasLineOfSight(target))
            {
                Vec3 targetPos = target.position();
                Vec3 bossPos = finder.position();

                Vec3 direction = new Vec3(targetPos.x - bossPos.x, 0, targetPos.z - bossPos.z).normalize();

                double dashSpeedCoefficient = 0.35D;
                Vec3 currentMovement = finder.getDeltaMovement();

                finder.setDeltaMovement(
                        direction.x * dashSpeedCoefficient,
                        currentMovement.y,
                        direction.z * dashSpeedCoefficient
                );

                entityPatch.rotateTo(target, 360f, false);
            } else
            {
                if (finder.tickCount % 5 == 0)
                {
                    finder.getNavigation().moveTo(target, 1.2D);
                }
            }
        }
    }

    @Override
    public void stop(LivingEntityPatch<?> entityPatch, AIController controller) {
        if (entityPatch.getOriginal() instanceof PathfinderMob finder)
        {
            finder.getNavigation().stop();
        }
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