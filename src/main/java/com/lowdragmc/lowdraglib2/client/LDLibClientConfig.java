package com.lowdragmc.lowdraglib2.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lowdragmc.lowdraglib2.LDLib2;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Client only settings for LDLib2, everything currently under the {@code font} section.
 * <p>
 * Fabric port: backed by a plain JSON file at {@code config/ldlib2-client.json} rather than
 * NeoForge's ModConfigSpec. The public accessors keep the same semantics; a change notifier
 * ({@link #addReloadListener}) replaces {@code ModConfigEvent.Reloading} so
 * {@link com.lowdragmc.lowdraglib2.client.font.LDFontManager} still rebuilds its atlases when the
 * file is edited through the development command.
 */
public class LDLibClientConfig {

    /**
     * Prefix for the translation keys a configuration screen looks up. Every entry additionally gets a
     * {@code .tooltip} key.
     */
    private static final String LANG = LDLib2.MOD_ID + ".configuration.";

    /**
     * How LDLib turns an outline into pixels.
     */
    public enum FontRenderMode {
        /**
         * Hand text to Minecraft's own font renderer and stay out of the way entirely. The baseline everything
         * else is compared against.
         */
        VANILLA,
        /**
         * Always sample a signed distance field. One atlas serves every size, scales and rotates smoothly, but
         * has no hinting so small text is softer than a hand tuned bitmap font, and sharp corners round off.
         */
        SDF,
        /**
         * Rasterize every glyph at the size it is actually drawn at, the way desktop UI toolkits do. Nothing is
         * approximated, so this is as sharp as the font gets, but each size needs its own atlas and a new size
         * has to be baked before it can be drawn. Only text beyond {@code fontRasterMaxSize} falls back, and
         * that is a memory guard rather than a judgement about how it looks.
         */
        RASTER,
        /**
         * Rasterize text that is sitting still on the pixel grid, and use the distance field for anything
         * scaled, rotated, skewed or animated, where it is both smoother and free of re-baking.
         */
        AUTO;

        public Component getTranslatedName() {
            return Component.translatable(LANG + "font.fontRenderMode." + name().toLowerCase(Locale.ROOT));
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("ldlib2-client.json");
    private static final java.util.List<Runnable> RELOAD_LISTENERS = new java.util.ArrayList<>();

    private static LDLibClientConfig INSTANCE = new LDLibClientConfig();
    private static boolean loaded = false;

    /**
     * Edge length in pixels of a glyph atlas page. Larger pages mean fewer draw calls.
     */
    public int fontAtlasSize = 1024;
    /**
     * Pixel height glyphs are rasterized at when generating the distance field.
     */
    public int sdfEmSize = 48;
    /**
     * Multiplier on the anti aliasing width. Above 1 is crisper, below 1 is softer.
     */
    public double sdfSharpness = 1.0;
    /**
     * Stem thickening in pixels. Raise slightly if small text looks too thin.
     */
    public double sdfWeight = 0.0;
    /**
     * Reuse the glyph positions of a string between frames.
     */
    public boolean textLayoutCache = true;
    /**
     * How LDLib UI text is rendered.
     */
    public FontRenderMode fontRenderMode = FontRenderMode.AUTO;
    /**
     * Largest device pixel line height the raster path will bake.
     */
    public int fontRasterMaxSize = 256;
    /**
     * Drop a rasterized size that has not been drawn for this many seconds.
     */
    public int fontRasterEvictSeconds = 30;

    public static synchronized void load() {
        if (Files.isRegularFile(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                LDLibClientConfig read = GSON.fromJson(reader, LDLibClientConfig.class);
                if (read != null) {
                    INSTANCE = read;
                }
            } catch (Exception e) {
                LDLib2.LOGGER.warn("Failed to read {}, using defaults", PATH, e);
            }
        }
        INSTANCE.sanitize();
        loaded = true;
    }

    private void sanitize() {
        fontAtlasSize = Math.max(256, Math.min(4096, fontAtlasSize));
        sdfEmSize = Math.max(16, Math.min(128, sdfEmSize));
        sdfSharpness = Math.max(0.1, Math.min(4.0, sdfSharpness));
        sdfWeight = Math.max(-0.5, Math.min(0.5, sdfWeight));
        fontRasterMaxSize = Math.max(16, Math.min(512, fontRasterMaxSize));
        fontRasterEvictSeconds = Math.max(1, Math.min(600, fontRasterEvictSeconds));
        if (fontRenderMode == null) fontRenderMode = FontRenderMode.AUTO;
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            LDLib2.LOGGER.warn("Failed to write {}", PATH, e);
        }
    }

    /**
     * Registers a callback fired after a value change is persisted, the fabric counterpart of
     * {@code ModConfigEvent.Reloading}. Runs on the calling thread.
     */
    public static void addReloadListener(Runnable listener) {
        RELOAD_LISTENERS.add(listener);
    }

    private static void fireReload() {
        for (var listener : RELOAD_LISTENERS) {
            listener.run();
        }
    }

    /**
     * @return true when LDLib renders text itself rather than handing it to the vanilla renderer
     */
    public static boolean isSmoothFont() {
        // fallback to modern ui if installed
        return fontRenderMode() != FontRenderMode.VANILLA && !LDLib2.isModLoaded("modernui");
    }

    public static int atlasSize() {
        return loaded ? INSTANCE.fontAtlasSize : 1024;
    }

    public static int emSize() {
        return loaded ? INSTANCE.sdfEmSize : 48;
    }

    public static float sharpness() {
        return loaded ? (float) INSTANCE.sdfSharpness : 1f;
    }

    public static float weight() {
        return loaded ? (float) INSTANCE.sdfWeight : 0f;
    }

    public static boolean isTextLayoutCache() {
        return loaded ? INSTANCE.textLayoutCache : true;
    }

    public static FontRenderMode fontRenderMode() {
        return loaded ? INSTANCE.fontRenderMode : FontRenderMode.AUTO;
    }

    /**
     * Writes the mode back to the config file, which is what the development command uses. Saving fires the
     * reload listeners, which is what rebuilds the glyph atlases.
     */
    public static void setFontRenderMode(FontRenderMode mode) {
        if (!loaded) load();
        INSTANCE.fontRenderMode = mode;
        save();
        fireReload();
    }

    public static int rasterMaxSize() {
        return loaded ? INSTANCE.fontRasterMaxSize : 256;
    }

    public static int rasterEvictSeconds() {
        return loaded ? INSTANCE.fontRasterEvictSeconds : 30;
    }
}
