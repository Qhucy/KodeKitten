package com.sylink.util;

import org.junit.jupiter.api.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SchedulerManagerTest
{

    private static SchedulerManager schedulerManager = null;

    @BeforeAll
    static void setUpAll()
    {
        schedulerManager = SchedulerManager.getInstance();
    }

    @Test
    @Order(1)
    void runDelayedTask()
    {
        AtomicInteger balance = new AtomicInteger(0);

        schedulerManager.runDelayed(() -> balance.set(10), 50, TimeUnit.MILLISECONDS);

        assertEquals(0, balance.get());

        assertDoesNotThrow(() -> Thread.sleep(100));
        assertEquals(10, balance.get());
    }

    @Test
    @Order(2)
    void addTimer()
    {
        AtomicInteger balance = new AtomicInteger(0);

        schedulerManager.addTimer(() -> balance.set(balance.get() + 1), 50, 50, TimeUnit.MILLISECONDS);

        assertEquals(0, balance.get());

        assertDoesNotThrow(() -> Thread.sleep(70));
        assertEquals(1, balance.get());

    }

    @Test
    @Order(3)
    void stopTimers()
    {
        AtomicInteger balance = new AtomicInteger(0);

        schedulerManager.addTimer(() -> balance.set(balance.get() + 1), 50, 50, TimeUnit.MILLISECONDS);

        assertEquals(0, balance.get());

        assertDoesNotThrow(() -> Thread.sleep(70));
        assertEquals(1, balance.get());

        schedulerManager.stopTimers();

        assertDoesNotThrow(() -> Thread.sleep(70));
        assertEquals(1, balance.get());
    }

}