package emulator;

import java.util.function.BooleanSupplier;

public class CPU{
  short[] registers = new short[12];
  short indexRegister = 0x00;
  Memory ram = new Memory(4096);
  int[] callStack = new int[64];
  short stackPointer = 0x0;
  static short ROM_START = 0x200;
  static short pc = ROM_START;
  Screen screen = new Screen();

  static BooleanSupplier[] input = new BooleanSupplier[16];
  short timer;
  public CPU(){
    ram.loadRom("IBM Logo.txt");
    ram.dumpMemory(8);
    ROM_START = 0x200;
    //screen.randomizeScreen();
    //screen.updateScreen();
  }
  //runs a single cpu cycle
  public void cycle(){
    while(true) {
      short opcode = ram.getIndex(pc);
      pc++;
      short argument = ram.getIndex(pc);
      pc++;
      execute(opcode, argument);
    }

  }
  private void execute(short opcode, short arg){

    short hb = opcode;
    short lb = arg;

    short b = (short) (hb << 8 + lb);

    short NNN = (short)( b & 0b111111111111);
    short NN = (short)( b & 0b1111_1111);
    short N = (short)( b & 0b1111);
    short X = (short)(b >> 8 & 0b1111);
    short Y = (short)((b>>4) & 0b1111);
    short nibble = (short)(lb >> 12 & 0b1111);
    if(b == 0xFF) {
      screen.updateScreen();
    }
    switch(nibble){
       case 0x0:
         if (b == 0x00E0) {
           //clear screen
           //screen.clear();
         }
         break;

      case 0x1:
         //jump
         pc = NNN;
         System.exit(2);
         break;
       case 0x2:
         //call subroutine
         stackPointer = (short) ((stackPointer + 1) % 64);
         callStack[stackPointer] = pc;
         pc = NNN;
         break;
       case 0x6:
         registers[X] = NN;
         break;
       case 0x7:
         registers[X] = (short) ((registers[X] + NN) % 256);
         break;
       case 0xA:
         indexRegister = NNN;
         break;
       case 0xD:
         X = (short)(registers[X] & 63);
         Y = (short)(registers[Y] &31);
         registers[0xF] = 0;
         for(int i = 0; i < N; i++){
           short data = (short)(indexRegister+i);
           for(int j = 0; j < 8; j++){
             if(X+i > 63)
               continue;
             boolean bit = ((data >> j) & 0b1) == 1;
             if (screen.vRam[X+i][Y+j] & bit){
               registers[0xF] = 1;
             }
             screen.vRam[X+i][Y+j] = screen.vRam[X+i][Y+j] ^ bit;

           }

         }
         screen.updateScreen();
         break;
      default:
        System.out.println(pc);
        break;

     }
  }
  private void OP_00E0(){
    screen.clear();
  }
  private void pushStack(short address){
  }

  
}