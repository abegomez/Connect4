package core;

import java.io.*;
import java.net.*;
import java.util.Date;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import ui.Connect4GUI;
import ui.Connect4GUI.Cell;
import core.Connect4.Connect4Constants;
import core.Connect4.GameBoard;

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
	private boolean connected = false;
	private boolean waiting = true;
	private int rowSelected;
	private int colSelected;
	

	Cell[][] cells = new Cell[6][7];

	@Override // Override the start method in the Connect4GUI class
	public void start(Stage primaryStage) throws Exception {

		// Pane to hold cell
//		Connect4GUI gui = new Connect4GUI();
//		GridPane pane = new GridPane();
//		for (int i = 0; i < 3; i++)
//			for (int j = 0; j < 3; j++)
//				pane.add(cell[i][j] = gui.new Cell(i, j), j, i);
//
//		BorderPane borderPane = new BorderPane();
//		borderPane.setTop(lblTitle);
//		borderPane.setCenter(pane);
//		borderPane.setBottom(lblStatus);
//
//		// Create a scene and place it in the stage
//		Scene scene = new Scene(borderPane, 320, 350);
//		primaryStage.setTitle("TicTacToeClient"); // Set the stage title
//		primaryStage.setScene(scene); // Place the scene in the stage
//		primaryStage.show(); // Display the stage
		initializeGame(true);
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
	}

	
	private void connectToServer() {

		// Create a socket to connect to the server
		try {
			Socket socket = new Socket(host, portNo);

			// Create an input stream to receive data from the server
			fromServer = new DataInputStream(socket.getInputStream());

			// Create an output stream to send data to the server
			toServer = new DataOutputStream(socket.getOutputStream());
			connected = true;
		} catch (Exception ce) {
			Platform.runLater(() -> lblStatus.setText("Unable to connect... Trying again"));
		}

		lblStatus.setText("Connected to server.");
		// Control the game on a separate thread
		new Thread(() -> {
			try {
				// Get notification from the server
				int player = fromServer.readInt();

				// Am I player 1 or 2?
				if (player == PLAYER1) {
					myToken = 'X';
					otherToken = 'O';
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
					Platform.runLater(() -> {
						lblTitle.setText("Player 2 with token 'O'");
						lblStatus.setText("Waiting for player 1 to move");
					});
				}

				// Continue to play
				while (continueToPlay) {
					if (player == PLAYER1) {
						waitForPlayerAction(); // Wait for player 1 to move
						
						sendMove(); // Send the move to the server
	
						receiveInfoFromServer(); // Receive info from the server
					} else if (player == PLAYER2) {
						receiveInfoFromServer(); // Receive info from the server
						waitForPlayerAction(); // Wait for player 2 to move
						sendMove(); // Send player 2's move to the server
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	private int receiveRow() throws IOException {
		int valid = fromServer.readInt();
		return valid;
	}
	private void waitForPlayerAction() throws InterruptedException {
		while (waiting) {
			Thread.sleep(100);
		}

		waiting = true;
	}

	private void sendMove() throws IOException {
		toServer.writeInt(rowSelected); // Send the selected row
		toServer.writeInt(colSelected); // Send the selected column
	}

	private void receiveInfoFromServer() throws IOException {
		// Receive game status
		int status = fromServer.readInt();

		if (status == PLAYER1_WON) {
			// Player 1 won, stop playing
			continueToPlay = false;
			if (myToken == 'X') {
				Platform.runLater(() -> lblStatus.setText("I won! (X)"));
			} else if (myToken == 'O') {
				Platform.runLater(() -> lblStatus.setText("Player 1 (X) has won!"));
				receiveMove();
			}
		} else if (status == PLAYER2_WON) {
			// Player 2 won, stop playing
			continueToPlay = false;
			if (myToken == 'O') {
				Platform.runLater(() -> lblStatus.setText("I won! (O)"));
			} else if (myToken == 'X') {
				Platform.runLater(() -> lblStatus.setText("Player 2 (O) has won!"));
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

	private void receiveMove() throws IOException {
		// Get the other player's move
		int row = fromServer.readInt();
		int column = fromServer.readInt();
		Platform.runLater(() -> cells[row][column].setToken(otherToken));
	}

	class Cell extends Connect4GUI.Cell {
		private int row;
		private int column;
		private char token = ' ';

		public Cell(int i, int j) {
			super(i, j);
			this.row = i;
			this.column = j;
			this.setOnMouseClicked(e -> handleMouseClick());
		}

		private void setToken(char otherToken) {
			this.token = otherToken;
			repaint();
		}

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

		private void handleMouseClick() {
			if (gb.isValidColumn(this.column) && myTurn) {
				setToken(myToken);
				myTurn = false;
				rowSelected = row;
				colSelected = column;
				lblStatus.setText("Waiting for other player to move");
				waiting = false;
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

}
