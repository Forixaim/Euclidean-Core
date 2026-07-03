package net.forixaim.euclidia.mob_ai.actions;

import net.forixaim.euclidia.mob_ai.core.AIController;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

public class ComboAttackAction implements IAction {
    private final List<IAction> subActions;
    private int index = 0;
    private int timeoutTicks = 0;
    public ComboAttackAction(AttackAction... animations) {
        this.subActions = List.of(animations);
    }

    @Override
    public boolean continuous() {
        return false;
    }

    @Override
    public boolean isAttack()
    {
        return true;
    }

    @Override
    public void start(LivingEntityPatch<?> entity, AIController controller) {
        timeoutTicks = 20;
        index = 0;
        advance(entity, controller);
    }

    @Override
    public void tick(LivingEntityPatch<?> entity, AIController controller) {
        if (entity.getEntityState().canBasicAttack())
        {
            if (timeoutTicks > 0) {
                timeoutTicks--;
                if (timeoutTicks <= 0)
                {
                    controller.stopAction();
                }
            }
        }
    }

    public void advance(LivingEntityPatch<?> entity, AIController controller) {
        if (index < subActions.size()) {
            subActions.get(index).start(entity, controller);
            index++;
        }
        else {
            controller.stopAction();
        }
    }

    @Override
    public void stop(LivingEntityPatch<?> entity, AIController controller) {
        this.index = 0;
    }

    @Override
    public boolean interruptible(LivingEntityPatch<?> entity, AIController controller) {
        return entity.getEntityState().canBasicAttack();
    }
}
