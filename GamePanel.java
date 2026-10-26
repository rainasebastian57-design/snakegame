import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

    // Authentic Nokia LCD Palette
    static final Color NOKIA_BG = new Color(155, 188, 15);       // Background screen green
    static final Color BORDER_COLOR = new Color(15, 56, 15);     // Dark screen frame
    static final Color SNAKE_HEAD = new Color(20, 60, 20);       // Darker pixel head
    static final Color SNAKE_BODY = new Color(43, 85, 43);       // Classic olive body segments
    static final Color APPLE_COLOR = new Color(75, 30, 30);      // Deep retro food color

    enum State { MENU, DIFFICULTY_SELECT, PLAYING, PAUSED, GAME_OVER }
    State currentState = State.MENU;

    int delay = 80;
    String difficultyName = "MEDIUM";
    int selectedMenuOption = 0;

    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten = 0;
    int appleX;
    int appleY;
    char direction = 'R';
    boolean canChangeDirection = true;

    Timer timer;
    Random random;

    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(NOKIA_BG);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        timer = new Timer(delay, this);
    }

    public void startNewGame() {
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        
        for (int i = 0; i < bodyParts; i++) {
            x[i] = 125 - (i * UNIT_SIZE);
            y[i] = 125;
        }
        
        newApple();
        timer.setDelay(delay);
        currentState = State.PLAYING;
        timer.start();
    }

    public void newApple() {
        appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
        appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Draw Retro LCD Border Frame
        g.setColor(BORDER_COLOR);
        g.drawRect(10, 10, SCREEN_WIDTH - 20, SCREEN_HEIGHT - 20);
        g.drawRect(11, 11, SCREEN_WIDTH - 22, SCREEN_HEIGHT - 22);

        switch (currentState) {
            case MENU:
                drawMenu(g);
                break;
            case DIFFICULTY_SELECT:
                drawDifficultySelect(g);
                break;
            case PLAYING:
            case PAUSED:
                drawGameplay(g);
                if (currentState == State.PAUSED) {
                    drawCenteredText(g, "PAUSED", 40, SCREEN_HEIGHT / 2);
                    drawCenteredText(g, "Press SPACE to Resume", 18, SCREEN_HEIGHT / 2 + 40);
                }
                break;
            case GAME_OVER:
                drawGameplay(g);
                drawGameOver(g);
                break;
        }
    }

    private void drawMenu(Graphics g) {
        g.setColor(BORDER_COLOR);
        g.setFont(new Font("Monospaced", Font.BOLD, 45));
        drawCenteredText(g, "NOKIA SNAKE", 45, 140);

        g.setFont(new Font("Monospaced", Font.BOLD, 26));
        String[] options = {
            "1. NEW GAME",
            "2. CONTINUE",
            "3. DIFFICULTY: " + difficultyName
        };

        for (int i = 0; i < options.length; i++) {
            if (i == selectedMenuOption) {
                g.drawString("> " + options[i], 110, 260 + (i * 60));
            } else {
                g.drawString("  " + options[i], 110, 260 + (i * 60));
            }
        }

        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        drawCenteredText(g, "Use UP/DOWN & ENTER to Select", 16, 520);
    }

    private void drawDifficultySelect(Graphics g) {
        g.setColor(BORDER_COLOR);
        g.setFont(new Font("Monospaced", Font.BOLD, 36));
        drawCenteredText(g, "SELECT DIFFICULTY", 36, 160);

        g.setFont(new Font("Monospaced", Font.BOLD, 26));
        String[] diffs = { "EASY", "MEDIUM", "HARD" };

        for (int i = 0; i < diffs.length; i++) {
            if (i == selectedMenuOption) {
                g.drawString("> " + diffs[i], 210, 270 + (i * 60));
            } else {
                g.drawString("  " + diffs[i], 210, 270 + (i * 60));
            }
        }
    }

    private void drawGameplay(Graphics g) {
        // Draw Retro Apple (with inner pixel shading)
        g.setColor(APPLE_COLOR);
        g.fillRect(appleX + 3, appleY + 3, UNIT_SIZE - 6, UNIT_SIZE - 6);
        g.setColor(BORDER_COLOR);
        g.drawRect(appleX + 3, appleY + 3, UNIT_SIZE - 6, UNIT_SIZE - 6);

        // Draw Snake
        for (int i = 0; i < bodyParts; i++) {
            if (i == 0) {
                // Snake Head: Distinct darker look with a tiny inner border for pixel style
                g.setColor(SNAKE_HEAD);
                g.fillRect(x[i] + 1, y[i] + 1, UNIT_SIZE - 2, UNIT_SIZE - 2);
                g.setColor(BORDER_COLOR);
                g.drawRect(x[i] + 1, y[i] + 1, UNIT_SIZE - 2, UNIT_SIZE - 2);
            } else {
                // Snake Body: Segmented classic look with margins
                g.setColor(SNAKE_BODY);
                g.fillRect(x[i] + 2, y[i] + 2, UNIT_SIZE - 4, UNIT_SIZE - 4);
            }
        }

        // Draw Score
        g.setColor(BORDER_COLOR);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.drawString("SCORE: " + applesEaten, 25, 35);
    }

    private void drawGameOver(Graphics g) {
        // Retro popup box
        g.setColor(NOKIA_BG);
        g.fillRect(80, 180, SCREEN_WIDTH - 160, 240);
        g.setColor(BORDER_COLOR);
        g.drawRect(80, 180, SCREEN_WIDTH - 160, 240);
        g.drawRect(82, 182, SCREEN_WIDTH - 164, 236);

        g.setFont(new Font("Monospaced", Font.BOLD, 42));
        drawCenteredText(g, "GAME OVER", 42, 250);

        g.setFont(new Font("Monospaced", Font.BOLD, 24));
        drawCenteredText(g, "SCORE: " + applesEaten, 24, 310);

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        drawCenteredText(g, "Press ENTER for Menu", 18, 370);
    }

    private void drawCenteredText(Graphics g, String text, int size, int yPos) {
        g.setFont(new Font("Monospaced", Font.BOLD, size));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString(text, (SCREEN_WIDTH - metrics.stringWidth(text)) / 2, yPos);
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U': y[0] -= UNIT_SIZE; break;
            case 'D': y[0] += UNIT_SIZE; break;
            case 'L': x[0] -= UNIT_SIZE; break;
            case 'R': x[0] += UNIT_SIZE; break;
        }
        canChangeDirection = true;
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                currentState = State.GAME_OVER;
            }
        }
        if (x[0] < 10 || x[0] >= SCREEN_WIDTH - 20 || y[0] < 10 || y[0] >= SCREEN_HEIGHT - 20) {
            currentState = State.GAME_OVER;
        }

        if (currentState == State.GAME_OVER) {
            timer.stop();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentState == State.PLAYING) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (currentState == State.MENU) {
                if (key == KeyEvent.VK_UP) {
                    selectedMenuOption = (selectedMenuOption - 1 + 3) % 3;
                } else if (key == KeyEvent.VK_DOWN) {
                    selectedMenuOption = (selectedMenuOption + 1) % 3;
                } else if (key == KeyEvent.VK_ENTER) {
                    if (selectedMenuOption == 0) {
                        startNewGame();
                    } else if (selectedMenuOption == 1) {
                        if (applesEaten > 0 || currentState == State.PAUSED) {
                            currentState = State.PLAYING;
                            timer.start();
                        } else {
                            startNewGame();
                        }
                    } else if (selectedMenuOption == 2) {
                        selectedMenuOption = 1;
                        currentState = State.DIFFICULTY_SELECT;
                    }
                }
            } else if (currentState == State.DIFFICULTY_SELECT) {
                if (key == KeyEvent.VK_UP) {
                    selectedMenuOption = (selectedMenuOption - 1 + 3) % 3;
                } else if (key == KeyEvent.VK_DOWN) {
                    selectedMenuOption = (selectedMenuOption + 1) % 3;
                } else if (key == KeyEvent.VK_ENTER) {
                    if (selectedMenuOption == 0) {
                        delay = 120;
                        difficultyName = "EASY";
                    } else if (selectedMenuOption == 1) {
                        delay = 80;
                        difficultyName = "MEDIUM";
                    } else if (selectedMenuOption == 2) {
                        delay = 40;
                        difficultyName = "HARD";
                    }
                    selectedMenuOption = 0;
                    currentState = State.MENU;
                }
            } else if (currentState == State.PLAYING) {
                if (canChangeDirection) {
                    if (key == KeyEvent.VK_LEFT && direction != 'R') { direction = 'L'; canChangeDirection = false; }
                    else if (key == KeyEvent.VK_RIGHT && direction != 'L') { direction = 'R'; canChangeDirection = false; }
                    else if (key == KeyEvent.VK_UP && direction != 'D') { direction = 'U'; canChangeDirection = false; }
                    else if (key == KeyEvent.VK_DOWN && direction != 'U') { direction = 'D'; canChangeDirection = false; }
                }
                if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_P) {
                    currentState = State.PAUSED;
                    timer.stop();
                }
            } else if (currentState == State.PAUSED) {
                if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_P) {
                    currentState = State.PLAYING;
                    timer.start();
                } else if (key == KeyEvent.VK_ESCAPE) {
                    currentState = State.MENU;
                }
            } else if (currentState == State.GAME_OVER) {
                if (key == KeyEvent.VK_ENTER) {
                    selectedMenuOption = 0;
                    currentState = State.MENU;
                }
            }
            repaint();
        }
    }
}