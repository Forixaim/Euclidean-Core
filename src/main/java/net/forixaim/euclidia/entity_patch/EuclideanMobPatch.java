package net.forixaim.euclidia.entity_patch;

import net.forixaim.euclidia.utilities.IEuclideanMobPatch;
import net.minecraft.world.entity.PathfinderMob;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public abstract class EuclideanMobPatch<T extends PathfinderMob> extends MobPatch<T> implements IEuclideanMobPatch
{
    public EuclideanMobPatch(T entity)
    {
        super(entity);
    }

    public EuclideanMobPatch(T entity, Faction faction)
    {
        super(entity, faction);
    }
}
