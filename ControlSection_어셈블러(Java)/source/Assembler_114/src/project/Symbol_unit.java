package project;

/*
* �ɺ��� �����ϴ� Ŭ�����̴�.
* �ɺ� ���̺��� �ɺ� �̸�, �ɺ��� ��ġ(�ּ�), �������� �����ȴ�.
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
	
	//�־��� ���ڿ��� symbol���� Ȯ���ϴ� �Լ��̴�.
	//��ȯ : ���� ��� : true ���� ��� : false
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
