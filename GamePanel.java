import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {

  static final int WIDTH = 300;
  static final int HEIGHT = 300;
  static final int UNIT_SIZE = 10;
  static final int GAME_UNITS = (WIDTH * HEIGHT) / UNIT_SIZE;
  static final int DELAY = 75;

  final int xCoord[] = new int[GAME_UNITS];
  final int yCoord[] = new int[GAME_UNITS];

  int segments = 6;
  int eatenFruits;
  int xFruit;
  int yFruit;
  boolean isRunning = false;
  Timer timer;
  Random random;
  Direction direction = Direction.RIGHT;

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
    this.setLocation(100,0);
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
    this.getActionMap().put("turnRight",turnRightAction);

    start();
  }

  public void start() {
    putFruit();
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
      for (int i = 0; i < HEIGHT / UNIT_SIZE; i++) {
        g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, HEIGHT);
        g.drawLine(0, i * UNIT_SIZE, WIDTH, i * UNIT_SIZE);
      }
      g.setColor(Color.red);
      g.fillOval(xFruit, yFruit, UNIT_SIZE, UNIT_SIZE);

      for (int i = 0; i < segments; i++) {
        if (i == 0) {
          g.setColor(Color.green);
          g.fillRect(xCoord[i], yCoord[i], UNIT_SIZE, UNIT_SIZE);
        }
        else {
          g.setColor(new Color(45,180,0));
          g.fillRect(xCoord[i], yCoord[i], UNIT_SIZE, UNIT_SIZE);
        }
      }
      g.setColor(Color.red);
      g.setFont(new Font("Arial", Font.BOLD, 10));
      FontMetrics metrics = getFontMetrics(g.getFont());
      g.drawString("Score: " + eatenFruits, 
                  (WIDTH - metrics.stringWidth("Score: " + eatenFruits)) / 2, 
                  g.getFont().getSize());
    }
    else {
      gameOver(g);
    }
  }
  
  public void move() {
    for (int i = segments; i > 0; i--) {
      xCoord[i] = xCoord[i - 1];
      yCoord[i] = yCoord[i - 1];
    }

    switch(direction) {
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

  public void putFruit() {
    xFruit = random.nextInt((int)(WIDTH / UNIT_SIZE)) * UNIT_SIZE;
    yFruit = random.nextInt((int)(HEIGHT / UNIT_SIZE)) * UNIT_SIZE;
  }
  
  public void checkFruit() {
    if (xCoord[0] == xFruit && yCoord[0] == yFruit) {
      segments++;
      eatenFruits++;
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
                 HEIGHT/2);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (isRunning) {
      move();
      checkFruit();
      checkCollisions();
    }
    repaint();
  }
}