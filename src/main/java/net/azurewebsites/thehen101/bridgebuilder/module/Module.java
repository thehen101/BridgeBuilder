package net.azurewebsites.thehen101.bridgebuilder.module;

import net.azurewebsites.thehen101.bridgebuilder.event.EventListener;
import net.azurewebsites.thehen101.bridgebuilder.keybind.Bindable;
import net.azurewebsites.thehen101.bridgebuilder.keybind.Keybind;

public abstract class Module extends ModuleWrapper implements EventListener, Bindable {
	private final ModuleManager parent;
	private final String name;
	private final Category category;
	private boolean enabled;
	private Keybind keybind;
	
	public Module(ModuleManager parent, String name, int key, Category category) {
		this.parent = parent;
		this.name = name;
		this.category = category;
		
		Keybind keybind = new Keybind(this.name + " toggle", key, this);
		this.keybind = keybind;
		this.parent.getKeybindManager().addKeybind(keybind);
	}
	
	@Override
	public void onKeyEvent() {
		this.toggle();
	}
	
	public void toggle() {
		this.enabled = !this.enabled;
		if (this.enabled)
			onEnable();
		else
			onDisable();
	}
	
	public abstract void onEnable();
	
	public abstract void onDisable();
	
	public void setKeybind(Keybind newKeybind) {
		this.getParent().getKeybindManager().removeKeybind(this.keybind);
		this.keybind = newKeybind;
		this.getParent().getKeybindManager().addKeybind(this.keybind);
	}
	
	public ModuleManager getParent() {
		return this.parent;
	}
	
	public String getName() {
		return this.name;
	}

	public Keybind getKeybind() {
		return this.keybind;
	}

	public Category getCategory() {
		return this.category;
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}

	public enum Category {
		COMBAT, MOVEMENT, RENDER, WORLD
	}
}
