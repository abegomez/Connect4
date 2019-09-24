package ui;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import core.Connect4;
import core.Connect4.GameBoard;
import core.Connect4.Player;
import core.Connect4ComputerPlayer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Ellipse;

/**
 * GUI for computer game
 * @author Abraham Gomez
 * @version 3.0
 */
public class Connect4GUI extends Application {
	protected Player playerX;
	protected Player playerO;
	protected GameBoard gb;
	protected Connect4 connect4;
	private boolean play;
	protected int compCol;
	private Label lblStatus;
	protected boolean isPlayerOpponent;
	protected Player currPlayer;

	private Cell[][] cells = new Cell[6][7];
	
	/**
	 * Entry for JavaFX
	 * Creates dialog alert to prompt user for a player or computer opponent.  Sets up the game board.
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		lblStatus = new Label("Click a column to place your token!");
		Dialog<Boolean> dialog = new Dialog<>();
		dialog.setTitle("Play against another player or against a computer?");
		dialog.setResizable(true);
		dialog.setHeaderText("Welcome to Connect4.  Who is your opponent?");
		ButtonType playerType = new ButtonType("Player", ButtonData.LEFT);
		ButtonType computerType = new ButtonType("Computer", ButtonData.RIGHT);
		dialog.getDialogPane().getButtonTypes().addAll(playerType,computerType);
		dialog.setResultConverter(new Callback<ButtonType, Boolean>() {
			@Override
			public Boolean call(ButtonType b) {
				return b == playerType;
			}
		});
		
		Optional<Boolean> result = dialog.showAndWait();
		
		initializeGame(result.get());
		play = true;
		GridPane pane = new GridPane();
		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(pane);
		borderPane.setBottom(lblStatus);
		
		for (int i = 0; i < 6; i++)
			for (int j = 0; j < 7; j++)
	 			pane.add(cells[i][j] = new Cell(i,j), j, i);
		Scene scene = new Scene(borderPane, 450, 385);
		primaryStage.setTitle("Welcome to Connect4"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage
	}
	
	/**
	 * Initializes the game depending on if the opponent is a player or a computer
	 * Changes the names accordingly.
	 * 
	 * @param playerOpponent boolean if the opponent is a player
	 */
	protected void initializeGame(boolean playerOpponent) {
		connect4 = new Connect4();
		playerX = connect4.new Player('X');
		isPlayerOpponent = playerOpponent;
		if(playerOpponent) {
			playerO = connect4.new Player('O');
			playerO.setName("Yellow");
			playerX.setName("Red");
		} else {
			playerX.setName("You");
			playerO = new Connect4ComputerPlayer(connect4, 'O');
			playerO.setName("Computer");
		}
		
		currPlayer = playerX;
		gb = connect4.getGameBoard();
	}
	
	/**
	 * Entry point for the JavaFx GUI
	 * 
	 * @param args arguments sent to the main function
	 */
	public static void main(String[] args) {
		launch(args);
	}
	
	/**
	 * Calls the function to draw on the correct cell
	 * 
	 * @param cell Which cell to draw the token
	 */
	public void drawToken(Cell cell) {
		cell.setToken();
	}
	
	/**
	 * Calls check winner on the connect4 object and prints status in the bottom label
	 * 
	 * @param c  The character to check win condition on
	 * @return true if winner or full game
	 */
	public boolean checkWinner(char c) {
		// Check game status
 	    if (connect4.checkWinner(currPlayer)) {
 	    	lblStatus.setText(currPlayer.getName() + " won! The game is over");
 	    	play = false; // Game is over
 	    	return true;
 	    }
 	    else if (gb.isFull()) {
 	    	lblStatus.setText("Draw! The game is over");
 	    	play= false; // Game is over
 	    	return true;
 	    }
		return false;
	}
	
	/**
	 * Cell class that is the space for where a user can place a token.
	 * 
	 * @author Abraham Gomez
	 * @version 1.0
	 *
	 */
	public class Cell extends Pane {
	    // Token used for this cell
	    private char token = ' ';

	    private int column;
	    private int row;

