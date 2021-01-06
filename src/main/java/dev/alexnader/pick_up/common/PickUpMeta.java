package dev.alexnader.pick_up.common;

import net.minecraft.util.Identifier;

public class PickUpMeta {
    public final String NAMESPACE = "pick_up";

    public Identifier id(String path) {
        return new Identifier(NAMESPACE, path);
    }
}
