package org.terraform.structure.mineshaft;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.data.SimpleBlock;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.BlockUtils;

public class OreLiftSchematicParser extends SchematicParser{
	
	
	public OreLiftSchematicParser() {
		super();
	}

	@Override
	public void applyData(SimpleBlock block, BlockData data){
		if(data.getMaterial().toString().endsWith("ORE")) {
			data = Bukkit.createBlockData(BlockUtils.ores.get(new Random().nextInt(BlockUtils.ores.size())));
		}
		//super.applyData(block, data);
		//Override the normal way as it updates physics. 
		//Don't update physics because rails are cancer.
		
		PopulatorDataAbstract pop = block.getPopData();
		if(pop instanceof PopulatorDataPostGen) {
			PopulatorDataPostGen gen = (PopulatorDataPostGen) pop;
			gen.noPhysicsUpdateForce(block.getX(), block.getY(), block.getZ(), data);
		}else {
			//L, broken rails.
			TerraformGeneratorPlugin.logger.error("Ore-lift generation attempted without PopulatorDataPostGen");
			super.applyData(block, data);
		}
	}
	
}