import java.util.ArrayList;
import java.util.Iterator;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�.
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;
	String[] lit; //���ͷ��� byte������ �����ϱ� ���� ����
	// ��Ÿ literal, external ���� �� ó������� �����Ѵ�.
	
	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * @param symbol : ���� �߰��Ǵ� symbol�� label
	 * @param location : �ش� symbol�� ������ �ּҰ�
	 * <br><br>
	 * ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public SymbolTable() {
		symbolList = new ArrayList<String>();
		locationList = new ArrayList<Integer>();
	}
	public void putSymbol(String symbol, int location) {
				symbolList.add(symbol); 
				locationList.add(location);
		}

	
	/**
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param symbol : ������ ���ϴ� symbol�� label
	 * @param newLocation : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newLocation) {
		if(search(symbol)!=-1) {
			lit = symbol.split("'"); //���ͷ��̶�� '�� �������� �и��ϰ�
			for(int i=0;i<symbolList.size();i++) { 
				if(symbol.equals(symbolList.get(i))) //���� �ɺ��� �ɺ�����Ʈ�� �ִٸ� �������ֱ�
					locationList.set(i, newLocation);	
					
				}
			
			}
	}
	
	/**
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param symbol : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public int search(String symbol) {
		int address = 0;
		if(symbolList.contains(symbol)) { //�ɺ����̺� ���� �ɺ��� �����Ѵٸ�
			for(int i=0;i<symbolList.size();i++) {
				if(symbol.equals(symbolList.get(i))) { 
					address = locationList.get(i);		// �ּҸ� ����			
				}
			}
		}
		else {
			return -1;
		}
		return address;
	}	
}