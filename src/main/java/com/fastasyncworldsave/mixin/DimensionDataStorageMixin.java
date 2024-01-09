package com.fastasyncworldsave.mixin;

import com.fastasyncworldsave.FastAsyncWorldSave;
import com.fastasyncworldsave.ISaveData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;

@Mixin(DimensionSavedDataManager.class)
public abstract class DimensionDataStorageMixin
{
    @Shadow
    protected abstract File getDataFile(final String string);

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldSavedData;save(Ljava/io/File;)V"))
    private void fastasyncworldsave$saveOffthread(final WorldSavedData savedData, final File file)
    {
        if (savedData != null)
        {
            CompoundNBT compoundTag = new CompoundNBT();

            final CompoundNBT ser = savedData.save(new CompoundNBT());

            if (ser == null)
            {
                return;
            }

            compoundTag.put("data", ser);
            compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
            ((ISaveData) savedData).setToSave(compoundTag);

            Util.ioPool().execute(() -> {
                try
                {
                    savedData.save(getDataFile(savedData.getId()));
                }
                catch (Exception e)
                {
                    FastAsyncWorldSave.LOGGER.error("Could not save data " + compoundTag.toString(), this, e);
                }
            });
        }
    }
}
