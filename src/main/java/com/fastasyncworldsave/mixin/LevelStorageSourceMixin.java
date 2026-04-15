package com.fastasyncworldsave.mixin;

import com.fastasyncworldsave.FastAsyncWorldSave;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public class LevelStorageSourceMixin
{
    @Shadow
    @Final
    public LevelStorageSource.LevelDirectory levelDirectory;

    /**
     * @author sam
     * @reason offthread saving
     */
    @Overwrite
    private void saveLevelData(CompoundTag compoundtag1)
    {
        FastAsyncWorldSave.threadPool.submit(() -> {
            Path path = this.levelDirectory.path();

            try
            {
                Path path1 = Files.createTempFile(path, "level", ".dat");
                NbtIo.writeCompressed(compoundtag1, path1);
                Path path2 = this.levelDirectory.oldDataFile();
                Path path3 = this.levelDirectory.dataFile();
                Util.safeReplaceFile(path3, path1, path2);
            }
            catch (Exception e)
            {
                FastAsyncWorldSave.LOGGER.error("Failed to save level {} data:" + compoundtag1, path, e);
            }
        });
    }
}
