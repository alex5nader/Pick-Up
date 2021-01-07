package dev.alexnader.pick_up.common.item;

import net.minecraft.item.ItemStack;

public interface HeldItem {
    String getHeldTranslationKey(ItemStack stack);
}
