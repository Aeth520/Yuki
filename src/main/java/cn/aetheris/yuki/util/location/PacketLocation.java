package cn.aetheris.yuki.util.location;

import cn.aetheris.yuki.math.vector.Vector3dm;
import com.github.retrooper.packetevents.util.Vector3d;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@ToString
public final class PacketLocation {
    private World world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private long timeStamp;
    private boolean onGround;

    
    public PacketLocation(double x, double y, double z) {
        this(x, y, z, 0.0f, 0.0f, System.currentTimeMillis());
    }

    
    public PacketLocation(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        this(x, y, z, yaw, pitch, System.currentTimeMillis());
        this.onGround = onGround;
    }

    
    public PacketLocation(double x, double y, double z, float yaw, float pitch, long timeStamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.timeStamp = timeStamp;
    }

    
    public PacketLocation(double x, double y, double z, World world) {
        this(x, y, z);
        this.world = world;
    }

    
    public PacketLocation(double x, double y, double z, float yaw, float pitch, long timeStamp, String world, boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.timeStamp = timeStamp;
        this.onGround = onGround;
        this.world = Bukkit.getWorld(world);
    }

    
    public Location toLocation(Player player) {
        if (player != null) {
            return new Location(player.getWorld(), x, y, z, yaw, pitch);
        }
        return null;
    }

    private int locToBlock(double loc) {
        return NumberConversions.floor(loc);
    }

    public int getBlockX() {
        return locToBlock(x);
    }

    public int getBlockY() {
        return locToBlock(y);
    }

    public int getBlockZ() {
        return locToBlock(z);
    }

    
    public double distanceXZ(PacketLocation other) {
        return Math.hypot(other.x - x, other.z - z);
    }

    
    public double distanceY(PacketLocation other) {
        return Math.abs(other.y - y);
    }

    
    public double distanceSquare(PacketLocation other) {
        return Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2) + Math.pow(other.z - z, 2);
    }

    
    public double distance(PacketLocation other) {
        return Math.sqrt(distanceSquare(other));
    }

    public double distanceSquareXZ(PacketLocation other) {
        return Math.pow(other.x - x, 2) + Math.pow(other.z - z, 2);
    }

    public PacketLocation clone() {
        return new PacketLocation(x, y, z, yaw, pitch, onGround);
    }

    public @NotNull PacketLocation subtract(@NotNull PacketLocation vec) {
        if (vec.getWorld() == this.getWorld()) {
            this.x -= vec.x;
            this.y -= vec.y;
            this.z -= vec.z;
            return this;
        } else {
            throw new IllegalArgumentException("Cannot add Locations of differing worlds");
        }
    }

    public @NotNull PacketLocation subtract(@NotNull Vector vec) {
        this.x -= vec.getX();
        this.y -= vec.getY();
        this.z -= vec.getZ();
        return this;
    }

    public @NotNull PacketLocation subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vector3dm toVector() {
        return new Vector3dm(x, y, z);
    }

    public Vector3d toVector3d() {
        return new Vector3d(x, y, z);
    }

    public @NotNull PacketLocation add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }
}
