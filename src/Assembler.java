import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
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
	int[] sec_arr;
	
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
		assembler.printObjectCode("output_20160273.txt");
		
	}

	/**
	 * �ۼ��� codeList�� ������¿� �°� ����Ѵ�.<br>
	 * @param fileName : ����Ǵ� ���� �̸�
	 * @throws FileNotFoundException 
	 */
	private void printObjectCode(String fileName) throws FileNotFoundException {
		// TODO Auto-generated method stub
		File file = new File(fileName);

		
		/* �ڹ� output ���� ���� */
		BufferedWriter output =null; 
		try {
			output= new BufferedWriter(new FileWriter(file)); 
		for(int i=0;i<TokenList.size();i++) {
			output.write(codeList.get(i)+"\r\n");
			
		}
		}catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
			if(output!=null) output.close();
			} catch(IOException e) {
			e.printStackTrace();
			}
		}
	}
	
	/**
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.<br>
	 * @param fileName : ����Ǵ� ���� �̸�
	 * @throws FileNotFoundException 
	 */
	private void printSymbolTable(String fileName) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		/* �ڹ� symboltable ���� ���� */
		File file = new File(fileName);
		BufferedWriter symbol =null;
		try {
		symbol = new BufferedWriter(new FileWriter(file));
		for(int i=0;i<symtabList.size();i++) {
			for(int j=0;j<symtabList.get(i).symbolList.size();j++) {
			symbol.write(symtabList.get(i).symbolList.get(j)+"\t"+Integer.toHexString(symtabList.get(i).locationList.get(j)).toUpperCase() + "\r\n");
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
		sec_arr = new int[lineList.size()];
		for(int i=0;i<lineList.size();i++) { 
			i_line[i] = lineList.get(i);
			if(i_line[i].contains(".")) {
				continue;
			}
			for(int j=0;j<l_token.length;j++) {  // ù label�� START�� CSECT �̸� ���  �ɺ�,���ͷ�,��ū ����Ʈ�� ���� (���� ������ ����) 
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
			
			TokenList.get(section).putToken(i_line[i]); //�� ���� �� input.txt �� �پ� �־��ֱ�
		}
				
	}
	
	/**
	 * pass2 ������ �����Ѵ�.<br>
	 *   1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		// TODO Auto-generated method stub
		String output = "";
		int litloc =0;
		String H_code = "";
		String lit_code = "";
		String T_code ="";
		String M_code = "";
		String loc = "";
		String leng = "";
		int total_leng =0;
		int[] addr = new int[TokenList.size()];
		int end = 0;

		for(int c=0;c<TokenList.size();c++) { // �ش� ���ǿ� ���� ��ü ���� ���ϱ�
			for(int d=0;d<TokenList.get(c).tokenList.size();d++) {
				Token a = TokenList.get(c).tokenList.get(d);
				if(d==TokenList.get(c).tokenList.size()-1) {
					if(c==0) // ���� 0���� BUFEND �ּҰ�
						end = symtabList.get(c).search("BUFEND");	
					else //�� �� ������ ���� �ּ� + byteSize 
						end = a.location + a.byteSize;
					 addr[c] = end; //�迭�� �ֱ�
					break;
				}		
			}

		}
		for(int i=0;i<TokenList.size();i++) { // ���, �ؽ�Ʈ, ��������̼� ���ֱ�
			for(int j=0;j<TokenList.get(i).tokenList.size();j++) {
				Token t = TokenList.get(i).tokenList.get(j);
				TokenList.get(i).makeObjectCode(j);
				
				if(t.operator.equals("START")||(t.operator.equals("CSECT"))) { // START�� CSECT ������ H ����
					loc = String.format("%06X", symtabList.get(i).locationList.get(j));
					H_code = "H"+t.label+"\t"+loc;
					if(i==0) //������ ���� �ش� ���ǿ� ���� ��ü ���� �ֱ�
						H_code += String.format("%06X", addr[0]);
					else if(i==1)
						H_code += String.format("%06X", addr[1]);
					else
						H_code += String.format("%06X", addr[2]);

					
					loc=String.format("%X", 0);	// 0 ���� �ʱ�ȭ
					
				}
				else if(t.operator.contains("EXTDEF")) { //EXTDEF ó��
					H_code += "\nD";
						for(int a=0;a<t.operand.length;a++) {
							loc = String.format("%06X", symtabList.get(i).search(t.operand[a])).toUpperCase();
							H_code += t.operand[a]+loc;
					}
				}
				else if(t.operator.contains("EXTREF")) { //EXTREF ó��
					H_code += "\nR";
					for(int a=0;a<t.operand.length;a++) {
						loc = t.operand[a];
						H_code += loc+"";
					}
					H_code +="\n";
					
				}
				
				else if(t.operator.contains("+")) { // 4���� M ó��
					loc = String.format("%06X", t.location+1);
					
					int count =0;
					int fromIndex = -1;
					while((fromIndex = t.objectCode.indexOf("0",fromIndex+1))>=0) {
						count++;
					}
					
					M_code+="M"+loc+String.format("%02X", count)+"+"+t.operand[0]+"\n";

				}
				
				else if(t.operator.equals("WORD")) { // WORD M ó��
					loc = String.format("%06X", t.location);
					int count =0;
					int fromIndex = -1;
					while((fromIndex = t.objectCode.indexOf("0",fromIndex+1))>=0) {
						count++;
					}
					t.operand = t.operand[0].split("-");
					M_code+="M"+loc+String.format("%02X", count)+"+"+t.operand[0]+"\n";
					M_code+="M"+loc+String.format("%02X", count)+"-"+t.operand[1]+"\n";
					
				}
				
				else if(t.location==0&&!t.objectCode.isEmpty()){ // ���� �ּҰ� 0�̰�, �����ڵ尡 �ִٸ� T ���ֱ�
					loc = String.format("%06X",TokenList.get(i).tokenList.get(j).location); //�ּ� ��������
					output += "T"+loc; // output ������ �����ּҿ� ������Ʈ �ڵ� ���� ���̸� ����
					for(int m=0;m<TokenList.get(i).tokenList.size();m++) {
						TokenList.get(i).makeObjectCode(m);
						
						leng = Integer.toString(TokenList.get(i).tokenList.get(m).byteSize);
						
						if(total_leng+Integer.parseInt(leng)>30) { // ���±����� byte �� byteSize + ���� ���� byteSize�� 30�� �Ѿ�� ���� �ֱ�
						
							output += String.format("%02X", total_leng)+T_code;
							total_leng = 0;
							loc = String.format("%06X", TokenList.get(i).tokenList.get(m).location);
							output += "\nT"+loc;
							T_code = "";
						}
						total_leng += Integer.parseInt(leng);
						T_code +=  TokenList.get(i).tokenList.get(m).objectCode;
						
					}
					output += String.format("%02X", total_leng)+T_code+"\n"; // ���� �� ���̿� Text �ֱ�
		

				}
				else if(t.operand[0].contains("=")) { //���ͷ� ó�� �κ� 
					for(int a=0;a<TokenList.get(i).tokenList.size();a++) {
						if(literalList.get(a).search(t.operand[0])!=-1) { 
							litloc = literalList.get(a).search(t.operand[0]); //���ͷ� ���̺� �˻�
							break;
						}
					
					}
					for(int b=0;b<TokenList.get(i).tokenList.size();b++) { 
						if(litloc==TokenList.get(i).tokenList.get(b).location&&litloc!=0) { 
							if(t.operand[0].contains("=C")) // ������ �� ���ͷ�ó��
							lit_code += "T"+String.format("%06X", litloc)+String.format("%02X", TokenList.get(i).tokenList.get(b).litSize)
										+TokenList.get(i).tokenList.get(b).literal+"\n";	
						}
					}
				}
				
			}
			codeList.add(H_code+output+lit_code+M_code+"E"); // codeList�� ���� ���� ������
			
		/* codeList�� �߰����� code ���� �ʱ�ȭ ���� */
			M_code = ""; 
			output = "";
			T_code = "";
			lit_code = "";
			H_code = "";
			total_leng=0;
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
		//Input.txt �ҷ����� 
		BufferedReader rInput = new BufferedReader(new FileReader("./input.txt"));
		while(true) {
			String rline = rInput.readLine();
			if(rline==null) break;
			lineList.add(rline);
		}
	}
	
}
