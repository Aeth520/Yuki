package cn.aetheris.yuki.math.vector;

import cn.aetheris.yuki.math.MathUtil;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Random;

@Getter
@ToString
public class SimdVector3 implements Cloneable, Serializable, IVector {
    @Serial
    private static final long serialVersionUID = -2657651106777219169L;
    private static final Random RANDOM = new Random();
    private static final double EPSILON = 1e-6;

    DoubleVector delegate;

    public SimdVector3() {
        this(0.0, 0.0, 0.0);
    }

    public SimdVector3(DoubleVector delegate) {
        this.delegate = delegate;
    }

    public SimdVector3(int x, int y, int z) {
        this(x, y, (double) z);
    }

    public SimdVector3(double x, double y, double z) {
        this(DoubleVectorBridge.vecWith(x, y, z));
    }

    public SimdVector3(float x, float y, float z) {
        this(x, y, (double) z);
    }

    public static double getEpsilon() {
        return EPSILON;
    }

    public static @NotNull IVector getMinimum(@NotNull SimdVector3 v1, @NotNull SimdVector3 v2) {
        return new SimdVector3(v1.delegate.min(v2.delegate));
    }

    public static @NotNull IVector getMaximum(@NotNull SimdVector3 v1, @NotNull SimdVector3 v2) {
        return new SimdVector3(v1.delegate.max(v2.delegate));
    }

    public static @NotNull IVector getRandom() {
        return new SimdVector3(RANDOM.nextDouble(), RANDOM.nextDouble(), RANDOM.nextDouble());
    }


    public DoubleVector reverseVector(DoubleVector vector) {
        VectorSpecies<Double> species = DoubleVector.SPECIES_256;
        int size = vector.length();
        int[] indexMap = new int[size];
        for (int i = 0; i < size; i++) {
            indexMap[i] = size - 1 - i;
        }
        return DoubleVector.fromArray(species, vector.toArray(), 0, indexMap, 0);
    }


    public DoubleVector crossProduct0(@NotNull IVector o) {
        DoubleVector lanewise = reverseVector(((SimdVector3) o).delegate);
        DoubleVector curLanewise = reverseVector(this.delegate);
        return delegate.mul(lanewise).sub(((SimdVector3) o).delegate.mul(curLanewise));
    }


    @Override
    public @NotNull IVector add(@NotNull IVector vec) {
        this.delegate = this.delegate.add(((SimdVector3) vec).delegate);
        return this;
    }

    @Override
    public @NotNull IVector subtract(@NotNull IVector vec) {
        this.delegate = this.delegate.sub(((SimdVector3) vec).delegate);
        return this;
    }

    @Override
    public @NotNull IVector multiply(@NotNull IVector vec) {
        this.delegate = this.delegate.mul(((SimdVector3) vec).delegate);
        return this;
    }

    @Override
    public @NotNull IVector divide(@NotNull IVector vec) {
        this.delegate = this.delegate.div(((SimdVector3) vec).delegate);
        return this;
    }

    @Override
    public @NotNull IVector copy(@NotNull IVector vec) {
        this.delegate = ((SimdVector3) vec).delegate.reinterpretAsDoubles();
        return this;
    }

    @Override
    public double lengthSquared() {
        return this.delegate.mul(this.delegate).reduceLanes(VectorOperators.ADD);
    }

    @Override
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    @Override
    public double distance(@NotNull IVector o) {
        return Math.sqrt(distanceSquared(o));
    }

    @Override
    public double distanceSquared(@NotNull IVector o) {

        DoubleVector sub = this.delegate.sub(((SimdVector3) o).delegate);
        return sub.mul(sub).reduceLanes(VectorOperators.ADD);
    }

    @Override
    public @NotNull IVector midpoint(@NotNull IVector other) {
        this.delegate = this.delegate.add(((SimdVector3) other).delegate).div(2);
        return this;
    }

    @Override
    public @NotNull IVector premidpoint(@NotNull IVector other) {
        return new SimdVector3(this.delegate.add(((SimdVector3) other).delegate).div(2));
    }

    @Override
    public @NotNull IVector multiply(int m) {
        return multiply((double) m);
    }

