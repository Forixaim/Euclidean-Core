package net.forixaim.euclidia.mob_ai.data;

public record BossSnapshot(
        GeneralParameters general,
        LongTermOpponentParameters longTermOpponent,
        ShortTermOpponentParameters shortTermOpponent,
        ShortTermBossParameters shortTermBoss
) {
    public static final int INPUT_SIZE = 38;
    public float[] flattenSnapshotToVector() {
        int index = 0;
        float[] networkInputVector = new float[INPUT_SIZE];
        index = this.general.flattenInto(networkInputVector, index);
        index = this.shortTermBoss.flattenInto(networkInputVector, index);
        index = this.shortTermOpponent.flattenInto(networkInputVector, index);
        this.longTermOpponent.flattenInto(networkInputVector, index);
        return networkInputVector;
    }
}
