package net.forixaim.euclidia.mob_ai.actions;

import net.forixaim.euclidia.registry.MatrixWeights;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;

public class ActionBuilder<T extends IAction>
{
    private final T action;
    // ────────────────────────────────────────────────────────
    // ZONE I: GLOBAL ARENA MACRO PARAMETERS (9 Fields)
    // ────────────────────────────────────────────────────────
    private float relativeHealthRemaining    = 0.0f;
    private float combatExhaustion           = -0.15f; // Safe fallback: discourage actions when tired
    private float predictabilityScore        = 0.0f;
    private float targetEvadeRatio           = 0.0f;
    private float tradeSuccessRate           = 0.0f;
    private float totalActiveAggressors      = 0.0f;
    private float averageOpponentHealth      = 0.0f;
    private float spacingPressure            = 0.0f;
    private float cornerEscapeConfidence     = 0.0f;
    private float environmentalHazardRisk    = 0.0f;

    // ────────────────────────────────────────────────────────
    // ZONE II: INSTANTANEOUS BOSS REFLEXES (5 Fields)
    // ────────────────────────────────────────────────────────
    private float recentDamageSustained      = 0.0f;
    private float isPoiseProtected           = 0.0f;
    public float cornered                    = 0.0f;
    private float animationRecovery          = -0.90f; // Safe fallback: block actions during frame endlag
    private float currentExecutionTicksNormalized = -0.30f; // Safe fallback: don't loop the same move mid-swing
    private float targetTrackingError        = 0.0f;
    private float actionSpamTendency         = 0.0f;
    private float lookingAtTarget            = 0.0f;
    private float distanceToTarget           = 0.0f;

    // ────────────────────────────────────────────────────────
    // ZONE III: INSTANTANEOUS TARGET INTENTS (12 Fields)
    // ────────────────────────────────────────────────────────
    private float willAttack                 = 0.0f;
    private float willGuard                  = 0.0f;
    private float willDodge                  = 0.0f;
    private float willMove                   = 0.0f;
    private float willStall                  = 0.0f;
    private float willCloseDistance          = 0.0f;
    private float willRetreat                = 0.0f;
    private float willFlank                  = 0.0f;
    private float projectileSpamTendency     = 0.0f;
    private float willCombo                  = 0.0f;
    private float isVulnerable               = 0.0f;
    private float willUseSpecial             = 0.0f;
    private float isIsolated                 = 0.0f;

    // ────────────────────────────────────────────────────────
    // ZONE IV: TARGET COMBAT PLAYSTYLE TRENDS (5 Fields)
    // ────────────────────────────────────────────────────────
    private float aggressiveTendency         = 0.0f;
    private float defensiveTendency          = 0.0f;
    private float evasiveTendency            = 0.0f;
    private float panicFactor                = 0.0f;
    private float projectileReliance         = 0.0f;
    private float predictability             = 0.0f;


    public ActionBuilder(T action)
    {
        this.action = action;
    }

    public T build(ResourceLocation location) {
        // 1. Compile all local fields into a position-perfect 31D array
        float[] compiledWeights = new float[] {
                // === ZONE I: GLOBAL ARENA MACRO PARAMETERS (9 Columns) ===
                this.relativeHealthRemaining,
                this.combatExhaustion,
                this.predictabilityScore,
                this.targetEvadeRatio,
                this.tradeSuccessRate,
                this.totalActiveAggressors,
                this.averageOpponentHealth,
                this.spacingPressure,
                this.cornerEscapeConfidence,
                this.environmentalHazardRisk,

                // === ZONE II: INSTANTANEOUS BOSS REFLEXES (5 Columns) ===
                this.recentDamageSustained,
                this.isPoiseProtected,
                this.cornered,
                this.animationRecovery,
                this.currentExecutionTicksNormalized,
                this.targetTrackingError,
                this.actionSpamTendency,
                this.lookingAtTarget,
                this.distanceToTarget,

                // === ZONE III: INSTANTANEOUS TARGET INTENTS (12 Columns) ===
                this.willAttack,
                this.willGuard,
                this.willDodge,
                this.willMove,
                this.willStall,
                this.willCloseDistance,
                this.willRetreat,
                this.willFlank,
                this.projectileSpamTendency,
                this.willCombo,
                this.isVulnerable,
                this.willUseSpecial,
                this.isIsolated,

                // === ZONE IV: TARGET COMBAT PLAYSTYLE TRENDS (5 Columns) ===
                this.aggressiveTendency,
                this.defensiveTendency,
                this.evasiveTendency,
                this.panicFactor,
                this.projectileReliance,
                this.predictability
        };

        MatrixWeights.register(location, compiledWeights);
        return this.action;
    }

