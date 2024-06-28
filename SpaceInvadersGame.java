import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SpaceInvadersGame extends Application {
    // Define size of window
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    // Define initial coordinates of ship
    private double shipX = WIDTH / 2.0 - 20;
    private double shipY = HEIGHT - 50;

    // Define speed of ship
    private double shipSpeed = 5;
    
    // Define speed of enemy
    private double enemySpeed = 2;

    // Define image path
    private final Image shipImage = new Image("image/ship.png");
    private final Image backgroundImage = new Image("image/background.png");
    
    // Define label for game score
    private Label scoreLabel;

    // Define initial game state
    private int score = 0;
    private boolean isPaused = false;
    private List<Bullet> bullets = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            if (!isPaused) {
                run(gc);
            } else {
                showPauseScreen(gc);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Create background image
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setFitWidth(WIDTH);
        backgroundImageView.setFitHeight(HEIGHT);

        // Create score label
        scoreLabel = new Label("Score: 0");
        scoreLabel.setTextFill(Color.WHITE); // Set text color
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Set font
        scoreLabel.setTranslateX(10); // Position X
        scoreLabel.setTranslateY(10); // Position Y

        StackPane root = new StackPane();
        root.getChildren().addAll(backgroundImageView, canvas, scoreLabel);
        StackPane.setAlignment(scoreLabel, Pos.TOP_LEFT); // Align score to top-left

        Scene scene = new Scene(root);
        
        scene.setOnKeyPressed(this::handleKeyPress);
        scene.setOnKeyReleased(this::handleKeyRelease);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Space Invaders");
        primaryStage.show();

        // Initialize enemies
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                enemies.add(new Enemy(50 + j * 60, 50 + i * 40));
            }
        }
    }

    private void run(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        // Draw ship
        gc.drawImage(shipImage, shipX, shipY, 80, 40);

        // Draw bullets
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update();
            if (bullet.getY() < 0) {
                bulletIterator.remove();
            } else {
                gc.drawImage(bullet.getBulletImage(), bullet.getX(), bullet.getY(), 10, 20);
            }
        }

        // Draw enemies
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.update();
            gc.drawImage(enemy.getEnemyImage(), enemy.getX(), enemy.getY(), 40, 20);
        }

        // Check for collisions
        bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
                if (bullet.intersects(enemy)) {
                    bulletIterator.remove();
                    enemyIterator.remove();
                    score += 1; // Increase score
                    updateScoreLabel(); // Update score display 
                    break;
                }
            }
        }
    }

    private void updateScoreLabel() {
        scoreLabel.setText("Score: " + score);
    }

    private void togglePause() {
        isPaused = !isPaused;
    }

    private void showPauseScreen(GraphicsContext gc) {
        gc.setFill(new Color(0, 0, 0, 0.5)); // Semi-transparent black
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        gc.fillText("PAUSED", WIDTH/2 - 100, HEIGHT/2);
        
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        gc.fillText("Press P to resume", WIDTH/2 - 80, HEIGHT/2 + 40);
    }

    private void resetScore() {
        score = 0;
        updateScoreLabel();
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.P) {
            togglePause();
            return;
        }

        if (!isPaused) {
            if (event.getCode() == KeyCode.LEFT) {
                shipX -= shipSpeed;
            } else if (event.getCode() == KeyCode.RIGHT) {
                shipX += shipSpeed;
            } else if (event.getCode() == KeyCode.SPACE) {
                bullets.add(new Bullet(shipX + 17.5, shipY));
            }
        }
    }

    private void handleKeyRelease(KeyEvent event) {
        if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.RIGHT) {
            shipSpeed = 5;
        }
    }

    class Bullet {
        private double x, y;
        private final Image bulletImage = new Image("image/bullet.png");

        public Bullet(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void update() {
            y -= 10;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public Image getBulletImage() {
            return bulletImage;
        }

        public boolean intersects(Enemy enemy) {
            return x < enemy.getX() + 40 && x + 5 > enemy.getX() && y < enemy.getY() + 20 && y + 10 > enemy.getY();
        }
    }

    class Enemy {
        private double x, y;
        private boolean movingRight = true;
        private final Image enemyImage = new Image("image/enemy.png");

        public Enemy(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void update() {
            if (movingRight) {
                x += enemySpeed;
                if (x > WIDTH - 40) {
                    movingRight = false;
                    y += 40;
                }
            } else {
                x -= enemySpeed;
                if (x < 0) {
                    movingRight = true;
                    y += 40;
                }
            }
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public Image getEnemyImage() {
            return enemyImage;
        }
    }
}
