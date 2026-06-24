package net.forixaim.euclidia.mob_ai.data;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record OpponentSnapshot(
        Vec3 targetPosition,
        Vec3 targetVelocity,
        boolean guarding,
        boolean invincible,
        boolean isAttacking,
        boolean isStunned,
        boolean airborne,
        AnimationPhase animPhase,
        int remainingPhaseTicks,
        boolean bossInsideCollider
) {
    public enum AnimationPhase implements StringRepresentable {
        NONE,
        STARTUP,
        ACTIVE,
        ENDLAG;
        @Override
        public @NotNull String getSerializedName() {
            return this.name();
        }
    }


}


