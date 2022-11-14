package emulator;
import emulator.Memory;
import java.util.function.BooleanSupplier;

public class CPU{
  short[] registers = new short[12];
  short indexRegister = 0x00;
  Memory ram = new Memory(1024);
  int[] callStack = new int[16];
  byte stackPointer = 0x0;
  static short ROM_START = 0x200;
  short pc = ROM_START;
  Graphics screen = new Graphics();

  static BooleanSupplier[] input = new BooleanSupplier[16];
  short timer;
  public CPU(){
    ram.loadRom("test.txt");
    ram.dumpMemory(1);
  }
  //runs a single cpu cycle
  public void cycle(){
    short opcode = ram.getIndex(pc);
    pc++;
    short argument = ram.getIndex(pc);
    pc ++;
    execute(opcode, argument);

  }
  private void execute(short opcode, short arg){
     System.out.println(String.format("0x%02X", (byte) opcode));
     System.out.println(String.format("0x%02X", (byte) arg));  
  }
  private void OP_00E0(){
    screen.clear();
  }

  
}