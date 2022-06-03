import javax.swing.*;

import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class GameFrame extends JFrame implements ActionListener {
  GamePanel game;
  JButton resetButton;
  JLabel label;
  static final String DATAFILE = "record.dat";

  GameFrame() {
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setSize(600, 500);
    this.setLayout(null);
    
    resetButton = new JButton("Reset");
    resetButton.setSize(100, 50);
    resetButton.setLocation(0, 0);
    resetButton.addActionListener(this);
    readHighestScore();
    game = new GamePanel();

    this.add(resetButton);
    this.add(game);

    this.setVisible(true);
  }

  public void readHighestScore()  {
    Scanner istream = null;
    File datafile = new File(DATAFILE);
    try {
      istream = new Scanner(datafile);
    } catch (FileNotFoundException e) {
      System.out.println("Cannot open file");
    }

    int record = istream.nextInt();
    label = new JLabel("Record: " + record);
    label.setBounds(0, 100,
                    label.getPreferredSize().width, 
                    label.getPreferredSize().height);
    this.add(label);
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == resetButton) {
      this.remove(game);
      this.remove(label);
      game = new GamePanel();
      this.add(game);
      readHighestScore();
      SwingUtilities.updateComponentTreeUI(this);
    }
  }
}