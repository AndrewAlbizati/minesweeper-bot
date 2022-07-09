package com.github.AndrewAlbizati;

import org.javacord.api.entity.message.Message;

import java.util.Random;

public class Game {
    private final Tile[][] tiles;
    private Message message;

    private final int rows;
    private final int cols;
    private final int mines;

    private boolean gameStarted = false;
    private boolean gameEnded = false;

    private long startTime;

    /**
     * Sets up a game of Minesweeper that is ready to be started by the start() method.
     */
    public Game() {
        this.rows = 9;
        this.cols = 9;
        this.mines = 10;

        tiles = new Tile[rows][cols];

        // Create tiles
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tiles[r][c] = new Tile(r, c);
            }
        }
        generateBoard();
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * Starts the timer.
     */
    public void start() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Creates a brand-new board with newly and randomly placed bombs. Removes all flags and hides all tiles.
     */
    private void generateBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = tiles[r][c];
                tile.setText(":white_medium_square:");
                tile.setRevealed(false);
                tile.setHasFlag(false);
                tile.setHasBomb(false);
            }
        }

        // Generate bombs
        int minesOnBoard = 0;
        while (minesOnBoard < mines) {

            int x = (int) (Math.random() * rows);
            int y = (int) (Math.random() * cols);

            if (tiles[x][y].hasBomb()) {
                continue;
            }
            tiles[x][y].setHasBomb(true);
            minesOnBoard++;
        }

        // Calculate adjacent bombs for each tile
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = tiles[r][c];
                if (tile.hasBomb()) {
                    continue;
                }

                int adjacentBombs = 0;

                // Get tiles above
                if (r > 0) {
                    // Top middle tile
                    if (tiles[r - 1][c].hasBomb()) {
                        adjacentBombs++;
                    }

                    // Top left tile
                    if (c > 0) {
                        if (tiles[r - 1][c - 1].hasBomb()) {
                            adjacentBombs++;
                        }
                    }

                    // Top right tile
                    if (c < cols - 1) {
                        if (tiles[r - 1][c + 1].hasBomb()) {
                            adjacentBombs++;
                        }
                    }
                }

                // Get tiles below
                if (r < rows - 1) {
                    // Bottom middle tile
                    if (tiles[r + 1][c].hasBomb()) {
                        adjacentBombs++;
                    }

                    // Bottom left tile
                    if (c > 0) {
                        if (tiles[r + 1][c - 1].hasBomb()) {
                            adjacentBombs++;
                        }
                    }

                    // Bottom right tile
                    if (c < cols - 1) {
                        if (tiles[r + 1][c + 1].hasBomb()) {
                            adjacentBombs++;
                        }
                    }
                }

                // Get left tile
                if (c > 0) {
                    if (tiles[r][c - 1].hasBomb()) {
                        adjacentBombs++;
                    }
                }

                // Get right tile
                if (c < cols - 1) {
                    if (tiles[r][c + 1].hasBomb()) {
                        adjacentBombs++;
                    }
                }

                tile.setNumber(adjacentBombs);
            }
        }
    }

    /**
     * Handles when a user add a flag on a tile. It can place a flag or remove a flag.
     * @param row The row of the tile that will be flagged.
     * @param col The column of the tile that will be flagged.
     */
    public void addFlag(int row, int col) {
        Tile tile = tiles[row][col];
        if (tile.getRevealed()) {
            return;
        }

        // Remove flag
        if (tile.getHasFlag()) {
            tile.setText(":white_medium_square:");
            tile.setHasFlag(false);
        // Place flag
        } else {
            tile.setText(":triangular_flag_on_post:");
            tile.setHasFlag(true);
        }
        refreshBoard();
    }

    /**
     * Handles when a player clicks on a tile. It can win the game, end the game, or reveal tiles.
     * @param row The row of the tile to be clicked on.
     * @param col The column of the tile to be clicked on.
     */
    public void onClick(int row, int col) {
        Tile tile = tiles[row][col];
        if (tile.getHasFlag()) {
            return; // Ignore when a player left-clicks a tile with a flag
        }

        // Generate new boards until the first tile revealed is a blank space
        // Prevents game from instantly ending
        if (!gameStarted) {
            if (tile.hasBomb()) {
                generateBoard();
                onClick(tile.getRow(), tile.getColumn());
            } else if (tile.getNumber() != 0) {
                generateBoard();
                onClick(tile.getRow(), tile.getColumn());
            }
            gameStarted = true;
        }

        // Clicked on a tile with a bomb
        if (tile.hasBomb()) {
            revealAllTiles();
            gameEnded = true;
            return;
        }

        if (tile.getNumber() != 0) {
            tile.setRevealed(true);
        } else {
            revealSurroundingTiles(tile);
        }
        refreshBoard();
    }

    /**
     * Recursively reveals all tiles that should be revealed.
     * Reveals all adjacent tiles until it reaches a tile with a number (tile with adjacent bomb).
     * @param tile Tile that a player has clicked.
     */
    private void revealSurroundingTiles(Tile tile) {
        if (tile.getHasFlag()) {
            return;
        }
        tile.setRevealed(true);

        // Get tiles above
        if (tile.getRow() > 0) {
            // Top middle tile
            Tile topMiddle = tiles[tile.getRow() - 1][tile.getColumn()];
            if (topMiddle.getNumber() == 0) {
                if (!topMiddle.getRevealed()) {
                    revealSurroundingTiles(topMiddle);
                }
            } else {
                topMiddle.setRevealed(true);
            }

            // Top left tile
            if (tile.getColumn() > 0) {
                Tile topLeft = tiles[tile.getRow() - 1][tile.getColumn() - 1];
                if (topLeft.getNumber() == 0) {
                    if (!topLeft.getRevealed()) {
                        revealSurroundingTiles(topLeft);
                    }
                } else {
                    topLeft.setRevealed(true);
                }
            }

            // Top right tile
            if (tile.getColumn() < cols - 1) {
                Tile topRight = tiles[tile.getRow() - 1][tile.getColumn() + 1];
                if (topRight.getNumber() == 0) {
                    if (!topRight.getRevealed()) {
                        revealSurroundingTiles(topRight);
                    }
                } else {
                    topRight.setRevealed(true);
                }
            }
        }

        // Get tiles below
        if (tile.getRow() < rows - 1) {
            // Bottom middle tile
            Tile bottomMiddle = tiles[tile.getRow() + 1][tile.getColumn()];
            if (bottomMiddle.getNumber() == 0) {
                if (!bottomMiddle.getRevealed()) {
                    revealSurroundingTiles(bottomMiddle);
                }
            } else {
                bottomMiddle.setRevealed(true);
            }

            // Bottom left tile
            if (tile.getColumn() > 0) {
                Tile bottomLeft = tiles[tile.getRow() + 1][tile.getColumn() - 1];
                if (bottomLeft.getNumber() == 0) {
                    if (!bottomLeft.getRevealed()) {
                        revealSurroundingTiles(bottomLeft);
                    }
                } else {
                    bottomLeft.setRevealed(true);
                }
            }

            // Bottom right tile
            if (tile.getColumn() < cols - 1) {
                Tile bottomRight = tiles[tile.getRow() + 1][tile.getColumn() + 1];
                if (bottomRight.getNumber() == 0) {
                    if (!bottomRight.getRevealed()) {
                        revealSurroundingTiles(bottomRight);
                    }
                } else {
                    bottomRight.setRevealed(true);
                }
            }
        }

        // Get left tile
        if (tile.getColumn() > 0) {
            Tile leftTile = tiles[tile.getRow()][tile.getColumn() - 1];
            if (leftTile.getNumber() == 0) {
                if (!leftTile.getRevealed()) {
                    revealSurroundingTiles(leftTile);
                }
            } else {
                leftTile.setRevealed(true);
            }
        }

        // Get right tile
        if (tile.getColumn() < cols - 1) {
            Tile rightTile = tiles[tile.getRow()][tile.getColumn() + 1];
            if (rightTile.getNumber() == 0) {
                if (!rightTile.getRevealed()) {
                    revealSurroundingTiles(rightTile);
                }
            } else {
                rightTile.setRevealed(true);
            }
        }
    }

    /**
     * Refreshes a board when a player changes any tiles.
     * Color tiles, adds a flag to flagged tiles.
     */
    public void refreshBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = tiles[r][c];
                if (tile.getRevealed()) {
                    if (tile.getNumber() != 0) {
                        tile.setText(switch(tile.getNumber()) {
                            case 1 -> ":one:";
                            case 2 -> ":two:";
                            case 3 -> ":three:";
                            case 4 -> ":four:";
                            case 5 -> ":five:";
                            case 6 -> ":six:";
                            case 7 -> ":seven:";
                            case 8 -> ":eight:";
                            case 9 -> ":nine:";
                            default -> tile.getText();
                        });
                    } else {
                        tile.setText(":black_medium_square:");
                    }
                } else if (tile.getHasFlag()) {
                    tile.setText(":triangular_flag_on_post:");
                }
            }
        }
    }

    /**
     * Determines if the current board has been completed.
     * @return if all normal tiles have been revealed.
     */
    public boolean hasWin() {
        boolean flag = true;
        loop:
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = tiles[r][c];
                // Detect if a normal tile hasn't been revealed
                if (!tile.hasBomb() && !tile.getRevealed()) {
                    flag = false;
                    break loop;
                }
            }
        }

        return flag;
    }

    public boolean hasEnded() {
        return gameEnded;
    }

    /**
     * Reveals all tiles to the player.
     * Bombs are labeled "B", other squares are labeled by their number.
     */
    public void revealAllTiles() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = tiles[r][c];
                tile.setRevealed(true);

                if (tile.hasBomb()) {
                    tile.setText(":b:");
                    continue;
                }

                if (tile.getNumber() == 0) {
                    tile.setText(":black_medium_square:");
                    continue;
                }

                tile.setText(switch(tile.getNumber()) {
                    case 1 -> ":one:";
                    case 2 -> ":two:";
                    case 3 -> ":three:";
                    case 4 -> ":four:";
                    case 5 -> ":five:";
                    case 6 -> ":six:";
                    case 7 -> ":seven:";
                    case 8 -> ":eight:";
                    case 9 -> ":nine:";
                    default -> tile.getText();
                });
            }
        }
    }

    public String toString() {
        String[] numbers = {":one:", ":two:", ":three:", ":four:", ":five:", ":six:", ":seven:", ":eight:", ":nine:"};
        String[] letters = {
                ":regional_indicator_a:",
                ":regional_indicator_b:",
                ":regional_indicator_c:",
                ":regional_indicator_d:",
                ":regional_indicator_e:",
                ":regional_indicator_f:",
                ":regional_indicator_g:",
                ":regional_indicator_h:",
                ":regional_indicator_i:"};

        StringBuilder sb = new StringBuilder();
        sb.append(":black_medium_square: ");
        for (int i = 0; i < rows; i++) {
            sb.append(numbers[i]);
            sb.append(" ");
        }

        sb.append("\n");

        for (int r = 0 ; r < rows; r++) {
            sb.append(letters[r]);
            sb.append(" ");
            for (int c = 0; c < cols; c++) {
                sb.append(tiles[r][c].getText());
                sb.append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
