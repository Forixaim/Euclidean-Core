package net.forixaim.euclidia.mob_ai.actions;

import net.forixaim.euclidia.mob_ai.core.AIController;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PlayAnimationAction implements IAction
{
    private final AnimationManager.AnimationAccessor<? extends StaticAnimation> animation;
    protected int duration;

    public PlayAnimationAction(AnimationManager.AnimationAccessor<? extends StaticAnimation> attackAnimation) {
        this.animation = attackAnimation;
        duration = (int) (attackAnimation.get().getTotalTime() * 20);
    }

    @Override
    public void start(LivingEntityPatch<?> entity, AIController controller) {
        entity.playAnimationSynchronized(animation, 0);
    }

    @Override
    public void tick(LivingEntityPatch<?> entity, AIController controller) {
        if (controller.getTicksSinceActionStarted() <= duration)
        {
            controller.stopAction();
        }
    }

    public AnimationManager.AnimationAccessor<? extends StaticAnimation> getAnimationAccessor()
    {
        return animation;
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
        return animation.get().isMainFrameAnimation();
    }
}
