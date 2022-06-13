package com.sylink.util;

import com.sylink.account.AccountManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class that manages repeating function timers.
 */
public final class SchedulerManager
{

    // ScheduledExecutorService that manages all timers.
    private final static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    /**
     * Starts all program timers.
     */
    public static void startTimers()
    {
        executorService.scheduleAtFixedRate(minuteTimer, 1, 1, TimeUnit.MINUTES);
        executorService.scheduleAtFixedRate(hourTimer, 1, 1, TimeUnit.HOURS);
    }

    /**
     * Closes all timers safely.
     */
    public static void stopTimers()
    {
        executorService.shutdown();
    }

    /**
     * Runnable method that runs every minute.
     */
    private static final Runnable minuteTimer = () ->
    {
        AccountManager.checkConnection();
    };

    /**
     * Runnable method that runs every hour.
     */
    private static final Runnable hourTimer = () ->
    {
        AccountManager.checkAccounts();
    };

}