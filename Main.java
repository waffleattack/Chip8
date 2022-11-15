import emulator.CPU;

import java.util.Timer;

class Main {
  static CPU cpu = new CPU();
  static Timer timer = new Timer();
  static short[] test = new short[10];
  public static void main(String[] args) {
  while(true){
    cpu.cycle();
  }
  }
}