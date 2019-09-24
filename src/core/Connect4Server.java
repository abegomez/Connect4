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
	private Connect4 connect4;

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

					// Notify that the player is Player 1
					new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);

					// Connect to player 2
					Socket player2 = serverSocket.accept();

					Platform.runLater(() -> {
						taLog.appendText(new Date() + ": Player 2 joined session " + sessionNo + '\n');
						taLog.appendText("Player 2's IP address" + player2.getInetAddress().getHostAddress() + '\n');
					});

					// Notify that the player is Player 2
					new DataOutputStream(player2.getOutputStream()).writeInt(PLAYER2);

					// Display this session and increment session number
					Platform.runLater(
							() -> taLog.appendText(new Date() + ": Start a thread for session " + sessionNo++ + '\n'));

					// Launch a new thread for this session of two players
					new Thread(new HandleASession(player1, player2)).start();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	class HandleASession implements Runnable, Connect4Constants {
		private Socket player1;
		private Socket player2;
		private Player playerX;
		private Player playerO;
		private Connect4 connect4;
		private GameBoard gb;
		// Create and initialize cells
		private char[][] cells = new char[3][3];
		private DataInputStream fromPlayer1;
		private DataOutputStream toPlayer1;
		private DataInputStream fromPlayer2;
		private DataOutputStream toPlayer2;
		private int actualRow;

		// Continue to play
		private boolean continueToPlay = true;

		/** Construct a thread */
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
				fromPlayer1 = new DataInputStream(player1.getInputStream());
				toPlayer1 = new DataOutputStream(player1.getOutputStream());
				fromPlayer2 = new DataInputStream(player2.getInputStream());
				toPlayer2 = new DataOutputStream(player2.getOutputStream());

				// Write anything to notify player 1 to start
				// This is just to let player 1 know to start
				toPlayer1.writeInt(1);

				// Continuously serve the players and determine and report
				// the game status to the players
				while (true) {
					// Receive a move from player 1
					int row = fromPlayer1.readInt();
					int column = fromPlayer1.readInt();
					if (gb.isValidColumn(column)) {
						actualRow = gb.setToken(column+1, playerX.getChar());
					}

					// Check if Player 1 wins
					if (connect4.checkWinner(playerX)) {
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
					
					// Receive a move from Player 2
					row = fromPlayer2.readInt();
					column = fromPlayer2.readInt();
					
					if (gb.isValidColumn(column)) {
						actualRow = gb.setToken(column+1, playerO.getChar());
					}

					// Check if Player 2 wins
					if (connect4.checkWinner(playerO)) {
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
				
					Connect4TextConsole.displayBoard(gb);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		/** Send the move to other player */
		private void sendMove(DataOutputStream out, int row, int column) throws IOException {
			out.writeInt(row); // Send row index
			out.writeInt(column); // Send column index
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

}
