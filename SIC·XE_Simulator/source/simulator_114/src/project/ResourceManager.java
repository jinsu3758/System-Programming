package project;

import java.util.ArrayList;

public class ResourceManager extends VisualSimulator {

	
	static String pro_name;	//���α׷��� �̸��� ����(�����ƾ ����)
	static int start_addr;	//���α׷��� �����ּ� 
	static int length;	//���α׷��� ����
	static int first;	//�����ּ�
	static int[] r_cnt = new int[10];	//�����ּҸ� ���� ���̸� ����
	static int reg_cnt = 0;		//���������� �ε���
	static String device_name=null;	//����̽� �̸�
	byte[] device = new byte[100];	//����̽��� ���� ����
	byte[] memory = new byte[10000];	//���� �޸�
	static int[] target = new int[300];	//
	static int[] reg_a = new int[300];	//
	static int[] reg_x = new int[300];	//	�� ���������� �迭
	static int[] reg_l = new int[300];	//	(��ɾ� ����)
	static int[] reg_b = new int[300];	//
	static int[] reg_s = new int[300];	//
	static int[] reg_t = new int[300];	//
	static int[] reg_f = new int[300];	//
	static int[] reg_pc = new int[300];	//
	static int[] reg_sw = new int[300];	//

	// �޸� ������ �ʱ�ȭ �ϴ� �޼ҵ�
	public void initializeMemory() {

		for(int i=0; i<10000; i++)
		{
			memory[i]=0;
		}
	}

	// �� �������� ���� �ʱ�ȭ �ϴ� �޼ҵ�
	public void initializeRegister() 
	{
		for (int i = 0; i < 300; i++) 
		{
			reg_a[i] = -1;
			reg_b[i] = -1;
			reg_x[i] = -1;
			reg_l[i] = -1;
			reg_s[i] = -1;
			reg_t[i] = -1;
			reg_f[i] = -1;
			reg_pc[i] = -1;
			reg_sw[i] = -1;
			
		}
	
	}

	// ����̽� ���ٿ� ���� �޼ҵ�
	// ����̽��� �� �̸��� ��Ī�Ǵ� ���Ϸ� �����Ѵ�
	// (F1 �̶�� ����̽��� ������ F1 �̶�� ���Ͽ��� ���� �д´�.)
	// �ش� ����̽�(����)�� ��� ������ ���·� ����� �޼ҵ�
	public void initialDevice(String devName) {
		device_name=devName;
	}

	// ������ ����̽�(����)�� ���� ���� �޼ҵ�. �Ķ���ʹ� ���� �����ϴ�.
	public void writeDevice(String devName, byte[] data, int size) {
		if(device_name==devName)
		{
			for(int i=0; i<size; i++)
			{
				device[i]=data[i];
			}
		}
	}

	// ������ ����̽�(����)���� ���� �д� �޼ҵ�. �Ķ���ʹ� ���� �����ϴ�.
	public byte[] readDevice(String devName, int size) 
	{
		byte [] by = new byte[100];
		if(device_name==devName)
		{
			for(int i=0; i<size; i++)
			{
				by[i]=device[i];
			}
		}
		return by;
	}

	// �޸� ������ ���� ���� �޼ҵ�
	public void setMemory(int locate, byte[] data, int size) 
	{
		for(int i=0; i<size; i++)
		{
			memory[locate]=data[i];
			locate++;
		}
	}



	// ���α׷��� �̸��� ����
	public void setName(String str) {
		pro_name=str;
	}
	//�������Ϳ� ���� �����ϴ� �޼ҵ� str�� ��ɾ�/value�� ��/cnt�� �������� ����
	public void setRe(String str,int value,int cnt)
	{
		if(str.equals("JSUB"))
		{
			reg_l[cnt]=value;
			target[cnt]=value;
		}
		else if(str.equals("STCH") || str.equals("STX"))
		{
			target[cnt]=value;
		}
		else if(str.equals("LDT"))
		{
			reg_t[cnt]=value;
			target[cnt]=value;
		}
		else if(str.equals("LDCH"))
		{
			reg_a[cnt]=value;
			target[cnt]=value;
		}
	}

