package com.example.project_iteration_2_copy;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

//Extra Import for Start Screen
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;
import java.util.stream.Collectors;


//Move with A and D, shoot with SPACE
//TO DO:
//Error checking (try catch)
//Remove magic numbers
//Use in-line comments to indicate code blocks and describe decisions or complex expressions
// let the player know if they inputted a wrong input
//Fix enemy spacing when changing levels
// After GAME OVER, have an option to restart
//OPTIONAL?
//Start screen
//High score
//If alien collides with player, lose life
//"lose life" animation
//Use enums somewhere - player chooses difficulty and change enemy speed and lives number based on that

//CITATION: Almas Baimagambetov (almaslvl@gmail.com) (https://www.youtube.com/watch?v=FVo1fm52hz0) to help set up template
//IMAGES CITATION:
//"player" image: https://codeheir.com/2019/03/17/how-to-code-space-invaders-1978-7/
//"enemy" image: https://hero.fandom.com/wiki/Laser_Cannon_(Space_Invaders)
//"bullet" image: made ourselves
// "bg1":  https://wallpaperaccess.com/black-star-hd
// "title": https://static.wikia.nocookie.net/classics/images/a/a1/Space_Invaders_Logo.jpeg/revision/latest?cb=20210725054724


/**
 * Main game class for Space Invaders.
 */
public class SpaceInvadersApp extends Application {

    private boolean movingRight = true; //ChatGPT help
    boolean hitEdge = false;

    private int playerLevel=1;
    private int numRows;
    private int numCols;
    private int enemySpacing;
    private double enemySpeed = 1;
    private int playerScore=0;
    private boolean gameOver = false;

    // UI Text
    private Text levelText = new Text();
    private Text livesText = new Text();
    private Text scoreText=new Text();

    //CITATION: https://stackoverflow.com/questions/57066959/how-to-limit-key-input-while-the-key-is-held-in-javafx
    // Time threshold for key presses
    private static final long THRESHOLD = 100_000_000L; // 200 ms
    private long lastMoveNanos;

    // Player sprite
    private Sprite player = new Sprite(300, 750, 40, 40, "player", getClass().getResource("/images/player.png").toExternalForm()); // Player image

    private Pane root = new Pane();
    private double t = 0;

    //for start screen
    private Stage window;
    private Scene scene1, scene;


    /**
     * Initializes the text properties for the level, lives, and score.
     * */
    private void initializeTextProperties() {
        levelText.setFill(Color.WHITE);
        levelText.setTranslateX(10);
        levelText.setTranslateY(20);
        levelText.setFont(Font.font("Lucida Console", FontWeight.BOLD, FontPosture.REGULAR, 10));

        livesText.setFill(Color.WHITE);
        livesText.setTranslateX(80);
        livesText.setTranslateY(20);
        livesText.setFont(Font.font("Lucida Console", FontWeight.BOLD, FontPosture.REGULAR, 10));

        scoreText.setFill(Color.WHITE);
        scoreText.setTranslateX(150);
        scoreText.setTranslateY(20);
        scoreText.setFont(Font.font("Lucida Console", FontWeight.BOLD, FontPosture.REGULAR, 10));

        root.getChildren().addAll(levelText, livesText, scoreText);
    }

    /**
     * Creates the content for the game: player, enemies.
     * */
    private Parent createContent() {
        root.setPrefSize(600, 800);
        backgroundimage();
        root.getChildren().add(player);

        initializeTextProperties();

        // Timer to update game logic
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };

