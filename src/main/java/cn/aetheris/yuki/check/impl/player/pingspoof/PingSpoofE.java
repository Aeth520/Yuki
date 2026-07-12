package cn.aetheris.yuki.check.impl.player.pingspoof;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.PacketCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.time.Watch;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(
        name = "PingSpoofE (Magic)",
        configName = "PingSpoofE",
        description = "Magic check from a code bug :(",
        decay = 0.5,
        type = CheckType.PINGSPOOF
)
public final class PingSpoofE extends Check implements PacketCheck {

    private final Watch transactionTimer = new Watch();
    private final Watch flyingTimer = new Watch();
    private final Watch resetVlTimer = new Watch();
    private final Watch attackTimer = new Watch();

    private int laggedCount = 0;
    private int flyingCount = 0;
    private int buffer2 = 0;
    private double previousTransactionTime = 0;
    private double attackCounter = 0;
    private double fakeLagCounter = 0;

    public PingSpoofE(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity entity = new WrapperPlayClientInteractEntity(event);
            if (entity.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (transactionTimer.hasTimeElapsed(400) && shouldModifyPackets()) {
                    event.setCancelled(true);
                }

                long transactionTime = transactionTimer.getTime();
                long attackTime = attackTimer.getTime();
                int diff = (int) Math.abs(previousTransactionTime - transactionTime);
                long transactionPing = player.transactionPing;

                if (transactionTime > 70 + 3 + transactionPing * 0.35 && attackTime > 40 - Math.min(transactionPing * 0.03, 10)
                        && attackTime < 1000 && (Math.abs(previousTransactionTime - transactionTime) > 30 || transactionTime - previousTransactionTime > 10)) {
                    attackCounter++;
                    if (attackCounter > 20) {
                        if (fakeLagCounter++ > 2 && flagAndAlert("#1")) {
                            player.mitigateDamage();
                        }
                    } else {
                        fakeLagCounter = Math.max(fakeLagCounter - Math.max(Math.min(0.01 * attackTime / 50, 1), 0.1), 0);
                    }
                } else if (attackTime > 40 || transactionTime == 0) {
                    attackCounter = (transactionTime == 0 ? 0 : Math.max(attackCounter - 0.007 * attackTime / 50.0, 0));
                }

                if (transactionTime == 0 && attackTime < 200 && diff > 5) {
                    buffer = Math.max(buffer - (attackTime < 55 ? (20 * 20) : Math.min(20 * 10 * attackTime / 50, 20 * 16)), 0);
                } else {
                    buffer++;
                    if (buffer > 20 * 60) {
                        buffer -= 200;
                        if (flagAndAlert("#2")) {
                            player.mitigateDamage();
                        }
                    }
                }

                previousTransactionTime = transactionTime;
                attackTimer.reset();
            }
        } else if (isTransaction(event.getPacketType())) {
            transactionTimer.reset();
        } else if (isFlying(event.getPacketType())) {
            if (flyingTimer.hasTimeElapsed(90)) {
                flyingCount = 0;
                laggedCount++;
            } else if (laggedCount >= 1) {
                flyingCount++;
                if (flyingCount > 30) {
                    flyingCount = 0;
                    laggedCount = 0;
                    buffer2++;
                }
            }
            if (buffer2 > 5) {
                if (flagAndAlert("(Tick)\nfl= " + laggedCount + "\nnl= " + flyingCount + "\nb= " + buffer2)) {
                    player.mitigateDamage();
                }
            }
            if (resetVlTimer.hasTimeElapsed(4000)) {
                buffer2 = Math.max(buffer2 - 1, 0);
            }
            flyingTimer.reset();
        }
    }
}
