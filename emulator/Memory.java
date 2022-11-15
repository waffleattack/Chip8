package emulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


class Memory{
  private byte[] ram;
  private short counter = 0;
  private short ROM_START = 0x200;
  private  short[] fonts = {
	0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
	0x20, 0x60, 0x20, 0x20, 0x70, // 1
	0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
	0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
	0x90, 0x90, 0xF0, 0x10, 0x10, // 4
	0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
	0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
	0xF0, 0x10, 0x20, 0x40, 0x40, // 7
	0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
	0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
	0xF0, 0x90, 0xF0, 0x90, 0x90, // A
	0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
	0xF0, 0x80, 0x80, 0x80, 0xF0, // C
	0xE0, 0x90, 0x90, 0x90, 0xE0, // D
	0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
	0xF0, 0x80, 0xF0, 0x80, 0x80  // F
  };
  public Memory(int size){
    ram = new byte[size];

        for(short bit : fonts){
        ram[counter] =(byte) bit;
        counter++;
      }
  }
  public byte getIndex(short index){
    return ram[index];
  }
  public void setIndex(short index, byte input){
    ram[index] = input;
  }

  public void setIndex(int index,int input){ ram[index] = (byte) input;}
  public void dumpMemory(int slice){
    String[] temp = new String[ram.length];
    for(int n = 0; n < ram.length; n++){
        temp[n] = String.format("0x%02X", ram[n]);
    }
    for(int i = 0; i<=ram.length; i+= slice){
      System.out.printf("0x%04X",  i);
      System.out.println(Arrays.toString(Arrays.copyOfRange(temp, i, i+slice)));
    }
  }
  public void loadRom(String input){
    try{
      byte[] array = Files.readAllBytes(Paths.get(input));

        counter = ROM_START;
        for(byte bit : array){
        ram[counter] = bit;
        counter++;
      }
    }
    catch(IOException e){
      e.printStackTrace();
      }
  }
}
