package net.azurewebsites.thehen101.bridgebuilder.keybind;

public final class Keybind {
	private final String name;
	private final int[] keybinds;
	private final Bindable[] bindables;

	public Keybind(String name, int keybind, Bindable bindable) {
		this(name, new int[] { keybind }, new Bindable[] { bindable });
	}

	public Keybind(String name, int keybind, Bindable[] bindables) {
		this(name, new int[] { keybind }, bindables);
	}

	public Keybind(String name, int[] keybinds, Bindable bindable) {
		this(name, keybinds, new Bindable[] { bindable });
	}

	public Keybind(String name, int[] keybinds, Bindable[] bindables) {
		this.keybinds = keybinds;
		this.name = name;
		this.bindables = bindables;
	}

	public String getName() {
		return this.name;
	}

	public int[] getKeybinds() {
		return this.keybinds;
	}

	public Bindable[] getBound() {
		return this.bindables;
	}

	public void dispatchPress() {
		for (Bindable b : this.bindables) {
			b.onKeyEvent();
		}
	}
}