    public ActionBuilder<T> passive(Operation op) {
        return this.advanced()
                .spacingPressure(op, 0.70f)
                .combatExhaustion(op, 0.40f)
                .willAttack(op, -0.50f)
                .herding();
    }

    public ActionBuilder<T> berserk(Operation op) {
        return this.advanced()
                .combatExhaustion(op, 0.90f)      // Ignores exhaustion constraints entirely
                .isPoiseProtected(op, 0.85f)     // Heavily relies on hyper-armor trade mechanics
                .tradeSuccessRate(op, 0.70f)     // Strongly scales with trading scenarios
                .recentDamageSustained(op, 0.80f) // Scales activation higher the more it gets hit
                .defensiveTendency(op, -0.60f)   // Disregards cautious opponents
                .herding();
    }

    public ActionBuilder<T> aggressive(Operation op) {
        return this.advanced()
                .combatExhaustion(op, -0.60f)
                .willRetreat(op, 0.80f)
                .willCloseDistance(op, 0.70f)
                .herding();
    }

    public ActionBuilder<T> closeIn(Operation op)
    {
        return this.advanced()
                .distanceToTarget(op, 0.50f)
                .herding();
    }

    public ActionBuilder<T> zoning(Operation op) {
        return this.advanced()
                .willCloseDistance(op, 0.90f)    // ★ PUNISH TARGETS RUSHING INWARD INTO RANGE ★
                .willMove(op, 0.40f)             // Great tool to throw out when players are repositioning
                .aggressiveTendency(op, 0.55f)  // Favored against highly relentless, aggressive players
                .actionSpamTendency(op, 0.50f)
                .distanceToTarget(op, 0.50f)
                .spacingPressure(op, -0.40f)    // Suppressed if the player is already hugging our collision box
                .targetTrackingError(op, -0.60f) // Strict spatial alignment required (don't whiff long range)
                .herding();
    }

    public ActionBuilder<T> lookingAtTarget(Operation op, boolean val) {
        return this.advanced()
                .lookingAtTarget(op, val ? 1.0f : 0.0f).herding();
    }

    // 2. Core algebraic implementation
    public ActionBuilder<T> evasive(Operation op) {
        return this.advanced()
                .willAttack(op, 0.85f)          // ★ HIGH TRIGGER IF PLAYER IS ACTIVELY SWINGING ★
                .spacingPressure(op, 0.75f)     // Highly active if pinned or crowded
                .targetTrackingError(op, 0.60f) // Excellent choice if the player has flank-rolled us
                .willCombo(op, 0.50f)           // Great fallback to break away from player combo strings
                .combatExhaustion(op, 0.20f)    // Accessible even when the boss's stamina is low
                .herding();
    }

    public ActionBuilder<T> neutral(Operation op) {
        return this.advanced()
                .combatExhaustion(op, -0.10f)    // Mild stamina constraint
                .willMove(op, 0.40f)             // Great when the player is just positioning
                .targetTrackingError(op, -0.30f) // Requires decent structural alignment, but not perfect
                .predictability(op, -0.20f)      // Slightly preferred if the player is erratic/unpredictable
                .herding();
    }

    public ActionBuilder<T> opportunist(Operation op) {
        return this.advanced()
                .isVulnerable(op, 0.95f)
                .willDodge(op, 0.50f)
                .recentDamageSustained(op, -0.40f)
                .herding();
    }

    public ActionBuilder<T> skirmisher(Operation op) {
        return this.advanced()
                .willFlank(op, 0.80f)            // Activates if player flank attempts are high
                .targetTrackingError(op, 0.75f)  // Great for catching opponents missing their tracking frames
                .targetEvadeRatio(op, 0.40f)     // Scales up against highly agile, slippery targets
                .willCloseDistance(op, -0.30f)   // Prefers dynamic movement over linear approaches
                .herding();
    }

    public ActionBuilder<T> punisher(Operation op) {
        return this.advanced()
                .willGuard(op, 0.95f)            // ★ OBLITERATE OPPONENT BLOCKS/GUARDS ★
                .willStall(op, 0.65f)            // Punishes players who try to sit passively
                .predictabilityScore(op, 0.50f)  // Highly active if the target behaves predictably
                .defensiveTendency(op, 0.80f)    // Tailored to crush defensive archetypes
                .herding();
    }

