package cn.aetheris.yuki.util.message;

import cn.aetheris.yuki.Yuki;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.text.format.TextColor;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtils {

    private static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();
    private static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.plainText();

    public static Color blend(Color color1, Color color2, double ratio) {
        float r = (float) ratio;
        float ir = 1.0f - r;
        float[] rgb1 = new float[3];
        float[] rgb2 = new float[3];
        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
        return new Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r + rgb2[1] * ir, rgb1[2] * r + rgb2[2] * ir, color1.getAlpha() / 255f);
    }

    public static Color blend(Color color1, Color color2) {
        return ColorUtils.blend(color1, color2, 0.5);
    }

    public static Color getRainbow(int speed, float sat, int alpha, int index) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        Color color2 = new Color(Color.HSBtoRGB(hue, sat, 1.0f));
        return new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), alpha);
    }

    public static Color reAlpha(int color, float alpha) {
        return reAlpha(new Color(color), alpha);
    }

    public static Color reAlpha(int color, int alpha) {
        return reAlpha(new Color(color), alpha);
    }

    public static Color reAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static Color reAlpha(Color color, float alpha) {
        return reAlpha(color, (int) (alpha * 255));
    }

    public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end, boolean trueColor) {
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return trueColor ? interpolateColorHue(start, end, angle / 360f) : interpolateColor_(start, end, angle / 360f);
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, (float) interpolationValue).floatValue();
    }

    public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, (float) interpolationValue).intValue();
    }

    public static Color interpolateColor_(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount),
                interpolateInt(color1.getGreen(), color2.getGreen(), amount),
                interpolateInt(color1.getBlue(), color2.getBlue(), amount),
                interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Color interpolateColorHue(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));

        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);

        Color resultColor = Color.getHSBColor(interpolateFloat(color1HSB[0], color2HSB[0], amount),
                interpolateFloat(color1HSB[1], color2HSB[1], amount), interpolateFloat(color1HSB[2], color2HSB[2], amount));

        return reAlpha(resultColor, interpolateInt(color1.getAlpha(), color2.getAlpha(), amount) / 255f);
    }

    public static String color(String message) {
        if (Yuki.getInstance().getPacketEventsManager().getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_16)) {
            message = translateHexCodes(message);
        }
        return LEGACY_SECTION.serialize(LEGACY_AMPERSAND.deserialize(message));
    }

    public static String stripColor(String message) {
        return PLAIN_TEXT.serialize(LEGACY_SECTION.deserialize(message));
    }

    public static String translateHexCodes(String message) {
        final String hexPattern = "#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})";
        Matcher matcher = Pattern.compile(hexPattern).matcher(message);
        StringBuilder sb = new StringBuilder(message.length());
        while (matcher.find()) {
            String hex = matcher.group(1);
            TextColor color = TextColor.fromHexString("#" + hex);
            if (color != null) {
                String legacy = LEGACY_SECTION.serialize(Component.text("").color(color));
                matcher.appendReplacement(sb, Matcher.quoteReplacement(legacy));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
