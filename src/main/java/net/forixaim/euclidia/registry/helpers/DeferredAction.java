package net.forixaim.euclidia.registry.helpers;

import net.forixaim.euclidia.mob_ai.actions.IAction;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

public class DeferredAction<T extends IAction> extends DeferredHolder<IAction, T> {
    public DeferredAction(ResourceKey<IAction> key) {
        super(key);
    }
}
