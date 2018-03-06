package project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

//�ùķ����͸� ���۽�Ű�� ���� ������ �����Ѵ�.
//�޸� �۾� �� �������� �ʱ�ȭ �۾��� �����Ѵ�.
public class SICXE extends VisualSimulator implements SicSimulator, SicLoader {

	BufferedReader br = null;
	static Token_unit[] ref = new Token_unit[50];	//�������� �����ϴ� ��ü�迭
	static Token_unit[] def = new Token_unit[50];	//���ǰ��� �����Ѵ� ��ü�迭
	static Token_unit[] mod = new Token_unit[50];	//�������� �����ϴ� ��ü�迭
	static Token_unit[] dev = new Token_unit[50];	//����̽��� �̸��� �ּҸ� �����ϴ� ��ü�迭
	static int ref_cnt=0;	//������ ����
	static int def_cnt=0;	//���ǰ� ����
	static int sub_cnt=0;	//��ƾ�� ����
	static int mod_cnt=0;	//������ ����
	static int dev_cnt=0;	//����̽� ����
 	static byte[] r_byte = new byte[100];	//����̽����� �о�� ����Ʈ�迭
	static ArrayList<Integer> f_mod = new ArrayList<Integer>();	//mod���ڵ忡�� ����ּҸ� �����ϴ� ����Ʈ
	static ArrayList<String> inst = new ArrayList<String>();	//��ɾ��� �ּҸ� �����ϴ� ����Ʈ
	static ArrayList<String> inst_name = new ArrayList<String>();	//��ɾ��� �̸��� �����ϴ� ����Ʈ
	static ArrayList<String> start = new ArrayList<String>();	//��ƾ�� �����ּ�
	static ArrayList<String> r_start = new ArrayList<String>();	//��ƾ�� ���������ּ�
	static ArrayList<String> name = new ArrayList<String>();	//��ƾ�� �̸�
	static ArrayList<String> t_start = new ArrayList<String>();	//T���ڵ��� �����ּ�
	static ArrayList<String> t_length = new ArrayList<String>();	//T���ڵ��� ����
	static ArrayList<String> length = new ArrayList<String>();	//��ƾ�� ����
	static ArrayList<String> target = new ArrayList<String>();	//target ����Ʈ
	static int loc=0;	//loc��
	

