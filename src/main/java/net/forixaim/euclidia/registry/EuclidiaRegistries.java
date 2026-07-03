package net.forixaim.euclidia.registry;

import net.forixaim.euclidia.Euclidia;
import net.forixaim.euclidia.mob_ai.actions.IAction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class EuclidiaRegistries
{
    public static final Registry<IAction> ACTIONS = new RegistryBuilder<>(Keys.ACTIONS).sync(true).create();
    public static class Keys {
        public static final ResourceKey<Registry<IAction>> ACTIONS = ResourceKey.createRegistryKey(Euclidia.identifier("actions"));
    }

    public static void onNewRegistry(NewRegistryEvent event)
    {
        event.register(ACTIONS);
    }
}
