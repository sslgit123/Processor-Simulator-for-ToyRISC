package processor.pipeline;

import generic.Instruction;
import generic.Simulator;
import generic.Statistics;
// import generic.Instruction.OperationType;
import processor.Processor;

public class RegisterWrite {
	Processor containingProcessor;
	MA_RW_LatchType MA_RW_Latch;
	IF_EnableLatchType IF_EnableLatch;
	
	public RegisterWrite(Processor containingProcessor, MA_RW_LatchType mA_RW_Latch, IF_EnableLatchType iF_EnableLatch)
	{
		this.containingProcessor = containingProcessor;
		this.MA_RW_Latch = mA_RW_Latch;
		this.IF_EnableLatch = iF_EnableLatch;
	}
	
	public void performRW()
	{
		if(MA_RW_Latch.getIsNop())
		{
			MA_RW_Latch.setIsNop(false);
		}
		else if(MA_RW_Latch.isRW_enable())
		{
			Instruction instruction = MA_RW_Latch.getInstruction();
			String op = instruction.getOperationType().toString();
			Statistics.setnumberOfRegisterWriteInstructions(Statistics.getNumberOfRegisterWriteInstructions() + 1);
			if(op.equals("load"))
			{
				int ldResult = MA_RW_Latch.getldResult();
				int rd = instruction.getDestinationOperand().getValue();
				containingProcessor.getRegisterFile().setValue(rd, ldResult);
			}
			else if(op.equals("store")||op.equals("jmp")||op.equals("beq")||op.equals("blt")||op.equals("bne")||op.equals("bgt"))
			{
			}
			else if(op.equals("end"))
			{
				Simulator.setSimulationComplete(true);
			}
			else
			{
				int aluResult = MA_RW_Latch.getaluResult();
				int rd = instruction.getDestinationOperand().getValue();
				containingProcessor.getRegisterFile().setValue(rd, aluResult);
			}
						
			MA_RW_Latch.setRW_enable(false);
			if(!op.equals("end"))
			IF_EnableLatch.setIF_enable(true);
		}
	}

}