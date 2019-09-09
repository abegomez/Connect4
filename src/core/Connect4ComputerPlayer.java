package core;

import java.util.List;
import java.util.Random;

import core.Connect4.GameBoard;
import core.Connect4.Player;
import core.Connect4;

public class Connect4ComputerPlayer extends Player{
	private GameBoard gb;
	public Connect4ComputerPlayer(Connect4 connect4, char c) {
		connect4.super(c);
		gb = connect4.getGameBoard();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void takeTurn() {
		int randColumn = getRandomColumn();
		for(int i = gb.rows -1; i >=0; i--) {
			if(gb.board[i][randColumn] == ' ') {
				gb.board[i][randColumn] = this.token;
				break;
			}
		}
		System.out.println("Computer Player chooses column " + (randColumn+1));
		
	}
	
	public int getRandomColumn() {
		List<Integer> validColumns = gb.getValidColumns();
		Random r = new Random();
		return validColumns.get(r.nextInt(validColumns.size()));
	}

}
