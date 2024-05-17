package teal.hdhead.mixin;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.texture.PlayerSkinProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import teal.hdhead.HeadClient;

import java.io.File;

@Mixin(PlayerSkinProvider.class)
public abstract class PlayerSkinProviderMixin {
    @Mutable
    @Shadow
    @Final
    private File skinCacheDir;

    @Redirect(
        method = "loadSkin(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/texture/PlayerSkinProvider$SkinTextureAvailableCallback;)Lnet/minecraft/util/Identifier;",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;getHash()Ljava/lang/String;",
            remap = false
        )
    )
    private String getHashRedirect(MinecraftProfileTexture profileTexture) {
        if (HeadClient.doRunMod() && !this.skinCacheDir.getPath().contains("hdheads"))
            this.skinCacheDir = new File(this.skinCacheDir.getPath(), "hdheads");
        return HeadClient.doRunMod() && HeadClient.getConfig().isHash() ? profileTexture.getUrl() : profileTexture.getHash();
    }

}
