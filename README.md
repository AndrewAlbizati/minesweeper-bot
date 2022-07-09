# Minesweeper Discord Bot
This bot allows users to play Minesweeper online through Discord.
This bot was written in Java 17 using IntelliJ IDEA and is designed to be used on small Discord servers.

## Setup
1. Add a file titled `config.properties` into the folder.
2. Add the following to the file (replace `{Discord bot token}` with your bot's token)
```
token={Discord bot token}
```
3. If on Windows:
    1. `gradlew build`
    2. `move build\libs\minesweeper-bot-1.0.0.jar .`
    3. `java -jar minesweeper-bot-1.0.0.jar`

4. If on macOS/Linux
    1. `chmod +x gradlew`
    2. `./gradlew build`
    3. `mv build/libs/minesweeper-bot-1.0.0.jar .`
    4. `java -jar minesweeper-bot-1.0.0.jar`

## How to Play
Type **/play** to start a game. Use **/click** on any random tile to begin.
The number on the tiles represents how many tiles that are adjacent contain bombs.
To win the game, the player must have all non-bomb tiles be revealed.

## Commands
### /play
Starts a new game of Minesweeper. Each player can only have 1 game active at a time.

### /click \<position\>
Clicks on a tile at a given position (e.g. A5 or E6). If the tile has a bomb, the game will end. If the tile doesn't have a bomb, the game will remove all adjacent tiles without a bomb.

### /flag \<position\>
Places a flag at a given position (e.g. A5 of E6). Flags cannot be placed on tiles that have already been revealed.

## Dependencies
- Javacord 3.4.0 (https://github.com/Javacord/Javacord)