package project;

/*
* object code Ŭ����
*/
public class Address {
	String byte_ob;	//operator�� byte�� ��츦 ���� ���ڿ�
	int op;	//opcode�� ���� ����(ni���� ����)
	int loc;	//�ּҰ��� ���� ����
	int xbpe;	//xbpe���� ���� ����
	int r_cnt;	//������ ������ ���� ����
	int is_r;	//pass2���� �������ڵ忡 ���� ���� �������� ��� 1�� �����ϴ� ���� LTORG�� ���� 2 �� �� �ƴ� ���� 0 
	int format;	//pass2���� T���ڵ带 ������ ���� ���� �� ������ ������ �ּҰ�
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
