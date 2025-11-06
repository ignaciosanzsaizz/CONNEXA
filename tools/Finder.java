import java.nio.file.*;
import java.util.*;

public class Finder {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get(args[0]);
        String needle = args[1];
        List<String> lines = Files.readAllLines(p);
        for (int i=0;i<lines.size();i++) {
            if (lines.get(i).contains(needle)) {
                int from=Math.max(0,i-3), to=Math.min(lines.size(),i+4);
                System.out.println("-- "+(from+1)+".."+to);
                for (int j=from;j<to;j++) System.out.printf("%04d: %s%n", j+1, lines.get(j));
            }
        }
    }
}

