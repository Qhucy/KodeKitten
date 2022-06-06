package com.sylink.account;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that contains all data about a user account.
 */
public class Account
{

    private final long discordId;

    private final List<String> updateStatements = new ArrayList<>();

    public Account(final long discordId)
    {
        this.discordId = discordId;
    }

    public void loadFromDatabase()
    {

    }

}