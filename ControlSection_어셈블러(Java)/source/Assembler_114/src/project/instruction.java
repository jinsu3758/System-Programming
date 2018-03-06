package project;

//
//instruction 목록 파일로 부터 정보를 받아와서 생성하는 클래스
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