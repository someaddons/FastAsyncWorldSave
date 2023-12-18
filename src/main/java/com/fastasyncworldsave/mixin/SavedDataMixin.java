package com.fastasyncworldsave.mixin;

import com.fastasyncworldsave.FastAsyncWorldSave;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.saveddata.SavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.io.IOException;

@Mixin(SavedData.class)
public class SavedDataMixin
{
    @Redirect(method = "save(Ljava/io/File;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/io/File;)V"))
    private void fastasyncworldsave$saveOffthread(final CompoundTag compoundtag, final File file)
    {
        Util.ioPool().submit(() -> {
            try
            {
                NbtIo.writeCompressed(compoundtag, file);
            }
            catch (IOException ioexception)
            {
                FastAsyncWorldSave.LOGGER.error("Could not save data " + compoundtag.toString(), this, ioexception);
            }
        });
    }
}
