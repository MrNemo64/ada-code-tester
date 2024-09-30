package me.nemo_64.ada_code_tester;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException {
        var running = new AtomicBoolean(true);
        var killingQueue = new ConcurrentLinkedQueue<Process>();
        new Thread(() -> {
            while (!killingQueue.isEmpty() && running.get()) {
                killingQueue.poll().destroy();
            }
        }).start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Graphics.showCursor();
            running.set(false);
        }));
        Util.COMMANDER.parse(args);
        ForkJoinPool pool = new ForkJoinPool(Util.OPTIONS.getTestRunnersAmount());
        List<IndividualTest> tests = listTests(pool, killingQueue);
        tests.sort(Comparator.comparing(IndividualTest::name, String::compareTo));
        synchronized (Graphics.class) {
            Graphics.cls();
            Graphics.hideCursor();
            tests.forEach(IndividualTest::run);
            while (!allFinished(tests)) {
                draw(tests);
                try {
                    Thread.sleep((long) Util.OPTIONS.getUpdateEvery() * 1000);
                } catch (InterruptedException e) {
                }
            }

            draw(tests);
            Graphics.showCursor();
            Graphics.gotoXY(tests.size() + 3, 0);
        }

        StringWriter writter = new StringWriter();
        for (IndividualTest individualTest : tests) {
            individualTest.writeSummary(writter);
            writter.write("\n\n");
        }
        Files.write(Path.of(Util.OPTIONS.getSummary()), writter.toString().getBytes(StandardCharsets.UTF_8));
        running.set(false);
    }

    public static void draw(List<IndividualTest> tests) {
        for (int i = 0; i < tests.size(); i++) {
            Graphics.gotoXY(i + 2, 5);
            Graphics.clearLine();
            System.out.print(tests.get(i).display());
        }
    }

    public static boolean allFinished(Collection<IndividualTest> c) {
        for (IndividualTest t : c)
            if (!t.finished())
                return false;
        return true;
    }

    public static List<IndividualTest> listTests(ForkJoinPool pool, Queue<Process> killingQueue) throws IOException {
        List<IndividualTest> individualTests = new ArrayList<>();
        Options options = Util.OPTIONS;
        File testFolder = new File(options.getTestFolder());
        if (!(testFolder.exists() && testFolder.isDirectory())) {
            Util.processFailureReason(ProgramFailureReason.TEST_FOLDER_DOES_NOT_EXIST);
        }
        Map<String, List<File>> tests = Stream.of(testFolder.listFiles())
                .filter(f -> f.getName().endsWith(options.getTestExtension())
                        || f.getName().endsWith(options.getTestSolutionExtension()))
                .collect(Collectors.groupingBy(Main::testName));
        for (Map.Entry<String, List<File>> entries : tests.entrySet()) {
            if (entries.getValue().size() != 2)
                continue;
            List<String> expectedOutput;
            File testFile;
            if (entries.getValue().get(0).getName().endsWith(options.getTestSolutionExtension())) {
                expectedOutput = Files.readAllLines(entries.getValue().get(0).toPath());
                testFile = entries.getValue().get(1);
            } else {
                expectedOutput = Files.readAllLines(entries.getValue().get(1).toPath());
                testFile = entries.getValue().get(0);
            }
            individualTests.add(new IndividualTest(testFile.getName(), testFile, expectedOutput, pool, killingQueue));
        }
        return individualTests;
    }

    public static String testName(File f) {
        if (f.getName().endsWith(Util.OPTIONS.getTestSolutionExtension())) {
            return f.getName().substring(0, f.getName().lastIndexOf(Util.OPTIONS.getTestSolutionExtension()));
        } else {
            return f.getName().substring(0, f.getName().lastIndexOf(Util.OPTIONS.getTestExtension()));
        }
    }

}