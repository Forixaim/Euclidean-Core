package net.forixaim.euclidia;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Euclidia.MOD_ID)
public class Euclidia
{
    public static final String MOD_ID = "euclidia";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Euclidia(IEventBus modEventBus, ModContainer modContainer)
    {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
