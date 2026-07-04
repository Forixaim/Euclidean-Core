package net.forixaim.euclidia.mob_ai.core;

import net.forixaim.euclidia.Euclidia;
import net.forixaim.euclidia.mob_ai.actions.IAction;
import net.forixaim.euclidia.mob_ai.data.*;
import net.forixaim.euclidia.registry.MatrixWeights;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A neural network that tracks and analyzes the behavior of opponents in combat.
 */
public class BossBrain
{

    private static final float TEMPERATURE = 0.5f; // Dial down for optimal play, up for chaotic mixups

    private final ArchetypeBias bossArchetypeBias;
    private boolean idle;
    private boolean disabled;
    private final ActionHistory actionHistory = new ActionHistory();
    private final Map<LivingEntity, OpponentProfile> activeOpponents = new HashMap<>();
    private final GeneralParameters generalParams;
    private final ShortTermBossParameters shortTermParams;
    AtomicInteger decisionCooldown;

    public BossBrain(ArchetypeBias bossArchetypeBias) {
        this.bossArchetypeBias = bossArchetypeBias;
        this.generalParams = new GeneralParameters();
        this.shortTermParams = new ShortTermBossParameters();
        decisionCooldown = new AtomicInteger(0);
        setDisabled(false);
        setIdle(true);
    }


    public ActionHistory getActionHistory() {
        return actionHistory;
    }

    public boolean isIdle() {
        return idle;
    }

    public ArchetypeBias getBossBias() {
        return bossArchetypeBias;
    }

    private float calculateTrackingError(LivingEntityPatch<?> bossPatch, LivingEntity target) {
        LivingEntity boss = bossPatch.getOriginal();
        Vec3 lookVec = boss.getLookAngle();
        Vec2 bossLookDir = new Vec2((float) lookVec.x, (float) lookVec.z).normalized();
        Vec3 diffVec = target.position().subtract(boss.position());
        if (diffVec.horizontalDistanceSqr() < 0.001) {
            return 0.0f;
        }
        Vec2 targetDir = new Vec2((float) diffVec.x, (float) diffVec.z).normalized();
        float dotProduct = (bossLookDir.x * targetDir.x) + (bossLookDir.y * targetDir.y);
        return Math.clamp((1.0f - dotProduct) / 2.0f, 0.0f, 1.0f);
    }

    public Holder<IAction> selectBestAction(AIController controller, BossSnapshot snapshot, ArchetypeBias archetypeBias) {
        List<Holder<IAction>> movePool = controller.getAvailableActions();
        List<ActionCandidate> candidates = new ArrayList<>();
        float highestScore = -Float.MAX_VALUE;
        float[] inputs = snapshot.flattenSnapshotToVector();
        for (Holder<IAction> action : movePool) {
            if (!action.value().canExecute(controller, snapshot)) {
                continue;
            }
            float baseScore = calculateDotProduct(action, inputs);
            float finalScore = applyBiasModifications(action, baseScore, archetypeBias);
            finalScore += getFromReward(controller, action);
            finalScore -= this.actionHistory.getPenaltyFor(action.value());
            candidates.add(new ActionCandidate(action, finalScore));
            if (finalScore > highestScore) {
                highestScore = finalScore;
            }
        }
        if (!candidates.isEmpty()) {
            final float finalHighestScore = highestScore;
            return sampleStochastically(candidates.stream().filter(candidate -> within(candidate.score(), finalHighestScore - TEMPERATURE, finalHighestScore + TEMPERATURE)).toList(), highestScore);
        }
        return controller.getDefaultFallbackAction();
    }

    private boolean within(float target, float min, float max)
    {
        return target >= min && target <= max;
    }

    private float getFromReward(AIController controller, Holder<IAction> action) {
        return controller.getFullActionMap().getOrDefault(action, 0.0f);
    }

    private Holder<IAction> sampleStochastically(List<ActionCandidate> candidates, float highestScore) {

        double totalWeight = 0.0;
        List<Double> accumulatedWeights = new ArrayList<>();
        for (ActionCandidate candidate : candidates) {
            double weight = Math.exp((candidate.score() - highestScore) / TEMPERATURE);
            totalWeight += weight;
            accumulatedWeights.add(totalWeight);
        }
        double randomValue = ThreadLocalRandom.current().nextDouble() * totalWeight;
        for (int i = 0; i < candidates.size(); i++) {
            if (randomValue <= accumulatedWeights.get(i)) {
                return candidates.get(i).action();
            }
        }
        return candidates.getLast().action();
    }

