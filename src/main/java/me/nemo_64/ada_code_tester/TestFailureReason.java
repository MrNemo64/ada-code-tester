package me.nemo_64.ada_code_tester;

public interface TestFailureReason {

    TestFailureReason TIME_OUT = () -> "The test exceeded the maximum allowed time to run";
    TestFailureReason COULD_NOT_PARSE_METS = () -> "Could not parse mets to compare them";

    static TestFailureReason differentMet(double expected, double obtained) {
        return () -> "The obtained output met does not match the expected met. Expected: '"
                + expected + "' Obtained: '" + obtained + "'";
    }

    static TestFailureReason notEnoughOutput(int expectedLines, int obtainedLines) {
        return () -> "The test did not generate enough lines. Expected: "
                + expectedLines + " Obtained: " + obtainedLines;
    }

    static TestFailureReason testDidNotExitSuccessfully(int exitCode) {
        return () -> "The test exited with code " + exitCode;
    }

    static TestFailureReason exception(Exception e) {
        return () -> "The test had a " + e.getClass().getName() + " exception: " + e.getMessage();
    }

    String description();
}
