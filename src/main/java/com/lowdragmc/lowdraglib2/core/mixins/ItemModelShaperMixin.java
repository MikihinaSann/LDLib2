package com.lowdragmc.lowdraglib2.core.mixins;

import com.lowdragmc.lowdraglib2.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib2.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib2.client.renderer.LDFabricBakedModel;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(ItemModelShaper.class)
public abstract class ItemModelShaperMixin {
    @Unique
    private final static Map<IRenderer, BakedModel> SHAPES_CACHE = new ConcurrentHashMap<>();

    @Inject(method = "getItemModel(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/resources/model/BakedModel;", at = @At("HEAD"), cancellable = true)
    public void ldlib2$injectGetModel(ItemStack stack, CallbackInfoReturnable<BakedModel> cir) {
        if (stack.getItem() instanceof IItemRendererProvider provider) {
            IRenderer renderer = provider.getRenderer(stack);
            if(renderer != null) {
                cir.setReturnValue(SHAPES_CACHE.computeIfAbsent(renderer, r -> new LDFabricBakedModel(r, stack)));
            }
        }
    }
}
