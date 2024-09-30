package me.nemo_64.ada_code_tester;

import com.beust.jcommander.JCommander;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {
    private Util() {
    }

    public static final Path TEMP_FOLDER;

    static {
        Path f = null;
        try {
            f = Files.createTempDirectory("ada-code-tester");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        TEMP_FOLDER = f;
    }

    public static final Options OPTIONS = new Options();

    public static JCommander COMMANDER = JCommander.newBuilder()
            .addObject(OPTIONS)
            .build();

    public static final Logger LOGGER = Logger.getLogger("AdaProgramTester");

    public static void processFailureReason(ProgramFailureReason reason) {
        LOGGER.log(Level.WARNING, reason.description());
        if (reason.showUsage())
            COMMANDER.usage();
        System.exit(reason.exitCode());
    }

    public static OptionalDouble parseDouble(String str) {
        try {
            return OptionalDouble.of(Double.parseDouble(str));
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }

    public static String formatMillis(long mills) {
        long secs = mills / 1000;
        long min = secs % 60;
        long sec = secs / 60;
        String str = "";
        if (sec < 10)
            str += "0";
        str += sec + ":";
        if (min < 10)
            str += "0";
        str += min;
        return str;
    }

    public static Set<Integer> stringToSet(String str) {
        return Stream.of(str.split(" "))
                .map(Integer::valueOf)
                .collect(Collectors.toSet());
    }

    public static boolean setsAreEqual(String s, String s1) {
        return stringToSet(s).equals(stringToSet(s1));
    }

    public static boolean booleansAreEqual(double a, double b) {
        return Math.abs(a - b) <= OPTIONS.getDelta();
    }
}