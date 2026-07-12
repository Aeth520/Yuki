package cn.aetheris.yuki.data.block;

public class PlayerPistonData {
    public final PistonTemplate pistonTemplate;
    public final int lastTransactionSent;

    
    int ticksOfPistonBeingAlive = 0;

    public PlayerPistonData(PistonTemplate playerPistonData, int lastTransactionSent) {
        this.pistonTemplate = playerPistonData;
        this.lastTransactionSent = lastTransactionSent;
    }

    
    
    
    public boolean tickIfGuaranteedFinished() {
        return ++ticksOfPistonBeingAlive >= 10;
    }
}
