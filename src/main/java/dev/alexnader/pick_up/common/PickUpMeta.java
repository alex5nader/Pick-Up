package dev.alexnader.pick_up.common;

import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PickUpMeta {
    public final String NAMESPACE = "pick_up";
    public final String INVALID_KEY = "pick_up.invalid";
    public final Logger LOGGER = LogManager.getLogger("Pick Up");

    public Identifier id(String path) {
        return new Identifier(NAMESPACE, path);
    }
}