	    /**
	     * Constructs the cell with appropriate dimensions and default appearance
	     * 
	     * @param i The cell's row
	     * @param j The cell's column
	     */
	    public Cell(int i, int j) {
		 	setStyle("-fx-border-color: blue; -fx-background-color:BLUE");
		 	this.setPrefSize(2000, 2000);
		 	this.setOnMouseClicked(e -> handleMouseClick());
		 	this.column = j;
		 	this.row = i;
		 	Ellipse e = new Ellipse(this.getWidth() / 2,
			 	    this.getHeight() / 2, this.getWidth() / 2 - 10,
			 	    this.getHeight() / 2 - 10);
		 	e.centerXProperty().bind(this.widthProperty().divide(2));
	 	    e.centerYProperty().bind(this.heightProperty().divide(2));
		    e.radiusXProperty().bind(this.widthProperty().divide(2).subtract(10));
	 	    e.radiusYProperty().bind(this.heightProperty().divide(2).subtract(10));
		    e.setStroke(Color.WHITE);
	 	    e.setFill(Color.WHITE);
	 	    e.setStyle("-fx-stroke-width:10px");
		    getChildren().add(e); // Add the ellipse to the pane
	    }

	    /**
	     * Sets token in gameboard and draws the token in the correct cell
	     * 
	     * @param column the column to set the token in
	     * @param c the player character to decide which symbol to print
	     */
	    public void setTokenInGB(int column, char c) {
	    	int setRow = gb.setToken(column+1, c);
	    	Cell setCell = cells[setRow][column];
	    	drawToken(setCell);
	    }
	    
	    /** 
	     * Draws the current players token on the GUI
	     */
	    public void setToken() {
	    	token = currPlayer.getChar();
	    	
		 	if (token == 'X') {
		 		Ellipse ellipse = new Ellipse(this.getWidth() / 2,
				 	    this.getHeight() / 2, this.getWidth() / 2 - 10,
				 	    this.getHeight() / 2 - 10);
		 	    ellipse.centerXProperty().bind(this.widthProperty().divide(2));
		 	    ellipse.centerYProperty().bind(this.heightProperty().divide(2));
			    ellipse.radiusXProperty().bind(this.widthProperty().divide(2).subtract(10));
		 	    ellipse.radiusYProperty().bind(this.heightProperty().divide(2).subtract(10));
			    ellipse.setStroke(Color.RED);
		 	    ellipse.setFill(Color.RED);
		 	    ellipse.setStyle("-fx-stroke-width:10px");
			    getChildren().add(ellipse); // Add the ellipse to the pane
		 	}
		 	else if (token == 'O') {
		 	    Ellipse ellipse = new Ellipse(this.getWidth() / 2,
		 	    this.getHeight() / 2, this.getWidth() / 2 - 10,
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
	     * Handles when a column is clicked. Checks for winner and swaps the control to opponent.
	     */
	    private void handleMouseClick() {
		 // If cell is empty and game is not over
		 	if (gb.isValidColumn(this.column)  && play) {
		 	    setTokenInGB(this.column, currPlayer.getChar()); // Set token in the cell
		 	    
		 	    if(!checkWinner(currPlayer.getChar()) && play) {
		 	    	currPlayer = currPlayer == playerX ? playerO : playerX;
		 	    	if(isPlayerOpponent && play)
		 	    		lblStatus.setText(currPlayer.getName() + "'s turn.");
		 	    	
		 	    	if(currPlayer.getClass() == Connect4ComputerPlayer.class) {
		 	    		this.getParent().getParent().setDisable(true);

		 	    		compCol = currPlayer.takeTurnGUI();
		 	    		
		 	    	    setTokenInGB(compCol, currPlayer.getChar());
		 	    	    if(!checkWinner(currPlayer.getChar()))
		 	    	    	lblStatus.setText("Computer(Yellow) chose col: " + 
		 	    	                         (compCol+1) +". Now, it is your(Red) turn.");
		 	    	    currPlayer = playerX;
		 	    	    this.getParent().getParent().setDisable(false);
		 	    	}
		 		}
		 	}else if(play) {
		 		lblStatus.setText("That column is full. Try again.");
		 	}
	    }
	}
}
