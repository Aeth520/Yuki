package cn.aetheris.yuki.math.vector;

import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Random;

@Getter
@ToString
public class Vector3dm implements Cloneable, Serializable {
    @Serial
    private static final long serialVersionUID = -2657651106777219169L;
    private static final Random RANDOM = new Random();
    private static final double EPSILON = 1e-6;
    IVector delegate;

    public Vector3dm() {
        this(0.0, 0.0, 0.0);
    }

    public Vector3dm(int x, int y, int z) {
        this(x, y, (double) z);
    }

    public Vector3dm(double x, double y, double z) {
        this.delegate = VecFactory.vecWith(x, y, z);
    }

    public Vector3dm(@NotNull IVector delegate) {
        this.delegate = delegate;
    }

    public Vector3dm(float x, float y, float z) {
        this(x, y, (double) z);
    }

    public static double getEpsilon() {
        return EPSILON;
    }

    public static @NotNull Vector3dm getRandom() {
        return new Vector3dm(RANDOM.nextDouble(), RANDOM.nextDouble(), RANDOM.nextDouble());
    }

    public double getX() {
        return delegate.getX();
    }

    public @NotNull Vector3dm setX(double x) {
        this.delegate.setX(x);
        return this;
    }

    public double getY() {
        return delegate.getY();
    }

    public @NotNull Vector3dm setY(double y) {
        this.delegate.setY(y);
        return this;
    }

    public double getZ() {
        return delegate.getZ();
    }

    public @NotNull Vector3dm setZ(double z) {
        this.delegate.setZ(z);
        return this;
    }

    public @NotNull Vector3dm add(@NotNull Vector3dm vec) {
        this.delegate.add(vec.delegate);
        return this;
    }

    public @NotNull Vector3dm subtract(@NotNull Vector3dm vec) {
        this.delegate.subtract(vec.delegate);
        return this;
    }

    public @NotNull Vector3dm multiply(@NotNull Vector3dm vec) {
        this.delegate.multiply(vec.delegate);
        return this;
    }

    public @NotNull Vector3dm divide(@NotNull Vector3dm vec) {
        this.delegate.divide(vec.delegate);
        return this;
    }

    public @NotNull Vector3dm copy(@NotNull Vector3dm vec) {
        this.delegate.copy(vec.delegate);
        return this;
    }

    public double lengthSquared() {
        return delegate.lengthSquared();
    }

    public double length() {
        return delegate.length();
    }

    public double distance(@NotNull Vector3dm o) {
        return delegate.distance(o.delegate);
    }

    public double distanceSquared(@NotNull Vector3dm o) {
        return delegate.distanceSquared(o.delegate);
    }

    public @NotNull Vector3dm midpoint(@NotNull Vector3dm other) {
        this.delegate.midpoint(other.delegate);
        return this;
    }

    public @NotNull Vector3dm getMidpoint(@NotNull Vector3dm other) {
        return new Vector3dm(this.delegate.premidpoint(other.delegate));
    }

    public @NotNull Vector3dm multiply(int m) {
        return multiply((double) m);
    }

    public @NotNull Vector3dm multiply(float m) {
        return multiply((double) m);
    }

    public @NotNull Vector3dm multiply(double m) {
        this.delegate.multiply(m);
        return this;
    }

    public double dot(@NotNull Vector3dm other) {
        return delegate.dot(other.delegate);
    }

    public @NotNull Vector3dm crossProduct(@NotNull Vector3dm o) {
        this.delegate.crossProduct(o.delegate);
        return this;
    }

    public @NotNull Vector3dm getCrossProduct(@NotNull Vector3dm o) {
        return new Vector3dm(
                this.delegate.getCrossProduct(o.delegate));
    }

    public @NotNull Vector3dm normalize() {
        this.delegate.normalize();
        return this;
    }

    public @NotNull Vector3dm zero() {
        this.delegate.zero();
        return this;
    }

    public boolean isZero() {
        return delegate.isZero();
    }

    @NotNull Vector3dm normalizeZeros() {
        this.delegate.normalizeZeros();
        return this;
    }

    public boolean isInAABB(@NotNull Vector3dm min, @NotNull Vector3dm max) {
        return this.delegate.isInAABB(min.delegate, max.delegate);
    }

    public boolean isInSphere(@NotNull Vector3dm origin, double radius) {
        return this.delegate.isInSphere(origin.delegate, radius);
    }

    public boolean isNormalized() {
        return delegate.isNormalized();
    }

    public int getBlockX() {
        return this.delegate.getBlockX();
    }

    public int getBlockY() {
        return this.delegate.getBlockY();
    }

    public int getBlockZ() {
        return this.delegate.getBlockZ();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector3dm other)) return false;
        return this.delegate.equals(other.delegate);
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }

    @Override
    public @NotNull Vector3dm clone() {

        return new Vector3dm(this.delegate.clone());
    }

    public @NotNull Vector3f toVector3f() {
        return this.delegate.toVector3f();
    }

    public @NotNull Vector3d toVector3d() {
        return this.delegate.toVector3d();
    }
}