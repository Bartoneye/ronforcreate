package com.solegendary.reignofnether.survival;

import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.building.Building;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.building.BuildingUtils;
import com.solegendary.reignofnether.building.buildings.piglins.Portal;
import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.player.RTSPlayer;
import com.solegendary.reignofnether.sounds.SoundAction;
import com.solegendary.reignofnether.sounds.SoundClientboundPacket;
import com.solegendary.reignofnether.time.TimeUtils;
import com.solegendary.reignofnether.unit.interfaces.Unit;
import com.solegendary.reignofnether.util.Faction;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.solegendary.reignofnether.survival.spawners.IllagerWaveSpawner.spawnIllagerWave;
import static com.solegendary.reignofnether.survival.spawners.MonsterWaveSpawner.spawnMonsterWave;
import static com.solegendary.reignofnether.survival.spawners.PiglinWaveSpawner.spawnPiglinWave;

public class SurvivalServerEvents {

    private static boolean isEnabled = false;
    public static Wave nextWave = Wave.getWave(0);
    private static WaveDifficulty difficulty = WaveDifficulty.EASY;
    private static final ArrayList<WaveEnemy> enemies = new ArrayList<>();
    public static final String ENEMY_OWNER_NAME = "Enemy";
    private static final Random random = new Random();
    public static Faction lastFaction = Faction.NONE;

    public static final ArrayList<WavePortal> portals = new ArrayList<>();

    public static final long TICK_INTERVAL = 10;
    private static long lastTime = -1;
    private static long lastEnemyCount = 0;
    private static long ticks = 0;

    private static ArrayList<Building> lastPortals = new ArrayList<>();

    private static ServerLevel serverLevel = null;

    public static void saveStage(ServerLevel level) {
        SurvivalSaveData survivalData = SurvivalSaveData.getInstance(level);
        survivalData.isEnabled = isEnabled;
        survivalData.waveNumber = nextWave.number;
        survivalData.difficulty = difficulty;
        survivalData.save();
        level.getDataStorage().save();
        ReignOfNether.LOGGER.info("saved survival data in serverevents");
    }

