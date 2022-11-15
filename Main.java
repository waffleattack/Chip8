import emulator.CPU;
class Main {
  static CPU cpu = new CPU();

  static short[] test = new short[10];
  public static void main(String[] args) {
  cpu.cycle();
  }
}