import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class Minesweeper extends Application{
    
    Stage stage;
    Scene welcomeScene, minesweeperScene;
    String unrevealedStyle = "-fx-background-color: black; -fx-text-fill: transparent;";
    String revealedStyle = "";
    String flagStyle  = "-fx-background-color: red; -fx-text-fill: transparent;";
    Button[][] buttons;
    
    Label flagsHUD;
    Button resetButton;
    Label Timer;
    
    Timeline timeline;
    
    public static void main(String[] args) {
        Board.setBoard();
        launch(args);    
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
    stage = primaryStage;
    stage.setTitle("Minesweeper");
    
    setWelcomeScene();
    
    stage.setResizable(false);
    stage.setScene(welcomeScene);
    stage.show();
    }
    
    public void setWelcomeScene(){

        VBox menu = new VBox();
        Label title = new Label("Minesweeper");
        Button play = new Button("Play");
        play.setOnAction(e -> {    
            setMinesweeperScene();
            stage.setScene(minesweeperScene);
        });
        Button seeScores = new Button("See Highscores");
        Button quit = new Button("Exit Game");
        quit.setOnAction(e -> stage.close());
        
        menu.getChildren().addAll(title, play, seeScores, quit);
        welcomeScene = new Scene(menu); 
        
    }

    public void setMinesweeperScene(){
        long startTime = System.currentTimeMillis();

        HBox menu = new HBox(10);
        menu.setPadding(new Insets(20, 20, 5, 20));
	menu.setSpacing(30);
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setHgap(2);
        grid.setVgap(2);


        buttons = new Button[Board.horizontalTiles][Board.verticalTiles];

        for (int x = 0; x < Board.horizontalTiles; x++) {
            for (int y = 0; y < Board.verticalTiles; y++) {
                Button button = new Button();
                buttons[x][y] = button;
                grid.add(button, x, y);


                button.setMinWidth(30);
                button.setMinHeight(30);
                String index = String.valueOf(Board.mineField[x][y]);
                button.setText(index);

                button.setStyle(unrevealedStyle);

                final int xx = x;
                final int yy = y;
                button.setOnAction(e -> reveal(button.getText(), xx, yy));
                button.setOnMouseClicked((MouseEvent event) -> {
	        MouseButton button1 = event.getButton();

                    if (button1 == MouseButton.PRIMARY) {
                        reveal(button.getText(), xx, yy);
                    } else if (button1 == MouseButton.SECONDARY) {
                        setFlag(xx, yy);
                    }
                });
            }
        }
        
        resetButton = new Button("Back to Menu"); 
        
        flagsHUD = new Label("Flags:\n   " + String.valueOf(Board.flags));
        
        Timer = new Label("Time: \n   " + String.valueOf(0) + " : "+ String.valueOf(0));
	
        
	resetButton.setOnAction(e -> {
            setWelcomeScene();
            stage.setScene(welcomeScene);
        });

        menu.getChildren().addAll(resetButton, flagsHUD, Timer);
        
        timeline = new Timeline(new KeyFrame(
        Duration.millis(1000),
            ae -> displaySeconds(startTime)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        

        BorderPane border = new BorderPane();
        border.setCenter(grid);
        border.setTop(menu);

        minesweeperScene = new Scene(border);
 
    }
    
    public void setFlag(int hIndex, int vIndex){	
		if(Board.flagsMap[hIndex][vIndex] == 0 && Board.flags <= Board.totalMines && Board.flags > 0){
			Board.flagsMap[hIndex][vIndex] = 1;	
			buttons[hIndex][vIndex].setStyle(flagStyle);
			Board.flags -= 1;
		}
		else if(Board.flagsMap[hIndex][vIndex] == 1){
			Board.flagsMap[hIndex][vIndex] = 0;	
			buttons[hIndex][vIndex].setStyle(unrevealedStyle);
			Board.flags += 1;
		}
                
	flagsHUD.setText("Flags:\n   " + String.valueOf(Board.flags));
    }   

    public void reveal(String text, int hIndex, int vIndex){
        
        if(Board.flagsMap[hIndex][vIndex] != 1){
            if (!"-1".equals(text)){

                buttons[hIndex][vIndex].setStyle(revealedStyle);
                buttons[hIndex][vIndex].setDisable(true);

                if("0".equals(text)){

                    buttons[hIndex][vIndex].setText(" ");

                    for (int h = hIndex - 1; h <= hIndex + 1; h++) {
                        for (int v = vIndex - 1; v <= vIndex + 1; v++) {

                            boolean inBoundsX = (h >= 0) && (h < Board.horizontalTiles);
                            boolean inBoundsY = (v >= 0) && (v < Board.verticalTiles);

                            if(inBoundsX && inBoundsY){
                                buttons[h][v].fire();
                            }

                        }
                    }
                }

                checkVictory();

            }else{
                lost();
            }
        }
    }
    
    public void checkVictory(){
        int revealedOnes = 0;
        
        for (int x = 0; x < Board.horizontalTiles; x++) {
            for (int y = 0; y < Board.verticalTiles; y++) {
                if (buttons[x][y].getStyle().equals(revealedStyle)){
                    revealedOnes += 1;
                }
            }
        }
        
        if(Board.totalMines == (Board.horizontalTiles * Board.verticalTiles - revealedOnes)){
            win();
        }
    }
    
    public void lost(){
        for (int x = 0; x < Board.horizontalTiles; x++) {
            for (int y = 0; y < Board.verticalTiles; y++) {
                buttons[x][y].setStyle(revealedStyle);
                buttons[x][y].setDisable(true);
                
                if("-1".equals(buttons[x][y].getText())){
                    Image image = new Image(getClass().getResourceAsStream("Mine.png"));
                    buttons[x][y].setText("");
                    buttons[x][y].setGraphic(new ImageView(image));
                }
                if("0".equals(buttons[x][y].getText())){
                    buttons[x][y].setText("");
                }
            }
	resetButton.setText("Try Again!");
        }
  
    }
    
    public void win(){
        System.out.println("Vc venceu!");
    }
    
    public void displaySeconds(long startTime){
                long elapsedTime = System.currentTimeMillis() - startTime;
                long elapsedSeconds = elapsedTime / 1000;
                long secondsDisplay = elapsedSeconds % 60;
                long elapsedMinutes = elapsedSeconds / 60;
        
                Timer.setText("Time: \n " + String.valueOf(elapsedMinutes) + " : "+ String.valueOf(secondsDisplay));          
    }
     
}
