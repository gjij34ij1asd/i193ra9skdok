package me.melonik.oneblockcore.models;

public class Generator {
    private int level;
    private int progress;
    private double moneyPerSecond;
    private int scrafCount;
    private long lastUpdate;
    private int selectedGeneratorLevel;

    public Generator() {
        this.level = 1;
        this.progress = 0;
        this.moneyPerSecond = 1.0;
        this.scrafCount = 0;
        this.lastUpdate = System.currentTimeMillis();
        this.selectedGeneratorLevel = 1;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public double getMoneyPerSecond() {
        return moneyPerSecond;
    }

    public void setMoneyPerSecond(double moneyPerSecond) {
        this.moneyPerSecond = moneyPerSecond;
    }

    public int getScrafCount() {
        return scrafCount;
    }

    public void setScrafCount(int scrafCount) {
        this.scrafCount = scrafCount;
        updateMoneyPerSecond();
    }

    public void addScraf(int amount) {
        this.scrafCount += amount;
        updateMoneyPerSecond();
    }

    public void removeScraf(int amount) {
        this.scrafCount = Math.max(0, this.scrafCount - amount);
        updateMoneyPerSecond();
    }

    public void clearScrafs() {
        this.scrafCount = 0;
        updateMoneyPerSecond();
    }

    private void updateMoneyPerSecond() {
        this.moneyPerSecond = 1.0 + (scrafCount * 0.1);
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getSelectedGeneratorLevel() {
        return selectedGeneratorLevel;
    }

    public void setSelectedGeneratorLevel(int selectedGeneratorLevel) {
        this.selectedGeneratorLevel = selectedGeneratorLevel;
    }
}