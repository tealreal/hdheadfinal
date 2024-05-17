package teal.hdhead.mixin;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import teal.hdhead.HeadClient;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import static teal.hdhead.HeadClient.doRunMod;
import static teal.hdhead.HeadClient.logger;

@Mixin(PlayerSkinTexture.class)
public abstract class PlayerSkinTextureMixin extends ResourceTexture {

    public PlayerSkinTextureMixin(Identifier location) {
        super(location);
    }

    @Unique
    private static boolean isInDimensions(int dimension) {
        int maxDimension = HeadClient.getConfig().getMaxDimension();
        return doRunMod() && (maxDimension < 0 || dimension <= maxDimension);
    }

    @Redirect(
        method = "remapTexture",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/texture/NativeImage;getHeight()I"
        )
    )
    private int getHeight(NativeImage image) {
        if (isInDimensions(image.getHeight())) {
            // Handle heads that use Minecraft's old skin system (prior to jackets)
            return switch ((image.getWidth() / image.getHeight())) {
                case 1 -> 64;
                case 2 -> 32;
                default -> image.getHeight();
            };
        } else return image.getHeight();
    }

    @Redirect(
        method = "remapTexture",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/texture/NativeImage;getWidth()I"
        )
    )
    private int getWidth(NativeImage image) {
        return isInDimensions(image.getWidth()) ? 64 : image.getWidth();
    }

    // Some amount of security? - Block files that are above a size set by the user.
    @Shadow
    @Final
    private String url;

    @Unique
    private static boolean isNotInSize(long length) {
        long size = HeadClient.getConfig().getThresholdSizeInKilobytes() * 1000;
        return doRunMod() && (size > 0 && length >= size);
    }

    @Unique
    private void logIgnore() {
        logger.error("Disregarding URL for being over {} kilobytes: {}", HeadClient.getConfig().getThresholdSizeInKilobytes(), this.url);
    }

    @Redirect(
        method = "load",
        at = @At(
            value = "INVOKE",
            target = "Ljava/io/File;isFile()Z"
        )
    )
    private boolean isFileAndSizeCheck(File file) {
        if (file.isFile()) {
            if (isNotInSize(file.length())) {
                logIgnore();
                return false;
            } else return true;
        } else return false;
    }

    // when you finally read the docs to see how to mixin to anonymous lambdas
    @Redirect(
        method = "method_22801",
        at = @At(
            value = "INVOKE",
            target = "Ljava/net/HttpURLConnection;getResponseCode()I"
        )
    )
    private int anonLambdaSizeChecker(HttpURLConnection instance) throws IOException {
        if (isNotInSize(instance.getContentLengthLong())) {
            logIgnore();
            return 0;
        }
        return instance.getResponseCode();
    }
}
