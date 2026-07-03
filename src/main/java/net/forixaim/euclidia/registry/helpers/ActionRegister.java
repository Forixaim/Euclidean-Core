package net.forixaim.euclidia.registry.helpers;

import net.forixaim.euclidia.Euclidia;
import net.forixaim.euclidia.mob_ai.actions.IAction;
import net.forixaim.euclidia.registry.EuclidiaRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ActionRegister extends DeferredRegister<IAction> {
    protected ActionRegister(ResourceKey<? extends Registry<IAction>> registryKey, String namespace) {
        super(registryKey, namespace);
    }

    public static ActionRegister create(String namespace) {
        return new ActionRegister(EuclidiaRegistries.Keys.ACTIONS, namespace);
    }

    public <T extends IAction> DeferredAction<T> registerAction(String name, Function<ResourceLocation, IAction> action)
    {
        this.register(name, action);
        ResourceKey<IAction> key = ResourceKey.create(EuclidiaRegistries.Keys.ACTIONS, ResourceLocation.fromNamespaceAndPath(this.getNamespace(), name));
        return new DeferredAction<>(key);
    }

    @Override
    public void register(IEventBus bus)
    {
        Euclidia.LOGGER.info("Registering actions");
        super.register(bus);
    }
}
