package src.snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;

    static final int UNIT_SIZE = 25; // how big are the objcs
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE; // how many units fit in scr

    static final int DELAY = 75; // budget dt
    // bodyparts
    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];

    int bodyParts = 6; // initial bodyparts
    int applesEaten = 0;
    int appleX, appleY;
    char direction = 'R'; // 'R'/'L'/'U'/'D'
    boolean running = false;
    Timer timer;
    Random random;

    GamePanel() {
        random = new Random();

        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        startGame();
    }

    public void startGame() {
        newApple();
        running = true;
        timer = new Timer(DELAY, this);// dictates how fast game runs
        timer.start();

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // draw grid
        for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
            g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
        }
        for (int i = 0; i < SCREEN_WIDTH / UNIT_SIZE; i++) {
            g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
        }
        // draw initial apple
        g.setColor(Color.red);
        g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

        // draw snake
        for (int i = 0; i < bodyParts; i++) {
            if (i == 0) { // draw head
                g.setColor(Color.green);
                g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
            } else { // draw body
                g.setColor(new Color(45, 180, 0));
                g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);

            }
        }
    }

    public void newApple() {
        // choose apple x,y randomly
        appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    public void move() {
        // moving the snake
        // cycle bodyparts
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
        // check direction and move
        switch (direction) {
            case 'U':
                y[0] -= UNIT_SIZE;
                break;
            case 'D':
                y[0] += UNIT_SIZE;
                break;
            case 'L':
                x[0] -= UNIT_SIZE;
                break;
            case 'R':
                x[0] += UNIT_SIZE;
                break;

            default:
                break;
        }
    }

    public void checkApple() {

    }

    public void checkCollisions() {
        // check colisions with bodyparts
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) { // heads collides with bodypart
                running = false;
            }
        }
        // check colisions with borders
        if (x[0] < 0) {
            running = false; // left
        }

        if (x[0] > SCREEN_WIDTH) {
            running = false; // right
        }
        if (y[0] < 0) {
            running = false; // top
        }

        if (y[0] > SCREEN_HEIGHT) {
            running = false; // bottom
        }
        // if the head of the snake collides, the game is over
    }

    public void gameOver(Graphics g) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    // input handler
    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                // check for inputs of the arrow keys
                // allow only 90deg turns
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;

                default:
                    break;
            }
        }
    }

}
