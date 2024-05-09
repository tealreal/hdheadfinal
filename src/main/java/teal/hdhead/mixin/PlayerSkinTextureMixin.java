package teal.hdhead.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import teal.hdhead.HDHeads;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static teal.hdhead.HDHeads.*;

@Mixin(PlayerSkinTexture.class)
public abstract class PlayerSkinTextureMixin extends ResourceTexture {

    public PlayerSkinTextureMixin(Identifier location) {
        super(location);
    }

    @Unique
    private static boolean isInDimensions(int dimension) {
        int maxDimension = HDHeads.getConfig().getMaxDimension();
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
    // The code is clunky as fuck, thanks lambda functions.

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final @Nullable private File cacheFile;
    @Shadow @Final private String url;

    @Shadow protected abstract void onTextureLoaded(NativeImage image);
    @Shadow protected abstract NativeImage loadTexture(InputStream stream);

    @Shadow @Final @Nullable private Runnable loadedCallback;

    @Shadow private boolean loaded;

    @Shadow protected abstract void uploadTexture(NativeImage image);

    @Unique
    private static boolean isNotInSize(long length) {
        long size = HDHeads.getConfig().getThresholdSize();
        return doRunMod() && (size > 0 && length >= size);
    }

    @Redirect(
            method = "load",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/io/File;isFile()Z"
            )
    )
    private boolean isFileAndSizeCheck(File file) {
        if(file.isFile()) {
            if(isNotInSize(file.length())) {
                logger.info("Disregarding URL for being over {} bytes: {}", HDHeads.getConfig().getThresholdSize(), this.url);
                return false;
            } else return true;
        } else return false;
    }

    @ModifyArg(
            method = "load",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;runAsync(Ljava/lang/Runnable;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            ),
            index = 0
    )
    private Runnable anonymousDownloader(Runnable runnable) {
        return () -> {
            HttpURLConnection httpURLConnection = null;
            LOGGER.debug("Downloading http texture from {} to {}", this.url, this.cacheFile);

            try {
                httpURLConnection = (HttpURLConnection)(new URL(this.url)).openConnection(MinecraftClient.getInstance().getNetworkProxy());
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(false);
                httpURLConnection.connect();

                // Sometimes this value is inaccurate depending on the website.
                if (httpURLConnection.getResponseCode() / 100 == 2) {
                    if(isNotInSize(httpURLConnection.getContentLengthLong())) {
                        logger.info("Disregarding URL for being over {} bytes: {}", HDHeads.getConfig().getThresholdSize(), this.url);
                        return;
                    }
                    InputStream inputStream;
                    if (this.cacheFile != null) {
                        FileUtils.copyInputStreamToFile(httpURLConnection.getInputStream(), this.cacheFile);
                        inputStream = new FileInputStream(this.cacheFile);
                    } else {
                        inputStream = httpURLConnection.getInputStream();
                    }

                    //MinecraftClient.getInstance().execute(() -> {
                    CompletableFuture.runAsync(() -> {
                        NativeImage nativeImage = this.loadTexture(inputStream);
                        if (nativeImage != null) {
                            this.onTextureLoaded(nativeImage);
                        }
                    });
                }
            } catch (Exception var6) {
                LOGGER.error("Couldn't download http texture", var6);
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }

            }
        };
    }
}
