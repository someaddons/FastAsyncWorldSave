package com.fastasyncworldsave.mixin;

import com.fastasyncworldsave.FastAsyncWorldSave;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(DimensionDataStorage.class)
public abstract class DimensionDataStorageMixin
{
    @Shadow
    protected abstract File getDataFile(final String string);

    @Shadow
    @Final
    private HolderLookup.Provider registries;

    @ModifyVariable(method = "getDataFile", at = @At("HEAD"), argsOnly = true)
    private String fixName(final String value)
    {
        String os = System.getProperty("os.name").toLowerCase();

        if (value.contains(":") && (os.contains("win") || os.contains("mac")))
        {
            return value.replace(":", "_");
        }

        return value;
    }

    @Redirect(method = "save", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
    private void fastasyncworldsave$saveOffthread(final Map<String, SavedData> instance, final BiConsumer<String, SavedData> entry)
    {
        Map<String, CompoundTag> toSave = null;
        for (Map.Entry<String, SavedData> mapEntry : instance.entrySet())
        {
            String id = mapEntry.getKey();
            SavedData savedData = mapEntry.getValue();
            if (savedData != null)
            {
                if (!savedData.isDirty())
                {
                    continue;
                }

                final CompoundTag compound;
                try
                {
                    compound = savedData.save(new CompoundTag(), registries);
                }
                catch (Exception e)
                {
                    FastAsyncWorldSave.LOGGER.error("Level data failed to save for: " + id + " report to the respective mod", e);
                    continue;
                }

                if (compound == null)
                {
                    continue;
                }

                if (toSave == null)
                {
                    toSave = new HashMap<>();
                }

                savedData.setDirty(false);
                toSave.put(id, compound);
            }
        }

        if (toSave != null)
        {
            final Map<String, CompoundTag> saveData = toSave;
            FastAsyncWorldSave.threadPool.submit(() -> {
                for (final var toSaveEntry : saveData.entrySet())
                {
                    try
                    {
                        final CompoundTag compoundtag = new CompoundTag();
                        compoundtag.put("data", toSaveEntry.getValue());
                        NbtUtils.addCurrentDataVersion(compoundtag);

                        File file = getDataFile(toSaveEntry.getKey());
                        File temp = file.toPath().getParent().resolve("tmp_" + file.getName()).toFile();

                        temp.getParentFile().mkdirs();
                        NbtIo.writeCompressed(compoundtag, temp.toPath());
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
                        FastAsyncWorldSave.LOGGER.error("Could not save data " + toSaveEntry.getValue().toString(), e);
                    }
                }
            });
        }
    }
}
