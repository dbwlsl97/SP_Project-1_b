import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.*;
import java.util.*;
import java.io.*;

/**
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ�. <br>
 * ���� instruction ���� ����, ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/** 
	 * inst.data ������ �ҷ��� �����ϴ� ����.
	 *  ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap;	
	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * @param instFile : instuction�� ���� ���� ����� ���� �̸�
	 * @throws IOException 
	 */
	public InstTable(String instFile) throws IOException {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
		
	}
	
	/**
	 * �Է¹��� �̸��� ������ ���� �ش� ������ �Ľ��Ͽ� instMap�� �����Ѵ�.
	 * @throws IOException 
	 */
	public void openFile(String fileName) throws IOException { //inst.dat�� �� �� �� �ҷ�����
		BufferedReader rInst = new BufferedReader(new FileReader("./inst.data")); 
		while(true) {
			String rline = rInst.readLine();
			if(rline==null) break;
			Instruction inst = new Instruction(rline);
			//���� �߶� instMap�� �־��ֱ�
			instMap.put(inst.instruction, inst);					
		}
		rInst.close();
	}
	
	//get, set, search ���� �Լ��� ���� ����

}
/**
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����.
 * instruction�� ���õ� �������� �����ϰ� �������� ������ �����Ѵ�.
 */
class Instruction {
	 String instruction;
	 int opcode;
	 int numberOfOperand;
	 String comment;	 
	
	/** instruction�� �� ����Ʈ ��ɾ����� ����. ���� ���Ǽ��� ���� */
	int format;
	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void parsing(String line) {
		// TODO Auto-generated method stub
		String[] inst_token = line.split("\t");
		instruction = inst_token[0];
		if(inst_token[1].equals("3/4")) { 
			format = 3; // ������ 3/4 �� ��, 3���� ��������
		}
		else {
			format = Integer.parseInt(inst_token[1]);
		}
		opcode = Integer.parseInt(inst_token[2],16); //opcode��  16������ �̿��ϱ� ����
		numberOfOperand = Integer.parseInt(inst_token[3]);
	}

	
	
}