    public ActionBuilder<T> desperation(Operation op) {
        return this.advanced()
                .relativeHealthRemaining(op, -0.90f) // ★ SCALES UP MASSIVELY AS HEALTH PLUMMETS ★
                .spacingPressure(op, 0.80f)         // Activates when pinned or overwhelmed
                .cornerEscapeConfidence(op, -0.70f) // Triggers when spatial escape odds look grim
                .panicFactor(op, 0.60f)             // Punishes players who are also panic-mashing
                .herding();
    }

    public ActionBuilder<T> ganker(Operation op) {
        return this.advanced()
                .totalActiveAggressors(op, 0.85f)   // Scales up dramatically if fighting multiple players
                .isIsolated(op, -0.75f)             // Discourages usage if a player is completely alone
                .averageOpponentHealth(op, -0.30f)  // Prefers sweeping through lower health groups
                .herding();
    }

    public Advanced advanced() {
        return new Advanced();
    }

    public class Advanced {
        public Advanced relativeHealthRemaining(Operation op, float val) {
            ActionBuilder.this.relativeHealthRemaining = op.apply(ActionBuilder.this.relativeHealthRemaining, val);
            return this;
        }

        public Advanced environmentalHazardRisk(Operation op, float val)
        {
            ActionBuilder.this.environmentalHazardRisk = op.apply(ActionBuilder.this.environmentalHazardRisk, val);
            return this;
        }

        public Advanced distanceToTarget(Operation op, float val)
        {
            ActionBuilder.this.distanceToTarget = op.apply(ActionBuilder.this.distanceToTarget, val);
            return this;
        }

        public Advanced lookingAtTarget(Operation op, float val) {
            ActionBuilder.this.lookingAtTarget = op.apply(ActionBuilder.this.lookingAtTarget, val);
            return this;
        }

        public Advanced cornered(Operation op, float val) {
            ActionBuilder.this.cornered = op.apply(ActionBuilder.this.cornered, val);
            return this;
        }

        public Advanced projectileReliance(Operation op, float val)
        {
            ActionBuilder.this.projectileReliance = op.apply(ActionBuilder.this.projectileReliance, val);
            return this;
        }

        public Advanced projectileSpamTendency(Operation op, float val)
        {
            ActionBuilder.this.projectileSpamTendency = op.apply(ActionBuilder.this.projectileSpamTendency, val);
            return this;
        }

        public Advanced actionSpamTendency(Operation op, float val)
        {
            ActionBuilder.this.actionSpamTendency = op.apply(ActionBuilder.this.actionSpamTendency, val);
            return this;
        }

        public Advanced combatExhaustion(Operation op, float val) {
            ActionBuilder.this.combatExhaustion = op.apply(ActionBuilder.this.combatExhaustion, val);
            return this;
        }

        public Advanced predictabilityScore(Operation op, float val) {
            ActionBuilder.this.predictabilityScore = op.apply(ActionBuilder.this.predictabilityScore, val);
            return this;
        }

        public Advanced targetEvadeRatio(Operation op, float val) {
            ActionBuilder.this.targetEvadeRatio = op.apply(ActionBuilder.this.targetEvadeRatio, val);
            return this;
        }

        public Advanced tradeSuccessRate(Operation op, float val) {
            ActionBuilder.this.tradeSuccessRate = op.apply(ActionBuilder.this.tradeSuccessRate, val);
            return this;
        }

        public Advanced totalActiveAggressors(Operation op, float val) {
            ActionBuilder.this.totalActiveAggressors = op.apply(ActionBuilder.this.totalActiveAggressors, val);
            return this;
        }

        public Advanced averageOpponentHealth(Operation op, float val) {
            ActionBuilder.this.averageOpponentHealth = op.apply(ActionBuilder.this.averageOpponentHealth, val);
            return this;
        }

        public Advanced spacingPressure(Operation op, float val) {
            ActionBuilder.this.spacingPressure = op.apply(ActionBuilder.this.spacingPressure, val);
            return this;
        }

        public Advanced cornerEscapeConfidence(Operation op, float val) {
            ActionBuilder.this.cornerEscapeConfidence = op.apply(ActionBuilder.this.cornerEscapeConfidence, val);
            return this;
        }

        public Advanced recentDamageSustained(Operation op, float val) {
            ActionBuilder.this.recentDamageSustained = op.apply(ActionBuilder.this.recentDamageSustained, val);
            return this;
        }

