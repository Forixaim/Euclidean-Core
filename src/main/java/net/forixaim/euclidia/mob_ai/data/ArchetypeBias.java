package net.forixaim.euclidia.mob_ai.data;

public record ArchetypeBias(
        float attackResponsiveness,
        float guardPunishBias,
        float commandGrabPreference,
        float evasionAnticipationBias,
        float parryAnticipationBias,
        float antiStallBias,
        float zoningPreference,

        float aggroVengeanceWeight,
        float bullyWeight,
        float tankRespectWeight,

        float hyperArmorReliance,
        float positioningPreference,
        float battlePatience,
        int reactionSpeed
)
{
}
