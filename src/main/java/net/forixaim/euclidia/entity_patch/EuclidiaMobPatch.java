package net.forixaim.euclidia.entity_patch;

import net.forixaim.euclidia.mob_ai.actions.GuardAction;
import net.forixaim.euclidia.utilities.IEuclideanMobPatch;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

public abstract class EuclidiaMobPatch<T extends PathfinderMob> extends MobPatch<T> implements IEuclideanMobPatch
{
    public EuclidiaMobPatch(T entity)
    {
        super(entity);
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

    public EuclidiaMobPatch(T entity, Faction faction)
    {
        super(entity, faction);
    }
}
