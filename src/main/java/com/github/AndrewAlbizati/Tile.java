package com.github.AndrewAlbizati;

public class Tile {
    private final int row;
    private final int col;
    public int getRow() {
        return row;
    }
    public int getColumn() {
        return col;
    }

    private boolean revealed = false;
    public boolean getRevealed() {
        return revealed;
    }
    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    private boolean hasBomb = false;
    public boolean getHasBomb() {
        return hasBomb;
    }
    public void setHasBomb(boolean hasBomb) {
        this.hasBomb = hasBomb;
    }

    private boolean hasFlag = false;
    public boolean getHasFlag() {
        return hasFlag;
    }
    public void setHasFlag(boolean hasFlag) {
        this.hasFlag = hasFlag;
    }

    private int number;
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }

    private String text;
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public Tile(int row, int col) {
        this.row = row;
        this.col = col;
    }
}
