package net.forixaim.euclidia.registry;

import net.forixaim.euclidia.mob_ai.actions.IAction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MatrixWeights {
    private static final Map<ResourceLocation, float[]> MATRICES = new ConcurrentHashMap<>();

    public static void register(ResourceLocation location, float[] weights)
    {
        MATRICES.put(location, weights);
    }

    /**
     * Retrieves the raw 31-column array for dot product multiplication.
     */
    public static float[] getRow(Holder<IAction> actionClass) {
        return MATRICES.get(ResourceLocation.parse(actionClass.getRegisteredName()));
    }
}
