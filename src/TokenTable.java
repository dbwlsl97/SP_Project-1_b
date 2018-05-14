import java.util.ArrayList;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	SymbolTable litTab;
	InstTable instTab;
	static int locctr;
	int addr;
	int i_format;
	String f_opt;
	int objcode; // int 형 nixbpe
	int T_addr;
	int PC_addr;
	int lit_addr;
	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;
	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * @param symTab : 해당 section과 연결되어있는 symbol table
	 * @param instTab : instruction 명세가 정의된 instTable
	 * @param litTab 
	 */
	public TokenTable(SymbolTable symTab, InstTable instTab, SymbolTable litTab) {
		this.symTab = symTab;
		this.instTab = instTab;
		this.litTab = litTab;
		tokenList = new ArrayList<Token>();
		}
	
	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		int loc1=0;
		int loc2=0;
		tokenList.add(new Token(line));
		Token t = tokenList.get(tokenList.size()-1);
		
		f_opt = t.operator; // 4형식을 위해 만든 string 변수
		
		t.location = locctr;
//		locctr = t.location;
		if(!t.label.isEmpty()) {
			symTab.putSymbol(t.label, locctr);
		}
		if(t.operator.contains("+")) {
			f_opt = t.operator.substring(1);
			if(instTab.instMap.containsKey(f_opt)) {
				i_format = instTab.instMap.get(f_opt).format;
			}                              
			locctr +=4;
		}
		else if(instTab.instMap.containsKey(t.operator)) {
			i_format = instTab.instMap.get(t.operator).format;
			if(i_format==1) {
				locctr +=1;
			}
			else if(i_format==2) {
				locctr +=2;
			}
			else if(i_format==3) {
				locctr +=3;
			}
		}
//		else if(t.operator.equals("END")) {
//			locctr =0;
//			t.location = locctr;
//		}
		else if(t.operator.equals("EQU")) {
		if(t.operand[0].contains("-")) {
				t.operand = t.operand[0].split("-",2);
				loc1 = symTab.search(t.operand[0]);
				loc2 = symTab.search(t.operand[1]);
				locctr = loc1 - loc2;
				symTab.putSymbol(t.label, locctr);
//				System.out.println(t.label+"\t"+locctr);
				t.location = locctr;
		}
//				System.out.println(t.label+"\t"+locctr);
		}
		else if(t.operator.equals("RESW")) {
			locctr += (3*Integer.parseInt(t.operand[0]));
		}
		else if(t.operator.equals("RESB")) {
			locctr += (1*Integer.parseInt(t.operand[0]));
		}
		else if(t.operator.equals("BYTE")) {
			locctr += 1;
		}
		else if((t.operator.equals("WORD"))||(t.operator.equals("LTORG"))) {
			locctr += 3;
		}
//		if(t.operator.equals("LTORG")) {
//			for(int i=0;i<tokenList.size();i++) {
//				if(tokenList.get(i).operand[0].contains("=")) {
//
//					litTab.putSymbol(tokenList.get(i).operand[0], locctr);
//				}
//			}
//			System.out.println(litTab.locationList+"\t"+litTab.symbolList);
//		}
		if(t.operand[0].contains("=")) {
			if(litTab.search(t.operand[0])==-1) {		
				litTab.putSymbol(t.operand[0], locctr);
//						System.out.println(litTab.symbolList+"\t"+t.operand[0] + "\t" + litTab.search(t.operand[0]));
					}
//			System.out.println(litTab.locationList+"\t"+litTab.symbolList);
				}
	
	
		if(t.operator.equals("START")||(t.operator.equals("CSECT"))) {
			locctr = 0;
		}
