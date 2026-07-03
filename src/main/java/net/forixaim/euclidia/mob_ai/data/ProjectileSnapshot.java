package net.forixaim.euclidia.mob_ai.data;

import net.forixaim.euclidia.mob_ai.core.AIController;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record ProjectileSnapshot(Vec3 position, Vec3 velocity, int ownerId)
{
    /**
     * Projects the projectile's vector forward over a short timeframe
     * to check if its path intersects the boss's bounding box.
     */
    public boolean willHitBoss(AIController controller) {
        var boss = controller.getOriginal().getOriginal();
        if (!boss.isAlive()) return false;
        AABB bossBounds = boss.getBoundingBox();
        Vec3 bossCenter = bossBounds.getCenter();
        Vec3 projToBoss = bossCenter.subtract(this.position);
        double speed = this.velocity.lengthSqr();
        if (speed < 0.001) return false;
        Vec3 projDir = this.velocity.scale(1.0 / speed);
        double distanceAlongRay = projToBoss.dot(projDir);
        if (distanceAlongRay < 0) return false;
        if (distanceAlongRay > 30.0) return false;
        Vec3 closestPointOnLine = this.position.add(projDir.scale(distanceAlongRay));
        AABB trackingTarget = bossBounds.inflate(0.25);
        return trackingTarget.contains(closestPointOnLine);
    }

    public float getThreat(AIController controller)
    {
        if (!this.willHitBoss(controller)) {
            return 0.0f;
        }
        var boss = controller.getOriginal().getOriginal();
        if (!boss.isAlive()) return 0.0f;
        Vec3 bossCenter = boss.getBoundingBox().getCenter();
        double distance = this.position.distanceTo(bossCenter);
        if (distance < 0.1) return 1.0f;
        double speed = this.velocity.length();
        if (speed < 0.05) return 0.0f;
        double timeToImpactTicks = distance / speed;
        double threatFactor;
        if (timeToImpactTicks <= 5.0) {
            threatFactor = 1.0;
        } else if (timeToImpactTicks > 40.0) {
            threatFactor = 0.0;
        } else {
            threatFactor = 1.0 - ((timeToImpactTicks - 5.0) / (40.0 - 5.0));
        }
        if (ownerId != -1) {
            if (this.ownerId == controller.getOriginal().getTarget().getId()) threatFactor *= 1.2;
        }
        return (float) Math.clamp(threatFactor, 0.0, 1.0);
    }
}
