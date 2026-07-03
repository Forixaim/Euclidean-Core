package net.forixaim.euclidia.events;

import net.forixaim.euclidia.Euclidia;
import net.forixaim.euclidia.mob_ai.core.AIController;
import net.forixaim.euclidia.mob_ai.core.ProjectileManager;
import net.forixaim.euclidia.registry.EuclidiaRegistries;
import net.forixaim.euclidia.utilities.IEuclideanMobPatch;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = Euclidia.MOD_ID)
public class GameEvents
{
    @SubscribeEvent
    public static void onEntityJoinLevelEvent(EntityJoinLevelEvent event)
    {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof Projectile projectile) {
            AABB aabb = projectile.getBoundingBox().inflate(45.0);
            List<Mob> nearbyMobs = event.getLevel().getEntitiesOfClass(Mob.class, aabb);
            List<AIController> targetControllers = new ArrayList<>();
            for (Mob mob : nearbyMobs) {
                var patch = EpicFightCapabilities.getEntityPatch(mob, EntityPatch.class);
                if (patch instanceof IEuclideanMobPatch euclideanPatch) {
                    AIController controller = euclideanPatch.controller();
                    if (controller != null) {
                        targetControllers.add(controller);
                    }
                }
            }

            if (!targetControllers.isEmpty()) {
                ProjectileManager.registerProjectile(
                        projectile,
                        targetControllers.toArray(new AIController[0])
                );
            }
        }
    }

    @SubscribeEvent
    public static void onRecipeFinalize(RecipesUpdatedEvent event)
    {
        EuclidiaRegistries.ACTIONS.holders().forEach(holder -> holder.value().initActions());
    }
}
