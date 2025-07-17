package me.kall.creeperfire;

import com.google.common.base.Predicates;
import me.kall.creeperfire.common.api.SunBurnable;
import me.kall.creeperfire.common.mixin.MobInvoker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Mod(CreeperFire.MOD_ID)
@SuppressWarnings({"deprecation", "unused"})
public final class CreeperFire {
    public static final String MOD_ID = "creeperfire";

    public static final ModConfigSpec CONFIG;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SUN_BURNABLE;

    public CreeperFire(IEventBus modEventBus, Dist dist, @NotNull ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, CONFIG);
        NeoForge.EVENT_BUS.addListener(this::onLivingUpdate);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onEntityJoinLevel);
    }


    public void onServerStarting(ServerStartingEvent event) {
        SUN_BURNABLE.get().forEach(name -> Optional.of(((SunBurnable) BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(name)))).ifPresent(SunBurnable::creeperFire$setSunBurnable));
    }

    public void onEntityJoinLevel(@NotNull EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof PathfinderMob entity && entity.level() instanceof ServerLevel && ((SunBurnable)entity.getType()).creeperFire$getSunBurnable()) {
            entity.goalSelector.addGoal(3, new FleeSunGoal(entity, 1.0));
            entity.goalSelector.addGoal(2, new RestrictSunGoal(entity));
        }
    }

    public void onLivingUpdate(@NotNull EntityTickEvent.Pre event) {
        if (!event.isCanceled() && event.getEntity() instanceof PathfinderMob entity && entity.level() instanceof ServerLevel && ((SunBurnable)entity.getType()).creeperFire$getSunBurnable() && ((MobInvoker)entity).creeperFire$isSunBrunTick()) {
            ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (!head.isEmpty()) {
                if (head.isDamageableItem()) {
                    head.setDamageValue(head.getDamageValue() - ThreadLocalRandom.current().nextInt(2));
                    if (head.getDamageValue() >= head.getMaxDamage()) {
                        entity.onEquippedItemBroken(head.getItem(), EquipmentSlot.HEAD);
                        entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    }
                }
            } else {
                entity.igniteForSeconds(8);
            }
        }
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("CreeperFire");
        SUN_BURNABLE = builder.defineList("SunBurnable", List.of("minecraft:creeper"), Predicates.alwaysTrue());
        builder.pop();
        CONFIG = builder.build();
    }
}
