package project;

//
//instruction ��� ���Ϸ� ���� ������ �޾ƿͼ� �����ϴ� Ŭ����
//
public class instruction {
	public String name;	//��ɾ��� �̸�
	public int opcode;	//��ɾ��� opcode
	public int format;	//��ɾ��� ����
	
	instruction(String n, int op,int form)
	{
		this.name=n;
		this.opcode=op;
		this.format=form;
	}
	//��ɾ��� �̸��� �����ϴ� �Լ�
	public void setName(String n)
	{
		this.name=n;
	}
	
	//��ɾ��� ������ �����ϴ� �Լ�
	public void setFormat(int f)
	{
		this.format=f;
	}
	
	//��ɾ��� ������ �������� �Լ�
	public int getFormat()
	{
		return format;
	}

}