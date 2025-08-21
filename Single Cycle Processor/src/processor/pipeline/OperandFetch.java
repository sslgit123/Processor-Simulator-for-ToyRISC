package processor.pipeline;

import processor.Processor;
import generic.Instruction;
import generic.Operand;
import generic.Instruction.OperationType;
import generic.Operand.OperandType;

public class OperandFetch {
    Processor containingProcessor;
    IF_OF_LatchType IF_OF_Latch;
    OF_EX_LatchType OF_EX_Latch;

    public OperandFetch(Processor containingProcessor, IF_OF_LatchType iF_OF_Latch, OF_EX_LatchType oF_EX_Latch) {
        this.containingProcessor = containingProcessor;
        this.IF_OF_Latch = iF_OF_Latch;
        this.OF_EX_Latch = oF_EX_Latch;
    }

    // Rewritten two's complement function using a conventional flip-and-add-one approach.
    public String twosComplement(String bin) {
        StringBuilder ones = new StringBuilder();
        for (int i = 0; i < bin.length(); i++) {
            ones.append(flip(bin.charAt(i)));
        }
        int carry = 1;
        for (int i = ones.length() - 1; i >= 0; i--) {
            if (ones.charAt(i) == '1' && carry == 1) {
                ones.setCharAt(i, '0');
            } else if (carry == 1) {
                ones.setCharAt(i, '1');
                carry = 0;
                break;
            }
        }
        if (carry == 1) {
            ones.insert(0, '1');
        }
        return ones.toString();
    }

    public char flip(char c) {
        return (c == '0') ? '1' : '0';
    }

    public void performOF() {
        if (IF_OF_Latch.isOF_enable()) {
            Instruction newInst = new Instruction();
            // Use String.format to get a 32-bit binary string.
            String newInstruction = String.format("%32s", Integer.toBinaryString(IF_OF_Latch.getInstruction()))
                                        .replace(' ', '0');
            String opCode = newInstruction.substring(0, 5);
            OperationType[] opConv = OperationType.values();
            OperationType opInst = opConv[Integer.parseInt(opCode, 2)];
            newInst.setOperationType(opInst);

            switch (opInst) {
                // R3-type instructions: three registers.
                case add:
                case sub:
                case mul:
                case div:
                case and:
                case or:
                case xor:
                case slt:
                case sll:
                case srl:
                case sra: {
                    Operand rs1 = new Operand();
                    rs1.setOperandType(OperandType.Register);
                    Operand rs2 = new Operand();
                    rs2.setOperandType(OperandType.Register);
                    Operand rd = new Operand();
                    rd.setOperandType(OperandType.Register);
                    rs1.setValue(Integer.parseInt(newInstruction.substring(5, 10), 2));
                    rs2.setValue(Integer.parseInt(newInstruction.substring(10, 15), 2));
                    rd.setValue(Integer.parseInt(newInstruction.substring(15, 20), 2));
                    newInst.setSourceOperand1(rs1);
                    newInst.setSourceOperand2(rs2);
                    newInst.setDestinationOperand(rd);
                    break;
                }
                // R2I-type instructions: one register and an immediate.
                case addi:
                case andi:
                case muli:
                case ori:
                case slli:
                case slti:
                case srai:
                case srli:
                case subi:
                case xori:
                case divi:
                case store:
                case load: {
                    Operand rs1 = new Operand();
                    rs1.setOperandType(OperandType.Register);
                    Operand rs2 = new Operand();
                    rs2.setOperandType(OperandType.Register);
                    rs1.setValue(Integer.parseInt(newInstruction.substring(5, 10), 2));
                    rs2.setValue(Integer.parseInt(newInstruction.substring(10, 15), 2));
                    
                    String immedStr = newInstruction.substring(15, 32);
                    Operand immedOp = new Operand();
                    immedOp.setOperandType(OperandType.Immediate);
                    int immedValue = Integer.parseInt(immedStr, 2);
                    if (immedStr.charAt(0) == '1') {
                        // Convert negative immediate using two's complement.
                        immedValue = -Integer.parseInt(twosComplement(immedStr), 2);
                    }
                    immedOp.setValue(immedValue);
                    
                    // For these instructions, the register read from bits 10-15 is the destination.
                    newInst.setSourceOperand1(rs1);
                    newInst.setSourceOperand2(immedOp);
                    newInst.setDestinationOperand(rs2);
                    break;
                }
                // jmp instruction: register and immediate.
                case jmp: {
                    Operand rd = new Operand();
                    rd.setOperandType(OperandType.Register);
                    rd.setValue(Integer.parseInt(newInstruction.substring(5, 10), 2));
                    newInst.setSourceOperand1(rd);
                    
                    String immedStr = newInstruction.substring(10, 32);
                    Operand immedOp = new Operand();
                    immedOp.setOperandType(OperandType.Immediate);
                    int immedValue = Integer.parseInt(immedStr, 2);
                    if (immedStr.charAt(0) == '1') {
                        immedValue = -Integer.parseInt(twosComplement(immedStr), 2);
                    }
                    immedOp.setValue(immedValue);
                    newInst.setDestinationOperand(immedOp);
                    break;
                }
                // Branch instructions: two registers and an immediate.
                case beq:
                case bgt:
                case blt:
                case bne: {
                    Operand rs1 = new Operand();
                    rs1.setOperandType(OperandType.Register);
                    Operand rs2 = new Operand();
                    rs2.setOperandType(OperandType.Register);
                    rs1.setValue(Integer.parseInt(newInstruction.substring(5, 10), 2));
                    rs2.setValue(Integer.parseInt(newInstruction.substring(10, 15), 2));
                    
                    String immedStr = newInstruction.substring(15, 32);
                    Operand immedOp = new Operand();
                    immedOp.setOperandType(OperandType.Immediate);
                    int immedValue = Integer.parseInt(immedStr, 2);
                    if (immedStr.charAt(0) == '1') {
                        immedValue = -Integer.parseInt(twosComplement(immedStr), 2);
                    }
                    immedOp.setValue(immedValue);
                    
                    newInst.setSourceOperand1(rs1);
                    newInst.setSourceOperand2(rs2);
                    newInst.setDestinationOperand(immedOp);
                    break;
                }
                case end:
                    // No operands to fetch for the 'end' instruction.
                    break;
                default:
                    break;
            }
            OF_EX_Latch.setInstruction(newInst);
            IF_OF_Latch.setOF_enable(false);
            OF_EX_Latch.setEX_enable(true);
        }
    }
}