package com.sylink;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

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
    void savingResources()
    {
        final File file = Paths.get("snowflake.toml").toFile();

        KodeKitten.saveResource("snowflake.toml", file.toPath());

        assertTrue(file.exists());
        assertTrue(file.delete());
    }

    @Test
    void gettingValidResource()
    {
        assertNotNull(KodeKitten.getResource("snowflake.toml"));
    }

    @Test
    void gettingInvalidResource()
    {
        assertNull(KodeKitten.getResource("invalid_resource.toml"));
    }

}