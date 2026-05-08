package net.forixaim.euclidean_core.utilities;

import net.forixaim.euclidean_core.EuclideanCore;
import net.forixaim.euclidean_core.mixin.PathAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static net.forixaim.euclidean_core.math.EuclideanMath.isEuclideanPathClear;

public class PathfindingUtilities
{
    public static Path smooth(Level level, Path path, Mob entity) {
        if (path instanceof PathAccessor accessor) {
            List<Node> nodes = new ArrayList<>(accessor.getNodes());
            if (nodes.size() < 3) return path;
            List<Node> smoothed = new ArrayList<>();
            smoothed.add(nodes.getFirst());
            int current = 0;
            while (current < nodes.size() - 1) {
                int furthestVisible = current + 1;
                for (int next = nodes.size() - 1; next > current + 1; next--) {
                    List<Node> segmentToSkip = nodes.subList(current, next + 1);
                    if (isEuclideanPathSafe(level, nodes.get(current), nodes.get(next), entity, segmentToSkip)) {
                        furthestVisible = next;
                        break;
                    }
                }
                if (!hasClearance(level, nodes.get(current), nodes.get(furthestVisible), entity)) {
                    shiftCornerPivot(smoothed, nodes.get(furthestVisible), level);
                }
                else {
                    smoothed.add(nodes.get(furthestVisible));
                }
                current = furthestVisible;
            }
            return new Path(smoothed, path.getTarget(), path.canReach());
        }
        EuclideanCore.LOGGER.warn("Path is not an instance of PathAccessor, returning original path");
        return path;
    }

    public static boolean isEuclideanPathSafe(Level level, Node start, Node end, Mob entity, List<Node> originalSegment) {
        if (!isEuclideanPathClear(level, start, end, entity)) return false;
        if (!isFloorSafe(level, start, end, entity)) return false;

        Vec3 startVec = new Vec3(start.x, start.y, start.z);
        Vec3 endVec = new Vec3(end.x, end.y, end.z);

        if (!avoidImpassibles(level, startVec, endVec, entity)) return false;

        return prioritizeLowerCost(originalSegment, startVec, endVec, entity);
    }

    public static boolean hasClearance(Level level, Node start, Node end, Mob entity) {
        Vec3 startVec = new Vec3(start.x + 0.5, start.y, start.z + 0.5);
        Vec3 endVec = new Vec3(end.x + 0.5, end.y, end.z + 0.5);

        float radius = (entity.getBbWidth() / 2.0f) + 0.1f;

        AABB pathBox = new AABB(startVec, endVec).inflate(radius, 0.0, radius);

        return level.noCollision(entity, pathBox);
    }

    private static boolean isFloorSafe(Level level, Node start, Node end, Mob entity) {
        Vec3 startPos = new Vec3(start.x + 0.5, start.y, start.z + 0.5);
        Vec3 endPos = new Vec3(end.x + 0.5, end.y, end.z + 0.5);
        Vec3 direction = endPos.subtract(startPos);
        double distance = direction.length();
        direction = direction.normalize();

        for (double i = 0.5; i < distance; i += 0.5) {
            Vec3 checkPoint = startPos.add(direction.scale(i));
            BlockPos floorPos = BlockPos.containing(checkPoint.x, checkPoint.y - 1.0, checkPoint.z);
            if (level.getBlockState(floorPos).isAir()) {
                return false;
            }

            BlockPos headPos = BlockPos.containing(checkPoint.x, checkPoint.y + 1.0, checkPoint.z);
            if (!level.getBlockState(headPos).isAir() && level.getBlockState(headPos).canOcclude()) {
                return false;
            }
        }
        return true;
    }



    public static boolean avoidImpassibles(Level level, Vec3 start, Vec3 end, Mob entity) {
        double distance = start.distanceTo(end);
        Vec3 direction = end.subtract(start).normalize();

        for (double i = 0; i < distance; i += 0.5) {
            Vec3 point = start.add(direction.scale(i));
            BlockPos pos = BlockPos.containing(point.x, point.y, point.z);

            if (!level.getBlockState(pos).isAir() && level.getBlockState(pos).canOcclude()) {
                return false;
            }

            PathType type = WalkNodeEvaluator.getPathTypeStatic(entity, pos);
            float malus = entity.getPathfindingMalus(type);

            if (malus < 0.0f) {
                return false;
            }
        }
        return true;
    }

    private static void shiftCornerPivot(List<Node> smoothed, Node cornerNode, Level level) {
        BlockPos cornerPos = new BlockPos(cornerNode.x, cornerNode.y, cornerNode.z);

        double targetX = cornerNode.x + 0.5;
        double targetZ = cornerNode.z + 0.5;

        if (level.getBlockState(cornerPos.west()).canOcclude()) targetX += 0.3;
        if (level.getBlockState(cornerPos.east()).canOcclude()) targetX -= 0.3;
        if (level.getBlockState(cornerPos.north()).canOcclude()) targetZ += 0.3;
        if (level.getBlockState(cornerPos.south()).canOcclude()) targetZ -= 0.3;

        Node shiftedNode = new Node((int)Math.floor(targetX), cornerNode.y, (int)Math.floor(targetZ));

        smoothed.add(shiftedNode);
    }

    public static boolean prioritizeLowerCost(List<Node> originalSegment, Vec3 start, Vec3 end, Mob entity) {
        float originalCost = 0;
        for (Node node : originalSegment) {
            originalCost += entity.getPathfindingMalus(
                    WalkNodeEvaluator.getPathTypeStatic(entity, node.asBlockPos())
            );
        }

        float euclideanCost = 0;
        double distance = start.distanceTo(end);
        Vec3 direction = end.subtract(start).normalize();

        for (double i = 0; i < distance; i += 0.5) {
            BlockPos pos = BlockPos.containing(start.add(direction.scale(i)));
            euclideanCost += entity.getPathfindingMalus(
                    WalkNodeEvaluator.getPathTypeStatic(entity, pos)
            );
        }

        return euclideanCost <= (originalCost * 1.1f);
    }
}
