package net.forixaim.euclidean_core.math;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EuclideanMath
{
    public static boolean isEuclideanPathClear(Level level, Node a, Node b, Mob entity) {
        Vec3 start = new Vec3(a.x + 0.5, a.y, a.z + 0.5);
        Vec3 end = new Vec3(b.x + 0.5, b.y, b.z + 0.5);
        BlockHitResult result = level.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        return result.getType() == HitResult.Type.MISS;
    }
}
