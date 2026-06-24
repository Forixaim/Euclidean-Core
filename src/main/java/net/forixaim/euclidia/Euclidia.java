package net.forixaim.euclidia;

import com.mojang.logging.LogUtils;
import net.forixaim.euclidia.events.EFEvents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import yesman.epicfight.api.event.EpicFightEventHooks;

@Mod(Euclidia.MOD_ID)
public class Euclidia
{
    public static final String MOD_ID = "euclidia";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation identifier(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(Euclidia.MOD_ID, path);
    }

    public Euclidia(IEventBus modEventBus, ModContainer modContainer)
    {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(this::registerEvents);
    }

    private void registerEvents()
    {
        EpicFightEventHooks.Animation.BEGIN.registerEvent(EFEvents::onAnimationStart);
        EpicFightEventHooks.Animation.END.registerEvent(EFEvents::onAnimationEnd);
    }
}
