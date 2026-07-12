package cn.aetheris.yuki.util.change;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Predicate;

public class PlayerBlockHistory {
    public final Deque<BlockModification> modificationQueue = new ConcurrentLinkedDeque<>();

    
    public void add(BlockModification modification) {
        modificationQueue.add(modification);
    }

    
    public List<BlockModification> getRecentModifications(Predicate<BlockModification> filter) {
        List<BlockModification> list = new LinkedList<>();
        for (BlockModification blockModification : modificationQueue) {
            if (filter.test(blockModification)) {
                list.add(blockModification);
            }
        }
        return list; 
    }

    public List<WrappedBlockState> getBlockStates(Predicate<BlockModification> filter) {
        List<WrappedBlockState> list = new LinkedList<>();
        for (BlockModification mod : modificationQueue) {
            if (filter.test(mod)) {
                list.addAll(Arrays.asList(mod.oldBlockContents(), mod.newBlockContents()));
            }
        }
        return list;
    }

    public List<WrappedBlockState> getPreviousBlockStates(Predicate<BlockModification> filter) {
        List<WrappedBlockState> list = new LinkedList<>();
        for (BlockModification blockModification : modificationQueue) {
            if (filter.test(blockModification)) {
                WrappedBlockState oldBlockContents = blockModification.oldBlockContents();
                list.add(oldBlockContents);
            }
        }
        return list;
    }

    public List<WrappedBlockState> getResultingBlockStates(Predicate<BlockModification> filter) {
        List<WrappedBlockState> list = new LinkedList<>();
        for (BlockModification blockModification : modificationQueue) {
            if (filter.test(blockModification)) {
                WrappedBlockState newBlockContents = blockModification.newBlockContents();
                list.add(newBlockContents);
            }
        }
        return list;
    }

    
    public void cleanup(int maxTick) {
        while (!modificationQueue.isEmpty() && maxTick - modificationQueue.peekFirst().tick() > 0) {
            modificationQueue.removeFirst();
        }
    }

    
    public int size() {
        return modificationQueue.size();
    }

    
    public void clear() {
        modificationQueue.clear();
    }
}