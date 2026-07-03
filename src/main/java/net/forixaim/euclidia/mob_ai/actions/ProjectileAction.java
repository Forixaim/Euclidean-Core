package net.forixaim.euclidia.mob_ai.actions;

public abstract class ProjectileAction implements IAction
{
    @Override
    public boolean isZoning()
    {
        return true;
    }
}
