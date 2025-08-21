package generic;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import processor.Clock;
import processor.Processor;

public class Simulator {

    static Processor processor;
    static boolean simulationComplete;

    public static void setupSimulation(String assemblyProgramFile, Processor p) {
        Simulator.processor = p;
        simulationComplete = false; // Ensure simulation flag is reset before loading the program
        loadProgram(assemblyProgramFile);
    }

    static void loadProgram(String assemblyProgramFile) {
        /*
         * TOD0
         * 1. load the program into memory according to the program layout described
         *    in the ISA specification
         * 2. set PC to the address of the first instruction in the main
         * 3. set the following registers:
         *     x0 = 0
         *     x1 = 65535
         *     x2 = 65535
         */
        try (InputStream inp = new FileInputStream(assemblyProgramFile);
             DataInputStream data = new DataInputStream(inp)) {

            if (data.available() > 0) {
                int next = data.readInt();
                processor.getRegisterFile().setProgramCounter(next);
            }

            int address = 0;
            while (data.available() > 0) {
                int next = data.readInt();
                processor.getMainMemory().setWord(address, next);
                address++;
            }

            processor.getRegisterFile().setValue(0, 0);
            processor.getRegisterFile().setValue(1, 65535);
            processor.getRegisterFile().setValue(2, 65535);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void simulate() {
        int numinst = 0;
        int numcycles = 0;
        while (!simulationComplete) {
            processor.getIFUnit().performIF();
            Clock.incrementClock();

            processor.getOFUnit().performOF();
            Clock.incrementClock();

            processor.getEXUnit().performEX();
            Clock.incrementClock();

            processor.getMAUnit().performMA();
            Clock.incrementClock();

            processor.getRWUnit().performRW();
            Clock.incrementClock();

            ++numinst;
            ++numcycles;
        }
        Statistics.setNumberOfInstructions(numinst);
        Statistics.setNumberOfCycles(numcycles);
    }

    public static void setSimulationComplete(boolean value) {
        simulationComplete = value;
    }
}
