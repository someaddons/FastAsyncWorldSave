package com.fastasyncworldsave.mixin;

import com.fastasyncworldsave.FastAsyncWorldSave;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.Util;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.io.IOException;

@Mixin(WorldSavedData.class)
public class SavedDataMixin
{
    @Redirect(method = "save(Ljava/io/File;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompressedStreamTools;writeCompressed(Lnet/minecraft/nbt/CompoundNBT;Ljava/io/File;)V"))
    private void fastasyncworldsave$saveOffthread(final CompoundNBT compoundtag, final File file)
    {
        Util.ioPool().execute(() -> {
            try
            {
                CompressedStreamTools.writeCompressed(compoundtag, file);
            }
            catch (IOException ioexception)
            {
                FastAsyncWorldSave.LOGGER.error("Could not save data " + compoundtag.toString(), this, ioexception);
            }
        });
    }
}
