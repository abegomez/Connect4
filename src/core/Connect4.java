package core;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import ui.Connect4TextConsole;

/**
 * A Connect4 Console Game
 * 
 * @author Abraham Gomez
 * @version 1.0
 * @see ui.Connect4TextConsole
 * @see GameBoard 
 * @see Player
 */
public class Connect4 {
	private GameBoard gb;
	private Player playerX;
	private Player playerO;
	private static Scanner in;
	private boolean play;
	private boolean tie;
	
	
	/** 
	 * Initializes a new Connect4 object
	 * Instance variables set to default values and instanciated.
	 */
	public Connect4() {
		gb = new GameBoard();
		tie = true;
		play= true;
	}
	
	/**
	 * Gameboard class
	 */
	public class GameBoard {
		char[][] board;
		int rows = 6;
		int cols = 7;

		/**
		 * Initialized gameboard with matrix filled with ' '
		 */
		public GameBoard() {
			board = new char[rows][cols];
			for(int row = 0; row < rows; row++) {
				for(int col = 0; col < cols; col++) {
					board[row][col] = ' ';
				}
			}
		}

		/**
		 * Returns number of rows of gameboard
		 * 
		 * @return Number of rows
		 */
		public int getRows() {
			return this.rows;
		}
		
		/**
		 * Returns number of columns
		 * 
		 * @return Number of columns
		 */
		public int getCols() {
			return this.cols;
		}
		
		/**
		 * Returns the character found at the row and col input
		 * 
		 * @param row Row of desired element
		 * @param col Column of desired element
		 * 
		 * @return Character found at row, column of gameboard
		 */
		public char getElement(int row, int col) {
			return board[row][col];
		}
		
		/**
		 * Checks if the column contains an open space
		 * 
		 * @param column The column to check
		 * @return True if column is not filled
		 */
		public boolean isValidColumn(int column) {
			if(gb.board[0][column] == ' ')
				return true;
			return false;
		}
		
		public List<Integer> getValidColumns() {
			List<Integer> list = new ArrayList<Integer>();
			for(int i =0; i < getCols(); i++) {
				if(isValidColumn(i)) {
					list.add(i);
				}
			}
			return list;
		}
	}
	public GameBoard getGameBoard() {
		return this.gb;
	}
	/**
	 * Player class
	 * @see GameBoard
	 */
	public class Player {
		char token;

		/**
		 * Initializes a Player object
		 * 
		 * @param c The character to use for player
		 */
		public Player(char c) {
			this.token = c;
		}

		/**
		 * Returns the players character
		 * 
		 * @return The player's character
		 */
		public char getChar() {
			return this.token;
		}

		/**
		 * Asks the player to input a column.  After validation, puts
		 * player's character in spot.
		 * 
		 * @see GameBoard
		 */
		public void takeTurn() {
			int input;
			in = new Scanner(System.in);
			do{
				System.out.println("Player " + this.getChar() + " Choose column: 1-7");
				System.out.println("[1][2][3][4][5][6][7]");
				while(!in.hasNextInt()) {
					System.out.println("Invalid input. Try again!");
					in.next();
				}
				input = in.nextInt();
				if(input <1 || input > gb.getCols())
					System.out.println("Invalid Column! Try Again.");
			} while (input <1 || input > gb.getCols());
			
			if( gb.isValidColumn(input-1)) {
				for(int i = gb.rows -1; i >=0; i--) {
					if(gb.board[i][input-1] == ' ') {
						gb.board[i][input-1] = this.token;
						break;
					}
				}
			} else {
				System.out.println("Column is already full. Try again. \n");
				this.takeTurn();
			}
		}
	}
	
	
	
	/** 
	 * Checks game board for 4 consecutive spaces filled with player's char
	 * in horizontal, vertical, or diagonal directions
	 * 
	 * @param p The player that just took their turn
	 * @return boolean returns true if player won, false if not
	 * @see Player
	 */
	public boolean checkWinner(Player p) {
		char tok = p.getChar();
		if(checkHorizontal(tok)|| checkVertical(tok) ||
		   checkDiagLR(tok) || checkDiagRL(tok)) {
			System.out.println(" Player " + p.getChar() + " won!");
			Connect4TextConsole.displayBoard(gb);
			return true;
		}
		return false;
	}
	
