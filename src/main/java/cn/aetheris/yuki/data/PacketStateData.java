package cn.aetheris.yuki.data;

import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.util.Vector3d;
import lombok.Getter;



public class PacketStateData {
    public boolean packetPlayerOnGround = false;
    public boolean lastPacketWasTeleport = false;
    public boolean cancelDuplicatePacket, lastPacketWasOnePointSeventeenDuplicate = false;
    public boolean lastTransactionPacketWasValid = false;
    public int lastSlotSelected;
    public InteractionHand eatingHand = InteractionHand.MAIN_HAND;
    public long lastRiptide = 0;
    public boolean tryingToRiptide = false;
    public int slowedByUsingItemTransaction = Integer.MIN_VALUE;
    public boolean receivedSteerVehicle = false;
    
    public boolean didLastLastMovementIncludePosition = false;
    public boolean didLastMovementIncludePosition = false;
    
    public boolean didSendMovementBeforeTickEnd = false;
    public KnownInput knownInput = new KnownInput(false, false, false, false, false, false, false);
    public Vector3d lastClaimedPosition = new Vector3d(0, 0, 0);
    public float lastHealth, lastSaturation;
    public int lastFood;
    public boolean lastServerTransWasValid = false;
    
    public boolean horseInteractCausedForcedRotation = false;
    @Getter
    private boolean slowedByUsingItem;
    @Getter
    private int slowedByUsingItemSlot = Integer.MIN_VALUE;

    public void setSlowedByUsingItem(boolean slowedByUsingItem) {
        this.slowedByUsingItem = slowedByUsingItem;
        slowedByUsingItemSlot = slowedByUsingItem ? lastSlotSelected : Integer.MIN_VALUE;
    }
}