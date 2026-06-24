package net.forixaim.euclidia.entity_patch;

import net.forixaim.euclidia.mob_ai.actions.GuardAction;
import net.forixaim.euclidia.utilities.IEuclideanMobPatch;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

public abstract class EuclidiaHumanoidMobPatch<T extends PathfinderMob> extends HumanoidMobPatch<T> implements IEuclideanMobPatch
{
    public EuclidiaHumanoidMobPatch(T original, Faction faction)
    {
        super(original, faction);
    }

    @Override
    public AttackResult tryHarm(Entity target, EpicFightDamageSource damagesource, float amount) {
        AttackResult result = super.tryHarm(target, damagesource, amount);
        controller().handleAttack(result, target, damagesource, amount);
        return result;
    }

    @Override
    public AttackResult tryHurt(DamageSource damageSource, float amount) {
        AttackResult originalResult = super.tryHurt(damageSource, amount);
        if (controller().getActiveAction() instanceof GuardAction guardAction)
        {
            AttackResult result = guardAction.processAttack(this, damageSource, amount);
            originalResult = result != null ? result : originalResult;
        }
        controller().handleOnHit(originalResult, damageSource, amount);
        return originalResult;
    }

    @Override
    public void preTickServer() {
        super.preTickServer();
        this.controller().update();
    }
}
