package com.solegendary.reignofnether.gamemode;

public enum GameMode {
    STANDARD, // Standard RTS match
    SURVIVAL, // Wave survival - left click changes difficulty
    KOTH, // King of the Beacon - left-click places the beacon if one doesn't already exist
    SANDBOX, // Enables mapmaker tools

    NONE // used for packets
}
