package com.fastasyncworldsave;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class FastAsyncWorldSave implements ModInitializer
{

    public static final String MOD_ID = "fastasyncworldsave";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    //private static CupboardConfig<CommonConfiguration> config = new CupboardConfig<>(MOD_ID, new CommonConfiguration());
    public static       Random rand   = new Random();

    @Override
    public void onInitialize()
    {
    }

    public static ResourceLocation id(String name)
    {
        return new ResourceLocation(MOD_ID, name);
    }
}
