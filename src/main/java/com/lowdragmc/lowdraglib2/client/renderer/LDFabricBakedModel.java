package com.lowdragmc.lowdraglib2.client.renderer;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Item-model adapter that lets an {@link IRenderer} back a vanilla {@link BakedModel}.
 * Lives outside the mixin package on purpose: injected bytecode class-loads it, and classes
 * under a mixin config's package are blocked from normal loading.
 */
public class LDFabricBakedModel implements BakedModel, FabricBakedModel {
    private final IRenderer renderer;
    private final ItemStack stack;

    public LDFabricBakedModel(IRenderer renderer, ItemStack stack) {
        this.renderer = renderer;
        this.stack = stack;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(net.minecraft.world.level.BlockAndTintGetter blockView, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        var quads = renderer.renderModel(null, null, null, null, randomSupplier.get(), ModelData.EMPTY, null);
        var emitter = context.getEmitter();
        for (var quad : quads) {
            emitter.fromVanilla(quad, null, null);
            emitter.emit();
        }
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return renderer.renderModel(null, null, state, direction, random, ModelData.EMPTY, null);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return renderer.useAO() == TriState.DEFAULT || renderer.useAO() == TriState.TRUE;
    }

    @Override
    public boolean isGui3d() {
        return renderer.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return renderer.useBlockLight(stack);
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return renderer.getParticleTexture(null, null, ModelData.EMPTY);
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
