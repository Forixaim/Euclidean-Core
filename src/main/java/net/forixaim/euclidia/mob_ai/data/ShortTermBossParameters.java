package net.forixaim.euclidia.mob_ai.data;

public record ShortTermBossParameters(
        float recentDamageSustained,

        float cornered,

        float isPoiseProtected,
        float animationRecovery,
        float currentExecutionTicksNormalized,
        float targetTrackingError
)
{
    public ShortTermBossParameters decay(float decayRate) {
        return new ShortTermBossParameters(
                Math.max(0.0f, this.recentDamageSustained - decayRate),
                this.cornered,
                this.isPoiseProtected,
                Math.max(0.0f, this.animationRecovery - decayRate),
                Math.max(0.0f, this.currentExecutionTicksNormalized - decayRate),
                Math.max(0.0f, this.targetTrackingError - decayRate)
        );
    }
}
