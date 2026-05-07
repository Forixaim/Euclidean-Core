package net.forixaim.euclidean_core;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(EuclideanCore.MOD_ID)
public class EuclideanCore
{
    public static final String MOD_ID = "euclidean_core";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EuclideanCore(IEventBus modEventBus, ModContainer modContainer)
    {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
