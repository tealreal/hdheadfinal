package teal.hdhead.mixin;

import com.mojang.authlib.yggdrasil.TextureUrlChecker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = TextureUrlChecker.class, remap = false)
public interface TUCInvoker {

    @Invoker
    static boolean callIsAllowedTextureDomain(final String url) {
        throw new AssertionError();
    }

}