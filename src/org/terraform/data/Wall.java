package org.terraform.data;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class Wall {
	
	private SimpleBlock block;
	private BlockFace direction;
	
	public Wall(SimpleBlock block, BlockFace dir){
		this.block = block;
		this.direction = dir;
	}
	
	public Wall clone(){
		return new Wall(block,direction);
	}
	
	public Wall getLeft(){
		return new Wall(block.getRelative(BlockUtils.getAdjacentFaces(direction)[0]), direction);
	}
	

	public Wall getLeft(int it){
		Wall w = this.clone();
		for(int i = 0; i < it; i++) w = w.getLeft();
		return w;
	}
	
	public Wall getRight(){
		return new Wall(block.getRelative(BlockUtils.getAdjacentFaces(direction)[1]), direction);
	}
	
	public Wall getRight(int it){
		Wall w = this.clone();
		for(int i = 0; i < it; i++) w = w.getRight();
		return w;
	}
	
	public Wall getHighestSolidBlockFromAbove(){
		int highest = GenUtils.getHighestGround(block.getPopData(), block.getX(), block.getZ());
		int x = block.getX();
		int z = block.getZ();
		
		return new Wall(new SimpleBlock(block.getPopData(), x, highest, z),direction);
	}
	
	public SimpleBlock get(){
		return block;
	}
	
	public void setBlockData(BlockData d){
		this.block.setBlockData(d);
	}
	
	public Material getType(){
		return block.getType();
	}
	
	public void setType(Material type){
		block.setType(type);
	}

	/**
	 * Replaces everything in its way
	 * @param height
	 * @param rand
	 * @param types
	 */
	public void Pillar(int height, Random rand, Material... types){
		for(int i = 0; i < height; i++){
			block.getRelative(0,i,0).setType(GenUtils.randMaterial(rand, types));
		}
	}
	
	/**
	 * Replaces until a solid block is reached.
	 * @param height
	 * @param rand
	 * @param types
	 */
	public void LPillar(int height, Random rand, Material... types){
		LPillar(height,false,rand,types);
	}
	
	/**
	 * Replaces until a solid block is reached.
	 * @param height
	 * @param rand
	 * @param types
	 */
	public void LPillar(int height, boolean pattern, Random rand, Material... types){
		for(int i = 0; i < height; i++){
			if(block.getRelative(0,i,0).getType().isSolid()) break;
			if(!pattern)
				block.getRelative(0,i,0).setType(GenUtils.randMaterial(rand, types));
			else
				block.getRelative(0,i,0).setType(types[i%types.length]);
		}
	}
	
	/**
	 * Replaces non-solid blocks only
	 * @param height
	 * @param rand
	 * @param types
	 */
	public void RPillar(int height, Random rand, Material... types){
		for(int i = 0; i < height; i++){
			if(!block.getRelative(0,i,0).getType().isSolid())
				block.getRelative(0,i,0).setType(GenUtils.randMaterial(rand, types));
		}
	}
	
	/**
	 * Replaces non-cave air only
	 * @param height
	 * @param rand
	 * @param types
	 */
	public void CAPillar(int height, Random rand, Material... types){
		for(int i = 0; i < height; i++){
			if(block.getRelative(0,i,0).getType() != Material.CAVE_AIR)
				block.getRelative(0,i,0).setType(GenUtils.randMaterial(rand, types));
		}
	}
	
	public void downUntilSolid(Random rand, Material... types){
		int depth = 0;
		for(int y = get().getY(); y > 0; y--){
			if(!block.getRelative(0,-depth,0).getType().isSolid()){
				block.getRelative(0,-depth,0).setType(GenUtils.randMaterial(rand, types));
			}else break;
			depth++;
		}
	}
	
	public void downLPillar(Random rand, int h, Material... types){
		int depth = 0;
		for(int y = get().getY(); y > 0; y--){
			if(depth >= h) break;
			if(!block.getRelative(0,-depth,0).getType().isSolid()){
				block.getRelative(0,-depth,0).setType(GenUtils.randMaterial(rand, types));
			}else break;
			depth++;
		}
	}
	
	public Wall getRear(){
		return new Wall(block.getRelative(direction.getOppositeFace()), direction);
	}
	
	public Wall getRear(int it){
		Wall w = this.clone();
		for(int i = 0; i < it; i++) w = w.getRear();
		return w;
	}
	
	public Wall getFront(){
		return new Wall(block.getRelative(direction), direction);
	}
	
	public Wall getFront(int it){
		Wall w = this.clone();
		for(int i = 0; i < it; i++) w = w.getFront();
		return w;
	}

	public BlockFace getDirection() {
		return direction;
	}
	
	public Wall getRelative(int x, int y, int z){
		return new Wall(block.getRelative(x,y,z), direction);
	}

	public Wall getRelative(BlockFace face) {
		// TODO Auto-generated method stub
		return new Wall(block.getRelative(face),direction);
	}

}
