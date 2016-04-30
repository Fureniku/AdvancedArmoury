package co.uk.silvania.advancedarmoury.items.components.generic.assault;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ItemAssaultBoltPart extends ItemAssaultComponent {
	
	String identColour;
	String identId;
	String mat;
	String matColour;
	
	public ItemAssaultBoltPart(String identColour, String identId, String partName, String displayName, double size, String materialName, double dura, int weight, float acc, String col, int rgb, int rate, String oredict) {
		super(partName, displayName, size, materialName, dura, weight, acc, col, rgb, rate, oredict);
		this.identColour = identColour;
		this.identId = identId;
		this.mat = materialName;
		this.matColour = col;
	}
	
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean p_77624_4_) {
		list.add(identColour + "Part Identifier: " + identId);
		list.add("");
		list.add(matColour + "Material: " + mat);
		list.add("");
		list.add("\u00A78" + "Accuracy: N/A");
		list.add("\u00A78" + "Fire Rate: N/A");
		list.add("Power Modifier: " + power);
		list.add("Weight: " + weight);
		list.add("");
		list.add("Cost (Parts): " + cost);
		list.add("Build Time: " + buildTime);
		list.add("");
		list.add("Damage: " + this.getDamage(item) + "/" + this.getMaxDamage());
	}
}
