import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Assembler : 
 * �� ���α׷��� SIC/XE �ӽ��� ���� Assembler ���α׷��� ���� ��ƾ�̴�.
 * ���α׷��� ���� �۾��� ������ ����. <br>
 * 1) ó�� �����ϸ� Instruction ���� �о�鿩�� assembler�� �����Ѵ�. <br>
 * 2) ����ڰ� �ۼ��� input ������ �о���� �� �����Ѵ�. <br>
 * 3) input ������ ������� �ܾ�� �����ϰ� �ǹ̸� �ľ��ؼ� �����Ѵ�. (pass1) <br>
 * 4) �м��� ������ �������� ��ǻ�Ͱ� ����� �� �ִ� object code�� �����Ѵ�. (pass2) <br>
 * 
 * <br><br>
 * �ۼ����� ���ǻ��� : <br>
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� �ȵȴ�.<br>
 *  2) ���������� �ۼ��� �ڵ带 �������� ������ �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br>
 * 
 * <br><br>
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */
public class Assembler {
	/** instruction ���� ������ ���� */
	InstTable instTable;
	/** �о���� input ������ ������ �� �� �� �����ϴ� ����. */
	ArrayList<String> lineList;
	/** ���α׷��� section���� symbol table�� �����ϴ� ����*/
	ArrayList<SymbolTable> symtabList;
	/** ���α׷��� section���� ���α׷��� �����ϴ� ����*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, �Ǵ� ���þ ���� ������� ������Ʈ �ڵ���� ��� ���·� �����ϴ� ����. <br>
	 * �ʿ��� ��� String ��� ������ Ŭ������ �����Ͽ� ArrayList�� ��ü�ص� ������.
	 */
	ArrayList<String> codeList;
	
	/**
	 * Ŭ���� �ʱ�ȭ. instruction Table�� �ʱ�ȭ�� ���ÿ� �����Ѵ�.
	 * 
	 * @param instFile : instruction ���� �ۼ��� ���� �̸�. 
	 * @throws IOException 
	 */
	public Assembler(String instFile) throws IOException {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/** 
	 * ��U���� ���� ��ƾ
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");
		
		assembler.pass1();
		assembler.printSymbolTable("symtab_20160273");
		
		assembler.pass2();
		assembler.printObjectCode("output_00000000");
		
	}

	/**
	 * �ۼ��� codeList�� ������¿� �°� ����Ѵ�.<br>
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printObjectCode(String fileName) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.<br>
	 * @param fileName : ����Ǵ� ���� �̸�
	 * @throws FileNotFoundException 
	 */
	private void printSymbolTable(String fileName) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
//		FileOutputStream f_sym = new FileOutputStream(fileName+".txt");
		SymbolTable s = new SymbolTable();
		s.symbolList = new ArrayList<String>();
		s.locationList = new ArrayList<Integer>();
		Iterator it = s.symbolList.iterator();
		int loc = 0;
		String sym= "";
		while (it.hasNext()) {
		    sym = (String)it.next();
		     System.out.print(sym+"\t");
		}
		Iterator it2 = s.locationList.iterator();
		while (it2.hasNext()) {
		    loc = (int)it2.next();
		     System.out.print(loc+"\n");
		}
	}

	/** 
	 * pass1 ������ �����Ѵ�.<br>
	 *   1) ���α׷� �ҽ��� ��ĵ�Ͽ� ��ū������ �и��� �� ��ū���̺� ����<br>
	 *   2) label�� symbolTable�� ����<br>
	 *   <br><br>
	 *    ���ǻ��� : SymbolTable�� TokenTable�� ���α׷��� section���� �ϳ��� ����Ǿ�� �Ѵ�.
	 */
	private void pass1() {
		// TODO Auto-generated method stub
		
		String[] i_line = new String[lineList.size()]; //lineList�� �� �پ� ���� ��
		String[] l_token = new String[4]; //�� input line�� ���� �������� �ڸ����� ���� ��	
		SymbolTable symTab = new SymbolTable();
		TokenTable toTab = new TokenTable(symTab,instTable);
//		TokenList.add(toTab);
		for(int i=0;i<i_line.length;i++) {
			i_line[i] = lineList.get(i);
			if(i_line[i].contains(".")) {
				continue;
//				i_line[i] = ".\t\t\t";
			}
			for(int j=0;j<l_token.length;j++) {
				l_token = i_line[i].split("\t",4);

			if((l_token[j].equals("START"))||(l_token[j].equals("CSECT"))) {
				toTab.locctr =0;
				TokenList.add(toTab);
				symtabList.add(symTab);
			}
			}
//			System.out.println(i_line[i]);
			toTab.putToken(i_line[i]);
//			System.out.println(toTab.getToken(i).operator);
		}

		
	}

	
	/**
	 * pass2 ������ �����Ѵ�.<br>
	 *   1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * inputFile�� �о�鿩�� lineList�� �����Ѵ�.<br>
	 * @param inputFile : input ���� �̸�.
	 * @throws IOException 
	 */
	private void loadInputFile(String inputFile) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader rInput = new BufferedReader(new FileReader("./input.txt"));
		while(true) {
			String rline = rInput.readLine();
			if(rline==null) break;
			lineList.add(rline);

		}
	}
	
}