    @Override
    public @NotNull IVector multiply(float m) {
        return multiply((double) m);
    }

    @Override
    public @NotNull IVector multiply(double m) {
        this.delegate = this.delegate.mul(m);
        return this;
    }

    @Override
    public double dot(@NotNull IVector other) {
        return this.delegate.mul(((SimdVector3) other).delegate).reduceLanes(VectorOperators.ADD);
    }

    @Override
    public @NotNull IVector crossProduct(@NotNull IVector o) {
        this.delegate = crossProduct0(o);
        return this;
    }

    @Override
    public @NotNull IVector getCrossProduct(@NotNull IVector o) {
        return new SimdVector3(crossProduct0(o));
    }

    @Override
    public @NotNull IVector normalize() {
        double len = length();
        this.delegate = this.delegate.div(len);
        return this;
    }

    @Override
    public @NotNull IVector zero() {
        this.delegate = this.delegate.mul(0);
        return this;
    }

    @Override
    public boolean isZero() {
        return this.delegate.test(VectorOperators.IS_DEFAULT).allTrue();
    }

    @Override
    public @NotNull IVector normalizeZeros() {
        DoubleVector delegate1 = this.delegate;
        VectorMask<Double> compare = delegate1.test(VectorOperators.IS_DEFAULT);
        if (compare.laneIsSet(0)) delegate1 = delegate1.withLane(0, 0);
        if (compare.laneIsSet(1)) delegate1 = delegate1.withLane(1, 0);
        if (compare.laneIsSet(2)) delegate1 = delegate1.withLane(2, 0);
        this.delegate = delegate1;
        return this;
    }

    @Override
    public boolean isInAABB(@NotNull IVector min, @NotNull IVector max) {
        VectorMask<Double> compare = this.delegate.compare(VectorOperators.GE, ((SimdVector3) min).delegate);
        VectorMask<Double> compare1 = this.delegate.compare(VectorOperators.LE, ((SimdVector3) min).delegate);
        return compare1.allTrue() && compare.allTrue();
    }

    @Override
    public boolean isInSphere(@NotNull IVector origin, double radius) {
        return distanceSquared(origin) <= MathUtil.square(radius);
    }

    @Override
    public boolean isNormalized() {
        return Math.abs(lengthSquared() - 1.0) < EPSILON;
    }

    @Override
    public @NotNull IVector rotateRight(double rad) {
        this.delegate = this.delegate.lanewise(VectorOperators.ROR, rad);
        return this;
    }

    @Override
    public @NotNull IVector rotateLeft(double rad) {
        this.delegate = this.delegate.lanewise(VectorOperators.ROL, rad);
        return this;
    }

    @Override
    public @NotNull IVector setX(double x) {
        this.delegate = this.delegate.withLane(0, x);
        return this;
    }

    @Override
    public @NotNull IVector setY(double y) {
        this.delegate = this.delegate.withLane(1, y);
        return this;
    }

    @Override
    public @NotNull IVector setZ(double z) {
        this.delegate = this.delegate.withLane(2, z);
        return this;
    }

    @Override
    public double getX() {
        return this.delegate.lane(0);
    }

    @Override
    public double getY() {

        return this.delegate.lane(1);
    }

    @Override
    public double getZ() {
        return this.delegate.lane(2);
    }

    @Override
    public int getBlockX() {
        return MathUtil.mojangFloor(getX());
    }

    @Override
    public int getBlockY() {
        return MathUtil.mojangFloor(getY());
    }

    @Override
    public int getBlockZ() {
        return MathUtil.mojangFloor(getZ());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SimdVector3 other)) return false;
        return this.delegate.equals(other.delegate);
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }

    @Override
    public @NotNull IVector clone() {
        return new SimdVector3(this.delegate.reinterpretAsDoubles());
    }

    @Override
    public @NotNull Vector3f toVector3f() {
        return new Vector3f((float) getX(), (float) getY(), (float) getZ());
    }

    @Override
    public @NotNull Vector3d toVector3d() {
        return new Vector3d(getX(), getY(), getZ());
    }
}