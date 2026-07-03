package net.forixaim.euclidia.mob_ai.data;

import net.forixaim.euclidia.mob_ai.core.AIController;
import net.forixaim.euclidia.mob_ai.core.ActionHistory;
import net.minecraft.util.Mth;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class GeneralParameters {

    // Public fields for direct, high-performance matrix access
    public float relativeHealthRemaining;
    public float combatExhaustion;
    public float predictabilityScore;
    public float targetEvadeRatio;
    public float tradeSuccessRate;
    public int totalActiveAggressors;
    public float groupAttackConfidence;
    public float averageOpponentHealth;
    public float corneredRecency;
    public float environmentalHazardRisk;

    public GeneralParameters() {
        this(
                1.0f,
                0.0f,
                0.0f,
                0.0f,
                1.0f,
                0,
                1.0f,
                1.0f,
                0.0f,
                0.0f
        );
    }

    // Standard Constructor
    public GeneralParameters(
            float relativeHealthRemaining, float combatExhaustion, float predictabilityScore,
            float targetEvadeRatio, float tradeSuccessRate, int totalActiveAggressors,
            float groupAttackConfidence, float averageOpponentHealth, float corneredRecency,
            float environmentalHazardRisk
    ) {
        this.relativeHealthRemaining = relativeHealthRemaining;
        this.combatExhaustion = combatExhaustion;
        this.predictabilityScore = predictabilityScore;
        this.targetEvadeRatio = targetEvadeRatio;
        this.tradeSuccessRate = tradeSuccessRate;
        this.totalActiveAggressors = totalActiveAggressors;
        this.groupAttackConfidence = groupAttackConfidence;
        this.averageOpponentHealth = averageOpponentHealth;
        this.corneredRecency = corneredRecency;
        this.environmentalHazardRisk = environmentalHazardRisk;
    }

    /**
     * Updates the macro strategic state in-place directly on this instance.
     * Replaces the old static worker and merge allocation layers completely.
     */
    public void updateMacroState(
            LivingEntityPatch<?> bossPatch,
            AIController controller,
            ShortTermBossParameters shortTerm // 🧠 Your primary short-term feature bridge
    ) {
        LivingEntity boss = bossPatch.getOriginal();

        // 1. Authoritative State Calculations (Immediate, un-smoothed overrides)
        this.relativeHealthRemaining = boss.getHealth() / Math.max(1.0f, boss.getMaxHealth());
        this.totalActiveAggressors = controller.getTrackingEntities().size();

        this.predictabilityScore = shortTerm.actionSpamTendency;
        this.corneredRecency = shortTerm.cornered;

        this.environmentalHazardRisk = (boss.isInLava() || boss.isOnFire()) ? 1.0f : 0.0f;

        float targetAverageOpponentHealth = controller.getAverageHealthPercentage();

        float healthLost = 1.0f - this.relativeHealthRemaining;
        float crowdMultiplier = Math.max(0.2f, 1.0f - ((this.totalActiveAggressors - 1) * 0.15f));
        float targetConfidence = Mth.clamp((1.0f - healthLost) * crowdMultiplier, 0.0f, 1.0f);

        float targetExhaustion;
        if (shortTerm.currentExecutionTicksNormalized > 0.0f) {
            targetExhaustion = Math.min(1.0f, this.combatExhaustion + 0.02f + (shortTerm.actionSpamTendency * 0.01f));
        } else {
            targetExhaustion = Math.max(0.0f, this.combatExhaustion - 0.01f);
        }
        float blendFactor = 0.10f;
        this.combatExhaustion      = Mth.lerp(blendFactor, this.combatExhaustion, targetExhaustion);
        this.targetEvadeRatio      = Mth.lerp(blendFactor, this.targetEvadeRatio, this.targetEvadeRatio);
        this.tradeSuccessRate      = Mth.lerp(blendFactor, this.tradeSuccessRate, this.tradeSuccessRate);
        this.groupAttackConfidence = Mth.lerp(blendFactor, this.groupAttackConfidence, targetConfidence);
        this.averageOpponentHealth = Mth.lerp(blendFactor, this.averageOpponentHealth, targetAverageOpponentHealth);
    }

    /**
     * Flattens the 10 macro parameters into your core snapshot array.
     */
    public int flattenInto(float[] inputs, int startIndex) {
        inputs[startIndex]     = this.relativeHealthRemaining;
        inputs[startIndex + 1] = this.combatExhaustion;
        inputs[startIndex + 2] = this.predictabilityScore;
        inputs[startIndex + 3] = this.targetEvadeRatio;
        inputs[startIndex + 4] = this.tradeSuccessRate;
        inputs[startIndex + 5] = (float) this.totalActiveAggressors;
        inputs[startIndex + 6] = this.groupAttackConfidence;
        inputs[startIndex + 7] = this.averageOpponentHealth;
        inputs[startIndex + 8] = this.corneredRecency;
        inputs[startIndex + 9] = this.environmentalHazardRisk;

        return startIndex + 10;
    }
}