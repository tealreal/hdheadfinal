package teal.hdhead.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import static net.minecraft.util.Formatting.FORMATTING_CODE_PREFIX;

// I wrote this and I hate it. Minecraft probably has some internal class, but I can't find it, so I wrote one to work in conjunction with hd heads.

public interface Rawsay {
    // RGB consists of HEX, which is 16 characters (0-F), coincidentally the same amount of default colors for Minecraft text colors.
    String rgb = "0123456789AaBbCcDdEeFf";
    // Styling consists of: k - obfuscated, l - bold, m - strikethrough, n - underline, o - italic, r - reset
    String style = "KkLlMmNnOoRr";
    // Formatting includes the default 16 colors AND styling options
    String format = rgb + style;

    // Checks to see if the text has any available formatting that can be converted
    // i.e. "&ehello" will convert to "§ehello" (which is "hello" in yellow), but "&ghello" will stay as is.
    static String correctFormatting(String text, char separator) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == separator && (format.indexOf(chars[i + 1]) > -1 || (chars[i + 1] == '#' && text.substring(i + 2, i + 8).matches("^[" + rgb + "]{6}$")))) {
                chars[i] = FORMATTING_CODE_PREFIX;
            }
        }
        return String.valueOf(chars);
    }

    static Text parseFormatting(String text, char separator) {
        // Create empty text to append to
        MutableText msg = Text.literal("");

        // Split the string from correctFormatting by §, a character that cannot be typed in Minecraft.
        String[] readyToParse = correctFormatting(text, separator).split(FORMATTING_CODE_PREFIX + "+");

        // Setup default color and styling
        TextColor color = TextColor.fromFormatting(Formatting.WHITE);
        boolean bold, italic, underlined, strikethrough, obfuscated;
        bold = italic = underlined = strikethrough = obfuscated = false;
        for (int i = 0; i < readyToParse.length; i++) {
            // §6hell§eo -> ['', '6hell', 'eo']
            String portion = readyToParse[i];
            // Amount of characters that is styling before the actual text
            int index = 1;
            char[] chars = portion.toCharArray();
            // Check for empty text
            if (chars.length < 1) continue;

            // The first character WILL ALWAYS BE STYLING OR A COLOR, INCLUDING RGB.
            char format = Character.toLowerCase(chars[0]);

            // If you set the name to something plain like 'lol' (NOT '&lol'), you will get 'ol' in bold. This prevents that.
            if (i > 0) {
                // If format begins with a style, modify as such.
                if (style.contains(Character.toString(format))) {
                    bold = format == 'l';
                    italic = format == 'o';
                    underlined = format == 'n';
                    strikethrough = format == 'm';
                    obfuscated = format == 'k';
                    // If the color is RGB or 0-9a-f...
                } else if (rgb.contains(Character.toString(format)) || (format == '#' && portion.substring(1, 7).matches("^[" + rgb + "]{6}$"))) {
                    // Check if it is RGB
                    boolean isRGB = format == '#';

                    // Adjust index so the actual RGB code won't get into the text
                    if (isRGB) index = 7;

                    // Get the TextColor from the code or RGB
                    color = isRGB ? TextColor.fromRgb(Integer.parseInt(portion.substring(1, 7), 16)) : TextColor.fromFormatting(Formatting.byCode(format));

                    // Changing the color will reset the styling.
                    bold = italic = underlined = strikethrough = obfuscated = false;
                    // 'r' is a special code that resets the color & formatting.
                } else if (format == 'r') {
                    color = TextColor.fromFormatting(Formatting.WHITE);
                    bold = italic = underlined = strikethrough = obfuscated = false;
                }
            } else index = 0;
            msg.append(Text.literal(portion.substring(index)).setStyle(Style.EMPTY
                .withColor(color)
                .withBold(bold)
                .withItalic(italic)
                .withUnderline(underlined)
                .withStrikethrough(strikethrough)
                .withObfuscated(obfuscated)
            ));
        }
        return msg;
    }
}
