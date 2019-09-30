package core;

import java.io.*;
import java.net.*;
import java.util.Optional;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.stage.Stage;
import javafx.util.Callback;
import ui.Connect4GUI;
import ui.Connect4GUI.Cell;
import ui.Connect4TextConsole;
import core.Connect4.Connect4Constants;
import core.Connect4.GameBoard;
import core.Connect4.Player;

/**
 * Client for Connect4 game
 * 
 * @author Abraham Gomez
 * @version 1.0
 */
public class Connect4Client extends Connect4GUI implements Connect4Constants {

	private Label lblStatus = new Label();
	private Label lblTitle = new Label();
	private int portNo = 8000;
	private String host = "localhost";
	private DataInputStream fromServer;
	private DataOutputStream toServer;
	private char myToken;
	private char otherToken;
	private boolean myTurn;
	private boolean continueToPlay = true;
	private boolean waiting = true;
	private int rowSelected;
	private int colSelected;
	private boolean pOpponent;
	private Scanner in;
	private Socket socket;

	
	/**
	 *Start method for Client GUI
	 */
	@Override // Override the start method in the Connect4GUI class
	public void start(Stage primaryStage) throws Exception {

		GridPane pane = new GridPane();
		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(pane);
		borderPane.setBottom(lblStatus);

		for (int i = 0; i < 6; i++)
			for (int j = 0; j < 7; j++)
				pane.add(cells[i][j] = new Cell(i, j), j, i);
		Scene scene = new Scene(borderPane, 450, 385);
		primaryStage.setTitle("Welcome to Connect4"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage
		// Connect to the server
		connectToServer();
		lblStatus.setText("Follow console prompts.");
		// ask text or GUI

		Dialog<Boolean> textDialog = new Dialog<>();
		textDialog.setTitle("Play Text Console or GUI?");
		textDialog.setResizable(true);
		textDialog.setHeaderText("Welcome to Connect4.  Which interface would you like to play on?");
		ButtonType textType = new ButtonType("Text Console", ButtonData.LEFT);
		ButtonType GUIType = new ButtonType("GUI", ButtonData.RIGHT);
		textDialog.getDialogPane().getButtonTypes().addAll(textType, GUIType);
		textDialog.setResultConverter(new Callback<ButtonType, Boolean>() {
			@Override
			public Boolean call(ButtonType b) {
				return b == GUIType;
			}
		});

		Optional<Boolean> GUIResult = textDialog.showAndWait();
		if (!GUIResult.get()) {
			primaryStage.hide();
			playTextGame();
		} else {
			Dialog<Boolean> dialog = new Dialog<>();
			dialog.setTitle("Play against another player or against a computer?");
			dialog.setResizable(true);
			dialog.setHeaderText("Welcome to Connect4.  Who is your opponent?");
			ButtonType playerType = new ButtonType("Player", ButtonData.LEFT);
			ButtonType computerType = new ButtonType("Computer", ButtonData.RIGHT);
			dialog.getDialogPane().getButtonTypes().addAll(playerType, computerType);
			dialog.setResultConverter(new Callback<ButtonType, Boolean>() {
				@Override
				public Boolean call(ButtonType b) {
					return b == playerType;
				}
			});

			Optional<Boolean> result = dialog.showAndWait();
			pOpponent = result.get();
			initializeGame(pOpponent);
			startNetworkGame(pOpponent);
		}
	}

	
	/**
	 * Prompts user between GUI or Text console game
	 * @return true if user wants GUI
	 */
	public boolean isGUI() {
		try {
			in = new Scanner(System.in);
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
	 * Start of a text console game. Prompts user between another player or a computer opponent
	 */
	private void playTextGame() {
		try {
			in = new Scanner(System.in);
			System.out.println("Enter (P) to play against another player or (C) to play against the computer.");
			while (!in.hasNext("[pPcC]")) {
				System.out.println(
						"That's not a valid choice." + " (P) to play against player or (C) to play against computer");
				in.next();
			}

			String res = in.next();
			if (res.charAt(0) == 'P' || res.charAt(0) == 'p') {
				pOpponent = true;
				System.out.println("\nThank you! Playing against a player\n");
			} else {
				pOpponent = false;
				System.out.println("\nThank you! Playing against a computer\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		initializeGame(pOpponent);
		new Thread(() -> {
			try {
				toServer.writeBoolean(pOpponent);

				int player = fromServer.readInt();

				if (player == PLAYER1) {
					myToken = 'X';
					otherToken = 'O';
					currPlayer = playerX;
					Platform.runLater(() -> {
						System.out.println("Player 1 with token 'X'");
						System.out.println("Waiting for player 2 to join");
					});

					// Receive startup notification from the server
					fromServer.readInt(); // Whatever read is ignored

					// The other player has joined
					System.out.println("Player 2 has joined. I start first");

					// It is my turn
					myTurn = true;
				} else if (player == PLAYER2) {
					myToken = 'O';
					otherToken = 'X';
					currPlayer = playerX;
					Platform.runLater(() -> {
						System.out.println("Player 2 with token 'O'");
						System.out.println("Waiting for player 1 to move\n");
					});
				}

				// Continue to play
				while (continueToPlay) {
					if (player == PLAYER1) {
						currPlayer = playerX;
						 // Wait for player 1 to move
						takeTurnText();
						sendMove(); // Send the move to the server
						int actualRow = receiveRow();
						if (actualRow < 0) {
							System.out.println("" + "Column is full. Choose a different column.");
							continue;
						}

						receiveInfoFromServerText(); // Receive info from the server
					} else if (player == PLAYER2) {
						currPlayer = playerO;
						receiveInfoFromServerText(); // Receive info from the server
						// Wait for player 2 to move
						if(continueToPlay) {
							takeTurnText();
							sendMove(); // Send player 2's move to the server
							int actualRow = receiveRow();
							if (actualRow < 0) {
								System.out.println("" + "Column is full. Choose a different column.");
								continue;
							}
						}
						
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	/**
	 * Player's turn to select a column
	 */
	private void takeTurnText() {
		int input;
			in = new Scanner(System.in);
		do{
			System.out.println(" Choose column: 1-7");
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
			gb.setToken(input, myToken);
			rowSelected = 0;
			colSelected = input-1;
		} else {
			System.out.println("Column is already full. Try again. \n");
			this.takeTurnText();
		}
		Connect4TextConsole.displayBoard(gb);
		System.out.println("Waiting for player 1 to move.");
		waiting = false;
	}
	
	
	/**
	 * Receives game status from server
	 * @throws IOException if error receiving data
	 */
	private void receiveInfoFromServerText() throws IOException {
		// Receive game status
		int status = fromServer.readInt();

		if (status == PLAYER1_WON) {
			// Player 1 won, stop playing
			continueToPlay = false;
			if (myToken == 'X') {
				System.out.println("I won!");
			} else if (myToken == 'O') {
				receiveMoveText();
				System.out.println("Player 1 (RED) has won!");				
			}
		} else if (status == PLAYER2_WON) {
			// Player 2 won, stop playing
			continueToPlay = false;
			if (myToken == 'O') {
				System.out.println("I won!");
			} else if (myToken == 'X') {
				receiveMoveText();
				System.out.println("Player 2 (YELLOW) has won!");
			}
		} else if (status == DRAW) {
			// No winner, game is over
			continueToPlay = false;
			System.out.println("Game is over, no winner!");

			if (myToken == 'O') {
				receiveMoveText();
			}
		} else {
			receiveMoveText();
			//Platform.runLater(() -> System.out.println("My turn"));
			myTurn = true; // It is my turn
		}
	}

	
	/**
	 * Receives row and column from server from opponent
	 * @throws IOException if error receiving data from server
	 */
	private void receiveMoveText() throws IOException {
		// Get the other player's move
		int row = fromServer.readInt();
		int column = fromServer.readInt();
		//Platform.runLater(() -> cells[row][column].setToken(otherToken));
		System.out.println("Opponent chose column: " + (column+1));
		gb.setToken(column + 1, otherToken);
		Connect4TextConsole.displayBoard(gb);
	}

	
	/**
	 * Start of a game over the server
	 * @param playerOpponent true if the user is playing against another player
	 */
	private void startNetworkGame(boolean playerOpponent) {
		lblStatus.setText("Connected to server.");
		// Control the game on a separate thread
		new Thread(() -> {
			try {
				toServer.writeBoolean(playerOpponent);
				// Get notification from the server
				int player = fromServer.readInt();

				// Am I player 1 or 2?
				if (player == PLAYER1) {
					myToken = 'X';
					otherToken = 'O';
					currPlayer = playerX;
					Platform.runLater(() -> {
						lblTitle.setText("Player 1 with token 'X'");
						lblStatus.setText("Waiting for player 2 to join");
					});

					// Receive startup notification from the server
					fromServer.readInt(); // Whatever read is ignored

					// The other player has joined
					Platform.runLater(() -> lblStatus.setText("Player 2 has joined. I start first"));

					// It is my turn
					myTurn = true;
				} else if (player == PLAYER2) {
					myToken = 'O';
					otherToken = 'X';
					currPlayer = playerX;
					Platform.runLater(() -> {
						lblTitle.setText("Player 2 with token 'O'");
						lblStatus.setText("Waiting for Red to move");
					});
				}

				// Continue to play
				while (continueToPlay) {
					if (player == PLAYER1) {
						currPlayer = playerX;
						waitForPlayerAction(); // Wait for player 1 to move
						sendMove(); // Send the move to the server
						int actualRow = receiveRow();
						if (actualRow < 0) {
							Platform.runLater(
									() -> lblStatus.setText("" + "Column is full. Choose a different column."));
							continue;
						}

						receiveInfoFromServer(); // Receive info from the server
					} else if (player == PLAYER2) {
						currPlayer = playerO;
						receiveInfoFromServer(); // Receive info from the server
						waitForPlayerAction(); // Wait for player 2 to move
						sendMove(); // Send player 2's move to the server
						int actualRow = receiveRow();
						if (actualRow < 0) {
							Platform.runLater(
									() -> lblStatus.setText("" + "Column is full. Choose a different column."));
							continue;
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	
	/**
	 * Opens connection with server
	 */
	private void connectToServer() {

		// Create a socket to connect to the server
		try {
			socket = new Socket(host, portNo);

			// Create an input stream to receive data from the server
			fromServer = new DataInputStream(socket.getInputStream());

			// Create an output stream to send data to the server
			toServer = new DataOutputStream(socket.getOutputStream());
		} catch (Exception ce) {
			Platform.runLater(() -> lblStatus.setText("Unable to connect " + "to Server. Restart server and client"));
		}

	}

	
	/**
	 * Used in testing to receive input from server
	 * @return row received from server
	 * @throws IOException if error receiving data
	 */
	private int receiveRow() throws IOException {
		int valid = fromServer.readInt();
		return valid;
	}

	
	/**
	 * Puts thread to sleep until player makes a move
	 * @throws InterruptedException if error sleeping thread
	 */
	private void waitForPlayerAction() throws InterruptedException {
		while (waiting) {
			Thread.sleep(100);
		}
		waiting = true;
	}

	
	/**
	 * Sends the users row and column to the server
	 * @throws IOException if error transmitting data to server
	 */
	private void sendMove() throws IOException {
		toServer.writeInt(rowSelected); // Send the selected row
		toServer.writeInt(colSelected); // Send the selected column
	}

	/**
	 * Receives status from server
	 * @throws IOException if error receiving status data
	 */
	private void receiveInfoFromServer() throws IOException {
		// Receive game status
		int status = fromServer.readInt();

		if (status == PLAYER1_WON) {
			// Player 1 won, stop playing
			continueToPlay = false;
			if (myToken == 'X') {
				Platform.runLater(() -> lblStatus.setText("I won!"));
			} else if (myToken == 'O') {
				Platform.runLater(() -> lblStatus.setText("Player 1 (RED) has won!"));
				receiveMove();
			}
		} else if (status == PLAYER2_WON) {
			// Player 2 won, stop playing
			continueToPlay = false;
			if (myToken == 'O') {
				Platform.runLater(() -> lblStatus.setText("I won!"));
			} else if (myToken == 'X') {
				Platform.runLater(() -> lblStatus.setText("Player 2 (YELLOW) has won!"));
				receiveMove();
			}
		} else if (status == DRAW) {
			// No winner, game is over
			continueToPlay = false;
			Platform.runLater(() -> lblStatus.setText("Game is over, no winner!"));

			if (myToken == 'O') {
				receiveMove();
			}
		} else {
			receiveMove();
			Platform.runLater(() -> lblStatus.setText("My turn"));
			myTurn = true; // It is my turn
		}
	}

	
	/**
	 * Receives row and column data from server and updates the gameboard
	 * @throws IOException exception if error receiving data
	 */
	private void receiveMove() throws IOException {
		// Get the other player's move
		int row = fromServer.readInt();
		int column = fromServer.readInt();
		Platform.runLater(() -> cells[row][column].setToken(otherToken));
		Platform.runLater(() -> gb.setToken(column + 1, otherToken));
	}

	
	/**
	 * Cell class, the spaces of the board
	 *
	 */
	class Cell extends Connect4GUI.Cell {
		private int row;
		private int column;
		private char token = ' ';

		
		/**
		 * Constructor for cell
		 * @param i row
		 * @param j column
		 */
		public Cell(int i, int j) {
			super(i, j);
			this.row = i;
			this.column = j;
			this.setOnMouseClicked(e -> handleMouseClick());
		}

		/**
		 * Updates the GUI with correct token
		 */
		protected void repaint() {
			if (token == 'X') {
				Ellipse ellipse = new Ellipse(this.getWidth() / 2, this.getHeight() / 2, this.getWidth() / 2 - 10,
						this.getHeight() / 2 - 10);
				ellipse.centerXProperty().bind(this.widthProperty().divide(2));
				ellipse.centerYProperty().bind(this.heightProperty().divide(2));
				ellipse.radiusXProperty().bind(this.widthProperty().divide(2).subtract(10));
				ellipse.radiusYProperty().bind(this.heightProperty().divide(2).subtract(10));
				ellipse.setStroke(Color.RED);
				ellipse.setFill(Color.RED);
				ellipse.setStyle("-fx-stroke-width:10px");
				getChildren().add(ellipse); // Add the ellipse to the pane
			} else if (token == 'O') {
				Ellipse ellipse = new Ellipse(this.getWidth() / 2, this.getHeight() / 2, this.getWidth() / 2 - 10,
						this.getHeight() / 2 - 10);
				ellipse.centerXProperty().bind(this.widthProperty().divide(2));
				ellipse.centerYProperty().bind(this.heightProperty().divide(2));
				ellipse.radiusXProperty().bind(this.widthProperty().divide(2).subtract(10));
				ellipse.radiusYProperty().bind(this.heightProperty().divide(2).subtract(10));
				ellipse.setStroke(Color.YELLOW);
				ellipse.setFill(Color.YELLOW);
				ellipse.setStyle("-fx-stroke-width:10px");
				getChildren().add(ellipse); // Add the ellipse to the pane
			}
		}

		
		/**
		 * Response to a mouse click.  Updates gameboard, row and column to be sent to server, tells thread to continue
		 */
		private void handleMouseClick() {
			if (gb.isValidColumn(this.column) && myTurn) {
				setTokenInGB(this.column, myToken);

				myTurn = false;
				rowSelected = row;
				colSelected = column;
				lblStatus.setText(myToken=='O'?"Waiting for Red to Move":"Waiting for Yellow to Move");
				waiting = false;
			}
		}
	}

	
	/**
	 * Start of client GUI
	 * @param args String arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
