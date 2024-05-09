package teal.hdhead.mixin;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import teal.hdhead.HDHeads;

import static net.minecraft.block.SkullBlock.Type.PLAYER;

@Mixin(SkullBlockEntityRenderer.class)
public abstract class SkullBlockEntityRendererMixin {

    @Unique
    private static final double preciseValue = 1E-3F;

    @Inject(
            method = "render(Lnet/minecraft/block/entity/SkullBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/block/entity/SkullBlockEntityRenderer;renderSkull(Lnet/minecraft/util/math/Direction;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/block/entity/SkullBlockEntityModel;Lnet/minecraft/client/render/RenderLayer;)V"
            )
    )
    public void preRenderSkull(SkullBlockEntity skullBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j, CallbackInfo ci) {
        if(HDHeads.doRunMod() && !HDHeads.getConfig().isMerge() && ((AbstractSkullBlock) skullBlockEntity.getCachedState().getBlock()).getSkullType() == PLAYER) {
            BlockState blockState = skullBlockEntity.getCachedState();
            boolean bl = blockState.getBlock() instanceof WallSkullBlock;
            Direction direction = bl ? blockState.get(WallSkullBlock.FACING) : null;
            if (direction == null)
                matrixStack.translate(0.0F, preciseValue, 0.0F);
            else {
                double xOffset = direction.getOffsetX();
                double zOffset = direction.getOffsetZ();
                if (Math.abs(xOffset) > 0) xOffset = xOffset > 0 ? preciseValue : -preciseValue;
                if (Math.abs(zOffset) > 0) zOffset = zOffset > 0 ? preciseValue : -preciseValue;
                matrixStack.translate(xOffset, 0.0D, zOffset);
            }
        }
    }

    @Redirect(
            method = "renderSkull",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V"
            )
    )
    private static void scaleMeDaddy(MatrixStack matrixStack, float x, float y, float z) {
        Integer[] t = HDHeads.getConfig().getScaleInject();
        matrixStack.scale(t[0], t[1], t[2]);
    }

}
