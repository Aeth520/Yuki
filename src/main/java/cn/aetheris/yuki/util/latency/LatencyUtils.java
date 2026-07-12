package cn.aetheris.yuki.util.latency;

import cn.aetheris.yuki.PluginLoader;
import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.util.message.LogUtils;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import net.kyori.adventure.text.Component;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class LatencyUtils implements ILatencyUtils {

    private final List<TransactionTask> transactionMap = new LinkedList<>();
    private final PlayerData player;
    private final List<Runnable> tasksToRun = new LinkedList<>();

    public LatencyUtils(PlayerData player) {
        this.player = player;
    }

    public void addRealTimeTask(int transaction, Runnable runnable) {
        addRealTimeTaskInternal(transaction, player.isAsyncTransactionSend(), runnable);
    }

    public void addRealTimeTaskAsync(int transaction, Runnable runnable) {
        addRealTimeTaskInternal(transaction, true, runnable);
    }

    private void addRealTimeTaskInternal(int transactionId, boolean async, Runnable runnable) {
        if (player.lastTransactionReceived.get() >= transactionId) {
            if (async) {
                ChannelHelper.runInEventLoop(player.user.getChannel(), runnable);
            } else {
                runnable.run();
            }
            return;
        }
        synchronized (transactionMap) {
            transactionMap.add(new TransactionTask(transactionId, runnable));
        }
    }

    @Override
    public void handleNettySyncTransaction(int receivedTransactionId) {
        synchronized (transactionMap) {
            tasksToRun.clear();

            Iterator<TransactionTask> iterator = transactionMap.iterator();
            while (iterator.hasNext()) {
                TransactionTask taskEntry = iterator.next();
                int taskTransactionId = taskEntry.transactionId();

                
                
                
                if (receivedTransactionId + 1 < taskTransactionId) {
                    break;
                }

                
                if (receivedTransactionId == taskTransactionId - 1) {
                    continue; 
                }

                
                tasksToRun.add(taskEntry.task());
                iterator.remove(); 
            }

            
            for (Runnable runnable : tasksToRun) {
                try {
                    runnable.run();
                } catch (Exception e) {
                    handleRunnableError(e);
                    break;
                }
            }
        }
    }

    private void handleRunnableError(Exception e) {
        LogUtils.exception("An error has occurred when running transactions for player: " + player.user.getName(), e);
        if (!Boolean.getBoolean("yuki.disable-transaction-kick")) {
            player.disconnect(Component.translatable(PluginLoader.INSTANCE.getLangManager().i18nWithoutPrefix("kick.timed-out")));
        }
    }

    
    private static final class TransactionTask {
        private final int transactionId;
        private final Runnable task;

        private TransactionTask(int transactionId, Runnable task) {
            this.transactionId = transactionId;
            this.task = task;
        }

        public int transactionId() {
            return transactionId;
        }

        public Runnable task() {
            return task;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (TransactionTask) obj;
            return this.transactionId == that.transactionId &&
                    Objects.equals(this.task, that.task);
        }

        @Override
        public int hashCode() {
            return Objects.hash(transactionId, task);
        }

        @Override
        public String toString() {
            return "TransactionTask[" +
                    "transactionId=" + transactionId + ", " +
                    "task=" + task + ']';
        }

    }
}