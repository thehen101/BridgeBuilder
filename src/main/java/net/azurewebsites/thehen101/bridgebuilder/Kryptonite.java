package net.azurewebsites.thehen101.bridgebuilder;

import net.azurewebsites.thehen101.bridgebuilder.event.EventManager;
import net.azurewebsites.thehen101.bridgebuilder.keybind.KeybindManager;
import net.azurewebsites.thehen101.bridgebuilder.module.ModuleManager;
import net.azurewebsites.thehen101.bridgebuilder.util.NahrFont;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(name = Kryptonite.MOD_NAME, modid = Kryptonite.MOD_ID, version = Kryptonite.MOD_VERSION)
public class Kryptonite {
	public static final String MOD_NAME = "BridgeBuilder";
	public static final String MOD_ID = "BridgeBuilder";
	public static final String MOD_VERSION = "1.0.0";
	
	private final EventManager eventManager;
	private final KeybindManager keybindManager;
	private final ModuleManager moduleManager;
	private NahrFont defaultFont;
	
	public Kryptonite() {
		this.eventManager = new EventManager(this);
		this.keybindManager = new KeybindManager(this);
		this.moduleManager = new ModuleManager(this, this.keybindManager);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		System.out.println("Loading " + MOD_NAME + " v" + MOD_VERSION);
		this.defaultFont = new NahrFont("Tahoma", 18);
		FMLCommonHandler.instance().bus().register(eventManager);
		MinecraftForge.EVENT_BUS.register(eventManager);
	}

	public ModuleManager getModuleManager() {
		return this.moduleManager;
	}
	
	public KeybindManager getKeybindManager() {
		return this.keybindManager;
	}

	public EventManager getEventManager() {
		return this.eventManager;
	}
	
	public NahrFont getDefaultFont() {
		return this.defaultFont;
	}
}