//		System.out.println(t.location);
	}
	
	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 과정에서 사용한다.
	 * instruction table, symbol table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
	 * @param index
	 */
	public void makeObjectCode(int index){
		Instruction op = instTab.instMap.get(tokenList.get(index).operator);
		int format_2 = 0;
		objcode =0;
		T_addr =0;
		PC_addr =0;
		if(tokenList.get(index).operator.contains("+")) {
			f_opt = tokenList.get(index).operator.substring(1);
			op = instTab.instMap.get(f_opt);
			tokenList.get(index).setFlag(nFlag, 1);
			tokenList.get(index).setFlag(iFlag, 1);
			tokenList.get(index).setFlag(eFlag, 1);
			if(tokenList.get(index).operand[1]!=null) {
				if(tokenList.get(index).operand[1].equals("X")) {
					tokenList.get(index).setFlag(xFlag, 1);
				}
			}
			objcode += op.opcode << 24;
			objcode += tokenList.get(index).nixbpe <<20 ;
			tokenList.get(index).objectCode = String.format("%X", objcode);
			System.out.println(op.instruction + "\t"+ tokenList.get(index).objectCode);
		}
		else if(instTab.instMap.containsKey(tokenList.get(index).operator)) {
			i_format = instTab.instMap.get(tokenList.get(index).operator).format;
			if(i_format==2) {
				for(int i=0;i<2;i++) {
					if(tokenList.get(index).operand[i]!=null) {
						if(tokenList.get(index).operand[i].equals("A")) {
							format_2 |=0;
						}
	             					else if(tokenList.get(index).operand[i].equals("X")) {
							format_2 |=1;
						}
						else if(tokenList.get(index).operand[i].equals("L")) {
							format_2 |=2;
						}
						else if(tokenList.get(index).operand[i].equals("B")) {
							format_2 |=3;
						}
						else if(tokenList.get(index).operand[i].equals("S")) {
							format_2 |=4;
						}
						else if(tokenList.get(index).operand[i].equals("T")) {
							format_2 |=5;
						}
						else if(tokenList.get(index).operand[i].equals("F")) {
							format_2 |=6;
						}
						else if(tokenList.get(index).operand[i].equals("PC")) {
							format_2 |=8;
						}
						else if(tokenList.get(index).operand[i].equals("SW")) {
							format_2 |=9;
						}
						if(i==0)
							format_2 = format_2 << 4;
					}
					}
				tokenList.get(index).objectCode = String.format("%02X%02X", op.opcode, format_2);
				System.out.println(op.instruction + "\t"+ tokenList.get(index).objectCode);
			}
			else if(i_format==3) {
				objcode = op.opcode<<16;
				if(tokenList.get(index).operand[0].contains("#")) {
					T_addr = Integer.parseInt(tokenList.get(index).operand[0].substring(1));
					tokenList.get(index).setFlag(iFlag, 1);
					objcode +=tokenList.get(index).nixbpe<<12;
					objcode +=T_addr;
					tokenList.get(index).objectCode = String.format("%06X", objcode);
					System.out.println(op.instruction + "\t"+ tokenList.get(index).objectCode);
					
				}
				else if(tokenList.get(index).operand[0].contains("@")) {
					tokenList.get(index).setFlag(nFlag, 1);
					tokenList.get(index).setFlag(pFlag, 1);
					objcode +=tokenList.get(index).nixbpe<<12;
					T_addr = symTab.search(tokenList.get(index).operand[0].substring(1));
					PC_addr = tokenList.get(index+1).location;
					objcode += (T_addr - PC_addr);
					tokenList.get(index).objectCode = String.format("%06X", objcode);
					System.out.println(op.instruction + "\t"+ tokenList.get(index).objectCode);
				}
				else if(tokenList.get(index).operand[0].contains("=")) {
					tokenList.get(index).setFlag(nFlag, 1);
					tokenList.get(index).setFlag(iFlag, 1);
					tokenList.get(index).setFlag(pFlag, 1);
					objcode += tokenList.get(index).nixbpe<<12;
					for(int i=index; i<tokenList.size(); i++) {
						if(tokenList.get(i).operator.equals("LTORG")) {
						litTab.modifySymbol(tokenList.get(index).operand[0], tokenList.get(i).location);
						break;
						}
						else
						litTab.modifySymbol(tokenList.get(index).operand[0], tokenList.get(i).location);
					}					
					T_addr = litTab.search(tokenList.get(index).operand[0]);
					PC_addr = tokenList.get(index+1).location;
					objcode += (T_addr - PC_addr);
					tokenList.get(index).objectCode = String.format("%06X", objcode);
					System.out.println(op.instruction + "\t"+ tokenList.get(index).objectCode);

					
				}
				else if(tokenList.get(index).operand[0].isEmpty()) {
					tokenList.get(index).setFlag(nFlag, 1);
					tokenList.get(index).setFlag(iFlag, 1);
					objcode += tokenList.get(index).nixbpe<<12;
					tokenList.get(index).objectCode = String.format("%06X", objcode);
					System.out.println(op.instruction + "\t"+ tokenList.get(index).objectCode);

				}
				else {
					tokenList.get(index).setFlag(nFlag, 1);
					tokenList.get(index).setFlag(iFlag, 1);
					tokenList.get(index).setFlag(pFlag, 1);
					objcode += tokenList.get(index).nixbpe<<12;
					T_addr = symTab.search(tokenList.get(index).operand[0]);
					PC_addr = tokenList.get(index+1).location;
					if(T_addr <= PC_addr) {
						objcode += ((T_addr - PC_addr) & 0x00000FFF);
					}
					else
					objcode += (T_addr - PC_addr);
					tokenList.get(index).objectCode = String.format("%06X", objcode);
					System.out.println(op.instruction + "\t"+ tokenList.get(index).objectCode);
				}

			}
		}
	}
	
	/** 
	 * index번호에 해당하는 object code를 리턴한다.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 
 * 의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token{
	//의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe; 
//	InstTable instTab;
	// object code 생성 단계에서 사용되는 변수들 
	String objectCode;
	int byteSize;
//	static int count; //line 번호 세기 = index
	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다. 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		nixbpe =0;
		operand = new String[3];
		//initialize 추가
		parsing(line);
		
	}
	
	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {

		String[] line_token = line.split("\t",4);
		label = line_token[0];
		operator = line_token[1];
		if(line_token[2].contains(",")) {
			operand = line_token[2].split(",",3);
		}
		else {
//			operand = new String[1];
			operand[0] = line_token[2];
		}

		comment = line_token[3];
		
	}
	
	/** 
	 * n,i,x,b,p,e flag를 설정한다. <br><br>
	 * 
	 * 사용 예 : setFlag(nFlag, 1); <br>
	 *   또는     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		if(value==1) {
			nixbpe |= flag;
		}
		else if(value==0) {
			nixbpe &= flag;
		}
		
	}
	
	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 <br><br>
	 * 
	 * 사용 예 : getFlag(nFlag) <br>
	 *   또는     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
