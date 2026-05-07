package net.forixaim.euclidean_core.utilities;

import net.forixaim.euclidean_core.EuclideanCore;
import net.forixaim.euclidean_core.mixin.PathAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

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
            while (current < nodes.size() - 1)
            {
                int furthestVisible = current + 1;
                for (int next = nodes.size() - 1; next > current + 1; next--) {
                    if (isEuclideanPathClear(level, nodes.get(current), nodes.get(next), entity)) {
                        furthestVisible = next;
                        break;
                    } else if (next == current + 2) {
                        injectCornerPivot(smoothed, nodes.get(next - 1));
                    }
                }
                smoothed.add(nodes.get(furthestVisible));
                current = furthestVisible;
            }
            return new Path(smoothed, path.getTarget(), path.canReach());
        }
        EuclideanCore.LOGGER.warn("Path is not an instance of PathAccessor, returning original path");
        return path;
    }

    private static void injectCornerPivot(List<Node> smoothed, Node cornerNode) {
        Node pivot = new Node(cornerNode.x, cornerNode.y, cornerNode.z);
        smoothed.add(pivot);
    }
}
