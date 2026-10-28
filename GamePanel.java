import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {

    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

    // Authentic Nokia LCD Palette
    static final Color NOKIA_BG = new Color(155, 188, 15);
    static final Color BORDER_COLOR = new Color(15, 56, 15);
    static final Color SNAKE_HEAD = new Color(20, 60, 20);
    static final Color SNAKE_BODY = new Color(43, 85, 43);
    static final Color APPLE_COLOR = new Color(75, 30, 30);

    enum State { MENU, DIFFICULTY_SELECT, HIGH_SCORE_SCREEN, PLAYING, PAUSED, GAME_OVER }
    State currentState = State.MENU;

    int delay = 80;
    String difficultyName = "MEDIUM";
    int selectedMenuOption = 0; // 0: New Game, 1: Difficulty, 2: High Score

    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten = 0;
    int highScore = 0;
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
        
        loadHighScore();
        
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

    public void loadHighScore() {
        try {
            File file = new File("highscore.txt");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                if (line != null) {
                    highScore = Integer.parseInt(line.trim());
                }
                reader.close();
            }
        } catch (Exception e) {
            highScore = 0;
        }
    }

    public void saveHighScore() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("highscore.txt"));
            writer.write(String.valueOf(highScore));
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            case HIGH_SCORE_SCREEN:
                drawHighScoreScreen(g);
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
            "2. DIFFICULTY: " + difficultyName,
            "3. HIGH SCORE"
        };

        for (int i = 0; i < options.length; i++) {
            if (i == selectedMenuOption) {
                g.drawString("> " + options[i], 110, 270 + (i * 60));
            } else {
                g.drawString("  " + options[i], 110, 270 + (i * 60));
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

    private void drawHighScoreScreen(Graphics g) {
        g.setColor(NOKIA_BG);
        g.fillRect(80, 180, SCREEN_WIDTH - 160, 240);
        g.setColor(BORDER_COLOR);
        g.drawRect(80, 180, SCREEN_WIDTH - 160, 240);
        g.drawRect(82, 182, SCREEN_WIDTH - 164, 236);

        g.setFont(new Font("Monospaced", Font.BOLD, 36));
        drawCenteredText(g, "HIGH SCORE", 36, 240);

        g.setFont(new Font("Monospaced", Font.BOLD, 30));
        drawCenteredText(g, "" + highScore, 30, 310);

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        drawCenteredText(g, "Press ENTER to Return", 18, 370);
    }

    private void drawGameplay(Graphics g) {
        g.setColor(APPLE_COLOR);
        g.fillRect(appleX + 3, appleY + 3, UNIT_SIZE - 6, UNIT_SIZE - 6);
        g.setColor(BORDER_COLOR);
        g.drawRect(appleX + 3, appleY + 3, UNIT_SIZE - 6, UNIT_SIZE - 6);

        for (int i = 0; i < bodyParts; i++) {
            if (i == 0) {
                g.setColor(SNAKE_HEAD);
                g.fillRect(x[i] + 1, y[i] + 1, UNIT_SIZE - 2, UNIT_SIZE - 2);
                g.setColor(BORDER_COLOR);
                g.drawRect(x[i] + 1, y[i] + 1, UNIT_SIZE - 2, UNIT_SIZE - 2);
            } else {
                g.setColor(SNAKE_BODY);
                g.fillRect(x[i] + 2, y[i] + 2, UNIT_SIZE - 4, UNIT_SIZE - 4);
            }
        }

        g.setColor(BORDER_COLOR);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString("SCORE: " + applesEaten, 25, 35);
        g.drawString("HI: " + highScore, SCREEN_WIDTH - 110, 35);
    }

    private void drawGameOver(Graphics g) {
        g.setColor(NOKIA_BG);
        g.fillRect(80, 170, SCREEN_WIDTH - 160, 260);
        g.setColor(BORDER_COLOR);
        g.drawRect(80, 170, SCREEN_WIDTH - 160, 260);
        g.drawRect(82, 172, SCREEN_WIDTH - 164, 256);

        g.setFont(new Font("Monospaced", Font.BOLD, 40));
        drawCenteredText(g, "GAME OVER", 40, 230);

        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        drawCenteredText(g, "SCORE: " + applesEaten, 22, 280);
        drawCenteredText(g, "HIGH SCORE: " + highScore, 22, 320);

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        drawCenteredText(g, "Press ENTER for Menu", 18, 385);
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
            
            if (applesEaten > highScore) {
                highScore = applesEaten;
                saveHighScore();
            }
            
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
                        selectedMenuOption = 1;
                        currentState = State.DIFFICULTY_SELECT;
                    } else if (selectedMenuOption == 2) {
                        currentState = State.HIGH_SCORE_SCREEN;
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
            } else if (currentState == State.HIGH_SCORE_SCREEN) {
                if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_ESCAPE) {
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