package com.solegendary.reignofnether.survival;

import com.solegendary.reignofnether.registrars.EntityRegistrar;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Wave {

    private static final Random random = new Random();

    public int number;
    public int population; // multiplied by number of players
    public int highestUnitTier;

    public Wave(int number, int population, int highestUnitTier) {
        this.number = number;
        this.population = population;
        this.highestUnitTier = highestUnitTier;
    }

    public int getNumPortals() {
        return Math.max(1, 1 + number / 3);
    }

    public static Wave getWave(int number) {
        if (number <= 0)
            return WAVES.get(0);
        if (number > WAVES.size())
            return WAVES.get(WAVES.size() - 1);

        return WAVES.get(number - 1);
    }

    private static final List<Wave> WAVES = List.of(
        new Wave(1, 5, 1),
        new Wave(2, 10, 1),
        new Wave(3, 15, 1),
        new Wave(4, 20, 2),
        new Wave(5, 25, 2),
        new Wave(6, 30, 2),
        new Wave(7, 35, 3),
        new Wave(8, 40, 3),
        new Wave(9, 45, 3),
        new Wave(10, 50, 4), // after this, increase pop geometrically every 2nd wave
        new Wave(11, 60, 4),
        new Wave(12, 70, 4),
        new Wave(13, 85, 5),
        new Wave(14, 100, 5),
        new Wave(15, 120, 5),
        new Wave(16, 140, 5),
        new Wave(17, 165, 5),
        new Wave(18, 190, 5),
        new Wave(19, 220, 5),
        new Wave(20, 250, 5),
        new Wave(21, 285, 5),
        new Wave(22, 320, 5),
        new Wave(23, 360, 5),
        new Wave(24, 400, 5),
        new Wave(25, 445, 5),
        new Wave(26, 490, 5),
        new Wave(27, 540, 5),
        new Wave(28, 590, 5),
        new Wave(29, 645, 5),
        new Wave(30, 700, 5)
    );
}
