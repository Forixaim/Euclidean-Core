package net.forixaim.euclidia.mob_ai.actions;

import net.forixaim.euclidia.mob_ai.core.AIController;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class DodgeAction extends PlayAnimationAction implements EvasiveAction {
    public DodgeAction(AnimationManager.AnimationAccessor<? extends DodgeAnimation> dodgeAnimation) {
        super(dodgeAnimation);
    }

    @Override
    public boolean interruptible(LivingEntityPatch<?> entity, AIController controller) {
        return false;
    }
}
