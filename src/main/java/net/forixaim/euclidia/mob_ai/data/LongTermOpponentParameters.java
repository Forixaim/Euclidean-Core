package net.forixaim.euclidia.mob_ai.data;

public record LongTermOpponentParameters(
        float aggressiveTendency,
        float defensiveTendency,
        float evasiveTendency,
        float panicFactor,
        float predictability
) {
    public LongTermOpponentParameters applyIntermissionDecay(float retentionFactor) {
        float alpha = Math.clamp(retentionFactor, 0.0f, 1.0f);
        return new LongTermOpponentParameters(
                lerp(0.5f, this.aggressiveTendency, alpha),
                lerp(0.5f, this.defensiveTendency, alpha),
                lerp(0.5f, this.panicFactor, alpha),
                lerp(0.5f, this.predictability, alpha),
                lerp(0.5f, this.evasiveTendency, alpha)
        );
    }

    private float lerp(float start, float end, float alpha) {
        return start + alpha * (end - start);
    }
}