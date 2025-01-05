package com.solegendary.reignofnether.survival;

import com.solegendary.reignofnether.gamemode.GameMode;
import com.solegendary.reignofnether.registrars.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class SurvivalServerboundPacket {

    public WaveDifficulty difficulty;

    // copies the gamemode to all other clients
    public static void startSurvivalMode(WaveDifficulty mode) {
        PacketHandler.INSTANCE.sendToServer(new SurvivalServerboundPacket(mode));
    }

    public SurvivalServerboundPacket(WaveDifficulty gameMode) {
        this.difficulty = gameMode;
    }

    public SurvivalServerboundPacket(FriendlyByteBuf buffer) {
        this.difficulty = buffer.readEnum(WaveDifficulty.class);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.difficulty);
    }

    // server-side packet-consuming functions
    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        final var success = new AtomicBoolean(false);
        ctx.get().enqueueWork(() -> {
            SurvivalServerEvents.enable(difficulty);
            success.set(true);
        });
        ctx.get().setPacketHandled(true);
        return success.get();
    }
}