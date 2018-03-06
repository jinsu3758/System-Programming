package project;

/*
* object code 클래스
*/
public class Address {
	String byte_ob;	//operator이 byte인 경우를 위한 문자열
	int op;	//opcode를 위한 변수(ni값도 포함)
	int loc;	//주소값을 위한 변수
	int xbpe;	//xbpe값을 위한 변수
	int r_cnt;	//섹션의 순서를 위한 변수
	int is_r;	//pass2에서 수정레코드에 쓰기 위해 참조값일 경우 1을 저장하는 변수 LTORG일 경우는 2 둘 다 아니 경우는 0 
	int format;	//pass2에서 T레코드를 나누기 위해 쓰일 각 라인의 증가할 주소값
	Address(String ob,
	int o,
	int l,
	int x,
	int r,
	int i,
	int f)
	{
		this.byte_ob=ob;
		this.op=0;
		this.loc=l;
		this.xbpe=x;
		this.r_cnt=r;
		this.is_r=i;
		this.format=f;
	}
}
