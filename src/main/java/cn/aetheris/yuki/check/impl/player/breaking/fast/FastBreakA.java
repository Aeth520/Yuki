package cn.aetheris.yuki.check.impl.player.breaking.fast;

import cn.aetheris.yuki.Yuki;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockBreakCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.core.plugin.init.HookInit;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.math.MathUtil;
import cn.aetheris.yuki.protocol.nms.BlockBreakSpeed;
import cn.aetheris.yuki.util.update.BlockBreak;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.util.Set;

@CheckData(name = "FastBreakA (Edit)",
        configName = "FastBreakA",
        description = "Break block too fast",
        decay = 0.45,
        type = CheckType.BREAK,
        experimental = true)
public final class FastBreakA extends Check implements BlockBreakCheck {

    
    
    private static final Set<StateType> EXEMPT_STATES = Set.of();
    private final boolean clientOlderThanServer = Yuki.getInstance().getPacketEventsManager().getServerVersion().getProtocolVersion() > player.getClientVersion().getProtocolVersion();

    
    
    double maximumBlockDamage = 0;

    
    Vector3i targetBlockPosition = null;
    
    public FastBreakA(PlayerData playerData) {
        super(playerData);
    }
    
    long lastFinishBreak = 0;
    
    long startBreak = 0;

    
    double blockBreakBalance = 0;
    double blockDelayBalance = 0;

    private StateType targetType = null;

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (isExempt(ExemptType.INVALID_GAMEMODE)) {
            return;
        }

        if (blockBreak.action == DiggingAction.START_DIGGING) {
            if (!HookInit.getViaPluginHook().isEnabled()) {
                
                final WrappedBlockState defaultState = WrappedBlockState.getDefaultState(player.getClientVersion(), blockBreak.block.getType());
                if (defaultState.getType() == StateTypes.AIR || EXEMPT_STATES.contains(defaultState.getType())) {
                    return;
                }
            }
            
            
            
            
            
            WrappedBlockState block = clientOlderThanServer ? WrappedBlockState.getByGlobalId(player.getClientVersion(), player.getViaTranslatedClientBlockID(blockBreak.block.getGlobalId())) : blockBreak.block;

            startBreak = time() - (targetBlockPosition == null ? 50 : 0); 
            targetBlockPosition = blockBreak.position;
            targetType = block.getType();
            maximumBlockDamage = BlockBreakSpeed.getBlockDamage(player, block);

            double breakDelay = time() - lastFinishBreak;

            if (breakDelay >= 275) { 
                blockDelayBalance *= 0.9;
            } else { 
                blockDelayBalance += 300 - breakDelay;
            }

            if (Double.isInfinite(breakDelay)) {
                return;
            }

            if (blockDelayBalance > 1000) { 
                if (flagAndAlert("delay= " + breakDelay + "\ntype= " + targetType) && shouldModifyPackets()) {
                    blockBreak.cancel();
                }
            }

            clampBalance();
        }

        if (blockBreak.action == DiggingAction.FINISHED_DIGGING && targetBlockPosition != null) {
            double predictedTime = Math.ceil(1 / maximumBlockDamage) * 50;
            double realTime = time() - startBreak;
            double diff = predictedTime - realTime;

            clampBalance();

            if (diff < 25) {  
                blockBreakBalance *= 0.9;
            } else { 
                blockBreakBalance += diff;
            }

            if (Double.isInfinite(diff)) {
                return;
            }

            if (blockBreakBalance > 1000) { 
                if (flagAndAlert("diff= " + diff + "\nbalance= " + blockBreakBalance
                        + "\nmaxBlockDamage= " + maximumBlockDamage + "\ntype= " + targetType)) {
                    blockBreak.cancel();
                }
            }

            
            lastFinishBreak = startBreak = time();
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        
        
        if ((player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? event.getPacketType() == PacketType.Play.Client.ANIMATION : WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) && targetBlockPosition != null) {
            maximumBlockDamage = Math.max(maximumBlockDamage, BlockBreakSpeed.getBlockDamage(player, player.compensatedWorld.getBlock(targetBlockPosition)));
        }
    }

    private void clampBalance() {
        double balance = Math.max(1000, (player.getTransactionPing()));
        blockBreakBalance = MathUtil.clamp(blockBreakBalance, -balance, balance); 
        blockDelayBalance = MathUtil.clamp(blockDelayBalance, -balance, balance);
    }
}