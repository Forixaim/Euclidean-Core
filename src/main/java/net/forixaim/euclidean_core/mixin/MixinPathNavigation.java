package net.forixaim.euclidean_core.mixin;

import net.forixaim.euclidean_core.utilities.PathfindingUtilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(PathNavigation.class)
public class MixinPathNavigation
{
    @Shadow @Final
    protected Mob mob;

    @Inject(method = "createPath(Ljava/util/Set;IZIF)Lnet/minecraft/world/level/pathfinder/Path;", at = @At("RETURN"), cancellable = true)
    private void createPath(Set<BlockPos> targets, int regionOffset, boolean offsetUp, int accuracy, float margin, CallbackInfoReturnable<Path> cir) {
        Path originalPath = cir.getReturnValue();
        if (originalPath != null && !originalPath.isDone()) {
            Mob entity = this.mob;
            Level level = entity.level();
            Path euclideanPath = PathfindingUtilities.smooth(level, originalPath, entity);
            cir.setReturnValue(euclideanPath);
        }
    }
}
