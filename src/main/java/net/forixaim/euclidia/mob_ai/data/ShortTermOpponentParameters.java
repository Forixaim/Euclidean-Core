package net.forixaim.euclidia.mob_ai.data;

import net.minecraft.world.phys.Vec3;

public record ShortTermOpponentParameters(
        float willAttack,
        float willGuard,
        float willDodge,
        float willMove,
        float willStall,

        float willCloseDistance,
        float willRetreat,
        float willFlank,

        float willCombo,
        float isVulnerable,
        float willUseSpecial,

        float isIsolated
) {
    public static ShortTermOpponentParameters fromRaw(OpponentSnapshot snapshot, Vec3 bossPosition) {
        Vec3 toBoss = bossPosition.subtract(snapshot.targetPosition());
        double distance = toBoss.length();
        Vec3 directionToBoss = distance > 0 ? toBoss.normalize() : Vec3.ZERO;
        Vec3 velocity = snapshot.targetVelocity();
        double speed = velocity.length();
        float forwardDot = speed > 0 ? (float) velocity.normalize().dot(directionToBoss) : 0.0f;
        float lateralDot = speed > 0 ? (float) Math.abs(velocity.normalize().cross(directionToBoss).length()) : 0.0f;
        float movementConfidence = Math.min((float) speed / 0.35f, 1.0f);
        float proximityFactor = distance <= 2.0 ? 1.0f : Math.max(0.0f, 1.0f - ((float)(distance - 2.0) / 6.0f));
        float willAttack = snapshot.isAttacking() ? 1.0f : Math.max(0.0f, forwardDot * movementConfidence * proximityFactor);
        if (snapshot.animPhase() == OpponentSnapshot.AnimationPhase.STARTUP || snapshot.animPhase() == OpponentSnapshot.AnimationPhase.ACTIVE) {
            willAttack = 1.0f;
        }
        float willGuard = snapshot.guarding() ? 1.0f : 0.0f;
        float willDodge = snapshot.invincible() ? 1.0f : 0.0f;
        float willStall = (1.0f - movementConfidence) * (snapshot.guarding() ? 0.8f : 1.0f);
        float willCloseDistance = forwardDot > 0.1f ? forwardDot * movementConfidence : 0.0f;
        float willRetreat       = forwardDot < -0.1f ? Math.abs(forwardDot) * movementConfidence : 0.0f;
        float willFlank         = lateralDot * movementConfidence;
        float willCombo = (snapshot.isAttacking() && snapshot.animPhase() == OpponentSnapshot.AnimationPhase.ACTIVE) ? 1.0f : 0.0f;
        float isVulnerable = snapshot.isStunned() ? 1.0f : 0.0f;
        if (snapshot.animPhase() == OpponentSnapshot.AnimationPhase.ENDLAG && snapshot.remainingPhaseTicks() > 0) {
            isVulnerable = Math.min((float) snapshot.remainingPhaseTicks() / 20.0f, 1.0f);
        }
        float willUseSpecial = (distance > 4.0 && distance < 10.0) ? willCloseDistance * 0.7f : 0.0f;
        float isIsolated = snapshot.bossInsideCollider() ? 1.0f : 0.0f;
        return new ShortTermOpponentParameters(
                willAttack, willGuard, willDodge, movementConfidence, willStall,
                willCloseDistance, willRetreat, willFlank,
                willCombo, isVulnerable, willUseSpecial, isIsolated
        );
    }

    public ShortTermOpponentParameters decay(float percent) {
        float factor = Math.clamp(percent, 0.0f, 1.0f);
        return new ShortTermOpponentParameters(
                lerp(this.willAttack, 0.0f, factor),         // Fades to non-aggressive
                lerp(this.willGuard, 0.0f, factor),          // Fades to unguarded
                lerp(this.willDodge, 0.0f, factor),          // Fades to non-rolling
                lerp(this.willMove, 0.0f, factor),           // Fades to stationary
                lerp(this.willStall, 1.0f, factor),          // Spikes to idling/stalling (Resting baseline)

                lerp(this.willCloseDistance, 0.0f, factor),  // Fades out direction vectors
                lerp(this.willRetreat, 0.0f, factor),
                lerp(this.willFlank, 0.0f, factor),

                lerp(this.willCombo, 0.0f, factor),          // They are no longer in an active chain
                lerp(this.isVulnerable, 0.0f, factor),       // They recover from stun naturally
                lerp(this.willUseSpecial, 0.0f, factor),

                lerp(this.isIsolated, 0.0f, factor)          // Spatially unlinked
        );
    }

    public ShortTermOpponentParameters update(OpponentSnapshot newData, ShortTermOpponentParameters old, Vec3 bossPos, float value) {
        ShortTermOpponentParameters fresh = ShortTermOpponentParameters.fromRaw(newData, bossPos);
        float alpha = Math.clamp(value, 0.0f, 1.0f);
        if (old == null) {
            return fresh;
        }
        return new ShortTermOpponentParameters(
                lerp(old.willAttack(), fresh.willAttack(), alpha),
                lerp(old.willGuard(), fresh.willGuard(), alpha),
                lerp(old.willDodge(), fresh.willDodge(), alpha),
                lerp(old.willMove(), fresh.willMove(), alpha),
                lerp(old.willStall(), fresh.willStall(), alpha),

                lerp(old.willCloseDistance(), fresh.willCloseDistance(), alpha),
                lerp(old.willRetreat(), fresh.willRetreat(), alpha),
                lerp(old.willFlank(), fresh.willFlank(), alpha),

                lerp(old.willCombo(), fresh.willCombo(), alpha),
                lerp(old.isVulnerable(), fresh.isVulnerable(), alpha),
                lerp(old.willUseSpecial(), fresh.willUseSpecial(), alpha),
                lerp(old.isIsolated(), fresh.isIsolated(), alpha)
        );
    }

    private float lerp(float start, float end, float alpha) {
        return start + alpha * (end - start);
    }

    public int flattenInto(float[] inputs, int startIndex) {
        inputs[startIndex]      = this.willAttack;
        inputs[startIndex + 1]  = this.willGuard;
        inputs[startIndex + 2]  = this.willDodge;
        inputs[startIndex + 3]  = this.willMove;
        inputs[startIndex + 4]  = this.willStall;
        inputs[startIndex + 5]  = this.willCloseDistance;
        inputs[startIndex + 6]  = this.willRetreat;
        inputs[startIndex + 7]  = this.willFlank;
        inputs[startIndex + 8]  = this.willCombo;
        inputs[startIndex + 9]  = this.isVulnerable;
        inputs[startIndex + 10] = this.willUseSpecial;
        inputs[startIndex + 11] = this.isIsolated;
        return startIndex + 12;
    }
}
