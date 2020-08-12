package org.terraform.coregen.v1_14_R1;

import java.lang.reflect.Method;
import java.util.Random;
import java.util.Set;

import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_14_R1.generator.CraftChunkData;
import org.bukkit.generator.ChunkGenerator.BiomeGrid;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.coregen.TerraformPopulator;
import org.terraform.data.TerraformWorld;
import org.terraform.structure.farmhouse.FarmhousePopulator;
import org.terraform.structure.monument.MonumentPopulator;
import org.terraform.structure.stronghold.StrongholdPopulator;

import net.minecraft.server.v1_14_R1.BiomeBase;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ChunkGenerator;
import net.minecraft.server.v1_14_R1.ChunkSection;
import net.minecraft.server.v1_14_R1.GeneratorAccess;
import net.minecraft.server.v1_14_R1.GeneratorSettingsDefault;
import net.minecraft.server.v1_14_R1.HeightMap.Type;
import net.minecraft.server.v1_14_R1.IChunkAccess;
import net.minecraft.server.v1_14_R1.ITileEntity;
import net.minecraft.server.v1_14_R1.RegionLimitedWorldAccess;
import net.minecraft.server.v1_14_R1.TileEntity;
import net.minecraft.server.v1_14_R1.World;
import net.minecraft.server.v1_14_R1.WorldChunkManager;

public class NMSChunkGenerator extends ChunkGenerator {
	
	public NMSChunkGenerator(GeneratorAccess generatoraccess,
			WorldChunkManager worldchunkmanager, GeneratorSettingsDefault c0) {
		super(generatoraccess, worldchunkmanager, c0);
        tw = TerraformWorld.get(generatoraccess.getWorldData().getName(), generatoraccess.getWorldData().getSeed());
        pop = new TerraformPopulator(tw);
	}

	private TerraformPopulator pop;
	private TerraformWorld tw;
	
