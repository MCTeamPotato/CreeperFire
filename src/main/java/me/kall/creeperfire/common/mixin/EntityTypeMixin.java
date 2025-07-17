package me.kall.creeperfire.common.mixin;

import me.kall.creeperfire.common.api.SunBurnable;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements SunBurnable {
    @Unique
    private boolean creeperFire$sunBurnable;

    @Override
    public boolean creeperFire$getSunBurnable() {
        return this.creeperFire$sunBurnable;
    }

    @Override
    public void creeperFire$setSunBurnable() {
        this.creeperFire$sunBurnable = true;
    }
}
