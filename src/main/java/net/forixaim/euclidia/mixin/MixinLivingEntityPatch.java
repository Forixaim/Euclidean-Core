package net.forixaim.euclidia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(LivingEntityPatch.class)
public class MixinLivingEntityPatch implements AttackAnimationTracker
{
    @Unique
    private AssetAccessor<? extends StaticAnimation> euclidia$attackAnimation;

    @Override
    public AssetAccessor<? extends StaticAnimation> euclidia$getAttackAnimation() {
        return euclidia$attackAnimation;
    }

    @Override
    public void euclidia$setAttackAnimation(AssetAccessor<? extends StaticAnimation> animation) {
        this.euclidia$attackAnimation = animation;
    }
}
