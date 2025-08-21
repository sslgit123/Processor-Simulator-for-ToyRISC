package generic;

import java.io.*;
import java.nio.ByteBuffer;

public class Simulator {
    
    static FileInputStream programStream = null;
    
    public static void setupSimulation(String assemblyProgramFile, String objectProgramFile)
	{	
		int firstCodeAddress = ParsedProgram.parseDataSection(assemblyProgramFile);
		ParsedProgram.parseCodeSection(assemblyProgramFile, firstCodeAddress);
		ParsedProgram.printState();
	}

    public static String computeTwosComplement(String binary) {
        StringBuilder onesComplement = new StringBuilder();
        
        for (char bit : binary.toCharArray()) {
            onesComplement.append(bit == '0' ? '1' : '0');
        }
        
        StringBuilder twosComplement = new StringBuilder(onesComplement);
        boolean carry = true;
        
        for (int i = onesComplement.length() - 1; i >= 0; i--) {
            if (onesComplement.charAt(i) == '0') {
                twosComplement.setCharAt(i, '1');
                carry = false;
                break;
            } else {
                twosComplement.setCharAt(i, '0');
            }
        }
        
        if (carry) {
            twosComplement.insert(0, '1');
        }
        
        return twosComplement.toString();
    }
    
    public static void assemble(String outputFile) {
        try (BufferedOutputStream bufferedStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            bufferedStream.write(ByteBuffer.allocate(4).putInt(ParsedProgram.firstCodeAddress).array());
            
            for (int value : ParsedProgram.data) {
                bufferedStream.write(ByteBuffer.allocate(4).putInt(value).array());
            }
            
            for (Instruction instruction : ParsedProgram.code) {
                int opcode = instruction.getOperationType().ordinal();
                StringBuilder binaryString = new StringBuilder(String.format("%05d", Integer.parseInt(Integer.toBinaryString(opcode))));
                
                if (opcode == 29) {
                    binaryString.append("000000000000000000000000000");
                } else if (opcode == 24) {
                    handleOpcode24(instruction, binaryString);
                } else if (opcode <= 21 && opcode % 2 == 0) {
                    handleEvenOpcode(instruction, binaryString);
                } else if (opcode <= 23) {
                    handleOddOpcode(instruction, binaryString);
                } else {
                    handleJumpOpcode(instruction, binaryString);
                }
                
                int instructionInt = (int) Long.parseLong(binaryString.toString(), 2);
                bufferedStream.write(ByteBuffer.allocate(4).putInt(instructionInt).array());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleOpcode24(Instruction inst, StringBuilder binStr) {
        int dest = inst.getDestinationOperand().getValue();
        if (inst.getDestinationOperand().getOperandType() == Operand.OperandType.Register) {
            binStr.append(String.format("%05d", Integer.parseInt(Integer.toBinaryString(dest))));
            binStr.append("0000000000000000000000");
        } else {
            binStr.append("00000");
            int labelValue = ParsedProgram.symtab.get(inst.getDestinationOperand().getLabelValue());
            int pc = inst.getProgramCounter();
            binStr.append(formatLabelOffset(pc, labelValue, 22));
        }
    }

    private static void handleEvenOpcode(Instruction inst, StringBuilder binStr) {
        binStr.append(formatRegisterValue(inst.getSourceOperand1().getValue()));
        binStr.append(formatRegisterValue(inst.getSourceOperand2().getValue()));
        binStr.append(formatRegisterValue(inst.getDestinationOperand().getValue()));
        binStr.append("000000000000");
    }
    
    private static void handleOddOpcode(Instruction inst, StringBuilder binStr) {
        binStr.append(formatRegisterValue(inst.getSourceOperand1().getValue()));
        binStr.append(formatRegisterValue(inst.getDestinationOperand().getValue()));
        binStr.append(String.format("%017d", Integer.parseInt(Integer.toBinaryString(inst.getSourceOperand2().getValue()))));
    }
    
    private static void handleJumpOpcode(Instruction inst, StringBuilder binStr) {
        binStr.append(formatRegisterValue(inst.getSourceOperand1().getValue()));
        binStr.append(formatRegisterValue(inst.getSourceOperand2().getValue()));
        int labelValue = ParsedProgram.symtab.get(inst.getDestinationOperand().getLabelValue());
        int pc = inst.getProgramCounter();
        binStr.append(formatLabelOffset(pc, labelValue, 17));
    }

    private static String formatRegisterValue(int value) {
        return String.format("%05d", Integer.parseInt(Integer.toBinaryString(value)));
    }
    
    private static String formatLabelOffset(int pc, int labelValue, int bitSize) {
        int offset = labelValue - pc;
        String binaryOffset = Integer.toBinaryString(Math.abs(offset));
        if (offset < 0) {
            return computeTwosComplement(String.format("%0" + bitSize + "d", Integer.parseInt(binaryOffset)));
        } else {
            return String.format("%0" + bitSize + "d", Integer.parseInt(binaryOffset));
        }
    }
}
