package net.forixaim.euclidia.mob_ai.data;

import net.forixaim.euclidia.mixin.AttackAnimationTracker;
import net.forixaim.euclidia.mob_ai.core.AIController;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.registry.entries.EpicFightMobEffects;

import java.util.Optional;

public class ShortTermBossParameters {

    public float recentDamageSustained;
    public float cornered;
    public float isPoiseProtected;
    public float animationRecovery;
    public float currentExecutionTicksNormalized;
    public float targetTrackingError;
    public float actionSpamTendency;
    public float lookingAtTarget;
    public float distanceToTarget;

    public void updateCurrentSnapshot(
            AIController controller,
            LivingEntity opponent,
            float trackingErrorTelemetry
    )
    {
        int totalActions = 0;
        int duplicateMatches = 0;
        var history = controller.getBossBrain().getActionHistory().getSnapshotView();

        for (int i = 0; i < history.size(); i++)
        {
            totalActions++;
            for (int j = i + 1; j < history.size(); j++)
            {
                if (history.get(i).value() == history.get(j).value())
                {
                    duplicateMatches++;
                }
            }
        }
        if (totalActions > 1)
        {
            float spamDensity = (float) duplicateMatches / (totalActions * 2.0f);
            this.actionSpamTendency = Math.min(1.0f, this.actionSpamTendency + (spamDensity * 0.1f));
        }
        LivingEntity boss = controller.getOriginal().getOriginal();
        float currentDamage = (boss.hurtTime > 0) ? 1.0f : Math.max(0.0f, this.recentDamageSustained - 0.05f);

        Vec3 lookVec = boss.getLookAngle();
        boolean isPinned = !boss.level().noCollision(boss.getBoundingBox().move(lookVec.scale(-1.0)));
        float cornered = isPinned ? 1.0f : 0.0f;

        float isPoiseProtected = boss.hasEffect(EpicFightMobEffects.STUN_IMMUNITY) ? 1.0f : 0.0f;
        boolean isLocked = !controller.getOriginal().getEntityState().canBasicAttack();
        float animationRecovery = (isLocked && isPoiseProtected == 0.0f) ? 1.0f : 0.0f;

        float executionProgress = 0.0f;
        var animator = controller.getOriginal().getAnimator();


        if (controller.getOriginal() instanceof AttackAnimationTracker tracker)
        {
            var animationOpt = tracker.euclidia$getAttackAnimation();
            if (animationOpt != null && animationOpt.isPresent())
            {
                Optional<AnimationPlayer> player = animator.getPlayer(animationOpt);
                if (player.isPresent())
                {
                    float elapsedTime = player.get().getElapsedTime();
                    float totalTime = Math.max(1.0f, animationOpt.get().getTotalTime());
                    executionProgress = elapsedTime / totalTime;
                }
            }
        }



        this.recentDamageSustained = currentDamage;
        this.cornered = cornered;
        this.isPoiseProtected = isPoiseProtected;
        this.animationRecovery = animationRecovery;
        this.currentExecutionTicksNormalized = executionProgress;
        this.targetTrackingError = trackingErrorTelemetry;
        updateLineOfSight(boss, opponent);
        updateDistance(controller, opponent);
    }

    public void updateLineOfSight(LivingEntity boss, LivingEntity opponent) {
        var lookVec = boss.getViewVector(1.0F);
        double lookX = lookVec.x;
        double lookY = lookVec.y;
        double lookZ = lookVec.z;
        double diffX = opponent.getX() - boss.getX();
        double diffY = opponent.getEyeY() - boss.getEyePosition().y;
        double diffZ = opponent.getZ() - boss.getZ();
        double distance = Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
        if (distance < 0.001) {
            this.lookingAtTarget = 1.0f;
            return;
        }
        double targetX = diffX / distance;
        double targetY = diffY / distance;
        double targetZ = diffZ / distance;
        double dotProduct = (lookX * targetX) + (lookY * targetY) + (lookZ * targetZ);
        this.lookingAtTarget = (dotProduct > 0.92) ? 1.0f : 0.0f;
    }

    public void updateDistance(AIController boss, LivingEntity opponent) {
        double rawDistance = boss.getOriginal().getOriginal().distanceTo(opponent);
        double maxRange = 20.0;

        this.distanceToTarget = (float) (Math.min(rawDistance, maxRange) / maxRange);
    }

    public ShortTermBossParameters() {
        this(
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                0.0f
        );
    }

    public ShortTermBossParameters(
            float recentDamageSustained,
            float cornered,
            float isPoiseProtected,
            float animationRecovery,
            float currentExecutionTicksNormalized,
            float targetTrackingError,
            float lookingAtTarget,
            float spamTendency,
            float distanceToTarget
            )
    {
        this.recentDamageSustained = recentDamageSustained;
        this.cornered = cornered;
        this.isPoiseProtected = isPoiseProtected;
        this.animationRecovery = animationRecovery;
        this.currentExecutionTicksNormalized = currentExecutionTicksNormalized;
        this.targetTrackingError = targetTrackingError;
        this.actionSpamTendency = spamTendency;
        this.lookingAtTarget = lookingAtTarget;
        this.distanceToTarget = distanceToTarget;
    }
    public void decay(float decayRate) {
        this.recentDamageSustained           = Math.max(0.0f, this.recentDamageSustained - decayRate);
        this.animationRecovery               = Math.max(0.0f, this.animationRecovery - decayRate);
        this.currentExecutionTicksNormalized = Math.max(0.0f, this.currentExecutionTicksNormalized - decayRate);
        this.targetTrackingError             = Math.max(0.0f, this.targetTrackingError - decayRate);
    }

    public int flattenInto(float[] inputs, int startIndex) {
        inputs[startIndex]     = this.recentDamageSustained;
        inputs[startIndex + 1] = this.cornered;
        inputs[startIndex + 2] = this.isPoiseProtected;
        inputs[startIndex + 3] = this.animationRecovery;
        inputs[startIndex + 4] = this.currentExecutionTicksNormalized;
        inputs[startIndex + 5] = this.targetTrackingError;
        inputs[startIndex + 6] = this.actionSpamTendency;
        inputs[startIndex + 7] = this.lookingAtTarget;
        inputs[startIndex + 8] = this.distanceToTarget;

        return startIndex + 9;
    }
}