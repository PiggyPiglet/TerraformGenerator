package org.terraform.tree;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.util.Vector;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.v1_15_R1.BeeHiveSpawner;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Version;

public class FractalTreeBuilder {
	
	int height = 0;
	SimpleBlock top;
	
	float baseThickness = 3;
	int baseHeight = 7;
	float thicknessDecrement = 0.5f;
	int maxDepth = 4;
	float leafRadiusX = 2;
	float leafRadiusY = 2;
	float leafRadiusZ = 2;
	float lengthDecrement = 1;
	Material logType = Material.OAK_WOOD;
	Material leafType = Material.OAK_LEAVES;
	Random rand;
	double minBend = 0.8*Math.PI/6;
	double maxBend = 1.2*Math.PI/6;
	int heightVar = 0;
	double initialTilt = 0;
	boolean snowy = false;
	boolean alwaysOneStraight = false;
	double beeChance = 0.0f;
	int vines = 0;
	int cocabeans = 0;
	int fractalThreshold = 1;
	int fractalsDone = 0;
	double maxPitch = 9999;
	double minPitch = -9999;
	double maxYaw = 9999;
	
	public FractalTreeBuilder(FractalTreeType ftt){
		switch(ftt){
		case FOREST:
			this.setBeeChance(TConfigOption.ANIMALS_BEE_HIVEFREQUENCY.getDouble()).setBaseHeight(10).setBaseThickness(3).setThicknessDecrement(0.5f).setMaxDepth(4).setLeafRadius(3).setHeightVar(2);
			break;
		case NORMAL_SMALL:
			this.setBeeChance(TConfigOption.ANIMALS_BEE_HIVEFREQUENCY.getDouble()).setBaseHeight(5).setBaseThickness(1).setThicknessDecrement(1f).setMaxDepth(1).setLeafRadius(3).setHeightVar(1);
			break;
		case BIRCH_BIG:
			this.setBaseHeight(6).setBaseThickness(1).setThicknessDecrement(0f).setMaxDepth(4).setLeafRadiusX(3).setLeafRadiusZ(3).setLeafRadiusY(2).setHeightVar(2).setMinBend(0.9*Math.PI/6).setMaxBend(1.1*Math.PI/6).setLengthDecrement(0.5f).setLeafType(Material.BIRCH_LEAVES).setLogType(Material.BIRCH_WOOD);
			break;
		case BIRCH_SMALL:
			this.setBaseHeight(3).setBaseThickness(1).setThicknessDecrement(0f).setMaxDepth(3).setLeafRadiusX(3).setLeafRadiusZ(3).setLeafRadiusY(1).setHeightVar(1).setMinBend(0.9*Math.PI/6).setMaxBend(1.1*Math.PI/6).setLengthDecrement(0.5f).setLeafType(Material.BIRCH_LEAVES).setLogType(Material.BIRCH_WOOD);
			break;
		case SAVANNA_SMALL: 
			this.setBaseHeight(7).setBaseThickness(1).setThicknessDecrement(0).setMaxDepth(2).setLeafRadiusX(4).setLeafRadiusZ(4).setLeafRadiusY(1).setLogType(Material.ACACIA_LOG).setLeafType(Material.ACACIA_LEAVES).setMinBend(0.5*Math.PI/2).setMaxBend(0.8*Math.PI/2).setLengthDecrement(1).setHeightVar(1);
			break;
		case JUNGLE_BIG:
			this.setBaseHeight(15).setBaseThickness(5).setThicknessDecrement(1f).setMaxDepth(3).setLeafRadiusX(4).setLeafRadiusY(1).setLeafRadiusZ(4).setHeightVar(6).setMaxBend(Math.PI/6).setLengthDecrement(2).setVines(7).setLogType(Material.JUNGLE_WOOD).setLeafType(Material.JUNGLE_LEAVES).setCocabeans(3);
			break;
		case JUNGLE_SMALL:
			this.setBaseHeight(3).setBaseThickness(3).setThicknessDecrement(1f).setMaxDepth(3).setLeafRadiusX(3).setLeafRadiusY(2).setLeafRadiusZ(3).setHeightVar(1).setMaxBend(Math.PI / 6).setLengthDecrement(0.1f).setVines(3).setLogType(Material.JUNGLE_WOOD).setLeafType(Material.JUNGLE_LEAVES).setCocabeans(1);
			break;
		case SAVANNA_BIG: 
			this.setBaseHeight(15)
			.setBaseThickness(20)
			.setThicknessDecrement(5.5f)
			.setMaxDepth(4)
			.setLeafRadiusX(4f)
			.setLeafRadiusZ(4f)
			.setLeafRadiusY(1.5f)
			.setLogType(Material.ACACIA_LOG)
			.setLeafType(Material.ACACIA_LEAVES)
			//.setMinBend(0.5*Math.PI/2)
			//.setMaxBend(0.8*Math.PI/2)
			.setLengthDecrement(0.5f)
			.setHeightVar(3);
			break;
		case WASTELAND_BIG: 
			this.setBaseHeight(6)
			.setBaseThickness(4)
			.setThicknessDecrement(1f)
			.setMaxDepth(4)
			.setLeafRadius(0)
			.setLogType(Material.SPRUCE_WOOD)
			.setLeafType(Material.AIR)
			.setLengthDecrement(0.5f)
			//.setGnarl(5)
			.setHeightVar(1);
			//.setInitialTilt(Math.PI/6);
			break;
		case TAIGA_BIG: 
			this.setBaseHeight(10).setBaseThickness(4)
					.setThicknessDecrement(0.5f)
					.setMaxDepth(4).setLeafRadiusX(4)
					.setLeafRadiusZ(4).setLeafRadiusY(2)
					.setLogType(Material.SPRUCE_WOOD)
					.setLeafType(Material.SPRUCE_LEAVES)
					.setLengthDecrement(3)
					.setHeightVar(3)
					.setAlwaysOneStraight(true)
					.setMinBend(1.4*Math.PI/6)
					.setMaxBend(1.5*Math.PI/6);
			break;
		case TAIGA_SMALL:
			this.setBaseHeight(7).setBaseThickness(1).setMaxDepth(1).setLeafRadiusX(4).setLeafRadiusZ(4).setLeafRadiusY(1).setLogType(Material.SPRUCE_WOOD).setLeafType(Material.SPRUCE_LEAVES).setHeightVar(1);
			break;
		case BROWN_MUSHROOM_BASE: 
			this.setBaseHeight(3)
			.setBaseThickness(3)
			.setThicknessDecrement(0)
			.setMaxDepth(2)
			.setFractalThreshold(4)
			.setLeafRadius(0)
			.setLogType(Material.MUSHROOM_STEM)
			.setLeafType(Material.AIR)
			.setLengthDecrement(0)
			.setMinBend(0.2*Math.PI/6)
			.setMaxBend(0.4*Math.PI/6);
			break;
		case RED_MUSHROOM_BASE: 
			this.setBaseHeight(3)
			.setBaseThickness(3.5f)
			.setThicknessDecrement(0)
			.setMaxDepth(1)
			.setFractalThreshold(4)
			.setLeafRadius(0)
			.setLogType(Material.MUSHROOM_STEM)
			.setLeafType(Material.AIR)
			.setLengthDecrement(0)
			.setMinBend(0.4*Math.PI/6)
			.setMaxBend(0.6*Math.PI/6);
			break;
		case SWAMP_BOTTOM:
			this.setBaseHeight(1).setBaseThickness(3).setThicknessDecrement(0.5f).setMaxDepth(3).setLeafRadius(0).setLogType(Material.OAK_WOOD).setLeafType(Material.OAK_LEAVES).setLengthDecrement(-2f).setMaxBend(-Math.PI/6).setMinBend(-Math.PI/3);
			break;
		case SWAMP_TOP:
			this.setBaseHeight(8).setBaseThickness(3).setThicknessDecrement(0.5f).setMaxDepth(4).setLengthDecrement(0f).setLeafRadiusX(6).setLeafRadiusZ(6).setLeafRadiusY(2).setHeightVar(2).setLogType(Material.OAK_WOOD).setVines(7);
			break;
		case COCONUT_TOP:
			this.setBaseHeight(8)
			.setInitialTilt(Math.PI/6)
			.setBaseThickness(1)
			.setThicknessDecrement(0f)
			.setMaxDepth(1)
			.setLeafRadius(3)
			.setLeafRadiusY(1.2f)
			.setLengthDecrement(2)
			.setHeightVar(1)
			.setVines(3)
			.setLogType(Material.JUNGLE_WOOD);
			break;
		case GIANT_PUMPKIN:
			this.setBaseHeight(6)
			.setBaseThickness(1)
			.setThicknessDecrement(1f)
			.setMaxDepth(0)
			.setLeafRadius(4)
			.setLengthDecrement(-0.5f)
			.setHeightVar(0)
			.setLogType(Material.OAK_LOG)
			.setLeafType(Material.PUMPKIN);
		case DARK_OAK_SMALL:
			this.setBaseHeight(1).setBaseThickness(2)
			.setThicknessDecrement(0.5f)
			.setMaxDepth(3).setLeafRadiusX(2)
			.setLeafRadiusZ(2).setLeafRadiusY(1)
			.setLogType(Material.DARK_OAK_WOOD)
			.setLeafType(Material.DARK_OAK_LEAVES)
			.setLengthDecrement(0)
			.setHeightVar(0)
			.setFractalThreshold(4)
			.setMaxBend(1.4*Math.PI/6)
			.setMinBend(1*Math.PI/6)
			.setMaxPitch(Math.PI)
			.setMinPitch(0);
			break;
		case DARK_OAK_BIG_TOP:
			this.setBaseHeight(6).setBaseThickness(8).setThicknessDecrement(2.5f).setMaxDepth(3).setLeafRadiusX(4).setLeafRadiusZ(4).setLeafRadiusY(2).setLogType(Material.DARK_OAK_WOOD).setLeafType(Material.DARK_OAK_LEAVES).setLengthDecrement(0).setHeightVar(1).setFractalThreshold(4).setMaxBend(1.4*Math.PI/6).setMinBend(1*Math.PI/6).setMaxPitch(Math.PI).setMinPitch(0);
			break;
		case DARK_OAK_BIG_BOTTOM:
			this.setBaseHeight(4).setBaseThickness(4).setThicknessDecrement(0f).setMaxDepth(3).setLeafRadiusX(0).setLeafRadiusZ(0).setLeafRadiusY(0).setLogType(Material.DARK_OAK_WOOD).setLeafType(Material.DARK_OAK_LEAVES).setLengthDecrement(-1).setHeightVar(1).setFractalThreshold(5).setMaxBend(2.3*Math.PI/6).setMinBend(2.0*Math.PI/6);
			break;
		case FROZEN_TREE_BIG:
			this.setBaseHeight(4).setBaseThickness(4)
			.setThicknessDecrement(2f)
			.setMaxDepth(4).setLeafRadiusX(4)
			.setVines(4)
			.setLeafRadiusZ(4).setLeafRadiusY(2)
			.setLogType(Material.SPRUCE_WOOD)
			.setLeafType(Material.ICE)
			.setLengthDecrement(0)
			.setHeightVar(1)
			.setFractalThreshold(4)
			.setMaxBend(1.6*Math.PI/6)
			.setMinBend(1.2*Math.PI/6)
			.setMaxPitch(Math.PI)
			.setMinPitch(0);
			break;
		case FROZEN_TREE_SMALL:
			this.setBaseHeight(1).setBaseThickness(2)
			.setThicknessDecrement(0.2f)
			.setMaxDepth(4).setLeafRadiusX(4)
			.setVines(4)
			.setLeafRadiusZ(4).setLeafRadiusY(1)
			.setLogType(Material.SPRUCE_WOOD)
			.setLeafType(Material.ICE)
			.setLengthDecrement(0)
			.setHeightVar(0)
			.setFractalThreshold(4)
			.setMaxBend(1.6*Math.PI/6)
			.setMinBend(1.2*Math.PI/6)
			.setMaxPitch(Math.PI)
			.setMinPitch(0);
			break;
		}
	}
	
