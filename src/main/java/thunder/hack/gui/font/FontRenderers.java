package thunder.hack.gui.font;

import org.jetbrains.annotations.NotNull;
import thunder.hack.ThunderHack;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class FontRenderers {
    public static FontAdapter settings;
    public static FontAdapter modules;
    public static FontAdapter categories;
    public static FontAdapter icons;
    public static FontAdapter mid_icons;
    public static FontAdapter big_icons;
    public static FontAdapter thglitch;
    public static FontAdapter thglitchBig;
    public static FontAdapter monsterrat;
    public static FontAdapter sf_bold;
    public static FontAdapter sf_bold_mini;
    public static FontAdapter sf_medium;
    public static FontAdapter sf_medium_mini;
    public static FontAdapter sf_medium_modules;

    public static FontAdapter getSettingsRenderer() {
        return settings;
    }

    public static FontAdapter getModulesRenderer() {
        return modules;
    }
    
    public static @NotNull RendererFontAdapter createDefault(float size, String name) throws IOException, FontFormatException {
        return new RendererFontAdapter(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(ThunderHack.class.getClassLoader().getResourceAsStream("assets/thunderhack/fonts/" + name + ".ttf"))).deriveFont(Font.PLAIN, size / 2f), size / 2f);
    }
    
    public static @NotNull RendererFontAdapter createIcons(float size) throws IOException, FontFormatException {
        return new RendererFontAdapter(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(ThunderHack.class.getClassLoader().getResourceAsStream("assets/thunderhack/fonts/icons.ttf"))).deriveFont(Font.PLAIN, size / 2f), size / 2f);
    }
}
