package tech.comfortheart.utils;

public class Timer {
    long startTime;
    public Timer() {
        startTime = System.nanoTime();
    }

    public void reset() {
        startTime = System.nanoTime();
    }

    public double elapsedMillisecs() {
        return elapsedNanoSecs() / 1000_000.0;
    }

    public double elapsedSeconds() {
        return elapsedNanoSecs() / 1000_000_000.0;
    }

    public long elapsedNanoSecs() {
        long endTime = System.nanoTime();
        return endTime - startTime;
    }
}
