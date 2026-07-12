package cn.aetheris.yuki.data.movement;

import cn.aetheris.yuki.math.vector.Vector3dm;

public class VelocityData {
    public final Vector3dm vector;
    public final int entityID;
    public final int transaction;
    public double offset = Integer.MAX_VALUE;
    public boolean isSetback;

    public VelocityData(int entityID, int transaction, boolean isSetback, Vector3dm vector) {
        this.entityID = entityID;
        this.vector = vector;
        this.transaction = transaction;
        this.isSetback = isSetback;
    }

    
    public VelocityData(int entityID, int transaction, Vector3dm vector, boolean isSetback, double offset) {
        this.entityID = entityID;
        this.vector = vector;
        this.transaction = transaction;
        this.isSetback = isSetback;
        this.offset = offset;
    }
}
