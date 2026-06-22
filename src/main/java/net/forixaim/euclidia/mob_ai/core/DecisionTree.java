package net.forixaim.euclidia.mob_ai.core;

import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Comparator;
import java.util.Stack;

public class DecisionTree
{
    private DecisionTreeNode root;
    private Stack<DecisionTreeNode> cachedStack = new Stack<>();

    public DecisionTree(DecisionTreeNode root)
    {
        this.root = root;
    }

    public void step(LivingEntityPatch<?> entity)
    {
        if (cachedStack.empty())
        {
            cachedStack.push(root);
        }
        DecisionTreeNode currentNode = cachedStack.pop();
        currentNode.action().start(entity);

        if (currentNode.comboNode())
        {
            currentNode.action().start(entity);
        }
        else if (currentNode.children().isEmpty())
        {
            currentNode.action().start(entity);
            return;
        }
        currentNode.children().stream()
                .filter(c -> c.condition().test(entity))
                .max(Comparator.comparingInt(DecisionTreeNode::priority))
                .ifPresent(cachedStack::push);
    }
}
