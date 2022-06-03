import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Random;


public class GamePanel extends JPanel implements ActionListener {

  static final int WIDTH = 500;
  static final int HEIGHT = 500;
  static final int UNIT_SIZE = 10;
  static final int ELEMENTS = 5;
  static final int OBSTACLES_NUM = 8;
  static final int GAME_UNITS = (WIDTH * HEIGHT) / UNIT_SIZE;
  static final int DELAY = 65;
  static final String DATAFILE = "record.dat";

  final int xCoord[] = new int[GAME_UNITS];
  final int yCoord[] = new int[GAME_UNITS];
  final int aiXCoord[] = new int[GAME_UNITS];
  final int aiYCoord[] = new int[GAME_UNITS];

  final int xFruits[] = new int[ELEMENTS];
  final int yFruits[] = new int[ELEMENTS];

  int segments = 6;
  int aiSegments = 6;
  int eatenFruits;
  int aiEatenFruits;
  int record;
  int xFrog;
  int yFrog;
  int[][] obstaclesX;
  int[][] obstaclesY;
  boolean isRunning = false;
  boolean gamerWon = false;
  boolean aiWon = false;
  Timer timer;
  Random random;
  Direction direction = Direction.RIGHT;
  Direction aiDirection = Direction.LEFT;
  Direction frogDirection = Direction.LEFT;
  File datafile;

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

    Scanner istream = null;
    datafile = new File(DATAFILE);
    
    try {
      istream = new Scanner(datafile);
    } catch (FileNotFoundException e) {
      System.out.println("Cannot open file");
    }

    record = istream.nextInt();

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
  
    for (int i = 0; i < aiSegments; i++) {
      aiXCoord[i] = WIDTH;
      aiYCoord[i] = HEIGHT;
    }

    for (int i = 0; i < ELEMENTS; i++) {
      xFruits[i] = -1;
      yFruits[i] = -1;
    }

