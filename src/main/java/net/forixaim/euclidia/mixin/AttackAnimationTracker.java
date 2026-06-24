package net.forixaim.euclidia.mixin;

import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;

public interface AttackAnimationTracker {
    AssetAccessor<? extends StaticAnimation> euclidia$getAttackAnimation();

    void euclidia$setAttackAnimation(AssetAccessor<? extends StaticAnimation> animation);
}