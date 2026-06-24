package net.forixaim.euclidia.registry.helpers;

import net.forixaim.euclidia.mob_ai.actions.IAction;
import net.forixaim.euclidia.registry.EuclidiaRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ActionRegister extends DeferredRegister<IAction> {
    protected ActionRegister(ResourceKey<? extends Registry<IAction>> registryKey, String namespace) {
        super(registryKey, namespace);
    }

    public static ActionRegister create(String namespace) {
        return new ActionRegister(EuclidiaRegistries.Keys.ACTIONS, namespace);
    }

    public DeferredAction<IAction> registerAnimation(ResourceKey<IAction> key) {
        return new DeferredAction<>(key);
    }
}
