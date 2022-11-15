package emulator;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

class Screen {
  private int w = 64, h = 32;

  public JFrame frame;
  public boolean[][] vRam = new boolean[w][h];
  public void clear(){
    vRam = new boolean[w][h];
  }
  public Screen(){
    frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setSize(w, h);
    frame.setVisible(true);
  }
  public void updateScreen(){
    int color;
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
    for(int i=0; i<h; i++)
      for(int j=0; j<w; j++) {
        color = vRam[j][i] ? Color.BLACK.getRGB() : Color.WHITE.getRGB();
        bi.setRGB(j, i, color);
      }
    JPanel pane = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bi, 0, 0, w*8,h*8,null);
      }
    };
    frame.add(pane);
  }
  public void randomizeScreen() {
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        vRam[j][i] = Math.random()>= 0.5;
      }
    }
  }
}