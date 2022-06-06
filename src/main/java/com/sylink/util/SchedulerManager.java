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
    private final static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    /**
     * Starts all program timers.
     */
    public static void startTimers()
    {
        executorService.scheduleAtFixedRate(AccountManager::checkConnection, 0, 1, TimeUnit.MINUTES);
    }

    /**
     * Closes all timers safely.
     */
    public static void stopTimers()
    {
        executorService.shutdown();
    }

}