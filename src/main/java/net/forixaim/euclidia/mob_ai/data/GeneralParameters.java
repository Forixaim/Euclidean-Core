package net.forixaim.euclidia.mob_ai.data;

public record GeneralParameters(
        float relativeHealthRemaining,    // 1.0 down to 0.0 (Authoritative)
        //float postureStaminalFraction,   // Current balance/posture pool percentage (0.0 to 1.0)

        float combatExhaustion,           // Builds up as the boss throws heavy moves (Smoothed)
        float predictabilityScore,         // Driven by your 8-slot ActionHistory Queue (Authoritative)

        float targetEvadeRatio,            // High if players successfully dodge/parry boss swings
        float tradeSuccessRate,            // High if boss successfully trades hits using poise/armor

        int totalActiveAggressors,        // How many players are actively tracking/hitting the boss
        float groupAttackConfidence,      // 0.0 to 1.0 determining tactical bravery/desperation
        float averageOpponentHealth,      // Combined health pool fraction of the entire raid group

        float corneredRecency,            // 1.0 if backed up against a wall, 0.0 in the open
        float environmentalHazardRisk     // Spikes if standing in fire, lava, etc.
) {
    /**
     * Unified selective smoothing merge logic.
     */
    public GeneralParameters merge(GeneralParameters newParams, float factor) {
        return new GeneralParameters(
                // 1. Structural Boss Vitale
                newParams.relativeHealthRemaining,
                //lerp(this.postureStaminalFraction, newParams.postureStaminalFraction, factor),

                // 2. Tactical & Memory Diagnostics
                lerp(this.combatExhaustion, newParams.combatExhaustion, factor),
                newParams.predictabilityScore,

                // 3. Behavioral Success Trackers
                lerp(this.targetEvadeRatio, newParams.targetEvadeRatio, factor),
                lerp(this.tradeSuccessRate, newParams.tradeSuccessRate, factor),

                // 4. Multi-Target & Crowd Telemetry
                newParams.totalActiveAggressors,
                lerp(this.groupAttackConfidence, newParams.groupAttackConfidence, factor),
                lerp(this.averageOpponentHealth, newParams.averageOpponentHealth, factor),

                // 5. Spatial Arena Awareness
                newParams.corneredRecency,
                newParams.environmentalHazardRisk
        );
    }

    private static float lerp(float start, float end, float alpha) {
        return start + alpha * (end - start);
    }
}