package net.forixaim.euclidia.mob_ai.core;

import net.forixaim.euclidia.mob_ai.data.GeneralParameters;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class GeneralUpdater {

    public static GeneralParameters updateMacroState(
            LivingEntityPatch<?> bossPatch,
            AIController controller,
            GeneralParameters currentGeneral,
            ActionHistory history
    ) {
        LivingEntity boss = bossPatch.getOriginal();
        float relativeHealth = boss.getHealth() / Math.max(1.0f, boss.getMaxHealth());
        float instantaneousExhaustion = currentGeneral.combatExhaustion();
        float instantaneousPredictability = history.calculateSequenceRepetitionScore();
        float instantaneousEvadeRatio = currentGeneral.targetEvadeRatio();
        float instantaneousTradeRate = currentGeneral.tradeSuccessRate();
        int totalPlayersEngaged = controller.getTrackingEntities().size();

        float averageThreatHealth = controller.getAverageHealthPercentage();

        float healthLost = 1.0f - relativeHealth;
        float crowdMultiplier = Math.max(0.2f, 1.0f - ((totalPlayersEngaged - 1) * 0.15f));
        float instantaneousConfidence = Math.clamp((1.0f - healthLost) * crowdMultiplier, 0.0f, 1.0f);

        Vec3 lookVec = boss.getLookAngle();
        boolean isPinned = !boss.level().noCollision(boss.getBoundingBox().move(lookVec.scale(-1.0)));
        float instantaneousSpacingPressure = isPinned ? 1.0f : 0.0f;

        float instantaneousHazardRisk = (boss.isInLava() || boss.isOnFire()) ? 1.0f : 0.0f;

        GeneralParameters freshSnapshot = new GeneralParameters(
                relativeHealth,
                //instantaneousPostureFraction,

                instantaneousExhaustion,
                instantaneousPredictability,

                instantaneousEvadeRatio,
                instantaneousTradeRate,

                totalPlayersEngaged,
                instantaneousConfidence,
                averageThreatHealth,

                instantaneousSpacingPressure,
                instantaneousHazardRisk
        );

        // Blend the old history state with the modern snapshot over a 10% movement lerp
        float blendFactor = 0.10f;
        return currentGeneral.merge(freshSnapshot, blendFactor);
    }
}