package processor.pipeline;

import generic.Instruction;
import generic.Simulator;
import generic.Instruction.OperationType;
import processor.Processor;

public class RegisterWrite {
    Processor containingProcessor;
    MA_RW_LatchType MA_RW_Latch;
    IF_EnableLatchType IF_EnableLatch;
    
    public RegisterWrite(Processor containingProcessor, MA_RW_LatchType mA_RW_Latch, IF_EnableLatchType iF_EnableLatch) {
        this.containingProcessor = containingProcessor;
        this.MA_RW_Latch = mA_RW_Latch;
        this.IF_EnableLatch = iF_EnableLatch;
    }
    
    public void performRW() {
        
        if (!MA_RW_Latch.isRW_enable()) {
            return;
        }
        
        Instruction instruction = MA_RW_Latch.getInstruction();
        String op = instruction.getOperationType().toString();
        int rd;  
        
        switch(op) {
            case "load": {
                int ldResult = MA_RW_Latch.getldResult();
                rd = instruction.getDestinationOperand().getValue();
                containingProcessor.getRegisterFile().setValue(rd, ldResult);
                break;
            }
            case "store":
            case "jmp":
            case "beq":
            case "blt":
            case "bne":
            case "bgt":
                break;
            case "end":
                Simulator.setSimulationComplete(true);
                break;
            default: {
                int aluResult = MA_RW_Latch.getaluResult();
                rd = instruction.getDestinationOperand().getValue();
                containingProcessor.getRegisterFile().setValue(rd, aluResult);
                break;
            }
        }
        
        MA_RW_Latch.setRW_enable(false);
        IF_EnableLatch.setIF_enable(true);
    }
}