package dev.alexnader.pick_up.mixin;

import dev.alexnader.pick_up.common.item.HeldBlockItem;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

import static dev.alexnader.pick_up.common.PickUp.ITEMS;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract Item getItem();

    @Shadow @Nullable public abstract CompoundTag getSubTag(String key);

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    void customHeldName(CallbackInfoReturnable<Text> cir) {
        if (getItem() == ITEMS.HELD_BLOCK_ITEM.value) {
            cir.setReturnValue(new TranslatableText("pick_up.holding").append(new TranslatableText(HeldBlockItem.getState((ItemStack) (Object) this).getBlock().getTranslationKey())));
        } else if (getItem() == ITEMS.HELD_ENTITY_ITEM.value) {
            cir.setReturnValue(new TranslatableText("pick_up.holding").append(new TranslatableText(EntityType.fromTag(getSubTag("entity")).get().getTranslationKey())));
        }
    }
}