    start();
  }

  public void start() {
    putFrog();
    putObstacles();
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
      for (int i = 0; i < ELEMENTS; i++) {
        g.fillOval(xFruits[i], yFruits[i], UNIT_SIZE, UNIT_SIZE);
      }

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

      //
      // frog
      //
      g.setColor(Color.blue);
      g.fillOval(xFrog, yFrog, UNIT_SIZE, UNIT_SIZE);

      //
      // obstacles
      //

      for (int i = 0; i < OBSTACLES_NUM; i++)
      {
        for (int j = 0; j < ELEMENTS; j++)
        {
          g.setColor(Color.black);
          g.fillRect(obstaclesX[i][j], obstaclesY[i][j], UNIT_SIZE, UNIT_SIZE);
        }
      }

      g.setColor(Color.red);
      g.setFont(new Font("Arial", Font.BOLD, 10));
      FontMetrics metrics = getFontMetrics(g.getFont());
      g.drawString("You: " + eatenFruits + " AI: " + aiEatenFruits,
          (WIDTH - metrics.stringWidth("You: " + eatenFruits + " AI: " + aiEatenFruits)) / 2,
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

  public void moveFrog() {
    if (xFrog == WIDTH - UNIT_SIZE && yFrog == 0) {
      switch (frogDirection) {
        case RIGHT:
          frogDirection = Direction.DOWN;
          break;
        case UP:
          frogDirection = Direction.LEFT;
          break;
        default:
          break;
      }
    }

    if (xFrog == 0 && yFrog == HEIGHT - UNIT_SIZE) {
      switch (frogDirection) {
        case LEFT:
          frogDirection = Direction.UP;
          break;
        case DOWN:
          frogDirection = Direction.RIGHT;
          break;
        default:
          break;
      }
    }

    switch (xFrog) {
      case 0:
        if (frogDirection == Direction.LEFT) {
          frogDirection = Direction.DOWN;
        }
        break;
      case WIDTH - UNIT_SIZE:
        if (frogDirection == Direction.RIGHT) {
          frogDirection = Direction.UP;
        }
        break;
    }

    switch (yFrog) {
      case 0:
        if (frogDirection == Direction.UP) {
          frogDirection = Direction.LEFT;
        }
        break;
      case HEIGHT - UNIT_SIZE:
        if (frogDirection == Direction.DOWN) {
          frogDirection = Direction.RIGHT;
        }
        break;
    }

    if (xFrog == 0 && yFrog == 0) {
      switch (frogDirection) {
        case UP:
          frogDirection = Direction.RIGHT;
          break;
        case LEFT:
          frogDirection = Direction.DOWN;
          break;
        default:
          break;
      }
    }
    if (xFrog == WIDTH - UNIT_SIZE && yFrog == HEIGHT - UNIT_SIZE) {
      switch (frogDirection) {
        case DOWN:
          frogDirection = Direction.LEFT;
          break;
        case RIGHT:
          frogDirection = Direction.UP;
          break;
        default:
          break;
      }
    }

    for (int i  = 1; i < segments; i++) {
      if (xCoord[i] == xFrog && yCoord[i] == yFrog) {
        switch (frogDirection)
        {
          case RIGHT:
            frogDirection = Direction.LEFT;
            break;
          case DOWN:
            frogDirection = Direction.UP;
            break;
          case UP:
            frogDirection = Direction.DOWN;
            break;
          case LEFT:
            frogDirection = Direction.RIGHT;
            break;
        }
      }
    }

    for (int i  = 1; i < aiSegments; i++) {
      if (aiXCoord[i] == xFrog && aiYCoord[i] == yFrog) {
        switch (frogDirection)
        {
          case RIGHT:
            frogDirection = Direction.LEFT;
            break;
          case DOWN:
            frogDirection = Direction.UP;
            break;
          case UP:
            frogDirection = Direction.DOWN;
            break;
          case LEFT:
            frogDirection = Direction.RIGHT;
            break;
        }
      }
    }

    for (int i = 0; i < OBSTACLES_NUM; i++)
    {
      for (int j = 0; j < ELEMENTS; j++)
      {
        if (Math.abs(xFrog - obstaclesX[i][j])  == UNIT_SIZE && 
            Math.abs(yFrog - obstaclesY[i][j]) == 0) {
            if (xFrog < obstaclesX[i][j] && frogDirection == Direction.RIGHT) {
              frogDirection = Direction.UP;
            }
            if (xFrog > obstaclesX[i][j] && frogDirection == Direction.LEFT) {
              frogDirection = Direction.DOWN;
            }
        }

        if (Math.abs(xFrog - obstaclesX[i][j])  == 0 && 
            Math.abs(yFrog - obstaclesY[i][j]) == UNIT_SIZE) {
          if (yFrog < obstaclesY[i][j] && frogDirection == Direction.DOWN) {
            frogDirection = Direction.LEFT;
          }
          if (yFrog > obstaclesY[i][j] && frogDirection == Direction.UP) {
            frogDirection = Direction.RIGHT;
          }
        }
      }
    }

    switch (frogDirection) {
      case UP:
        yFrog -= UNIT_SIZE;
        break;
      case DOWN:
        yFrog += UNIT_SIZE;
        break;
      case RIGHT:
        xFrog += UNIT_SIZE;
        break;
      case LEFT:
        xFrog -= UNIT_SIZE;
        break;
    }
  }

  public void aiMove() {

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

    int index = closestFruitIndex();
    int xClosestFruit = xFruits[index];
    int yClosestFruit = yFruits[index];

    if (aiXCoord[0] == xClosestFruit) {
      if (aiYCoord[0] > yClosestFruit && aiDirection != Direction.DOWN) {
        aiDirection = Direction.UP;
      } 
      if (aiYCoord[0] < yClosestFruit && aiDirection != Direction.UP) {
        aiDirection = Direction.DOWN;
      }
    }

    if (aiYCoord[0] == yClosestFruit) {
      if (aiXCoord[0] < xClosestFruit && aiDirection != Direction.LEFT) {
        aiDirection = Direction.RIGHT;
      } 
      if (aiXCoord[0] > xClosestFruit && aiDirection != Direction.RIGHT) {
        aiDirection = Direction.LEFT;
      }
    }

    for (int i = 0; i < OBSTACLES_NUM; i++)
    {
      for (int j = 0; j < ELEMENTS; j++)
      {
        if (Math.abs(aiXCoord[0] - obstaclesX[i][j])  == UNIT_SIZE && 
            Math.abs(aiYCoord[0] - obstaclesY[i][j]) == 0) {
            if (aiXCoord[0] < obstaclesX[i][j] && aiDirection == Direction.RIGHT) {
              aiDirection = Direction.UP;
            }
            if (aiXCoord[0] > obstaclesX[i][j] && aiDirection == Direction.LEFT) {
              aiDirection = Direction.DOWN;
            }
        }

        if (Math.abs(aiXCoord[0] - obstaclesX[i][j])  == 0 && 
            Math.abs(aiYCoord[0] - obstaclesY[i][j]) == UNIT_SIZE) {
          if (aiYCoord[0] < obstaclesY[i][j] && aiDirection == Direction.DOWN) {
            aiDirection = Direction.LEFT;
          }
          if (aiYCoord[0] > obstaclesY[i][j] && aiDirection == Direction.UP) {
          aiDirection = Direction.RIGHT;
          }
        }
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

  public void putObstacles() {
    obstaclesX = new int[OBSTACLES_NUM][ELEMENTS];
    obstaclesY = new int[OBSTACLES_NUM][ELEMENTS];
    for (int i = 0; i < OBSTACLES_NUM; i++)
    {
      obstaclesX[i][0] = random.nextInt((WIDTH - 5*UNIT_SIZE) / UNIT_SIZE) * UNIT_SIZE;
      obstaclesY[i][0] = random.nextInt((HEIGHT - 5*UNIT_SIZE) / UNIT_SIZE) * UNIT_SIZE;
      for (int j = 1; j < ELEMENTS; j++) {
        if (i % 2 == 0) {
          obstaclesX[i][j] = obstaclesX[i][0];
          obstaclesY[i][j] = obstaclesY[i][0] + j*UNIT_SIZE;
        } else {
          obstaclesX[i][j] = obstaclesX[i][0] + j*UNIT_SIZE;
          obstaclesY[i][j] = obstaclesY[i][0];
        }
      }
    }
  }

  public void putFruit() {
    for (int i = 0; i < ELEMENTS; i++) {

      if (xFruits[i] == -1) xFruits[i] = random.nextInt((int) (WIDTH / UNIT_SIZE)) * UNIT_SIZE;
      if (yFruits[i] == -1) yFruits[i] = random.nextInt((int) (HEIGHT / UNIT_SIZE)) * UNIT_SIZE;

      for (int j = 0; j < OBSTACLES_NUM; j++)
      {
        for (int k = 0; k < ELEMENTS; k++) {
          if (xFruits[i] == obstaclesX[j][k] && yFruits[i] == obstaclesY[j][k]) {
           xFruits[i] = -1;
           yFruits[i] = -1;
          }
        }
      }
    }    
  }

  public void putFrog() {
    xFrog = WIDTH / 2;
    yFrog = HEIGHT / 2;
    int dir = random.nextInt(4);
    switch(dir) {
      case 0:
        frogDirection = Direction.RIGHT;
        break;
      case 1:
        frogDirection = Direction.LEFT;
        break;
      case 2:
        frogDirection = Direction.UP;
        break;
      case 3:
        frogDirection = Direction.DOWN;
        break;
    }
  }

  public void checkFruit() {
    for (int i = 0; i < ELEMENTS; i++) {
      if (xCoord[0] == xFruits[i] && yCoord[0] == yFruits[i]) {
        segments++;
        eatenFruits++;
        xFruits[i] = -1;
        yFruits[i] = -1;
        int newFrogDirection = random.nextInt(2);
        switch (newFrogDirection) {
          case 0:
            if (frogDirection == Direction.UP) {
              frogDirection = Direction.RIGHT;
            }
            if ( frogDirection == Direction.RIGHT) {
              frogDirection = Direction.DOWN;
            }
            break;
          case 1:
            if (frogDirection == Direction.DOWN) {
              frogDirection = Direction.LEFT;
            }
            if (frogDirection == Direction.LEFT) {
              frogDirection = Direction.UP;
            }
            break;
        }
        
        putFruit();
      }
    }
  }

  public void aiCheckFruit() {
    for (int i = 0; i < ELEMENTS; i++) {
      if (aiXCoord[0] == xFruits[i] && aiYCoord[0] == yFruits[i]) {
        aiSegments++;
        aiEatenFruits++;
        xFruits[i] = -1;
        yFruits[i] = -1;
        putFruit();
      }
    }
  }

  public void checkFrog() {
    if (xCoord[0] == xFrog && yCoord[0] == yFrog) {
      segments++;
      eatenFruits += 3;
      putFrog();
    }
  }

  public void aiCheckFrog() {
    if (aiXCoord[0] == xFrog && aiYCoord[0] == yFrog) {
      aiSegments++;
      aiEatenFruits += 3;
      putFrog();
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

    for (int i = 0; i < OBSTACLES_NUM; i++)
    {
      for (int j = 0; j < ELEMENTS; j++)
      {
        if (xCoord[0] == obstaclesX[i][j] && yCoord[0] == obstaclesY[i][j]) {
          isRunning = false;
        }
      }
    }

    for (int i = segments; i > 0; i--) {
      for (int j = aiSegments; j > 0; j--) {
        if (xCoord[i] == aiXCoord[j] && yCoord[i] == aiYCoord[j]) {
          isRunning = false;
        }
      }
    }

    if (!isRunning) {
      aiEatenFruits += 5;
      if (aiEatenFruits > eatenFruits) {
        aiWon = true;
      } else if (aiEatenFruits < eatenFruits) {
        gamerWon = true;
      }
      timer.stop();
    }
  }

  public void aiCheckCollisions() {
    for (int i = aiSegments; i > 0; i--) {
      if (aiXCoord[0] == aiXCoord[i] && aiYCoord[0] == aiYCoord[i]) {
        isRunning = false;
      }
    }

    if (aiXCoord[0] < 0 || aiXCoord[0] > WIDTH || aiYCoord[0] < 0 || aiYCoord[0] > HEIGHT) {
      isRunning = false;
      gamerWon = true;
    }

    for (int i = 0; i < OBSTACLES_NUM; i++)
    {
      for (int j = 0; j < ELEMENTS; j++)
      {
        if (aiXCoord[0] == obstaclesX[i][j] && aiYCoord[0] == obstaclesY[i][j]) {
          isRunning = false;
          gamerWon = true;
        }
      }
    }

    if (!isRunning) {
      eatenFruits += 5;
      if (eatenFruits > aiEatenFruits) {
        gamerWon = true;
      } else if (eatenFruits < aiEatenFruits) {
        aiWon = true;
      }
      timer.stop();
    }
  }

  public void gameOver(Graphics g) {
    this.setFocusable(false);

    if (record < eatenFruits) {
      PrintWriter ostream = null;
      try {
        ostream = new PrintWriter(datafile);
      } catch (FileNotFoundException e) {
        System.out.println("Cannot open file");
      }

      ostream.println(eatenFruits);
      ostream.close();
    }
    g.setColor(Color.red);

    g.setFont(new Font("Arial", Font.BOLD, 10));
    FontMetrics metrics1 = getFontMetrics(g.getFont());
    g.drawString("You: " + eatenFruits + " AI: " + aiEatenFruits,
          (WIDTH - metrics1.stringWidth("You: " + eatenFruits + " AI: " + aiEatenFruits)) / 2,
          g.getFont().getSize());

    g.setFont(new Font("Arial", Font.BOLD, 20));
    FontMetrics metrics2 = getFontMetrics(g.getFont());
    g.drawString("Game Over",
        (WIDTH - metrics2.stringWidth("Game Over")) / 2,
        HEIGHT / 2);

    if (gamerWon) {
      g.drawString("You won!",
                   (WIDTH - metrics2.stringWidth("You won!")) / 2,
                   HEIGHT / 2 - 5*UNIT_SIZE);
    } else if (aiWon) {
      g.drawString("AI won!",
                   (WIDTH - metrics2.stringWidth("AI won!")) / 2,
                   HEIGHT / 2 - 5*UNIT_SIZE);
    } else {
      g.drawString("Draw",
                   (WIDTH - metrics2.stringWidth("Draw")) / 2,
                   HEIGHT / 2 - 5*UNIT_SIZE);
    }
  }

  public int closestFruitIndex() {
    int shortestDistance = WIDTH + HEIGHT;
    int outIndex = 0;
    for (int i = 0; i < ELEMENTS; i++) {
      int distance = Math.abs(aiXCoord[0] - xFruits[i]) + Math.abs(aiYCoord[0] - yFruits[i]);
      if (distance < shortestDistance) {
        shortestDistance = distance;
        outIndex = i;
      }
    }
    return outIndex;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (isRunning) {
      move();
      moveFrog();
      aiMove();
      checkFruit();
      checkFrog();
      aiCheckFruit();
      aiCheckFrog();
      checkCollisions();
      aiCheckCollisions();
    }
    repaint();
  }
}