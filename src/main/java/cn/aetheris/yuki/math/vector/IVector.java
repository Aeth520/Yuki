package cn.aetheris.yuki.math.vector;

import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import org.jetbrains.annotations.NotNull;

public interface IVector {
    @NotNull IVector add(@NotNull IVector vec);

    @NotNull IVector subtract(@NotNull IVector vec);

    @NotNull IVector multiply(@NotNull IVector vec);

    @NotNull IVector divide(@NotNull IVector vec);

    @NotNull IVector copy(@NotNull IVector vec);

    double lengthSquared();

    double length();

    double distance(@NotNull IVector o);

    double distanceSquared(@NotNull IVector o);

    @NotNull IVector midpoint(@NotNull IVector other);

    @NotNull IVector premidpoint(@NotNull IVector other);

    @NotNull IVector multiply(int m);

    @NotNull IVector multiply(float m);

    @NotNull IVector multiply(double m);

    double dot(@NotNull IVector other);

    @NotNull IVector crossProduct(@NotNull IVector o);


    @NotNull IVector getCrossProduct(@NotNull IVector o);

    @NotNull IVector normalize();

    @NotNull IVector zero();

    boolean isZero();

    @NotNull IVector normalizeZeros();

    boolean isInAABB(@NotNull IVector min, @NotNull IVector max);

    boolean isInSphere(@NotNull IVector origin, double radius);

    boolean isNormalized();

    @NotNull IVector rotateRight(double rad);

    @NotNull IVector rotateLeft(double rad);

    double getX();

    @NotNull IVector setX(double x);

    double getY();

    @NotNull IVector setY(double y);

    double getZ();

    @NotNull IVector setZ(double z);

    int getBlockX();

    int getBlockY();

    int getBlockZ();

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

    @NotNull IVector clone();

    @NotNull Vector3f toVector3f();

    @NotNull Vector3d toVector3d();
}
