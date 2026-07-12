package cn.aetheris.yuki.math.vector;

import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Random;

@ToString
public class CompatibleVector3 implements IVector, Cloneable, Serializable {
    @Serial
    private static final long serialVersionUID = -2657651106777219169L;
    private static final Random RANDOM = new Random();
    private static final double EPSILON = 1e-6;

    protected double x;
    protected double y;
    protected double z;

    public CompatibleVector3() {
        this(0.0, 0.0, 0.0);
    }

    public CompatibleVector3(int x, int y, int z) {
        this(x, y, (double) z);
    }

    public CompatibleVector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public CompatibleVector3(float x, float y, float z) {
        this(x, y, (double) z);
    }


    public @NotNull IVector add(@NotNull IVector vec) {
        this.x += vec.getX();
        this.y += vec.getY();
        this.z += vec.getZ();
        return this;
    }

    public @NotNull IVector subtract(@NotNull IVector vec) {
        this.x -= vec.getX();
        this.y -= vec.getY();
        this.z -= vec.getZ();
        return this;
    }

    public @NotNull IVector multiply(@NotNull IVector vec) {
        this.x *= vec.getX();
        this.y *= vec.getY();
        this.z *= vec.getZ();
        return this;
    }

    public @NotNull IVector divide(@NotNull IVector vec) {
        this.x /= vec.getX();
        this.y /= vec.getY();
        this.z /= vec.getZ();
        return this;
    }

    public @NotNull IVector copy(@NotNull IVector vec) {
        this.x = vec.getX();
        this.y = vec.getY();
        this.z = vec.getZ();
        return this;
    }

    public double lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double distance(@NotNull IVector o) {
        return Math.sqrt(
                MathUtil.square(this.x - o.getX()) +
                        MathUtil.square(this.y - o.getY()) +
                        MathUtil.square(this.z - o.getZ())
        );
    }

    public double distanceSquared(@NotNull IVector o) {
        return MathUtil.square(this.x - o.getX())
                + MathUtil.square(this.y - o.getY())
                + MathUtil.square(this.z - o.getZ());
    }

    public @NotNull IVector midpoint(@NotNull IVector other) {
        this.x = (this.x + other.getX()) * 0.5;
        this.y = (this.y + other.getY()) * 0.5;
        this.z = (this.z + other.getZ()) * 0.5;
        return this;
    }

    public @NotNull IVector premidpoint(@NotNull IVector other) {
        return new CompatibleVector3(
                (this.x + other.getX()) * 0.5,
                (this.y + other.getY()) * 0.5,
                (this.z + other.getZ()) * 0.5
        );
    }

    public @NotNull IVector multiply(int m) {
        return multiply((double) m);
    }

    public @NotNull IVector multiply(float m) {
        return multiply((double) m);
    }

    public @NotNull IVector multiply(double m) {
        this.x *= m;
        this.y *= m;
        this.z *= m;
        return this;
    }

    public double dot(@NotNull IVector other) {
        return this.x * other.getX() + this.y * other.getY() + this.z * other.getZ();
    }

    public @NotNull IVector crossProduct(@NotNull IVector o) {
        double nx = this.y * o.getZ() - o.getY() * this.z;
        double ny = this.z * o.getX() - o.getZ() * this.x;
        double nz = this.x * o.getY() - o.getX() * this.y;
        this.x = nx;
        this.y = ny;
        this.z = nz;
        return this;
    }

    public @NotNull IVector getCrossProduct(@NotNull IVector o) {
        return new CompatibleVector3(
                this.y * o.getZ() - o.getY() * this.z,
                this.z * o.getX() - o.getZ() * this.x,
                this.x * o.getY() - o.getX() * this.y
        );
    }

    public @NotNull IVector normalize() {
        double len = length();
        this.x /= len;
        this.y /= len;
        this.z /= len;
        return this;
    }

    public @NotNull IVector zero() {
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
        return this;
    }

    public boolean isZero() {
        return this.x == 0.0 && this.y == 0.0 && this.z == 0.0;
    }

    @NotNull
    public IVector normalizeZeros() {
        if (this.x == -0.0) this.x = 0.0;
        if (this.y == -0.0) this.y = 0.0;
        if (this.z == -0.0) this.z = 0.0;
        return this;
    }

    public boolean isInAABB(@NotNull IVector min, @NotNull IVector max) {
        return this.x >= min.getX() && this.x <= max.getX()
                && this.y >= min.getY() && this.y <= max.getY()
                && this.z >= min.getZ() && this.z <= max.getZ();
    }

    public boolean isInSphere(@NotNull IVector origin, double radius) {
        return distanceSquared(origin) <= MathUtil.square(radius);
    }

    public boolean isNormalized() {
        return Math.abs(lengthSquared() - 1.0) < EPSILON;
    }


    public @NotNull IVector rotateRight(double angle) {
        return this;
    }

    public @NotNull IVector rotateLeft(double angle) {
        return this;
    }

    public @NotNull IVector setX(double x) {
        this.x = x;
        return this;
    }

    public @NotNull IVector setY(double y) {
        this.y = y;
        return this;
    }

    public @NotNull IVector setZ(double z) {
        this.z = z;
        return this;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    public int getBlockX() {
        return MathUtil.mojangFloor(x);
    }

    public int getBlockY() {
        return MathUtil.mojangFloor(y);
    }

    public int getBlockZ() {
        return MathUtil.mojangFloor(z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IVector other)) return false;
        return Math.abs(this.x - other.getX()) < EPSILON
                && Math.abs(this.y - other.getY()) < EPSILON
                && Math.abs(this.z - other.getZ()) < EPSILON;
    }

    @Override
    public int hashCode() {
        int h = 7;
        h = 31 * h + Double.hashCode(x);
        h = 31 * h + Double.hashCode(y);
        h = 31 * h + Double.hashCode(z);
        return h;
    }

    @Override
    public @NotNull IVector clone() {
        return new CompatibleVector3(x, y, z);
    }

    public @NotNull Vector3f toVector3f() {
        return new Vector3f((float) x, (float) y, (float) z);
    }

    public @NotNull Vector3d toVector3d() {
        return new Vector3d(x, y, z);
    }
}