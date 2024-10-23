import config.WindowConfig;

import javax.swing.*;

public class Main {

    static final int BOARD_WIDTH = WindowConfig.BOARD_WIDTH;
    static final int BOARD_HEIGHT = WindowConfig.BOARD_HEIGHT;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        frame.setSize(BOARD_WIDTH, BOARD_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        FlappyBird flappyBird = new FlappyBird();
        frame.add(flappyBird);
        frame.pack();
        flappyBird.requestFocus();
        frame.setVisible(true);
    }
}