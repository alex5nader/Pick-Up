package dev.alexnader.fabric_template.common;

import net.minecraft.util.Identifier;

public class Id<T> {
    public final T value;
    public final Identifier id;

    public Id(T value, Identifier id) {
        this.value = value;
        this.id = id;
    }
}
