package com.sylink.util;

import com.sylink.account.AccountManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Singleton class that manages repeating function timers.
 */
public final class SchedulerManager
{

    // More methods to this class to make it more of a singleton.
    // Also unit testing

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
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    /**
     * Starts all program timers.
     */
    public void startTimers()
    {
        executorService.scheduleAtFixedRate(minuteTimer, 1, 1, TimeUnit.MINUTES);
        executorService.scheduleAtFixedRate(hourTimer, 1, 1, TimeUnit.HOURS);
    }

    /**
     * Closes all timers safely.
     */
    public void stopTimers()
    {
        executorService.shutdown();
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