package com.fastasyncworldsave;

import net.minecraft.nbt.CompoundTag;

public interface ISaveData
{
    void setToSave(CompoundTag tag);

    void setThread(final Thread thread);
}
