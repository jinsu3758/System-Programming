package project;

//
//������, ���ǰ�, ������ ���� �̸�,���� ���� �����ϴ� ���̺�
//
public class Token_unit {
	public String name;	//�̸�
	public int addr;	//�ּ�
	public int sub_cnt;	//��ƾ index
	public int mod_n;	//��������
	public int is_op;	//���� /+ : 1 /- : 2 /���� : 0
	
	Token_unit(String str,int f)
	{
		this.name=str;
		this.addr=f;
		this.sub_cnt=f;
		this.mod_n=f;
		this.is_op=f;
	}

}
