import javax.swing.*;
import java.awt.event.*;

public class GameFrame extends JFrame implements ActionListener {
  GamePanel game;
  JButton resetButton;

  GameFrame() {
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setSize(400, 300);
    this.setLayout(null);
    resetButton = new JButton("Reset");
    resetButton.setSize(100, 50);
    resetButton.setLocation(0, 0);
    resetButton.addActionListener(this);

    game = new GamePanel();

    this.add(resetButton);
    this.add(game);

    this.setVisible(true);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == resetButton) {
      this.remove(game);
      game = new GamePanel();
      this.add(game);
      SwingUtilities.updateComponentTreeUI(this);
    }
  }
}