	// �������Ϳ� ���� �����ϴ� �޼ҵ�. regNum1,2�� �������� ������ ��Ÿ����.
	//reg_r�� �ּҹ��	value�� ��  \loc�� LOCCTR \flag �� ����,�ε� ��ɾ� \format�� ��ɾ��� ���� \ inst�� ��ɾ� �̸�
	//flag 0�� ����/1�� ����/-1�� �ٸ� ��ɾ�   reg_r�� ����ּ�������Ŀ� ���� 3�� ���̽�, 8�� pc, -1�� �����ּ�������� �Ǵ� 2�����̴�.
	public void setRegister(int regNum1, int regNum2, int reg_r, int value, int loc, int flag,int format, String inst) 
	{
		target[reg_cnt]=value;	//Ÿ���ּ� ����
		if (reg_r == 3) //���̽� �������͸� ����ϴ� ���
		{
			for (int k = reg_cnt - 1; k >= 0; k--) 
			{
				if (reg_b[k] >= 0) 
				{
					reg_b[reg_cnt] = reg_b[k];
					break;
				}
			}
			//�������� �ѹ��� ���� ����
			switch (regNum1) {
			case 0: 
			{
				if (flag == 0)	//A��������, flag�� ����/���� ��ɾ ���� ���� 0�� ����
				{
					reg_a[reg_cnt] = value;
				} 
				else 
				{
					for (int k = reg_cnt - 1; k >= 0; k--) 
					{
						if (reg_a[k] >= 0) 
						{
							reg_a[reg_cnt] = reg_a[k];
							break;
						}
					}
				}
				break;
			}
			case 1: {	//X���������� ���
				if (flag == 0) 
				{
					reg_x[reg_cnt] = value;
				} 
				else 
				{
					for (int k = reg_cnt - 1; k >= 0; k--) 
					{
						if (reg_x[k] >= 0) 
						{
							reg_x[reg_cnt] = reg_x[k];
							break;
						}
					}
				}
				break;
			}
			case 2: 
			{
				if (flag == 0) 
				{
					reg_l[reg_cnt]=value;
				} 
				else 
				{
					for (int k = reg_cnt - 1; k >= 0; k--) 
					{
						if (reg_l[k] >= 0) 
						{
							reg_l[reg_cnt] = reg_l[k];
							break;
						}
					}
				}
				break;
			}
			case 3: //base���������� ���
			{
				if (flag == 0) 
				{
					reg_b[reg_cnt] = value;
				} 
				else 
				{
					for (int k = reg_cnt - 1; k >= 0; k--) 
					{
						if (reg_b[k] >= 0) 
						{
							reg_b[reg_cnt] = reg_b[k];
							break;
						}
					}
				}
				break;
			}
			case 4: //S���������� ���
			{
				if (flag == 0) 
				{
					reg_s[reg_cnt] = value;
				} else {
					for (int k = reg_cnt - 1; k >= 0; k--) 
					{
						if (reg_s[k] >= 0) 
						{
							reg_s[reg_cnt] = reg_s[k];
							break;
						}
					}
				}
				break;
			}
			
			case 5: //t���������� ���
			{
				if (flag == 0) 
				{
					reg_t[reg_cnt] = value;
				} 
				else 
				{
					for (int k = reg_cnt - 1; k >= 0; k--) 
					{
						if (reg_t[k] >= 0) 
						{
							reg_t[reg_cnt] = reg_t[k];
							break;
						}
					}
				}
				break;
			}
			//�б� ��ɾ��� ���
			case 8:
			{
				reg_pc[reg_cnt]=value;
			}
			//TIX�� ���
			case 11:
			{
				for (int k = reg_cnt - 1; k >= 0; k--) 
				{
					if (reg_x[k] >= 0) 
					{
						reg_x[reg_cnt] = reg_x[k]+1;
						break;
					}
				}
			}
			default:
				break;
			}

			if (regNum2 == 1) //�������Ͱ� 2���� ���
			{
				for (int k = reg_cnt - 1; k >= 0; k--) 
				{
					if (reg_x[k] >= 0) 
					{
						reg_x[reg_cnt] = reg_x[k];
						break;
					}
				}
			}

			reg_cnt++;
		} 
		else if (reg_r == 8) //pc����ּ���������� ���
		{
			reg_pc[reg_cnt] = loc;
			switch (regNum1) {	//�������� �ѹ��� ���� ���� ���̽� ����ּ�������İ� ������ ���
			case 0: 
			{
				if (flag == 0) 
				{
					reg_a[reg_cnt] = value;
				} 
				else 
				{
					for (int k = reg_cnt - 1; k >= 0; k--) 
					{
						if (reg_a[k] >= 0) 
						{
							reg_a[reg_cnt] = reg_a[k];
							break;
						}
					}
				}
				break;
			}
			case 1: {
				if (flag == 0) 
				{
					reg_x[reg_cnt] = value;
				} 
				else 
				{
					for (int k = reg_cnt - 1; k >= 0; k--) 
					{
						if (reg_x[k] >= 0) 
						{
							reg_x[reg_cnt] = reg_x[k];
							break;
						}
					}
				}
				break;
			}
			case 2: 
			{
				if (flag == 0) 
				{
					reg_l[reg_cnt] = value;
				} 
				else 
				{
					for (int k = reg_cnt - 1; k >= 0; k--) 
					{
						if (reg_l[k] >= 0) 
						{
							reg_l[reg_cnt] = reg_l[k];
							break;
						}
					}
				}
				break;
			}
			case 3: 
			{
				if (flag == 0) 
				{
					reg_b[reg_cnt] = value;
				} 
				else 
				{
					for (int k = reg_cnt - 1; k >= 0; k--) 
					{
						if (reg_b[k] >= 0) 
						{
							reg_b[reg_cnt] = reg_b[k];
							break;
						}
					}
				}
				break;
			}
			case 4: 
			{
				if (flag == 0) 
				{
					reg_s[reg_cnt] = value;
				} else {
					for (int k = reg_cnt - 1; k >= 0; k--) 
					{
						if (reg_s[k] >= 0) 
						{
							reg_s[reg_cnt] = reg_s[k];
							break;
						}
					}
				}
				break;
			}
			
			case 5: 
			{
				if (flag == 0) 
				{
					reg_t[reg_cnt] = value;
				} 
				else 
				{
					for (int k = reg_cnt - 1; k >= 0; k--) 
					{
						if (reg_t[k] >= 0) 
						{
							reg_t[reg_cnt] = reg_t[k];
							break;
						}
					}
				}
				break;
			}
			//�б� ��ɾ��� ���
			case 8:
			{
				reg_pc[reg_cnt]=value;
			}
			case 11:
			{
				for (int k = reg_cnt - 1; k >= 0; k--) 
				{
					if (reg_x[k] >= 0) 
					{
						reg_x[reg_cnt] = reg_x[k]+1;
						break;
					}
				}
			}
			default:
				break;
			}
			
			if (regNum2 == 1) 
			{
				for (int k = reg_cnt - 1; k >= 0; k--) 
				{
					if (reg_x[k] >= 0) 
					{
						reg_x[reg_cnt] = reg_x[k];
						break;
					}
				}
			}
			reg_cnt++;
		}
		
		else if (reg_r == -1) //�����ּ���������̰ų� 2������ ���
		{
			//2������ ���
			if(format==2)
			{
				if(inst.equals("ADDR"))	//addr�� ���
				{
					int rg1 = 0;
					switch(regNum1)
					{
					case 0:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_a[k] >= 0) 
							{
								rg1 = reg_a[k];
								reg_a[reg_cnt] = reg_a[k];
								break;
							}
						}
					}
					case 1:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_x[k] >= 0) 
							{
								rg1 = reg_x[k];
								reg_x[reg_cnt] = reg_x[k];
								break;
							}
						}
					}
					case 2:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_l[k] >= 0) 
							{
								rg1 = reg_l[k];
								reg_l[reg_cnt] = reg_l[k];
								break;
							}
						}
					}
					case 3:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_b[k] >= 0) 
							{
								rg1 = reg_b[k];
								reg_b[reg_cnt] = reg_b[k];
								break;
							}
						}
					}
					case 4:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_s[k] >= 0) 
							{
								rg1 = reg_s[k];
								reg_s[reg_cnt] = reg_s[k];
								break;
							}
						}
					}
					case 5:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_t[k] >= 0) 
							{
								rg1 = reg_t[k];
								reg_t[reg_cnt] = reg_t[k];
								break;
							}
						}
					}
					case 6:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_f[k] >= 0) 
							{
								rg1 = reg_f[k];
								reg_f[reg_cnt] = reg_f[k];
								break;
							}
						}
					}
					case 8:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_pc[k] >= 0) 
							{
								rg1 = reg_pc[k];
								reg_pc[reg_cnt] = reg_pc[k];
								break;
							}
						}
					}
					case 9:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_sw[k] >= 0) 
							{
								rg1 = reg_sw[k];
								reg_sw[reg_cnt] = reg_sw[k];
								break;
							}
						}
					}
						
					}
					switch(regNum2)
					{
					case 0:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_a[k] >= 0) 
							{
								reg_a[reg_cnt] = reg_a[k]+rg1;
								break;
							}
						}
					}
					case 1:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_x[k] >= 0) 
							{
								reg_x[reg_cnt] = reg_x[k]+rg1;
								break;
							}
						}
					}
					case 2:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_l[k] >= 0) 
							{
								reg_l[reg_cnt] = reg_l[k]+rg1;
								break;
							}
						}
					}
					case 3:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_b[k] >= 0) 
							{
								reg_b[reg_cnt] = reg_b[k]+rg1;
								break;
							}
						}
					}
					case 4:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_s[k] >= 0) 
							{
								reg_s[reg_cnt] = reg_s[k]+rg1;
								break;
							}
						}
					}
					case 5:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_t[k] >= 0) 
							{
								reg_t[reg_cnt] = reg_t[k]+rg1;
								break;
							}
						}
					}
					case 6:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_f[k] >= 0) 
							{
								reg_f[reg_cnt] = reg_f[k]+rg1;
								break;
							}
						}
					}
					case 8:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_pc[k] >= 0) 
							{
								reg_pc[reg_cnt] = reg_pc[k]+rg1;
								break;
							}
						}
					}
					case 9:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_sw[k] >= 0) 
							{
								reg_sw[reg_cnt] = reg_sw[k]+rg1;
								break;
							}
						}
					}
						
					}
				}
				
				else if(inst.equals("CLEAR"))	//clear�� ���
				{
					switch(regNum1)
					{
					case 0:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_a[k] >= 0) 
							{
								reg_a[reg_cnt] = 0;
								break;
							}
						}
					}
					case 1:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_x[k] >= 0) 
							{
								reg_x[reg_cnt] = 0;
								break;
							}
						}
					}
					case 2:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_l[k] >= 0) 
							{
								reg_l[reg_cnt] = 0;
								break;
							}
						}
					}
					case 3:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_b[k] >= 0) 
							{
								reg_b[reg_cnt] = 0;
								break;
							}
						}
					}
					case 4:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_s[k] >= 0) 
							{
								reg_s[reg_cnt] = 0;
								break;
							}
						}
					}
					case 5:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_t[k] >= 0) 
							{
								reg_t[reg_cnt] = 0;
								break;
							}
						}
					}
					case 6:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_f[k] >= 0) 
							{
								reg_f[reg_cnt] = 0;
								break;
							}
						}
					}
					case 8:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_pc[k] >= 0) 
							{
								reg_pc[reg_cnt] = 0;
								break;
							}
						}
					}
					case 9:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_sw[k] >= 0) 
							{
								reg_sw[reg_cnt] = 0;
								break;
							}
						}
					}
						
					}
				}
				else if(inst.equals("COMPR"))	//compr�� ���
				{
					switch(regNum1)
					{
					case 0:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_a[k] >= 0) 
							{
								reg_a[reg_cnt] = reg_a[k];
								break;
							}
						}
					}
					case 1:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_x[k] >= 0) 
							{
								reg_x[reg_cnt] = reg_x[k];
								break;
							}
						}
					}
					case 2:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_l[k] >= 0) 
							{
								reg_l[reg_cnt] = reg_l[k];
								break;
							}
						}
					}
					case 3:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_b[k] >= 0) 
							{
								reg_b[reg_cnt] = reg_b[k];
								break;
							}
						}
					}
					case 4:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_s[k] >= 0) 
							{
								reg_s[reg_cnt] = reg_s[k];
								break;
							}
						}
					}
					case 5:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_t[k] >= 0) 
							{
								reg_t[reg_cnt] = reg_t[k];
								break;
							}
						}
					}
					case 6:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_f[k] >= 0) 
							{
								reg_f[reg_cnt] = reg_f[k];
								break;
							}
						}
					}
					case 8:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_pc[k] >= 0) 
							{
								reg_pc[reg_cnt] = reg_pc[k];
								break;
							}
						}
					}
					case 9:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_sw[k] >= 0) 
							{
								reg_sw[reg_cnt] = reg_sw[k];
								break;
							}
						}
					}
						
					}
				}
				else //������ ��ɾ��� ���
				{
					switch(regNum1)
					{
					case 0:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_a[k] >= 0) 
							{
								reg_a[reg_cnt] = reg_a[k];
								break;
							}
						}
					}
					case 1:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_x[k] >= 0) 
							{
								reg_x[reg_cnt] = reg_x[k];
								break;
							}
						}
					}
					case 2:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_l[k] >= 0) 
							{
								reg_l[reg_cnt] = reg_l[k];
								break;
							}
						}
					}
					case 3:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_b[k] >= 0) 
							{
								reg_b[reg_cnt] = reg_b[k];
								break;
							}
						}
					}
					case 4:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_s[k] >= 0) 
							{
								reg_s[reg_cnt] = reg_s[k];
								break;
							}
						}
					}
					case 5:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_t[k] >= 0) 
							{
								reg_t[reg_cnt] = reg_t[k];
								break;
							}
						}
					}
					case 6:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_f[k] >= 0) 
							{
								reg_f[reg_cnt] = reg_f[k];
								break;
							}
						}
					}
					case 8:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_pc[k] >= 0) 
							{
								reg_pc[reg_cnt] = reg_pc[k];
								break;
							}
						}
					}
					case 9:
					{
						for (int k = reg_cnt - 1; k >= 0; k--) 
						{
							if (reg_sw[k] >= 0) 
							{
								reg_sw[reg_cnt] = reg_sw[k];
								break;
							}
						}
					}
						
					}
				}
			}
			
			else	//4���� �� �����ּ����� ����� ���
			{
				if(inst.contains("LD"))	//������ ��ɾ��� ���
				{
					switch (regNum1) {
					case 0: 
					{
							reg_a[reg_cnt] = value;
					
						break;
					}
					case 1: {
							reg_x[reg_cnt] = value;
				
						break;
					}
					case 2: 
					{
	
						reg_l[reg_cnt]=value;
						break;
					}
					case 3: 
					{
							reg_b[reg_cnt] = value;
						break;
					}
					case 4: 
					{
							reg_s[reg_cnt] = value;
						break;
					}
					
					case 5: 
					{
							reg_t[reg_cnt] = value;
						break;
					}
					//�б� ��ɾ��� ���
					case 8:
					{
						reg_pc[reg_cnt]=value;
					}
				
					default:
						break;
					}
				}
				else if(inst.contains("ST"))	//�����ɾ��� ���
				{
					switch (regNum1) {
					case 0: 
					{
							for (int k = reg_cnt - 1; k >= 0; k--) 
							{
								if (reg_a[k] >= 0) 
								{
									reg_a[reg_cnt] = reg_a[k];
									break;
								}
							}
						break;
					}
					case 1: {
							for (int k = reg_cnt - 1; k >= 0; k--) 
							{
								if (reg_x[k] >= 0) 
								{
									reg_x[reg_cnt] = reg_x[k];
									break;
								}
							}
						break;
					}
					case 2: 
					{
							for (int k = reg_cnt - 1; k >= 0; k--) 
							{
								if (reg_l[k] >= 0) 
								{
									reg_l[reg_cnt] = reg_l[k];
									break;
								}
							}
						break;
					}
					case 3: 
					{
							for (int k = reg_cnt - 1; k >= 0; k--) 
							{
								if (reg_b[k] >= 0) 
								{
									reg_b[reg_cnt] = reg_b[k];
									break;
								}
							}
						break;
					}
					case 4: 
					{
							for (int k = reg_cnt - 1; k >= 0; k--) 
							{
								if (reg_s[k] >= 0) 
								{
									reg_s[reg_cnt] = reg_s[k];
									break;
								}
							}
						break;
					}
					
					case 5: 
					{
							for (int k = reg_cnt - 1; k >= 0; k--) 
							{
								if (reg_t[k] >= 0) 
								{
									reg_t[reg_cnt] = reg_t[k];
									break;
								}
							}
						break;
					}
					//�б� ��ɾ��� ���
					case 8:
					{
						reg_pc[reg_cnt]=value;
					}
					default:
						break;
					}
				}
				else if(inst.equals("JSUB"))	//JSUB��ɾ��� ���
				{
					for (int k = reg_cnt - 1; k >= 0; k--) 
					{
						if (reg_pc[k] >= 0) 
						{
							reg_l[reg_cnt] = reg_pc[k];
							break;
						}
					}
					reg_pc[reg_cnt]=value;
				}
				else if(inst.equals("RSUB"))	//RSUB��ɾ��� ���
				{
					for (int k = reg_cnt - 1; k >= 0; k--) 
					{
						if (reg_l[k] >= 0) 
						{
							reg_pc[reg_cnt] = reg_l[k];
							break;
						}
					}
				}
				
			}
			reg_cnt++;
		}
	}

	// �޸� �������� ���� �о���� �޼ҵ�
	public byte[] getMemory(int locate, int size) {
		byte[] by = null;
		int k=0;
		for(int i=locate; i<locate+size; i++)
		{
			by[k]=memory[i];
			k++;
		}
		return by;
	}

	// �������Ϳ��� ���� �������� �޼ҵ�
	public int getRegister(int regNum,int step) {
		switch(regNum)
		{
		case 0:
		{
			return reg_a[step];
		}
		case 1:
		{
			return reg_x[step];
		}
		case 2:
		{
			return reg_l[step];
		}
		case 3:
		{
			return reg_b[step];
		}
		case 4:
		{
			return reg_s[step];
		}
		case 5:
		{
			return reg_t[step];
		}
		case 6:
		{
			return reg_f[step];
		}
		case 8:
		{
			return reg_pc[step];
		}
		case 9:
		{
			return reg_sw[step];
		}
		default:
			break;
		}
		return step;
	}

}
