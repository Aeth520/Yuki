package cn.aetheris.yuki.check.impl.player.breaking.wrong;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.api.enums.CheckType;
import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.CheckData;
import cn.aetheris.yuki.check.type.BlockBreakCheck;
import cn.aetheris.yuki.check.util.exempts.types.ExemptType;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.change.BlockModification;
import cn.aetheris.yuki.util.update.BlockBreak;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;


@CheckData(name = "WrongBreakB", type = CheckType.BREAK, configName = "WrongBreakB", description = "air/water break", decay = 0.23, experimental = true)
public final class WrongBreakB extends Check implements BlockBreakCheck {

    public final boolean noFireHitbox = player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_15_2);
    private int lastTick;
    private boolean didLastFlag;
    
    private @NonNull Vector3i lastBreakLoc = new Vector3i();
    private @NonNull StateType lastBlockType = StateTypes.AIR;

    public WrongBreakB(PlayerData player) {
        super(player);
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.action != DiggingAction.START_DIGGING
                && blockBreak.action != DiggingAction.FINISHED_DIGGING)
            return;

        if (isExempt(ExemptType.CLIENT_ANTICHEAT)) return;

        if (blockBreak.isCancelled()) return;

        final StateType block = blockBreak.block.getType();

        int newTick = PluginLoader.INSTANCE.getTickManager().currentTick;
        if (lastTick == newTick
                && lastBreakLoc.equals(blockBreak.position)
                && !didLastFlag
                && lastBlockType.getHardness() == 0.0F
                && lastBlockType.getBlastResistance() == 0.0F
                && block == StateTypes.WATER
        ) {
            return;
        }
        
        List<StateType> previousBlockStates = new ArrayList<>();
        for (BlockModification blockModification : player.blockHistory.modificationQueue) {
            if (blockModification.location().equals(blockBreak.position)
                    && newTick - blockModification.tick() < 2
                    && (blockModification.cause() == BlockModification.Cause.START_DIGGING || blockModification.cause() == BlockModification.Cause.HANDLE_NETTY_SYNC_TRANSACTION)) {
                previousBlockStates.add(blockModification.oldBlockContents().getType());
            }
        }
        previousBlockStates.add(0, block);

        boolean invalid = false;
        for (StateType possibleBlockState : previousBlockStates) {

            if (possibleBlockState == StateTypes.KELP
                    || possibleBlockState == StateTypes.SEAGRASS
                    || possibleBlockState.getName().endsWith("_CORAL")
                    || possibleBlockState.getName().endsWith("_BUD")) {
                return;
            }

            invalid = (possibleBlockState == StateTypes.LIGHT && !(player.getInventory().getHeldItem().is(ItemTypes.LIGHT) || player.getInventory().getOffHand().is(ItemTypes.LIGHT)))
                    || possibleBlockState.isAir()
                    || possibleBlockState == StateTypes.WATER
                    || possibleBlockState == StateTypes.LAVA
                    || possibleBlockState == StateTypes.BUBBLE_COLUMN
                    || possibleBlockState == StateTypes.MOVING_PISTON
                    || possibleBlockState == StateTypes.FIRE && noFireHitbox
                    || possibleBlockState.getHardness() == -1.0f && blockBreak.action == DiggingAction.FINISHED_DIGGING;
            if (!invalid) {
                break;
            }
        }
        if (invalid && flagAndAlert("block= " + block.getName() + "\ntype= " + blockBreak.action)) {
            didLastFlag = true;
            blockBreak.cancel();
        } else {
            didLastFlag = false;
        }
        lastTick = newTick;
        lastBreakLoc = blockBreak.position;
        lastBlockType = block;
    }
}
