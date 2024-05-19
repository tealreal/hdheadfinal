package teal.hdhead.mixin;

import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import teal.hdhead.HeadClient;

import java.net.URI;
import java.util.Arrays;

@Mixin(value = YggdrasilMinecraftSessionService.class, remap = false)
public abstract class YggdrasilMinecraftSessionServiceMixin {
    @Shadow
    @Final
    private static String[] ALLOWED_DOMAINS;

    @Shadow
    private static boolean isDomainOnList(final String domain, final String[] domains) {
        return false;
    }

    @Redirect(
        method = "isAllowedTextureDomain",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/authlib/yggdrasil/YggdrasilMinecraftSessionService;isDomainOnList(Ljava/lang/String;[Ljava/lang/String;)Z"
        )
    )
    private static boolean isDomainOnListMod(String domain, String[] domains) {
        boolean isAllowed = Arrays.equals(ALLOWED_DOMAINS, domains);
        return isDomainOnList(domain, HeadClient.doRunMod() ? HeadClient.getConfig().getSites(isAllowed) : domains);
    }

    @Redirect(
        method = "isAllowedTextureDomain",
        at = @At(
            value = "INVOKE",
            target = "Ljava/net/URI;getHost()Ljava/lang/String;"
        )
    )
    private static String uriGetHostModifier(URI uri) {
        String host = uri.getHost();
        if (!(host.endsWith(".minecraft.net") || host.endsWith(".mojang.com")))
            HeadClient.logger.info("HD Heads Checkout - {}", uri);
        return host;
    }

    @Inject(
        method = "isDomainOnList",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void checkForAll(String domain, String[] list, CallbackInfoReturnable<Boolean> cir) {
        if (Arrays.asList(list).contains("*")) cir.setReturnValue(true);
    }

}