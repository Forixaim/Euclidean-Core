package net.forixaim.euclidia.mob_ai.responses;

import net.forixaim.euclidia.entity_patch.EuclideanHumanoidMobPatch;
import net.forixaim.euclidia.mob_ai.actions.IAction;

import java.util.List;
import java.util.function.Predicate;

public record DecisionTreeNode(
        List<DecisionTreeNode> children,
        Predicate<EuclideanHumanoidMobPatch<?>> condition,
        IAction action)
{
}
