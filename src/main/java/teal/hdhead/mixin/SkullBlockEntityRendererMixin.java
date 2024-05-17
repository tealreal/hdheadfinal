package teal.hdhead.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teal.hdhead.HeadClient;

@Mixin(SkullBlockEntityRenderer.class)
public abstract class SkullBlockEntityRendererMixin {

    @Unique
    private static final double preciseValue = 1E-3F;

    @Inject(
        method = "renderSkull",
        at = @At("HEAD")
    )
    private static void preventAxisMerging(@Nullable Direction direction, float yaw, float animationProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, SkullBlockEntityModel model, RenderLayer renderLayer, CallbackInfo ci) {
        if (HeadClient.doRunMod() && !HeadClient.getConfig().isMerge()) {
            if (direction == null) {
                matrices.translate(0.0F, preciseValue, 0.0F);
                return;
            }

            matrices.translate(
                preciseValue * Math.signum(direction.getOffsetX()),
                0.0,
                preciseValue * Math.signum(direction.getOffsetZ())
            );
        }
    }

    @Redirect(
        method = "renderSkull",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V"
        )
    )
    private static void scaleSkull(MatrixStack matrixStack, float x, float y, float z) {
        Float[] t = HeadClient.getConfig().getScaleInject();
        matrixStack.scale(t[0], t[1], t[2]);
    }

}
