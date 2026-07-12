package cn.aetheris.yuki.protocol.nms.vec;

import lombok.Data;

@Data
public class Vec2d {

    private double x, y;

    public Vec2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2d(Number x, Number y) {
        this.x = x.doubleValue();
        this.y = y.doubleValue();
    }

    public double distance(Vec2d vector2) {
        return Math.sqrt(Math.pow(this.x - vector2.x, 2) + Math.pow(this.y - vector2.y, 2));
    }

    public Vec2d add(Vec2d vector2) {
        return new Vec2d(this.x + vector2.x, this.y + vector2.y);
    }

    public Vec2d subtract(Vec2d vector2) {
        return new Vec2d(this.x - vector2.x, this.y - vector2.y);
    }

    public Vec2d scale(double factor) {
        return new Vec2d(this.x * factor, this.y * factor);
    }

    public boolean compare(Vec2d vector2) {
        return this.x == vector2.x && this.y == vector2.y;
    }
}