    private record ActionCandidate(Holder<IAction> action, float score) {}
    private float applyBiasModifications(Holder<IAction> actionHolder, float baseScore, ArchetypeBias archetypeBias) {
        float modificationAmount = 0.0f;
        IAction action = actionHolder.value();
        if (action.isAttack())              modificationAmount += archetypeBias.attackResponsiveness();
        if (action.isGuardBreak())          modificationAmount += archetypeBias.guardPunishBias();
        if (action.isCommandGrab())         modificationAmount += archetypeBias.commandGrabPreference();
        if (action.isEvasive())             modificationAmount += archetypeBias.evasionAnticipationBias();
        if (action.isParry())               modificationAmount += archetypeBias.parryAnticipationBias();
        if (action.isGapCloser())           modificationAmount += archetypeBias.antiStallBias();
        if (action.isFastCounter())         modificationAmount += archetypeBias.aggroVengeanceWeight();
        if (action.isAggressiveChase())     modificationAmount += archetypeBias.bullyWeight();
        if (action.isPoiseProtected())      modificationAmount += archetypeBias.hyperArmorReliance();
        if (action.isPositioningOnly())     modificationAmount += archetypeBias.positioningPreference();
        if (action.isZoning())              modificationAmount += archetypeBias.zoningPreference();
        return baseScore + modificationAmount;
    }
    private float calculateDotProduct(Holder<IAction> action, float[] inputs) {
        float[] weights = MatrixWeights.getRow(action);
        if (weights == null) {
            return 0.0f;
        }
        if (weights.length != inputs.length) {
            throw new IllegalStateException(String.format(
                    "Matrix column mismatch! Action %s expected %d weights, but received %d flattened inputs.",
                    action.getClass().getSimpleName(), weights.length, inputs.length
            ));
        }
        float dotProductSum = 0.0f;
        for (int i = 0; i < inputs.length; i++) {
            dotProductSum += weights[i] * inputs[i];
        }
        return dotProductSum;
    }

    public void baseTick(LivingEntityPatch<?> boss, AIController controller)
    {
        if (disabled) return;
        controller.getTrackingEntities().forEach(op -> this.activeOpponents.put(op, new OpponentProfile(op)));
        if (this.activeOpponents.isEmpty()) return;
        LivingEntity primaryTarget = this.selectPrimaryTarget(this.bossArchetypeBias);
        if (primaryTarget == null) {
            this.idle = true;
            if (boss.getOriginal().tickCount % 20 == 0) {
                float scaledIdleDecay = 0.00384f;
                this.activeOpponents.forEach((entity, profile) -> profile.decay(scaledIdleDecay));
            }
            return;
        }
        Set<Projectile> trackingProjectiles = ProjectileManager.getTrackingProjectiles(controller);
        this.idle = false;
        OpponentProfile targetProfile = this.activeOpponents.get(primaryTarget);
        if (targetProfile == null) {
            targetProfile = new OpponentProfile(primaryTarget);
            this.activeOpponents.put(primaryTarget, targetProfile);
        }
        this.recordOpponentTick(primaryTarget, boss.getOriginal(), controller.createSnapshot(primaryTarget));
        if (boss.getOriginal().tickCount % 20 == 0) {
            this.generalParams.updateMacroState(boss, controller, shortTermParams);
        }
        float currentTrackingError = calculateTrackingError(boss, primaryTarget);
        this.shortTermParams.updateCurrentSnapshot(controller, primaryTarget, currentTrackingError);
        ShortTermOpponentParameters targetTelemetry = targetProfile.getDerivedIntent();
        LongTermOpponentParameters longTermTelemetry = targetProfile.getLongTermStats();
        BossSnapshot multiDimensionalSnapshot = new BossSnapshot(
                this.generalParams,
                longTermTelemetry,
                targetTelemetry,
                this.shortTermParams
        );

        if (this.decisionCooldown.get() > 0) {
            this.decisionCooldown.decrementAndGet();
            return;
        }

        if (controller.isActionQueued() || (controller.getActiveAction() != null && controller.getActiveAction().value().interruptible(boss, controller)))
            return;

        controller.setForceQueued(true);
        CompletableFuture.runAsync(() -> {
            try {
                Holder<IAction> bestAction = this.selectBestAction(controller, multiDimensionalSnapshot, bossArchetypeBias);

                controller.queueActionForMainThread(bestAction);

            } catch (Exception e) {
                Euclidia.LOGGER.error(e.getLocalizedMessage());
            } finally {
                controller.setForceQueued(false);
                this.decisionCooldown.set(Math.max(0, bossArchetypeBias.reactionSpeed()));
            }
        });
    }



    public void setDisabled(boolean disabled)
    {
        this.disabled = disabled;
    }

    public void setIdle(boolean idle) {
        this.idle = idle;
    }

    public enum AvoidanceType
    {
        NONE,
        INTERRUPT,
        DODGE,
        BLOCK
    }
    public static class OpponentProfile {
        private final LivingEntity entity;
        private final LongTermOpponentParameters longTermStats;
        private final ShortTermOpponentParameters derivedIntent;
        private AvoidanceType recommendedAvoidance;

