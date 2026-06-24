package net.forixaim.euclidia.mob_ai.data;

public record BossSnapshot(
        GeneralParameters general,
        LongTermOpponentParameters longTermOpponent,
        ShortTermOpponentParameters shortTermOpponent,
        ShortTermBossParameters shortTermBoss
) {
    public float[] flattenSnapshotToVector() {
        GeneralParameters general = this.general();
        ShortTermBossParameters shortTerm = this.shortTermBoss;
        ShortTermOpponentParameters opponentShort = this.shortTermOpponent;
        LongTermOpponentParameters opponentLong = this.longTermOpponent;
        return new float[] {
                general.relativeHealthRemaining(),
                general.combatExhaustion(),
                general.predictabilityScore(),
                general.targetEvadeRatio(),
                general.tradeSuccessRate(),
                (float) general.totalActiveAggressors(),
                general.averageOpponentHealth(),
                general.corneredRecency(),
                general.groupAttackConfidence(),
                shortTerm.recentDamageSustained(),
                shortTerm.isPoiseProtected(),
                shortTerm.animationRecovery(),
                shortTerm.currentExecutionTicksNormalized(),
                shortTerm.targetTrackingError(),
                opponentShort.willAttack(),
                opponentShort.willGuard(),
                opponentShort.willDodge(),
                opponentShort.willMove(),
                opponentShort.willStall(),
                opponentShort.willCloseDistance(),
                opponentShort.willRetreat(),
                opponentShort.willFlank(),
                opponentShort.willCombo(),
                opponentShort.isVulnerable(),
                opponentShort.willUseSpecial(),
                opponentShort.isIsolated(),
                opponentLong.aggressiveTendency(),
                opponentLong.defensiveTendency(),
                opponentLong.evasiveTendency(),
                opponentLong.panicFactor(),
                opponentLong.predictability()
        };
    }
}
