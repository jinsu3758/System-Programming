package project;

//
//instruction ��� ���Ϸ� ���� ������ �޾ƿͼ� �����ϴ� Ŭ����
//
public class instruction {
	public String name;
	public int opcode;
	public int format;
	
	instruction(String n, int op,int form)
	{
		this.name=n;
		this.opcode=op;
		this.format=form;
	}
	public void setName(String n)
	{
		this.name=n;
	}
	
	public void setFormat(int f)
	{
		this.format=f;
	}
	
	public int getFormat()
	{
		return format;
	}

}