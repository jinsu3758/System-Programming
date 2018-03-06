package project;

import java.util.ArrayList;

public class ResourceManager extends VisualSimulator {

	
	static String pro_name;	//프로그램의 이름을 저장(서브루틴 포함)
	static int start_addr;	//프로그램의 시작주소 
	static int length;	//프로그램의 길이
	static int first;	//시작주소
	static int[] r_cnt = new int[10];	//절대주소를 위해 길이를 저장
	static int reg_cnt = 0;		//레지스터의 인덱스
	static String device_name=null;	//디바이스 이름
	byte[] device = new byte[100];	//디바이스의 값을 저장
	byte[] memory = new byte[10000];	//가상 메모리
	static int[] target = new int[300];	//
	static int[] reg_a = new int[300];	//
	static int[] reg_x = new int[300];	//	각 레지스터의 배열
	static int[] reg_l = new int[300];	//	(명령어 마다)
	static int[] reg_b = new int[300];	//
	static int[] reg_s = new int[300];	//
	static int[] reg_t = new int[300];	//
	static int[] reg_f = new int[300];	//
	static int[] reg_pc = new int[300];	//
	static int[] reg_sw = new int[300];	//

	// 메모리 영역을 초기화 하는 메소드
	public void initializeMemory() {

		for(int i=0; i<10000; i++)
		{
			memory[i]=0;
		}
	}

	// 각 레지스터 값을 초기화 하는 메소드
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

	// 디바이스 접근에 대한 메소드
	// 디바이스는 각 이름과 매칭되는 파일로 가정한다
	// (F1 이라는 디바이스를 읽으면 F1 이라는 파일에서 값을 읽는다.)
	// 해당 디바이스(파일)를 사용 가능한 상태로 만드는 메소드
	public void initialDevice(String devName) {
		device_name=devName;
	}

	// 선택한 디바이스(파일)에 값을 쓰는 메소드. 파라미터는 변경 가능하다.
	public void writeDevice(String devName, byte[] data, int size) {
		if(device_name==devName)
		{
			for(int i=0; i<size; i++)
			{
				device[i]=data[i];
			}
		}
	}

	// 선택한 디바이스(파일)에서 값을 읽는 메소드. 파라미터는 변경 가능하다.
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

	// 메모리 영역에 값을 쓰는 메소드
	public void setMemory(int locate, byte[] data, int size) 
	{
		for(int i=0; i<size; i++)
		{
			memory[locate]=data[i];
			locate++;
		}
	}



	// 프로그램의 이름를 지정
	public void setName(String str) {
		pro_name=str;
	}
	//레지스터에 값을 세팅하는 메소드 str은 명령어/value는 값/cnt는 레지스터 순서
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

	// 레지스터에 값을 세팅하는 메소드. regNum1,2은 레지스터 종류를 나타낸다.
	//reg_r은 주소방식	value는 값  \loc는 LOCCTR \flag 는 저장,로드 명령어 \format은 명령어의 형식 \ inst는 명령어 이름
	//flag 0은 적재/1은 저장/-1은 다른 명령어   reg_r은 상대주소지정방식에 따라 3은 베이스, 8은 pc, -1은 절대주소지정방식 또는 2형식이다.
	public void setRegister(int regNum1, int regNum2, int reg_r, int value, int loc, int flag,int format, String inst) 
	{
		target[reg_cnt]=value;	//타겟주소 저장
		if (reg_r == 3) //베이스 레지스터를 사용하는 경우
		{
			for (int k = reg_cnt - 1; k >= 0; k--) 
			{
				if (reg_b[k] >= 0) 
				{
					reg_b[reg_cnt] = reg_b[k];
					break;
				}
			}
			//레지스터 넘버에 따라 나눔
			switch (regNum1) {
			case 0: 
			{
				if (flag == 0)	//A레지스터, flag는 저장/적재 명령어를 나눈 기준 0은 적재
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
			case 1: {	//X레지스터인 경우
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
			case 3: //base레지스터인 경우
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
			case 4: //S레지스터인 경우
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
			
			case 5: //t레지스터인 경우
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
			//분기 명령어일 경우
			case 8:
			{
				reg_pc[reg_cnt]=value;
			}
			//TIX일 경우
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

			if (regNum2 == 1) //레지스터가 2개인 경우
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
		else if (reg_r == 8) //pc상대주소지정방식인 경우
		{
			reg_pc[reg_cnt] = loc;
			switch (regNum1) {	//레지스터 넘버에 따라 나눔 베이스 상대주소지정방식과 동일한 방식
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
			//분기 명령어일 경우
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
		
		else if (reg_r == -1) //절대주소지정방식이거나 2형식일 경우
		{
			//2형식일 경우
			if(format==2)
			{
				if(inst.equals("ADDR"))	//addr인 경우
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
				
				else if(inst.equals("CLEAR"))	//clear인 경우
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
				else if(inst.equals("COMPR"))	//compr인 경우
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
				else //나머지 명령어인 경우
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
			
			else	//4형식 등 절대주소지정 방식일 경우
			{
				if(inst.contains("LD"))	//적재방식 명령어일 경우
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
					//분기 명령어일 경우
					case 8:
					{
						reg_pc[reg_cnt]=value;
					}
				
					default:
						break;
					}
				}
				else if(inst.contains("ST"))	//저장명령어일 경우
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
					//분기 명령어일 경우
					case 8:
					{
						reg_pc[reg_cnt]=value;
					}
					default:
						break;
					}
				}
				else if(inst.equals("JSUB"))	//JSUB명령어일 경우
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
				else if(inst.equals("RSUB"))	//RSUB명령어일 경우
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

	// 메모리 영역에서 값을 읽어오는 메소드
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

	// 레지스터에서 값을 가져오는 메소드
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
