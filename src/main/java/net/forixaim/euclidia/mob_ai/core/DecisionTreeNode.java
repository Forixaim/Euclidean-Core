package net.forixaim.euclidia.mob_ai.core;

import net.forixaim.euclidia.entity_patch.EuclideanHumanoidMobPatch;
import net.forixaim.euclidia.mob_ai.actions.IAction;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;
import java.util.function.Predicate;

public record DecisionTreeNode(
        List<DecisionTreeNode> children,
        Predicate<LivingEntityPatch<?>> condition,
        byte priority,
        IAction action, boolean comboNode)
{
}
