package cn.aetheris.yuki.util.color;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradientParser {

    private static final Pattern GRADIENT_TAG = Pattern.compile(
            "<color from=\"#([0-9a-fA-F]{6})\" to=\"#([0-9a-fA-F]{6})\">(.*?)</color>",
            Pattern.DOTALL
    );

    private static final Pattern FORMAT_CODE = Pattern.compile("(§[0-9a-fk-orA-FK-OR])");

    public static Component parse(String input) {
        Component root = Component.empty();
        Matcher matcher = GRADIENT_TAG.matcher(input);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                root = root.append(parseNormalText(input.substring(lastEnd, matcher.start())));
            }
            Color start = Color.decode("#" + matcher.group(1));
            Color end = Color.decode("#" + matcher.group(2));
            String content = matcher.group(3);
            root = root.append(createGradientWithFormatCodes(start, end, content));
            lastEnd = matcher.end();
        }

        if (lastEnd < input.length()) {
            root = root.append(parseNormalText(input.substring(lastEnd)));
        }

        return root;
    }

    private static Component createGradientWithFormatCodes(Color startColor, Color endColor, String text) {
        Component component = Component.empty();
        List<TextSegment> segments = splitByFormatCodes(text);
        Color currentStart = startColor;
        Color currentEnd = endColor;
        int gradientLength = text.replaceAll("§[0-9a-fk-orA-FK-OR]", "").length();
        int processedChars = 0;

        for (TextSegment segment : segments) {
            if (segment.isFormatCode()) {
                char code = segment.content.charAt(1);
                Component formatComp = Component.text(segment.content);

                switch (code) {
                    case 'l':
                        formatComp = formatComp.decoration(TextDecoration.BOLD, true);
                        break;
                    case 'o':
                        formatComp = formatComp.decoration(TextDecoration.ITALIC, true);
                        break;
                    case 'n':
                        formatComp = formatComp.decoration(TextDecoration.UNDERLINED, true);
                        break;
                    case 'm':
                        formatComp = formatComp.decoration(TextDecoration.STRIKETHROUGH, true);
                        break;
                    case 'k':
                        formatComp = formatComp.decoration(TextDecoration.OBFUSCATED, true);
                        break;
                    case 'r':
                        formatComp = formatComp.decoration(TextDecoration.BOLD, false);
                        formatComp = formatComp.decoration(TextDecoration.ITALIC, false);
                        formatComp = formatComp.decoration(TextDecoration.UNDERLINED, false);
                        formatComp = formatComp.decoration(TextDecoration.STRIKETHROUGH, false);
                        formatComp = formatComp.decoration(TextDecoration.OBFUSCATED, false);
                        break;
                    default:
                        NamedTextColor namedColor = getNamedColorByChar(code);
                        if (namedColor != null) {
                            currentStart = new Color(namedColor.red(), namedColor.green(), namedColor.blue());
                            currentEnd = currentStart;
                            formatComp = formatComp.color(namedColor);
                        }
                }
                component = component.append(formatComp);
            } else {
                for (int i = 0; i < segment.content.length(); i++) {
                    float ratio = gradientLength > 1 ? (float) (processedChars + i) / (gradientLength - 1) : 0.5f;
                    Color color = interpolate(currentStart, currentEnd, ratio);
                    component = component.append(Component.text(segment.content.charAt(i))
                            .color(TextColor.color(color.getRed(), color.getGreen(), color.getBlue())));
                }
                processedChars += segment.content.length();
            }
        }

        return component;
    }

    private static List<TextSegment> splitByFormatCodes(String text) {
        List<TextSegment> segments = new ArrayList<>();
        Matcher matcher = FORMAT_CODE.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                segments.add(new TextSegment(text.substring(lastEnd, matcher.start()), false));
            }
            segments.add(new TextSegment(matcher.group(), true));
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            segments.add(new TextSegment(text.substring(lastEnd), false));
        }

        return segments;
    }

    private static Component parseNormalText(String text) {
        Component component = Component.empty();
        List<TextSegment> segments = splitByFormatCodes(text);

        for (TextSegment segment : segments) {
            if (segment.isFormatCode()) {
                char code = segment.content.charAt(1);
                Component formatComp = Component.text(segment.content);

                switch (code) {
                    case 'l':
                        formatComp = formatComp.decoration(TextDecoration.BOLD, true);
                        break;
                    case 'o':
                        formatComp = formatComp.decoration(TextDecoration.ITALIC, true);
                        break;
                    case 'n':
                        formatComp = formatComp.decoration(TextDecoration.UNDERLINED, true);
                        break;
                    case 'm':
                        formatComp = formatComp.decoration(TextDecoration.STRIKETHROUGH, true);
                        break;
                    case 'k':
                        formatComp = formatComp.decoration(TextDecoration.OBFUSCATED, true);
                        break;
                    case 'r':
                        formatComp = formatComp.decoration(TextDecoration.BOLD, false);
                        formatComp = formatComp.decoration(TextDecoration.ITALIC, false);
                        formatComp = formatComp.decoration(TextDecoration.UNDERLINED, false);
                        formatComp = formatComp.decoration(TextDecoration.STRIKETHROUGH, false);
                        formatComp = formatComp.decoration(TextDecoration.OBFUSCATED, false);
                        break;
                    default:
                        NamedTextColor namedColor = getNamedColorByChar(code);
                        if (namedColor != null) {
                            formatComp = formatComp.color(namedColor);
                        }
                }
                component = component.append(formatComp);
            } else {
                component = component.append(Component.text(segment.content));
            }
        }

        return component;
    }

    private static NamedTextColor getNamedColorByChar(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> NamedTextColor.BLACK;
            case '1' -> NamedTextColor.DARK_BLUE;
            case '2' -> NamedTextColor.DARK_GREEN;
            case '3' -> NamedTextColor.DARK_AQUA;
            case '4' -> NamedTextColor.DARK_RED;
            case '5' -> NamedTextColor.DARK_PURPLE;
            case '6' -> NamedTextColor.GOLD;
            case '7' -> NamedTextColor.GRAY;
            case '8' -> NamedTextColor.DARK_GRAY;
            case '9' -> NamedTextColor.BLUE;
            case 'a' -> NamedTextColor.GREEN;
            case 'b' -> NamedTextColor.AQUA;
            case 'c' -> NamedTextColor.RED;
            case 'd' -> NamedTextColor.LIGHT_PURPLE;
            case 'e' -> NamedTextColor.YELLOW;
            case 'f' -> NamedTextColor.WHITE;
            default -> null;
        };
    }

    private static Color interpolate(Color start, Color end, float ratio) {
        int r = (int) (start.getRed() + (end.getRed() - start.getRed()) * ratio);
        int g = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * ratio);
        int b = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * ratio);
        return new Color(
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, b))
        );
    }

    private static class TextSegment {
        final String content;
        final boolean isFormatCode;

        TextSegment(String content, boolean isFormatCode) {
            this.content = content;
            this.isFormatCode = isFormatCode;
        }

        boolean isFormatCode() {
            return isFormatCode;
        }
    }
}
