package dev.alexnader.pick_up.common;

import dev.alexnader.server_config.api.Config;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.alexnader.pick_up.common.PickUpMeta.LOGGER;

public class Denylist<T> implements Config {
    private final Registry<T> registry;
    private final Set<T> denied = new ReferenceOpenHashSet<>();

    public Denylist(Registry<T> registry) {
        this.registry = registry;
    }

    public boolean isDenied(T toCheck) {
        return denied.contains(toCheck);
    }

    @Override
    public void initFromPacket(PacketByteBuf buf) {
        denied.clear();

        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            denied.add(registry.get(buf.readIdentifier()));
        }
    }

    @Override
    public void writeToPacket(PacketByteBuf buf) {
        buf.writeVarInt(denied.size());
        for (T denied : this.denied) {
            buf.writeIdentifier(registry.getId(denied));
        }
    }

    @Override
    public void initFromPath(Path path) {
        Stream<String> lines;
        try {
            lines = Files.newBufferedReader(path).lines();
        } catch (IOException e) {
            lines = Stream.empty();
            LOGGER.warn("`{}` didn't exist, creating.", path);
            try {
                Files.createFile(path);
            } catch (IOException e2) {
                LOGGER.warn("Failed to create `{}`.", path);
            }
        }

        lines
            .filter(line -> !line.isEmpty())
            .flatMap(line -> {
                try {
                    return Stream.of(new Identifier(line));
                } catch (InvalidIdentifierException e) {
                    LOGGER.warn("Invalid denylist entry in `{}`: `{}`", path, line);
                    return Stream.empty();
                }
            })
            .filter(id -> {
                // containsId is client-only :(
                if (!registry.getOrEmpty(id).isPresent()) {
                    LOGGER.warn("Invalid denylist entry in `{}`: `{}`", path, id);
                    return false;
                } else {
                    return true;
                }
            })
            .map(registry::get)
            .forEach(denied::add);
    }

    @Override
    public void saveToPath(Path path) {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(path))) {
            denied.stream()
                .map(registry::getId)
                .filter(Objects::nonNull)
                .map(Identifier::toString)
                .forEach(out::println);
        } catch (IOException e) {
            LOGGER.warn("Failed to write `{}`.", path);
        }
    }

    @Override
    public String toString() {
        return denied.stream()
            .map(registry::getId)
            .filter(Objects::nonNull)
            .map(Object::toString)
            .collect(Collectors.joining("\n"));
    }

    public void deny(T block) {
        denied.add(block);
    }
}
