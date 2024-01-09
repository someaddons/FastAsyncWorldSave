package com.fastasyncworldsave;

import com.fastasyncworldsave.event.EventHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

import static com.fastasyncworldsave.FastAsyncWorldSave.MOD_ID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MOD_ID)
public class FastAsyncWorldSave
{
    public static final String                              MOD_ID = "fastasyncworldsave";
    public static final Logger                              LOGGER = LogManager.getLogger();
    public static       Random                              rand   = new Random();

    public FastAsyncWorldSave()
    {
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(EventHandler.class);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event)
    {
        // Side safe client event handler
        FastAsyncWorldSaveClient.onInitializeClient(event);
    }
}
