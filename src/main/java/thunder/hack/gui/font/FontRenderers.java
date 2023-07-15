package thunder.hack.gui.font;


import thunder.hack.Thunderhack;

import java.awt.*;
import java.io.IOException;

public class FontRenderers {
    public static FontAdapter settings;
    public static FontAdapter modules;
    public static FontAdapter categories;
    public static FontAdapter icons;
    public static FontAdapter mid_icons;
    public static FontAdapter big_icons;
    public static FontAdapter thglitch;
    public static FontAdapter monsterrat;
    public static FontAdapter sf_bold;
    public static FontAdapter sf_bold_mini;
    public static FontAdapter sf_medium;
    public static FontAdapter sf_medium_mini;


    public static FontAdapter getRenderer() {
        return settings;
    }

    public static FontAdapter getRenderer2() {
        return modules;
    }

    public static RendererFontAdapter createDefault(float size,String name) throws IOException, FontFormatException {
        return new RendererFontAdapter(Font.createFont(Font.TRUETYPE_FONT, Thunderhack.class.getClassLoader().getResourceAsStream(name + ".ttf")).deriveFont(Font.PLAIN, size * 2), size * 2);
    }

    public static RendererFontAdapter createIcons(float size) throws IOException, FontFormatException {
        return new RendererFontAdapter(Font.createFont(Font.TRUETYPE_FONT, Thunderhack.class.getClassLoader().getResourceAsStream("icons.ttf")).deriveFont(Font.PLAIN, size * 2), size * 2);
    }
}
