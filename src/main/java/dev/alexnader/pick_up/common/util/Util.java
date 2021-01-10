package dev.alexnader.pick_up.common.util;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class Util {
    private Util() {
        throw new IllegalStateException("Helper class should not be instantiated.");
    }

    public static Text linkTo(String url) {
        return new LiteralText(url)
            .setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.link.open")))
                .withFormatting(Formatting.UNDERLINE)
            );
    }

    public static Vec3d toVec3d(Vec3i vec) {
        return new Vec3d(vec.getX(), vec.getY(), vec.getZ());
    }
}
