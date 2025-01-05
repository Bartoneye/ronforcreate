package com.solegendary.reignofnether.gamemode;

import com.solegendary.reignofnether.registrars.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class GameModeClientboundPacket {

    public GameMode gameMode;
    public boolean disallowSurvival;

    // sets the gamemode of all players
    // unlocked and reset back to
    public static void setAndLockAllClientGameModes(GameMode mode) {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new GameModeClientboundPacket(mode, false));
    }

    public static void disallowSurvival() {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new GameModeClientboundPacket(GameMode.NONE, true));
    }

    public static void allowSurvival() {
        PacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new GameModeClientboundPacket(GameMode.NONE, false));
    }

    public GameModeClientboundPacket(GameMode gameMode, boolean disallowSurvival) {
        this.gameMode = gameMode;
        this.disallowSurvival = disallowSurvival;
    }

    public GameModeClientboundPacket(FriendlyByteBuf buffer) {
        this.gameMode = buffer.readEnum(GameMode.class);
        this.disallowSurvival = buffer.readBoolean();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.gameMode);
        buffer.writeBoolean(this.disallowSurvival);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);

        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> {
                        if (gameMode != GameMode.NONE) {
                            ClientGameModeHelper.gameModeLocked = true;
                            ClientGameModeHelper.gameMode = this.gameMode;
                        } else if (disallowSurvival) {
                            ClientGameModeHelper.disallowSurvival = true;
                            ClientGameModeHelper.gameModeLocked = true;
                            ClientGameModeHelper.gameMode = GameMode.STANDARD;
                        } else {
                            ClientGameModeHelper.disallowSurvival = false;
                            ClientGameModeHelper.gameModeLocked = false;
                        }
                        success.set(true);
                    });
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}
