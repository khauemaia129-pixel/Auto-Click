package com.eric.mods;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoClickerMod implements ModInitializer {
    public static final String MOD_ID = "auto-clicker-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Auto Clicker Mod inicializado!");
    }
}
