package me.melonik.oneblockcore.models;

public class IslandUpgrades {
    private int borderSize;
    private int spawnerLimit;
    private int hopperLimit;
    private int memberLimit;
    private int pistonLimit;
    private int spawnerCount;
    private int hopperCount;
    private int pistonCount;

    public IslandUpgrades() {
        this.borderSize = 50;
        this.spawnerLimit = 1;
        this.hopperLimit = 150;
        this.memberLimit = 2;
        this.pistonLimit = 10;
        this.spawnerCount = 0;
        this.hopperCount = 0;
        this.pistonCount = 0;
    }

    public int getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
    }

    public int getSpawnerLimit() {
        return spawnerLimit;
    }

    public void setSpawnerLimit(int spawnerLimit) {
        this.spawnerLimit = spawnerLimit;
    }

    public int getHopperLimit() {
        return hopperLimit;
    }

    public void setHopperLimit(int hopperLimit) {
        this.hopperLimit = hopperLimit;
    }

    public int getMemberLimit() {
        return memberLimit;
    }

    public void setMemberLimit(int memberLimit) {
        this.memberLimit = memberLimit;
    }

    public int getPistonLimit() {
        return pistonLimit;
    }

    public void setPistonLimit(int pistonLimit) {
        this.pistonLimit = pistonLimit;
    }

    public int getSpawnerCount() {
        return spawnerCount;
    }

    public void setSpawnerCount(int spawnerCount) {
        this.spawnerCount = spawnerCount;
    }

    public int getHopperCount() {
        return hopperCount;
    }

    public void setHopperCount(int hopperCount) {
        this.hopperCount = hopperCount;
    }

    public int getPistonCount() {
        return pistonCount;
    }

    public void setPistonCount(int pistonCount) {
        this.pistonCount = pistonCount;
    }

    public void incrementHopperCount() {
        this.hopperCount++;
    }

    public void decrementHopperCount() {
        if (this.hopperCount > 0) {
            this.hopperCount--;
        }
    }

    public static double getBorderUpgradeCost(int currentSize) {
        switch (currentSize) {
            case 50: return 10000;
            case 100: return 45000;
            case 150: return 150000;
            case 200: return 200000;
            case 250: return 250000;
            default: return -1;
        }
    }

    public static double getSpawnerUpgradeCost(int currentLimit) {
        switch (currentLimit) {
            case 1: return 50000;
            case 2: return 150000;
            case 3: return 500000;
            case 4: return 1000000;
            default: return -1;
        }
    }

    public static double getHopperUpgradeCost(int currentLimit) {
        switch (currentLimit) {
            case 150: return 5000;
            case 300: return 7000;
            case 500: return 10000;
            case 700: return 15000;
            default: return -1;
        }
    }

    public static double getMemberUpgradeCost(int currentLimit) {
        switch (currentLimit) {
            case 2: return 3000;
            case 3: return 5000;
            case 4: return 7000;
            case 5: return 10000;
            case 6: return 15000;
            case 7: return 25000;
            case 8: return 35000;
            case 9: return 50000;
            default: return -1;
        }
    }

    public static double getPistonUpgradeCost(int currentLimit) {
        switch (currentLimit) {
            case 10: return 30000;
            case 20: return 100000;
            case 30: return 150000;
            case 40: return 300000;
            default: return -1;
        }
    }

    public static int getNextBorderSize(int currentSize) {
        switch (currentSize) {
            case 50: return 100;
            case 100: return 150;
            case 150: return 200;
            case 200: return 250;
            case 250: return 300;
            default: return currentSize;
        }
    }

    public static int getNextSpawnerLimit(int currentLimit) {
        if (currentLimit >= 1 && currentLimit < 5) {
            return currentLimit + 1;
        }
        return currentLimit;
    }

    public static int getNextHopperLimit(int currentLimit) {
        switch (currentLimit) {
            case 150: return 300;
            case 300: return 500;
            case 500: return 700;
            case 700: return 1000;
            default: return currentLimit;
        }
    }

    public static int getNextMemberLimit(int currentLimit) {
        if (currentLimit >= 2 && currentLimit < 10) {
            return currentLimit + 1;
        }
        return currentLimit;
    }

    public static int getNextPistonLimit(int currentLimit) {
        switch (currentLimit) {
            case 10: return 20;
            case 20: return 30;
            case 30: return 40;
            case 40: return 50;
            default: return currentLimit;
        }
    }
}