package net.azurewebsites.thehen101.bridgebuilder.module;

import java.text.SimpleDateFormat;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.network.Packet;

public class ModuleWrapper {
	private final static Minecraft mc = Minecraft.getMinecraft();
	private final static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private final static Random r = new Random();

	protected static Minecraft getMinecraft() {
		return mc;
	}

	protected static EntityPlayerSP getPlayer() {
		return mc.thePlayer;
	}

	protected static PlayerControllerMP getPlayerController() {
		return mc.playerController;
	}

	protected static WorldClient getWorld() {
		return mc.theWorld;
	}

	protected static GameSettings getGameSettings() {
		return mc.gameSettings;
	}

	protected static void addToSendQueue(Packet p) {
		mc.thePlayer.sendQueue.addToSendQueue(p);
	}

	protected static ScaledResolution getScaledResolution() {
		return new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
	}

	protected static String getPlayerDirection() {
		String facing = mc.thePlayer.getHorizontalFacing().toString().toLowerCase();
		return facing.substring(0, 1).toUpperCase() + facing.substring(1);
	}

	protected static String getTimeOfDay() {
		return sdf.format(System.currentTimeMillis());
	}

	protected static Random getRandom() {
		return r;
	}
}