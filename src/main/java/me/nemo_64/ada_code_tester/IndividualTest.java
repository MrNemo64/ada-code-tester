package me.nemo_64.ada_code_tester;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.Queue;
import java.util.OptionalDouble;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class IndividualTest implements Runnable {

    private final String testName;
    private final File testFile;
    private final List<String> expectedOutput;
    private final AtomicReference<List<String>> obtainedOutput = new AtomicReference<>();
    private final ForkJoinPool pool;
    private final Queue<Process> killingQueue;
    private final AtomicLong endRuntime = new AtomicLong();
    private final AtomicReference<IndividualTestState> state = new AtomicReference<>(IndividualTestState.WAITING);
    private final AtomicReference<TestFailureReason> failureReason = new AtomicReference<>();
    private final AtomicReference<ForkJoinTask<?>> runningTask = new AtomicReference<>();
    private final AtomicReference<String> gwConf = new AtomicReference<>();

    public IndividualTest(String testName, File testFile, List<String> expectedOutput, ForkJoinPool pool,
            Queue<Process> killingQueue) {
        this.testName = testName;
        this.testFile = testFile;
        this.expectedOutput = expectedOutput;
        this.pool = pool;
        this.killingQueue = killingQueue;
    }

    @Override
    public void run() {
        if (state.get() != IndividualTestState.WAITING)
            return;
        runningTask.set(pool.submit(() -> {
            Process p = null;
            try {
                state.set(IndividualTestState.RUNNING);
                File temp = File.createTempFile(testFile.getName(), ".temp.out", Util.TEMP_FOLDER.toFile());
                p = new ProcessBuilder(Util.OPTIONS.getExecutablePath(), "-f", testFile.getAbsolutePath())
                        .redirectOutput(temp).start();
                endRuntime.set(System.currentTimeMillis() + (Util.OPTIONS.getMaxRuntime() * 1000));
                if (p.waitFor(Util.OPTIONS.getMaxRuntime(), TimeUnit.SECONDS)) {
                    if (p.exitValue() == 0) {
                        checkOutput(Files.readAllLines(temp.toPath()));
                    } else {
                        fail(TestFailureReason.testDidNotExitSuccessfully(p.exitValue()));
                    }
                } else {
                    fail(TestFailureReason.TIME_OUT);
                }
            } catch (IOException | InterruptedException e) {
                fail(TestFailureReason.exception(e));
            } finally {
                if (p != null) {
                    /*
                     * Kill the process off-thread to not block a worker thread just waiting for
                     * the process to be killed
                     * 
                     * This will call `Process#destroy` in an offthread at some point
                     * which is an noop if the process is not alive,
                     * so we can call it regardless of how the process ended
                     * > If the process is not alive, no action is taken.
                     */
                    killingQueue.offer(p);
                }
            }
        }));
    }

    private void checkOutput(List<String> output) {
        obtainedOutput.set(output);
        if (output.size() != expectedOutput.size()) {
            fail(TestFailureReason.notEnoughOutput(expectedOutput.size(), output.size()));
            return;
        }
        OptionalDouble expectedMet = Util.parseDouble(expectedOutput.get(0));
        OptionalDouble obtainedMet = Util.parseDouble(output.get(0));
        if (!expectedMet.isPresent() || !obtainedMet.isPresent()) {
            fail(TestFailureReason.COULD_NOT_PARSE_METS);
            return;
        }

        if (!Util.booleansAreEqual(expectedMet.getAsDouble(), obtainedMet.getAsDouble())) {
            fail(TestFailureReason.differentMet(expectedMet.getAsDouble(), obtainedMet.getAsDouble()));
            return;
        }
        state.set(IndividualTestState.COMPLETED);
        if (!Util.setsAreEqual(expectedOutput.get(1), output.get(1))) {
            gwConf.set(output.get(1));
        }
    }

    private void fail(TestFailureReason reason) {
        state.set(IndividualTestState.FAILED);
        failureReason.set(reason);
        if (runningTask.get() != null)
            runningTask.get().cancel(true);
    }

    public boolean finished() {
        return runningTask.get() != null && runningTask.get().isDone();
    }

    public String display() {
        StringBuilder builder = new StringBuilder();
        if (state.get() == IndividualTestState.FAILED) {
            builder.append(Graphics.RED_COLOR);
        } else if (state.get() == IndividualTestState.COMPLETED) {
            builder.append(Graphics.GREEN_COLOR);
        }
        builder.append(testName)
                .append(": ")
                .append(state.get().toString());
        if (state.get() == IndividualTestState.FAILED) {
            builder.append(": ").append(failureReason.get().description());
        } else if (state.get() == IndividualTestState.RUNNING) {
            builder.append(", remaining runtime: ")
                    .append(Util.formatMillis(endRuntime.get() - System.currentTimeMillis()));
        } else if (state.get() == IndividualTestState.COMPLETED && gwConf.get() != null) {
            builder.append(", found different gateway configuration: ").append(gwConf.get());
        }
        builder.append(Graphics.RESET);
        return builder.toString();
    }

    public void writeSummary(StringWriter out) {
        out.write("# ");
        out.write(testName);
        out.write("\n");
        switch (state.get()) {
            case COMPLETED:
                out.write("Ok");
                break;
            case FAILED:
                out.write("Failed\n");
                out.write("- Expected\n");
                expectedOutput.forEach((s) -> {
                    out.write(s);
                    out.write("\n");
                });
                out.write("- Obtained\n");
                if (obtainedOutput.get() != null) {
                    obtainedOutput.get().forEach((s) -> {
                        out.write(s);
                        out.write("\n");
                    });
                } else {
                    out.write("A test error: " + failureReason.get().description());
                }
                break;
            case RUNNING:
                out.write("Running");
                break;
            case WAITING:
                out.write("Waiting");
                break;
        }
    }

    public String name() {
        return testName;
    }
}
