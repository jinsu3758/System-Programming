package project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

//시뮬레이터를 동작시키기 위한 세팅을 수행한다.
//메모리 작업 등 실질적인 초기화 작업을 수행한다.
public class SICXE extends VisualSimulator implements SicSimulator, SicLoader {

	BufferedReader br = null;
	static Token_unit[] ref = new Token_unit[50];	//참조값을 저장하는 객체배열
	static Token_unit[] def = new Token_unit[50];	//정의값을 저장한는 객체배열
	static Token_unit[] mod = new Token_unit[50];	//수정값을 저장하는 객체배열
	static Token_unit[] dev = new Token_unit[50];	//디바이스의 이름과 주소를 저장하는 객체배열
	static int ref_cnt=0;	//참조값 개수
	static int def_cnt=0;	//정의값 개수
	static int sub_cnt=0;	//루틴의 개수
	static int mod_cnt=0;	//수정값 개수
	static int dev_cnt=0;	//디바이스 개수
 	static byte[] r_byte = new byte[100];	//디바이스에서 읽어올 바이트배열
	static ArrayList<Integer> f_mod = new ArrayList<Integer>();	//mod레코드에서 상대주소를 저장하는 리스트
	static ArrayList<String> inst = new ArrayList<String>();	//명령어의 주소를 저장하는 리스트
	static ArrayList<String> inst_name = new ArrayList<String>();	//명령어의 이름을 저장하는 리스트
	static ArrayList<String> start = new ArrayList<String>();	//루틴의 시작주소
	static ArrayList<String> r_start = new ArrayList<String>();	//루틴의 실제시작주소
	static ArrayList<String> name = new ArrayList<String>();	//루틴의 이름
	static ArrayList<String> t_start = new ArrayList<String>();	//T레코드의 시작주소
	static ArrayList<String> t_length = new ArrayList<String>();	//T레코드의 길이
	static ArrayList<String> length = new ArrayList<String>();	//루틴의 길이
	static ArrayList<String> target = new ArrayList<String>();	//target 리스트
	static int loc=0;	//loc값
	

