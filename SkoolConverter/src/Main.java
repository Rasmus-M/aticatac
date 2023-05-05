import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        new SkoolConverter(true).convert(
            new File("zx spectrum/aticatac.skool"),
            new File("src/atic-atac.a99"),
            0x4000
        );
    }
}
