package me.melonik.oneblockcore.models;

import java.util.HashMap;
import java.util.Map;

public class Generator {
    private int level;
    private Map<Integer, Integer> levelProgress;
    private double moneyPerSecond;
    private int scrafCount;
    private long lastUpdate;
    private int selectedGeneratorLevel;
    private long lastScrafRemoval;

    public Generator() {
        this.level = 1;
        this.levelProgress = new HashMap<>();
        this.moneyPerSecond = 1.0;
        this.scrafCount = 0;
        this.lastUpdate = System.currentTimeMillis();
        this.selectedGeneratorLevel = 1;
        this.lastScrafRemoval = System.currentTimeMillis();

        // Inicjalizacja progresu dla wszystkich poziomów
        for (int i = 1; i <= 7; i++) {
            levelProgress.put(i, 0);
        }
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getProgress() {
        return levelProgress.getOrDefault(selectedGeneratorLevel, 0);
    }

    public void setProgress(int progress) {
        levelProgress.put(selectedGeneratorLevel, progress);
    }

    public int getProgressForLevel(int level) {
        return levelProgress.getOrDefault(level, 0);
    }

    public void setProgressForLevel(int level, int progress) {
        levelProgress.put(level, progress);
    }

    public Map<Integer, Integer> getLevelProgress() {
        return levelProgress;
    }

    public void setLevelProgress(Map<Integer, Integer> levelProgress) {
        this.levelProgress = levelProgress;
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

    public long getLastScrafRemoval() {
        return lastScrafRemoval;
    }

    public void setLastScrafRemoval(long lastScrafRemoval) {
        this.lastScrafRemoval = lastScrafRemoval;
    }

    public void checkAndRemoveScrafs() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScrafRemoval >= 1000) {
            if (scrafCount > 0) {
                removeScraf(1);
            }
            lastScrafRemoval = currentTime;
        }
    }
}