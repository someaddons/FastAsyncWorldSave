package com.fastasyncworldsave.mixin;

import com.fastasyncworldsave.FastAsyncWorldSave;
import com.fastasyncworldsave.ISaveData;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(DimensionDataStorage.class)
public abstract class DimensionDataStorageMixin
{
    @Shadow
    protected abstract File getDataFile(final String string);

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
    private void fastasyncworldsave$saveOffthread(final Map<String, SavedData> instance, final BiConsumer<String, SavedData> entry)
    {
        instance.forEach((string, savedData) ->
        {
            if (savedData != null)
            {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.put("data", savedData.save(new CompoundTag()));
                NbtUtils.addCurrentDataVersion(compoundTag);
                ((ISaveData) savedData).setToSave(compoundTag);

                Util.ioPool().submit(() -> {
                    try
                    {
                        savedData.save(getDataFile(string));
                    }
                    catch (Exception e)
                    {
                        FastAsyncWorldSave.LOGGER.error("Could not save data " + compoundTag.toString(), this, e);
                    }
                });
            }
        });
    }
}
