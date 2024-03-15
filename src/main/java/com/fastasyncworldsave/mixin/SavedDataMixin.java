package com.fastasyncworldsave.mixin;

import com.fastasyncworldsave.ISaveData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(SavedData.class)
public class SavedDataMixin implements ISaveData
{
    @Unique
    private final ConcurrentLinkedQueue<CompoundTag> toSave = new ConcurrentLinkedQueue<>();

    @Override
    public void setToSave(final CompoundTag tag)
    {
        toSave.add(tag);
    }

    @Redirect(method = "save(Ljava/io/File;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/SavedData;isDirty()Z"))
    private boolean isSaving(final SavedData instance)
    {
        if (!toSave.isEmpty())
        {
            return true;
        }
        else
        {
            return instance.isDirty();
        }
    }

    @Redirect(method = "save(Ljava/io/File;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/SavedData;save(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag skipSer(final SavedData instance, final CompoundTag compoundTag)
    {
        if (!toSave.isEmpty())
        {
            return toSave.peek();
        }
        else
        {
            return instance.save(compoundTag);
        }
    }

    @Redirect(method = "save(Ljava/io/File;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/SavedData;setDirty(Z)V"))
    private void finishSaving(final SavedData instance, final boolean dirty)
    {
        if (!toSave.isEmpty())
        {
            toSave.poll();
        }
        else
        {
            instance.setDirty(dirty);
        }
    }
}