        public OpponentProfile(LivingEntity entity) {
            this.entity = entity;
            this.longTermStats = new LongTermOpponentParameters();
            this.derivedIntent = new ShortTermOpponentParameters(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
            this.recommendedAvoidance = AvoidanceType.NONE;
        }

        public void decay(float decayRate) {
            this.derivedIntent.decay(decayRate);

            float longTermScale = decayRate / 3600.0f;
            this.longTermStats.applyIntermissionDecay(1.0f - longTermScale);
        }

        private boolean isFullyDecayed(ShortTermOpponentParameters params) {
            return params.willAttack < 0.01f && params.willStall > 0.99f;
        }

        public LivingEntity getEntity() { return entity; }
        public LongTermOpponentParameters getLongTermStats() { return longTermStats; }
        public ShortTermOpponentParameters getDerivedIntent() { return derivedIntent; }
        public AvoidanceType getRecommendedAvoidance() { return recommendedAvoidance; }
    }

    public void recordOpponentTick(LivingEntity opponent, LivingEntity boss, OpponentSnapshot snapshot) {
        OpponentProfile profile = activeOpponents.computeIfAbsent(opponent, k -> new OpponentProfile(opponent));

        float attentionWeight = this.calculateAttentionWeight(boss, opponent);
        profile.derivedIntent.update(snapshot, boss.position(), attentionWeight);

        profile.longTermStats.updateLongTermTendencies(profile.derivedIntent);
        profile.recommendedAvoidance = calculateAvoidance(profile.derivedIntent, snapshot);
    }

    private AvoidanceType calculateAvoidance(ShortTermOpponentParameters intent, OpponentSnapshot latest) {
        float interruptUrgency = intent.willAttack * 0.5f;
        float dodgeUrgency = (latest.bossInsideCollider() && latest.isAttacking()) ? 0.9f : 0.0f;
        float blockUrgency = latest.isAttacking() ? 0.6f : 0.0f;
        float finalInterrupt = interruptUrgency * bossArchetypeBias.attackResponsiveness();
        float finalDodge = dodgeUrgency * bossArchetypeBias.evasionAnticipationBias();
        float finalBlock = blockUrgency * bossArchetypeBias.guardPunishBias();
        float maxScore = Math.max(finalInterrupt, Math.max(finalDodge, finalBlock));
        if (maxScore < 0.45f) return AvoidanceType.NONE;
        if (maxScore == finalInterrupt) return AvoidanceType.INTERRUPT;
        if (maxScore == finalDodge) return AvoidanceType.DODGE;
        return AvoidanceType.BLOCK;
    }

    public LivingEntity selectPrimaryTarget(ArchetypeBias bossArchetypeBias) {
        LivingEntity primeTarget = null;
        float highestThreat = -1.0f;

        for (Map.Entry<LivingEntity, OpponentProfile> entry : activeOpponents.entrySet()) {
            LivingEntity opponent = entry.getKey();
            OpponentProfile profile = entry.getValue();

            if (opponent == null || !opponent.isAlive()) continue;

            float threat = getThreat(bossArchetypeBias, profile);
            if (threat > highestThreat) {
                highestThreat = threat;
                primeTarget = opponent;
            }
        }
        return primeTarget;
    }

    private float calculateAttentionWeight(LivingEntity boss, LivingEntity target) {
        if (!boss.hasLineOfSight(target)) return 0.15f; // Sight-break tracing memory factor

        double distance = boss.distanceTo(target);
        float distanceFactor = distance > 4.0 ? Math.max(0.2f, 1.0f - ((float)(distance - 4.0) / 20.0f)) : 1.0f;

        Vec3 bossLookDir = boss.getViewVector(1.0f).normalize();
        Vec3 toTargetVector = target.position().subtract(boss.position()).normalize();
        float lookDot = (float) bossLookDir.dot(toTargetVector);
        float fovFactor = lookDot < 0.9f ? Math.clamp((lookDot + 1.0f) / 2.0f, 0.25f, 1.0f) : 1.0f;

        return Math.clamp(distanceFactor * fovFactor, 0.0f, 1.0f);
    }

    private static float getThreat(ArchetypeBias bossArchetypeBias, OpponentProfile profile) {
        LongTermOpponentParameters longTerm = profile.getLongTermStats();
        ShortTermOpponentParameters shortTerm = profile.getDerivedIntent();
        float baseThreat = longTerm.aggressiveTendency * 2.0f;
        float vengeanceThreat = shortTerm.willAttack * bossArchetypeBias.aggroVengeanceWeight();
        float threat = baseThreat + vengeanceThreat;
        if (shortTerm.isVulnerable > 0.5f) {
            threat += (shortTerm.isVulnerable * 1.5f * bossArchetypeBias.bullyWeight());
        }
        threat += (shortTerm.willCloseDistance * 0.5f);
        return threat;
    }

    private void onActionExecute(Holder<IAction> action)
    {
        actionHistory.add(action);

    }



    public void forgetOpponent(LivingEntity opponent) {
        activeOpponents.remove(opponent);
    }
}
