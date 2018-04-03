package net.azurewebsites.thehen101.bridgebuilder.event;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import net.azurewebsites.thehen101.bridgebuilder.Kryptonite;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class EventManager {
	private final Kryptonite bb;
	private final ArrayList<EventListener> eventListeners;
	private final boolean[] keyStates = new boolean[256];
	
	public EventManager(Kryptonite instance) {
		this.bb = instance;
		this.eventListeners = new ArrayList<EventListener>();
	}
	
	public void addListener(EventListener newListener) {
		if (!this.eventListeners.contains(newListener))
			this.eventListeners.add(newListener);
	}
	
	 public void removeListener(EventListener listenerToRemove) {
		this.eventListeners.remove(listenerToRemove);
	}
	
	//
	//FORGE EVENT LISTENING METHODS:
	//
	
	@SubscribeEvent
	public void onLivingUpdateEvent(LivingUpdateEvent event) {
		for (int i = 0; i < this.eventListeners.size(); i++)
			this.eventListeners.get(i).onUpdate();
	}
	
	@SubscribeEvent
	public void onRender2D(RenderGameOverlayEvent.Chat event) {
		for (int i = 0; i < this.eventListeners.size(); i++)
			this.eventListeners.get(i).onRender2D(event.partialTicks);
	}
	
	//Forge is REALLLLLLLLYYY gay, you have to check each key individually yourself 
	//(why make a key event WITHOUT PASSING A FUCKING KEY?!)
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		for (int i = 0; i < this.keyStates.length; i++)
			if (this.checkKey(i))
				this.bb.getKeybindManager().dipatchKeypress(i);
	}
	
	private boolean checkKey(final int key) {
		if (Minecraft.getMinecraft().currentScreen != null)
			return false;
		return Keyboard.getEventKey() > -1
				&& Keyboard.isKeyDown(key) != keyStates[key]
				&& (keyStates[key] = !keyStates[key]);
	}
}