	public FractalTreeBuilder(){}
	
	int oriX;
	int oriY;
	int oriZ;
	
	private boolean spawnedBees = false;
	
	public void build(TerraformWorld tw, PopulatorDataAbstract data, int x, int y, int z){
		this.oriX = x;
		this.oriY = y;
		this.oriZ = z;
		this.rand = tw.getRand(16*16*x+16*y+z);
		SimpleBlock base = new SimpleBlock(data,x,y,z);
		if(this.top == null) top = base;
		double angle = Math.PI/2+GenUtils.randDouble(rand, -initialTilt, initialTilt);
		
		fractalBranch(rand,base,
				angle,
				GenUtils.randDouble(rand, -initialTilt, initialTilt),
				0,baseThickness,
				baseHeight+GenUtils.randInt(-heightVar, heightVar));
		
		if(alwaysOneStraight){
			for(int i = 1; i <= maxDepth; i++){

//				double minBend = 0.8*Math.PI/6;
//				double maxBend = 1.2*Math.PI/6;
				//this.minBend -= 0.1*Math.PI/6;
				//this.maxBend -= 0.1*Math.PI/6;
				fractalBranch(rand,base.getRelative(0,baseHeight*i,0),
						angle,
						GenUtils.randDouble(rand, -initialTilt, initialTilt),
						0,baseThickness,
						baseHeight);
			}
		}
	}
	
