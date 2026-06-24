package net.forixaim.euclidia.events;

import net.forixaim.euclidia.Euclidia;
import net.forixaim.euclidia.utilities.IEuclideanMobPatch;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@EventBusSubscriber(modid = Euclidia.MOD_ID)
public class GameEvents {
    @SubscribeEvent
    public static void onHurt(LivingDamageEvent.Post event)
    {
        if (EpicFightCapabilities.getEntityPatch(event.getSource().getEntity(), LivingEntityPatch.class) instanceof IEuclideanMobPatch iEuclideanMobPatch)
        {
            iEuclideanMobPatch.onHit();
        }
    }
}
