package co.uk.silvania.advancedarmoury.items_old.components.asset;

public class Receiver extends ItemAsset {
	
	public float barrelX;
	public float barrelY;
	public float barrelZ;
	public float stockX;
	public float stockY;
	public float stockZ;
	public float triggerX;
	public float triggerY;
	public float triggerZ;
	public float magX;
	public float magY;
	public float magZ;
	public float attachmentX;
	public float attachmentY;
	public float attachmentZ;
	
	public int magId = 0;
	public boolean topRail = false;
	
	static double size = 19*0.75;

	public Receiver(String name, String modelName, String modelTexture, String iconTexture, String gunType, 
			float xSize, float ySize, float zSize, 
			float barrelX, float barrelY, float barrelZ,
			float stockX, float stockY, float stockZ,
			float triggerX, float triggerY, float triggerZ,
			float magX, float magY, float magZ,
			float attachmentX, float attachmentY, float attachmentZ,
			int magId, boolean topRail,
			int weight, double durability, String materialName, String textCol) {
		
		super(gunType, "\u00A7d", "R", "receiver", size, materialName, textCol);
		this.displayName = name;
		this.modelName = modelName;
		this.modelTexture = modelTexture;
		this.iconTexture = iconTexture;
		
		this.ySize = ySize;
		this.zSize = zSize;
		
		this.barrelX = barrelX;
		this.barrelY = barrelY;
		this.barrelZ = barrelZ;
		this.stockX = stockX;
		this.stockY = stockY;
		this.stockZ = stockZ;
		this.triggerX = triggerX;
		this.triggerY = triggerY;
		this.triggerZ = triggerZ;
		this.magX = magX;
		this.magY = magY;
		this.magZ = magZ;
		this.attachmentX = attachmentX;
		this.attachmentY = attachmentY;
		this.attachmentZ = attachmentZ;
		
		this.magId = magId;
		this.topRail = topRail;
	}

}