	@Override 
    public void createBiomes(IChunkAccess ichunkaccess) {
    	//Bukkit.getLogger().info("createBiomes");

		final BiomeBase[] biomeBases = new BiomeBase[16 * 16];
        try {

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                	int rawX = x+ichunkaccess.getPos().x*16;
                	int rawZ = x+ichunkaccess.getPos().z*16;
                	int y = HeightMap.getHeight(tw, rawX, rawZ);
                	BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(tw.getBiomeBank(rawX, y, rawZ).getHandler().getBiome()); //BiomeBank.calculateBiome(tw,tw.getTemperature(x,z), y).getHandler().getBiome()

                    biomeBases[(x * 16) + z] = biomeBase;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        ichunkaccess.a(biomeBases);
    	//Bukkit.getLogger().info("createBiomes-finish");
		
	}

	@Override 
    protected BiomeBase getDecoratingBiome(RegionLimitedWorldAccess rlwa, BlockPosition bp) {
    	return CraftBlock.biomeToBiomeBase(tw.getBiomeBank(bp.getX(), bp.getY(), bp.getZ()).getHandler().getBiome());
    }
	
    @Override
    public void addDecorations(RegionLimitedWorldAccess rlwa) {
    	//Bukkit.getLogger().info("addDecorations");
		int chunkX = rlwa.a();
        int chunkZ = rlwa.b();
        PopulatorData popDat = new PopulatorData(rlwa,this,chunkX,chunkZ);
        pop.populate(tw, rlwa.getRandom(), popDat);
    	//Bukkit.getLogger().info("addDecorations-finish");
    }
    
    @Override
    public int getSeaLevel(){
    	return TerraformGenerator.seaLevel;
    }

	@Override
	public int getSpawnHeight() {
		return 50;
	}


	@Override
	public void buildNoise(GeneratorAccess generatoraccess,
			IChunkAccess ichunkaccess) {}
	
    @Override
    public void buildBase(IChunkAccess ichunkaccess) {
    	//Bukkit.getLogger().info("buildBase");
    	try{
	        int x = ichunkaccess.getPos().x;
	        int z = ichunkaccess.getPos().z;
	        TerraformGenerator generator = new TerraformGenerator();
	        Random random = tw.getRand(3);
	        random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
	
	        // Get default biome data for chunk
	        CustomBiomeGrid biomegrid = new CustomBiomeGrid();
	        biomegrid.biome = this.getWorldChunkManager().getBiomeBlock(x << 4, z << 4, 16, 16);
	
	        ChunkData data;
	        if (generator.isParallelCapable()) {
	            data = generator.generateChunkData(this.tw.getWorld(), random, x, z, biomegrid);
	        } else {
	            synchronized (this) {
	                data = generator.generateChunkData(this.tw.getWorld(), random, x, z, biomegrid);
	            }
	        }
	
	        CraftChunkData craftData = (CraftChunkData) data;
	        Method getRawChunkData = CraftChunkData.class.getDeclaredMethod("getRawChunkData");
	        getRawChunkData.setAccessible(true);
	        ChunkSection[] sections = (ChunkSection[]) getRawChunkData.invoke(craftData);
	
	
	        ChunkSection[] csect = ichunkaccess.getSections();
	        int scnt = Math.min(csect.length, sections.length);
	
	        // Loop through returned sections
	        for (int sec = 0; sec < scnt; sec++) {
	            if (sections[sec] == null) {
	                continue;
	            }
	            ChunkSection section = sections[sec];
	
	            csect[sec] = section;
	        }
	
	        // Set biome grid
	        ichunkaccess.a(biomegrid.biome);
	
	        Method getTiles;
			getTiles = CraftChunkData.class.getDeclaredMethod("getTiles");
	        getTiles.setAccessible(true);
	        Set<BlockPosition> tiles = (Set<BlockPosition>) getTiles.invoke(craftData);
	        
	        if (tiles != null) {
	            for (BlockPosition pos : tiles) {
	                int tx = pos.getX();
	                int ty = pos.getY();
	                int tz = pos.getZ();
	                net.minecraft.server.v1_14_R1.Block block = craftData.getTypeId(tx, ty, tz).getBlock();
	
	                if (block.isTileEntity()) {
	                    TileEntity tile = ((ITileEntity) block).createTile(((CraftWorld) tw.getWorld()).getHandle());
	                    ichunkaccess.setTileEntity(new BlockPosition((x << 4) + tx, ty, (z << 4) + tz), tile);
	                }
	            }
	        }
    	}catch(Throwable e){
    		e.printStackTrace();
    	}
    	//Bukkit.getLogger().info("buildBase-finish");
    }

	@Override
	public int getBaseHeight(int i, int j, Type heightmap_type) {
		return org.terraform.coregen.HeightMap.getHeight(tw,i,j);
	}
	
	@Override
    public BlockPosition findNearestMapFeature(World world, String s, BlockPosition blockposition, int i, boolean flag) {
        //StructureGenerator<?> structuregenerator = (StructureGenerator) WorldGenerator.ao.get(s.toLowerCase(Locale.ROOT));
		int pX = blockposition.getX();
		int pZ = blockposition.getZ();
		if(s.equalsIgnoreCase("Stronghold")){
			int[] coords = new StrongholdPopulator().getNearestFeature(tw, pX, pZ);
			return new BlockPosition(coords[0],20,coords[1]);
		}else if(s.equalsIgnoreCase("Village")){
			int[] coords = new FarmhousePopulator().getNearestFeature(tw, pX, pZ);
			return new BlockPosition(coords[0],100,coords[1]);
		}else if(s.equalsIgnoreCase("Monument")){
			int[] coords = new MonumentPopulator().getNearestFeature(tw, pX, pZ);
			return new BlockPosition(coords[0],100,coords[1]);
		}
		
        return null;
    }
	
    private static class CustomBiomeGrid implements BiomeGrid {
        BiomeBase[] biome;

        @Override
        public Biome getBiome(int x, int z) {
            return CraftBlock.biomeBaseToBiome(biome[(z << 4) | x]);
        }

        @Override
        public void setBiome(int x, int z, Biome bio) {
            biome[(z << 4) | x] = CraftBlock.biomeToBiomeBase(bio);
        }

        public Biome getBiome(int x, int y, int z) {
            return getBiome(x,z);
        }

        public void setBiome(int x, int y, int z, Biome bio) {
            setBiome(x,z,bio);
        }
    }
}
