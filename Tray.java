import java.awt.*;
import java.util.*;

public class Tray {
	
	public boolean debug = false;

	private int trayWidth;
	private int trayHeight;


	private ArrayList<Block> goalBlocks = new ArrayList<Block>();
	private ArrayList<Block> blocks = new ArrayList<Block>();
	private Block[] occupied; //ROW-MAJOR
	
	public Tray() {
	}
	
	public Tray(int numRows, int numCols) {
		
		trayHeight = numRows;
		trayWidth = numCols;

		occupied = new Block[(trayWidth) * (trayHeight)];
	}

	public void addBlock (Block b) {
		if (b != null){


			//POPULATE MATRIX OF OCCUPIED SPACES
			for (int i = b.trow(); i <= b.brow(); i++) {
				for (int j = b.lcol(); j <= b.rcol(); j++) {
					//System.out.println("i: " + i);
					//System.out.println("j: " + j);
					//System.out.println("index: " + (j + i*trayWidth));
					//System.out.println("occupied.length: " + occupied.length);
					if (occupied[j + i*trayWidth] != null) {
						throw new IllegalStateException("Cannot add block to occupied space.");
					}
					occupied[j + i*trayWidth] = b;
				}
			}

			blocks.add(b);
			

			//FOR DEBUGGING
			if (debug) {
				printOccupied();
			}
		}
	}

	public Block[] getOccupied ( ) {
		return occupied;
	}
	
	public void addGoalBlock (Block b) {
		if (b != null) {
			goalBlocks.add(b);
		}
	}

	public void printOccupied() {

		for (int i = 0; i < trayHeight; i++) {
			for (int j = 0; j < trayWidth; j++) {
				if (occupied[j + i*trayWidth] != null) {
					System.out.print("1 ");
				}
				else {
					System.out.print("0 ");
				}
			}
			System.out.println("");
		}
		System.out.println("");

	}
	
	public void moveBlock (Block b, int row, int col) {
		if (validMove(b, row, col)) {

			for (int i = b.trow(); i <= b.brow(); i++) {
					for (int j = b.lcol(); j <= b.rcol(); j++) {
						occupied[j + i*trayWidth] = null;
						if (debug) {
						}
					}
			}

			for (int i = row; i <= row + b.height(); i++) {
				for (int j = col; j <= col + b.width(); j++) {
					occupied[j + i*trayWidth] = b;
						if (debug) {
						}
				}
			}

			b.move(row, col);

			if (debug) {
				printOccupied();
			}
		}
	}
	
	public boolean containsBlock (int topRow, int leftCol, int bottomRow, int rightCol) {
		for (Block block: blocks) {
			if (block.trow() == topRow && block.lcol() == leftCol && block.brow() == bottomRow && block.rcol() == rightCol) {
				return true;
			}
		}
		return false;
	}

	public Tray copy () {
		Tray copyTray = new Tray(height(), width());
		copyTray.goalBlocks = new ArrayList<Block> (goalBlocks);
		for (Block block : blocks) {
			copyTray.addBlock(new Block(block));
		}
		return copyTray;
	}
	
	public boolean containsGoalBlock (int topRow, int leftCol, int bottomRow, int rightCol) {
		for (Block block: goalBlocks) {
			if (block.trow() == topRow && block.lcol() == leftCol && block.brow() == bottomRow && block.rcol() == rightCol) {
				return true;
			}
		}
		return false;
	}
	
	public boolean validMove (Block b, int trowDest, int lcolDest) {

		if (!blocks.contains(b)){
			return false;   // can only move blocks on the board
		}
		if ((trowDest < 0) || (trowDest > trayHeight) || (lcolDest < 0) || (lcolDest > trayWidth)) {
			return false;   // cannot move off the board
		}
		
		if ((b.trow() != trowDest) && (b.lcol() != lcolDest)) {
			return false; //cannot move diagonally
		}
		
		int browDest = trowDest + b.height();
		int rcolDest = lcolDest + b.width();

		if ((Math.abs(b.trow() - trowDest) > 1) || (Math.abs(b.lcol() - lcolDest) > 1) || (Math.abs(b.brow() - browDest) > 1) || (Math.abs(b.rcol() - rcolDest) > 1)) {
			return false; // can only move one space at a time
		}

		for (Block block : blocks) {
			if (!b.equals(block) && block.overlapping(trowDest, lcolDest, browDest, rcolDest)) {
				return false;
			}
		}
		return true;
	}
	
