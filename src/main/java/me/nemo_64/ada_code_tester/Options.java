package me.nemo_64.ada_code_tester;

import com.beust.jcommander.Parameter;

public class Options {

    @Parameter(names = { "-rt", "-max-runtime" }, description = "Maximum amount of seconds that each test can run")
    private int maxRuntime = 5 * 60;

    @Parameter(names = { "-tf", "-test-folder" }, description = "Path to the folder with the tests")
    private String testFolder = "./tests";

    @Parameter(names = { "-te", "-test-extension" }, description = "Extension of the files with a test")
    private String testExtension = ".p";

    @Parameter(names = { "-tse",
            "-test-solution-extension" }, description = "Extension of the files with the solution of a test")
    private String testSolutionExtension = ".p.sol";

    @Parameter(names = { "-ep", "-exe" }, description = "Path to the executable")
    private String executablePath = "bt_bb";

    @Parameter(names = { "-tr", "-runners" }, description = "Amount of worker threads to execute the tests")
    private int testRunnersAmount = 1;

    @Parameter(names = { "-fr", "-frames" }, description = "Seconds to wait before updating the status of the tests")
    private float updateEvery = 1;

    @Parameter(names = { "-summary" }, description = "Summary to show the result")
    private String summary = "summary.txt";

    @Parameter(names = { "-delta",
            "-dt" }, description = "Difference between the expected met and the obtained mep allowed for the test to be considered successfull")
    private double delta = 0.5;

    public int getMaxRuntime() {
        return maxRuntime;
    }

    public String getTestFolder() {
        return testFolder;
    }

    public String getTestExtension() {
        return testExtension;
    }

    public String getTestSolutionExtension() {
        return testSolutionExtension;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public int getTestRunnersAmount() {
        return testRunnersAmount;
    }

    public float getUpdateEvery() {
        return updateEvery;
    }

    public String getSummary() {
        return summary;
    }

    public double getDelta() {
        return delta;
    }
}
