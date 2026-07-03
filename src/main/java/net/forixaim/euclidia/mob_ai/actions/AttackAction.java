package net.forixaim.euclidia.mob_ai.actions;

import net.forixaim.euclidia.mob_ai.core.AIController;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class AttackAction extends PlayAnimationAction implements IRangedAction {
    private float reach;
    private boolean playing = false;

    private Vec3 initialPosition;

    public AttackAction(AnimationManager.AnimationAccessor<? extends AttackAnimation> attackAnimation, float initialReach) {
        super(attackAnimation);
        this.duration = -1;
        this.reach = initialReach;
    }

    @Override
    public boolean isAttack()
    {
        return true;
    }


    @Override
    public void initActions()
    {
        if (animation.get() instanceof AttackAnimation attackAnimation)
        {
            duration = (int) Math.min((attackAnimation.phases[attackAnimation.phases.length - 1].recovery * 20), attackAnimation.getTotalTime() * 20);
        }
    }

    @Override
    public void start(LivingEntityPatch<?> entity, AIController controller) {
        super.start(entity, controller);
        this.playing = true;
        this.initialPosition = entity.getOriginal().position();
    }

    public void adjustReach(Vec3 hitPosition) {
        this.reach = (float) Math.max(initialPosition.distanceTo(hitPosition), 0);
    }

    @Override
    public void stop(LivingEntityPatch<?> entity, AIController controller) {
        super.stop(entity, controller);
        this.playing = false;
    }

    @Override
    public void tick(LivingEntityPatch<?> entity, AIController controller) {
        if (entity.getEntityState().canBasicAttack()) {
            controller.stopAction();
        }
    }

    @Override
    public boolean interruptible(LivingEntityPatch<?> entity, AIController controller) {
        return entity.getEntityState().canBasicAttack();
    }

    @Override
    public float range()
    {
        return reach;
    }
}
