package emulator;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BooleanSupplier;

public class CPU {
    short[] registers = new short[16];
    short indexRegister = 0x00;
    int[] callStack = new int[64];
    short stackPointer = 0x0;
    static short ROM_START = 0x200;
    static short pc = ROM_START;
    Random randgen = new Random();

    static BooleanSupplier[] input = new BooleanSupplier[16];
    short delayTime;
    Timer timer = new Timer();

    Memory ram = new Memory(4096);
    Screen screen = new Screen();
    Keyboard keyboard = new Keyboard();


    public CPU() {
        ram.loadRom("c8_test.c8");
        //ram.dumpMemory(8);
        ROM_START = 0x200;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(delayTime > 0){
                    delayTime--;
                }
            }
        },16, 16);
    }

    //runs a single cpu cycle
    public void cycle() {
            byte hb = ram.getIndex(pc);
            byte lb = ram.getIndex((short) (pc + 0x1));
            pc += 2;
            execute(hb, lb);
    }

    private void execute(short hb, short lb) {


        //0x00FF is neccesary or Java puts FF instead of 00's !!

        short b = (short) ((short) (hb << 8) | (lb & 0x00FF));

        short NNN = extractNNN(b);
        short NN = extractNN(b);
        byte N = extractN(b);
        byte X = extractX(b);
        byte Y = extractY(b);
        short nibble = extractNibble(b, 3);
        //System.out.printf("Executing %04X \n",b);
        if (b == 0xFF) {
            screen.updateScreen();
        }
        switch (nibble) {
            case 0x0:
                if (b == 0x00E0) {
                    System.out.println("clearing Screen");
                    screen.clear();
                }
                if (b == 0x00EE){
                    pc = (short) callStack[stackPointer];
                    stackPointer = (short) (stackPointer - 1);
                }
                break;
            case 0x1:
                //jump
                pc = NNN;
                break;
            case 0x2:
                //call subroutine
                stackPointer = (short) ((stackPointer + 1) % 64);
                callStack[stackPointer] = pc;
                pc = NNN;
                break;
            case 0x3:
                if ((registers[X] ^ NN) == 0)
                    pc += 2;
                break;
            case 0x4:
                if ((registers[X] ^ NN) != 0)
                    pc += 2;
                break;
            case 0x5:
                if ((registers[X] ^ registers[Y]) == 0)
                    pc+=2;
                break;
            case 0x6:
                registers[X] = NN;
                break;
            case 0x7:
                registers[X] = (short) ((registers[X] + NN) % 256);
                break;
            case 0x8:
                LogicalInstructions(b,X,Y);
                break;
            case 0xA:
                indexRegister = NNN;
                break;
            case 0xB:
                pc = (short) (NNN + registers[0]);
                break;
            case 0xC:
                registers[X] = (short) (NN & randgen.nextInt());
                break;
            case 0xD:
                draw(X, Y, N);
                screen.updateScreen();
                break;
            case 0xE:
                boolean pressed = keyboard.pressed[registers[X]];
                pc += (((NN ^ 0x9E) == 0) == pressed) ? 2 : 0;
                break;
            case 0xF:
                F_Operations(NN, X);
                break;


        }
    }

    private void F_Operations(short NN, byte X) {
        switch (NN) {
            case 0x07 -> registers[X] = delayTime;
            case 0x15 -> delayTime = registers[X];
            case 0x1E -> {
                indexRegister += registers[X];
                if (indexRegister > 0x1000)
                    registers[0xF] = 1;
            }
            case 0x0A -> {
                System.out.println("waiting for key");
                registers[X] = keyboard.waitForKey();
            }
            case 0x29 -> indexRegister = (short) (registers[X] * 5);
            case 0x33 -> {
                short startmemoryAddr = indexRegister;
                int int_vx = registers[X] & 0xff; //Get unsigned int from register Vx
                int hundreds = int_vx / 100; //Calculate hundreds
                int_vx = int_vx - hundreds * 100;
                int tens = int_vx / 10; //Calculate tens
                int_vx = int_vx - tens * 10;
                int units = int_vx; //Calculate units
                ram.setIndex(startmemoryAddr, (byte) hundreds);
                ram.setIndex((short) (startmemoryAddr + 1), (byte) tens);
                ram.setIndex((short) (startmemoryAddr + 2), (byte) units);
            }
            case 0x55 -> {
                for(short i = 0; i <= X; i++)
                    ram.setIndex((short) (indexRegister + i), (byte) registers[i]);
            }
            case 0x65 -> {
                for(short i = 0; i <= X; i++)
                    registers[i] = ram.getIndex((short) (indexRegister+i));
            }
        }
    }

    private void LogicalInstructions(short b, byte X, byte Y){
        byte nib = extractNibble(b,0);
        short resultValue;
        switch (nib) {
            case 0 ->
                    registers[X] = registers[Y];
            case 1 ->
                //bitwise OR
                    registers[X] = (short) (registers[Y] | registers[X]);
            case 2 ->
                //bitwise AND
                    registers[X] = (short) (registers[Y] & registers[X]);
            case 3 ->
                //bitwise XOR
                    registers[X] = (short) (registers[Y] ^ registers[X]);
            case 4 -> {
                //add with carry
                resultValue = (short) (registers[X] + registers[Y]);
                registers[0xF] = (short) (resultValue > 255 ? 1 : 0);
                registers[X] = (short) (resultValue & 0b11111111);
            }
            case 5 -> {
                //subtract with carry

                if (registers[X] > registers[Y]) {
                    resultValue = (short) (registers[X] - registers[Y]);
                    registers[0xF] = 1;
                } else {
                    resultValue = (short) (256 + registers[X] - registers[Y]);
                    registers[0xF] = 0;
                }
                registers[X] = resultValue;
            }
            case 7 -> {
                //subtract with carry
                if (registers[Y] > registers[X]) {
                    resultValue = (short) (registers[Y] - registers[X]);
                    registers[0xF] = 1;
                } else {
                    resultValue = (short) (256 + registers[Y] - registers[X]);
                    registers[0xF] = 0;
                }
                registers[X] = resultValue;
            }
            case 6 -> {
                registers[0xF] = (short) (registers[X] & 0b1);
                registers[X] = (short) (registers[X] >> 1);
            }
            case 0xE -> {
                registers[0xF] = (short) (registers[X] >> 7 & 0b1);
                registers[X] = (short) (registers[X] << 1);
            }
        }
    }
    //nnn are the 12 lowest bits (oNNN)
    private short extractNNN(short instruction) {
        return (short) (instruction & 0xFFF);
    }
    //kk are the 8 lowest bits (ooNN)
    private byte extractNN(short instruction) {
        return (byte) (instruction & 0xFF);
    }

    //x are the oXoo
    private byte extractX(short instruction) {
        return (byte) ((instruction & 0x0F00) >>> 8);
    }

    //y are the ooYo
    private byte extractY(short instruction) {
        return (byte) ((instruction & 0x00F0) >>> 4);
    }

    //n are the ooNo
    private byte extractN(short instruction) {
        return (byte) (instruction & 0x00F);
    }

    private byte extractNibble(short instruction, int position) {

        instruction = (short) (instruction >> position * 4);

        //Return only last 4 bits
        return (byte) (instruction & 0x000F);
    }

    public void draw(byte x, byte y, byte nibble) {

        byte readBytes = 0;


        byte vf = (byte) 0x0;
        while (readBytes < nibble) {

            byte currentByte = ram.getIndex((short) (indexRegister + readBytes)); //Read one byte
            for (int i = 0; i <= 7; i++) {
                //For every pixel

                //Calculate real coordinate
                int int_x = registers[x] & 0xFF;
                int int_y = registers[y] & 0xFF;
                int real_x = (int_x + i) % 64;
                int real_y = (int_y + readBytes) % 32;
                boolean previousPixel = screen.vRam[real_x][real_y]; //Previous value of pixel
                boolean newPixel = previousPixel ^ isBitSet(currentByte, 7 - i); //XOR
                screen.vRam[real_x][real_y] = newPixel;
                if (previousPixel && !newPixel) {
                    //A pixel has been erased
                    vf = (byte) 0x01;
                }
            }

            registers[0xF] = vf; //Set Vf. Will be 1 if a pixel has been erased
            readBytes++;
        }

    }

    private Boolean isBitSet(byte b, int bit) {
        return (b & (1 << bit)) != 0;
    }
}
