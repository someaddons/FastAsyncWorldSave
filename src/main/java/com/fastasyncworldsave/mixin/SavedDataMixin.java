package com.fastasyncworldsave.mixin;

import com.fastasyncworldsave.FastAsyncWorldSave;
import com.fastasyncworldsave.ISaveData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.io.IOException;

@Mixin(WorldSavedData.class)
public abstract class SavedDataMixin implements ISaveData
{
    @Shadow
    public abstract CompoundNBT save(final CompoundNBT p_189551_1_);

    @Unique
    private CompoundNBT toSave = null;

    @Override
    public void setToSave(final CompoundNBT tag)
    {
        toSave = tag;
    }

    @Redirect(method = "save(Ljava/io/File;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldSavedData;save(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;"))
    private CompoundNBT skipSer(final WorldSavedData instance, final CompoundNBT compoundTag)
    {
        if (toSave != null)
        {
            return compoundTag;
        }
        else
        {
            return save(compoundTag);
        }
    }

    @Redirect(method = "save(Ljava/io/File;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompressedStreamTools;writeCompressed(Lnet/minecraft/nbt/CompoundNBT;Ljava/io/File;)V"))
    private void doSave(final CompoundNBT compoundTag, final File file)
    {
        if (toSave != null)
        {
            try
            {
                CompressedStreamTools.writeCompressed(toSave, file);
            }
            catch (IOException var4)
            {
                FastAsyncWorldSave.LOGGER.error("Could not save data {}", this, var4);
            }

            toSave = null;
        }
        else
        {
            try
            {
                CompressedStreamTools.writeCompressed(compoundTag, file);
            }
            catch (IOException var4)
            {
                FastAsyncWorldSave.LOGGER.error("Could not save data {}", this, var4);
            }
        }
    }
}
