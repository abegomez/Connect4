package core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ui.Connect4GUI;
import ui.Connect4TextConsole;

/**
 * A Connect4 Console and GUI Game Option to play against a computer and play on
 * a GUI or Text Console
 * 
 * @author Abraham Gomez
 * @version 4.0
 * @see ui.Connect4TextConsole
 * @see GameBoard
 * @see Player
 */
public class Connect4 implements Serializable {
	private static final long serialVersionUID = 1L;
	private GameBoard gb;
	private Player playerX;
	private Player playerO;
	private static Scanner in;
	private boolean play;
	private boolean tie;

	public interface Connect4Constants {
		public static int PLAYER1 = 1; // Indicate player 1
		public static int PLAYER2 = 2; // Indicate player 2
		public static int PLAYER1_WON = 1; // Indicate player 1 won
		public static int PLAYER2_WON = 2; // Indicate player 2 won
		public static int DRAW = 3; // Indicate a draw
		public static int CONTINUE = 4; // Indicate to continue}
		public static int VALID = 5;
	}

	/**
	 * Initializes a new Connect4 object Instance variables set to default values
	 * and instantiated.
	 */
	public Connect4() {
		gb = new GameBoard();
		tie = true;
		play = true;
	}

	/**
	 * Gameboard class
	 */
	public class GameBoard {
		public char[][] board;
		int rows = 6;
		int cols = 7;

		/**
		 * Initialized gameboard with matrix filled with ' '
		 */
		public GameBoard() {
			board = new char[rows][cols];
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < cols; col++) {
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
		public boolean isValidColumn(int column) throws IllegalArgumentException {
			if (column < 0 || column >= gb.getCols())
				throw new IllegalArgumentException();
			if (gb.board[0][column] == ' ')
				return true;
			return false;
		}

		/**
		 * Checks if board is filled up
		 * 
		 * @return true if board is full
		 */
		public boolean isFull() {
			for (int i = 0; i < this.cols; i++) {
				if (gb.getElement(0, i) == ' ')
					return false;
			}
			return true;
		}

		/**
		 * Sets token in gameboard
		 * 
		 * @param column column to set, need to -1 for 0-based index
		 * @param c      token to set space to
		 * @return returns the row the token is set in, -1 if unable to do so
		 */
		public int setToken(int column, char c) {
			for (int i = gb.rows - 1; i >= 0; i--) {
				if (gb.board[i][column - 1] == ' ') {
					gb.board[i][column - 1] = c;
					// Connect4TextConsole.displayBoard(gb);
					return i;
				}
			}
			return -1;
		}

		/**
		 * Helper function for computer player to get list of valid columns
		 * 
		 * @return List containing all valid columns
		 */
		public List<Integer> getValidColumns() {
			List<Integer> list = new ArrayList<Integer>();
			for (int i = 0; i < getCols(); i++) {
				if (isValidColumn(i)) {
					list.add(i);
				}
			}
			return list;
		}
	}

	/**
	 * Returns connect4 gameboard
	 * 
	 * @return current gameboard
	 */
	public GameBoard getGameBoard() {
		return this.gb;
	}

	/**
	 * Player class
	 * 
	 * @see GameBoard
	 */
	public class Player {
		char token;
		String name;

		/**
		 * Initializes a Player object
		 * 
		 * @param c The character to use for player
		 */
		public Player(char c) {
			this.token = c;
			name = "Player " + c;
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
		 * Returns the name of the player
		 * 
		 * @return player name
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * Changes the players name to input s
		 * 
		 * @param s the name to set player name to
		 */
		public void setName(String s) {
			this.name = s;
		}

		/**
		 * Required because player class is not abstract
		 * 
		 * @return 0 for the purpose of testing
		 */
		public int takeTurnGUI() {
			return 0;
		}

		/**
		 * Asks the player to input a column. After validation, puts player's character
		 * in spot.
		 * 
		 * @see GameBoard
		 */
		public void takeTurn() {
			int input;
			in = new Scanner(System.in);
			do {
				System.out.println(name + " Choose column: 1-7");
				System.out.println("[1][2][3][4][5][6][7]");
				while (!in.hasNextInt()) {
					System.out.println("Invalid input. Try again!");
					in.next();
				}
				input = in.nextInt();
				if (input < 1 || input > gb.getCols())
					System.out.println("Invalid Column! Try Again.");
			} while (input < 1 || input > gb.getCols());

			if (gb.isValidColumn(input - 1)) {
				gb.setToken(input, this.token);
//				for(int i = gb.rows -1; i >=0; i--) {
//					if(gb.board[i][input-1] == ' ') {
//						gb.board[i][input-1] = this.token;
//						break;
//					}
//				}
			} else {
				System.out.println("Column is already full. Try again. \n");
				this.takeTurn();
			}
		}
	}

	/**
	 * Returns the x player
	 * 
	 * @return playerX
	 */
	public Player getPlayerX() {
		return this.playerX;
	}

	/**
	 * Returns the O player
	 * 
	 * @return playerO
	 */
	public Player getPlayerO() {
		return this.playerO;
	}

