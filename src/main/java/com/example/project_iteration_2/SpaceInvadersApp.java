package com.example.project_iteration_2;

//Move with A and D, shoot with SPACE

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

import java.util.List;
import java.util.stream.Collectors;

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
    private Text levelText = new Text();
    private Text livesText = new Text();
    private Text scoreText=new Text();
    //CITATION: https://stackoverflow.com/questions/57066959/how-to-limit-key-input-while-the-key-is-held-in-javafx
    private static final long THRESHOLD = 100_000_000L; // 200 ms
    private long lastMoveNanos;

    private Sprite player = new Sprite(300, 750, 40, 40, "player", "file:src\\main\\resources\\images\\player.png"); // Player image

    private Pane root = new Pane();

    private double t = 0;

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

    private Parent createContent() {
        root.setPrefSize(600, 800);

        root.getChildren().add(player);

        initializeTextProperties();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };


        timer.start();
        spawnEnemies();


        return root;
    }

    private List<Sprite> sprites() {
        return root.getChildren().stream()
                .filter(n -> n instanceof Sprite)
                .map(n -> (Sprite) n)
                .collect(Collectors.toList());
    }

    private void update() {
        if (gameOver) {
            return;
        }

        livesText.setText("LIVES: " + player.lives);
        levelText.setText("LEVEL: " + playerLevel);
        scoreText.setText("SCORE: " + playerScore);

        t += 0.016;

        if (player.lives <= 0) {
            gameOver = true;
            showGameOver();
            return;
        }

        //ChatGPT
        boolean hitEdge = false;
        for (Sprite s : sprites().stream().filter(e -> e.type.equals("enemy")).collect(Collectors.toList())) {
            if (movingRight) {
                s.setTranslateX(s.getTranslateX() + enemySpeed);
                if (s.getTranslateX() + s.getFitWidth() >= root.getPrefWidth()) {
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

        if (hitEdge) {
            for (Sprite s : sprites().stream().filter(e -> e.type.equals("enemy")).collect(Collectors.toList())) {
                s.setTranslateY(s.getTranslateY() + 20);
            }
            movingRight = !movingRight;
        }

        sprites().forEach(s -> {
            switch (s.type) {

                case "enemybullet":
                    s.moveDown();

                    if (s.getBoundsInParent().intersects(player.getBoundsInParent())) {
                        player.lives--;
                        playerScore--;
                        //TO DO call on a method for kill animation
                        if (player.lives <= 0) {
                            player.dead = true;
                        }
                        s.dead = true;
                    }
                    break;

                case "playerbullet":
                    s.moveUp();

                    sprites().stream().filter(e -> e.type.equals("enemy")).forEach(enemy -> {
                        if (s.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                            playerScore++;
                            enemy.dead = true;
                            s.dead = true;
                        }
                    });

                    break;

                case "enemy":

                    if (t > 2) {
                        if (Math.random() < 0.3) {
                            shoot(s);
                        }
                    }

                    break;
            }
        });

        root.getChildren().removeIf(n -> {
            if (n instanceof Sprite) {
                Sprite s = (Sprite) n;
                return s.dead;
            }
            return false;
        });

        if (t > 2) {
            t = 0;
        }

        checkLevelUp();
    }

    //ChatGPT help
    private void showGameOver() {
        Text gameOverText = new Text("GAME OVER");
        gameOverText.setFill(Color.RED);
        gameOverText.setTextAlignment(TextAlignment.CENTER);
        gameOverText.setTranslateX(root.getPrefWidth() / 2 -135);
        gameOverText.setTranslateY(root.getPrefHeight() / 2);
        gameOverText.setFont(Font.font("Lucida Console", FontWeight.BOLD, FontPosture.REGULAR, 50));
        root.getChildren().add(gameOverText);
    }

    private void shoot(Sprite who) {
        Sprite s = new Sprite((int) who.getTranslateX() + 20, (int) who.getTranslateY(), 5, 20, who.type + "bullet", "file:src\\main\\resources\\images\\bullet.png");

        root.getChildren().add(s);
    }

    private void spawnEnemies(){ //ChatGPT help
        numCols = 5;
        enemySpacing = 90-(playerLevel*2);
        numRows = playerLevel * 2;
        root.getChildren().removeIf(node->node instanceof Sprite &&((Sprite)node).type.equals("enemy"));

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                int x = 100 + col * enemySpacing;
                int y = 50 + row * enemySpacing;
                Sprite s = new Sprite(x,y, 30, 30, "enemy", "file:src\\main\\resources\\images\\alien.png");
                root.getChildren().add(s);
            }
        }
    }

    private void levelUp() {
        playerLevel++;
        enemySpeed += 0.2;
        spawnEnemies();
    }

    private void checkLevelUp() {
        if (allEnemiesDefeated()){
            levelUp();
        }
    }

    //ChatGPT
    private boolean allEnemiesDefeated() {
        for (Node node : root.getChildren()) {
            if (node instanceof Sprite) {
                Sprite sprite = (Sprite) node;
                if (sprite.type.equals("enemy") && !sprite.dead) {
                    return false;
                }
            }
        }
        return true;
    }

    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
        scene.setFill(Color.BLACK);

        try {
            scene.setOnKeyPressed(e -> {
                //CITATION: https://stackoverflow.com/questions/57066959/how-to-limit-key-input-while-the-key-is-held-in-javafx
                long now = System.nanoTime();
                if (lastMoveNanos <= 0L || now - lastMoveNanos >= THRESHOLD) {
                    switch (e.getCode()) {
                        case A:
                            player.moveLeft();
                            break;
                        case D:
                            player.moveRight();
                            break;
                        case SPACE:
                            shoot(player);
                            break;
                    }
                    lastMoveNanos = now;
                }
            });
            stage.setResizable(false);
            stage.setTitle("Space Invaders");
            stage.setScene(scene);
            stage.show();
        } catch (Exception exception){
            System.out.println("Something went wrong.");
        }
    }

    private static class Sprite extends ImageView {
        boolean dead = false;
        int lives = 3;
        final String type;

        Sprite(int x, int y, int w, int h, String type, String imagePath) {
            super(new Image(imagePath));

            this.type = type;
            setTranslateX(x);
            setTranslateY(y);
            setFitWidth(w);
            setFitHeight(h);
        }

        void moveLeft() { if (getTranslateX()-5>0) {
            setTranslateX(getTranslateX() - 5);
        }
        }

        void moveRight() { if (getTranslateX()+5<600-40) {
            setTranslateX(getTranslateX() + 5);
        }
        }

        void moveUp() { setTranslateY(getTranslateY() - 5);
        }

        void moveDown() {
            setTranslateY(getTranslateY() + 5);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}