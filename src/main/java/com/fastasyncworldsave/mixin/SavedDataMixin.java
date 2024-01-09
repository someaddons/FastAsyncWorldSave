package com.fastasyncworldsave.mixin;

import com.fastasyncworldsave.FastAsyncWorldSave;
import com.fastasyncworldsave.ISaveData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.saveddata.SavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.io.IOException;

@Mixin(SavedData.class)
public class SavedDataMixin implements ISaveData
{
    @Unique
    private CompoundTag toSave = null;

    @Override
    public void setToSave(final CompoundTag tag)
    {
        toSave = tag;
    }

    @Redirect(method = "save(Ljava/io/File;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/SavedData;save(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag skipSer(final SavedData instance, final CompoundTag compoundTag)
    {
        if (toSave != null)
        {
            return compoundTag;
        }
        else
        {
            return instance.save(compoundTag);
        }
    }

    @Redirect(method = "save(Ljava/io/File;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/io/File;)V"))
    private void doSave(final CompoundTag compoundTag, final File file)
    {
        if (toSave != null)
        {
            try
            {
                NbtIo.writeCompressed(toSave, file);
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
                NbtIo.writeCompressed(compoundTag, file);
            }
            catch (IOException var4)
            {
                FastAsyncWorldSave.LOGGER.error("Could not save data {}", this, var4);
            }
        }
    }
}
