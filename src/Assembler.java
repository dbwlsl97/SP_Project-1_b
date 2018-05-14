import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
	ArrayList<SymbolTable> literalList;

	ArrayList<String> codeList;
	static int section;
	int end_sec ;
	int[] sec;
	
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
		literalList = new ArrayList<SymbolTable>();
		
	}

	/** 
	 * ��U���� ���� ��ƾ
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");
		
		assembler.pass1();
		assembler.printSymbolTable("symtab_20160273.txt");
		
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
		File file = new File(fileName);
		FileWriter symbol = null;

		try {
		symbol = new FileWriter(file, true);
		for(int i=0;i<symtabList.size();i++) {
			for(int j=0;j<symtabList.get(i).symbolList.size();j++) {
			symbol.write(symtabList.get(i).symbolList.get(j)+"\t"+Integer.toHexString(symtabList.get(i).locationList.get(j)).toUpperCase() + "\r\n");
			symbol.flush();
			
			}
		}
		}catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
			if(symbol!=null) symbol.close();
			} catch(IOException e) {
			e.printStackTrace();
			}
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
		sec = new int[lineList.size()];
		for(int i=0;i<lineList.size();i++) {
			i_line[i] = lineList.get(i);
			if(i_line[i].contains(".")) {
				continue;
			}
			for(int j=0;j<l_token.length;j++) {
				l_token = i_line[i].split("\t",4);
			if(l_token[j].equals("START")) {
				symtabList.add(new SymbolTable());
				literalList.add(new SymbolTable());
				TokenList.add(new TokenTable(symtabList.get(section),instTable,literalList.get(section)));
			}
			if(l_token[j].equals("CSECT")) {
				section++;
				literalList.add(new SymbolTable());
				symtabList.add(new SymbolTable());
				TokenList.add(new TokenTable(symtabList.get(section),instTable,literalList.get(section)));			
				}

			}
			
			TokenList.get(section).putToken(i_line[i]);
		}
				
	}

	
	/**
	 * pass2 ������ �����Ѵ�.<br>
	 *   1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		// TODO Auto-generated method stub
		String output = "";
		String loc = "";
		for(int i=0;i<TokenList.size();i++) {
			for(int j=0;j<TokenList.get(i).tokenList.size();j++) {
				Token t = TokenList.get(i).tokenList.get(j);
				if(t.operator.equals("START")||(t.operator.equals("CSECT"))) {
					loc = String.format("%06X", symtabList.get(i).locationList.get(j));
					output = "H"+t.label+"\t"+loc;
				}
				else if(t.operator.contains("EXTDEF")) {
					output += "\nD";
						for(int a=0;a<t.operand.length;a++) {
							loc = String.format("%06X", symtabList.get(i).search(t.operand[a])).toUpperCase();
							output += t.operand[a]+loc;
					}
				}
				else if(t.operator.contains("EXTREF")) {
					output += "\nR";
					for(int a=0;a<t.operand.length;a++) {
						loc = t.operand[a];
						output += loc;
					}
				}
				else if(t.label.equals("FIRST")) {
					
					output +="\nT";
					loc =String.format("%06X", symtabList.get(i).search(t.label));
					output += loc;
					for(int a=0;a<TokenList.get(i).tokenList.size();a++) 
						TokenList.get(i).makeObjectCode(a);
						loc = String.format("%02X", t.byteSize);
					output += loc;
					
					output += t.objectCode;
					
					if(Integer.parseInt(loc)<=30) {
	//					output += "\n";
					}
				}
				
				
			}
			System.out.println(output);
			
		}
//		for(int i=0;i<TokenList.size();i++) {
//			for(int j=0;j<TokenList.get(i).tokenList.size();j++) {
//				TokenList.get(i).makeObjectCode(j);
//				codeList.add(TokenList.get(i).tokenList.get(j).objectCode);
//			}
//		}
		
		for(int i=0;i<codeList.size();i++){
			System.out.println(codeList.get(i));
		}
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
//for(int i=0;i<codeList.size();i++){
//if(i==0) {
//	codeList.set(0,"H")
//}
//}
