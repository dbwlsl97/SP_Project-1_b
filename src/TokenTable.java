import java.util.ArrayList;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�. <br>
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ� �̸� ��ũ��Ų��.<br>
 * section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit ������ �������� ���� ���� */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	SymbolTable symTab;
	SymbolTable litTab;
	InstTable instTab;
	static int locctr;
	int addr;
	int i_format;
	String f_opt;
	int objcode; // int �� nixbpe
	int T_addr;
	int PC_addr;
	int lit_addr;
	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����. */
	ArrayList<Token> tokenList;
	/**
	 * �ʱ�ȭ�ϸ鼭 symTable�� instTable�� ��ũ��Ų��.
	 * @param symTab : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param instTab : instruction ���� ���ǵ� instTable
	 * @param litTab 
	 */
	public TokenTable(SymbolTable symTab, InstTable instTab, SymbolTable litTab) {
		this.symTab = symTab;
		this.instTab = instTab;
		this.litTab = litTab;
		tokenList = new ArrayList<Token>();
		}
	
	/**
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * @param line : �и����� ���� �Ϲ� ���ڿ�
	 */
	public void putToken(String line) {
		int loc1=0;
		int loc2=0;
		tokenList.add(new Token(line));
		Token t = tokenList.get(tokenList.size()-1);
		
		f_opt = t.operator; // 4������ ���� ���� string ����
		
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
	 * tokenList���� index�� �ش��ϴ� Token�� �����Ѵ�.
	 * @param index
	 * @return : index��ȣ�� �ش��ϴ� �ڵ带 �м��� Token Ŭ����
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 �������� ����Ѵ�.
	 * instruction table, symbol table ���� �����Ͽ� objectcode�� �����ϰ�, �̸� �����Ѵ�.
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
	 * index��ȣ�� �ش��ϴ� object code�� �����Ѵ�.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
}

/**
 * �� ���κ��� ����� �ڵ带 �ܾ� ������ ������ ��  �ǹ̸� �ؼ��ϴ� ���� ���Ǵ� ������ ������ �����Ѵ�. 
 * �ǹ� �ؼ��� ������ pass2���� object code�� �����Ǿ��� ���� ����Ʈ �ڵ� ���� �����Ѵ�.
 */
class Token{
	//�ǹ� �м� �ܰ迡�� ���Ǵ� ������
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe; 
//	InstTable instTab;
	// object code ���� �ܰ迡�� ���Ǵ� ������ 
	String objectCode;
	int byteSize;
//	static int count; //line ��ȣ ���� = index
	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�. 
	 * @param line ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line) {
		nixbpe =0;
		operand = new String[3];
		//initialize �߰�
		parsing(line);
		
	}
	
	/**
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * @param line ��������� ����� ���α׷� �ڵ�.
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
	 * n,i,x,b,p,e flag�� �����Ѵ�. <br><br>
	 * 
	 * ��� �� : setFlag(nFlag, 1); <br>
	 *   �Ǵ�     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : ���ϴ� ��Ʈ ��ġ
	 * @param value : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
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
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ� <br><br>
	 * 
	 * ��� �� : getFlag(nFlag) <br>
	 *   �Ǵ�     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
