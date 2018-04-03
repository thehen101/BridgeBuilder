package net.azurewebsites.thehen101.bridgebuilder.module;

import java.util.concurrent.CopyOnWriteArrayList;

import org.lwjgl.input.Keyboard;
import net.azurewebsites.thehen101.bridgebuilder.Kryptonite;
import net.azurewebsites.thehen101.bridgebuilder.keybind.KeybindManager;
import net.azurewebsites.thehen101.bridgebuilder.module.impl.ModuleBridgeBuilder;

public class ModuleManager {
	private final Kryptonite bb;
	private final CopyOnWriteArrayList<Module> moduleList = new CopyOnWriteArrayList<Module>();
	private final KeybindManager keybindManager;
	
	public ModuleManager(Kryptonite bb, KeybindManager keybindManager) {
		this.bb = bb;
		this.keybindManager = keybindManager;
		this.moduleList.add(new ModuleBridgeBuilder(this, "Bridge Builder", Keyboard.KEY_B, Module.Category.MOVEMENT));
		//this.moduleList.add(new ModuleBrightness(this, "Brightness", Keyboard.KEY_B, Module.Category.RENDER));
		//this.moduleList.add(new ModuleTest(this, "Test", Keyboard.KEY_P, Module.Category.COMBAT));
	}
	
	public Kryptonite getKryptonite() {
		return this.bb;
	}
	
	public CopyOnWriteArrayList<Module> getModuleList() {
		return this.moduleList;
	}
	
	public KeybindManager getKeybindManager() {
		return this.keybindManager;
	}
}