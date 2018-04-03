package net.azurewebsites.thehen101.bridgebuilder.keybind;

public class Keypress {
	private final int key;
	private final long timeDown;

	public Keypress(int key, long timeDown) {
		this.key = key;
		this.timeDown = timeDown;
	}

	public int getKey() {
		return this.key;
	}

	public long getTimeDown() {
		return this.timeDown;
	}
}
