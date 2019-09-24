package core;

import java.util.List;
import java.util.Random;

import core.Connect4.GameBoard;
import core.Connect4.Player;
import core.Connect4;
/**
 * A Computer player for Connect4
 * 
 * @author Abraham Gomez
 * @version 2.0
 * @see ui.Connect4TextConsole
 * @see core.Connect4
 * @see GameBoard 
 * @see Player
 */
public class Connect4ComputerPlayer extends Player {
	private GameBoard gb;
	
	/**
	 * Constructor for a computer player
	 * 
	 * @param connect4 The connect 4 game reference
	 * @param c The computer player's token
	 */
	public Connect4ComputerPlayer(Connect4 connect4, char c) {
		connect4.super(c);
		gb = connect4.getGameBoard();
	}
	
	/**
	 * Override take turn function to choose a column
	 * for the computer player
	 */
	@Override
	public void takeTurn() {
		int randColumn;
		try {
			randColumn = getRandomColumn();
			for(int i = gb.rows -1; i >=0; i--) {
				if(gb.board[i][randColumn] == ' ') {
					gb.board[i][randColumn] = this.token;
					break;
				}
			}
			System.out.println("Computer Player chooses column " + (randColumn+1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the column from getRandomColumn()
	 * 
	 * @return the random column or -1 if unable to do so
	 */
	@Override
	public int takeTurnGUI() {
		int randColumn;
		try {
			randColumn = getRandomColumn();
			return randColumn;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -1;
	
	}
	
	/**
	 * Gets the list of valid columns of the game and returns
	 * a randomly chosen column from the list
	 * 
	 * @return randomly chosen column from list of valid columns
	 * @throws Exception from creating random column
	 */
	public int getRandomColumn() throws Exception {
		List<Integer> validColumns = gb.getValidColumns();
		try {
			Random r = new Random();
			return validColumns.get(r.nextInt(validColumns.size()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
	}
}
