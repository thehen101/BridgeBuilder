package net.azurewebsites.thehen101.bridgebuilder.keybind;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.lwjgl.input.Keyboard;

import net.azurewebsites.thehen101.bridgebuilder.Kryptonite;

public class KeybindManager {
	private final Kryptonite bb;
	private ArrayList<Keybind> keybindList = new ArrayList<Keybind>();
	private ArrayList<Keypress> lastKeysPressed = new ArrayList<Keypress>();
	private int graceTime = 2500; // MS
	
	public KeybindManager(Kryptonite bb) {
		this.bb = bb;
	}

	public void addKeybind(Keybind k) {
		this.keybindList.add(k);
	}

	public void removeKeybind(Keybind k) {
		this.keybindList.remove(k);
	}

	public boolean doesKeybindExist(Keybind k) {
		return this.keybindList.contains(k);
	}

	public ArrayList<Keybind> getList() {
		return this.keybindList;
	}

	public void dipatchKeypress(int key) {
		this.updateKeypressList();
		this.lastKeysPressed.add(new Keypress(key, TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS)));
		for (Keybind k : keybindList) {
			int[] binds = k.getKeybinds();
			int firstIndex = -1;
			for (int i = 0; i < this.lastKeysPressed.size(); i++) {
				Keypress temp = this.lastKeysPressed.get(i);
				if (temp.getKey() == binds[0])
					firstIndex = this.lastKeysPressed.indexOf(temp);
			}
			if (binds.length == 1 && firstIndex != -1) {
				this.lastKeysPressed.clear();
				k.dispatchPress();
			}
			if (binds.length > 1 && firstIndex != -1) {
				int matchedKeys = 0;
				for (int i = 0; i < binds.length; i++)
					if ((firstIndex + i) < this.lastKeysPressed.size())
						if (binds[i] == this.lastKeysPressed.get(firstIndex + i).getKey())
							matchedKeys++;
				
				int downCount = 0;
				for (int currentKey : binds)
					if (Keyboard.isKeyDown(currentKey))
						downCount++;
				
				if (matchedKeys == binds.length)
					if (downCount == binds.length) {
						this.lastKeysPressed.clear();
						k.dispatchPress();
					}
			}
		}
	}

	private void updateKeypressList() {
		long time = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
		for (int i = 0; i < this.lastKeysPressed.size(); i++) {
			Keypress k = this.lastKeysPressed.get(i);
			if (k.getTimeDown() + this.graceTime < time) {
				this.lastKeysPressed.remove(k);
			}
		}
	}
}
