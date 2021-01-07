package dev.alexnader.pick_up.common;

import net.minecraft.util.Identifier;

public class PickUpMeta {
    public final String NAMESPACE = "pick_up";
    public final String INVALID_KEY = "pick_up.invalid";

    public Identifier id(String path) {
        return new Identifier(NAMESPACE, path);
    }
}