	//�����ڵ带 �о� �޸𸮿� �ε��Ѵ�.
    //�����ڵ��� �� ���(H, T, M ��)�� �о� ������ �����Ѵ�.
	@Override
	public void load(File objFile, ResourceManager rMgr) 
	{
		String line;
		int b_cnt=0;
		byte[] by = new byte[3];	//�޸𸮿� �����ҋ� ����Ʈ�� ��ȯ(packing)�� ����Ʈ�迭
		//�� token�� �ʱ�ȭ
		for(int i=0; i<50;  i++)
		{
			ref[i]=new Token_unit(null, 0);
			def[i]=new Token_unit(null, 0); 
			mod[i]=new Token_unit(null, 0); 
			dev[i]=new Token_unit(null, 0);
		}
		try {
			br = new BufferedReader(new FileReader(objFile));
			//object program�� �� �پ� �о����
			while((line=br.readLine())!=null)
			{
				if(line.charAt(0)=='H')	//H���ڵ��� ���
				{
					sub_cnt++;
					name.add(line.substring(1,7));
					//���� ���α׷��� ���
					if(sub_cnt==1)
					{
						rMgr.setName(line.substring(1,7));	
					}
					//�����ƾ�� ��쿡�� ����ּҰ� �ƴ� �����ּҷ� ���
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

				else if(line.charAt(0)=='D')	//D���ڵ��� ��� def ��ū�� �ּ�,�̸�,������ƾ������ ���� ����
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
				
				else if(line.charAt(0)=='R')	//R���ڵ��� ��쵵 D���ڵ�� �����ϰ� ��ū�� ����
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
				//T���ڵ��� ��� ���̿� �°� �Ľ��Ͽ� ��ɾ �����ϰ� �޸𸮿� �δ��ϰ� �������Ͱ� ����
				else if(line.charAt(0)=='T')	
				{
					t_start.add(line.substring(1,7));	//T���ڵ��� �����ּ� ����
					t_length.add(line.substring(7,9));	//T���ڵ��� ���� ����
					loc+=Integer.parseInt(line.substring(1,7),16);	
					int len = Integer.parseInt(line.substring(7,9),16);	//���ڵ��� ����
					int cnt=9;	//�Ľ��ϱ� ���� count����
					while(true)
					{
						if((cnt/2)>len+3)	//���̸� �Ѿ��� ��� �ݺ��� ����
						{
							break;
						}
						if(line.length()<cnt+3)	//���ڵ� �������� ����Ʈ�� ���� ��� �޸𸮿� �δ��ϰ� �ݺ��� ����
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
						
						int op=Integer.parseInt(line.substring(cnt,cnt+2),16);	//op�ڵ� ����(ni���� ����)
						int xbpe=Integer.parseInt(line.substring(cnt+2,cnt+3),16);	//xbpe�� ����
						int cal=Integer.parseInt(line.substring(cnt+2,cnt+4),16);	//��ɾ� ������ ���� ��
						int k = search_opcode(op);	//op�ڵ忡 ���� ��ɾ����� ����
						if(inst_table[k].format==2)	//2������ ��� xbpe=0
						{
							xbpe=0;
						}
						
						if(k>-1)	//��ɾ��� ���
						{
							int jp=0;
							int jp2=0;
							int jp3=0;
							String iname = inst_table[k].name;	//��ɾ��� �̸�
							//op�ڵ尡 ���� ���� ��츦 ����� if��
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
							//��ɾ��� ���
							else
							{
								inst_name.add(iname);	//��ɾ� �̸��� �߰�
								//3������ ��� ũ�� 6�� ���ڿ��� 3����Ʈ�� �Ľ��Ͽ� �޸𸮿� �ø�
								if (xbpe % 2 == 0 && inst_table[k].format==3) 
								{
									inst.add(line.substring(cnt, cnt + 6));
									by=byteorder(line.substring(cnt,cnt+6), 6);
									rMgr.setMemory(b_cnt, by, 3);
									b_cnt+=3;
									cnt+=6;
									loc+=3;
								} 
								//2������ ���
								else if(inst_table[k].format==2) 
								{
									inst.add(line.substring(cnt, cnt + 4));
									by=byteorder(line.substring(cnt,cnt+4), 4);
									rMgr.setMemory(b_cnt, by, 2);
									b_cnt+=2;
									cnt+=4;
									loc+=2;
								}
								//4������ ���
								else if(xbpe>=1)
								{
									inst.add(line.substring(cnt, cnt + 8));
									by=byteorder(line.substring(cnt,cnt+8), 8);
									rMgr.setMemory(b_cnt, by, 4);
									b_cnt+=4;
									cnt+=8;
									loc+=4;
								}
								//��ɾ �ƴ� ��츦 ���
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
								

								//�������� A�� ���� 3/4������ ��� �������Ͱ� ����
								if (iname.equals("COMP") || iname.equals("AND") || iname.equals("ADD") || iname.equals("LDA")
										|| iname.equals("DIV") || iname.equals("LDCH") || iname.equals("MUL")
										|| iname.equals("OR") || iname.equals("RD") || iname.equals("SUB")|| iname.equals("WD")
										|| iname.equals("SSK") || iname.equals("STA") || iname.equals("STCH")) 
								{
									if(xbpe>=8 && iname.equals("STCH"))	//x�������͸� ����ϰ� STCH��ɾ��� ���
									{
										if(xbpe==12)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(0, 1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
													,inst_table[k].name);
										}
										else if(xbpe==10)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(0,1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,1,3,
													inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(0, 1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3,
														inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(0, 1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 1, 4,
														inst_table[k].name);
											}
										}
									}
									else if(xbpe>=8 && iname.equals("LDCH"))	//x�������͸� ����ϰ� LDCH�� ���
									{
										if(xbpe==12)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(0, 1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==10)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(0,1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3
													,inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(0, 1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(0, 1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
									else if(iname.contains("ST"))	//store��ɾ��� ���
									{
										if(xbpe==4)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(0, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(0,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,1,3
													,inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(0, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
														,inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(0, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 1, 4
														,inst_table[k].name);
											}
										}
									}
									else	//������ ��ɾ��� ���
									{
										if(xbpe==4)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(0, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(0,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3,inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(0, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(0, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
								} 
								//B�������Ϳ� �ε��ϰų� �޸𸮿� B�������͸� �����Ѱ��
								else if (iname.equals("LDB") || iname.equals("STB")) 
								{
									if(iname.equals("LDB"))
									{
										if(xbpe==4)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(3, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(3,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3,inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(3, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(3, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
									else
									{
										if(xbpe==4)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(3, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(3,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,1,3,inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(3, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
														,inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(3, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 1, 4
														,inst_table[k].name);
											}
										}
									}

								} 
								//T�������͸� ����ϴ� ���
								else if (iname.equals("LDT") || iname.equals("STT"))
								{
									if(iname.equals("LDT"))
									{
										if(xbpe==4)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(5, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(5,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3,inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(5, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(5, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
									else
									{
										if(xbpe==4)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(5, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(5,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,1,3,inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(5, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
														,inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(5, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 1, 4
														,inst_table[k].name);
											}
										}
									}
								} 
								//S�������͸� ����ϴ� ���
								else if (iname.equals("LDS") || iname.equals("STS"))
								{
									if(iname.equals("LDS"))
									{
										if(xbpe==4)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(4, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(4,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3,inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(4, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(4, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
									else
									{
										if(xbpe==4)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(4, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(4,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,1,3,inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(4, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
														,inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(4, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 1, 4
														,inst_table[k].name);
											}
										}
									}
								} 
								//L�������͸� ����ϴ� ���
								else if (iname.equals("LDL") || iname.equals("STL")) 
								{
									if(iname.equals("LDL"))
									{
										if(xbpe==4)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(2, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(2,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3,inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(2, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(2, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
									else
									{
										if(xbpe==4)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(2, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(2,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,1,3,inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(2, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
														,inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(2, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 1, 4
														,inst_table[k].name);
											}
										}
									}
								} 
								//X�������͸� ����ϰ� 3/4������ ���
								else if (iname.equals("TIX") || iname.equals("STX") || iname.equals("LDX")) 
								{
									if(iname.equals("LDX"))
									{
										if(xbpe==4)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(1, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(1,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3,inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(1, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(1, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
									else if(iname.equals("STX"))
									{
										if(xbpe==4)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(1, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(1,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,1,3,inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(1, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 1, 3
														,inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(1, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 1, 4
														,inst_table[k].name);
											}
										}
									}
									else
									{
										if(xbpe==4)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(11, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(11,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3,inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(11, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(11, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
								} 
								//PC�������͸� �̿��Ͽ� �б��ϴ� ���
								else if (iname.equals("J") || iname.equals("JSUB") || iname.equals("RSUB") || iname.equals("JEQ")
										|| iname.equals("JLT") || iname.equals("JGT")) 
								{
									if(iname.equals("JSUB"))
									{
										if(xbpe%2==0)	//3������ ���
										{
										rMgr.setRegister(82, -1, -1,
												Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
												,inst_table[k].name);
										}
										else	//4������ ���
										{
											rMgr.setRegister(82, -1, -1,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
													,inst_table[k].name);
										}
									}
									else if(iname.equals("RSUB"))	//��ɾ RSUB�� ���
									{
										rMgr.setRegister(81, -1, -1,
												Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
												,inst_table[k].name);
									}
									else
									{
										if(xbpe==4)	//base �������͸� ����ϴ� ���
										{
											rMgr.setRegister(8, -1, 3,
													Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
													,inst_table[k].name);
										}
										else if(xbpe==2)	//pc �������͸� ����ϴ� ���
										{
											rMgr.setRegister(8,-1,8,Integer.parseInt(inst.get(inst.size()-1)
												.substring(3,6),16),loc,0,3
													,inst_table[k].name);
										}
										else	//���� �ּ��� ���
										{
											if(xbpe%2==0)	//�����ּ����� ����� ���
											{
												rMgr.setRegister(8, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 6), 16), loc, 0, 3
														,inst_table[k].name);
											}
											else	//4������ ���
											{
												rMgr.setRegister(8, -1, -1,
														Integer.parseInt(inst.get(inst.size()-1).substring(3, 8), 16), loc, 0, 4
														,inst_table[k].name);
											}
										}
									}
								}

								//2������ ���
								//�������Ͱ� 2���� ���� 1���� ��츦 ������ �������Ͱ� ����
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
						//������ ����� ��ɾ �ƴ� ���
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
				//���� ���ڵ�
				//mod ��ū�� �̸��� �ּҸ� ���� �ּ��� ��쿡�� �����ּҷ� ��ġ�� ���� �����ּҷ� ���� ����
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
					//������ ���� ���
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
				//E���ڵ��� ���
				else if(line.charAt(0)=='E')
				{
					rMgr.r_cnt[sub_cnt-1]=inst.size()+1;
				}
			}
			//�����ƾ�̳� �������α׷��� ���� ��� mod�� ���� modification ����
			int m_cnt=0;
			String m=null;
			for(int i=0; i<mod_cnt; i++)
			{
				int op1=0;
				int op2=0;
				
				if(mod[i].mod_n==5)	//5�ڸ��� �����ؾ� �� ���
				{
					for(int j=0; j<def_cnt;j++)
					{
						if(def[j].name.contains(mod[i].name))
						{
							//������ ���� ��� ������ �����Ͽ� ����
							if(mod[i].is_op==1)	//+�� ���
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
							else if(mod[i].is_op==2)	//-�� ���
							{
								for(int k=0; k<def_cnt; k++)
								{
									if(def[k].name.contains(mod[i-1].name))
									{
										op1=def[k].addr;
									}
								}
								op2=def[j].addr;
								m=String.format("%04X",op1-op2);	//������ �ּҿ� 4�ڸ��� ����
							}
							else	//������ �ƴ� ��� 4�ڸ��� ����
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
					//�����ּҸ� ��� �Ͽ� �������Ͱ��� ����
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
				else	//6�ڸ��� �����ؾ� �� ��� ���� ����� ���� 6�ڸ��� ������ �ٸ�
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

	//�޸𸮿� �������͸� �ʱ�ȭ �ϴ� �Լ�
	@Override
	public void initialize(File objFile, ResourceManager rMgr) 
	{
		rMgr.initializeMemory();
		rMgr.initializeRegister();

	}

	//�ϳ��� ��ɾ �����Ѵ�. �ش� ��ɾ ����ǰ� �� ���� ��ȭ�� 
    //�����ְ�, ���� ��ɾ �������Ѵ�.
    //�������� ������ �����ϴ� �޼ҵ�
	@Override
	public void oneStep(int cnt,int step) {
		//����̽��� �ҷ����� ���
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
		//����̽��� �����ϴ� ���
		else
		{
			rmgr.initialDevice(null);
		}
		}
		rmgr.setName(name.get(cnt-1));

	}

	//���� ��ɾ ��� �����ϴ� �޼ҵ�.
    //���� �ڵ带 ��� �����ϰ� �� ���� ��ȭ�� �����ش�.
	@Override
	public void allStep() {
		rmgr.initialDevice(null);

	}


	//op�ڵ带 �޾� ��ɾ ã���ִ� �Լ�(ni���� ����Ͽ�)
    //��ȯ �� : ��ɾ��� index
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
	
	//���ڿ��� ���̿� �°� ����Ʈ�� parsing���ִ� �Լ�
	//��ȯ�� : parsing�� ����Ʈ �迭
	//�Ű����� : str=�Ľ��� ���ڿ�, len=�Ľ��� ���ڿ��� ���� 
	public static byte[] byteorder(String str,int len)
	{
		int s1;
		int s2;
		int s3;
		int s4;
		if(len==6)	//�Ϲ� ��ɾ��� ���
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
		else if(len==4)	//2�����̰ų� ���̰� 2�� ���
		{
			byte[] by3 = new byte[2];
			s1=Integer.parseInt(str.substring(0,2),16);
			s2=Integer.parseInt(str.substring(2,str.length()),16);
			by3[0]=(byte)s1;
			by3[1]=(byte)s2;
			return by3;
		}
		//4������ ���
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
		//1����Ʈ�� ���
		else
		{
			byte[] by2 = new byte[1];
			s1=Integer.parseInt(str.substring(0,str.length()),16);
			by2[0]=(byte)s1;
			return by2;
		}

		
	}
}
