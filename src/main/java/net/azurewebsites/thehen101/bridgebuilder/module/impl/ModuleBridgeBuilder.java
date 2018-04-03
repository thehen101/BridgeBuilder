package net.azurewebsites.thehen101.bridgebuilder.module.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.azurewebsites.thehen101.bridgebuilder.module.Module;
import net.azurewebsites.thehen101.bridgebuilder.module.ModuleManager;
import net.azurewebsites.thehen101.bridgebuilder.util.NahrFont.FontType;
import net.azurewebsites.thehen101.coremod.forgepacketmanagement.ForgePacketManagement;
import net.azurewebsites.thehen101.coremod.forgepacketmanagement.event.EventPacketQueued;
import net.azurewebsites.thehen101.coremod.forgepacketmanagement.event.PacketQueueListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

public class ModuleBridgeBuilder extends Module implements PacketQueueListener {
	private static final ResourceLocation LOGO = new ResourceLocation("bridgebuilder", "BridgeBuilder-48px.png");
	private static final double HALF_BLOCK_SIZE = 0.5D;
	private final ArrayList<Packet> packetCache = new ArrayList<Packet>();
	private final ArrayList<FrameEntry> frames = new ArrayList<FrameEntry>();
	private BlockPos lastBlock;
	private EnumFacing side;
	private float rainbow = 0.01F;
	private float prevPartialTicks;
	private boolean switched;

	public ModuleBridgeBuilder(ModuleManager parent, String name, int key, Category category) {
		super(parent, name, key, category);
	}

	@Override
	public void onUpdate() {
		//if we're in a game
		if (getWorld() != null) {
			//get a list of all boundingboxes we collide with if we drop down 0.01 blocks
			//its in a try block because the call crashes sometimes (cba to fix)
			List blocksStoodOn = null;
			try {
				blocksStoodOn = getWorld().getCollidingBoundingBoxes(getPlayer(),
						getPlayer().getEntityBoundingBox().offset(0.0D, -0.01D, 0.0D));
			} catch (Exception e) { }
			//if it's 0, we're not standing on a block. we don't have a block to work from
			if (blocksStoodOn != null && blocksStoodOn.size() != 0) {
				//this code finds the closest block to us (that we're standing on)
				int bestIndex = 0;
				double bestDist = 1337.0D;
				for (int i = 0; i < blocksStoodOn.size(); i++) {
					AxisAlignedBB bb = (AxisAlignedBB) blocksStoodOn.get(i);
					BlockPos bp = new BlockPos(bb.minX, bb.minY, bb.minZ);
					double a = Math.sqrt(((bp.getX()) + HALF_BLOCK_SIZE - getPlayer().posX)
							* ((bp.getX()) + HALF_BLOCK_SIZE - getPlayer().posX)
							+ ((bp.getZ()) + HALF_BLOCK_SIZE - getPlayer().posZ)
									* ((bp.getZ()) + HALF_BLOCK_SIZE - getPlayer().posZ));
					if (a < bestDist) {
						bestDist = a;
						bestIndex = i;
					}
				}
				//set our working block (block to build from) to the closest block we just found
				AxisAlignedBB stood = (AxisAlignedBB) blocksStoodOn.get(bestIndex);
				this.lastBlock = new BlockPos(stood.minX, stood.minY, stood.minZ);
			} else
				this.lastBlock = null;
			
			//If the player switched to an item that we can't bridge with, toggle the mod off
			if (!this.holdingSuitableItem() && this.switched) {
				this.toggle();
				return;
			}
			//Change our item if we're not holding on to a good one
			if (!this.holdingSuitableItem() && !this.switched) {
				int desiredIndex = this.getSuitableIndex(-1);
				if (desiredIndex != -1) {
					getPlayer().inventory.currentItem = desiredIndex;
					queueAndCachePacket(new C09PacketHeldItemChange(getPlayer().inventory.currentItem));
				}
			}
			this.switched = true;
		}
	}
	
