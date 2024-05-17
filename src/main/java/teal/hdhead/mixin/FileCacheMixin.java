package teal.hdhead.mixin;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.texture.PlayerSkinProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import teal.hdhead.HeadClient;

import java.nio.file.Path;

@Mixin(PlayerSkinProvider.FileCache.class)
public abstract class FileCacheMixin {

    @Redirect(
        method = "store",
        at = @At(
            value = "INVOKE",
            target = "Ljava/nio/file/Path;resolve(Ljava/lang/String;)Ljava/nio/file/Path;",
            ordinal = 0
        )
    )
    private Path resolveDirectoryRedirect(Path instance, String other) {
        return (HeadClient.doRunMod() && HeadClient.getConfig().isHash() ? instance.resolve("hdheads") : instance).resolve(other);
    }

    // errrors in intellij but works coz idk
    @Redirect(
        method = {"store", "get"},
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;getHash()Ljava/lang/String;",
            remap = false
        )
    )
    private String getHashRedirect(MinecraftProfileTexture profileTexture) {
        return HeadClient.doRunMod() && HeadClient.getConfig().isHash() ? profileTexture.getUrl() : profileTexture.getHash();
    }

}