	/**
	 * Checks game board for 4 consecutive spaces filled with player's char in
	 * horizontal, vertical, or diagonal directions
	 * 
	 * @param p The player that just took their turn
	 * @return boolean returns true if player won, false if not
	 * @see Player
	 */
	public boolean checkWinner(Player p) {
		char tok = p.getChar();
		if (checkHorizontal(tok) || checkVertical(tok) || checkDiagLR(tok) || checkDiagRL(tok)) {
			System.out.println(" " + p.name + " won!");
			Connect4TextConsole.displayBoard(gb);
			return true;
		}
		return false;
	}
	
	/**
	 * Checks game board for 4 consecutive spaces filled with player's char in
	 * horizontal, vertical, or diagonal directions for the server
	 * 
	 * @param p The player that just took their turn
	 * @return true if the player won
	 */
	public boolean checkWinnerServer(Player p) {
		char tok = p.getChar();
		if (checkHorizontal(tok) || checkVertical(tok) || checkDiagLR(tok) || checkDiagRL(tok)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks every row for 4 consecutive spaces that match the player's character.
	 * 
	 * @param c The character to check for
	 * @return boolean returns true if 4 consecutive spaces filled matching c
	 */
	public boolean checkHorizontal(char c) {
		for (int row = 0; row < gb.getRows(); row++) {
			for (int col = 0; col < gb.getCols() - 3; col++) {
				if (gb.getElement(row, col) == c && gb.getElement(row, col + 1) == c && gb.getElement(row, col + 2) == c
						&& gb.getElement(row, col + 3) == c)
					return true;
			}
		}
		return false;
	}

	/**
	 * Checks every column for 4 consecutive spaces that param c character.
	 * 
	 * @param c The character to check for
	 * @return boolean returns true if 4 consecutive spaces filled matching c
	 */
	public boolean checkVertical(char c) {
		for (int col = 0; col < gb.getCols(); col++) {
			for (int row = 0; row < gb.getRows() - 3; row++) {
				if (gb.getElement(row, col) == c && gb.getElement(row + 1, col) == c && gb.getElement(row + 2, col) == c
						&& gb.getElement(row + 3, col) == c)
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
		for (int row = 0; row < gb.getRows() - 3; row++) {
			for (int col = 0; col < gb.getCols() - 3; col++) {
				if (gb.getElement(row, col) == c && gb.getElement(row + 1, col + 1) == c
						&& gb.getElement(row + 2, col + 2) == c && gb.getElement(row + 3, col + 3) == c)
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
		for (int row = 0; row < gb.getRows() - 3; row++) {
			for (int col = 3; col < gb.getCols(); col++) {
				if (gb.getElement(row, col) == c && gb.getElement(row + 1, col - 1) == c
						&& gb.getElement(row + 2, col - 2) == c && gb.getElement(row + 3, col - 3) == c)
					return true;
			}
		}
		return false;
	}

	/**
	 * Start of the Connect4 game. Players alternate turns until board is filled or
	 * a player has 4 adjacent spaces filled with his/her character
	 */
	public void start() {
		System.out.println("Welcome to Connect4!\n");

		if (isGUISelected()) {
			playGUI();
		} else {
			initializePlayers();
			playTextConsole();
		}
		play = false;
	}

	/**
	 * Prompts the user to choose between Text console game or GUI version
	 * 
	 * @return true if player selects GUI version
	 */
	public boolean isGUISelected() {
		try {
			Scanner in = new Scanner(System.in);
			System.out.println("Enter (G) for GUI or (T) for Text Console");
			while (!in.hasNext("[gGtT]")) {
				System.out.println("That's not a valid choice");
				in.next();
			}

			String res = in.next();
			if (res.charAt(0) == 'T' || res.charAt(0) == 't')
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Calls the Connect4GUI main method
	 */
	public void playGUI() {
		// Connect4GUI gui = new Connect4GUI();
		Connect4GUI.main(null);
	}

	/**
	 * Text console game loop
	 */
	public void playTextConsole() {
		Connect4TextConsole.displayBoard(gb);
		boolean turn = true;
		Player p = playerX;

		for (int i = 1; i <= 42; i++) {
			p.takeTurn();

			if (checkWinner(p)) {
				play = false;
				tie = false;
				break;
			}

			if (turn)
				p = playerO;
			else
				p = playerX;

			turn = !turn;

			Connect4TextConsole.displayBoard(gb);
		}

		if (tie) {
			play = false;
			System.out.println("Tie Game!");
		}
		in.close();
	}

	/**
	 * Initializes Players or A player and computer
	 * 
	 * @see Player
	 * @see Connect4ComputerPlayer
	 */
	public void initializePlayers() {
		try {
			Scanner in = new Scanner(System.in);
			System.out.println("Enter (P) to play against another player or (C) to play against the computer.");
			while (!in.hasNext("[pPcC]")) {
				System.out.println(
						"That's not a valid choice." + " (P) to play against player or (C) to play against computer");
				in.next();
			}

			String res = in.next();
			if (res.charAt(0) == 'P' || res.charAt(0) == 'p')
				playerO = new Player('O');
			else {
				System.out.println("\nThank you! Playing against a computer\n");
				playerO = new Connect4ComputerPlayer(this, 'O');
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			playerX = new Player('X');
		}
	}

	/**
	 * Main function to start game
	 * 
	 * @param args[] not used
	 */
	public static void main(String[] args) {
		Connect4 game = new Connect4();
		while (game.play) {
			game.start();
		}
	}
}
