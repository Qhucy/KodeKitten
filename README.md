# KodeKitten

KodeKitten is the Discord Bot implementation of the KodeKitten bot on the Code Mushroom Discord server. It uses the
Java JDA implementation to hook to Discord. This is an open source project and anyone can contribute in the improvement
of this bot.

# Cloning Help

Some unit tests in this project require a connection to a mock discord bot and valid file paths.

1. In Snowflake.java AND Bot.java, update the two arguments in the TEST enums to match to local files on your system
   with the proper information.
2. The test token text file must have a single line with the mock discord bot's token.
3. The test snowflake TOML config must mirror the normal snowflake TOML config but with the correct Discord Snowflake
   Ids for the mock discord server.

# Running KodeKitten

Create a project directory with the following files:

1. KodeKitten.jar (the compiled output of the program)
2. token.txt (the text file that contains the token to connect to Discord with)
3. run.bat (the batch file to run the jar file)

Run the 'run.bat' file to start the program.