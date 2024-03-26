package com.fastasyncworldsave.mixin;

import com.fastasyncworldsave.FastAsyncWorldSave;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
                if (!savedData.isDirty())
                {
                    return;
                }

                final CompoundTag ser = savedData.save(new CompoundTag());

                if (ser == null)
                {
                    return;
                }

                savedData.setDirty(false);

                Util.ioPool().submit(() -> {
                    try
                    {
                        final CompoundTag compoundtag = new CompoundTag();
                        compoundtag.put("data", ser);
                        NbtUtils.addCurrentDataVersion(compoundtag);

                        File file = getDataFile(string);
                        File temp = file.toPath().getParent().resolve("tmp_" + file.getName()).toFile();

                        temp.getParentFile().mkdirs();
                        NbtIo.writeCompressed(compoundtag, temp);
                        try
                        {
                            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE);
                        }
                        catch (Exception e)
                        {
                            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    catch (Exception e)
                    {
                        FastAsyncWorldSave.LOGGER.error("Could not save data " + ser.toString(), e);
                    }
                });
            }
        });
    }
}
