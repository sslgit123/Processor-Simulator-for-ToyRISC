package processor.pipeline;

import processor.Processor;
import generic.Instruction;

public class MemoryAccess {
	Processor containingProcessor;
	EX_MA_LatchType EX_MA_Latch;
	MA_RW_LatchType MA_RW_Latch;
	
	public MemoryAccess(Processor containingProcessor, EX_MA_LatchType eX_MA_Latch, MA_RW_LatchType mA_RW_Latch)
	{
		this.containingProcessor = containingProcessor;
		this.EX_MA_Latch = eX_MA_Latch;
		this.MA_RW_Latch = mA_RW_Latch;
	}
	
	public void performMA()
	{
		if(EX_MA_Latch.isMA_enable())
		{
			Instruction instruction = EX_MA_Latch.getInstruction();
			String op = instruction.getOperationType().toString();
			int alu_result = EX_MA_Latch.getaluResult();
			MA_RW_Latch.setaluResult(alu_result);

			if(op.equals("load"))
			{
				int load_result = containingProcessor.getMainMemory().getWord(alu_result);
				MA_RW_Latch.setldResult(load_result);
			}
			else if(op.equals("store"))
			{
				int rs1 = instruction.getSourceOperand1().getValue();
				int input = containingProcessor.getRegisterFile().getValue(rs1);
				containingProcessor.getMainMemory().setWord(alu_result, input);
			}

			MA_RW_Latch.setInstruction(instruction);
			EX_MA_Latch.setMA_enable(false);
			MA_RW_Latch.setRW_enable(true);
		}
	}

}