	/** 
	 * Checks every row for 4 consecutive spaces that match the player's 
	 * character.
	 * 
	 * @param c The character to check for
	 * @return boolean returns true if 4 consecutive spaces filled matching c
	 */
	public boolean checkHorizontal(char c) {
		for(int row = 0; row < gb.getRows(); row++) {
			for(int col = 0; col < gb.getCols() -3; col++) {
				if(gb.getElement(row, col) == c &&
				   gb.getElement(row, col+1) == c &&
				   gb.getElement(row, col+2) == c &&
				   gb.getElement(row, col+3) == c)
					return true;
			}
		}
		return false;
	}
	
	/** 
	 * Checks every column for 4 consecutive spaces that param c 
	 * character.
	 * 
	 * @param c The character to check for
	 * @return boolean returns true if 4 consecutive spaces filled matching c
	 */
	public boolean checkVertical(char c) {
		for(int col = 0; col < gb.getCols(); col++) {
			for(int row =0; row < gb.getRows()-3; row++) {
				if(gb.getElement(row, col) == c &&
				   gb.getElement(row+1, col)==c &&
				   gb.getElement(row+2, col)==c &&
				   gb.getElement(row+3, col)==c)
					return true;
			}
		}
		return false;
	}
	
	/** 
	 * Checks every possible diagonal top left to right for 4 consecutive spaces 
	 * that match param c character
	 * 
	 * @param c The character to check for
	 * @return boolean returns true if 4 consecutive spaces filled matching c
	 */
	public boolean checkDiagLR(char c) {
		for(int row = 0; row < gb.getRows() -3; row++) {
			for(int col = 0; col < gb.getCols() -3; col++) {
				if(gb.getElement(row, col) == c && 
				   gb.getElement(row+1, col+1) == c &&
				   gb.getElement(row+2, col+2) == c &&
				   gb.getElement(row+3, col+3) == c)
					return true;
			}
		}
		return false;
	}
	
	/** 
	 * Checks every possible diagonal top right to left for 4 consecutive spaces 
	 * that match param c character
	 * 
	 * @param c The character to check for
	 * @return boolean returns true if 4 consecutive spaces filled matching c
	 */
	public boolean checkDiagRL(char c) {
		for(int row = 0; row < gb.getRows() -3; row++) {
			for(int col = 3; col < gb.getCols(); col++) {
				if(gb.getElement(row, col) == c && 
				   gb.getElement(row+1, col-1) == c &&
				   gb.getElement(row+2, col-2) == c &&
				   gb.getElement(row+3, col-3) == c)
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Start of the Connect4 game.  
	 * Players alternate turns until board is filled or a player has 4 
	 * adjacent spaces filled with his/her character
	 */
	public void start() {	
		initializePlayers();
		System.out.println("Welcome to Connect4!\n");
		Connect4TextConsole.displayBoard(gb);
		boolean turn = true;
		Player p = playerX;

		for(int i =1; i<=42; i++) {
			p.takeTurn();

			if(checkWinner(p)) {
				play = false;
				tie = false;
				break;
			}

			if(turn) 
				p = playerO;
			else
				p = playerX;

			turn = !turn;

			Connect4TextConsole.displayBoard(gb);
		}
		
		if(tie) {
			play = false;
			System.out.println("Tie Game!");
		}
		in.close();
	}
	
	//TODO validate input
	public void initializePlayers() {
		Scanner in = new Scanner(System.in);
		System.out.println("Would you like to play against another (P)layer or a (C)omputer");
		String res = in.next();
		
		if(res.charAt(0) =='P' ||res.charAt(0) == 'p')
			playerO = new Player('O');
		else
			playerO = new Connect4ComputerPlayer(this, 'O');
		
		playerX = new Player('X');
	}

	
	/** 
	 * Main function to start game
	 * @param args[] not used
	 */
	public static void main(String[] args) {
		Connect4 game = new Connect4();
		while(game.play) {
			game.start();
		}
		
	}
}