        public Advanced isPoiseProtected(Operation op, float val) {
            ActionBuilder.this.isPoiseProtected = op.apply(ActionBuilder.this.isPoiseProtected, val);
            return this;
        }

        public Advanced animationRecovery(Operation op, float val) {
            ActionBuilder.this.animationRecovery = op.apply(ActionBuilder.this.animationRecovery, val);
            return this;
        }

        public Advanced currentExecutionTicksNormalized(Operation op, float val) {
            ActionBuilder.this.currentExecutionTicksNormalized = op.apply(ActionBuilder.this.currentExecutionTicksNormalized, val);
            return this;
        }

        public Advanced targetTrackingError(Operation op, float val) {
            ActionBuilder.this.targetTrackingError = op.apply(ActionBuilder.this.targetTrackingError, val);
            return this;
        }

        public Advanced willAttack(Operation op, float val) {
            ActionBuilder.this.willAttack = op.apply(ActionBuilder.this.willAttack, val);
            return this;
        }

        public Advanced willGuard(Operation op, float val) {
            ActionBuilder.this.willGuard = op.apply(ActionBuilder.this.willGuard, val);
            return this;
        }

        public Advanced willDodge(Operation op, float val) {
            ActionBuilder.this.willDodge = op.apply(ActionBuilder.this.willDodge, val);
            return this;
        }

        public Advanced willMove(Operation op, float val) {
            ActionBuilder.this.willMove = op.apply(ActionBuilder.this.willMove, val);
            return this;
        }

        public Advanced willStall(Operation op, float val) {
            ActionBuilder.this.willStall = op.apply(ActionBuilder.this.willStall, val);
            return this;
        }

        public Advanced willCloseDistance(Operation op, float val) {
            ActionBuilder.this.willCloseDistance = op.apply(ActionBuilder.this.willCloseDistance, val);
            return this;
        }

        public Advanced willRetreat(Operation op, float val) {
            ActionBuilder.this.willRetreat = op.apply(ActionBuilder.this.willRetreat, val);
            return this;
        }

        public Advanced willFlank(Operation op, float val) {
            ActionBuilder.this.willFlank = op.apply(ActionBuilder.this.willFlank, val);
            return this;
        }

        public Advanced willCombo(Operation op, float val) {
            ActionBuilder.this.willCombo = op.apply(ActionBuilder.this.willCombo, val);
            return this;
        }

        public Advanced isVulnerable(Operation op, float val) {
            ActionBuilder.this.isVulnerable = op.apply(ActionBuilder.this.isVulnerable, val);
            return this;
        }

        public Advanced willUseSpecial(Operation op, float val) {
            ActionBuilder.this.willUseSpecial = op.apply(ActionBuilder.this.willUseSpecial, val);
            return this;
        }

        public Advanced isIsolated(Operation op, float val) {
            ActionBuilder.this.isIsolated = op.apply(ActionBuilder.this.isIsolated, val);
            return this;
        }

        public Advanced aggressiveTendency(Operation op, float val) {
            ActionBuilder.this.aggressiveTendency = op.apply(ActionBuilder.this.aggressiveTendency, val);
            return this;
        }

        public Advanced defensiveTendency(Operation op, float val) {
            ActionBuilder.this.defensiveTendency = op.apply(ActionBuilder.this.defensiveTendency, val);
            return this;
        }

        public Advanced evasiveTendency(Operation op, float val) {
            ActionBuilder.this.evasiveTendency = op.apply(ActionBuilder.this.evasiveTendency, val);
            return this;
        }

        public Advanced panicFactor(Operation op, float val) {
            ActionBuilder.this.panicFactor = op.apply(ActionBuilder.this.panicFactor, val);
            return this;
        }

        public Advanced predictability(Operation op, float val) {
            ActionBuilder.this.predictability = op.apply(ActionBuilder.this.predictability, val);
            return this;
        }

        public ActionBuilder<T> herding() {
            return ActionBuilder.this;
        }
    }

    public enum Operation {
        SET((current, incoming) -> incoming),
        ADD(Float::sum),
        SUB((current, incoming) -> current - incoming),
        MUL((current, incoming) -> current * incoming);

        private final BiFunction<Float, Float, Float> math;

        Operation(BiFunction<Float, Float, Float> math) {
            this.math = math;
        }

        public float apply(float current, float incoming) {
            return this.math.apply(current, incoming);
        }
    }
}
