package com.fastasyncworldsave.mixin;

import com.fastasyncworldsave.FastAsyncWorldSave;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public class LevelStorageSourceMixin
{
    @Shadow
    @Final
    public LevelStorageSource.LevelDirectory levelDirectory;

    @Inject(method = "saveDataTag(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/storage/WorldData;Lnet/minecraft/nbt/CompoundTag;)V",
        at = @At(value = "INVOKE", target = "Ljava/io/File;createTempFile(Ljava/lang/String;Ljava/lang/String;Ljava/io/File;)Ljava/io/File;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void writeAsync(
        final RegistryAccess p_78291_,
        final WorldData p_78292_,
        final CompoundTag p_wrong,
        final CallbackInfo ci,
        File asdf,
        CompoundTag p_wrong2,
        CompoundTag compoundtag1)
    {
        if (compoundtag1 == null)
        {
            return;
        }

        ci.cancel();

        FastAsyncWorldSave.threadPool.submit(() -> {
            File file1 = this.levelDirectory.path().toFile();
            try
            {
                File tempFile = File.createTempFile("level", ".dat", file1);
                File oldFile = this.levelDirectory.oldDataFile().toFile();
                File actualFile = this.levelDirectory.dataFile().toFile();
                NbtIo.writeCompressed(compoundtag1, tempFile);
                Util.safeReplaceFile(actualFile, tempFile, oldFile);
            }
            catch (Exception e)
            {
                FastAsyncWorldSave.LOGGER.error("Failed to save level {} data:" + compoundtag1, file1, e);
            }
        });
    }
}
