package net.forixaim.euclidia.mob_ai.actions;

import net.forixaim.euclidia.mob_ai.core.AIController;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;

import javax.annotation.Nullable;

public class GuardAction implements IAction, DefensiveAction
{
    private final int duration;

    public GuardAction(int duration)
    {
        this.duration = duration;
    }

    @Override
    public void start(LivingEntityPatch<?> entity, AIController controller) {

    }

    @Override
    public void tick(LivingEntityPatch<?> entity, AIController controller) {
        if (controller.getTicksSinceActionStarted() >= duration)
        {
            controller.stopAction();
        }
    }

    @Nullable
    public AttackResult processAttack(LivingEntityPatch<?> entity, DamageSource source, float amount)
    {
        if (source.is(EpicFightDamageTypeTags.UNBLOCKALBE) || source.is(EpicFightDamageTypeTags.BYPASS_DODGE))
        {
            return null;
        }
        if (source instanceof EpicFightDamageSource damageSource)
        {
            float staminaDamage = entity.getHoldingItemCapability(InteractionHand.OFF_HAND).isWeaponCategory(CapabilityItem.WeaponCategories.SHIELD) ? 0.8f : 0.5f;
            if (EpicFightCapabilities.getEntityPatch(source.getEntity(), LivingEntityPatch.class) instanceof ServerPlayerPatch serverPlayerPatch)
            {
                float impact = damageSource.calculateImpact();
                staminaDamage *= impact;
                serverPlayerPatch.getModifiedStaminaConsume(staminaDamage);
            }
        }
        return AttackResult.blocked(amount);
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
        return false;
    }
}
