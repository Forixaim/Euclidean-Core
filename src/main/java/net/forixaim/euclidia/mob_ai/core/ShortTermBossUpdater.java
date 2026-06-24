package net.forixaim.euclidia.mob_ai.core;

import net.forixaim.euclidia.mixin.AttackAnimationTracker;
import net.forixaim.euclidia.mob_ai.data.ShortTermBossParameters;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Optional;

public class ShortTermBossUpdater {

    public static ShortTermBossParameters createCurrentSnapshot(
            LivingEntityPatch<?> bossPatch,
            ShortTermBossParameters previousFrame,
            float trackingErrorTelemetry
    ) {
        LivingEntity boss = bossPatch.getOriginal();

        float currentDamage = (boss.hurtTime > 0) ? 1.0f : Math.max(0.0f, previousFrame.recentDamageSustained() - 0.05f);
        Vec3 lookVec = boss.getLookAngle();
        boolean isPinned = !boss.level().noCollision(boss.getBoundingBox().move(lookVec.scale(-1.0)));
        float cornered = isPinned ? 1.0f : 0.0f;


        var animator = bossPatch.getAnimator();

        float isPoiseProtected = boss.hasEffect(EpicFightMobEffects.STUN_IMMUNITY) ? 1.0f : 0.0f;
        boolean isLocked = !bossPatch.getEntityState().canBasicAttack();
        float animationRecovery = (isLocked && isPoiseProtected == 0.0f) ? 1.0f : 0.0f;


        float executionProgress = 0.0f;

        if (bossPatch instanceof AttackAnimationTracker tracker)
        {
            Optional<AnimationPlayer> player = animator.getPlayer(tracker.euclidia$getAttackAnimation());
            if (player.isPresent())
            {
                executionProgress = player.get().getElapsedTime() / Math.max(1.0f, tracker.euclidia$getAttackAnimation().get().getTotalTime());
            }
        }

        // 6. RETURN THE FRESH RECORD MATRIX
        return new ShortTermBossParameters(
                currentDamage,
                cornered,
                isPoiseProtected,
                animationRecovery,
                executionProgress,
                trackingErrorTelemetry
        );
    }
}