package net.forixaim.euclidia.mob_ai.data;

import net.minecraft.util.Mth;

public class LongTermOpponentParameters {

    public float aggressiveTendency;
    public float defensiveTendency;
    public float evasiveTendency;
    public float panicFactor;
    public float projectileReliance;
    public float predictability;

    public LongTermOpponentParameters() {
        this(
                0.5f,
                0.5f,
                0.5f,
                0.5f,
                0.5f,
                0.5f
        );
    }


    public void updateLongTermTendencies(ShortTermOpponentParameters derivedIntent) {
        float blendFactor = 0.005f;

        float immediateAggressionTarget = Math.max(derivedIntent.willAttack, derivedIntent.willCloseDistance);
        this.aggressiveTendency = Mth.lerp(blendFactor, this.aggressiveTendency, immediateAggressionTarget);

        this.defensiveTendency  = Mth.lerp(blendFactor, this.defensiveTendency, derivedIntent.willGuard);
        this.projectileReliance = Mth.lerp(blendFactor, this.projectileReliance, derivedIntent.projectileSpamTendency);

        float immediateEvasiveTarget = Math.max(derivedIntent.willDodge, derivedIntent.willFlank);
        this.evasiveTendency    = Mth.lerp(blendFactor, this.evasiveTendency, immediateEvasiveTarget);

        float immediatePanicTarget = derivedIntent.willRetreat * derivedIntent.willStall;
        this.panicFactor        = Mth.lerp(blendFactor, this.panicFactor, immediatePanicTarget);
    }

    /**
     * Master Parameterized Constructor
     */
    public LongTermOpponentParameters(
            float aggressiveTendency,
            float defensiveTendency,
            float evasiveTendency,
            float panicFactor,
            float projectileReliance,
            float predictability
    ) {
        this.aggressiveTendency = aggressiveTendency;
        this.defensiveTendency  = defensiveTendency;
        this.evasiveTendency    = evasiveTendency;
        this.panicFactor        = panicFactor;
        this.projectileReliance = projectileReliance;
        this.predictability     = predictability;
    }

    /**
     * Mutates the current player profile parameters in-place during dynamic combat intermissions.
     * Slowly pulls hyper-specific tracking habits back toward the unread 0.5f baseline.
     * Zero memory allocations.
     */
    public void applyIntermissionDecay(float retentionFactor) {
        float alpha = Mth.clamp(retentionFactor, 0.0f, 1.0f);

        this.aggressiveTendency = Mth.lerp(alpha, 0.5f, this.aggressiveTendency);
        this.defensiveTendency  = Mth.lerp(alpha, 0.5f, this.defensiveTendency);
        this.evasiveTendency    = Mth.lerp(alpha, 0.5f, this.evasiveTendency);
        this.panicFactor        = Mth.lerp(alpha, 0.5f, this.panicFactor);
        this.predictability     = Mth.lerp(alpha, 0.5f, this.predictability);
    }

    /**
     * Smoothly blends fresh macro-behavior observations into the long-term memory matrix.
     */
    public void updateProfiling(
            float freshAggression, float freshDefense, float freshEvasion,
            float freshPanic, float freshPredictability, float learningRateAlpha
    ) {
        float alpha = Mth.clamp(learningRateAlpha, 0.0f, 1.0f);

        this.aggressiveTendency = Mth.lerp(alpha, this.aggressiveTendency, freshAggression);
        this.defensiveTendency  = Mth.lerp(alpha, this.defensiveTendency, freshDefense);
        this.evasiveTendency    = Mth.lerp(alpha, this.evasiveTendency, freshEvasion);
        this.panicFactor        = Mth.lerp(alpha, this.panicFactor, freshPanic);
        this.predictability     = Mth.lerp(alpha, this.predictability, freshPredictability);
    }

    /**
     * Flattens the 5 profiling variables into the core snapshot array.
     */
    public int flattenInto(float[] inputs, int startIndex) {
        inputs[startIndex]     = this.aggressiveTendency;
        inputs[startIndex + 1] = this.defensiveTendency;
        inputs[startIndex + 2] = this.evasiveTendency;
        inputs[startIndex + 3] = this.panicFactor;
        inputs[startIndex + 4] = this.projectileReliance;
        inputs[startIndex + 5] = this.predictability;

        return startIndex + 6;
    }
}