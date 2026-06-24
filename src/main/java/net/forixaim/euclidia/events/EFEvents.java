package net.forixaim.euclidia.events;

import net.forixaim.euclidia.mixin.AttackAnimationTracker;
import net.forixaim.euclidia.utilities.IEuclideanMobPatch;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.event.types.animation.AnimationBeginEvent;
import yesman.epicfight.api.event.types.animation.AnimationEndEvent;

public class EFEvents
{
    public static void onAnimationStart(AnimationBeginEvent event)
    {
        if (event.getEntityPatch() instanceof AttackAnimationTracker tracker)
        {
            if (event.getAnimation().checkType(AttackAnimation.class))
            {
                tracker.euclidia$setAttackAnimation(event.getAnimation());
            }
        }
    }

    public static void onAnimationEnd(AnimationEndEvent event)
    {
        if (event.getEntityPatch() instanceof AttackAnimationTracker tracker)
        {
            tracker.euclidia$setAttackAnimation(null);
        }
    }
}