    @SubscribeEvent
    public static void loadWaveData(ServerStartedEvent evt) {
        ServerLevel level = evt.getServer().getLevel(Level.OVERWORLD);
        if (level != null) {
            SurvivalSaveData survivalData = SurvivalSaveData.getInstance(level);
            isEnabled = survivalData.isEnabled;
            nextWave = Wave.getWave(survivalData.waveNumber);
            difficulty = survivalData.difficulty;

            if (isEnabled()) {
                SurvivalClientboundPacket.enableAndSetDifficulty(difficulty);
                SurvivalClientboundPacket.setWaveNumber(nextWave.number);
            }
            ReignOfNether.LOGGER.info("loaded survival data: isEnabled: " + isEnabled());
            ReignOfNether.LOGGER.info("loaded survival data: nextWave: " + nextWave.number);
            ReignOfNether.LOGGER.info("loaded survival data: difficulty: " + difficulty);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END || evt.level.isClientSide() || evt.level.dimension() != Level.OVERWORLD)
            return;

        serverLevel = (ServerLevel) evt.level;

        if (!isEnabled())
            return;

        ticks += 1;
        if (ticks % TICK_INTERVAL != 0)
            return;

        long time = evt.level.getDayTime();
        long normTime = TimeUtils.normaliseTime(evt.level.getDayTime());

        if (!isStarted()) {
            setToGameStartTime();
            return;
        }

        if (lastTime >= 0) {
            if (lastTime <= TimeUtils.DUSK - 600 && normTime > TimeUtils.DUSK - 600) {
                PlayerServerEvents.sendMessageToAllPlayers("survival.reignofnether.dusksoon", true);
                SoundClientboundPacket.playSoundForAllPlayers(SoundAction.RANDOM_CAVE_AMBIENCE);
            }
            if (lastTime <= TimeUtils.DUSK && normTime > TimeUtils.DUSK) {
                PlayerServerEvents.sendMessageToAllPlayers("survival.reignofnether.dusk", true);
                SoundClientboundPacket.playSoundForAllPlayers(SoundAction.RANDOM_CAVE_AMBIENCE);
                setToStartingNightTime();
            }
            if (lastTime <= TimeUtils.DUSK + getDifficultyTimeModifier() + 50 &&
                    normTime > TimeUtils.DUSK + getDifficultyTimeModifier() + 50) {
                startNextWave((ServerLevel) evt.level);
            }
            if (lastTime <= TimeUtils.DAWN && normTime > TimeUtils.DAWN && nextWave.number > 1) {
                PlayerServerEvents.sendMessageToAllPlayers("survival.reignofnether.dawn", true);
                setToStartingDayTime();
            }
        }

        int enemyCount = getCurrentEnemies().size() + portals.size();
        if (enemyCount < lastEnemyCount && enemyCount <= 3) {
            if (enemyCount == 0)
                waveCleared((ServerLevel) evt.level);
            else if (enemyCount == 1) {
                PlayerServerEvents.sendMessageToAllPlayers("survival.reignofnether.remaining_enemies_one");
            } else {
                PlayerServerEvents.sendMessageToAllPlayers("survival.reignofnether.remaining_enemies", false, enemyCount);
            }
        }
        for (WaveEnemy enemy : enemies)
            enemy.tick(TICK_INTERVAL);

        // detect new portals and update portals list accordingly
        List<Building> currentPortals = BuildingServerEvents.getBuildings().stream().filter(b ->
                ENEMY_OWNER_NAME.equals(b.ownerName) && !b.ownerName.isBlank() && b instanceof Portal)
                .toList();

        for (Building portal : currentPortals)
            if (!lastPortals.contains(portal))
                SurvivalServerEvents.portals.add(new WavePortal((Portal) portal, nextWave));
        SurvivalServerEvents.portals.removeIf(p -> !currentPortals.contains(p.getPortal()));

        lastPortals.clear();
        lastPortals.addAll(currentPortals);

        for (WavePortal portal : portals)
            portal.tick(TICK_INTERVAL);

        lastTime = normTime;
        lastEnemyCount = enemyCount;
    }

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent evt) {
        evt.getDispatcher().register(Commands.literal("debug-end-wave")
                .executes((command) -> {
                    if (!isEnabled())
                        return 0;
                    PlayerServerEvents.sendMessageToAllPlayers("Ending current wave");
                    ArrayList<WaveEnemy> enemiesCopy = new ArrayList<>(enemies);
                    for (WaveEnemy enemy : enemiesCopy)
                        enemy.getEntity().kill();
                    ArrayList<WavePortal> portalsCopy = new ArrayList<>(portals);
                    for (WavePortal portal : portalsCopy)
                        portal.portal.destroy(serverLevel);
                    return 1;
                }));
        evt.getDispatcher().register(Commands.literal("debug-next-night")
                .executes((command) -> {
                    if (!isEnabled())
                        return 0;
                    PlayerServerEvents.sendMessageToAllPlayers("Advancing to next night and wave");
                    serverLevel.setDayTime(12450);
                    return 1;
                }));
    }

    public static void enable(WaveDifficulty diff) {
        if (!isEnabled()) {
            reset();
            lastEnemyCount = 0;
            difficulty = diff;
            isEnabled = true;
            SurvivalClientboundPacket.enableAndSetDifficulty(difficulty);
            if (serverLevel != null)
                saveStage(serverLevel);
        }
    }

    public static void reset() {
        for (WaveEnemy enemy : enemies)
            enemy.getEntity().kill();
        ArrayList<WavePortal> portalsCopy = new ArrayList<>(portals);
        for (WavePortal portal : portalsCopy)
            portal.portal.destroy(serverLevel);
        nextWave = Wave.getWave(0);
        difficulty = WaveDifficulty.EASY;
        isEnabled = false;
        portals.clear();
        enemies.clear();
        if (serverLevel != null)
            saveStage(serverLevel);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent evt) {
        if (isEnabled()) {
            SurvivalClientboundPacket.enableAndSetDifficulty(difficulty);
            SurvivalClientboundPacket.setWaveNumber(nextWave.number);
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent evt) {
        if (evt.getEntity() instanceof Unit unit &&
                evt.getEntity() instanceof LivingEntity entity &&
                !evt.getLevel().isClientSide &&
                ENEMY_OWNER_NAME.equals(unit.getOwnerName())) {

            enemies.add(new WaveEnemy(unit));
        }
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent evt) {
        if (evt.getEntity() instanceof Unit unit &&
                evt.getEntity() instanceof LivingEntity entity &&
                !evt.getLevel().isClientSide &&
                ENEMY_OWNER_NAME.equals(unit.getOwnerName())) {

            enemies.removeIf(e -> e.getEntity().getId() == entity.getId());
        }
    }

    // standard vanilla length is 20mins for a full day/night cycle (24000)
    // 1min == 1200, but is applied twice per cycle (dawn and dusk), so effectively 1min == 600
    public static long getDifficultyTimeModifier() {
        return switch (difficulty) {
            default -> 3000; // 15mins per day
            case MEDIUM -> 4800; // 12mins per day
            case HARD -> 6600; // 9mins per day
            case EXTREME -> 8400; // 6mins per day
        };
    }

    public static long getDayLength() {
        return 12000 - getDifficultyTimeModifier();
    }

    public static void setToStartingDayTime() {
        serverLevel.setDayTime(TimeUtils.DAWN + getDifficultyTimeModifier());
    }

    public static void setToStartingNightTime() {
        serverLevel.setDayTime(TimeUtils.DUSK + getDifficultyTimeModifier());
    }

    public static void setToGameStartTime() {
        serverLevel.setDayTime(TimeUtils.DUSK + getDifficultyTimeModifier() + 60);
    }

    public static boolean isEnabled() { return isEnabled; }

    public static boolean isStarted() {
        for (RTSPlayer player : PlayerServerEvents.rtsPlayers)
            if (BuildingUtils.getTotalCompletedBuildingsOwned(false, player.name) > 0)
                return true;
        return false;
    }

    public static List<WaveEnemy> getCurrentEnemies() {
        return enemies;
    }

    public static int getTotalEnemyPopulation() {
        int pop = 0;
        for (WaveEnemy waveEnemy : getCurrentEnemies())
            pop += waveEnemy.unit.getPopCost();
        return pop;
    }

    public static boolean isWaveInProgress() {
        return !getCurrentEnemies().isEmpty();
    }

    // triggered at nightfall
    public static void startNextWave(ServerLevel level) {
        SurvivalClientboundPacket.setWaveNumber(nextWave.number);
        saveStage(level);

        switch (lastFaction) {
            case VILLAGERS -> {
                if (random.nextBoolean())
                    spawnMonsterWave(level, nextWave);
                else
                    spawnPiglinWave(level, nextWave);
            }
            case MONSTERS -> {
                if (random.nextBoolean())
                    spawnIllagerWave(level, nextWave);
                else
                    spawnPiglinWave(level, nextWave);
            }
            case PIGLINS -> {
                if (random.nextBoolean())
                    spawnMonsterWave(level, nextWave);
                else
                    spawnIllagerWave(level, nextWave);
            }
            case NONE -> {
                switch (random.nextInt(3)) {
                    case 0 -> spawnMonsterWave(level, nextWave);
                    case 1 -> spawnIllagerWave(level, nextWave);
                    case 2 -> spawnPiglinWave(level, nextWave);
                }
            }
        }
        nextWave = Wave.getWave(nextWave.number + 1);
    }

    // triggered when last enemy is killed
    public static void waveCleared(ServerLevel level) {
        PlayerServerEvents.sendMessageToAllPlayers("survival.reignofnether.wave_cleared", true);
        SoundClientboundPacket.playSoundForAllPlayers(SoundAction.ALLY);
    }
}
