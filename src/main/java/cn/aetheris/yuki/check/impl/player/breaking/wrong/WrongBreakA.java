package cn.aetheris.yuki.check.impl.player.breaking.wrong;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockBreakCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.update.BlockBreak;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;

import static cn.aetheris.yuki.protocol.nms.BlockBreakSpeed.getBlockDamage;

@CheckData(name = "WrongBreakA (Abort)", type = CheckType.BREAK, configName = "WrongBreakA", description = "Abort Break", decay = 0.215, experimental = true)
public final class WrongBreakA extends Check implements BlockBreakCheck {

    
    private final int exemptedY = player.getClientVersion().isOlderThan(ClientVersion.V_1_8) ? 255 : (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14) ? -1 : 4095);
    private boolean lastBlockWasInstantBreak = false;
    private Vector3i lastBlock, lastCancelledBlock, lastLastBlock = null;
    private long lastFlag;
    private long lastFlag2;

    public WrongBreakA(PlayerData player) {
        super(player);
    }

    
    private boolean shouldExempt(final WrappedBlockState block, int yPos) {
        
        if (lastLastBlock != null || lastBlock == null)
            return false;

        
        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_14_4) && yPos != exemptedY)
            return false;

        
        return player.getClientVersion().isOlderThan(ClientVersion.V_1_14_4) || getBlockDamage(player, block) < 1;
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.action == DiggingAction.START_DIGGING) {
            final Vector3i pos = blockBreak.position;

            lastBlockWasInstantBreak = getBlockDamage(player, blockBreak.block) >= 1;
            lastCancelledBlock = null;
            lastLastBlock = lastBlock;
            lastBlock = pos;
        }

        if (blockBreak.action == DiggingAction.CANCELLED_DIGGING) {
            final Vector3i pos = blockBreak.position;

            if (!shouldExempt(blockBreak.block, pos.y) && !pos.equals(lastBlock)) {
                if (time() - lastFlag < 400L) {
                    return;
                }
                
                if (player.getClientVersion().isOlderThan(ClientVersion.V_1_14_4) || (!lastBlockWasInstantBreak && pos.equals(lastCancelledBlock))) {
                    if (flagAndAlert("action= CANCELLED_DIGGING" + "\nlast= " + PluginLoader.INSTANCE.getLangManger().toUnlabledString(lastBlock) + "\npos=" + PluginLoader.INSTANCE.getLangManger().toUnlabledString(pos))) {
                        if (shouldModifyPackets()) {
                            blockBreak.cancel();
                            lastFlag = time();
                        }
                    }
                }
            }

            lastCancelledBlock = pos;
            lastLastBlock = null;
            lastBlock = null;
            return;
        }

        if (blockBreak.action == DiggingAction.FINISHED_DIGGING) {
            final Vector3i pos = blockBreak.position;

            
            if (!pos.equals(lastCancelledBlock) && (!lastBlockWasInstantBreak || player.getClientVersion().isOlderThan(ClientVersion.V_1_14_4)) && !pos.equals(lastBlock)) {
                if (time() - lastFlag2 < 600L) {
                    return;
                }
                if (flagAndAlert("action= FINISHED_DIGGING" + "\nlast=" + PluginLoader.INSTANCE.getLangManger().toUnlabledString(lastBlock) + "\npos=" + PluginLoader.INSTANCE.getLangManger().toUnlabledString(pos))) {
                    if (shouldModifyPackets()) {
                        blockBreak.cancel();
                        lastFlag2 = time();
                    }
                }
            }

            
            if (player.getClientVersion().isOlderThan(ClientVersion.V_1_14_4)) {
                lastCancelledBlock = null;
                lastLastBlock = null;
                lastBlock = null;
            }
        }
    }
}