package net.forixaim.euclidia.entity_patch;

import net.forixaim.euclidia.utilities.IEuclideanMobPatch;
import net.minecraft.world.entity.PathfinderMob;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;

public abstract class EuclideanHumanoidMobPatch<T extends PathfinderMob> extends HumanoidMobPatch<T> implements IEuclideanMobPatch
{
    public EuclideanHumanoidMobPatch(T original, Faction faction)
    {
        super(original, faction);
    }
}