	//목적코드를 읽어 메모리에 로드한다.
    //목적코드의 각 헤더(H, T, M 등)를 읽어 동작을 수행한다.
	@Override
	public void load(File objFile, ResourceManager rMgr) 
	{
		String line;
		int b_cnt=0;
		byte[] by = new byte[3];	//메모리에 저장할떄 바이트로 변환(packing)할 바이트배열
		//각 token을 초기화
		for(int i=0; i<50;  i++)
		{
			ref[i]=new Token_unit(null, 0);
			def[i]=new Token_unit(null, 0); 
			mod[i]=new Token_unit(null, 0); 
			dev[i]=new Token_unit(null, 0);
		}
		try {
			br = new BufferedReader(new FileReader(objFile));
			//object program을 한 줄씩 읽어들임
			while((line=br.readLine())!=null)
			{
				if(line.charAt(0)=='H')	//H레코드일 경우
				{
					sub_cnt++;
					name.add(line.substring(1,7));
					//메인 프로그램일 경우
					if(sub_cnt==1)
					{
						rMgr.setName(line.substring(1,7));	
					}
					//서브루틴인 경우에는 상대주소가 아닌 절대주소로 계산
					if(sub_cnt>=2)
					{
						int s1 = 0;
						int s2 = 0;
						for(int i=start.size()-1; i>=0; i--)
						{
							s1+=Integer.parseInt(length.get(i),16);
						}
						s2=Integer.parseInt(line.substring(7,13),16);
						loc=s1+s2;
						String s=String.format("%06X", s1+s2);
						r_start.add(s);
						System.out.println(s);
						b_cnt=0;
						b_cnt+=Integer.parseInt(s,16);
					}
					start.add(line.substring(7,13));
					length.add(line.substring(13,19));
				}

				else if(line.charAt(0)=='D')	//D레코드일 경우 def 토큰에 주소,이름,무슨루틴인지를 각각 저장
				{
					
					def[def_cnt].sub_cnt=sub_cnt;
					def[def_cnt].name=line.substring(1,7);
					if(sub_cnt>=2)
					{
						def[def_cnt++].addr=Integer.parseInt(line.substring(7,13),16)
								+Integer.parseInt(r_start.get(sub_cnt-2),16);
					}
					else
					{
						def[def_cnt++].addr=Integer.parseInt(line.substring(7,13),16);
					}
					if(line.length()>14)
					{
						def[def_cnt].sub_cnt=sub_cnt;
						def[def_cnt].name=line.substring(13,19);
						if(sub_cnt>=2)
						{
							def[def_cnt++].addr=Integer.parseInt(line.substring(19,25),16)
									+Integer.parseInt(r_start.get(sub_cnt-2),16);
						}
						else
						{
							def[def_cnt++].addr=Integer.parseInt(line.substring(19,25),16);
						}
								
					}
					if(line.length()>26)
					{
						def[def_cnt].sub_cnt=sub_cnt;
						def[def_cnt].name=line.substring(25,31);
						if(sub_cnt>=2)
						{
							def[def_cnt++].addr=Integer.parseInt(line.substring(31,line.length()),16)
									+Integer.parseInt(r_start.get(sub_cnt-2),16);
						}
						else
						{
							def[def_cnt++].addr=Integer.parseInt(line.substring(31,line.length()),16);
						}
					}
				}
				
				else if(line.charAt(0)=='R')	//R레코드일 경우도 D레코드와 동일하게 토큰에 저장
				{
					ref[ref_cnt].sub_cnt=sub_cnt;
					ref[ref_cnt++].name=line.substring(1,7);
					if(line.length()>8)
					{
						ref[ref_cnt].sub_cnt=sub_cnt;
						ref[ref_cnt++].name=line.substring(7,13);
					}
					if(line.length()>14)
					{
						ref[ref_cnt].sub_cnt=sub_cnt;
						ref[ref_cnt++].name=line.substring(13,line.length());
					}
				}
				//T레코드일 경우 길이에 맞게 파싱하여 명령어를 구분하고 메모리에 로더하고 레지스터값 지정
				else if(line.charAt(0)=='T')	
				{
					t_start.add(line.substring(1,7));	//T레코드의 시작주소 저장
					t_length.add(line.substring(7,9));	//T레코드의 길이 저장
					loc+=Integer.parseInt(line.substring(1,7),16);	
					int len = Integer.parseInt(line.substring(7,9),16);	//레코드의 길이
					int cnt=9;	//파싱하기 위한 count변수
					while(true)
					{
						if((cnt/2)>len+3)	//길이를 넘었을 경우 반복문 종료
						{
							break;
						}
						if(line.length()<cnt+3)	//레코드 마지막에 바이트만 있을 경우 메모리에 로더하고 반복문 종료
						{
							if(Integer.parseInt(line.substring(cnt,cnt+2),16)==0)
							{
								break;
							}
							by=byteorder(line.substring(cnt,cnt+2), 2);
							dev[dev_cnt].sub_cnt=sub_cnt;
							dev[dev_cnt].addr=loc;
							dev[dev_cnt++].name=line.substring(cnt,cnt+2);
							rMgr.setMemory(b_cnt, by, 1);
							b_cnt+=1;
							cnt+=2;
							break;
						}
						
						int op=Integer.parseInt(line.substring(cnt,cnt+2),16);	//op코드 저장(ni값도 포함)
						int xbpe=Integer.parseInt(line.substring(cnt+2,cnt+3),16);	//xbpe값 저장
						int cal=Integer.parseInt(line.substring(cnt+2,cnt+4),16);	//명령어 구분을 위한 값
						int k = search_opcode(op);	//op코드에 따라 명령어인지 구분
						if(inst_table[k].format==2)	//2형식일 경우 xbpe=0
						{
							xbpe=0;
						}
						
						if(k>-1)	//명령어일 경우
						{
							int jp=0;
							int jp2=0;
							int jp3=0;
							String iname = inst_table[k].name;	//명령어의 이름
							//op코드가 같은 값일 경우를 대비한 if문
							if(xbpe>15 || inst_table[k].format!=2 && (cal>=65 && cal<=90))
							{
								jp=Integer.parseInt(line.substring(cnt,cnt+2),16);
								if (line.length() > (cnt-9)*2+5) 
								{
									jp2 = Integer.parseInt(line.substring(cnt + 2, cnt + 4), 16);
								}
								if (line.length() > (cnt-9)*2 +9 ) 
								{
									jp3 = Integer.parseInt(line.substring(cnt + 4, cnt + 6), 16);
								}
								if(jp3<=65 && jp2<=65)
								{
									by=byteorder(line.substring(cnt,cnt+2), 2);
									rMgr.setMemory(b_cnt, by, 1);
									b_cnt++;
									cnt+=2;
								}
								else
								{
									by=byteorder(line.substring(cnt,cnt+6), 6);
									rMgr.setMemory(b_cnt, by, 3);
									b_cnt+=3;
									cnt+=6;
								}
							}
							//명령어인 경우
							else
							{
								inst_name.add(iname);	//명령어 이름을 추가
								//3형식일 경우 크기 6의 문자열을 3바이트로 파싱하여 메모리에 올림
								if (xbpe % 2 == 0 && inst_table[k].format==3) 
								{
									inst.add(line.substring(cnt, cnt + 6));
									by=byteorder(line.substring(cnt,cnt+6), 6);
									rMgr.setMemory(b_cnt, by, 3);
									b_cnt+=3;
									cnt+=6;
									loc+=3;
								} 
								//2형식일 경우
								else if(inst_table[k].format==2) 
								{
									inst.add(line.substring(cnt, cnt + 4));
									by=byteorder(line.substring(cnt,cnt+4), 4);
									rMgr.setMemory(b_cnt, by, 2);
									b_cnt+=2;
									cnt+=4;
									loc+=2;
								}
								//4형식일 경우
								else if(xbpe>=1)
								{
									inst.add(line.substring(cnt, cnt + 8));
									by=byteorder(line.substring(cnt,cnt+8), 8);
									rMgr.setMemory(b_cnt, by, 4);
									b_cnt+=4;
									cnt+=8;
									loc+=4;
								}
								//명령어가 아닌 경우를 대비
								else if(xbpe==0)
								{
									inst_name.remove(inst_name.size()-1);
									by=byteorder(line.substring(cnt,cnt+2), 2);
									dev[dev_cnt].sub_cnt=sub_cnt;
									dev[dev_cnt].addr=loc;
									dev[dev_cnt++].name=line.substring(cnt,cnt+2);
									rMgr.initialDevice(line.substring(cnt,cnt+2));
									rMgr.setMemory(b_cnt, by, 1);
									b_cnt+=1;
									cnt+=6;
								}
								

								//레지스터 A를 쓰고 3/4형식일 경우 레지스터값 저장
								if (iname.equals("COMP") || iname.equals("AND") || iname.equals("ADD") || iname.equals("LDA")
										|| iname.equals("DIV") || iname.equals("LDCH") || iname.equals("MUL")
										|| iname.equals("OR") || iname.equals("RD") || iname.equals("SUB")|| iname.equals("WD")
										|| iname.equals("SSK") || iname.equals("STA") || iname.equals("STCH")) 
								{
									if(xbpe>=8 && iname.equals("STCH"))	//x레지스터를 사용하고 STCH명령어일 경우
									{
										if(xbpe==12)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(0, 1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
													,inst_table[k].name);
										}
										else if(xbpe==10)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(0,1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,1,3,
													inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(0, 1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3,
														inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(0, 1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 1, 4,
														inst_table[k].name);
											}
										}
									}
									else if(xbpe>=8 && iname.equals("LDCH"))	//x레지스터를 사용하고 LDCH인 경우
									{
										if(xbpe==12)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(0, 1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==10)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(0,1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3
													,inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(0, 1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(0, 1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
									else if(iname.contains("ST"))	//store명령어일 경우
									{
										if(xbpe==4)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(0, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(0,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,1,3
													,inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(0, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
														,inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(0, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 1, 4
														,inst_table[k].name);
											}
										}
									}
									else	//나머지 명령어일 경우
									{
										if(xbpe==4)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(0, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(0,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3,inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(0, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(0, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
								} 
								//B레지스터에 로드하거나 메모리에 B레지스터를 저장한경우
								else if (iname.equals("LDB") || iname.equals("STB")) 
								{
									if(iname.equals("LDB"))
									{
										if(xbpe==4)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(3, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(3,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3,inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(3, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(3, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
									else
									{
										if(xbpe==4)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(3, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(3,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,1,3,inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(3, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
														,inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(3, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 1, 4
														,inst_table[k].name);
											}
										}
									}

								} 
								//T레지스터를 사용하는 경우
								else if (iname.equals("LDT") || iname.equals("STT"))
								{
									if(iname.equals("LDT"))
									{
										if(xbpe==4)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(5, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(5,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3,inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(5, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(5, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
									else
									{
										if(xbpe==4)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(5, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(5,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,1,3,inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(5, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
														,inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(5, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 1, 4
														,inst_table[k].name);
											}
										}
									}
								} 
								//S레지스터를 사용하는 경우
								else if (iname.equals("LDS") || iname.equals("STS"))
								{
									if(iname.equals("LDS"))
									{
										if(xbpe==4)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(4, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(4,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3,inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(4, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(4, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
									else
									{
										if(xbpe==4)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(4, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(4,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,1,3,inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(4, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
														,inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(4, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 1, 4
														,inst_table[k].name);
											}
										}
									}
								} 
								//L레지스터를 사용하는 경우
								else if (iname.equals("LDL") || iname.equals("STL")) 
								{
									if(iname.equals("LDL"))
									{
										if(xbpe==4)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(2, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(2,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3,inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(2, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(2, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
									else
									{
										if(xbpe==4)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(2, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(2,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,1,3,inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(2, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
														,inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(2, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 1, 4
														,inst_table[k].name);
											}
										}
									}
								} 
								//X레지스터를 사용하고 3/4형식일 경우
								else if (iname.equals("TIX") || iname.equals("STX") || iname.equals("LDX")) 
								{
									if(iname.equals("LDX"))
									{
										if(xbpe==4)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(1, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(1,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3,inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(1, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(1, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
									else if(iname.equals("STX"))
									{
										if(xbpe==4)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(1, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(1,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,1,3,inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(1, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
														,inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(1, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 1, 4
														,inst_table[k].name);
											}
										}
									}
									else
									{
										if(xbpe==4)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(11, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(11,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3,inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(11, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(11, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
								} 
								//PC레지스터를 이용하여 분기하는 경우
								else if (iname.equals("J") || iname.equals("JSUB") || iname.equals("RSUB") || iname.equals("JEQ")
										|| iname.equals("JLT") || iname.equals("JGT")) 
								{
									if(iname.equals("JSUB"))
									{
										if(xbpe%2==0)	//3형식일 경우
										{
										rMgr.setRegister(82, -1, -1,
												Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
												,inst_table[k].name);
										}
										else	//4형식일 경우
										{
											rMgr.setRegister(82, -1, -1,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
													,inst_table[k].name);
										}
									}
									else if(iname.equals("RSUB"))	//명령어가 RSUB인 경우
									{
										rMgr.setRegister(81, -1, -1,
												Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
												,inst_table[k].name);
									}
									else
									{
										if(xbpe==4)	//base 레지스터를 사용하는 경우
										{
											rMgr.setRegister(8, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc 레지스터를 사용하는 경우
										{
											rMgr.setRegister(8,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3
													,inst_table[k].name);
										}
										else	//절대 주소일 경우
										{
											if(xbpe%2==0)	//직접주소지정 방식일 경우
											{
												rMgr.setRegister(8, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4형식일 경우
											{
												rMgr.setRegister(8, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
								}

								//2형식일 경우
								//레지스터가 2개일 경우와 1개일 경우를 나눠서 레지스터값 저장
								else if(inst_table[k].format==2)
								{
									if(iname.equals("ADDR") || iname.equals("CLEAR") || iname.equals("DIVR") || iname.equals("COMPR")
											|| iname.equals("MULR") || iname.equals("RMO") || iname.equals("SUBR") || 
											iname.equals("SHIFTR") || iname.equals("SHIFTL"))
									{
										rMgr.setRegister(Integer.parseInt(inst.get(inst.size()-1)
												.substring(2),16), Integer.parseInt(inst.get(inst.size()-1)
														.substring(3),16), -1, 0, 0, 0, 2,inst_table[k].name);
									}
									else
									{
										rMgr.setRegister(Integer.parseInt(inst.get(inst.size()-1)
												.substring(2),16), -1, -1, 0, 0, 0, 2,inst_table[k].name);
									}
								}
						
							}
						
						}
						//만약을 대비한 명령어가 아닌 경우
						else
						{
							if(xbpe>15 && inst_table[k].format!=2 || cal>=65)
							{
								int jp=Integer.parseInt(line.substring(cnt,cnt+2),16);
								int jp2=Integer.parseInt(line.substring(cnt+2,cnt+4),16);
								int jp3=Integer.parseInt(line.substring(cnt+4,line.length()),16);
								if(jp3<=65)
								{
									cnt+=2;
								}
								else
								{
									cnt+=6;
								}
							}
							
						}
					
					}
				}
				//수정 레코드
				//mod 토큰에 이름과 주소를 저장 주소의 경우에는 절대주소로 고치기 위해 절대주소로 고쳐 저장
				else if(line.charAt(0)=='M')
				{
					if(sub_cnt>=2)
					{
						mod[mod_cnt].addr=Integer.parseInt(line.substring(1,7),16)
								+Integer.parseInt(r_start.get(sub_cnt-2),16);
					}
					else
					{
						mod[mod_cnt].addr=Integer.parseInt(line.substring(1,7),16);
								
					}
					f_mod.add(Integer.parseInt(line.substring(1,7),16));
					mod[mod_cnt].mod_n=Integer.parseInt(line.substring(7,9),16);
					//연산이 있을 경우
					if (mod_cnt >= 1) 
					{
						if ((mod[mod_cnt].addr == mod[mod_cnt - 1].addr) && line.substring(9, 10).equals("+")) 
						{
							mod[mod_cnt].is_op = 1;
						}
						if ((mod[mod_cnt].addr == mod[mod_cnt - 1].addr) && line.substring(9, 10).equals("-")) 
						{
							mod[mod_cnt].is_op = 2;
						}
					}
					mod[mod_cnt++].name=line.substring(10,line.length());
				}
				//E레코드일 경우
				else if(line.charAt(0)=='E')
				{
					rMgr.r_cnt[sub_cnt-1]=inst.size()+1;
				}
			}
			//서브루틴이나 메인프로그램이 끝난 경우 mod를 통해 modification 수행
			int m_cnt=0;
			String m=null;
			for(int i=0; i<mod_cnt; i++)
			{
				int op1=0;
				int op2=0;
				
				if(mod[i].mod_n==5)	//5자리를 수정해야 할 경우
				{
					for(int j=0; j<def_cnt;j++)
					{
						if(def[j].name.contains(mod[i].name))
						{
							//연산이 있을 경우 연산을 수행하여 저장
							if(mod[i].is_op==1)	//+일 경우
							{
								for(int k=0; k<def_cnt; k++)
								{
									if(def[k].name.contains(mod[i-1].name))
									{
										op1=def[k].addr;
									}
								}
								op2=def[j].addr;
								m=String.format("%04X",op1+op2);
							}
							else if(mod[i].is_op==2)	//-일 경우
							{
								for(int k=0; k<def_cnt; k++)
								{
									if(def[k].name.contains(mod[i-1].name))
									{
										op1=def[k].addr;
									}
								}
								op2=def[j].addr;
								m=String.format("%04X",op1-op2);	//수정할 주소에 4자리를 수정
							}
							else	//연산이 아닐 경우 4자리를 수정
							{
								m=String.format("%04X",def[j].addr);
							}
							break;
						}
					}
					for(int k=0; k<name.size();k++)
					{
						if(name.get(k).contains(mod[i].name))
						{
							m=r_start.get(k-1).substring(2,r_start.get(k-1).length());
							//m=start.get(k).substring(2,start.get(k).length());
							break;
						}
					}
					//절대주소를 계산 하여 레지스터값을 변경
					int mor=(int)((f_mod.get(i)+2)/3);
					if(inst_name.get(mor-1).equals("JSUB"))
					{
						rmgr.setRe(inst_name.get(mor-1), Integer.parseInt(m,16), mor-1);
					}
					else if(inst_name.get(mor-1).equals("STCH"))
					{
						rmgr.setRe(inst_name.get(mor-1), Integer.parseInt(m,16), mor-1);
					}
					else if(inst_name.get(mor-1).equals("STX"))
					{
						rmgr.setRe(inst_name.get(mor-1), Integer.parseInt(m,16), mor-1);
					}
					else if(inst_name.get(mor-1).equals("LDT"))
					{
						rmgr.setRe(inst_name.get(mor-1), Integer.parseInt(m,16), mor-1);
					}
					else if(inst_name.get(mor-1).equals("LDCH"))
					{
						rmgr.setRe(inst_name.get(mor-1), Integer.parseInt(m,16), mor-1);
					}
					by=byteorder(m, 4);
					rMgr.setMemory(mod[i].addr+1, by, 2);
				}
				else	//6자리를 수정해야 할 경우 위와 방식은 동일 6자리로 수정만 다름
				{
						for(int j=0; j<def_cnt;j++)
						{
							if(def[j].name.contains(mod[i].name))
							{
								if(mod[i].is_op==1)
								{
									for(int k=0; k<def_cnt; k++)
									{
										if(def[k].name.contains(mod[i-1].name))
										{
											op1=def[k].addr;
										}
									}
									op2=def[j].addr;
									m=String.format("%0X",op1+op2);
								}
								else if(mod[i].is_op==2)
								{
									for(int k=0; k<def_cnt; k++)
									{
										if(def[k].name.contains(mod[i-1].name))
										{
											op1=def[k].addr;
										}
									}
									op2=def[j].addr;
									m=String.format("%06X",op1-op2);
								}
								else
								{
									m=String.format("%06X",def[j].addr);
								}
								break;
							}
						}
						for(int k=0; k<name.size();k++)
						{
							if(name.get(k).contains(mod[i].name))
							{
								m=r_start.get(k-1);
								//m=start.get(k);
								break;
							}
						}
						by=byteorder(m, 6);
						rMgr.setMemory(mod[i].addr, by, 3);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	//메모리와 레지스터를 초기화 하는 함수
	@Override
	public void initialize(File objFile, ResourceManager rMgr) 
	{
		rMgr.initializeMemory();
		rMgr.initializeRegister();

	}

	//하나의 명령어만 수행한다. 해당 명령어가 수행되고 난 값의 변화를 
    //보여주고, 다음 명령어를 포인팅한다.
    //실질적인 동작을 수행하는 메소드
	@Override
	public void oneStep(int cnt,int step) {
		//디바이스를 불러오는 경우
		if(inst_name.size()>step)
		{
			if (inst_name.get(step).equals("TD")) 
			{
				for (int i = 0; i < dev_cnt; i++) 
				{
					if (dev[i].sub_cnt == cnt) 
					{
						rmgr.initialDevice(dev[i].name);
					}
				}

			} else if (inst_name.get(step).equals("RD")) 
			{

				for (int i = 0; i < dev_cnt; i++) 
				{
					if (dev[i].sub_cnt == cnt) 
					{
						r_byte = rmgr.readDevice(dev[i].name, rmgr.reg_x[cnt]);
						rmgr.initialDevice(dev[i].name);
					}
				}
			}
		else if(inst_name.get(step).equals("WD"))
		{
			byte [] data = new byte[10];
			for(int i=0; i<dev_cnt; i++)
			{
				if(dev[i].sub_cnt==cnt)
				{
					rmgr.initialDevice(dev[i].name);
					rmgr.writeDevice(dev[i].name, data, rmgr.reg_x[cnt]);
					break;
				}
			}
			
		}
		//디바이스를 사용안하는 경우
		else
		{
			rmgr.initialDevice(null);
		}
		}
		rmgr.setName(name.get(cnt-1));

	}

	//남은 명령어를 모두 수행하는 메소드.
    //목적 코드를 모두 수행하고 난 값의 변화를 보여준다.
	@Override
	public void allStep() {
		rmgr.initialDevice(null);

	}


	//op코드를 받아 명령어를 찾아주는 함수(ni값도 고려하여)
    //반환 값 : 명령어의 index
	public static int search_opcode(int op) 
	{
		int k = -1;
		for (int i = 0; i < inst_num; i++) 
		{
			if ((op - 3) == inst_table[i].opcode) 
			{
				return i;
			} else if ((op - 2) == inst_table[i].opcode) 
			{
				return i;
			} else if ((op - 1) == inst_table[i].opcode) 
			{
				return i;
			} else if (op == inst_table[i].opcode) 
			{
				return i;
			}
		}
		return k;
	}
	
	//문자열을 길이에 맞게 바이트로 parsing해주는 함수
	//반환값 : parsing된 바이트 배열
	//매개변수 : str=파싱할 문자열, len=파싱할 문자열의 길이 
	public static byte[] byteorder(String str,int len)
	{
		int s1;
		int s2;
		int s3;
		int s4;
		if(len==6)	//일반 명령어일 경우
		{
			byte[] by = new byte[3];
			s1=Integer.parseInt(str.substring(0,2),16);
			s2=Integer.parseInt(str.substring(2,4),16);
			s3=Integer.parseInt(str.substring(4,str.length()),16);
			by[0]=(byte)s1;
			by[1]=(byte)s2;
			by[2]=(byte)s3;
			return by;
		}
		else if(len==4)	//2형식이거나 길이가 2일 경우
		{
			byte[] by3 = new byte[2];
			s1=Integer.parseInt(str.substring(0,2),16);
			s2=Integer.parseInt(str.substring(2,str.length()),16);
			by3[0]=(byte)s1;
			by3[1]=(byte)s2;
			return by3;
		}
		//4형식일 경우
		else if(len==8)
		{
			byte[] by4 = new byte[4];
			s1=Integer.parseInt(str.substring(0,2),16);
			s2=Integer.parseInt(str.substring(2,4),16);
			s3=Integer.parseInt(str.substring(4,6),16);
			s4=Integer.parseInt(str.substring(6,str.length()),16);
			by4[0]=(byte)s1;
			by4[1]=(byte)s2;
			by4[2]=(byte)s3;
			by4[3]=(byte)s4;
			return by4;
		}
		//1바이트일 경우
		else
		{
			byte[] by2 = new byte[1];
			s1=Integer.parseInt(str.substring(0,str.length()),16);
			by2[0]=(byte)s1;
			return by2;
		}

		
	}
}
