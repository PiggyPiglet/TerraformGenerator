package org.terraform.structure.animalfarm;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.VillageHousePopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

public class AnimalFarmPopulator extends VillageHousePopulator{



	@Override
	public void populate(TerraformWorld tw, Random random,
			PopulatorDataAbstract data) {
		int seaLevel = TerraformGenerator.seaLevel;
		MegaChunk mc = new MegaChunk(data.getChunkX(),data.getChunkZ());
		int[] coords = getCoordsFromMegaChunk(tw,mc);
		int x = coords[0];//data.getChunkX()*16 + random.nextInt(16);
		int z = coords[1];//data.getChunkZ()*16 + random.nextInt(16);
		int height = GenUtils.getHighestGround(data, x, z);
		spawnAnimalFarm(tw,tw.getHashedRand(x, height, z, 425332),data,x,height+1,z);
	}
	
	public void spawnAnimalFarm(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z){
		try {
			BiomeBank biome = tw.getBiomeBank(x, y, z);
			BlockFace dir = BlockUtils.getDirectBlockFace(random);
			TerraSchematic animalFarm = TerraSchematic.load("animalfarm", new Location(tw.getWorld(),x,y,z));
			animalFarm.parser = new AnimalFarmSchematicParser(biome,random,data);
			animalFarm.setFace(dir);
			animalFarm.apply();

			TerraformGeneratorPlugin.logger.info("Spawning animal farm at " + x + "," + y + "," + z + " with rotation of " + animalFarm.getFace().toString());
			
			data.addEntity(x, y+1, z, EntityType.VILLAGER); //Two villagers
			data.addEntity(x, y+1, z, EntityType.VILLAGER);
			data.addEntity(x, y+1, z, EntityType.CAT); //And a cat.
			
			//Spawn a base on the house to sit on
			if(dir == BlockFace.NORTH || dir == BlockFace.SOUTH)
				for(int nx = -4; nx <= 4; nx++){
					for(int nz = -9; nz <= 9; nz++){
						if(data.getType(x+nx,y-1,z+nz).isSolid())
							BlockUtils.setDownUntilSolid(x+nx, y-2, z+nz, data, Material.SPRUCE_LOG);
					}
				}
			else
				for(int nx = -9; nx <= 9; nx++){
					for(int nz = -4; nz <= 4; nz++){
						if(data.getType(x+nx,y-1,z+nz).isSolid())
						BlockUtils.setDownUntilSolid(x+nx, y-2, z+nz, data, Material.SPRUCE_LOG);
					}
				}
			
			
			
//			//Spawn a stairway from the house.
//			Wall w = new Wall(new SimpleBlock(data,x,y-1,z),winterCabin.getFace()).getRight();
//			for(int i = 0; i < 7; i++) 
//				w = w.getFront();
//			//while(w.getType() != Material.DIRT){
//			while(!w.getType().isSolid() || 
//					w.getType().toString().contains("PLANKS")){
//				Stairs stairs = (Stairs) Bukkit.createBlockData(GenUtils.randMaterial(random, Material.COBBLESTONE_STAIRS,Material.COBBLESTONE_STAIRS,Material.COBBLESTONE_STAIRS,Material.MOSSY_COBBLESTONE_STAIRS));
//				stairs.setFacing(w.getDirection().getOppositeFace());
//				w.getRight().setBlockData(stairs);
//				w.setBlockData(stairs);
//				w.getLeft().setBlockData(stairs);
//				w.getLeft().getLeft().getRelative(0,1,0).downUntilSolid(random,BlockUtils.getWoodForBiome(biome, "LOG"));
//				w.getLeft().getLeft().getRelative(0,2,0).setType(GenUtils.randMaterial(random, Material.COBBLESTONE_WALL,Material.COBBLESTONE_WALL,Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL));
//				w.getRight().getRight().getRelative(0,1,0).downUntilSolid(random,BlockUtils.getWoodForBiome(biome, "LOG"));
//				w.getRight().getRight().getRelative(0,2,0).setType(GenUtils.randMaterial(random, Material.COBBLESTONE_WALL,Material.COBBLESTONE_WALL,Material.COBBLESTONE_WALL,Material.MOSSY_COBBLESTONE_WALL));
//				w = w.getFront().getRelative(0,-1,0);
//			}
			
			createSurroundingFences(tw,biome, random,data,x,y,z);
			
		} catch (Throwable e) {
			TerraformGeneratorPlugin.logger.error("Something went wrong trying to place farmhouse at " + x + "," + y + "," + z + "!");
			e.printStackTrace();
		}
	}
	
