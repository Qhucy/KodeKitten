package com.sylink.util;

import com.sylink.account.AccountManager;
import lombok.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Singleton class that manages repeating function timers.
 */
public final class SchedulerManager
{

    private static SchedulerManager schedulerManager = null;

    public static SchedulerManager getInstance()
    {
        if (schedulerManager == null)
        {
            schedulerManager = new SchedulerManager();
        }

        return schedulerManager;
    }

    // ScheduledExecutorService that manages all timers.
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);

    /**
     * Starts all continuous background program timers.
     */
    public void startTimers()
    {
        addTimer(minuteTimer, 1, 1, TimeUnit.MINUTES);
        addTimer(hourTimer, 1, 1, TimeUnit.HOURS);
    }

    /**
     * Closes all timers safely.
     */
    public void stopTimers()
    {
        executorService.shutdown();
    }

    /**
     * Adds a new continuous repeating timer to the execution pool.
     */
    public void addTimer(@NonNull final Runnable runnable, final long initialDelay, final long period,
                         @NonNull final TimeUnit timeUnit)
    {
        executorService.scheduleAtFixedRate(runnable, initialDelay, period, timeUnit);
    }

    /**
     * Runs a task after a specified delay.
     */
    public void runDelayed(@NonNull final Runnable runnable, final long delay, @NonNull final TimeUnit timeUnit)
    {
        executorService.schedule(runnable, delay, timeUnit);
    }

    /**
     * Runnable method that runs every minute.
     */
    private final Runnable minuteTimer = () ->
    {
        AccountManager.checkConnection();
    };

    /**
     * Runnable method that runs every hour.
     */
    private final Runnable hourTimer = () ->
    {
        AccountManager.checkAccounts();
    };

}