package com.lowdragmc.lowdraglib2.core.mixins.shader;

import com.lowdragmc.lowdraglib2.client.shader.LDLibShaders;
import com.lowdragmc.lowdraglib2.client.window.OsWindowManager;
import com.lowdragmc.lowdraglib2.uitest.UITestBootstrap;
import com.mojang.blaze3d.shaders.Program;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "reloadShaders", at = {@At(value = "HEAD")})
    private static void ldlib$reloadShaders(ResourceProvider resourceProvider, CallbackInfo ci) {
        LDLibShaders.GEOMETRY_TYPE.getPrograms().values().forEach(Program::close);
    }

    /**
     * Post-frame pump for secondary OS windows — the seam NeoForge exposes as
     * {@code RenderFrameEvent.Post}.
     */
    @Inject(method = "render", at = {@At(value = "RETURN")})
    private void ldlib$renderFramePost(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        OsWindowManager.onFrameRendered(deltaTracker.getGameTimeDeltaPartialTick(false));
        UITestBootstrap.onFrameRendered();
    }
}
