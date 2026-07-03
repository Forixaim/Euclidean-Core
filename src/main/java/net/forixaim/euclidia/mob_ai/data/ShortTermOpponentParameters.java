package net.forixaim.euclidia.mob_ai.data;

import net.minecraft.world.phys.Vec3;


import org.joml.Math;

public class ShortTermOpponentParameters {

    public float willAttack;
    public float willGuard;
    public float willDodge;
    public float willMove;
    public float willStall;

    public float willCloseDistance;
    public float willRetreat;
    public float willFlank;
    public float projectileSpamTendency;

    public float willCombo;
    public float isVulnerable;
    public float willUseSpecial;
    public float isIsolated;

    // Full Constructor
    public ShortTermOpponentParameters(
            float willAttack, float willGuard, float willDodge, float willMove, float willStall,
            float willCloseDistance, float willRetreat, float willFlank, float projectileSpamTendency,
            float willCombo, float isVulnerable, float willUseSpecial, float isIsolated
    ) {
        this.willAttack = willAttack;
        this.willGuard = willGuard;
        this.willDodge = willDodge;
        this.willMove = willMove;
        this.willStall = willStall;
        this.willCloseDistance = willCloseDistance;
        this.willRetreat = willRetreat;
        this.willFlank = willFlank;
        this.projectileSpamTendency = projectileSpamTendency;
        this.willCombo = willCombo;
        this.isVulnerable = isVulnerable;
        this.willUseSpecial = willUseSpecial;
        this.isIsolated = isIsolated;
    }

    /**
     * Instantiates a completely fresh parameter set.
     * Best used only once when the boss first acquires a target.
     */
    public static ShortTermOpponentParameters createFresh(OpponentSnapshot snapshot, Vec3 bossPosition) {
        ShortTermOpponentParameters params = new ShortTermOpponentParameters(
                0,0,0,0,0,0,0,0,0,0,0,0,0 // Initialize blanks
        );
        // Force an immediate update with an alpha of 1.0 (instant override)
        params.update(snapshot, bossPosition, 1.0f);
        return params;
    }

    /**
     * Mutates the current parameters in-place, fading them toward baseline.
     * Zero memory allocations.
     */
    public void decay(float percent) {
        float factor = Math.clamp(percent, 0.0f, 1.0f);

        this.willAttack             = lerp(this.willAttack, 0.0f, factor);
        this.willGuard              = lerp(this.willGuard, 0.0f, factor);
        this.willDodge              = lerp(this.willDodge, 0.0f, factor);
        this.willMove               = lerp(this.willMove, 0.0f, factor);
        this.willStall              = lerp(this.willStall, 1.0f, factor); // Spikes to idling baseline

        this.willCloseDistance      = lerp(this.willCloseDistance, 0.0f, factor);
        this.willRetreat            = lerp(this.willRetreat, 0.0f, factor);
        this.willFlank              = lerp(this.willFlank, 0.0f, factor);
        this.projectileSpamTendency = lerp(this.projectileSpamTendency, 0.0f, factor);

        this.willCombo              = lerp(this.willCombo, 0.0f, factor);
        this.isVulnerable           = lerp(this.isVulnerable, 0.0f, factor);
        this.willUseSpecial         = lerp(this.willUseSpecial, 0.0f, factor);
        this.isIsolated             = lerp(this.isIsolated, 0.0f, factor);
    }

