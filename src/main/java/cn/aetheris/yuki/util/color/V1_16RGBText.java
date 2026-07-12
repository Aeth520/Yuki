package cn.aetheris.yuki.util.color;

import cn.aetheris.yuki.util.message.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.awt.*;

public class V1_16RGBText {
    public static Component createRGBText(Color color1, Color color2, String text, boolean bold) {
        Component message = Component.empty();
        int i = 0;
        for (char c : text.toCharArray()) {
            Color interpolated = ColorUtils.interpolateColor_(color1, color2, (float) i / text.length());
            message = message.append(Component.text(c)
                    .color(TextColor.color(interpolated.getRGB()))
                    .decoration(TextDecoration.BOLD, bold));
            i++;
        }
        return message;
    }
}
