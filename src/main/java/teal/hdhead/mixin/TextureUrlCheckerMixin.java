package teal.hdhead.mixin;

import com.mojang.authlib.yggdrasil.TextureUrlChecker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import teal.hdhead.HDHeads;

import java.net.URI;
import java.util.List;
import java.util.Set;

@Mixin(value = TextureUrlChecker.class, remap = false)
public abstract class TextureUrlCheckerMixin {
    @Shadow @Final private static List<String> ALLOWED_DOMAINS;

    @Shadow private static boolean isDomainOnList(final String domain, final List<String> list) { return false; }

    @Redirect(
            method = "isAllowedTextureDomain",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/authlib/yggdrasil/TextureUrlChecker;isDomainOnList(Ljava/lang/String;Ljava/util/List;)Z"
            )
    )
    private static boolean isDomainOnListMod(String domain, List<String> domainList) {
        boolean isAllowed = ALLOWED_DOMAINS.equals(domainList);
        return isDomainOnList(domain, HDHeads.doRunMod() ? List.of(HDHeads.getConfig().getSites(isAllowed)) : domainList);
    }

    @Redirect(
            method = "isAllowedTextureDomain",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"
            )
    )
    private static boolean allowedSchemeModifier(Set<String> allowedSchemes, Object o) {
        List<String> schemes = List.of(HDHeads.getConfig().getSchemes());
        return HDHeads.doRunMod() ? schemes.contains("*") || schemes.contains( (String) o ) : allowedSchemes.contains( (String) o );
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
        if (!(host.endsWith(".minecraft.net") || host.endsWith(".mojang.com"))) HDHeads.logger.info("HD Heads Checkout - {}", uri);
        return host;
    }

    @Inject(
            method = "isDomainOnList",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void checkForAll(String domain, List<String> list, CallbackInfoReturnable<Boolean> cir) {
        if (list.contains("*")) cir.setReturnValue(true);
    }

}