package core;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.Connect4TextConsole;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import java.io.*;
import java.net.*;
import javafx.application.Platform;
import java.util.Date;
import core.Connect4.Connect4Constants;
import core.Connect4.GameBoard;
import core.Connect4.Player;

/**
 * Server for Connect4 game
 * 
 * @author Abraham Gomez
 * @version 1.0
 */
public class Connect4Server extends Application implements Connect4Constants {
	private int sessionNo = 1;
	private int portNo = 8000;

	/**
	 * Entry to Server Program
	 */
	@Override
	public void start(Stage primaryStage) {
		TextArea taLog = new TextArea();

		// scene
		Scene scene = new Scene(new ScrollPane(taLog), 450, 200);
		primaryStage.setTitle("Connect4 Server"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage

		new Thread(() -> {
			try {
				// Create a server socket
				ServerSocket serverSocket = new ServerSocket(portNo);
				Platform.runLater(() -> taLog.appendText(new Date() + ": Server started at socket " + portNo + "\n"));

				// Ready to create a session for every two players
				while (true) {
					Platform.runLater(() -> taLog
							.appendText(new Date() + ": Waiting for players to join session " + sessionNo + '\n'));

					// Connect to player 1
					Socket player1 = serverSocket.accept();

					Platform.runLater(() -> {
						taLog.appendText(new Date() + ": Player 1 joined session " + sessionNo + '\n');
						taLog.appendText("Player 1's IP address" + player1.getInetAddress().getHostAddress() + '\n');
					});

					boolean playerGame = new DataInputStream(player1.getInputStream()).readBoolean();
					// Notify that the player is Player 1
					if (playerGame) {
						new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);

						// Connect to player 2
						Socket player2 = serverSocket.accept();

						Platform.runLater(() -> {
							taLog.appendText(new Date() + ": Player 2 joined session " + sessionNo + '\n');
							taLog.appendText(
									"Player 2's IP address" + player2.getInetAddress().getHostAddress() + '\n');
						});

						// Notify that the player is Player 2
						new DataOutputStream(player2.getOutputStream()).writeInt(PLAYER2);

						// Display this session and increment session number
						Platform.runLater(() -> taLog
								.appendText(new Date() + ": Start a thread for session " + sessionNo++ + '\n'));

						boolean player2Game = new DataInputStream(player2.getInputStream()).readBoolean();
						// Launch a new thread for this session of two players
						new Thread(new HandleASession(player1, player2)).start();
					} else {
						new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);
						taLog.appendText(new Date() + ": Computer joined session " + sessionNo++ + '\n');
						new Thread(new HandleAComputerSession(player1)).start();
					}

				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	/**
	 * Handles a session when a player is playing against a computer
	 */
	class HandleAComputerSession implements Runnable, Connect4Constants {
		private Socket player1;
		private Socket player2;
		private Player playerX;
		private Player playerO;
		private Connect4 connect4;
		private GameBoard gb;
		private int actualRow;
		private int compCol;

		/**
		 * Constructor for handling a session against a computer
		 * 
		 * @param player1 the player socket
		 */
		public HandleAComputerSession(Socket player1) {
			this.player1 = player1;
			connect4 = new Connect4();
			playerX = connect4.new Player('X');
			playerO = new Connect4ComputerPlayer(connect4, 'O');
			gb = connect4.getGameBoard();
		}

		/**
		 * Starts game logic when a player plays against a computer
		 */
		public void run() {
			try {
				DataInputStream fromPlayer1 = new DataInputStream(player1.getInputStream());
				DataOutputStream toPlayer1 = new DataOutputStream(player1.getOutputStream());

				toPlayer1.writeInt(1);

				while (true) {
					// Receive a move from player 1
					int row = 0;
					int column = 0;
					boolean invalid = true;

					while (invalid) {
						row = fromPlayer1.readInt();
						column = fromPlayer1.readInt();
						if (gb.isValidColumn(column)) {
							actualRow = gb.setToken(column + 1, playerX.getChar());
							toPlayer1.writeInt(actualRow);
							invalid = false;
						} else {
							toPlayer1.writeInt(-1);
						}
					}

					// Check if Player 1 wins
					if (connect4.checkWinnerServer(playerX)) {
						toPlayer1.writeInt(PLAYER1_WON);
						break; // Break the loop
					} else if (gb.isFull()) { // Check if all cells are filled
						toPlayer1.writeInt(DRAW);
						break;
					} else {
						compCol = playerO.takeTurnGUI();
						actualRow = gb.setToken(compCol + 1, playerO.getChar());

					}

					// Check if Player 2 wins
					if (connect4.checkWinnerServer(playerO)) {
						toPlayer1.writeInt(PLAYER2_WON);
						sendMove(toPlayer1, actualRow, column);
						break;
					} else if (gb.isFull()) {
						toPlayer1.writeInt(DRAW);
						break;
					} else {
						// Notify player 1 to take the turn
						toPlayer1.writeInt(CONTINUE);

						// Send player 2's selected row and column to player 1
						sendMove(toPlayer1, actualRow, compCol);
					}
//				
					Connect4TextConsole.displayBoard(gb);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		/**
		 * Transmits the data to the computer player output stream
		 * 
		 * @param out    The player's output stream
		 * @param row    The row selected by opponent
		 * @param column The column selected by opponent
		 * @throws IOException DataOutputStream exception
		 */
		private void sendMove(DataOutputStream out, int row, int column) throws IOException {
			out.writeInt(row); // Send row index
			out.writeInt(column); // Send column index
		}
	}

	/**
	 * Class to handle a session between two players
	 */
	class HandleASession implements Runnable, Connect4Constants {
		private Socket player1;
		private Socket player2;
		private Player playerX;
		private Player playerO;
		private Connect4 connect4;
		private GameBoard gb;

		private int actualRow;

		
		/**
		 * Constructor for HandleASession
		 * @param player1 Player 1 socket
		 * @param player2 Player 2 socket
		 */
		public HandleASession(Socket player1, Socket player2) {
			this.player1 = player1;
			this.player2 = player2;
			connect4 = new Connect4();
			playerX = connect4.new Player('X');
			playerO = connect4.new Player('O');
			gb = connect4.getGameBoard();

		}

		/** Implement the run() method for the thread */
		public void run() {
			try {
				// Create data input and output streams
				DataInputStream fromPlayer1 = new DataInputStream(player1.getInputStream());
				DataOutputStream toPlayer1 = new DataOutputStream(player1.getOutputStream());
				DataInputStream fromPlayer2 = new DataInputStream(player2.getInputStream());
				DataOutputStream toPlayer2 = new DataOutputStream(player2.getOutputStream());

				// Write anything to notify player 1 to start
				// This is just to let player 1 know to start
				toPlayer1.writeInt(1);

				// Continuously serve the players and determine and report
				// the game status to the players
				while (true) {
					// Receive a move from player 1
					int row = 0;
					int column = 0;
					boolean invalid = true;
					while (invalid) {
						row = fromPlayer1.readInt();
						column = fromPlayer1.readInt();
						if (gb.isValidColumn(column)) {
							actualRow = gb.setToken(column + 1, playerX.getChar());
							toPlayer1.writeInt(actualRow);
							invalid = false;
						} else {
							toPlayer1.writeInt(-1);
						}
					}

					// Check if Player 1 wins
					if (connect4.checkWinnerServer(playerX)) {
						toPlayer1.writeInt(PLAYER1_WON);
						toPlayer2.writeInt(PLAYER1_WON);
						sendMove(toPlayer2, actualRow, column);
						break; // Break the loop
					} else if (gb.isFull()) { // Check if all cells are filled
						toPlayer1.writeInt(DRAW);
						toPlayer2.writeInt(DRAW);
						sendMove(toPlayer2, actualRow, column);
						break;
					} else {
						// Notify player 2 to take the turn
						toPlayer2.writeInt(CONTINUE);

						// Send player 1's selected row and column to player 2
						sendMove(toPlayer2, actualRow, column);
					}

					invalid = true;
					// Receive a move from Player 2
					while (invalid) {
						row = fromPlayer2.readInt();
						column = fromPlayer2.readInt();

						if (gb.isValidColumn(column)) {
							actualRow = gb.setToken(column + 1, playerO.getChar());
							toPlayer2.writeInt(actualRow);
							invalid = false;
						} else {
							toPlayer2.writeInt(-1);
						}
					}

					// Check if Player 2 wins
					if (connect4.checkWinnerServer(playerO)) {
						toPlayer1.writeInt(PLAYER2_WON);
						toPlayer2.writeInt(PLAYER2_WON);
						sendMove(toPlayer1, actualRow, column);
						break;
					} else if (gb.isFull()) {
						toPlayer1.writeInt(DRAW);
						toPlayer2.writeInt(DRAW);
						sendMove(toPlayer2, actualRow, column);
						break;
					} else {
						// Notify player 1 to take the turn
						toPlayer1.writeInt(CONTINUE);

						// Send player 2's selected row and column to player 1
						sendMove(toPlayer1, actualRow, column);
					}
//				
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		
		/**
		 * Sends row and column to server 
		 * @param out DataOutputStream to send data
		 * @param row the selected row
		 * @param column the selected column
		 * @throws IOException exception if error transmitting data
		 */
		private void sendMove(DataOutputStream out, int row, int column) throws IOException {
			out.writeInt(row); // Send row index
			out.writeInt(column); // Send column index
		}
	}

	/**
	 * Used when running from eclipse
	 * 
	 * @param args string arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
