package project;

//
//instruction 목록 파일로 부터 정보를 받아와서 생성하는 클래스
//
public class instruction {
	public String name;	//명령어의 이름
	public int opcode;	//명령어의 opcode
	public int format;	//명령어의 형식
	
	instruction(String n, int op,int form)
	{
		this.name=n;
		this.opcode=op;
		this.format=form;
	}
	//명령어의 이름을 지정하는 함수
	public void setName(String n)
	{
		this.name=n;
	}
	
	//명령어의 형식을 지정하는 함수
	public void setFormat(int f)
	{
		this.format=f;
	}
	
	//명령어의 형식을 가져오는 함수
	public int getFormat()
	{
		return format;
	}

}