	private static final EntityType[] farmAnimals = new EntityType[]{
		EntityType.PIG,
		EntityType.SHEEP,
		EntityType.COW,
		EntityType.HORSE,
		EntityType.CHICKEN
	};
	
	private void createSurroundingFences(TerraformWorld tw, BiomeBank biome, Random random, PopulatorDataAbstract data, int x, int y, int z){
		RoomLayoutGenerator gen = new RoomLayoutGenerator(random,RoomLayout.RANDOM_BRUTEFORCE,50,x,y,z,75);
		gen.setPathPopulator(new AnimalFarmPathPopulator(gen,tw.getHashedRand(x, y, z, 1234)));
		gen.setRoomMaxX(17);
		gen.setRoomMaxZ(17);
		gen.setRoomMaxHeight(1);
		gen.getRooms().add(new CubeRoom(20, 20, 30, x, y, z));
		
		gen.generate();
		
		//gen.fillPathsOnly(data, tw, Material.BARRIER);
		
		
		
		FastNoise fieldNoise = new FastNoise(tw.getHashedRand(x, y, z,23).nextInt(225));
		fieldNoise.SetNoiseType(NoiseType.Simplex);
		fieldNoise.SetFrequency(0.05f);
		
		FastNoise radiusNoise = new FastNoise(tw.getHashedRand(x, y, z,23).nextInt(225));
		radiusNoise.SetNoiseType(NoiseType.Cubic);
		radiusNoise.SetFrequency(0.09f);
		
		//Make paths
		for(int nx = -50; nx <= 50; nx++){
			for(int nz = -50; nz <= 50; nz++){
				int height = GenUtils.getHighestGround(data, x+nx, z+nz);
				if(!BlockUtils.isDirtLike(data.getType(x+nx, height, z+nz)) ||
						data.getType(x+nx,height+1,z+nz) != Material.AIR)
					continue;
				
				double noise = fieldNoise.GetNoise(nx+x, nz+z);
				
				double dist = Math.pow(nx,2) + Math.pow(nz, 2);
				double multiplier = Math.pow((1/(dist-2500))+1,255);
				if(multiplier < 0 || dist > 2500+(radiusNoise.GetNoise(nx, nz)*500.0)) multiplier = 0;
				noise = noise*multiplier;
				
				if(GenUtils.chance(random,(2500-dist)>0?(int)(2500 - dist):0,2500))
					if(noise < -0.2){
						
					}else if(noise > 0.2){
						
					}else  if(Math.abs(noise) < 0.2 && Math.abs(noise) > 0.1){ //Grass hedges
						data.setType(nx+x, height, nz+z, GenUtils.randMaterial(
								random,
								Material.CHISELED_STONE_BRICKS,
								Material.STONE_BRICKS,
								Material.STONE_BRICKS,
								Material.STONE_BRICKS));
					}else{
						if(GenUtils.chance(random,(int) (100*Math.pow(multiplier,3)),100)){
							data.setType(nx+x, height, nz+z, GenUtils.randMaterial(
									random,
									Material.COBBLESTONE_SLAB,
									Material.MOSSY_COBBLESTONE_SLAB));
						}
					}				
			}
		}
		
		//Create each pen
		for(CubeRoom room:gen.getRooms()){
			//Don't touch the center room
			if(room.getWidthX() == 20 && room.getWidthZ() == 20) continue;
			
			//Create fences
			for(int t = 0; t <= 360; t+=5){
				int ePX = room.getX() + (int) ((room.getWidthX()/2)  * Math.cos(Math.toRadians(t)));
				int ePZ = room.getZ() + (int) ((room.getWidthZ()/2) * Math.sin(Math.toRadians(t)));
				int highest = GenUtils.getHighestGround(data, ePX, ePZ);
				
				data.setType(ePX, highest+1, ePZ, Material.SPRUCE_FENCE);
				
				//General lighting
				if(GenUtils.chance(random,1,30)){
					data.setType(ePX, highest+1, ePZ, Material.CHISELED_STONE_BRICKS);
					data.setType(ePX, highest+2, ePZ, GenUtils.randMaterial( Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL));
					data.setType(ePX, highest+3, ePZ, GenUtils.randMaterial( Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL));
					data.setType(ePX, highest+4, ePZ, GenUtils.randMaterial( Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL));
					data.setType(ePX, highest+5, ePZ, Material.CAMPFIRE);
				}
				BlockUtils.correctSurroundingMultifacingData(new SimpleBlock(data,ePX,highest+1,ePZ));
			}
			
			HashMap<Wall,Integer> walls = room.getFourWalls(data, 0);
			
			//Spawn job blocks
			for(Entry<Wall,Integer> entry:walls.entrySet()){
				Wall w = entry.getKey();
				int length = entry.getValue();
				for(int i = 0; i < length; i++){
					int highest = GenUtils.getHighestGround(data, w.get().getX(), w.get().getZ());
					w = w.getLeft();
					if(GenUtils.chance(random, 1, 50)){
						SimpleBlock rear = w.getRear().get();
						highest = GenUtils.getHighestGround(data, rear.getX(),rear.getZ());
						data.setType(rear.getX(),highest+1,rear.getZ(),GenUtils.randMaterial(
								Material.CAULDRON,
								Material.SMOKER,
								Material.LOOM
						));
					}
				}
				
			}
			
			EntityType animal = farmAnimals[random.nextInt(farmAnimals.length)];
			
			//Spawn animals
			for(int i = 0; i < GenUtils.randInt(3, 7); i++){
				int[] coords = room.randomCoords(random,2);
				int highest = GenUtils.getHighestGround(data, coords[0],coords[2]);
				data.addEntity(coords[0], highest+1, coords[2], animal);
			}
			
			//Decorate it
			for(int nx = room.getLowerCorner()[0]+2; nx <= room.getUpperCorner()[0]-2; nx++){
				for(int nz = room.getLowerCorner()[1]+2; nz <= room.getUpperCorner()[1]-2; nz++){
					int highest = GenUtils.getHighestGround(data, nx, nz);
					if(data.getType(nx,highest,nz) == Material.CHISELED_STONE_BRICKS) 
						highest--;
					
					if(Math.pow((nx-room.getX())/(room.getWidthX()/2), 2)
							+ Math.pow((nz-room.getZ())/(room.getWidthZ()/2), 2) <= 1){
						
						data.setType(nx, highest, nz, GenUtils.randMaterial(random,
								Material.GRASS_BLOCK,
								Material.PODZOL,
								Material.GRASS_BLOCK,
								Material.GRASS_BLOCK,
								Material.GRASS_BLOCK,
								Material.GRASS_PATH,
								Material.GRASS_BLOCK,
								Material.COARSE_DIRT));
					}
					
					if(GenUtils.chance(random, 1, 60)){
						BlockUtils.replaceUpperSphere(nx+7*nz+17*17, 1.1f, 2f, 1.1f, 
								new SimpleBlock(data,nx,highest+1,nz), 
								false, Material.HAY_BLOCK);
					}
				}
				
			}
			
		}
		
	}
}
