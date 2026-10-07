package com.eric.mods.mixin;

import com.eric.mods.AutoClickerConfig;
import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** O cliente envia a "marca" (brand) ao servidor ao entrar. O Fabric troca "vanilla" por "fabric"; aqui voltamos para "vanilla". */
@Mixin(ClientBrandRetriever.class)
public class ClientBrandMixin {
    @Inject(method = "getClientModName", at = @At("HEAD"), cancellable = true)
    private static void spoofBrand(CallbackInfoReturnable<String> cir) {
        if (AutoClickerConfig.isSpoofBrand()) {
            cir.setReturnValue("vanilla");
        }
    }
}
