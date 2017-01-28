package onitama.ai;

/**
 * Timer that supports stopping an ongoing search after a fixed time. It also supports suspending/resuming a search,
 * and adjusting the search time once the search has started.
 */
class SearchTimer {
    long searchStartTime;
    long maxTimeMs;
    private boolean timeUp = false;

    volatile boolean requestSuspension = false;
    volatile boolean suspended = false;

    Object SUSPENSION_REQUESTED_LOCK = new Object();
    Object SUSPENDED_LOCK = new Object();

    /** Check for time-out every this number of states, to prevent calling System.currentTimeMillis() for every node. */
    final long timeoutCheckFrequency;

    long nextTimeoutCheckCount;

    /** @param timeoutCheckFrequency See {@link #timeoutCheckFrequency} */
    SearchTimer(long maxTimeMs, long timeoutCheckFrequency) {
        this.maxTimeMs = maxTimeMs;
        this.nextTimeoutCheckCount = this.timeoutCheckFrequency = timeoutCheckFrequency;
        reset();
    }

    void reset() {
        searchStartTime = System.currentTimeMillis();
    }

    boolean timeIsUp(long count) {
        if (timeUp)
            return true;

        // no need to check for suspension/termination too often since it is quite resource intensive
        if (count < nextTimeoutCheckCount)
            return false;

        waitIfSuspended();

        nextTimeoutCheckCount = count + timeoutCheckFrequency;
        return timeUp = elapsedTimeMs() > maxTimeMs;
    }

    void waitIfSuspended() {
        if (requestSuspension) {
            // let caller know that we are suspended
            suspended = true;
            synchronized (SUSPENSION_REQUESTED_LOCK) {
                SUSPENSION_REQUESTED_LOCK.notifyAll();
            }

            // wait until resumed
            synchronized (SUSPENDED_LOCK) {
                while (requestSuspension) {
                    try { SUSPENDED_LOCK.wait(); }
                    catch (InterruptedException ignore) { }
                }
            }

            suspended = false;
        }
    }

    void suspend() {
        requestSuspension = true;

        // wait for the thread to actually suspend itself
        synchronized (SUSPENSION_REQUESTED_LOCK) {
            while (!suspended) {
                try { SUSPENSION_REQUESTED_LOCK.wait(); }
                catch (InterruptedException ignore) { }
            }
        }
    }

    void resume() {
        requestSuspension = false;
        synchronized (SUSPENDED_LOCK) {
            SUSPENDED_LOCK.notifyAll();
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