package com.fastasyncworldsave;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

import static com.fastasyncworldsave.FastAsyncWorldSave.MOD_ID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MOD_ID)
public class FastAsyncWorldSave
{
    public static final String MOD_ID = "fastasyncworldsave";
    public static final Logger LOGGER = LogManager.getLogger();
    //private static      CupboardConfig<CommonConfiguration> config = new CupboardConfig<>(MOD_ID, new CommonConfiguration());
    public static       Random rand   = new Random();

    public FastAsyncWorldSave(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::clientSetup);
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event)
    {
        // Side safe client event handler
        FastAsyncWorldSaveClient.onInitializeClient(event);
    }
}
