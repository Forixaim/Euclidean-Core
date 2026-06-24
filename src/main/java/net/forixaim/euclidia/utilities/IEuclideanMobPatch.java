package net.forixaim.euclidia.utilities;

import net.forixaim.euclidia.mob_ai.core.AIController;
import net.forixaim.euclidia.mob_ai.core.BossBrain;

/**
 * Marker interface for entities that should use the Euclidean pathfinding post-processor.
 * <p>
 * <b>WARNING:</b> Implementing this interface is highly computationally expensive.
 * Running this logic on swarm mobs will <b>heavily impact the server's TPS</b>,
 * especially on systems <b>without a dedicated graphics card</b>.
 * <p>
 * It is recommended to use this sparingly and only for high-impact entities like Bosses
 * or Elites. While optimized for CPU, it is designed for future offloading to the GPU.
 */
public interface IEuclideanMobPatch {
    boolean waiting();
    AIController controller();

    void onHit();
}
