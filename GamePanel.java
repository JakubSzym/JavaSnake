import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {

  static final int WIDTH = 500;
  static final int HEIGHT = 500;
  static final int UNIT_SIZE = 10;
  static final int GAME_UNITS = (WIDTH * HEIGHT) / UNIT_SIZE;
  static final int DELAY = 50;

  final int xCoord[] = new int[GAME_UNITS];
  final int yCoord[] = new int[GAME_UNITS];
  final int aiXCoord[] = new int[GAME_UNITS];
  final int aiYCoord[] = new int[GAME_UNITS];

  int segments = 6;
  int aiSegments = 6;
  int eatenFruits;
  int xFruit;
  int yFruit;
  boolean isRunning = false;
  Timer timer;
  Random random;
  Direction direction = Direction.RIGHT;
  Direction aiDirection = Direction.LEFT;

  Action turnUpAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (direction != Direction.DOWN) {
        direction = Direction.UP;
      }
    }
  };

  Action turnDownAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (direction != Direction.UP) {
        direction = Direction.DOWN;
      }
    }
  };

  Action turnLeftAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (direction != Direction.RIGHT) {
        direction = Direction.LEFT;
      }
    }
  };

  Action turnRightAction = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
      if (direction != Direction.LEFT) {
        direction = Direction.RIGHT;
      }
    }
  };

  GamePanel() {
    random = new Random();

    this.setSize(WIDTH, HEIGHT);
    this.setLocation(100, 0);
    this.setBackground(Color.DARK_GRAY);
    this.setFocusable(true);
    this.requestFocusInWindow(true);

    this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "turnUp");
    this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "turnDown");
    this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "turnLeft");
    this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "turnRight");

    this.getActionMap().put("turnUp", turnUpAction);
    this.getActionMap().put("turnDown", turnDownAction);
    this.getActionMap().put("turnLeft", turnLeftAction);
    this.getActionMap().put("turnRight", turnRightAction);

    start();
  }

  public void start() {
    putFruit();
    aiXCoord[0] = HEIGHT;
    aiYCoord[0] = WIDTH;
    isRunning = true;
    timer = new Timer(DELAY, this);
    timer.start();
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    draw(g);
  }

  public void draw(Graphics g) {
    if (isRunning) {

      //
      // Board
      //

      for (int i = 0; i < HEIGHT / UNIT_SIZE; i++) {
        g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, HEIGHT);
        g.drawLine(0, i * UNIT_SIZE, WIDTH, i * UNIT_SIZE);
      }

      //
      // Fruits
      //

      g.setColor(Color.red);
      g.fillOval(xFruit, yFruit, UNIT_SIZE, UNIT_SIZE);

      //
      // Snake
      //

      for (int i = 0; i < segments; i++) {
        if (i == 0) {
          g.setColor(Color.green);
          g.fillRect(xCoord[i], yCoord[i], UNIT_SIZE, UNIT_SIZE);
        } else {
          g.setColor(new Color(45, 180, 0));
          g.fillRect(xCoord[i], yCoord[i], UNIT_SIZE, UNIT_SIZE);
        }
      }

      //
      // AI Snake
      //

      for (int i = 0; i < aiSegments; i++) {
        if (i == 0) {
          g.setColor(new Color(51,0,0));
          g.fillRect(aiXCoord[i], aiYCoord[i], UNIT_SIZE, UNIT_SIZE);
        } else {
          g.setColor(Color.orange);
          g.fillRect(aiXCoord[i], aiYCoord[i], UNIT_SIZE, UNIT_SIZE);
        }
      }


      g.setColor(Color.red);
      g.setFont(new Font("Arial", Font.BOLD, 10));
      FontMetrics metrics = getFontMetrics(g.getFont());
      g.drawString("Score: " + eatenFruits,
          (WIDTH - metrics.stringWidth("Score: " + eatenFruits)) / 2,
          g.getFont().getSize());
    } else {
      gameOver(g);
    }
  }

  public void move() {
    for (int i = segments; i > 0; i--) {
      xCoord[i] = xCoord[i - 1];
      yCoord[i] = yCoord[i - 1];
    }

    switch (direction) {
      case UP:
        yCoord[0] -= UNIT_SIZE;
        break;
      case DOWN:
        yCoord[0] += UNIT_SIZE;
        break;
      case RIGHT:
        xCoord[0] += UNIT_SIZE;
        break;
      case LEFT:
        xCoord[0] -= UNIT_SIZE;
        break;
    }
  }

  public void aiMove() {
    for (int i = 0; i < aiSegments; i++) {
      if (aiXCoord[0] == aiXCoord[i] - 1 && aiYCoord[0] == aiYCoord[i] && aiDirection == Direction.RIGHT) {
        aiDirection = Direction.DOWN;
      }

      if (aiXCoord[0] == aiXCoord[i] + 1 && aiYCoord[0] == aiYCoord[i] && aiDirection == Direction.LEFT) {
        aiDirection = Direction.UP;
      }

      if (aiXCoord[0] == aiXCoord[i] && aiYCoord[0] == aiYCoord[i] - 1 && aiDirection == Direction.UP) {
        aiDirection = Direction.RIGHT;
      }

      if (aiXCoord[0] == aiXCoord[i] && aiYCoord[0] == aiYCoord[i] + 1 && aiDirection == Direction.DOWN) {
        aiDirection = Direction.LEFT;
      }
    }

    if (aiXCoord[0] == WIDTH - UNIT_SIZE && aiYCoord[0] == 0) {
      switch (aiDirection) {
        case RIGHT:
          aiDirection = Direction.DOWN;
          break;
        case UP:
          aiDirection = Direction.LEFT;
          break;
        default:
          break;
      }
    }

    if (aiXCoord[0] == 0 && aiYCoord[0] == HEIGHT - UNIT_SIZE) {
      switch (aiDirection) {
        case LEFT:
          aiDirection = Direction.UP;
          break;
        case DOWN:
          aiDirection = Direction.RIGHT;
          break;
        default:
          break;
      }
    }

    switch (aiXCoord[0]) {
      case 0:
        if (aiDirection == Direction.LEFT) {
          aiDirection = Direction.DOWN;
        }
        break;
      case WIDTH - UNIT_SIZE:
        if (aiDirection == Direction.RIGHT) {
          aiDirection = Direction.UP;
        }
        break;
    }

    switch (aiYCoord[0]) {
      case 0:
        if (aiDirection == Direction.UP) {
          aiDirection = Direction.RIGHT;
        }
        break;
      case HEIGHT - UNIT_SIZE:
        if (aiDirection == Direction.DOWN) {
          aiDirection = Direction.LEFT;
        }
        break;
    }

    if (aiXCoord[0] == 0 && aiYCoord[0] == 0) {
      switch (aiDirection) {
        case UP:
          aiDirection = Direction.RIGHT;
          break;
        case LEFT:
          aiDirection = Direction.DOWN;
          break;
        default:
          break;
      }
    }
    if (aiXCoord[0] == WIDTH - UNIT_SIZE && aiYCoord[0] == HEIGHT - UNIT_SIZE) {
      switch (aiDirection) {
        case DOWN:
          aiDirection = Direction.LEFT;
          break;
        case RIGHT:
          aiDirection = Direction.UP;
          break;
        default:
          break;
      }
    }

    if (aiXCoord[0] == xFruit) {
      if (aiYCoord[0] > yFruit && aiDirection != Direction.DOWN) {
        aiDirection = Direction.UP;
      } 
      if (aiYCoord[0] < yFruit && aiDirection != Direction.UP) {
        aiDirection = Direction.DOWN;
      }
    }

    if (aiYCoord[0] == yFruit) {
      if (aiXCoord[0] < xFruit && aiDirection != Direction.LEFT) {
        aiDirection = Direction.RIGHT;
      } 
      if (aiXCoord[0] > xFruit && aiDirection != Direction.RIGHT) {
        aiDirection = Direction.LEFT;
      }
    }

    for (int i = aiSegments; i > 0; i--) {
      aiXCoord[i] = aiXCoord[i - 1];
      aiYCoord[i] = aiYCoord[i - 1];
    }

    switch (aiDirection) {
      case UP:
        aiYCoord[0] -= UNIT_SIZE;
        break;
      case DOWN:
        aiYCoord[0] += UNIT_SIZE;
        break;
      case RIGHT:
        aiXCoord[0] += UNIT_SIZE;
        break;
      case LEFT:
        aiXCoord[0] -= UNIT_SIZE;
        break;
    }
  }

  public void putFruit() {
    xFruit = random.nextInt((int) (WIDTH / UNIT_SIZE)) * UNIT_SIZE;
    yFruit = random.nextInt((int) (HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
  }

  public void checkFruit() {
    if (xCoord[0] == xFruit && yCoord[0] == yFruit) {
      segments++;
      eatenFruits++;
      putFruit();
    }
  }

  public void aiCheckFruit() {
    if (aiXCoord[0] == xFruit && aiYCoord[0] == yFruit) {
      aiSegments++;
      putFruit();
    }
  }

  public void checkCollisions() {
    for (int i = segments; i > 0; i--) {
      if (xCoord[0] == xCoord[i] && yCoord[0] == yCoord[i]) {
        isRunning = false;
      }
    }

    if (xCoord[0] < 0 || xCoord[0] > WIDTH || yCoord[0] < 0 || yCoord[0] > HEIGHT) {
      isRunning = false;
    }

    if (!isRunning) {
      timer.stop();
    }
  }

  public void gameOver(Graphics g) {
    this.setFocusable(false);
    g.setColor(Color.red);

    g.setFont(new Font("Arial", Font.BOLD, 10));
    FontMetrics metrics1 = getFontMetrics(g.getFont());
    g.drawString("Score: " + eatenFruits,
        (WIDTH - metrics1.stringWidth("Score: " + eatenFruits)) / 2,
        g.getFont().getSize());

    g.setFont(new Font("Arial", Font.BOLD, 20));
    FontMetrics metrics2 = getFontMetrics(g.getFont());
    g.drawString("Game Over",
        (WIDTH - metrics2.stringWidth("Game Over")) / 2,
        HEIGHT / 2);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (isRunning) {
      move();
      aiMove();
      checkFruit();
      aiCheckFruit();
      checkCollisions();
    }
    repaint();
  }
}