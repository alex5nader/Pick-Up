package dev.alexnader.pick_up.mixin;

import dev.alexnader.pick_up.common.item.HeldItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.alexnader.pick_up.common.PickUp.ITEMS;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract Item getItem();

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    void customHeldName(CallbackInfoReturnable<Text> cir) {
        Item item = getItem();
        if (ITEMS.isHeldItem(item)) {
            cir.setReturnValue(
                new TranslatableText("pick_up.holding")
                    .append(new TranslatableText(((HeldItem) item).getHeldTranslationKey((ItemStack) (Object) this)))
            );
        }
    }
}
