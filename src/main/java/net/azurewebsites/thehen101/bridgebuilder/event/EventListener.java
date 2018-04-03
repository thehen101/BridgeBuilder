package net.azurewebsites.thehen101.bridgebuilder.event;

import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public interface EventListener {
	public void onUpdate();
	
	public void onRender2D(float partialTicks);
}
