package net.forixaim.euclidia.mob_ai.actions;

import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public interface IAction
{
    void start(LivingEntityPatch<?> entity);

    void tick(LivingEntityPatch<?> entity);

    void stop(LivingEntityPatch<?> entity
    );
}
