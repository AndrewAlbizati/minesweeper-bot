package com.github.AndrewAlbizati;

import java.util.Random;

public class Game {
    private final Tile[][] tiles;

    private final int rows;
    private final int cols;
    private final int mines;

    private boolean gameStarted = false;
    private boolean gameEnded = false;

    private long startTime;

    /**
     * Sets up a game of Minesweeper that is ready to be started by the start() method.
     * @param difficulty The difficulty that the game will be set to. Changes the size of the board and amount of bombs.
     */
    public Game(Difficulties difficulty) {
        this.rows = difficulty.rows;
        this.cols = difficulty.columns;
        this.mines = difficulty.mines;

        tiles = new Tile[rows][cols];


        // Create buttons
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile t = new Tile(r, c);

                tiles[r][c] = t;
            }
        }
    }

    /**
     * Starts the timer and shows the game to the player.
     */
    public void start() {
        generateBoard();
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

        Random rand = new Random();


        // Generate bombs
        int minesOnBoard = 0;
        while (minesOnBoard < mines) {
            int x = rand.nextInt(rows);
            int y = rand.nextInt(cols);

            if (tiles[x][y].getHasBomb()) {
                continue;
            }
            tiles[x][y].setHasBomb(true);
            minesOnBoard++;
        }

        // Calculate adjacent bombs for each tile
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = tiles[r][c];
                if (tile.getHasBomb()) {
                    continue;
                }

                int adjacentBombs = 0;

                // Get tiles above
                if (r > 0) {
                    // Top middle tile
                    if (tiles[r - 1][c].getHasBomb()) {
                        adjacentBombs++;
                    }

                    // Top left tile
                    if (c > 0) {
                        if (tiles[r - 1][c - 1].getHasBomb()) {
                            adjacentBombs++;
                        }
                    }

                    // Top right tile
                    if (c < cols - 1) {
                        if (tiles[r - 1][c + 1].getHasBomb()) {
                            adjacentBombs++;
                        }
                    }
                }

                // Get tiles below
                if (r < rows - 1) {
                    // Bottom middle tile
                    if (tiles[r + 1][c].getHasBomb()) {
                        adjacentBombs++;
                    }

                    // Bottom left tile
                    if (c > 0) {
                        if (tiles[r + 1][c - 1].getHasBomb()) {
                            adjacentBombs++;
                        }
                    }

                    // Bottom right tile
                    if (c < cols - 1) {
                        if (tiles[r + 1][c + 1].getHasBomb()) {
                            adjacentBombs++;
                        }
                    }
                }

                // Get left tile
                if (c > 0) {
                    if (tiles[r][c - 1].getHasBomb()) {
                        adjacentBombs++;
                    }
                }

                // Get right tile
                if (c < cols - 1) {
                    if (tiles[r][c + 1].getHasBomb()) {
                        adjacentBombs++;
                    }
                }

                tile.setNumber(adjacentBombs);
            }
        }
    }

    /**
     * Handles when a user right-clicks on a tile. It can place a flag or remove a flag.
     * @param tile The tile that was right-clicked on.
     */
    private void onRightClick(Tile tile) {
        if (tile.getRevealed()) {
            return;
        }

        // Remove flag
        if (tile.getHasFlag()) {
            tile.setText("");
            tile.setHasFlag(false);
        // Place flag
        } else {
            tile.setText("F");
            tile.setHasFlag(true);
        }
        refreshBoard();
    }

    /**
     * Handles when a player left-clicks on a tile. It can win the game, end the game, or reveal tiles.
     * @param tile The tile that was left-clicked on.
     */
    private void onLeftClick(Tile tile) {
        if (tile.getHasFlag()) {
            return; // Ignore when a player left-clicks a tile with a flag
        }

        // Generate new boards until the first tile revealed is a blank space
        // Prevents game from instantly ending
        if (!gameStarted) {
            if (tile.getHasBomb()) {
                generateBoard();
                onLeftClick(tile);
            } else if (tile.getNumber() != 0) {
                generateBoard();
                onLeftClick(tile);
            }
            gameStarted = true;
        }

        if (tile.getHasBomb()) {
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
     * Color tiles, adds "F" to flagged tiles.
     */
    private void refreshBoard() {
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
                    }
                } else if (tile.getHasFlag()) {
                    tile.setText("F");
                }
            }
        }
    }

    /**
     * Determines if the current board has been completed.
     * @return if all normal tiles have been revealed.
     */
    private boolean hasWin() {
        boolean flag = true;
        loop:
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = tiles[r][c];
                // Detect if a normal tile hasn't been revealed
                if (!tile.getHasBomb() && !tile.getRevealed()) {
                    flag = false;
                    break loop;
                }
            }
        }

        return flag;
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

                if (tile.getHasBomb()) {
                    tile.setText(":b:");
                    continue;
                }

                if (tile.getNumber() == 0) {
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
        String[] numbers = {":one:", ":two:", ":three:", ":four:", ":five:", ":six:", ":seven:", ":eight:", ":nine:", ":keycap_ten:"};
        String[] letters = {
                ":regional_indicator_a:",
                ":regional_indicator_b:",
                ":regional_indicator_c:",
                ":regional_indicator_d:",
                ":regional_indicator_e:",
                ":regional_indicator_f:",
                ":regional_indicator_g:",
                ":regional_indicator_h:",
                ":regional_indicator_i:",
                ":regional_indicator_j:",
                ":regional_indicator_k:",
                ":regional_indicator_l:",
                ":regional_indicator_m:",
                ":regional_indicator_n:",
                ":regional_indicator_o:",
                ":regional_indicator_p:",
                ":regional_indicator_q:",
                ":regional_indicator_r:",
                ":regional_indicator_s:",
                ":regional_indicator_t:",
                ":regional_indicator_u:",
                ":regional_indicator_v:",
                ":regional_indicator_w:",
                ":regional_indicator_x:",
                ":regional_indicator_y:",
                ":regional_indicator_z:"};

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
