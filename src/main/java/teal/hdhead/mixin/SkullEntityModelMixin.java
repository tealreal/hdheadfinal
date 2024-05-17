package teal.hdhead.mixin;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.render.entity.model.SkullEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import teal.hdhead.HeadClient;

@Mixin(SkullEntityModel.class)
public abstract class SkullEntityModelMixin {
    @Redirect(
        method = "getHeadTexturedModelData",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/ModelPartBuilder;cuboid(FFFFFFLnet/minecraft/client/model/Dilation;)Lnet/minecraft/client/model/ModelPartBuilder;"
        )
    )
    private static ModelPartBuilder cuboid(ModelPartBuilder instance, float offsetX, float offsetY, float offsetZ, float sizeX, float sizeY, float sizeZ, Dilation extra) {
        return instance.cuboid(offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, HeadClient.getConfig().isShrinkHat() ? new Dilation(1E-3F) : extra);
    }
}
