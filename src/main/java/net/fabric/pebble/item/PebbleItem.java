package net.fabric.pebble.item;

import net.fabric.pebble.entity.PebbleEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class PebbleItem extends BowItem {

    public static float baseForce = 10f;
    public static float chargeTime = 10f;

    public PebbleItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        ItemStack itemStack = playerEntity.getStackInHand(hand);
        playerEntity.setCurrentHand(hand);
        return TypedActionResult.consume(itemStack);
    }

    @Override
    public void onStoppedUsing(ItemStack itemStack, World world, LivingEntity livingEntity, int remainingUseTicks) {
        PlayerEntity playerEntity = (PlayerEntity) livingEntity;

        // creates a new ItemStack instance of the user's itemStack in-hand
        world.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 1F); // plays a globalSoundEvent
        /*
         * user.getItemCooldownManager().set(this, 5); Optionally, you can add a
         * cooldown to your item's right-click use, similar to Ender Pearls.
         */
        if (!world.isClient) {
            PebbleEntity snowballEntity = new PebbleEntity(world, livingEntity);
            snowballEntity.setItem(itemStack);
            snowballEntity.setProperties(livingEntity, livingEntity.pitch, livingEntity.yaw, 0.0F, 1.5F, 0F);
            world.spawnEntity(snowballEntity); // spawns entity
        }

        playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!playerEntity.abilities.creativeMode) {
            itemStack.decrement(1); // decrements itemStack if user is not in creative mode
        }
    }
}