	@Override
	public void onRender2D(float partialTicks) {
		//frame counting (more accurate than using debugFPS)
		this.frames.add(new FrameEntry(System.currentTimeMillis()));
		while (this.frames.get(0).time + 1000 < System.currentTimeMillis())
			this.frames.remove(0);
		
		ScaledResolution sr = getScaledResolution();
		final int width = 84;
		final int height = 30;
		Gui.drawRect(sr.getScaledWidth() - width, sr.getScaledHeight() - height, sr.getScaledWidth(),
				sr.getScaledHeight(), 0xA03A3A3A);

		Gui.drawRect(sr.getScaledWidth() - width, sr.getScaledHeight() - height, sr.getScaledWidth(),
				sr.getScaledHeight() - height + 1, 0xff000000);

		// long
		drawRainbowRect(sr.getScaledWidth() - width, sr.getScaledHeight() - height, width, 1);
		drawRainbowRect(sr.getScaledWidth() - width, sr.getScaledHeight() - 1, width, 1);

		// up
		drawRainbowRect(sr.getScaledWidth() - width, sr.getScaledHeight() - height, 1, height);
		drawRainbowRect(sr.getScaledWidth() - 1, sr.getScaledHeight() - height, 1, height);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableAlpha();
		getMinecraft().getTextureManager().bindTexture(LOGO);
		
		final int size = 24;
		drawTexturedRect(sr.getScaledWidth() - width + 2, sr.getScaledHeight() - height + 3,
				size, size, size, size, size, size);

		this.getParent().getKryptonite().getDefaultFont().drawString("BridgeBuilder", sr.getScaledWidth() - width + 28,
				sr.getScaledHeight() - height + 1F, FontType.SHADOW_THIN, 0xFFFFFFFF, 0xFF000000);

		int count = this.getHotbarBlockCount();

		float r = (float) (1 - (count > 64 ? 64 : count) / 64.0F) * 1.7F;
		float g = (float) (((count > 64 ? 64 : count) / 64.0F)) * 1.2F;
		float b = 0.0F;

		if (r > 1.0F)
			r = 1.0F;

		if (g > 1.0F)
			g = 1.0F;

		this.getParent().getKryptonite().getDefaultFont().drawString(count + " block" + (count != 1 ? "s" : ""),
				sr.getScaledWidth() - width + 28, sr.getScaledHeight() - height + 13F, FontType.SHADOW_THIN,
				new Color(r, g, b).getRGB(), 0xFF000000);

		if (this.prevPartialTicks > partialTicks)
			this.prevPartialTicks -= 1.0F;

		float diff = Math.abs(this.prevPartialTicks - partialTicks);
		if (this.frames.size() <= 30)
			rainbow += ((1.0F / this.frames.size()) * 0.5F);
		else
			rainbow += (0.03F * diff);

		this.prevPartialTicks = partialTicks;
	}

	@Override
	public void onPacketQueued(EventPacketQueued event) {
		Packet pa = event.getPacket();
		if (pa instanceof C03PacketPlayer) {
			if (getWorld() != null && getPlayer().onGround && this.getHotbarBlockCount() > 0 && this.lastBlock != null) {
				//if this is one of our packets, ignore it.
				if (this.isCachedPacket(pa))
					return;
				//this is the position of the block we want to actually place
				//(currently it's air (this will be checked)
				BlockPos newPos = new BlockPos(getPlayer().posX, getPlayer().posY - 1, getPlayer().posZ);
				Block under = getWorld().getBlockState(newPos).getBlock();
				//get x and z distance separately
				double distX = Math.abs(getPlayer().posX - (this.lastBlock.getX() + HALF_BLOCK_SIZE));
				double distZ = Math.abs(getPlayer().posZ - (this.lastBlock.getZ() + HALF_BLOCK_SIZE));
				if (this.isBlockPlaceable(under) && this.lastBlock != null) {
					//get the aiming angles for the side of the block we want to place our new block on
					float[] angles = this.getBlockAngles(this.getPlayer(),
							this.getAimLocationFast(getPlayer(), this.lastBlock));
					getPlayer().rotationYaw += MathHelper.wrapAngleTo180_float(angles[0] - getPlayer().rotationYaw);
					getPlayer().rotationPitch = angles[1];
					//if we're a suitable distance away from the centre of the block we are standing on
					if (distX > 0.525D || distZ > 0.525D && this.lastBlock != null) {
						boolean lastBlock = getPlayer().getCurrentEquippedItem().stackSize == 1;
						//make sure the server knows we're aiming at the side of the block
						//we want to place a new block on
						event.setCancelled(true);
						queueAndCachePacket(new C06PacketPlayerPosLook(getPlayer().posX, getPlayer().posY,
								getPlayer().posZ, angles[0], angles[1], getPlayer().onGround));
						// ripped from vanilla source; we can use this hacky code because
						// we're already setting client view angles :)
						Vec3 hit = getMinecraft().objectMouseOver.hitVec;
						float f = 0.0F;
						float f1 = 0.0F;
						float f2 = 0.0F;
						if (getMinecraft().objectMouseOver != null && hit != null) {
							f = (float) (hit.xCoord - 
									(double) this.lastBlock.getX());
							f1 = (float) (hit.yCoord - (double) this.lastBlock.getY());
							f2 = (float) (hit.zCoord - (double) this.lastBlock.getZ());
						}
						
						//place the block in our world
						ItemBlock item = (ItemBlock) getPlayer().inventory.getCurrentItem().getItem();
						item.onItemUse(getPlayer().inventory.getCurrentItem(),
								getPlayer(),
								getWorld(),
								this.lastBlock,
								this.side,
								f1, f2, f2);
						
						//tell the server we wish to place a block and swing our fist
						queueAndCachePacket(new C08PacketPlayerBlockPlacement(this.lastBlock, this.side.getIndex(),
								getPlayer().inventory.getCurrentItem(), f, f1, f2));
						queueAndCachePacket(new C0APacketAnimation());
						
						//Autoswitch to new stack if we just used our last block
						if (lastBlock) {
							int desiredIndex = this.getSuitableIndex(getPlayer().inventory.currentItem);
							if (desiredIndex != -1) {
								getPlayer().inventory.currentItem = desiredIndex;
								queueAndCachePacket(new C09PacketHeldItemChange(getPlayer().inventory.currentItem));
							}
						}
					} else {
						//make sure that we send our updated view angles even if we don't place a block
						event.setCancelled(true);
						queueAndCachePacket(
								new C06PacketPlayerPosLook(getPlayer().posX, getPlayer().posY, getPlayer().posZ,
										getPlayer().rotationYaw, getPlayer().rotationPitch, getPlayer().onGround));
					}
				}
			}
		}
	}

