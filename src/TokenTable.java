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
	int objcode; 
	int T_addr; // Target Address
	int PC_addr; //PC Address
	char[] lit; //리터럴만 분리할 때 사용
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
		this.litTab = litTab; //심볼테이블을 활용해 litTab 생성
		tokenList = new ArrayList<Token>();
		}
	
	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		int loc1=0; //BUFEND 주소 처리할 때 이용
		int loc2=0; //BUFFER 주소 처리할 때 이용
		tokenList.add(new Token(line));
		Token t = tokenList.get(tokenList.size()-1); //현재 line 에 대한 Token 생성
		f_opt = t.operator; // 4형식을 위해 만든 string 변수
		t.location = locctr; 
		
		
		if(t.operator.equals("START")||(t.operator.equals("CSECT"))) { //START 나 CSECT 만났을 때 locctr 초기화
			locctr = 0;
		}
		if(!t.label.isEmpty()) { //label이 있다면 심볼테이블에 값 추가
			symTab.putSymbol(t.label, locctr);
		}

		if(t.operator.contains("+")) { //4형식일 때
			f_opt = t.operator.substring(1); // + 다음부터 instTab의 key 값으로 만들어주기위한 변수
			if(instTab.instMap.containsKey(f_opt)) { //operator를 key 값으로 해서 instruction 정보 얻기
				i_format = instTab.instMap.get(f_opt).format; // 해당 instruction format 
			}                              
			locctr +=4; 
			t.byteSize +=4; //주소와 함께 byteSize 증가
		}
		else if(instTab.instMap.containsKey(t.operator)) { //instruction인 operator 중,
			i_format = instTab.instMap.get(t.operator).format;
			if(i_format==1) { // 1형식일 때
				locctr +=1;
				t.byteSize +=1;
			}
			else if(i_format==2) { // 2형식일 때
				locctr +=2;
				t.byteSize +=2;
			}
			else if(i_format==3) { // 3형식일 때 
				locctr +=3;
				t.byteSize +=3;

			}
		}
		else if(t.operator.equals("EQU")) { //EQU 중,
		if(t.operand[0].contains("-")) { // '-' 가 들어간 operand[0]이라면 
				String a = t.operand[0]; //'-' 를 기준으로 각각 operand[0], operand[1]에 값 넣어주기로 변경함
				t.operand = t.operand[0].split("-",2);
				loc1 = symTab.search(t.operand[0]);
				loc2 = symTab.search(t.operand[1]);
				locctr = loc1 - loc2;
				symTab.modifySymbol(t.label, locctr); //modifySymbol 이용
				t.location = locctr;
		}
		}
		else if(t.operator.equals("RESW")) { 
			locctr += (3*Integer.parseInt(t.operand[0]));
		}
		else if(t.operator.equals("RESB")) {
			locctr += (1*Integer.parseInt(t.operand[0]));
		}
		else if(t.operator.equals("BYTE")) {
			locctr += 1;
			t.byteSize +=1;

		}
		else if(t.operator.equals("WORD")) {
			locctr += 3;
			t.byteSize +=3;
		}
		else if(t.operator.equals("LTORG"))	{
			locctr +=3;
		}

		if(t.operand[0].contains("=")) { //리터럴 중, 정수 리터럴 처리
			if(litTab.search(t.operand[0])==-1) {		
				litTab.putSymbol(t.operand[0], locctr);
				if(t.operand[0].contains("X")) {
					locctr+=1;
				t.byteSize +=1;
				}
			}
				}
	
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
	public void makeObjectCode(int index){ //objectCode 를 만들 때 사용
		Instruction op = instTab.instMap.get(tokenList.get(index).operator); //instruction 정보 얻어오는 변수
		int format_2 = 0; // 2형식 기계어코드처리할 때 사용
		objcode =0; // 비트 연산 처리를 위한 변수
		T_addr =0;
		PC_addr =0;
		if(tokenList.get(index).operator.contains("+")) { //4형식일 때 nixbpe 와 operand에 X가 있을 때 기계어 코드 처리
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
		}
		else if(tokenList.get(index).operator.equals("BYTE")) { // BYTE일 때 기계어 코드 처리
			int a = 0;
			String[] str = tokenList.get(index).operand[0].split("'");
			tokenList.get(index).objectCode = String.format("%02X", Integer.parseInt(str[1], 16));
		}
 		else if(tokenList.get(index).operator.equals("WORD")) { //WORD일 때 기계어 코드 처리
			int a =0, b=0;
			a = symTab.search(tokenList.get(index).operand[0]);
			b = symTab.search(tokenList.get(index).operand[1]);
			if(a==-1||b==-1) { //둘 다 현재 섹션에 있지않은 symbol 이라면 objectCode에 000000 을 넣어줌 (값을 모르므로)
				tokenList.get(index).objectCode = String.format("%06X", 0);
			}
		}
		else if(instTab.instMap.containsKey(tokenList.get(index).operator)) { //operator가 instruction일 때의 기계어 코드 처리
			i_format = instTab.instMap.get(tokenList.get(index).operator).format;
			if(i_format==2) {
				for(int i=0;i<2;i++) { 
					if(tokenList.get(index).operand[i]!=null) {
						if(tokenList.get(index).operand[i].equals("A")) { //2형식일 때, 각 레지스터에 따른 기계어 코드 처리
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
					}																			//2형식은 opcode와 형식을 따로 구분하여 저장함
				tokenList.get(index).objectCode = String.format("%02X%02X", op.opcode, format_2); //항상 마지막은 objectCode에 저장함 
			} 
			else if(i_format==3) { 
				objcode = op.opcode<<16;
				if(tokenList.get(index).operand[0].contains("#")) { //3형식의 immediate 주소 기계어 코드 처리
					T_addr = Integer.parseInt(tokenList.get(index).operand[0].substring(1));
					tokenList.get(index).setFlag(iFlag, 1);
					objcode +=tokenList.get(index).nixbpe<<12;
					objcode +=T_addr;
					tokenList.get(index).objectCode = String.format("%06X", objcode);
					
				}
				else if(tokenList.get(index).operand[0].contains("@")) { //3형식의 간접 주소 기계어 코드 처리
					tokenList.get(index).setFlag(nFlag, 1);
					tokenList.get(index).setFlag(pFlag, 1);
					objcode +=tokenList.get(index).nixbpe<<12;
					T_addr = symTab.search(tokenList.get(index).operand[0].substring(1));
					PC_addr = tokenList.get(index+1).location;
					objcode += (T_addr - PC_addr);
					tokenList.get(index).objectCode = String.format("%06X", objcode);
				}
				else if(tokenList.get(index).operand[0].contains("=")) { //operand에 "="을 가진 주소 기계어 코드 처리
					tokenList.get(index).setFlag(nFlag, 1); //한 마디로 리터럴
					tokenList.get(index).setFlag(iFlag, 1);
					tokenList.get(index).setFlag(pFlag, 1);
					objcode += tokenList.get(index).nixbpe<<12;
					for(int i=index; i<tokenList.size(); i++) {
						if(tokenList.get(i).operator.equals("LTORG")) { //현재 섹션에 LTORG가 있다면
						litTab.modifySymbol(tokenList.get(index).operand[0], tokenList.get(i).location);	
						lit = litTab.lit[1].toCharArray(); // 리터럴을 각 byte 마다 분리
						if(tokenList.get(i).literal.isEmpty()) { 
						for(int j=0;j<lit.length;j++) { 
							tokenList.get(i).literal += String.format("%02X", (int)lit[j]); // =C 일 때
							tokenList.get(i).litSize +=1;
						}
					
						}
						break;
						}
						else {
						litTab.modifySymbol(tokenList.get(index).operand[0], tokenList.get(i).location); // =X 일 때
						if(i==tokenList.size()-1) {
							tokenList.get(i).objectCode = String.format("%02X", Integer.parseInt(litTab.lit[1], 16)); 
							break;
						}				
						}
					}
					T_addr = litTab.search(tokenList.get(index).operand[0]);
					PC_addr = tokenList.get(index+1).location;
					objcode += (T_addr - PC_addr); //operand에 리터럴이 나왔을 때의 기계어 코드 처리
					tokenList.get(index).objectCode = String.format("%06X", objcode);
				}			

				else if(tokenList.get(index).operand[0].isEmpty()) { //operand가 없는 기계어 코드 처리 (ex: RSUB)
					tokenList.get(index).setFlag(nFlag, 1);
					tokenList.get(index).setFlag(iFlag, 1);
					objcode += tokenList.get(index).nixbpe<<12;
					tokenList.get(index).objectCode = String.format("%06X", objcode);
				}
				else { 												// 그 외 보통의 기계어코드 처리
					tokenList.get(index).setFlag(nFlag, 1);
					tokenList.get(index).setFlag(iFlag, 1);
					tokenList.get(index).setFlag(pFlag, 1);
					objcode += tokenList.get(index).nixbpe<<12;
					T_addr = symTab.search(tokenList.get(index).operand[0]);
					PC_addr = tokenList.get(index+1).location;
					if(T_addr <= PC_addr) { //그중, 타겟 주소가 피씨주소보다 작을 때의 기계어코드 처리
						objcode += ((T_addr - PC_addr) & 0x00000FFF);
					}
					else
					objcode += (T_addr - PC_addr);
					tokenList.get(index).objectCode = String.format("%06X", objcode);
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
	String literal;
	int byteSize;
	int litSize;
//	static int count; //line 번호 세기 = index
	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다. 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		nixbpe =0;
		byteSize=0;
		operand = new String[3];
		objectCode = "";
		literal = "";
		//initialize 추가
		parsing(line);
		
	}
	
	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {

		String[] line_token = line.split("\t",4);  //split으로 line을 토큰 분리
		label = line_token[0];
		operator = line_token[1];
		if(line_token[2].contains(",")) { //operand는 ,를 기준으로 분리
			operand = line_token[2].split(",",3);
		}
		else {
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
