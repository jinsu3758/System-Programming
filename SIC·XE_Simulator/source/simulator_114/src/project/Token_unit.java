package project;

//
//참조값, 정의값, 수정값 등의 이름,형식 등을 저장하는 테이블
//
public class Token_unit {
	public String name;	//이름
	public int addr;	//주소
	public int sub_cnt;	//루틴 index
	public int mod_n;	//수정갯수
	public int is_op;	//연산 /+ : 1 /- : 2 /없음 : 0
	
	Token_unit(String str,int f)
	{
		this.name=str;
		this.addr=f;
		this.sub_cnt=f;
		this.mod_n=f;
		this.is_op=f;
	}

}
