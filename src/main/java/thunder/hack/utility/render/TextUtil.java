package thunder.hack.utility.render;

import static thunder.hack.modules.Module.mc;

public class TextUtil {
    private final String[] words;
    private String currentWord = "_", currentResult = "_";
    private int arrayIndex = 0, currentIndex = 0;
    private boolean filip = false;

    public TextUtil(String...words) {
        this.words = words;
    }

    @Override
    public String toString() {
        return currentResult;
    }

    public void tick() {
        if (mc.player != null && mc.player.age % (filip ? 6 : 2) != 0)
            return;

        if (!currentWord.isEmpty())
            currentResult = currentWord.substring(0, currentWord.length() - Math.max(currentIndex, 0));
        if (currentIndex >= currentWord.length()) {
            filip = true;
            arrayIndex++;
            if (arrayIndex >= words.length) arrayIndex = 0;
            currentWord = words[arrayIndex];
            currentIndex = currentWord.length();
        }
        if (!filip) currentIndex++;
        else currentIndex--;
        if (currentIndex <= -5) {
            filip = false;
            currentIndex = 0;
        }
    }
}
