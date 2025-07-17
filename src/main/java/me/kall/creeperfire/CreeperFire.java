package me.kall.creeperfire;

import com.google.common.base.Predicates;
import me.kall.creeperfire.common.api.SunBurnable;
import me.kall.creeperfire.common.mixin.MobInvoker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Mod(CreeperFire.MOD_ID)
public final class CreeperFire {
    public static final String MOD_ID = "creeperfire";

    public static final ForgeConfigSpec CONFIG;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SUN_BURNABLE;

    public CreeperFire(FMLJavaModLoadingContext context) {
        MinecraftForge.EVENT_BUS.addListener(this::onLivingUpdate);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityJoinLevel);
        context.registerConfig(ModConfig.Type.COMMON, CONFIG);
    }

    public void onServerStarting(ServerStartingEvent event) {
        SUN_BURNABLE.get().forEach(name -> Optional.ofNullable(((SunBurnable) ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(name)))).ifPresent(SunBurnable::creeperFire$setSunBurnable));
    }

    public void onEntityJoinLevel(@NotNull EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof PathfinderMob entity && entity.level() instanceof ServerLevel && ((SunBurnable)entity.getType()).creeperFire$getSunBurnable()) {
            entity.goalSelector.addGoal(3, new FleeSunGoal(entity, 1.0));
            entity.goalSelector.addGoal(2, new RestrictSunGoal(entity));
        }
    }

    public void onLivingUpdate(@NotNull LivingEvent.LivingTickEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof PathfinderMob entity && entity.level() instanceof ServerLevel && ((SunBurnable)entity.getType()).creeperFire$getSunBurnable() && ((MobInvoker)entity).creeperFire$isSunBrunTick()) {
            ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (!head.isEmpty()) {
                if (head.isDamageableItem()) {
                    head.setDamageValue(head.getDamageValue() - ThreadLocalRandom.current().nextInt(2));
                    if (head.getDamageValue() >= head.getMaxDamage()) {
                        entity.broadcastBreakEvent(EquipmentSlot.HEAD);
                        entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    }
                }
            } else {
                entity.setSecondsOnFire(8);
            }
        }
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("CreeperFire");
        SUN_BURNABLE = builder.defineList("SunBurnable", List.of("minecraft:creeper"), Predicates.alwaysTrue());
        builder.pop();
        CONFIG = builder.build();
    }
}
