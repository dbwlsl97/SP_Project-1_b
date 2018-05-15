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
	int objcode; 
	int T_addr; // Target Address
	int PC_addr; //PC Address
	char[] lit; //���ͷ��� �и��� �� ���
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
		this.litTab = litTab; //�ɺ����̺��� Ȱ���� litTab ����
		tokenList = new ArrayList<Token>();
		}
	
	/**
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * @param line : �и����� ���� �Ϲ� ���ڿ�
	 */
	public void putToken(String line) {
		int loc1=0; //BUFEND �ּ� ó���� �� �̿�
		int loc2=0; //BUFFER �ּ� ó���� �� �̿�
		tokenList.add(new Token(line));
		Token t = tokenList.get(tokenList.size()-1); //���� line �� ���� Token ����
		f_opt = t.operator; // 4������ ���� ���� string ����
		t.location = locctr; 
		
		
		if(t.operator.equals("START")||(t.operator.equals("CSECT"))) { //START �� CSECT ������ �� locctr �ʱ�ȭ
			locctr = 0;
		}
		if(!t.label.isEmpty()) { //label�� �ִٸ� �ɺ����̺� �� �߰�
			symTab.putSymbol(t.label, locctr);
		}

		if(t.operator.contains("+")) { //4������ ��
			f_opt = t.operator.substring(1); // + �������� instTab�� key ������ ������ֱ����� ����
			if(instTab.instMap.containsKey(f_opt)) { //operator�� key ������ �ؼ� instruction ���� ���
				i_format = instTab.instMap.get(f_opt).format; // �ش� instruction format 
			}                              
			locctr +=4; 
			t.byteSize +=4; //�ּҿ� �Բ� byteSize ����
		}
		else if(instTab.instMap.containsKey(t.operator)) { //instruction�� operator ��,
			i_format = instTab.instMap.get(t.operator).format;
			if(i_format==1) { // 1������ ��
				locctr +=1;
				t.byteSize +=1;
			}
			else if(i_format==2) { // 2������ ��
				locctr +=2;
				t.byteSize +=2;
			}
			else if(i_format==3) { // 3������ �� 
				locctr +=3;
				t.byteSize +=3;

			}
		}
		else if(t.operator.equals("EQU")) { //EQU ��,
		if(t.operand[0].contains("-")) { // '-' �� �� operand[0]�̶�� 
				String a = t.operand[0]; //'-' �� �������� ���� operand[0], operand[1]�� �� �־��ֱ�� ������
				t.operand = t.operand[0].split("-",2);
				loc1 = symTab.search(t.operand[0]);
				loc2 = symTab.search(t.operand[1]);
				locctr = loc1 - loc2;
				symTab.modifySymbol(t.label, locctr); //modifySymbol �̿�
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

		if(t.operand[0].contains("=")) { //���ͷ� ��, ���� ���ͷ� ó��
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
	public void makeObjectCode(int index){ //objectCode �� ���� �� ���
		Instruction op = instTab.instMap.get(tokenList.get(index).operator); //instruction ���� ������ ����
		int format_2 = 0; // 2���� �����ڵ�ó���� �� ���
		objcode =0; // ��Ʈ ���� ó���� ���� ����
		T_addr =0;
		PC_addr =0;
		if(tokenList.get(index).operator.contains("+")) { //4������ �� nixbpe �� operand�� X�� ���� �� ���� �ڵ� ó��
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
		else if(tokenList.get(index).operator.equals("BYTE")) { // BYTE�� �� ���� �ڵ� ó��
			int a = 0;
			String[] str = tokenList.get(index).operand[0].split("'");
			tokenList.get(index).objectCode = String.format("%02X", Integer.parseInt(str[1], 16));
		}
 		else if(tokenList.get(index).operator.equals("WORD")) { //WORD�� �� ���� �ڵ� ó��
			int a =0, b=0;
			a = symTab.search(tokenList.get(index).operand[0]);
			b = symTab.search(tokenList.get(index).operand[1]);
			if(a==-1||b==-1) { //�� �� ���� ���ǿ� �������� symbol �̶�� objectCode�� 000000 �� �־��� (���� �𸣹Ƿ�)
				tokenList.get(index).objectCode = String.format("%06X", 0);
			}
		}
		else if(instTab.instMap.containsKey(tokenList.get(index).operator)) { //operator�� instruction�� ���� ���� �ڵ� ó��
			i_format = instTab.instMap.get(tokenList.get(index).operator).format;
			if(i_format==2) {
				for(int i=0;i<2;i++) { 
					if(tokenList.get(index).operand[i]!=null) {
						if(tokenList.get(index).operand[i].equals("A")) { //2������ ��, �� �������Ϳ� ���� ���� �ڵ� ó��
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
					}																			//2������ opcode�� ������ ���� �����Ͽ� ������
				tokenList.get(index).objectCode = String.format("%02X%02X", op.opcode, format_2); //�׻� �������� objectCode�� ������ 
			} 
			else if(i_format==3) { 
				objcode = op.opcode<<16;
				if(tokenList.get(index).operand[0].contains("#")) { //3������ immediate �ּ� ���� �ڵ� ó��
					T_addr = Integer.parseInt(tokenList.get(index).operand[0].substring(1));
					tokenList.get(index).setFlag(iFlag, 1);
					objcode +=tokenList.get(index).nixbpe<<12;
					objcode +=T_addr;
					tokenList.get(index).objectCode = String.format("%06X", objcode);
					
				}
				else if(tokenList.get(index).operand[0].contains("@")) { //3������ ���� �ּ� ���� �ڵ� ó��
					tokenList.get(index).setFlag(nFlag, 1);
					tokenList.get(index).setFlag(pFlag, 1);
					objcode +=tokenList.get(index).nixbpe<<12;
					T_addr = symTab.search(tokenList.get(index).operand[0].substring(1));
					PC_addr = tokenList.get(index+1).location;
					objcode += (T_addr - PC_addr);
					tokenList.get(index).objectCode = String.format("%06X", objcode);
				}
				else if(tokenList.get(index).operand[0].contains("=")) { //operand�� "="�� ���� �ּ� ���� �ڵ� ó��
					tokenList.get(index).setFlag(nFlag, 1); //�� ����� ���ͷ�
					tokenList.get(index).setFlag(iFlag, 1);
					tokenList.get(index).setFlag(pFlag, 1);
					objcode += tokenList.get(index).nixbpe<<12;
					for(int i=index; i<tokenList.size(); i++) {
						if(tokenList.get(i).operator.equals("LTORG")) { //���� ���ǿ� LTORG�� �ִٸ�
						litTab.modifySymbol(tokenList.get(index).operand[0], tokenList.get(i).location);	
						lit = litTab.lit[1].toCharArray(); // ���ͷ��� �� byte ���� �и�
						if(tokenList.get(i).literal.isEmpty()) { 
						for(int j=0;j<lit.length;j++) { 
							tokenList.get(i).literal += String.format("%02X", (int)lit[j]); // =C �� ��
							tokenList.get(i).litSize +=1;
						}
					
						}
						break;
						}
						else {
						litTab.modifySymbol(tokenList.get(index).operand[0], tokenList.get(i).location); // =X �� ��
						if(i==tokenList.size()-1) {
							tokenList.get(i).objectCode = String.format("%02X", Integer.parseInt(litTab.lit[1], 16)); 
							break;
						}				
						}
					}
					T_addr = litTab.search(tokenList.get(index).operand[0]);
					PC_addr = tokenList.get(index+1).location;
					objcode += (T_addr - PC_addr); //operand�� ���ͷ��� ������ ���� ���� �ڵ� ó��
					tokenList.get(index).objectCode = String.format("%06X", objcode);
				}			

				else if(tokenList.get(index).operand[0].isEmpty()) { //operand�� ���� ���� �ڵ� ó�� (ex: RSUB)
					tokenList.get(index).setFlag(nFlag, 1);
					tokenList.get(index).setFlag(iFlag, 1);
					objcode += tokenList.get(index).nixbpe<<12;
					tokenList.get(index).objectCode = String.format("%06X", objcode);
				}
				else { 												// �� �� ������ �����ڵ� ó��
					tokenList.get(index).setFlag(nFlag, 1);
					tokenList.get(index).setFlag(iFlag, 1);
					tokenList.get(index).setFlag(pFlag, 1);
					objcode += tokenList.get(index).nixbpe<<12;
					T_addr = symTab.search(tokenList.get(index).operand[0]);
					PC_addr = tokenList.get(index+1).location;
					if(T_addr <= PC_addr) { //����, Ÿ�� �ּҰ� �Ǿ��ּҺ��� ���� ���� �����ڵ� ó��
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
	String literal;
	int byteSize;
	int litSize;
//	static int count; //line ��ȣ ���� = index
	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�. 
	 * @param line ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line) {
		nixbpe =0;
		byteSize=0;
		operand = new String[3];
		objectCode = "";
		literal = "";
		//initialize �߰�
		parsing(line);
		
	}
	
	/**
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * @param line ��������� ����� ���α׷� �ڵ�.
	 */
	public void parsing(String line) {

		String[] line_token = line.split("\t",4);  //split���� line�� ��ū �и�
		label = line_token[0];
		operator = line_token[1];
		if(line_token[2].contains(",")) { //operand�� ,�� �������� �и�
			operand = line_token[2].split(",",3);
		}
		else {
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
