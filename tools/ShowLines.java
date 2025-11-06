import java.nio.file.*;
import java.util.*;

public class ShowLines {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get(args[0]);
        int from = Integer.parseInt(args[1]);
        int to = Integer.parseInt(args[2]);
        List<String> lines = Files.readAllLines(p);
        from = Math.max(1, from); to = Math.min(lines.size(), to);
        for (int i=from; i<=to; i++) {
            System.out.printf("%04d: %s%n", i, lines.get(i-1));
        }
    }
}

