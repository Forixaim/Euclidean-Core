package net.forixaim.euclidia.mob_ai.core;

import net.minecraft.world.entity.projectile.Projectile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectileManager
{
    public static final Map<Projectile, Set<AIController>> PROJECTILES = new ConcurrentHashMap<>();
    private static final Map<AIController, Set<Projectile>> REVERSE_LOOKUP = new ConcurrentHashMap<>();

    public static void registerProjectile(Projectile projectile, AIController... controllers) {
        if (controllers == null || controllers.length == 0) return;

        PROJECTILES.computeIfAbsent(projectile, k -> ConcurrentHashMap.newKeySet())
                .addAll(Arrays.asList(controllers));

        for (AIController controller : controllers) {
            if (controller != null) {
                REVERSE_LOOKUP.computeIfAbsent(controller, k -> ConcurrentHashMap.newKeySet())
                        .add(projectile);
            }
        }
    }

    public static Set<Projectile> getTrackingProjectiles(AIController controller) {
        Set<Projectile> projectiles = REVERSE_LOOKUP.get(controller);
        return projectiles != null ? projectiles : Collections.emptySet();
    }

    public static void requestRemoval(AIController controller) {
        PROJECTILES.entrySet().removeIf(entry -> {
            Set<AIController> controllers = entry.getValue();
            controllers.remove(controller);
            return controllers.isEmpty();
        });
    }
}
