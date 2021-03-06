package com.sylink.util;

import com.sylink.Bot;
import com.sylink.util.account.AccountManager;
import com.sylink.util.config.MainConfig;
import lombok.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Singleton class that manages repeating function timers.
 */
public final class SchedulerManager
{

    private static SchedulerManager instance = null;

    public static SchedulerManager getInstance()
    {
        if (instance == null)
        {
            instance = new SchedulerManager();
        }

        return instance;
    }

    // ScheduledExecutorService that manages all timers.
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

    /**
     * Starts all continuous background program timers.
     */
    public void startTimers()
    {
        addTimer(minuteTimer, 1, 1, TimeUnit.MINUTES);
        addTimer(hourTimer, 1, 1, TimeUnit.HOURS);
        addTimer(changeStatus, 0, 10, TimeUnit.MINUTES);
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
        AccountManager.getInstance().cleanupConnectionInactivity();
    };

    /**
     * Runnable method that runs every hour.
     */
    private final Runnable hourTimer = () ->
    {
        AccountManager.getInstance().cleanupAccountInactivity();
    };

    /**
     * Runnable method that randomly changes the status message of the bot.
     */
    private final Runnable changeStatus = () ->
    {
        Bot.MAIN.setStatus(MainConfig.getInstance().getRandomStatusMessage());
    };

}