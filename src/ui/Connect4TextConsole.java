package ui;

import core.Connect4.GameBoard;

/**
 * Console display for Connect4
 * 
 * @author Abraham Gomez
 * @version 1.0
 * @see core.Connect4
 * @see GameBoard
 */
public class Connect4TextConsole {
	
	/** 
	 * Displays the game board
	 * 
	 * @param gb The board to display
	 */
	public static void displayBoard(GameBoard gb) {
		if(gb == null) 
			throw new IllegalArgumentException();
		int rows = gb.getRows();
		int cols = gb.getCols();
		for(int row = 0; row < rows; row++) {
			System.out.print('|');
			for(int col = 0; col < cols; col++) {
				System.out.print(gb.getElement(row, col)+"|");
			}
			System.out.println();
		}
		System.out.println();
	}
}