	public void fractalBranch(Random rand,SimpleBlock base, double pitch, double yaw, int depth, double thickness, double size){

		if(pitch > maxPitch) {
			//reset pitch
			pitch = maxPitch-rta();
		}else if(pitch < minPitch){
			pitch = minPitch+rta();
		}
		
		if(depth >= maxDepth){
			replaceSphere(rand.nextInt(9999), leafRadiusX, leafRadiusY, leafRadiusZ, base, this.leafType);
			return;  
		}
		if(size <= 0){
			replaceSphere(rand.nextInt(9999), leafRadiusX, leafRadiusY, leafRadiusZ, base, this.leafType);
			return;
		}
			
		int y = (int) (Math.round(size*Math.sin(pitch))); //Pitch is vertical tilt
		int x;
		int z;
		
		x = (int) (Math.round(size*Math.cos(pitch)*Math.sin(yaw)));
		z = (int) (Math.round(size*Math.cos(pitch)*Math.cos(yaw)));
		
		SimpleBlock two = base.getRelative(x,y,z);
		if(two.getY() > top.getY()) top = two;
		
		//Set height
		if(two.getY() - oriY > height)
			height = two.getY() - oriY;
		
		drawLine(rand, base,two,(int) (size),thickness,this.logType);
		
		if(!spawnedBees 
				&& Version.isAtLeast("1_15_R1") 
				&& GenUtils.chance(rand,(int) (beeChance*100.0),100)){
			for(int i = 0; i < 3; i ++){
				if(!two.getRelative(0,-i,0).getType().isSolid()){
					spawnedBees = true;
					BeeHiveSpawner.spawnFullBeeNest(two.getRelative(0,-i,0));
					//TerraformGeneratorPlugin.logger.debug("Bee nest spawned at " + two.getRelative(0,-i,0).getCoords());
					break;
				}
			}
			
		}
		
		fractalsDone++;
		
		if(fractalsDone % fractalThreshold != 0 
				&& thickness >= 1
				&& size >= 1){
			//Make 1 branch
			fractalBranch(rand, two, pitch - randomAngle(), 
					yaw + GenUtils.randInt(rand, 1,5)
						*GenUtils.getSign(rand)*rta(),
					depth,
					thickness,
					size);
			return;
		}
		
		//Make 4 branches
		fractalBranch(rand, two, pitch - randomAngle(), yaw - rta(),depth+1,thickness-thicknessDecrement,size-lengthDecrement);
		fractalBranch(rand, two, pitch + randomAngle(), yaw + rta(),depth+1,thickness-thicknessDecrement,size-lengthDecrement);
		fractalBranch(rand, two, pitch + randomAngle(), yaw + 5*rta(),depth+1,thickness-thicknessDecrement,size-lengthDecrement);
		fractalBranch(rand, two, pitch + randomAngle(), yaw - 5*rta(),depth+1,thickness-thicknessDecrement,size-lengthDecrement);
	}
	
