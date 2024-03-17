package com.fastasyncworldsave.mixin;

import com.fastasyncworldsave.ISaveData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(SavedData.class)
public class SavedDataMixin implements ISaveData
{
    @Shadow private boolean dirty;

    @Unique
    private final ConcurrentLinkedQueue<CompoundTag> toSave = new ConcurrentLinkedQueue<>();

    @Unique
    private Thread currentThread = null;

    @Override
    public void setToSave(final CompoundTag tag)
    {
        toSave.add(tag);
    }

    @Override
    public void setThread(final Thread thread)
    {
        currentThread = thread;
    }

    /**
     * Dirty while saving
     * @return
     */
    @Overwrite
    public boolean isDirty()
    {
        return this.dirty || (currentThread != null && currentThread == Thread.currentThread());
    }

    /**
     * Redirect dirty, incase someone overrides it
     * @param instance
     * @return
     */
    @Redirect(method = "save(Ljava/io/File;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/SavedData;isDirty()Z"))
    private boolean isSaving(final SavedData instance)
    {
        if (currentThread != null && currentThread == Thread.currentThread())
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
        if (!toSave.isEmpty() && (currentThread != null && currentThread == Thread.currentThread()))
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
        if (!toSave.isEmpty() && (currentThread != null && currentThread == Thread.currentThread()))
        {
            toSave.poll();
        }
        else
        {
            instance.setDirty(dirty);
        }
    }
}
