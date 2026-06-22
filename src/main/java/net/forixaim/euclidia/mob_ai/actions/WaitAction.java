package net.forixaim.euclidia.mob_ai.actions;

import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.function.Predicate;

public class WaitAction implements IAction
{
    private int ticks;
    private Predicate<LivingEntityPatch<?>> condition;

    public WaitAction(int ticks)
    {
        this.ticks = ticks;
    }

    @Override
    public void start(LivingEntityPatch<?> entity)
    {

    }

    @Override
    public void tick(LivingEntityPatch<?> entity)
    {
        ticks--;
        if (ticks <= 0 || condition.test(entity)) {
            stop(entity);
        }
    }

    @Override
    public void stop(LivingEntityPatch<?> entity)
    {
        ticks = 0;
    }
}
