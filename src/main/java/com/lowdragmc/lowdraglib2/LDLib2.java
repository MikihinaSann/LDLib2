package com.lowdragmc.lowdraglib2;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceLocation;
import com.lowdragmc.lowdraglib2.client.ClientProxy;
import com.lowdragmc.lowdraglib2.core.mixins.MixinPluginShared;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;

public class LDLib2 implements ModInitializer {
    public static final String MOD_ID = "ldlib2";
    public static final String NAME = "LowDragLib2";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public static final String MODID_JEI = "jei";
    public static final String MODID_RUBIDIUM = "rubidium";
    public static final String MODID_REI = "roughlyenoughitems";
    public static final String MODID_EMI = "emi";
    public static final RandomSource RANDOM = RandomSource.createThreadSafe();
    public static final Gson GSON = new GsonBuilder().create();
    private static File assetsLocation;

    @Override
    public void onInitialize() {
        LDLib2.init();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Platform.SERVER = server;
            Platform.SERVER_REGISTRY_ACCESS = server.registryAccess();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            Platform.SERVER = null;
            Platform.SERVER_REGISTRY_ACCESS = null;
        });

        CommonListeners.register();
        new CommonProxy();
        com.lowdragmc.lowdraglib2.uitest.MPServerBootstrap.init();
    }

    public static void init() {
        LOGGER.info("{} is initializing on platform: {}", NAME, Platform.platformName());
        getAssetsDir();
    }

    public static File getAssetsDir() {
        if (assetsLocation == null) {
            assetsLocation = new File(Platform.getGamePath().toFile(), "ldlib2/assets");
            if (assetsLocation.mkdirs()) {
                LOGGER.info("Created assets folder {}", assetsLocation.getPath());
            }
            if (new File(assetsLocation, "ldlib2").mkdirs()) {
                LOGGER.info("Created ldlib2 assets folder {}", assetsLocation.getPath());
            }
        }
        return assetsLocation;
    }

    public static boolean isValidResourceLocation(String string) {
        int i = string.indexOf(":");
        if (i == -1) {
            for (int j = 0; j < string.length(); j++) {
                if (!ResourceLocation.isAllowedInResourceLocation(string.charAt(j))) {
                    return false;
                }
            }
        } else {
            var namespace = string.substring(0, i);
            var path = string.substring(i + 1);
            return ResourceLocation.isValidNamespace(namespace) && ResourceLocation.isValidPath(path);
        }
        return true;
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static boolean isClient() {
        return Platform.isClient();
    }

    public static boolean isRemote() {
        var minecraft = Platform.getMinecraftClient();
        return minecraft != null && minecraft.isSameThread();
    }

    public static boolean isServer() {
        if (!isClient()) return true;
        var server = Platform.getMinecraftServer();
        if (server != null) {
            return server.isSameThread();
        }
        return false;
    }

    public static boolean isModLoaded(String mod) {
        return Platform.isModLoaded(mod);
    }

    public static boolean isJeiLoaded() {
        return isModLoaded(MODID_JEI);
    }

    public static boolean isReiLoaded() {
        return isModLoaded(MODID_REI);
    }

    public static boolean isEmiLoaded() {
        return isModLoaded(MODID_EMI);
    }

    public static boolean isKubejsLoaded() {
        return Platform.isModLoaded("kubejs");
    }

    public static boolean isIrisLoaded() {
        return MixinPluginShared.IS_IRIS_LOAD;
    }

    public static boolean isOculusLoaded() {
        return MixinPluginShared.IS_OCULUS_LOAD;
    }

    public static boolean isOptifineLoaded() {
        return MixinPluginShared.IS_OPT_LOAD;
    }
}
