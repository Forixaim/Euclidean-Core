package net.forixaim.euclidean_core.mixin;

import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Path.class)
public interface PathAccessor
{
    @Accessor("nodes")
    List<Node> getNodes();
}
