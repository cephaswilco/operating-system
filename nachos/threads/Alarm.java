package nachos.threads;

import nachos.machine.*;
import java.util.Queue;
import java.util.ArrayDeque;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
        sleepingThreads = new ArrayDeque<SleepingThread>();
        Machine.timer().setInterruptHandler(new Runnable() {
            public void run() { timerInterrupt(); }
        });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
        KThread.currentThread().yield();
        Queue<SleepingThread> nextQueue = new ArrayDeque<SleepingThread>();
        for(int i = 0; i < sleepingThreads.size(); ++i){
            SleepingThread s = sleepingThreads.remove();
            if(Machine.timer().getTime() > s.wakeTime){
                s.thread.ready();
            }else{
                nextQueue.add(s);
            }
        }
        sleepingThreads = nextQueue;
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) &gt;= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param   x       the minimum number of clock ticks to wait.
     *
     * @see     nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
        sleepingThreads.add(new SleepingThread(KThread.currentThread(), Machine.timer().getTime() + x));
        KThread.yield();
    }

    private Queue<SleepingThread> sleepingThreads = null;

    private class SleepingThread {
        public KThread thread;
        public long wakeTime;

        public SleepingThread(KThread k, long t) {
            thread = k;
            wakeTime = t;
        }
    }
}
