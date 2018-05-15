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
 * 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다.
 * 프로그램의 수행 작업은 다음과 같다. <br>
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. <br>
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. <br>
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) <br>
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) <br>
 * 
 * <br><br>
 * 작성중의 유의사항 : <br>
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다.<br>
 *  2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br><br>
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간*/
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간. <br>
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<SymbolTable> literalList;

	ArrayList<String> codeList;
	static int section;
	int end_sec ;
	int[] sec_arr;
	
	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름. 
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
	 * 어셐블러의 메인 루틴
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
	 * 작성된 codeList를 출력형태에 맞게 출력한다.<br>
	 * @param fileName : 저장되는 파일 이름
	 * @throws FileNotFoundException 
	 */
	private void printObjectCode(String fileName) throws FileNotFoundException {
		// TODO Auto-generated method stub
		File file = new File(fileName);

		
		/* 자바 output 파일 생성 */
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
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.<br>
	 * @param fileName : 저장되는 파일 이름
	 * @throws FileNotFoundException 
	 */
	private void printSymbolTable(String fileName) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		/* 자바 symboltable 파일 생성 */
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
	 * pass1 과정을 수행한다.<br>
	 *   1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성<br>
	 *   2) label을 symbolTable에 정리<br>
	 *   <br><br>
	 *    주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		// TODO Auto-generated method stub
		
		String[] i_line = new String[lineList.size()]; //lineList를 한 줄씩 넣은 곳
		String[] l_token = new String[4]; //각 input line을 탭을 기준으로 자른것을 넣은 곳	
		sec_arr = new int[lineList.size()];
		for(int i=0;i<lineList.size();i++) { 
			i_line[i] = lineList.get(i);
			if(i_line[i].contains(".")) {
				continue;
			}
			for(int j=0;j<l_token.length;j++) {  // 첫 label이 START나 CSECT 이면 모든  심볼,리터럴,토큰 리스트를 증가 (섹션 구분을 위해) 
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
			
			TokenList.get(section).putToken(i_line[i]); //각 섹션 당 input.txt 한 줄씩 넣어주기
		}
				
	}
	
	/**
	 * pass2 과정을 수행한다.<br>
	 *   1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
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

		for(int c=0;c<TokenList.size();c++) { // 해당 섹션에 대한 전체 길이 구하기
			for(int d=0;d<TokenList.get(c).tokenList.size();d++) {
				Token a = TokenList.get(c).tokenList.get(d);
				if(d==TokenList.get(c).tokenList.size()-1) {
					if(c==0) // 섹션 0번은 BUFEND 주소값
						end = symtabList.get(c).search("BUFEND");	
					else //그 외 섹션은 현재 주소 + byteSize 
						end = a.location + a.byteSize;
					 addr[c] = end; //배열에 넣기
					break;
				}		
			}

		}
		for(int i=0;i<TokenList.size();i++) { // 헤더, 텍스트, 모디피케이션 써주기
			for(int j=0;j<TokenList.get(i).tokenList.size();j++) {
				Token t = TokenList.get(i).tokenList.get(j);
				TokenList.get(i).makeObjectCode(j);
				
				if(t.operator.equals("START")||(t.operator.equals("CSECT"))) { // START나 CSECT 만나면 H 쓰기
					loc = String.format("%06X", symtabList.get(i).locationList.get(j));
					H_code = "H"+t.label+"\t"+loc;
					if(i==0) //위에서 구한 해당 섹션에 대한 전체 길이 넣기
						H_code += String.format("%06X", addr[0]);
					else if(i==1)
						H_code += String.format("%06X", addr[1]);
					else
						H_code += String.format("%06X", addr[2]);

					
					loc=String.format("%X", 0);	// 0 으로 초기화
					
				}
				else if(t.operator.contains("EXTDEF")) { //EXTDEF 처리
					H_code += "\nD";
						for(int a=0;a<t.operand.length;a++) {
							loc = String.format("%06X", symtabList.get(i).search(t.operand[a])).toUpperCase();
							H_code += t.operand[a]+loc;
					}
				}
				else if(t.operator.contains("EXTREF")) { //EXTREF 처리
					H_code += "\nR";
					for(int a=0;a<t.operand.length;a++) {
						loc = t.operand[a];
						H_code += loc+"";
					}
					H_code +="\n";
					
				}
				
				else if(t.operator.contains("+")) { // 4형식 M 처리
					loc = String.format("%06X", t.location+1);
					
					int count =0;
					int fromIndex = -1;
					while((fromIndex = t.objectCode.indexOf("0",fromIndex+1))>=0) {
						count++;
					}
					
					M_code+="M"+loc+String.format("%02X", count)+"+"+t.operand[0]+"\n";

				}
				
				else if(t.operator.equals("WORD")) { // WORD M 처리
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
				
				else if(t.location==0&&!t.objectCode.isEmpty()){ // 현재 주소가 0이고, 기계어코드가 있다면 T 써주기
					loc = String.format("%06X",TokenList.get(i).tokenList.get(j).location); //주소 가져오기
					output += "T"+loc; // output 변수에 시작주소와 오브젝트 코드 라인 길이를 써줌
					for(int m=0;m<TokenList.get(i).tokenList.size();m++) {
						TokenList.get(i).makeObjectCode(m);
						
						leng = Integer.toString(TokenList.get(i).tokenList.get(m).byteSize);
						
						if(total_leng+Integer.parseInt(leng)>30) { // 여태까지의 byte 총 byteSize + 지금 들어올 byteSize가 30이 넘어가면 개행 넣기
						
							output += String.format("%02X", total_leng)+T_code;
							total_leng = 0;
							loc = String.format("%06X", TokenList.get(i).tokenList.get(m).location);
							output += "\nT"+loc;
							T_code = "";
						}
						total_leng += Integer.parseInt(leng);
						T_code +=  TokenList.get(i).tokenList.get(m).objectCode;
						
					}
					output += String.format("%02X", total_leng)+T_code+"\n"; // 라인 총 길이와 Text 넣기
		

				}
				else if(t.operand[0].contains("=")) { //리터럴 처리 부분 
					for(int a=0;a<TokenList.get(i).tokenList.size();a++) {
						if(literalList.get(a).search(t.operand[0])!=-1) { 
							litloc = literalList.get(a).search(t.operand[0]); //리터럴 테이블에 검색
							break;
						}
					
					}
					for(int b=0;b<TokenList.get(i).tokenList.size();b++) { 
						if(litloc==TokenList.get(i).tokenList.get(b).location&&litloc!=0) { 
							if(t.operand[0].contains("=C")) // 문자일 때 리터럴처리
							lit_code += "T"+String.format("%06X", litloc)+String.format("%02X", TokenList.get(i).tokenList.get(b).litSize)
										+TokenList.get(i).tokenList.get(b).literal+"\n";	
						}
					}
				}
				
			}
			codeList.add(H_code+output+lit_code+M_code+"E"); // codeList를 섹션 별로 저장함
			
		/* codeList에 추가해준 code 들을 초기화 해줌 */
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
	 * inputFile을 읽어들여서 lineList에 저장한다.<br>
	 * @param inputFile : input 파일 이름.
	 * @throws IOException 
	 */
	private void loadInputFile(String inputFile) throws IOException {
		// TODO Auto-generated method stub
		//Input.txt 불러오기 
		BufferedReader rInput = new BufferedReader(new FileReader("./input.txt"));
		while(true) {
			String rline = rInput.readLine();
			if(rline==null) break;
			lineList.add(rline);
		}
	}
	
}