	public int width() {
		return trayWidth;
	}
	
	public int height() {
		return trayHeight;
	}
	
	public void isOK() {
		if (debug) {
			//Integer[][] visited = new Integer[blocks.size()][4];
			//int index = 0;
			boolean moveExists = false;
			if (blocks.size() < goalBlocks.size()) {
				throw new IllegalStateException("More goal blocks than blocks on the board");
			}
			for (Block block : blocks) {
				if (block.trow() < 0 || block.brow() > trayHeight || block.lcol() < 0 || block.rcol() > trayWidth) {
					throw new IllegalStateException("Block not in board");
				}
				if (!Arrays.asList(occupied).contains(block)) {
					throw new IllegalStateException("Block incorrectly added");
				}
				//Integer[] coordinates = {Integer.valueOf(block.trow()), Integer.valueOf(block.lcol()), Integer.valueOf(block.brow()), Integer.valueOf(block.rcol())};
				//visited[index] = coordinates;
				for (Block otherBlock : blocks) {
					//System.out.println("block: " + block);
					//System.out.println("otherBlock: " + otherBlock);
					if (!(otherBlock == block) && block.overlapping(otherBlock.trow(), otherBlock.lcol(), otherBlock.brow(), otherBlock.rcol())) {
						throw new IllegalStateException("Two blocks are in the same location");
					}
				}
				for (int i = block.trow() - 1; i < block.brow() + 1; i++) {
					for (int j = block.lcol() - 1; j < block.rcol() + 1; j++) {
						if (validMove(block, i, j)) {
							moveExists = true;
						}
					}
				}
			}
			for (Block block : occupied) {
				//System.out.println(blocks);
				if (block != null) {
					if (!blocks.contains(block)) {
						throw new IllegalStateException("Block incorrectly added");
					}
				}
			}
			if (!moveExists) {
				throw new IllegalStateException("No valid moves");
			}
		}
	}
	
	public ArrayList<Block> getBlocks() {
		return blocks;
	}



	public ArrayList<Tray> posMoves() {
		ArrayList<Tray> babies = new ArrayList<Tray>();

		for(Block block : blocks) {
			for(int i =0; i < occupied.length; i++) {
				int row = i/trayWidth;
				int col = i%trayWidth;

				if (occupied[i] == null) { 
					System.out.println("row: " + row + " col: " + col);
					if (validMove(block, row, col)) {

						Tray t = this.copy();
						t.moveBlock(t.blocks.get(blocks.indexOf(block)), row, col);	
						babies.add(t);	

					}

				}

		}

	}
/*
		//removes duplicates
		for (Tray t : babies) {
			for (Tray b : babies) {
				if(t.equals(b) && (babies.indexOf(t) != babies.indexOf(b)) ) {
					babies.remove(b);

				}
			}
		}*/
		return babies;
	}


	public boolean equals (Tray t) {
	if ((height() != t.height()) || (width() != t.width())) {
		return false;
	}
	boolean inOther;
	for (Block block : blocks) {
		inOther = false;
		for (Block otherBlock : t.getBlocks()) {
			if (block.equals(otherBlock)) {
				inOther = true;
			}
		}
		if (!inOther) {
			return false;
		}
	}
	for (Block block : t.getBlocks()) {
		inOther = false;
		for (Block otherBlock : blocks) {
			if (block.equals(otherBlock)) {
				inOther = true;
			}
		}
		if (!inOther) {
			return false;
		}
	}
	return true;
}


}