    /**
     * Updates the current object's parameters smoothly based on fresh snapshot data.
     * Replaces the old immutable `update` method.
     */
    public void update(OpponentSnapshot snapshot, Vec3 bossPosition, float alpha) {
        float safeAlpha = Math.clamp(alpha, 0.0f, 1.0f);

        // --- 1. Calculate fresh raw values ---
        Vec3 toBoss = bossPosition.subtract(snapshot.targetPosition());
        double distance = toBoss.length();
        Vec3 directionToBoss = distance > 0 ? toBoss.normalize() : Vec3.ZERO;

        Vec3 velocity = snapshot.targetVelocity();
        double speed = velocity.length();

        float forwardDot = speed > 0 ? (float) velocity.normalize().dot(directionToBoss) : 0.0f;
        float lateralDot = speed > 0 ? (float) java.lang.Math.abs(velocity.normalize().cross(directionToBoss).length()) : 0.0f;

        float movementConfidence = java.lang.Math.min((float) speed / 0.35f, 1.0f);
        float proximityFactor = distance <= 2.0 ? 1.0f : java.lang.Math.max(0.0f, 1.0f - ((float)(distance - 2.0) / 6.0f));

        float freshWillAttack = snapshot.isAttacking() ? 1.0f : java.lang.Math.max(0.0f, forwardDot * movementConfidence * proximityFactor);
        if (snapshot.animPhase() == OpponentSnapshot.AnimationPhase.STARTUP || snapshot.animPhase() == OpponentSnapshot.AnimationPhase.ACTIVE) {
            freshWillAttack = 1.0f;
        }

        float freshWillGuard = snapshot.guarding() ? 1.0f : 0.0f;
        float freshWillDodge = snapshot.invincible() ? 1.0f : 0.0f;
        float freshWillStall = (1.0f - movementConfidence) * (snapshot.guarding() ? 0.8f : 1.0f);

        float freshWillCloseDistance = forwardDot > 0.1f ? forwardDot * movementConfidence : 0.0f;
        float freshWillRetreat       = forwardDot < -0.1f ? java.lang.Math.abs(forwardDot) * movementConfidence : 0.0f;
        float freshWillFlank         = lateralDot * movementConfidence;

        float freshWillCombo = (snapshot.isAttacking() && snapshot.animPhase() == OpponentSnapshot.AnimationPhase.ACTIVE) ? 1.0f : 0.0f;

        float freshIsVulnerable = snapshot.isStunned() ? 1.0f : 0.0f;
        if (snapshot.animPhase() == OpponentSnapshot.AnimationPhase.ENDLAG && snapshot.remainingPhaseTicks() > 0) {
            freshIsVulnerable = java.lang.Math.min((float) snapshot.remainingPhaseTicks() / 20.0f, 1.0f);
        }

        float freshWillUseSpecial = (distance > 4.0 && distance < 10.0) ? freshWillCloseDistance * 0.7f : 0.0f;
        float freshIsIsolated = snapshot.bossInsideCollider() ? 1.0f : 0.0f;

        // --- 2. Lerp the fresh values directly into THIS object's state ---
        this.willAttack        = lerp(this.willAttack, freshWillAttack, safeAlpha);
        this.willGuard         = lerp(this.willGuard, freshWillGuard, safeAlpha);
        this.willDodge         = lerp(this.willDodge, freshWillDodge, safeAlpha);
        this.willMove          = lerp(this.willMove, movementConfidence, safeAlpha);
        this.willStall         = lerp(this.willStall, freshWillStall, safeAlpha);

        this.willCloseDistance = lerp(this.willCloseDistance, freshWillCloseDistance, safeAlpha);
        this.willRetreat       = lerp(this.willRetreat, freshWillRetreat, safeAlpha);
        this.willFlank         = lerp(this.willFlank, freshWillFlank, safeAlpha);

        // Note: projectileSpamTendency is likely updated elsewhere (e.g., event listeners),
        // so we don't overwrite it with a fresh '0.0f' here. It retains its current value unless decayed.

        this.willCombo         = lerp(this.willCombo, freshWillCombo, safeAlpha);
        this.isVulnerable      = lerp(this.isVulnerable, freshIsVulnerable, safeAlpha);
        this.willUseSpecial    = lerp(this.willUseSpecial, freshWillUseSpecial, safeAlpha);
        this.isIsolated        = lerp(this.isIsolated, freshIsIsolated, safeAlpha);
    }

    /**
     * Flattens the 13 parameters into the neural network input array.
     */
    public int flattenInto(float[] inputs, int startIndex) {
        inputs[startIndex]      = this.willAttack;
        inputs[startIndex + 1]  = this.willGuard;
        inputs[startIndex + 2]  = this.willDodge;
        inputs[startIndex + 3]  = this.willMove;
        inputs[startIndex + 4]  = this.willStall;
        inputs[startIndex + 5]  = this.willCloseDistance;
        inputs[startIndex + 6]  = this.willRetreat;
        inputs[startIndex + 7]  = this.willFlank;
        inputs[startIndex + 8]  = this.projectileSpamTendency;
        inputs[startIndex + 9]  = this.willCombo;
        inputs[startIndex + 10] = this.isVulnerable;
        inputs[startIndex + 11] = this.willUseSpecial;
        inputs[startIndex + 12] = this.isIsolated;

        return startIndex + 13; // Adjusted for 13 variables
    }

    private float lerp(float start, float end, float alpha) {
        return start + alpha * (end - start);
    }
}
