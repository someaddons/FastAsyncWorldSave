package com.fastasyncworldsave.mixin;

import com.fastasyncworldsave.FastAsyncWorldSave;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.nio.file.Path;

import static com.fastasyncworldsave.FastAsyncWorldSave.LOGGER;

@Mixin(PlayerDataStorage.class)
public class PlayerDatStorageMixin
{
    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/nio/file/Path;)V"))
    private void writeAsync(final CompoundTag compoundTag, final Path path, final Player player)
    {
        FastAsyncWorldSave.threadPool.submit(() -> {
            try
            {
                NbtIo.writeCompressed(compoundTag, path);
            }
            catch (IOException e)
            {
                LOGGER.warn("Failed to save player data for {}", player.getName().getString());
            }
        });
    }

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;safeReplaceFile(Ljava/nio/file/Path;Ljava/nio/file/Path;Ljava/nio/file/Path;)V"))
    private void writeAsync(final Path path3, final Path path2, final Path path4, final Player player)
    {
        FastAsyncWorldSave.threadPool.submit(() -> {
            try
            {
                Util.safeReplaceFile(path3, path2, path4);
            }
            catch (Exception e)
            {
                LOGGER.warn("Failed to save player data for {}", player.getName().getString());
            }
        });
    }
}
