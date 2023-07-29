package thunder.hack.utility;

import static thunder.hack.modules.Module.mc;

public class Macro {

    private final String name;
    private String text;
    private int bind;

    public Macro(String name, String text, int bind) {
        this.name = name;
        this.text = text;
        this.bind = bind;
    }

    public void runMacro() {
        if(text.contains("/")){
            mc.player.networkHandler.sendChatCommand(text.replace("/",""));
        } else {
            mc.player.networkHandler.sendChatMessage(text);
        }
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getBind() {
        return bind;
    }

    public void setBind(int bind) {
        this.bind = bind;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Macro) {
            return (this.getName().equalsIgnoreCase(((Macro) obj).getName()));
        } else {
            return false;
        }
    }

}