        timer.start();
        spawnEnemies(); // Initialize enemies
        return root;
    }

    /**
     * Get all sprites currently in the game.
     */
    private List<Sprite> sprites() {
        return root.getChildren().stream()
                .filter(n -> n instanceof Sprite)
                .map(n -> (Sprite) n)
                .collect(Collectors.toList());
    }

    /**
     * Update the game state (called every frame).
     */
    private void update() {
        if (gameOver) { //Ensures no game logic executed after game over condition
            return;
        }

        //Update UI text
        livesText.setText("LIVES: " + player.lives);
        levelText.setText("LEVEL: " + playerLevel);
        scoreText.setText("SCORE: " + playerScore);

        // Delta time  is the time between frames
        double deltaTime = 0.016;
        // Increase the time counter
        t += deltaTime;

        if (player.lives <= 0) {
            gameOver = true;
            showGameOver();
            return;
        }

        //ChatGPT: Handle enemy movement and screen edge detection
        boolean hitEdge = false;
        for (Sprite s : sprites().stream().filter(e -> e.type.equals("enemy")).collect(Collectors.toList())) {
            if (movingRight) { //If true enemies move right ( s.getTranslateX() + enemySpeed)
                s.setTranslateX(s.getTranslateX() + enemySpeed);
                if (s.getTranslateX() + s.getFitWidth() >= root.getPrefWidth()) { //If reach edge of screen, hitEdge true, direction of movement reversed.
                    hitEdge = true;
                }
            } else {
                s.setTranslateX(s.getTranslateX() - enemySpeed);
                if (s.getTranslateX() <= 0) {
                    hitEdge = true;
                }
            }
            if (s.getTranslateY() + s.getFitHeight()>=root.getPrefHeight()){
                gameOver = true;
                showGameOver();
                return;
            }
        }

        if (hitEdge) { //When enemies hit edge of screen, drop 20 pixels, reverse direction
            for (Sprite s : sprites().stream().filter(e -> e.type.equals("enemy")).collect(Collectors.toList())) {
                s.setTranslateY(s.getTranslateY() + 20);
            }
            movingRight = !movingRight;
        }

        sprites().forEach(s -> {
            switch (s.type) {

                case "enemybullet": //Move downward, check for collision with player
                    s.moveDown();

                    if (s.getBoundsInParent().intersects(player.getBoundsInParent())) {
                        player.lives--;
                        playerScore--;
                        if (player.lives <= 0) {
                            player.dead = true;
                        }
                        s.dead = true;
                    }
                    break;

                case "playerbullet": //Move upward and check for collision with enemy
                    s.moveUp();

                    sprites().stream().filter(e -> e.type.equals("enemy")).forEach(enemy -> {
                        if (s.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                            playerScore++;
                            enemy.dead = true;
                            s.dead = true;
                        }
                    });

                    break;

                case "enemy": //Every 2 seconds, enemy has 30% chance to shoot

                    if (t > 2) {
                        if (Math.random() < 0.3) {
                            shoot(s);
                        }
                    }

                    break;
            }
        });

        //Remove any sprite marked as dead
        root.getChildren().removeIf(n -> {
            if (n instanceof Sprite) {
                Sprite s = (Sprite) n;
                return s.dead;
            }
            return false;
        });

        //Reset time counter
        if (t > 2) {
            t = 0;
        }

        //Check if player met level up conditions
        checkLevelUp();
    }

    /**
     * Display "Game Over" screen and restart option.
     *
     * ChatGPT help
     */

    private void showGameOver() {
        // Game over
        Text gameOverText = new Text("GAME OVER\n" );
        gameOverText.setFill(Color.RED);
        gameOverText.setTextAlignment(TextAlignment.CENTER);
        gameOverText.setTranslateX(root.getPrefWidth() / 2 - 150);
        gameOverText.setTranslateY(root.getPrefHeight() / 2 - 30);
        gameOverText.setFont(Font.font("Lucida Console", FontWeight.BOLD, FontPosture.REGULAR, 50));
        root.getChildren().add(gameOverText);

        Text scoreText = new Text("SCORE:"+ playerScore);
        scoreText.setFill(Color.RED);
        scoreText.setTextAlignment(TextAlignment.CENTER);
        scoreText.setTranslateX(root.getPrefWidth() / 2 - 90);
        scoreText.setTranslateY(root.getPrefHeight() / 2 + 40 );
        scoreText.setFont(Font.font("Lucida Console", FontWeight.BOLD, FontPosture.REGULAR, 30));
        root.getChildren().add(scoreText);


        // Restart Button
        Button restartButton = new Button("Restart");
        restartButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: Red; -fx-font-size:30;");

        restartButton.setTranslateX(root.getPrefWidth() / 2 - 150);
        restartButton.setTranslateY(root.getPrefHeight() / 2 + 60);
        restartButton.setOnAction(e -> {
            // Reset game state
            gameOver = false;
            playerLevel = 1;
            playerScore = 0;
            player.lives = 3;
            player.setTranslateX(300); // Reset to initial position
            player.setTranslateY(750); // Reset to initial position
            player.dead = false;

            // Clear root and reinitialize
            root.getChildren().clear();
            backgroundimage();
            root.getChildren().add(player); // re-add player
            initializeTextProperties();
            spawnEnemies();
        });

        // Quit Button
        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: Red; -fx-font-size:30;");
        quitButton.setTranslateX(root.getPrefWidth() / 2 + 30);
        quitButton.setTranslateY(root.getPrefHeight() / 2 + 60);
        quitButton.setOnAction(e -> {
            // Exit the application
            Stage stage = (Stage) quitButton.getScene().getWindow();
            stage.close();
        });

        root.getChildren().addAll(restartButton,quitButton);
    }

    /**
     * Creates/adds bullet sprite when shot by player or enemy
     */
    private void shoot(Sprite who) {
        Sprite s = new Sprite((int) who.getTranslateX() + 20, (int) who.getTranslateY(), 5, 20, who.type + "bullet", getClass().getResource("/images/bullet.png").toExternalForm());

        root.getChildren().add(s);
    }

    /**
     * Called to spawn enemies
     */
    private void spawnEnemies(){ //ChatGPT help
        numCols = 5;
        enemySpacing = 90-(playerLevel*2);
        numRows = playerLevel * 2;
        root.getChildren().removeIf(node->node instanceof Sprite &&((Sprite)node).type.equals("enemy")); //Removes existing enemies

        for (int row = 0; row < numRows; row++) { //Loops to spawn enemies in rows/columns
            for (int col = 0; col < numCols; col++) {
                int x = 100 + col * enemySpacing;
                int y = 50 + row * enemySpacing;
                Sprite s = new Sprite(x,y, 30, 30, "enemy",  getClass().getResource("/images/alien.png").toExternalForm()); //Creates new Sprite object for each enemy
                root.getChildren().add(s); //Add enemy sprite to root container
            }
        }
    }

    /**
     * If level up is triggered, the enemies' speed increases and new enemies are spawned.
     */
    private void levelUp() {
        playerLevel++;
        enemySpeed += 0.2;
        spawnEnemies();
    }

    /**
     * Determines if all enemies are defeated, and if so, changes the level.
     */
    private void checkLevelUp() {
        if (allEnemiesDefeated()){
            levelUp();
        }
    }

    /**
     * Determines if player has defeated all enemies.
     * If any enemy sprite is still alive (dead=false), it returns false (enemies still alive.)
     * ChatGPT help
     */
    private boolean allEnemiesDefeated() {
        for (Node node : root.getChildren()) { //Loop iterates over all nodes in root
            if (node instanceof Sprite) { //Checks if node is Sprite
                Sprite sprite = (Sprite) node;
                if (sprite.type.equals("enemy") && !sprite.dead) { //Checks if Sprite is an enemy and if its dead property is false
                    return false;
                }
            }
        }
        return true;
    }

    /**
     *  start screen
     *
     * */
    public void start(Stage stage) throws Exception {
        window = stage;

        // Load Background Image
        Image image = new Image(getClass().getResource("/images/bg1.png").toExternalForm());
        ImageView imageView = new ImageView(image);

        // Adjust ImageView Properties
        imageView.setFitWidth(600);  // Match scene width
        imageView.setFitHeight(800); // Match scene height
        imageView.setPreserveRatio(false);

        // Create StackPane for Layout
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(imageView); // Add the background image

        // Add title pic
        Image titleImage = new Image(getClass().getResource("/images/title.png").toExternalForm());
        ImageView titleImageView = new ImageView(titleImage);
        titleImageView.setFitWidth(400);       // Adjust size of title image
        titleImageView.setPreserveRatio(true); // Maintain aspect ratio

        // Position the title image higher on the screen
        titleImageView.setTranslateX(root.getPrefWidth() / 2 );  // make it in Center
        titleImageView.setTranslateY(-100);


        // Start Button
        Button button1 = new Button("PRESS START");
        button1.setFont(Font.font("Kanit", FontWeight.BOLD, 30));

        // Remove background and border, leaving only the text
        // Citation: https://www.demo2s.com/g/java/how-to-set-button-style-in-javafx.html
        button1.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-text-fill: Red; -fx-font-size:30;");

        button1.setOnAction(e -> {
            root = new Pane();
            scene = new Scene(createContent());
            scene.setOnKeyPressed(this::handleKeyPress);
            window.setScene(scene);
        });


        VBox layout1 = new VBox(20, titleImageView, button1); //layout that stacks eveything on each other
        layout1.setAlignment(Pos.CENTER);
        layout1.setStyle("-fx-background-color: transparent;");

        stackPane.getChildren().add(layout1);

        scene1 = new Scene(stackPane, 600, 800);

        window.setScene(scene1);
        window.setTitle("Space Invaders");
        window.show();
    }


    private void handleKeyPress(KeyEvent e) {
        //CITATION: https://stackoverflow.com/questions/57066959/how-to-limit-key-input-while-the-key-is-held-in-javafx
        long now = System.nanoTime();
        if (lastMoveNanos <= 0L || now - lastMoveNanos >= THRESHOLD) {
            switch (e.getCode()) {
                case A:
                    player.moveLeft();
                    break;
                case W:
                    player.moveUp();
                    break;
                case D:
                    player.moveRight();
                    break;
                case S:
                    player.moveDown();
                    break;
                case SPACE:
                    shoot(player);
                    break;
            }
            lastMoveNanos = now;
        }
    }

    /***
     *  Sprite class：
     *  This class represents a sprite in the game.
     *  A sprite can be any entity in the game, such as the player, enemies, or bullets.
     *  It contains methods to move the sprite around the screen and handle various game mechanics like collisions.
     */
    private static class Sprite extends ImageView {
        boolean dead = false;
        int lives = 3;
        final String type;
        private double playerSpeed = 20; // To control the player speed.

        //Constructor for new Sprite object
        Sprite(int x, int y, int w, int h, String type, String imagePath) {
            super(new Image(imagePath));
            this.type = type; //Type of sprite (ex player, enemy, bullet)
            //Setters for coordinates, size:
            setTranslateX(x);
            setTranslateY(y);
            setFitWidth(w);
            setFitHeight(h);
        }

        //Methods to change sprite position:
        void moveLeft() { if (getTranslateX()-playerSpeed>0) {
            setTranslateX(getTranslateX() - playerSpeed); //Ensures player can't go off left edge
        }
        }

        void moveRight() { if (getTranslateX()+playerSpeed<600-40) {
            setTranslateX(getTranslateX() + playerSpeed);
        } //Ensures player can't go off right edge
        }

        void moveUp() { setTranslateY(getTranslateY() - playerSpeed);
        }

        void moveDown() {
            setTranslateY(getTranslateY() + playerSpeed);
        }
    }

    /**
     * method of background image
     * */
    private void backgroundimage() {
        // Load Background Image
        Image image = new Image(getClass().getResource("/images/bg1.png").toExternalForm());
        ImageView backgroundImageView = new ImageView(image);

        // Set the image to fit the scene size
        backgroundImageView.setFitWidth(600);  // Match scene width
        backgroundImageView.setFitHeight(800); // Match scene height
        backgroundImageView.setPreserveRatio(false); // Allow the image to stretch

        // Add background image to the root (or any container in your scene)
        root.getChildren().add(backgroundImageView);
    }

    public static void main(String[] args) {
        launch(args);
    }
}