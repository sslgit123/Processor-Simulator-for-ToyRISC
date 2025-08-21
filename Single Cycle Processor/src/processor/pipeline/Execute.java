package processor.pipeline;
import generic.Instruction;
import generic.Operand.OperandType;
import processor.Processor;

public class Execute {
    Processor containingProcessor;
    OF_EX_LatchType OF_EX_Latch;
    EX_MA_LatchType EX_MA_Latch;
    EX_IF_LatchType EX_IF_Latch;
    private boolean b;

    public Execute(Processor containingProcessor, OF_EX_LatchType oF_EX_Latch, EX_MA_LatchType eX_MA_Latch, EX_IF_LatchType eX_IF_Latch) {
        this.containingProcessor = containingProcessor;
        this.OF_EX_Latch = oF_EX_Latch;
        this.EX_MA_Latch = eX_MA_Latch;
        this.EX_IF_Latch = eX_IF_Latch;
    }
    
    public void performEX() {
        boolean jmpRes = false;
        if (OF_EX_Latch.isEX_enable()) {
            Instruction instruction = OF_EX_Latch.getInstruction();
            EX_MA_Latch.setInstruction(instruction);
            String opType = instruction.getOperationType().toString();
            int nowPc = containingProcessor.getRegisterFile().programCounter - 1;
            int aluResult = 0;
            
            // Check if the instruction is of the immediate type (R2I arithmetic, load or store)
            b = opType.endsWith("i") || opType.equals("load") || opType.equals("store");
            if (b) {
                int rs1 = containingProcessor.getRegisterFile().getValue(instruction.getSourceOperand1().getValue());
                int immed = instruction.getSourceOperand2().getValue();
                // For most instructions, a destination value is read. 
                // (Note: In the modified store, we use rs1 as the base register.)
                int rd = containingProcessor.getRegisterFile().getValue(instruction.getDestinationOperand().getValue());
                switch (opType) {
                    case "addi":
                        aluResult = rs1 + immed;
                        break;
                    case "subi":
                        aluResult = rs1 - immed;
                        break;
                    case "muli":
                        aluResult = rs1 * immed;
                        break;
                    case "divi":
                        aluResult = rs1 / immed;
                        containingProcessor.getRegisterFile().setValue(31, rs1 % immed);
                        break;
                    case "andi":
                        aluResult = rs1 & immed;
                        break;
                    case "ori":
                        aluResult = rs1 | immed;
                        break;
                    case "xori":
                        aluResult = rs1 ^ immed;
                        break;
                    case "slti":
                        // Changed comparison: set aluResult to 1 if rs1 is less than immed.
                        aluResult = (rs1 < immed) ? 1 : 0;
                        break;
                    case "slli":
                        aluResult = rs1 << immed;
                        break;
                    case "srli":
                        aluResult = rs1 >>> immed;
                        break;
                    case "srai":
                        aluResult = rs1 >> immed;
                        break;
                    case "load":
                        aluResult = rs1 + immed;
                        break;
                    case "store":
                        // Modified store: use base register (rs1) instead of rd.
                        aluResult = rs1 + immed;
                        break;
                    default:
                        System.out.print("Issue detected in Execute.java, switch(OpType) for R2I");
                        break;
                }
            } else if (opType.equals("add") || opType.equals("sub") || opType.equals("mul") ||
                       opType.equals("div") || opType.equals("and") || opType.equals("or") ||
                       opType.equals("xor") || opType.equals("slt") || opType.equals("sll") ||
                       opType.equals("srl") || opType.equals("sra")) {
                int rs1 = containingProcessor.getRegisterFile().getValue(instruction.getSourceOperand1().getValue());
                int rs2 = containingProcessor.getRegisterFile().getValue(instruction.getSourceOperand2().getValue());
                switch (opType) {
                    case "add":
                        aluResult = rs1 + rs2;
                        break;
                    case "sub":
                        aluResult = rs1 - rs2;
                        break;
                    case "mul":
                        aluResult = rs1 * rs2;
                        break;
                    case "div":
                        aluResult = rs1 / rs2;
                        containingProcessor.getRegisterFile().setValue(31, rs1 % rs2);
                        break;
                    case "and":
                        aluResult = rs1 & rs2;
                        break;
                    case "or":
                        aluResult = rs1 | rs2;
                        break;
                    case "xor":
                        aluResult = rs1 ^ rs2;
                        break;
                    case "slt":
                        // Using a ternary operator for clarity.
                        aluResult = (rs1 < rs2) ? 1 : 0;
                        break;
                    case "sll":
                        aluResult = rs1 << rs2;
                        break;
                    case "srl":
                        aluResult = rs1 >>> rs2;
                        break;
                    case "sra":
                        aluResult = rs1 >> rs2;
                        break;
                    default:
                        System.out.print("Issue detected in R3 type switch");
                        break;
                }
            } else if (opType.equals("jmp")) {
                OperandType jmpType = instruction.getDestinationOperand().getOperandType();
                int immed = (jmpType == OperandType.Immediate) ? 
                            instruction.getDestinationOperand().getValue() : 
                            containingProcessor.getRegisterFile().getValue(instruction.getDestinationOperand().getValue());
                aluResult = immed + nowPc;
                jmpRes = true;
                EX_IF_Latch.setIsBranch_enable(true, aluResult);
            } else if (opType.equals("end")) {
                // End instruction logic; if needed, add termination steps here.
            } else {
                // Handle branch instructions: beq, bgt, bne, blt.
                int rs1 = containingProcessor.getRegisterFile().getValue(instruction.getSourceOperand1().getValue());
                int rd = containingProcessor.getRegisterFile().getValue(instruction.getSourceOperand2().getValue());
                int immed = instruction.getDestinationOperand().getValue();
                switch (opType) {
                    case "beq":
                        if (rs1 == rd) {
                            aluResult = nowPc + immed;
                            jmpRes = true;
                            EX_IF_Latch.setIsBranch_enable(true, aluResult);
                        }
                        break;
                    case "bgt":
                        if (rs1 > rd) {
                            aluResult = nowPc + immed;
                            jmpRes = true;
                            EX_IF_Latch.setIsBranch_enable(true, aluResult);
                        }
                        break;
                    case "bne":
                        if (rs1 != rd) {
                            aluResult = nowPc + immed;
                            jmpRes = true;
                            EX_IF_Latch.setIsBranch_enable(true, aluResult);
                        }
                        break;
                    case "blt":
                        if (rs1 < rd) {
                            aluResult = nowPc + immed;
                            jmpRes = true;
                            EX_IF_Latch.setIsBranch_enable(true, aluResult);
                        }
                        break;
                    default:
                        System.out.print("Issue detected in R2I type, for branch statements");
                        break;
                }
            }
            EX_MA_Latch.setaluResult(aluResult);
        }
        OF_EX_Latch.setEX_enable(false);
        if (!jmpRes) {
            EX_MA_Latch.setMA_enable(true);
        }
    }
}