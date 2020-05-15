package org.terraform.biome.ocean;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class FrozenOceansHandler extends BiomeHandler {

	@Override
	public boolean isOcean() {
		return true;
	}

	@Override
	public Biome getBiome() {
		return Biome.FROZEN_OCEAN;
	}

	@Override
	public Material[] getSurfaceCrust(Random rand) {
		return new Material[]{GenUtils.randMaterial(rand, Material.DIRT,Material.STONE,Material.COBBLESTONE,Material.STONE,Material.GRAVEL,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE,Material.STONE,Material.STONE,Material.GRAVEL,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE,Material.GRAVEL,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE),
				GenUtils.randMaterial(rand, Material.DIRT,Material.STONE)};
	}

	@Override
	public void populate(TerraformWorld world, Random random, PopulatorDataAbstract data) {

		for(int x = data.getChunkX()*16; x < data.getChunkX()*16+16; x++){
			for(int z = data.getChunkZ()*16; z < data.getChunkZ()*16+16; z++){
				int y = GenUtils.getTrueHighestBlock(data, x, z);
				if(data.getBiome(x,y+1,z) != getBiome()) continue;
				
				//Full ice-sheets
				data.setType(x,TerraformGenerator.seaLevel, z, Material.ICE);
				
				if(!data.getType(x, y, z).isSolid()) continue;
				if(GenUtils.chance(random, 2, 100)){
					BlockUtils.generateClayDeposit(x,y,z,data,random);
				}
			}
		}
	}
	

}
