package cn.aetheris.yuki.predictionengine;


import cn.aetheris.yuki.check.Check;
import cn.aetheris.yuki.check.type.PostPredictionCheck;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.ghostblock.GhostBlockUtil;
import cn.aetheris.yuki.util.message.LogUtils;
import cn.aetheris.yuki.util.update.PredictionComplete;

public final class GhostBlockDetector extends Check implements PostPredictionCheck {
    boolean ghostSync;

    public GhostBlockDetector(PlayerData player) {
        super(player);
    }

    
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        
        
        if (predictionComplete.getOffset() < 0.001 && (player.clientClaimsLastOnGround == player.onGround || player.inVehicle()))
            return;


        if (!ghostSync) {
            return;
        }

        
        
        boolean shouldResync = GhostBlockUtil.isGhostBlock(player);

        if (shouldResync) {
            
            if (player.clientClaimsLastOnGround != player.onGround) {
                
                
                player.onGround = player.clientClaimsLastOnGround;
            }

            predictionComplete.setOffset(0);
            player.getSetbackTeleportUtil().executeForceResync();
            LogUtils.sync("&b" + player.getName() + "&7 ForceResync for ghost block");
        }
    }
}