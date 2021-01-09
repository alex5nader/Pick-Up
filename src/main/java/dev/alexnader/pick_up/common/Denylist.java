package dev.alexnader.pick_up.common;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static dev.alexnader.pick_up.common.PickUp.META;

public class Denylist {
    private Denylist() { }

    public static Denylist fromConfig() {
        Denylist denylist = new Denylist();

        readFromModMetadata(denylist.deniedEntityTypes, denylist.deniedBlocks);

        Path configDir = ensureConfigDir();
        if (configDir != null) {
            readFromConfig(configDir.resolve(BLOCKS_FILE), Registry.BLOCK, denylist.deniedBlocks);
            readFromConfig(configDir.resolve(ENTITIES_FILE), Registry.ENTITY_TYPE, denylist.deniedEntityTypes);
        }

        return denylist;
    }

    public static Denylist fromBuf(PacketByteBuf buf) {
        Denylist denylist = new Denylist();

        int entityCount = buf.readInt();
        for (int i = 0; i < entityCount; i++) {
            denylist.deniedEntityTypes.add(Registry.ENTITY_TYPE.get(buf.readIdentifier()));
        }

        int blockCount = buf.readInt();
        for (int i = 0; i < blockCount; i++) {
            denylist.deniedBlocks.add(Registry.BLOCK.get(buf.readIdentifier()));
        }

        return denylist;
    }

    public static final Identifier PACKET_ID = META.id("denylist");

    private static final String CONFIG_DIR = "pick_up";
    private static final String BLOCKS_FILE = "denied_blocks.txt";
    private static final String ENTITIES_FILE = "denied_entities.txt";

    private static @Nullable Path ensureConfigDir() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_DIR);
        try {
            Files.createDirectories(configDir);
            return configDir;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private final Set<EntityType<?>> deniedEntityTypes = new ReferenceOpenHashSet<>();
    private final Set<Block> deniedBlocks = new ReferenceOpenHashSet<>();

    public void sendToPlayer(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, Denylist.PACKET_ID, this.toBuf());
    }

    public boolean isDenied(EntityType<?> entityType) {
        return deniedEntityTypes.contains(entityType);
    }
    public boolean isDenied(Entity entity) {
        return isDenied(entity.getType());
    }

    public boolean isDenied(Block block) {
        return deniedBlocks.contains(block);
    }
    public boolean isDenied(BlockState state) {
        return isDenied(state.getBlock());
    }

    public PacketByteBuf toBuf() {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeInt(deniedEntityTypes.size());
        for (EntityType<?> type : deniedEntityTypes) {
            buf.writeIdentifier(Registry.ENTITY_TYPE.getId(type));
        }

        buf.writeInt(deniedBlocks.size());
        for (Block block : deniedBlocks) {
            buf.writeIdentifier(Registry.BLOCK.getId(block));
        }

        return buf;
    }

    private static void readFromModMetadata(Set<EntityType<?>> deniedEntityTypes, Set<Block> deniedBlocks) {
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ModMetadata meta = mod.getMetadata();
            if (!meta.containsCustomValue(META.NAMESPACE)) {
                continue;
            }

            CustomValue.CvObject pickUpData = meta.getCustomValue(META.NAMESPACE).getAsObject();

            if (pickUpData.containsKey("denied_blocks")) {
                for (CustomValue deniedBlock : pickUpData.get("denied_blocks").getAsArray()) {
                    deniedBlocks.add(Registry.BLOCK.get(new Identifier(deniedBlock.getAsString())));
                }
            }

            if (pickUpData.containsKey("denied_entities")) {
                for (CustomValue deniedEntity : pickUpData.get("denied_entities").getAsArray()) {
                    deniedEntityTypes.add(Registry.ENTITY_TYPE.get(new Identifier(deniedEntity.getAsString())));
                }
            }
        }
    }

    private static <T> void readFromConfig(Path path, Registry<T> registry, Set<T> denylist) {
        Stream<String> lines;
        try {
            lines = Files.newBufferedReader(path).lines();
        } catch (IOException e) {
            lines = Stream.empty();
            META.LOGGER.warn("`{}` didn't exist, creating.", path);
            try {
                Files.createFile(path);
            } catch (IOException e2) {
                META.LOGGER.warn("Failed to create `{}`.", path);
            }
        }

        lines
            .filter(line -> !line.isEmpty())
            .flatMap(line -> {
                try {
                    return Stream.of(new Identifier(line));
                } catch (InvalidIdentifierException e) {
                    META.LOGGER.warn("Invalid denylist entry in `{}`: `{}`", path, line);
                    return Stream.empty();
                }
            })
            .filter(id -> {
                // containsId is client-only :(
                if (!id.equals(registry.getId(registry.get(id)))) {
                    META.LOGGER.warn("Invalid denylist entry in `{}`: `{}`", path, id);
                    return false;
                } else {
                    return true;
                }
            })
            .map(registry::get)
            .forEach(denylist::add);
    }

    public void save() {
        Path configDir = ensureConfigDir();
        if (configDir != null) {
            writeToConfig(configDir.resolve(ENTITIES_FILE), Registry.ENTITY_TYPE, deniedEntityTypes);
            writeToConfig(configDir.resolve(BLOCKS_FILE), Registry.BLOCK, deniedBlocks);
        }
    }

    private static <T> void writeToConfig(Path path, Registry<T> registry, Set<T> denylist) {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(path))) {
            denylist.stream()
                .map(registry::getId)
                .filter(Objects::nonNull)
                .map(Identifier::toString)
                .forEach(out::println);
        } catch (IOException e) {
            META.LOGGER.warn("Failed to write `{}`.", path);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("DENIED BLOCKS:\n");
        for (Block block : deniedBlocks) {
            builder.append(Registry.BLOCK.getId(block)).append("\n");
        }

        builder.append("\nDENIED ENTITIES:\n");
        for (EntityType<?> entityType : deniedEntityTypes) {
            builder.append(Registry.ENTITY_TYPE.getId(entityType)).append("\n");
        }

        return builder.toString();
    }

    public void deny(Block block) {
        deniedBlocks.add(block);
    }
}
