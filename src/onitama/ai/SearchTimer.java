package onitama.ai;

/**
 * Timer that supports stopping an ongoing search after a fixed time. It also supports suspending/resuming a search,
 * and adjusting the search time once the search has started.
 */
class SearchTimer {
    private long searchStartTime;
    private long maxTimeMs;
    private boolean timeUp = false;

    private volatile boolean requestSuspension = false;
    private volatile boolean suspended = false;

    private final Object suspensionRequestedLock = new Object();
    private final Object suspendedLock = new Object();

    /** Check for time-out every this number of states, to prevent calling System.currentTimeMillis() for every node. */
    private final long timeoutCheckFrequency;

    private long nextTimeoutCheckStateCount;

    /** @param timeoutCheckFrequency See {@link #timeoutCheckFrequency} */
    SearchTimer(long maxTimeMs, long timeoutCheckFrequency) {
        this.maxTimeMs = maxTimeMs;
        this.nextTimeoutCheckStateCount = this.timeoutCheckFrequency = timeoutCheckFrequency;
        reset();
    }

    void reset() {
        searchStartTime = System.currentTimeMillis();
    }

    boolean timeIsUp(long nrStatesVisited) {
        if (timeUp)
            return true;

        // no need to check for suspension/termination too often since it is quite resource intensive
        if (nrStatesVisited < nextTimeoutCheckStateCount)
            return false;

        waitIfSuspended();

        nextTimeoutCheckStateCount = nrStatesVisited + timeoutCheckFrequency;
        return timeUp = elapsedTimeMs() > maxTimeMs;
    }

    void waitIfSuspended() {
        if (requestSuspension) {
            // let caller know that we are suspended
            suspended = true;
            synchronized (suspensionRequestedLock) {
                suspensionRequestedLock.notifyAll();
            }

            // wait until resumed
            synchronized (suspendedLock) {
                while (requestSuspension) {
                    try { suspendedLock.wait(); }
                    catch (InterruptedException ignore) { }
                }
            }

            suspended = false;
        }
    }

    void suspend() {
        requestSuspension = true;

        // wait for the thread to actually suspend itself
        synchronized (suspensionRequestedLock) {
            while (!suspended) {
                try { suspensionRequestedLock.wait(); }
                catch (InterruptedException ignore) { }
            }
        }
    }

    void resume() {
        requestSuspension = false;
        synchronized (suspendedLock) {
            suspendedLock.notifyAll();
        }
    }

    void stop() {
        timeUp = true;
    }

    /** Adjust the timeout to let it run for the additional period provided. */
    void setRelativeTimeout(long remainingTimeMs) {
        maxTimeMs = elapsedTimeMs() + remainingTimeMs;
    }

    long elapsedTimeMs() {
        return System.currentTimeMillis() - searchStartTime;
    }
}