	public void drawLine(Random rand, SimpleBlock one, SimpleBlock two, int segments, double thickness, Material type) {
		//Vector one to two;
		Vector v = two.getVector().subtract(one.getVector());
		
		for(int i=0; i<=segments; i++){
			Vector seg = v.clone().multiply((float) ((float)i)/((float)segments));
			SimpleBlock segment = one.getRelative(seg);
			replaceSphere(rand.nextInt(9999), ((float)thickness)/2, segment, logType);
		}
	}
	
	private void replaceSphere(int seed, float radius, SimpleBlock base, Material type){
		if(radius <= 0){
			return;
		}
		replaceSphere(seed,radius,radius,radius,base,type);
	}
	
	private void replaceSphere(int seed, float rX, float rY, float rZ, SimpleBlock block, Material type){
		if(rX <= 0 &&
				rY <= 0 &&
				rZ <= 0){
			return;
		}
		
		if(rX <= 0.5 &&
				rY <= 0.5 &&
				rZ <= 0.5){
			//block.setReplaceType(ReplaceType.ALL);
			block.setType(type);
			return;
		}
		
		FastNoise noise = new FastNoise(seed);
		noise.SetNoiseType(NoiseType.Simplex);
		noise.SetFrequency(0.09f);
		
		double maxR = rX;
		if(rX < rY) maxR = rY;
		if(rY < rZ) maxR = rZ;
		
		for(float x = -rX; x <= rX; x++){
			for(float y = -rY; y <= rY; y++){
				for(float z = -rZ; z <= rZ; z++){
					
					SimpleBlock rel = block.getRelative((int)Math.round(x),(int)Math.round(y),(int)Math.round(z));
					double equationResult = Math.pow(x,2)/Math.pow(rX,2)
							+ Math.pow(y,2)/Math.pow(rY,2)
							+ Math.pow(z,2)/Math.pow(rZ,2);
					if(equationResult <= 1+0.7*noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())){
						
						//Anti-dirt block glitch
						if(BlockUtils.isDirtLike(rel.getRelative(0,-1,0).getType())){
							rel.getRelative(0,-1,0).setType(Material.DIRT);
						}
						
						//Leaves do not replace solid blocks.
						if(type.toString().contains("LEAVES") && !rel.getType().isSolid()){
							Leaves leaf = (Leaves) Bukkit.createBlockData(type);
							
							//Temporary fix: Big canopies dont decay now.
//							if(leafRadiusX > 5 || leafRadiusY > 5 || leafRadiusZ > 5)
//								leaf.setPersistent(true);
//							
							leaf.setDistance(1);
							rel.setBlockData(leaf);
						}else if(!type.toString().contains("LEAVES")){
							rel.setType(type);
						}
						
						if(snowy){
							if(!rel.getRelative(0,1,0).getType().isSolid()){
								rel.getRelative(0,1,0).setType(Material.SNOW);
							}
						}
						if(cocabeans > 0 
								&& Math.abs(x) >= rX-2
								&& Math.abs(z) >= rZ-2){
							//Coca beans
							if(GenUtils.chance(cocabeans,100)){
								for(BlockFace face:new BlockFace[]{BlockFace.NORTH,BlockFace.SOUTH,BlockFace.EAST,BlockFace.WEST}){
									Directional dir = (Directional) Bukkit.createBlockData(Material.COCOA);
									dir.setFacing(face.getOppositeFace());
									((Ageable) dir).setAge(GenUtils.randInt(rand,0,((Ageable) dir).getMaximumAge()));
									SimpleBlock beans = rel.getRelative(face);
									if(beans.getType().isSolid() || 
											beans.getType() == Material.WATER) continue;
									
									beans.setBlockData(dir);
								}
							}
								
						}
						if(vines > 0 
								&& Math.abs(x) >= rX-2
								&& Math.abs(z) >= rZ-2){
							if(GenUtils.chance(2, 10)){
								dangleLeavesDown(rel,(int) Math.ceil(maxR),vines/2, vines);
							}
							
							//Vines set only if the leaftype is leaves.
							if(leafType.toString().contains("LEAVES"))
								if(GenUtils.chance(1,10)){
									for(BlockFace face:new BlockFace[]{BlockFace.NORTH,BlockFace.SOUTH,BlockFace.EAST,BlockFace.WEST}){
										MultipleFacing dir = (MultipleFacing) Bukkit.createBlockData(Material.VINE);
										dir.setFace(face.getOppositeFace(),true);
										SimpleBlock vine = rel.getRelative(face);
										if(vine.getType().isSolid() || 
												vine.getType() == Material.WATER) continue;
										
										vine.setBlockData(dir);
										for(int i = 0; i < GenUtils.randInt(1,vines); i++){
											if(vine.getRelative(0,-i,0).getType().isSolid() || 
													vine.getRelative(0,-i,0).getType() == Material.WATER) break;
											vine.getRelative(0,-i,0).setBlockData(dir);
										}
									}
								}
								
						}
						//rel.setReplaceType(ReplaceType.ALL);
					}
				}
			}
		}
	}
	
	private void dangleLeavesDown(SimpleBlock block, int leafDist, int min, int max){
		BlockData type = Bukkit.createBlockData(leafType);
		if(leafType.toString().contains("LEAVES")){
			Leaves leaf = (Leaves) type;
			leaf.setDistance(1);
		}
		for(int i = 1; i <= GenUtils.randInt(min, max); i++){
			if(!block.getRelative(0,0-i,0).getType().isSolid()){
//				if(leafRadiusX > 5 || leafRadiusY > 5 || leafRadiusZ > 5)
//					leaf.setPersistent(true);
//				else
				
				block.getRelative(0,0-i,0).lsetBlockData(type);
			}else
				break;
		}
		
		//Log for good measure, as well as some surrounding leaves.
		if(leafType.toString().contains("LEAVES"))
			block.setType(this.logType);
		for(BlockFace face:BlockUtils.directBlockFaces){
			block.getRelative(face).lsetBlockData(type);
		}
		block.getRelative(0,1,0).lsetBlockData(type);
	}

	public FractalTreeBuilder setSnowy(boolean snowy){
		this.snowy = snowy;
		return this;
	}

	public FractalTreeBuilder setVines(int vines){
		this.vines = vines;
		return this;
	}
	public FractalTreeBuilder setHeightVar(int var){
		this.heightVar = var;
		return this;
	}
	
	public FractalTreeBuilder setMinBend(double bend){
		this.minBend = bend;
		return this;
	}
	public FractalTreeBuilder setMaxBend(double bend){
		this.maxBend = bend;
		return this;
	}
	
	public FractalTreeBuilder setInitialTilt(double initialTilt){
		this.initialTilt = initialTilt;
		return this;
	}
	
	public FractalTreeBuilder setLeafType(Material leaf){
		this.leafType = leaf;
		return this;
	}

	public FractalTreeBuilder setLogType(Material log){
		this.logType = log;
		return this;
	}
	
	public FractalTreeBuilder setLengthDecrement(float d){
		this.lengthDecrement = d;
		return this;
	}

	public FractalTreeBuilder setLeafRadius(int r){
		this.leafRadiusX = r;
		this.leafRadiusY = r;
		this.leafRadiusZ = r;
		return this;
	}
	
	public FractalTreeBuilder setLeafRadiusX(float f){
		this.leafRadiusX = f;
		return this;
	}

	public FractalTreeBuilder setLeafRadiusY(float r){
		this.leafRadiusY = r;
		return this;
	}

	public FractalTreeBuilder setLeafRadiusZ(float r){
		this.leafRadiusZ = r;
		return this;
	}

	public FractalTreeBuilder setMaxDepth(int d){
		this.maxDepth = d;
		return this;
	}
	
	public FractalTreeBuilder setAlwaysOneStraight(boolean bool){
		this.alwaysOneStraight = bool;
		return this;
	}

	/**
	 * @param beeChance the beeChance to set
	 */
	public FractalTreeBuilder setBeeChance(double beeChance) {
		this.beeChance = beeChance;
		return this;
	}

	/**
	 * @return the cocabeans
	 */
	public int getCocabeans() {
		return cocabeans;
	}

	/**
	 * @param cocabeans the cocabeans to set
	 */
	public FractalTreeBuilder setCocabeans(int cocabeans) {
		this.cocabeans = cocabeans;
		return this;
	}

	public FractalTreeBuilder setThicknessDecrement(float d){
		this.thicknessDecrement = d;
		return this;
	}
	
	public FractalTreeBuilder setBaseThickness(float baseThickness){
		this.baseThickness = baseThickness;
		return this;
	}
	
	public FractalTreeBuilder setBaseHeight(int h){
		this.baseHeight = h;
		return this;
	}
	
	public FractalTreeBuilder setFractalThreshold(int i){
		this.fractalThreshold = i;
		return this;
	}
	
	public FractalTreeBuilder setMaxPitch(double max){
		this.maxPitch = max;
		return this;
	}
	
	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	public FractalTreeBuilder setMinPitch(double min){
		this.minPitch = min;
		return this;
	}
	
	/**
	 * Random angle defined by the min and max bend angles
	 */
	public double randomAngle(){
		return GenUtils.randDouble(rand, minBend, maxBend);
	}
	
	/**
	 * Random-thirty-ish-angle
	 * @param rand
	 * @return An angle between 0.8*30 to 1.2*30 degrees in radians
	 */
	public double rta(){
		return GenUtils.randDouble(rand, 0.8*Math.PI/6, 1.2*Math.PI/6);
	}
	

	

}
