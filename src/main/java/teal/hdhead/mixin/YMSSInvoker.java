package teal.hdhead.mixin;

import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = YggdrasilMinecraftSessionService.class, remap = false)
public interface YMSSInvoker {

    @Invoker
    static boolean callIsAllowedTextureDomain(final String url) {
        throw new AssertionError();
    }

}