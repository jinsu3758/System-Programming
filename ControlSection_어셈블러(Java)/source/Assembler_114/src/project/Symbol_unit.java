package project;

/*
* 심볼을 관리하는 클래스이다.
* 심볼 테이블은 심볼 이름, 심볼의 위치(주소), 섹션으로 구성된다.
*/
public class Symbol_unit {
	public String name;
	public int addr;
	public int sub_cnt;
	
	Symbol_unit(String n, int a, int s)
	{
		this.name=n;
		this.addr=a;
		this.sub_cnt=s;
	}
	public void setName(String n)
	{
		this.name=n;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setAddr(int a)
	{
		this.addr=a;
	}
	
	public int getAddr()
	{
		return addr;
	}
	
	//주어진 문자열이 symbol인지 확인하는 함수이다.
	//반환 : 있을 경우 : true 없을 경우 : false
	public boolean isSymbol(String str,int r_cnt)
	{
		boolean is=false;	
		if(r_cnt==sub_cnt)
		{
			if(name.equals(str))
			{
				return true;
			}
		}
		
		return false;
	}
	
}
