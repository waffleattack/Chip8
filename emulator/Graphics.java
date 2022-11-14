package emulator;
class Graphics {
  private boolean[][] screen = new boolean[32][64]; 

  public void clear(){
    screen = new boolean[32][64];
  }
  public Graphics(){}
}