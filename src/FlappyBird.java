import config.GameConfig;
import config.ImagePaths;
import config.WindowConfig;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;
import java.util.*;
import java.util.List;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    final int BOARD_WIDTH = WindowConfig.BOARD_WIDTH;
    final int BOARD_HEIGHT = WindowConfig.BOARD_HEIGHT;
    String BACKGROUND_IMG_PATH = ImagePaths.BACKGROUND_IMG_PATH;
    String FLAPPY_BIRD_IMG_PATH = ImagePaths.FLAPPY_BIRD_IMG_PATH;
    String TOP_PIPE_IMG_PATH = ImagePaths.TOP_PIPE_IMG_PATH;
    String BOTTOM_PIPE_IMG_PATH = ImagePaths.BOTTOM_PIPE_IMG_PATH;

    // Image variables to hold the images of the game
    Image backgroundImage;
    Image birdImage;
    Image topPipeImage;
    Image bottomPipeImage;

    // Bird
    int birdX = BOARD_WIDTH/8;
    int birdY = BOARD_HEIGHT/2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image image;

        Bird(Image image) {
            this.image = image;
        }
    }

    // Pipes
    int pipeX = BOARD_WIDTH;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        boolean passed = false;
        Image image;

        Pipe(Image image) {
            this.image = image;
        }
    }

    // Game logic
    Bird bird;
    int velocityX = -4; // The speed at which the pipes move to the left (simulates the bird moving to the right)
    int velocityY = 0; // The speed at which the bird moves upward. It changes when pressing space bar
    int gravity = 1; // The speed at which the bird falls down to the ground. It's added to the velocity each frame

    List<Pipe> pipes;
    Random random = new Random();
    Timer gameLoop;
    Timer placePipesTimer;
    boolean isGameOver = false;
    double score = 0.0;

    public FlappyBird() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        // Loading the image resources
        backgroundImage = new ImageIcon(Objects.requireNonNull(getClass().getResource(BACKGROUND_IMG_PATH))).getImage();
        birdImage = new ImageIcon(Objects.requireNonNull(getClass().getResource(FLAPPY_BIRD_IMG_PATH))).getImage();
        topPipeImage = new ImageIcon(Objects.requireNonNull(getClass().getResource(TOP_PIPE_IMG_PATH))).getImage();
        bottomPipeImage = new ImageIcon(Objects.requireNonNull(getClass().getResource(BOTTOM_PIPE_IMG_PATH))).getImage();

        // Instantiating the bird
        bird = new Bird(birdImage);

        // Instantiating the pipes list
        pipes = new ArrayList<>();

        // Instantiating the timer to place the pipes
        placePipesTimer = new Timer(GameConfig.PIPE_INSTANTIATION_TIMER_DELAY, e -> placePipes());
        placePipesTimer.start();

        // Instantiating the game loop
        gameLoop = new Timer(GameConfig.MILLISECONDS_DELAY_BETWEEN_FRAMES, this); // 1000/60 = 16.6
        gameLoop.start();
    }

    private void placePipes() {
        // random -> {0.0 - 1.0}
        // 0 - 128 - (0-256) --> {1/4 pipeHeight - 3/4 pipeHeight}
        int randomPipeY = (int) (pipeY - (double) pipeHeight/4 - Math.random()*((double) pipeHeight/2));
        int openingSpace = BOARD_HEIGHT/6; // The space between each pipe so that the bird can fly through

        Pipe topPipe = new Pipe(topPipeImage);
        topPipe.y = randomPipeY;

        Pipe bottomPipe = new Pipe(bottomPipeImage);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        
        pipes.add(topPipe);
        pipes.add(bottomPipe);

        System.out.println(pipes);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        // Drawing the background image
        g.drawImage(backgroundImage, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, null);

        // Drawing the bird
        g.drawImage(bird.image, bird.x, bird.y, bird.width, bird.height, null);

        // Drawing the pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.image, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Displaying the score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 32));

        if (isGameOver) {
            g.drawString("Game Over!", 10, 35);
            g.drawString("Score: " + (int) score, 10, 70);
            g.drawString("Press Enter to restart", BOARD_WIDTH/12, BOARD_HEIGHT - 35);
        }
        else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    private void move() {
        // Moving the bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        // Moving the pipes
        Iterator<Pipe> it = pipes.iterator();
        while (it.hasNext()) {
            Pipe pipe = it.next();
            pipe.x += velocityX;

            // Checking if the bird has passed this pipe and adding up the score
            if (!pipe.passed && pipe.x + pipe.width < bird.x) {
                pipe.passed = true;
                score += 0.5; // Since there are always two pipes, each one will sum 0.5 when it's passed, so 1 for each pair of pipes
            }

            // Checking if the pipe has exited the screen to remove it from the list of pipes
            if (pipe.x + pipe.width < 0) {
                it.remove();
            }

            // Checking if the bird has collided with this pipe
            if (hasCollisionOccurred(bird, pipe)) {
                isGameOver = true;
            }
        }

        if (bird.y > BOARD_HEIGHT) {
            isGameOver = true;
        }
    }

    private boolean hasCollisionOccurred(Bird bird, Pipe pipe) {
        return  bird.x < pipe.x + pipe.width &&
                bird.x + bird.width > pipe.x &&
                bird.y < pipe.y + pipe.height &&
                bird.y + bird.height > pipe.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (isGameOver) {
            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (isGameOver) {
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0.0;
                isGameOver = false;
                gameLoop.start();
                placePipesTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

}
