package xyz.lokili.loutils.api;

public interface IAutoRestartManager {
    void start();
    void stop();
    void reload();
    boolean isRunning();
    String getTimeRemaining();
    long[] getTimeRemainingParts();
}
