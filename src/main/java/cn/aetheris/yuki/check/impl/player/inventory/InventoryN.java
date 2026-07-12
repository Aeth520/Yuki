package cn.aetheris.yuki.check.impl.player.inventory;

import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.InventoryCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;

import java.util.ArrayDeque;
import java.util.Queue;

@CheckData(
        name = "InventoryN (Frequency)",
        configName = "InventoryN",
        type = CheckType.INVENTORY,
        decay = 0.85,
        setback = 5,
        experimental = true)
public class InventoryN extends InventoryCheck {

    private final Queue<Integer> clickSlots = new ArrayDeque<>(5);
    private long lastClickTime = 0L;

    public InventoryN(PlayerData player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CLICK_WINDOW) return;

        WrapperPlayClientClickWindow wrapper = new WrapperPlayClientClickWindow(event);
        long currentTime = System.currentTimeMillis();

        if (lastClickTime <= 0L) {
            lastClickTime = currentTime;
            return;
        }

        long timeDiff = currentTime - lastClickTime;

        if (timeDiff > 0 && timeDiff < 50) {
            clickSlots.add(wrapper.getSlot());

            if (clickSlots.size() == 5) {
                double variance = MathUtil.variance(clickSlots);
                double average = MathUtil.getAverage(clickSlots);

                if (variance < 5.0 && average > 3.0) {
                    double avgFormatted = MathUtil.round(average, 4);
                    double varFormatted = MathUtil.round(variance, 4);
                    if (buffer++ > 2) {
                        if (flagAndAlert("avg= " + avgFormatted + "\nvar= " + varFormatted)) {
                            if (getViolations() > getSetbackVL()) {
                                closeInventory();
                            }
                            if (buffer >= 8) event.setCancelled(true);
                        }
                    }
                } else {
                    rewardBufferAndVL();
                }
                clickSlots.clear();
            }
        }
        lastClickTime = currentTime;
    }


}