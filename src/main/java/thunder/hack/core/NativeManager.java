package thunder.hack.core;

import thunder.hack.Thunderhack;
import thunder.hack.utility.render.font.NativeFontRasterizer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

public class NativeManager {
    public void setupNatives() {
        final String libName = System.mapLibraryName("renderer");
        final Path file = ConfigManager.MainFolder.toPath().resolve(libName);
        final URL resource = Thunderhack.class.getResource("/" + libName);

        try (final FileOutputStream stream = new FileOutputStream(file.toFile())) {
            stream.write(Objects.requireNonNull(resource)
                    .openStream()
                    .readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setupNativeLib();
    }

    private void setupNativeLib() {
        NativeFontRasterizer.nativeSetup(new NativeFontRasterizer());
    }
}
