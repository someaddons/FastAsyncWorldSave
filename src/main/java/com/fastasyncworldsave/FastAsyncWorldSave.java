package com.fastasyncworldsave;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static com.fastasyncworldsave.FastAsyncWorldSave.MOD_ID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MOD_ID)
public class FastAsyncWorldSave
{
    public static final String MOD_ID = "fastasyncworldsave";
    public static final Logger LOGGER = LogManager.getLogger();
    //private static      CupboardConfig<CommonConfiguration> config = new CupboardConfig<>(MOD_ID, new CommonConfiguration());
    public static       Random rand   = new Random();

    public static ExecutorService threadPool = Executors.newSingleThreadExecutor(new ThreadFactory()
    {
        @Override
        public Thread newThread(final Runnable r)
        {
            Thread thread = new Thread(r);
            thread.setName(MOD_ID);
            return thread;
        }
    });

    public FastAsyncWorldSave(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::clientSetup);
        NeoForge.EVENT_BUS.addListener(this::onShutdown);
    }

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event)
    {
        // Side safe client event handler
        FastAsyncWorldSaveClient.onInitializeClient(event);
    }

    @SubscribeEvent
    public void onShutdown(ServerStoppedEvent event)
    {
        if (event.getServer().isDedicatedServer())
        {
            threadPool.shutdown();
        }
    }
}
