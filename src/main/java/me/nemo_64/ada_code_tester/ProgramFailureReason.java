package me.nemo_64.ada_code_tester;

public interface ProgramFailureReason {

    ProgramFailureReason TEST_FOLDER_DOES_NOT_EXIST = () -> "The specified test folder does not exist or is not a folder";

    default boolean showUsage() {
        return true;
    }

    default int exitCode() {
        return -1;
    }

    String description();

}
