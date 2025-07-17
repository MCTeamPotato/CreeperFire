package me.kall.creeperfire.common.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobInvoker {
    @Invoker("isSunBurnTick")
    boolean creeperFire$isSunBrunTick();
}
