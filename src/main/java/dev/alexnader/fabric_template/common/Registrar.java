package dev.alexnader.fabric_template.common;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Registrar<T> {
    private final Registry<T> target;

    protected Registrar(final Registry<T> target) {
        this.target = target;
    }

    protected <U extends T> Id<U> register(final Id<U> value) {
        Registry.register(this.target, value.id, value.value);
        return value;
    }

    protected <U extends T> Id<U> register(final U value, final Identifier id) {
        return new Id<>(Registry.register(this.target, id, value), id);
    }
}
