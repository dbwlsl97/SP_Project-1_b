import java.util.ArrayList;
import java.util.Iterator;

/**
 * symbol과 관련된 데이터와 연산을 소유한다.
 * section 별로 하나씩 인스턴스를 할당한다.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;
	String[] lit; //리터럴을 byte단위로 구분하기 위한 변수
	// 기타 literal, external 선언 및 처리방법을 구현한다.
	
	/**
	 * 새로운 Symbol을 table에 추가한다.
	 * @param symbol : 새로 추가되는 symbol의 label
	 * @param location : 해당 symbol이 가지는 주소값
	 * <br><br>
	 * 주의 : 만약 중복된 symbol이 putSymbol을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다. 
	 * 매칭되는 주소값의 변경은 modifySymbol()을 통해서 이루어져야 한다.
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
	 * 기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
	 * @param symbol : 변경을 원하는 symbol의 label
	 * @param newLocation : 새로 바꾸고자 하는 주소값
	 */
	public void modifySymbol(String symbol, int newLocation) {
		if(search(symbol)!=-1) {
			lit = symbol.split("'"); //리터럴이라면 '을 기준으로 분리하고
			for(int i=0;i<symbolList.size();i++) { 
				if(symbol.equals(symbolList.get(i))) //현재 심볼이 심볼리스트에 있다면 변경해주기
					locationList.set(i, newLocation);	
					
				}
			
			}
	}
	
	/**
	 * 인자로 전달된 symbol이 어떤 주소를 지칭하는지 알려준다. 
	 * @param symbol : 검색을 원하는 symbol의 label
	 * @return symbol이 가지고 있는 주소값. 해당 symbol이 없을 경우 -1 리턴
	 */
	public int search(String symbol) {
		int address = 0;
		if(symbolList.contains(symbol)) { //심볼테이블에 현재 심볼이 존재한다면
			for(int i=0;i<symbolList.size();i++) {
				if(symbol.equals(symbolList.get(i))) { 
					address = locationList.get(i);		// 주소를 리턴			
				}
			}
		}
		else {
			return -1;
		}
		return address;
	}	
}