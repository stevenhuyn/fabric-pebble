package net.fabric.pebble.mixin;

import net.fabric.pebble.PebbleMod;
import net.fabric.pebble.item.PebbleItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    @Final
    private EntityRenderDispatcher renderManager;

    @Shadow
    public abstract void renderItem(LivingEntity entity, ItemStack stack, ModelTransformation.Mode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    @Shadow
    private void applyEquipOffset(MatrixStack matrices, Arm arm, float equipProgress) {}

    @Inject(at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/item/ItemStack;getUseAction()Lnet/minecraft/util/UseAction;"), method = "renderFirstPersonItem", cancellable = true)
    private void pebblePullAnimation(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {

        // Code below largely collected from HeldItemRenderer.renderFirstPersonItem
        boolean isMainHand = hand == Hand.MAIN_HAND;
        Arm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
        if (item.getItem() == PebbleMod.PebbleItem) {
            // I'm sorry
            float x, y, z, w, u, v;
            int o = isMainHand ? 1 : -1;

            this.applyEquipOffset(matrices, arm, equipProgress);

            int itemUseTimeLeft = this.client.player.getItemUseTimeLeft();
            u = itemUseTimeLeft != 0 ? (float) item.getMaxUseTime() - ((float) itemUseTimeLeft - tickDelta + 1.0F) : item.getMaxUseTime();

            matrices.translate((double) ((float) o * -0.2785682F), 0.18344387412071228D, 0.15731531381607056D);
            matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-13.935F));
            matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) o * 35.3F));
            matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion((float) o * -9.785F));

            v = u / (float) PebbleItem.maxChargeTimeTicks;
            v = (v * v + v * 2.0F) / 3.0F;
            if (v > 1.0F) {
                v = 1.0F;
            }

            if (v > 0.1F) {
                w = MathHelper.sin((u - 0.1F) * 1.3F);
                x = v - 0.1F;
                y = w * x;
                matrices.translate((double) (y * 0.0F), (double) (y * 0.004F), (double) (y * 0.0F));
            }

            matrices.translate((double) (v * 0.0F), (double) (v * 0.0F), (double) (v * 0.04F));
            matrices.scale(1.0F, 1.0F, 1.0F + v * 0.2F);
            matrices.multiply(Vector3f.NEGATIVE_Y.getDegreesQuaternion((float) o * 45.0F));
            this.renderItem(player, item, isMainHand ? ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND : ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND, !isMainHand, matrices, vertexConsumers, light);

            matrices.pop();
            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method="renderArmHoldingItem", cancellable = true)
    private void pebbleThirdPersonAnimation(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo info) {
        System.out.println("Fuck");
        boolean bl = arm != Arm.LEFT;
        float f = bl ? 1.0F : -1.0F;
        float g = MathHelper.sqrt(swingProgress);
        float h = -0.3F * MathHelper.sin(g * 3.1415927F);
        float i = 0.4F * MathHelper.sin(g * 6.2831855F);
        float j = -0.4F * MathHelper.sin(swingProgress * 3.1415927F);
        matrices.translate((double)(f * (h + 0.64000005F)), (double)(i + -0.6F + equipProgress * -0.6F), (double)(j + -0.71999997F));
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(f * 45.0F));
        float k = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
        float l = MathHelper.sin(g * 3.1415927F);
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(f * l * 70.0F));
        matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(f * k * -20.0F));
        AbstractClientPlayerEntity abstractClientPlayerEntity = this.client.player;
        this.client.getTextureManager().bindTexture(abstractClientPlayerEntity.getSkinTexture());
        matrices.translate((double)(f * -1.0F), 3.5999999046325684D, 3.5D);
        matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(f * 120.0F));
        matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(200.0F));
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(f * -135.0F));
        matrices.translate((double)(f * 5.6F), 0.0D, 0.0D);
        PlayerEntityRenderer playerEntityRenderer = (PlayerEntityRenderer)this.renderManager.getRenderer(abstractClientPlayerEntity);
        if (bl) {
            playerEntityRenderer.renderRightArm(matrices, vertexConsumers, light, abstractClientPlayerEntity);
        } else {
            playerEntityRenderer.renderLeftArm(matrices, vertexConsumers, light, abstractClientPlayerEntity);
        }

        info.cancel();
    }
}
