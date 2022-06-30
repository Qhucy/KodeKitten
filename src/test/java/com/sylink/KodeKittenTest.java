package com.sylink;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KodeKittenTest
{

    @Test
    void setupLogger()
    {
        KodeKitten.setupLogger();

        assertEquals("[%1$tF %1$tT] [%4$-7s] %5$s %n", KodeKitten.LOGGING_FORMAT);
    }

    @Test
    void registerCommands()
    {

    }

    @Test
    void readingConsoleCommands()
    {

    }

    @Test
    void savingResources()
    {

    }

    @Test
    void gettingResource()
    {

    }

    @Test
    void log()
    {

    }

    @Test
    void logInfo()
    {

    }

    @Test
    void logWarning()
    {

    }

    @Test
    void logSevere()
    {

    }

}