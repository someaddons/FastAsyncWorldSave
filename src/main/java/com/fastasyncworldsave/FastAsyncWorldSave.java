package com.fastasyncworldsave;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class FastAsyncWorldSave implements ModInitializer
{

    public static final String MOD_ID = "fastasyncworldsave";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    //private static CupboardConfig<CommonConfiguration> config = new CupboardConfig<>(MOD_ID, new CommonConfiguration());
    public static       Random rand   = new Random();

    public static ExecutorService threadPool = Executors.newSingleThreadExecutor(new ThreadFactory()
    {
        @Override
        public Thread newThread(final Runnable r)
        {
            Thread thread = new Thread(r);
            thread.setName(MOD_ID);
            thread.setDaemon(true);
            return thread;
        }
    });

    @Override
    public void onInitialize()
    {

    }
}