	private double[] getAimLocationFast(EntityLivingBase entity, BlockPos last) {
		double[] pos = new double[] { last.getX(), last.getY() + HALF_BLOCK_SIZE, last.getZ() };

		double[] west = pos.clone();
		west[2] += HALF_BLOCK_SIZE;
		double westDist = Math.sqrt((west[0] - getPlayer().posX) * (west[0] - getPlayer().posX)
				+ (west[2] - getPlayer().posZ) * (west[2] - getPlayer().posZ));

		double[] north = pos.clone();
		north[0] += HALF_BLOCK_SIZE;
		double northDist = Math.sqrt((north[0] - getPlayer().posX) * (north[0] - getPlayer().posX)
				+ (north[2] - getPlayer().posZ) * (north[2] - getPlayer().posZ));

		double[] east = pos.clone();
		east[0] += HALF_BLOCK_SIZE * 2;
		east[2] += HALF_BLOCK_SIZE;
		double eastDist = Math.sqrt((east[0] - getPlayer().posX) * (east[0] - getPlayer().posX)
				+ (east[2] - getPlayer().posZ) * (east[2] - getPlayer().posZ));

		double[] south = pos.clone();
		south[0] += HALF_BLOCK_SIZE;
		south[2] += HALF_BLOCK_SIZE * 2;
		double southDist = Math.sqrt((south[0] - getPlayer().posX) * (south[0] - getPlayer().posX)
				+ (south[2] - getPlayer().posZ) * (south[2] - getPlayer().posZ));

		double smallest = Math.min(westDist, Math.min(northDist, Math.min(eastDist, southDist)));
		if (smallest == westDist) {
			pos[2] -= Math.floor(getPlayer().posZ) - getPlayer().posZ + 0.00001D;
			this.side = EnumFacing.WEST;
		} else if (smallest == northDist) {
			pos[0] += getPlayer().posX - Math.floor(getPlayer().posX) + 0.00001D;
			this.side = EnumFacing.NORTH;
		} else if (smallest == eastDist) {
			pos[0] += HALF_BLOCK_SIZE * 2;
			pos[2] -= Math.floor(getPlayer().posZ) - getPlayer().posZ + 0.00001D;
			this.side = EnumFacing.EAST;
		} else if (smallest == southDist) {
			pos[2] += HALF_BLOCK_SIZE * 2;
			pos[0] += getPlayer().posX - Math.floor(getPlayer().posX) + 0.00001D;
			this.side = EnumFacing.SOUTH;
		}

		return pos;
	}
	
	private float[] getBlockAngles(EntityLivingBase entity, double[] blockPos) {
		final double[] toReturn = new double[2];
		double xDiff = entity.posX - blockPos[0];
		double zDiff = entity.posZ - blockPos[2];
		if ((xDiff < 0.0D && zDiff < 0.0D) || (xDiff < 0.0D && zDiff > 0.0D))
			toReturn[0] = Math.toDegrees(Math.atan(zDiff / xDiff)) - 90.0D;
		else if (xDiff > 0.0D && zDiff < 0.0D) {
			zDiff = blockPos[2] - entity.posZ;
			toReturn[0] = Math.toDegrees(Math.atan(xDiff / zDiff));
		} else if (xDiff > 0.0D && zDiff > 0.0D) {
			xDiff = blockPos[0] - entity.posX;
			toReturn[0] = Math.toDegrees(Math.atan(xDiff / zDiff)) - 180.0D;
		}
		final double xd = entity.posX - blockPos[0];
		final double zd = entity.posZ - blockPos[2];
		double yDiff = (entity.posY + entity.getEyeHeight()) - blockPos[1];
		final double xzDiff = Math.sqrt(xd * xd + zd * zd);
		toReturn[1] = Math.toDegrees(Math.atan(yDiff / xzDiff));
		return new float[] { (float) toReturn[0], (float) toReturn[1] };
	}
	
