package net.forixaim.euclidia.mob_ai.core;

import net.forixaim.euclidia.mixin.AttackAnimationTracker;
import net.forixaim.euclidia.mob_ai.actions.IAction;
import net.forixaim.euclidia.mob_ai.actions.IdleAction;
import net.forixaim.euclidia.mob_ai.actions.PlayAnimationAction;
import net.forixaim.euclidia.mob_ai.data.OpponentSnapshot;
import net.forixaim.euclidia.mob_ai.data.FrameData;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class AIController
{
    private static final int ACTION_TIMEOUT = 100;
    private final BossBrain bossBrain;
    private final LivingEntityPatch<?> original;
    private final Queue<Holder<IAction>> actionQueue = new ConcurrentLinkedQueue<>();
    private final Map<Holder<IAction>, Float> availableActions;
    private Holder<IAction> activeAction = null;
    private int ticksSinceActionStarted = -1;
    private final Predicate<LivingEntity> trackingPredicate;
    private float defaultDistanceScale = 128.0f;
    private final AtomicBoolean forceQueued = new AtomicBoolean(false);

    boolean isActionQueued()
    {
        return !actionQueue.isEmpty() || forceQueued.get();
    }

    void setForceQueued(boolean forceQueued) {
        this.forceQueued.set(forceQueued);
    }

    public LivingEntityPatch<?> getOriginal()
    {
        return original;
    }

    public Holder<IAction> getActiveAction() {
        return activeAction;
    }

    public void handleAttack(AttackResult result, Entity target, @NotNull EpicFightDamageSource damageSource, float amount)
    {
        Holder<IAction> executedAction = getActiveAction();
        if (!(executedAction instanceof PlayAnimationAction))
        {
            executedAction = bossBrain.getActionHistory().containsAnimation(damageSource.getAnimation());
        }
        if (executedAction == null)
        {
            return;
        }
        float rewardModifier;
        switch (result.resultType)
        {
            case BLOCKED -> rewardModifier = -5.0f;
            case MISSED -> rewardModifier = -7.5f;
            case SUCCESS -> {
                float baseReward = 10.0f;
                float stunBonus = 0.0f;
                float poiseDamage = damageSource.calculateImpact();
                stunBonus += poiseDamage * 1.5f;
                StunType stunType = damageSource.getStunType();
                switch (stunType) {
                    case NEUTRALIZE -> stunBonus = 15.0f;
                    case HOLD -> stunBonus = 10.0f;
                    case KNOCKDOWN -> stunBonus = 8.0f;
                    case LONG -> stunBonus = 3.5f;
                    case SHORT -> stunBonus = 1.0f;
                }
                rewardModifier = baseReward + stunBonus;
            }
            default -> rewardModifier = 0.0f;
        }

        float learningRate = 0.1f;
        float finalReward = rewardModifier;
        this.availableActions.computeIfPresent(executedAction, (holder, currentRunningValue) -> currentRunningValue + learningRate * (finalReward - currentRunningValue));
    }

    public void handleOnHit(AttackResult result, DamageSource source, float damage)
    {
        float reward = 0.0f;
        float learningRate = 0.1f;

        if (!(source instanceof EpicFightDamageSource) && result.resultType == AttackResult.ResultType.SUCCESS)
        {
            //Vanilla damage sources have 0 hitstun
            if (activeAction != null && !activeAction.value().interruptible(original, this))
            {
                reward += 5.0f;
            } else {
                reward = -3.0f;
            }
            float finalReward = reward;
            this.availableActions.computeIfPresent(activeAction, (action, value) -> finalReward * learningRate);
            return;
        }

        switch (result.resultType)
        {
            case SUCCESS -> {
                reward = -10.0f - (damage * 1.5f);
                if (activeAction != null && !activeAction.value().interruptible(original, this))
                {
                    reward -= 5.0f;
                }
            }
            case MISSED -> reward = 10.0f + (damage);
            case BLOCKED -> reward = 0.0f + (damage * 0.5f);
        }

        float finalReward = reward;
        if (activeAction == null) return;
        this.availableActions.computeIfPresent(activeAction, (action, value) -> finalReward * learningRate);
    }

    public OpponentSnapshot createSnapshot(LivingEntity opponent) {
        LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(opponent, LivingEntityPatch.class);
        boolean blocking = patch instanceof PlayerPatch<?> playerPatch && playerPatch.getHoldingSkill() instanceof GuardSkill;

        OpponentSnapshot.AnimationPhase phase = OpponentSnapshot.AnimationPhase.NONE;
        int remainingPhaseTicks = 0;
        boolean bossInsideCollider = false;

        if (patch instanceof AttackAnimationTracker tracker && tracker.euclidia$getAttackAnimation() != null
                && tracker.euclidia$getAttackAnimation().get() instanceof AttackAnimation animation) {

            Optional<AnimationPlayer> animationPlayer = patch.getAnimator().getPlayer(tracker.euclidia$getAttackAnimation());
            Optional<FrameData> frameData = FrameData.getFrameData(patch);

            if (animationPlayer.isPresent() && frameData.isPresent()) {
                float elapsedTime = animationPlayer.get().getElapsedTime();
                FrameData data = frameData.get();

                phase = data.getPhase(elapsedTime);
                remainingPhaseTicks = (int) (data.getRemainingEndlag(elapsedTime) * 20);

                AttackAnimation.JointColliderPair[] colliders = animation.getPhaseByTime(elapsedTime).getColliders();
                for (var pair : colliders) {
                    bossInsideCollider |= pair.getSecond().isCollide(original.getOriginal());
                }
            }
        }

        return new OpponentSnapshot(
                opponent.position(),
                opponent.getDeltaMovement(),
                blocking,
                opponent.isInvulnerableTo(opponent.level().damageSources().mobAttack(original.getOriginal())),
                patch.getEntityState().attacking(),
                patch.isStunned(),
                !opponent.onGround(),
                phase,
                remainingPhaseTicks,
                bossInsideCollider
        );
    }

    public int getTicksSinceActionStarted() {
        return ticksSinceActionStarted;
    }

    public Map<Holder<IAction>, Float> getFullActionMap() {
        return availableActions;
    }

    public List<Holder<IAction>> getAvailableActions() {
        return availableActions.keySet().stream().toList();
    }

    public AIController(BossBrain bossBrain, LivingEntityPatch<?> original, Predicate<LivingEntity> trackingPredicate) {
        this.bossBrain = bossBrain;
        this.original = original;
        this.availableActions = new ConcurrentHashMap<>();
        this.trackingPredicate = trackingPredicate;
    }

    public AIController(BossBrain bossBrain, LivingEntityPatch<?> original, Predicate<LivingEntity> trackingPredicate, float defaultDistanceScale) {
        this(bossBrain, original, trackingPredicate);
        this.defaultDistanceScale = defaultDistanceScale;
    }

    public BossBrain getBossBrain() {
        return bossBrain;
    }

    public synchronized void queueActionForMainThread(Holder<IAction> action) {
        this.actionQueue.offer(action);
    }

    public List<LivingEntity> getTrackingEntities() {
        AABB trackingBox = AABB.ofSize(original.getOriginal().position(), 1, 1, 1).inflate(defaultDistanceScale);
        return original.getOriginal().level().getEntitiesOfClass(LivingEntity.class, trackingBox, trackingPredicate);
    }

    public float getAverageHealthPercentage()
    {
        float totalHealthPercentage = 0.0f;
        for (LivingEntity entity : getTrackingEntities())
        {
            totalHealthPercentage += getHealthPercentage(entity);
        }
        return totalHealthPercentage / getTrackingEntities().size();
    }

    private float getHealthPercentage(LivingEntity entity)
    {
        return entity.getHealth() / entity.getMaxHealth();
    }

    public void stopAction() {
        if (activeAction != null) {
            activeAction.value().stop(original, this);
            activeAction = null;
            ticksSinceActionStarted = -1;
        }
    }

    @SafeVarargs
    public final void setAvailableActions(Holder<IAction>... actions)
    {
        availableActions.clear();
        for (Holder<IAction> action : actions)
        {
            availableActions.put(action, 0.0f);
        }
    }

    public Holder<IAction> getDefaultFallbackAction() {
        return Holder.direct(new IdleAction());
    }

    public void startAction(Holder<IAction> action) {
        if (activeAction != null) {
            stopAction();
        }
        activeAction = action;
        activeAction.value().start(original, this);
        this.bossBrain.getActionHistory().add(action);
        ticksSinceActionStarted = 0;
    }

    public void update() {
        bossBrain.baseTick(original, this);
        if (!actionQueue.isEmpty()) {
            Holder<IAction> queuedAction = actionQueue.poll();

            if (activeAction == null || activeAction.value().interruptible(original, this)) {
                startAction(queuedAction);
            }
        }

        if (activeAction != null) {
            activeAction.value().tick(original, this);
            ticksSinceActionStarted++;
            if (ticksSinceActionStarted >= ACTION_TIMEOUT) {
                stopAction();
            }
        }
    }
}
