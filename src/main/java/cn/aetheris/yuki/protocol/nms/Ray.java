package cn.aetheris.yuki.protocol.nms;

import cn.aetheris.yuki.player.PlayerData;
import cn.aetheris.yuki.data.Pair;
import cn.aetheris.yuki.math.vector.Vector3dm;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class Ray implements Cloneable {

    private Vector3dm origin;
    private Vector3dm direction;

    
    public Ray(Vector3dm origin, Vector3dm direction) {
        this.origin = origin;
        this.direction = direction;
    }

    
    public Ray(PlayerData player, double x, double y, double z, float xRot, float yRot) {
        this.origin = new Vector3dm(x, y, z);
        this.direction = calculateDirection(player, xRot, yRot);
    }

    
    public static Vector3dm calculateDirection(PlayerData player, float xRot, float yRot) {
        float radX = (float) Math.toRadians(xRot);
        float radY = (float) Math.toRadians(yRot);
        double cosY = player.trigHandler.cos(radY);
        Vector3dm vector = new Vector3dm();
        vector.setY(-player.trigHandler.sin(radY));
        vector.setX(-cosY * player.trigHandler.sin(radX));
        vector.setZ(cosY * player.trigHandler.cos(radX));
        return vector;
    }

    
    @Override
    public Ray clone() {
        try {
            Ray cloned = (Ray) super.clone();
            cloned.origin = this.origin.clone();
            cloned.direction = this.direction.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }


    
    public Vector3dm getPointAtDistance(double distance) {
        
        Vector3dm scaledDirection = direction.clone().multiply(distance);
        return origin.clone().add(scaledDirection);
    }


    
    public Pair<Vector3dm, Vector3dm> closestPointsBetweenLines(Ray other) {
        Vector3dm n1 = direction.clone().crossProduct(other.direction.clone().crossProduct(direction));
        Vector3dm n2 = other.direction.clone().crossProduct(direction.clone().crossProduct(other.direction));

        Vector3dm diff = other.origin.clone().subtract(origin);
        double factor1 = diff.dot(n2) / direction.dot(n2);
        Vector3dm c1 = origin.clone().add(direction.clone().multiply(factor1));
        Vector3dm diff2 = origin.clone().subtract(other.origin);
        double factor2 = diff2.dot(n1) / other.direction.dot(n1);
        Vector3dm c2 = other.origin.clone().add(other.direction.clone().multiply(factor2));

        return new Pair<>(c1, c2);
    }

}