	private boolean holdingSuitableItem() {
		if (getPlayer().inventory.getCurrentItem() == null)
			return false;
		if (getPlayer().inventory.getCurrentItem().getItem() == null)
			return false;
		Item held = getPlayer().inventory.getCurrentItem().getItem();
		return this.isItemSuitable(held);
	}
	
	private boolean isItemSuitable(Item i) {
		Block blockHeld = Block.getBlockFromItem(i);
		if (blockHeld == null)
			return false;
		if (blockHeld.isFullCube() && !(blockHeld instanceof BlockFalling) && !(blockHeld instanceof BlockContainer)
				&& !(blockHeld instanceof BlockWorkbench))
			return true;
		else
			return false;
	}
	
	private int getSuitableIndex(int ignore) {
		for (int i = 0; i < 9; i++) {
			if (i == ignore)
				continue;
			ItemStack is = getPlayer().inventory.getStackInSlot(i);
			if (is == null)
				continue;
			if (is.stackSize == 0)
				continue;
			if (this.isItemSuitable(is.getItem()))
				return i;
		}
		return -1;
	}
	
	private int getHotbarBlockCount() {
		int count = 0;
		for (int i = 0; i < 9; i++) {
			ItemStack is = getPlayer().inventory.getStackInSlot(i);
			if (is == null)
				continue;
			if (this.isItemSuitable(is.getItem()))
				count += is.stackSize;
		}
		return count;
	}
	
	private void drawRainbowRect(int x, int y, int width, int height) {
		if (rainbow > 1.0F)
			rainbow = 0.0F;

		for (int i = 0; i < width; i++) {
			float hue = (1.f / (float) width) * i;
			hue -= rainbow;
			if (hue < 0.f)
				hue += 1.f;

			int colRainbow = Color.HSBtoRGB(hue, 1F, 1F);
			Gui.drawRect(x + i, y, x + i + 1, y + height, colRainbow);
		}
	}

	public void drawTexturedRect(int x, int y, int u, int v, int width, int height, int textureWidth,
			int textureHeight) {
		float f = 1F / (float) textureWidth;
		float f1 = 1F / (float) textureHeight;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer wr = tessellator.getWorldRenderer();
		wr.startDrawingQuads();
		wr.addVertexWithUV((double) (x), (double) (y + height), 0, (double) ((float) (u) * f),
				(double) ((float) (v + height) * f1));
		wr.addVertexWithUV((double) (x + width), (double) (y + height), 0, (double) ((float) (u + width) * f),
				(double) ((float) (v + height) * f1));
		wr.addVertexWithUV((double) (x + width), (double) (y), 0, (double) ((float) (u + width) * f),
				(double) ((float) (v) * f1));
		wr.addVertexWithUV((double) (x), (double) (y), 0, (double) ((float) (u) * f), (double) ((float) (v) * f1));
		tessellator.draw();
	}

	private boolean isCachedPacket(Packet p) {
		this.pruneCache();
		if (this.packetCache.contains(p))
			return true;
		return false;
	}

	private void pruneCache() {
		while (this.packetCache.size() > 50)
			this.packetCache.remove(0);
	}

	private void queueAndCachePacket(Packet p) {
		this.pruneCache();
		this.packetCache.add(p);
		addToSendQueue(p);
	}

	private boolean isBlockPlaceable(Block block) {
		return block instanceof BlockAir
				|| block instanceof BlockTallGrass;
	}

	@Override
	public void onEnable() {
		this.switched = false;
		for (int i = 0; i < getMinecraft().getDebugFPS(); i++)
			this.frames.add(new FrameEntry(System.currentTimeMillis() 
					- (1000 - ((long) ((float) (i) / (float) (getMinecraft().getDebugFPS()) * 1000.0F)))));
		ForgePacketManagement.INSTANCE.getPacketQueueManager().addListener(this);
		this.getParent().getKryptonite().getEventManager().addListener(this);
	}

	@Override
	public void onDisable() {
		this.packetCache.clear();
		this.frames.clear();
		this.side = null;
		this.rainbow = 0.01F;
		this.prevPartialTicks = 0.0F;
		this.lastBlock = null;
		this.switched = false;
		ForgePacketManagement.INSTANCE.getPacketQueueManager().removeListener(this);
		this.getParent().getKryptonite().getEventManager().removeListener(this);
	}
	
	public class FrameEntry {
		private final long time;
		
		public FrameEntry(long time) {
			this.time = time;
		}
	}
}
