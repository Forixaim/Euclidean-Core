package net.forixaim.euclidia.mob_ai.data;

public record Bias(
        float attackResponsiveness,
        float guardPunishBias,
        float commandGrabPreference,
        float evasionAnticipationBias,
        float parryAnticipationBias,
        float antiStallBias,

        float aggroVengeanceWeight,
        float bullyWeight,
        float tankRespectWeight,

        float hyperArmorReliance,
        float positioningPreference,
        int reactionSpeed
) { }
