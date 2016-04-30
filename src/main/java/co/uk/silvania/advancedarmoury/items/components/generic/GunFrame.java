package co.uk.silvania.advancedarmoury.items.components.generic;

import java.text.DecimalFormat;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import co.uk.silvania.advancedarmoury.AAUtils;
import co.uk.silvania.advancedarmoury.AdvancedArmoury;
import co.uk.silvania.advancedarmoury.IGun;
import co.uk.silvania.advancedarmoury.IMagazine;
import co.uk.silvania.advancedarmoury.gun.inventory.ItemIInventory;
import co.uk.silvania.advancedarmoury.gun.inventory.ItemInventory;
import co.uk.silvania.advancedarmoury.gun.inventory.assault.AssaultContainer;
import co.uk.silvania.advancedarmoury.gun.inventory.assault.AssaultIInventory;
import co.uk.silvania.advancedarmoury.gun.inventory.assault.AssaultMagazineIInventory;
import co.uk.silvania.advancedarmoury.gun.mechanics.FireAutomaticGun;
import co.uk.silvania.advancedarmoury.items.components.asset.Receiver;
import co.uk.silvania.advancedarmoury.items.components.cores.IModifierCore;
import co.uk.silvania.advancedarmoury.items.magazines.ItemMagazine;
import co.uk.silvania.advancedarmoury.network.GunEventPacket;
import co.uk.silvania.advancedarmoury.network.GunGuiPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class GunFrame extends ItemInventory implements IGun {
	
	private Entity pointedEntity;
	private int fireRate = 2;
	private int tickFov;
	private int tickRecoil;
	private int fireCooldownClient;
	FireAutomaticGun gunFire = new FireAutomaticGun();
	
	public int weight;

	public GunFrame() {
		maxStackSize = 1;
		setCreativeTab(AdvancedArmoury.tabComponentsAssault);
		invSize = 32;
	}
	
	@Override
	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack item) {
		ItemIInventory inventory = new AssaultIInventory(item);
		ItemStack round = inventory.getStackInSlot(1);
		if (round == null) {
			if (entityLiving instanceof EntityPlayer) {
				if (!((EntityPlayer)entityLiving).capabilities.isCreativeMode) {
					dryFire(entityLiving.worldObj, entityLiving, round);
				}
			} else {
				dryFire(entityLiving.worldObj, entityLiving, round); //Eventually NPCs will have guns. We gotta know when they dry.
			}
		}
		//Return true cancels the swing and anything after this.
        return true;
    }
	
	@Override
	public String getItemStackDisplayName(ItemStack item) {
		if (item.stackTagCompound != null) {
			String name = item.stackTagCompound.getString("name");
			if (!name.isEmpty()) {
				return AAUtils.formatText(name);
			}
		}
		return ("" + StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(item) + ".name")).trim();
    }
	
	public boolean hasInternals(ItemStack item) {
		if (item.stackTagCompound != null) {
			return item.stackTagCompound.getBoolean("hasInternals");
		}
		return false;
	}
	
	//Gets the total power of all power-effective parts. Does nothing with it.
	public int getPower(ItemStack item) {
		AssaultIInventory inventory = new AssaultIInventory(item);
		int power = 0;
		
		for (int i = 2; i < 13; i++) {
			if (inventory.getStackInSlot(i) != null) {
				if (inventory.getStackInSlot(i).getItem() instanceof ItemComponent) {
					ItemComponent itemComp = (ItemComponent) inventory.getStackInSlot(i).getItem();
					power += itemComp.power;
				}
			}
		}
		return power;
	}
	
	public int getWeight(ItemStack item) {
		AssaultIInventory inventory = new AssaultIInventory(item);
		int weight = 0;
		
		for (int i = 2; i < 20; i++) {
			if (inventory.getStackInSlot(i) != null) {
				if (inventory.getStackInSlot(i).getItem() instanceof ItemComponent) {
					ItemComponent itemComp = (ItemComponent) inventory.getStackInSlot(i).getItem();
					weight += itemComp.weight;
				}
			}
		}
		return weight;
	}
	
	public String parseAccuracy(float acc) {
		DecimalFormat df = new DecimalFormat("#.##");
		float f0 = (Math.abs(acc-1)) * 100;
		String str = df.format(f0);
		String col = "";
		if (f0 >= 90) { col = "\u00a75"; }
		else if (f0 >= 85) { col = "\u00a72"; }
		else if (f0 >= 80) { col = "\u00a7a"; }
		else if (f0 >= 70) { col = "\u00a7e"; }
		else if (f0 >= 60) { col = "\u00a7c"; }
		else { col = "\u00a74"; }
		
		
		return col+"Accuracy: "+str+"%";
	}
	
	public ItemStack damageItem(ItemStack item) {
		System.out.println("Damage gun");
		if (item != null) {
			if (item.getItem() instanceof ItemComponent) {
				ItemComponent itemComponent = (ItemComponent) item.getItem();
				return itemComponent.getContainerItem(item);
			}
		}
		return item;
	}	
	
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean isInHand) {
		if (item.stackTagCompound != null) {
			if (!item.stackTagCompound.getString("tag").isEmpty()) {
				list.add(AAUtils.formatText(item.stackTagCompound.getString("tag")));
				list.add("");
			}
		}

		list.add(parseAccuracy(gunFire.getAccuracy(item)));
		list.add("Actual accuracy: " + gunFire.getAccuracy(item));
		list.add("Weight: " + getWeight(item));
		list.add("Power: " + getPower(item));
	}
	
	/*@Override
	public boolean hasCustomEntity(ItemStack item) {
		return true;
	}
	
	@Override
	public Entity createEntity(World world, Entity entity, ItemStack item) {
		EntityItemGun entityItem = new EntityItemGun(world, entity.posX, entity.posY, entity.posZ, item);
		
		return entityItem;
	}*/
	
	public void onUpdate(ItemStack item, World world, Entity entity, int par4, boolean par5) {		
		if (item.stackTagCompound == null) {
			return;
		}
		
		ItemIInventory inventory = new AssaultIInventory(item);
		ItemStack round = inventory.getStackInSlot(1);
		ItemStack modifier = inventory.getStackInSlot(16);
		
		boolean firing = item.stackTagCompound.getBoolean("firing");
		boolean aiming = item.stackTagCompound.getBoolean("aiming");
		boolean melee = item.stackTagCompound.getBoolean("melee");

		
		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;
			
			if (world.isRemote) {
				Minecraft mc = Minecraft.getMinecraft();
				if (mc.inGameHasFocus) {
					if (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof GunFrame) {
						if (Mouse.isButtonDown(0)) {
							if (round != null || player.capabilities.isCreativeMode) {
								if (!firing) {
									//Not firing. Send packet to start firing.
									AdvancedArmoury.network.sendToServer(new GunEventPacket("fire", ""+!firing));
								}
							}
						} else {
							if (firing) {
								//Is firing. Send packet to STOP firing, reset burst counter.
								AdvancedArmoury.network.sendToServer(new GunEventPacket("fire", ""+!firing));
								System.out.println("Finished firing. Burst was " + item.stackTagCompound.getInteger("burstCount"));
								item.stackTagCompound.setInteger("burstCount", 0);
							}
						}
						
						if (Mouse.isButtonDown(1)) { //This is right-click, still triggers dryfire - switch DF to left-click. TODO
							if (tickFov < 10) {
								mc.gameSettings.fovSetting -= 2;
								tickFov += 2;
							}
							if (!aiming) { AdvancedArmoury.network.sendToServer(new GunEventPacket("aim", ""+!aiming)); }
						} else {
							if (tickFov > 0) {
								mc.gameSettings.fovSetting += 2;
								tickFov -= 2;
							}
							if (aiming) {  AdvancedArmoury.network.sendToServer(new GunEventPacket("aim", ""+!aiming)); }
						}
						
						if (Mouse.isButtonDown(2)) {
							if (!melee) { AdvancedArmoury.network.sendToServer(new GunEventPacket("melee", ""+!melee)); }
						} else {
							if (melee) {  AdvancedArmoury.network.sendToServer(new GunEventPacket("melee", ""+!melee)); }
						}
						
						
						if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
							//TODO make an event that listens for this instead.
							AdvancedArmoury.network.sendToServer(new GunGuiPacket());
						}
					}
				}
			} else {
				//System.out.println("Player Motion, Forward: " + player.moveForward + ", Strafe: " + player.moveStrafing + ", Airbourne: " + player.isAirBorne);
				
				int meleeCooldown = item.stackTagCompound.getInteger("meleeCooldown");
				int fireCooldown = item.stackTagCompound.getInteger("fireCooldown");
				
				if (meleeCooldown > 0) { item.stackTagCompound.setInteger("meleeCooldown", meleeCooldown - 1); }
				if (fireCooldown > 0) { item.stackTagCompound.setInteger("fireCooldown", fireCooldown - 1);
				} else {
					if (firing) {
						if (!player.isSprinting()) {
							IModifierCore mod = null;
							if (modifier != null && modifier.getItem() instanceof IModifierCore) {
								mod = (IModifierCore) modifier.getItem();
								mod.onFireWeapon(item, player);
							}
							gunFire.fire(world, player, item, round, mod);
							if (!player.capabilities.isCreativeMode) {
								//Only remove round if not creative.
								inventory.setInventorySlotContents(1, null);
							}
							item.stackTagCompound.setBoolean("hasFired", true);
							item.stackTagCompound.setInteger("fireCooldown", fireRate);
							
							item.stackTagCompound.setDouble("xPos", player.posX);
							item.stackTagCompound.setDouble("yPos", player.posY);
							item.stackTagCompound.setDouble("zPos", player.posZ);
						}
					}
					if (melee) {
						meleeAttack(player, world);
						item.stackTagCompound.setInteger("meleeCooldown", 5);
					}
				}
			}
			
			if (world.isRemote) {
				/*int recoil = 20;
				
				if (firing && fireCooldownClient <= 0) {
					System.out.println("RECOILING");
					player.setAngles(0, recoil*3);
					tickRecoil += recoil;
					fireCooldownClient = fireRate + 1;
				}
				if (tickRecoil > 0) {
					//System.out.println("Tick: " + tickRecoil);
					//if (tickRecoil % 10 == 0 && !(tickRecoil % 30 == 0)) {
						//System.out.println("Windback");
						player.setAngles(0, -6);
					//}
					tickRecoil -= 2;
				}*/
				
				if (fireCooldownClient > 0) {
					fireCooldownClient--;
				}
			}
			
			if (player.getHeldItem() != null) {
				if (player.getHeldItem().getItem() instanceof GunFrame) {
					AssaultIInventory gunInv = new AssaultIInventory(player.getHeldItem());
					AssaultContainer container = new AssaultContainer(player, player.inventory, gunInv);
					
					ItemStack magazine = gunInv.getStackInSlot(0);
					
					ItemStack bolt = gunInv.getStackInSlot(2);
					ItemStack chargingHandle = gunInv.getStackInSlot(4);
					ItemStack fireSelector = gunInv.getStackInSlot(15);
					ItemStack receiver = gunInv.getStackInSlot(17);
					
					//Take round from magazine, feed it into chamber
					if (magazine != null) {
						if (magazine.getItem() instanceof IMagazine) {
							AssaultMagazineIInventory magInv = new AssaultMagazineIInventory(magazine);
							ItemMagazine.pushUpInventory(magInv);
							
							if (gunInv.getStackInSlot(1) == null) {
								if (magInv.getStackInSlot(0) != null) {
									gunInv.setInventorySlotContents(1, magInv.getStackInSlot(0));
									magInv.setInventorySlotContents(0, null);
									
									//TODO update GUI? None of these seem to work.
									gunInv.markDirty();
									player.inventory.markDirty();
									
									container.detectAndSendChanges();
									player.inventoryContainer.detectAndSendChanges();
								}
							}
						}
					}
					if (receiver != null) {
						if (receiver.getItem() instanceof Receiver) {
							Receiver rec = (Receiver) receiver.getItem();
							//ItemAssaultBoltPart boltItem = (ItemAssaultBoltPart) bolt.getItem();
							ItemComponent chargingHandleItem = (ItemComponent) chargingHandle.getItem();
							ItemFireSelector fireSelectorItem = (ItemFireSelector) fireSelector.getItem();
							
							if (receiver.stackTagCompound == null) {
								receiver.stackTagCompound = new NBTTagCompound();
							}
							
							//receiver.stackTagCompound.setString("boltMaterial", boltItem.material.getRawString());
							receiver.stackTagCompound.setString("chargingHandleMaterial", chargingHandleItem.material);
							receiver.stackTagCompound.setString("fireSelectorMaterial", fireSelectorItem.material);
							item.stackTagCompound.setInteger("weight", getWeight(item));
						}
					}
				}
			}
		}
	}
	
	public void dryFire(World world, EntityLivingBase entity, ItemStack item) {
		world.playSoundAtEntity(entity, "advancedarmoury:dryfire", 3.0F, 1.0F);
	}
	
	public void meleeAttack(EntityPlayer player, World world) {
		int rng = 3;
		if (!world.isRemote) {
			Vec3 lookVec = player.getLookVec();
			Vec3 vec3 = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);
			vec3.yCoord += player.getEyeHeight();

			Vec3 addedVector = vec3.addVector(lookVec.xCoord * rng, lookVec.yCoord * rng, lookVec.zCoord * rng);
			
	        this.pointedEntity = null;
	        List list = world.getEntitiesWithinAABBExcludingEntity(player, player.boundingBox.addCoord(lookVec.xCoord * rng, lookVec.yCoord * rng, lookVec.zCoord * rng).expand(1.0D, 1.0D, 1.0D));
	        double d2 = rng + 1;
	        
	        for (int i = 0; i < list.size(); ++i) {
	            Entity entity = (Entity)list.get(i);

	            if (entity.canBeCollidedWith()) {
	                float f2 = entity.getCollisionBorderSize();
	                AxisAlignedBB axisalignedbb = entity.boundingBox.expand((double)f2, (double)f2, (double)f2);
	                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, addedVector);

	                if (axisalignedbb.isVecInside(vec3)) {
	                    if (0.0D < d2 || d2 == 0.0D) {
	                        this.pointedEntity = entity;
	                        player.attackTargetEntityWithCurrentItem(entity);
	                    }
	                } else if (movingobjectposition != null) {
	                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);

	                    if (d3 < d2 || d2 == 0.0D) {
	                        if (!(entity == player.ridingEntity && !entity.canRiderInteract())) {
	                            this.pointedEntity = entity;
	                            player.attackTargetEntityWithCurrentItem(entity);
	                        }
	                    }
	                }
	            }
	        }
		}
	}
}
