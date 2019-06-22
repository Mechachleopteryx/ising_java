package com.matthagy.ising;

import java.util.Random;
import java.util.StringJoiner;

public class Ising {

    private final int lateralSize;
    private final double pairInteraction;
    private final double externalField;

    private final short[] spins;
    private final Random random;

    public Ising(int lateralSize, double pairInteraction, double externalField, long seed) {
        this.lateralSize = lateralSize;
        this.pairInteraction = pairInteraction;
        this.externalField = externalField;

        int size = lateralSize * lateralSize;
        spins = new short[size];
        random = new Random(seed);

        for (int i = 0; i < size; i++) {
            spins[i] = (short) (random.nextBoolean() ? 1 : -1);
        }
    }

    private static int[][] OFFSETS = {
            {0, 1},
            {1, 0},
            {0, -1},
            {-1, 0}
    };

    public boolean advanceSimulation() {
        int i = random.nextInt(lateralSize);
        int j = random.nextInt(lateralSize);
        short spin = spins[i * lateralSize + j];
        short newSpin = (short) -spin;

        double dSpin = (double) (newSpin - spin);
        double dU = -externalField * dSpin;

        for (int offsetI = 0; offsetI < OFFSETS.length; offsetI++) {
            int di = OFFSETS[offsetI][0];
            int dj = OFFSETS[offsetI][1];
            int ni = di == 0 ? i : periodize(i + di);
            int nj = dj == 0 ? j : periodize(j + dj);
            dU += -pairInteraction * (double) spins[ni * lateralSize + nj] * dSpin;
        }

        if (dU <= 0 || Math.exp(-dU) > random.nextDouble()) {
            spins[i * lateralSize + j] = newSpin;
            return true;
        } else {
            return false;
        }
    }

    public String getState() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        for (short spin : spins) {
            if (first) {
                first = false;
            } else {
                stringBuilder.append(",");
            }
            stringBuilder.append(spin);
        }
        return stringBuilder.toString();
    }

    public double netSpinRate() {
        int sum = 0;
        for (int i = 0; i < spins.length; i++) {
            sum += (int) spins[i];
        }
        return (double) sum / (double) spins.length;
    }

    private int periodize(int x) {
        if (x >= lateralSize) {
            return x - lateralSize;
        } else if (x < 0) {
            return x + lateralSize;
        } else {
            return x;
        }
    }

    public static void main(String[] args) {
        final Ising ising = new Ising(
                Integer.parseInt(args[0]),
                Double.parseDouble(args[1]),
                Double.parseDouble(args[2]),
                Long.parseLong(args[3])
        );
        final int analysisRate = Integer.parseInt(args[4]);
        final int cycles = Integer.parseInt(args[5]);

        long steps = 0;
        long accepts = 0;

        for (int cycleIndex = 0; cycleIndex < cycles; cycleIndex++) {
            for (int stepIndex = 0; stepIndex < analysisRate; stepIndex++) {
                if (ising.advanceSimulation())
                    accepts++;
                steps++;
            }
            //System.out.println(ising.getState());
            System.err.printf("steps=%d acc rate=%.3f net spin=%.3f\n",
                    steps,
                    (double) accepts / (double) steps,
                    ising.netSpinRate()
            );
        }
    }
}
