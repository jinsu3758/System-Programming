package project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Assembler {

	public static instruction[] inst = new instruction[256];	//��ɾ� ��ü ����
	static Token_unit[] token = new Token_unit[500];	//��ū ��ü ����
	static Symbol_unit[] symbol = new Symbol_unit[50]; 	//�ɺ� ��ü ����
	static Symbol_unit[] literal = new Symbol_unit[50];	//literal���̺� ��ü ���� (�ɺ��� ������ ����)
	static Symbol_unit[] refer = new Symbol_unit[50];	//������ ��ü ���� (�ɺ��� ������ ����)
	static Address[] addr = new Address[500];	//object code ��ü ����
	static int sym_num = 0;	//�ɺ��� ����
	static int token_num = 0;	//��ū ����
	static int refer_num = 0;	//������ ����
	static int lit_num = 0;	//literal �� ����
	static int loc_num = 0;	//output ��ɾ� ���� �� ����
	static int inst_num=0;	//��ɾ� ����
	static int LOCCTR[] = new int[5000];	//LOCCTR �迭 
	static int[] end=new int[4];	//���α׷��� ���̸� ��� ���� �迭
	static int[] m_record = new int[100];	//������ ���� �ּ�
	static int[] is_word = new int[100];	//������ ������� ��� ������ ����
	static String[] m_str=new String[15];	//������ ���� �̸�
	static ArrayList<String> literals = new ArrayList<>(); //literal pool�� ����ϱ� ���� Arraylist

	public static void main(String [] args)
	{
		//
		// ��ü �迭�� �ʱ�ȭ
		//
		for(int i=0; i<256; i++)
		{
			inst[i]=new instruction(null,0,0);
		}
		for(int i=0; i<500; i++)
		{
			token[i]=new Token_unit(null,null,null,null);
			addr[i]=new Address(null,0,0,0,0,0,0);
		}
		for(int i=0; i<50; i++)
		{
			symbol[i]=new Symbol_unit(null,0,0);
			literal[i]=new Symbol_unit(null,0,0);
			refer[i]=new Symbol_unit(null,0,0);
		}
		
		
		BufferedReader bw=null;
		BufferedReader tw=null;
		PrintWriter wr=null;
		//inst.data���� ��� �ڵ��� ������ �о� ���� ��� ���̺��� ����
		try {
			bw = new BufferedReader(new FileReader("C:/Users/jinsu/workspace/Assembler_114/src/project/inst.data"));
			for(int i=0; i<256; i++)
			{
			
			String li;
			li=bw.readLine();
			if(li==null)
			{
				inst_num=i;
				break;
			}
			String[] line=li.split(" ");
			inst[i].setName(line[0]);
			inst[i].opcode=Integer.parseInt(line[1],16);
			inst[i].format=Integer.parseInt(line[2]);
			
			}
			bw.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			//
			//�ҽ��ڵ带 �о�� ��ū������ �Ľ�
			//
			tw = new BufferedReader(new FileReader("C:/Users/jinsu/workspace/Assembler_114/src/project/input.txt"));
			for(int i=0; i<5000; i++)
			{
			
			String li;
			li=tw.readLine();
			if(li==null)
			{
				token_num=i;
				break;
			}
				
			String[] line=li.split("\t");
			
			token[i].setLabel(line[0]);
			token[i].setOperator(line[1]);
				if (line.length > 2) 	//null�����Ͱ� �Ȼ���� ���� ���̿� ���� ��ū���̺� �߰�
				{
					if (line[2].contains(",")) 
					{
						String[] op_line = line[2].split(",");
						token[i].setOperand(op_line[0], 0);
						token[i].setOperand(op_line[1], 1);
						if (op_line.length == 3) 
						{
							token[i].setOperand(op_line[2], 2);
						}
						token[i].setOperand(line[2], 3);

					} 
					else 
					{
						token[i].setOperand(line[2], 0);
						token[i].setOperand(line[2], 3);
					}
				}
			if(line.length==4)	//null�����Ͱ� �Ȼ���� ���� ���̿� ���� ��ū���̺� �߰�
			{
			token[i].setComment(line[3]);
			}
			
			
			}
			tw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//
		//input symbol : �ɺ� ���̺��� �����ϰ� LOCCTR���� ����
		//
		int r_cnt=0;	//�� ������ ����
		int loc=0;	//LOCCTR�� ������ ��
		int k=0;	//operator�� ��ɾ����� �ƴ��� �����ϴ� ��
		for(int i=0;i<token_num;i++,loc_num++)
		{
			k=search_opcode(token[i].operator);	//��ɾ��� index���� k�� ����
			if(token[i].operator.equals("START") || token[i].operator.equals("CSECT"))
			{
				//pass2���� ���� �� ������ ���̸� ���Ͽ� end�迭�� ����
				if(token[i].operator.equals("CSECT"))
	        	{
	        	int j=i-1;
	        	for(; j>0;j--)
	        	{
	        		if (token[j].operator.equals("EQU"))
					{
						continue;
					}
					else
					{
						break;
					}
	        	}
	        	j+=lit_num;
	        	if(LOCCTR[j+2]!=0)
	        	{
	        	end[r_cnt]=LOCCTR[j+2];
	        	}
	        	else
	        	{
	        		end[r_cnt]=loc;
	        	}
	        	//��°����� �� ������ ù ��ɾ� �ٿ� ������ �̸��� label�� �߰�
	        	for (int jk = 0; jk < 5; jk++)
				{					
					int jjk = search_opcode(token[i + jk + 1].operator);
					if (jjk >= 0)
					{
						token[i + jk + 2].label= token[i].label;
						break;
					}
				}	
	        	}
				loc=0;
				LOCCTR[loc_num]=loc;
				r_cnt++;
				continue;
			}
			else if(token[i].operator.equals("EXTDEF"))
			{
				LOCCTR[loc_num]=-1;
				continue;
			}
			//EXTREF�� ��� refer���̺� ����
			else if(token[i].operator.equals("EXTREF"))
			{
				LOCCTR[loc_num]=-1;
				for(int j=0; j<3; j++)
				{
					if(token[i].operand[j]!=null)
					{
						refer[refer_num].sub_cnt=r_cnt;
						refer[refer_num++].setName(token[i].operand[j]);
					}
				}
				continue;
			}
			
			else if(token[i].operator.equals("LTORG"))
			{
				//LTORG�� ���� ��� literal pool�� ���� literal���̺� ����
				LOCCTR[loc_num]=-1;
				loc_num++;
				for(int t=0; t<literals.size(); t++)
				{
					LOCCTR[loc_num]=loc;
					if(literals.get(t).startsWith("=X"))	//16������ ���
					{
						literal[lit_num].setName(literals.get(t));
						literal[lit_num].setAddr(loc);
						literal[lit_num].sub_cnt=r_cnt;
						lit_num++;
						loc_num++;
						loc+=1;
					}
					else if(literals.get(t).startsWith("=C"))	//character�� ���
					{
						int cnum=0;
						cnum=literals.get(t).indexOf("'",3);
						literal[lit_num].setName(literals.get(t));
						literal[lit_num].setAddr(loc);
						literal[lit_num].sub_cnt=r_cnt;
						lit_num++;
						loc_num++;
						//character���̿� ���� LOCCTR�� ����
						if(cnum==6)
						{
							loc+=3;
						}
						else if(cnum==5)
						{
							loc+=2;
						}
						else
						{
							loc+=1;
						}
					}
					else
					{
						literal[lit_num].setName(literals.get(t));
						literal[lit_num].setAddr(loc);
						literal[lit_num].sub_cnt=r_cnt;
						lit_num++;
						loc_num++;
						loc+=3;
					}
	
				}
				literals.clear();	//literal pool ����
				continue;
				
			}
			//RESW�� ��� �ɺ� ���̺� ���� 
			else if(token[i].operator.equals("RESW"))
			{
				LOCCTR[loc_num]=loc;
				symbol[sym_num].setName(token[i].label);
				symbol[sym_num].setAddr(loc);
				symbol[sym_num].sub_cnt=r_cnt;
				sym_num++;
				int num=3*(Integer.parseInt(token[i].operand[0]));
				loc+=num;
				continue;
			}
			//RESB�� ��� �ɺ� ���̺� ����
			else if(token[i].operator.equals("RESB"))
			{
				LOCCTR[loc_num]=loc;
				symbol[sym_num].setName(token[i].label);
				symbol[sym_num].setAddr(loc);
				symbol[sym_num].sub_cnt=r_cnt;
				sym_num++;
				int num=(Integer.parseInt(token[i].operand[0]));
				loc+=num;
				continue;
			}
			// EQU�� ��� �׿� �´� ���� ���Ͽ� �ɺ� ���̺� ����
			else if(token[i].operator.equals("EQU"))
			{
				if(token[i].operand[0].equals("*")) //�����ּҰ�(*)�� ����ϴ� ��� ���� �ּҰ� ����
				{
					LOCCTR[loc_num]=loc;
					symbol[sym_num].setName(token[i].label);
					symbol[sym_num].setAddr(loc);
					symbol[sym_num].sub_cnt=r_cnt;
					sym_num++;
					continue;
				}
				else
				{
					//������ ���� ��� ������ ���� ���Ͽ� ����
					if(token[i].operand[0].contains("+") || token[i].operand[0].contains("-"))
					{
						symbol[sym_num].setName(token[i].label);
						int c=token[i].Math(r_cnt);
						LOCCTR[loc_num]=c;
						symbol[sym_num].setAddr(c);
						symbol[sym_num].sub_cnt=r_cnt;
						sym_num++;
						continue;
					}
					//������ ���� ���
					else
					{
						//operand�� ���ڰ� �ƴ� ��� �ɺ����̺��� operand�� ���� ���� ã�� �ּҰ��� �����ϰ�
						//�ɺ����̺� ����
						if(!isNum(token[i].operand[0])) 	
						{
							for (int j = 0; j < sym_num; j++) 
							{
								if (symbol[j].isSymbol(token[i].operand[0], r_cnt)) 
								{
									symbol[sym_num].setName(token[i].label);
									LOCCTR[loc_num] = symbol[j].addr;
									symbol[sym_num].setAddr(symbol[j].addr);
									symbol[sym_num].sub_cnt = r_cnt;
									sym_num++;
									break;
								}
							}
						}
						//������ ���
						else
						{
							int a=Integer.parseInt(token[i].operand[0]);
							symbol[sym_num].setName(token[i].label);
							symbol[sym_num].sub_cnt=r_cnt;
							LOCCTR[loc_num]=a;
							symbol[sym_num].setAddr(a);
							sym_num++;
							continue;
						}
					}
				}
			}
			//WORD�� ��� �ɺ����̺� ����
			else if(token[i].operator.equals("WORD"))
			{
				symbol[sym_num].setAddr(loc);
				symbol[sym_num].setName(token[i].label);
				LOCCTR[loc_num] = loc;
				symbol[sym_num].sub_cnt=r_cnt;
				loc += 3;
				sym_num++;
				continue;
			}
			//BYTE�� ��� 16������ character�� �����Ͽ� �ɺ����̺� ����
			else if(token[i].operator.equals("BYTE"))
			{
				int cnum=0;
				if (token[i].operand[0].startsWith("X'"))	//16������ ���
				{
					symbol[sym_num].setAddr(loc);
					symbol[sym_num].setName(token[i].label);
					LOCCTR[loc_num] = loc;
					symbol[sym_num].sub_cnt=r_cnt;
					loc += 1;
					sym_num++;
					continue;
				}
				else	//character�� ���
				{
					cnum=token[i].operand[0].indexOf("'",2);
					
					symbol[sym_num].setAddr(loc);
					symbol[sym_num].setName(token[i].label);
					LOCCTR[loc_num] = loc;
					symbol[sym_num].sub_cnt=r_cnt;
					if(cnum==5)
					{
						loc+=3;
					}
					else if(cnum==4)
					{
						loc+=2;
					}
					else
					{
						loc+=1;
					}
					sym_num++;
					continue;
				}
			}
			//END�� ��� ���ͷ� ���� ���� ������� ���� input symbol �ݺ����� ����
			else if(token[i].operator.equals("END"))
			{
				LOCCTR[loc_num]=-1;
				loc_num++;
				for(int t=0; t<literals.size(); t++)
				{
					LOCCTR[loc_num]=loc;
					if(literals.get(t).startsWith("=X"))
					{
						literal[lit_num].setName(literals.get(t));
						literal[lit_num].setAddr(loc);
						literal[lit_num].sub_cnt=r_cnt;
						lit_num++;
						loc_num++;
						loc+=1;
					}
					else
					{
						int cnum=0;
						cnum=literals.get(t).indexOf("'",3);
						literal[lit_num].setName(literals.get(t));
						literal[lit_num].setAddr(loc);
						literal[lit_num].sub_cnt=r_cnt;
						lit_num++;
						loc_num++;
						if(cnum==6)
						{
							loc+=3;
						}
						else if(cnum==5)
						{
							loc+=2;
						}
						else
						{
							loc+=1;
						}
					}
					
					
				}
				literals.clear();
				end[r_cnt]=loc;
				break;
			}
			else
			{
				if(k>=0)	//operator�� ��ɾ��� ���
				{
				if(token[i].label!=null)	//label�� ������� �ɺ����̺� ����
				{
					symbol[sym_num].setAddr(loc);
					symbol[sym_num].setName(token[i].label);
					symbol[sym_num].sub_cnt=r_cnt;
					sym_num++;
				}
				//literal���� ����ϴ� ��� literal pool�� ����
				if(token[i].operand[0].startsWith("="))
				{
					if(!literals.contains(token[i].operand[0]))
					{
						literals.add(token[i].operand[0]);
					}
					
				}
				//��ɾ��� ���Ŀ� ���� switch�� ����
				switch(inst[k].format)
				{
				case 2:	//��ɾ 2������ ���
				{
					LOCCTR[loc_num]=loc;
					loc+=2;
					break;
				}
				case 3:	//��ɾ 3������ ���
				{
					LOCCTR[loc_num]=loc;
					loc+=3;
					break;
				}
				case 4:	//4������ ���
				{
					LOCCTR[loc_num]=loc;
					loc+=4;
					break;
				}
				default:
				{
					break;
				}
					
				}
			}
			}
		}
		
		//	PASS 1
		//  data output
		//  object code�� ���ϰ� �����ϰ� immediate data�� ȭ�鿡 ���  
		//
		r_cnt=0;
		int a=0;	//object code�� ��ü ������ ���� ����
		k=0;
		for(int i=0; i<token_num; i++,a++)
		{
			k=search_opcode(token[i].operator);
          if(token[i].operator.equals("START") || token[i].operator.equals("CSECT"))
          {
        	  if(token[i].operand[0]!=null)
        	  {
        	      System.out.format("%04X\t%s\t%s\t%s\r\n", LOCCTR[a],token[i].label,token[i].operator,token[i].operand[0]);
        	  }
        	  else
        	  {
        		  System.out.format("%04X\t%s\t%s\r\n", LOCCTR[a],token[i].label,token[i].operator);
        	  }
        	r_cnt++;
  			continue;
          }
          //object code�� ���� operator�� ��� LOCCTR���� �Բ� ���
          else if(token[i].operator.equals("RESW") || token[i].operator.equals("RESB") || token[i].operator.equals("EXTDEF")
        		  || token[i].operator.equals("EXTREF") || token[i].operator.equals("EQU"))
          {
        	  if(LOCCTR[a]==-1)
        	  {
        	  System.out.format("\t%s\t%s\t%s\r\n", token[i].label,token[i].operator,token[i].operand[3]);
        	  }
        	  else
        	  {
        		  System.out.format("%04X\t%s\t%s\t%s\r\n", LOCCTR[a],token[i].label,token[i].operator,token[i].operand[3]);
        	  }
        	  continue;
          }
          //END�� ��� literal���� �Բ� ���
          else if(token[i].operator.equals("END"))
          {
        	  addr[a].r_cnt=r_cnt;
        	  System.out.format("%s\t%s\t%s\r\n", token[i].label,token[i].operator,token[i].operand[0]);
        	  a++;
        	  int li=0;
        	  String li_str;
              //literal���� ���� ��� ���
        	  for(int t=0; t<lit_num; t++)
        	  {
        		  if(literal[t].sub_cnt==r_cnt)
        		  {
        			  li=literal[t].name.indexOf("'",3);	//������ ���� ����
            		  li_str=literal[t].name.substring(3, li);	//�����͸� ����
            		  addr[a].byte_ob=li_str;	//������ �����͸� ����
            		  addr[a].r_cnt=r_cnt;
            		  addr[a].is_r=0;
            		  //character�� ��� ���̿� ���� ���
            		  if(literal[t].name.startsWith("=C'"))
            		  {
            			  if(li==6)
  						{
            				  System.out.format("%04X\t*\t%s\t\t%02X%02X%02X\r\n",LOCCTR[a],literal[t].name,
                					  (int)addr[a].byte_ob.charAt(0),(int)addr[a].byte_ob.charAt(1),(int)addr[a].byte_ob.charAt(2));
            				  addr[a].format=3;
  						}
  						else if(li==5)
  						{
  							System.out.format("%04X\t*\t%s\t\t%02X%02X\r\n",LOCCTR[a],literal[t].name,
              					  (int)addr[a].byte_ob.charAt(0),(int)addr[a].byte_ob.charAt(1));
  							addr[a].format=2;
  						}
  						else
  						{
  							System.out.format("%04X\t*\t%s\t\t%02X\r\n",LOCCTR[a],literal[t].name,
                					  (int)addr[a].byte_ob.charAt(0));
  							addr[a].format=1;
  						}
            			
            		  }
            		  else	//16������ ���
            		  {
            			  System.out.format("%04X\t*\t%s\t\t%s\r\n", LOCCTR[a],literal[t].name,
            					  addr[a].byte_ob);
            			  addr[a].format=1;
            		  }
            		  a++;
        		  }
        	  }
        	  
        	  a--;
        	  break;
          }
          //WORD�� ��� ������ ����ϴ��� �ɺ��� ����ϴ��� ���ڸ� ����� ������ �Ǵ��Ͽ� ���
          else if(token[i].operator.equals("WORD"))
          {
        	  addr[a].r_cnt=r_cnt;
        	  addr[a].format=3;
        	  //������ ���� ��� ������ ���� object code�� �ּҰ��� ����
        	  if(token[i].operand[0].contains("+") || token[i].operand[0].contains("-"))
				{
					int c=token[i].Math(r_cnt);
					if(c==0)	//�������� ����ϴ� ���
					{
						addr[a].is_r=1;
					}
					addr[a].loc=c;
				}
        	  else
        	  {
        		  if(isNum(token[i].operand[0]))	//������ ���
        		  {
        			  addr[a].loc=Integer.parseInt(token[i].operand[0]);
        			  addr[a].is_r = 0;
        		  
        		  }
					else //���ڰ� �ƴ� ��� �ɺ����̺��� �˻��Ͽ� ����
					{
						for (int j = 0; j < sym_num; j++) 
						{
							if (symbol[j].isSymbol(token[i].operand[0], r_cnt)) 
							{
								addr[a].loc = symbol[j].addr;
								addr[a].is_r = 0;
								break;
							}
							else 
							{
								addr[a].loc = 0;
								addr[a].is_r = 1;
							}
						}
						
					}
        		  
        		 
        	  }
        	  //������ ���� ���
        	  System.out.format("%04X\t%s\t%s\t%s\t%06X\r\n",LOCCTR[a],token[i].label,token[i].operator,token[i].operand[0],addr[a].loc );
        	  continue;
          }
          //BYTE�� ��� literaló�� �����͸� �����Ͽ� �����ϰ� ���
          else if(token[i].operator.equals("BYTE"))
          {
        	  int by=0;
        	  String byte_str;
        	  by=token[i].operand[0].indexOf("'",2);
    		  byte_str=token[i].operand[0].substring(2, by);	//������ ����
    		  addr[a].byte_ob=byte_str;	//������ �����͸� ����
    		  addr[a].r_cnt=r_cnt;
    		  addr[a].is_r = 0;
    		  //character�� ��� ���̿� ���� ���
    		  if(addr[a].byte_ob.startsWith("C'"))
    		  {
    			  if(by==5)
  				{
    				  System.out.format("%04X\t%s\t%s\t%s\t%02X%02X%02X\r\n",LOCCTR[a],token[i].label,token[i].operator,token[i].operand[0],
        					  (int)addr[a].byte_ob.charAt(0),(int)addr[a].byte_ob.charAt(1),(int)addr[a].byte_ob.charAt(2));
    				  addr[a].format=3;
  				}
  				else if(by==4)
  				{
  					System.out.format("%04X\t%s\t%s\t%s\t%02X%02X\r\n",LOCCTR[a],token[i].label,token[i].operator,token[i].operand[0],
      					  (int)addr[a].byte_ob.charAt(0),(int)addr[a].byte_ob.charAt(1));
  					 addr[a].format=2;
  				}
  				else
  				{
  					System.out.format("%04X\t%s\t%s\t%s\t%02X\r\n",LOCCTR[a],token[i].label,token[i].operator,token[i].operand[0],
        					  (int)addr[a].byte_ob.charAt(0));
  					 addr[a].format=1;
  				}
    			 
    		  }
    		  else	//16������ ���
    		  {
    			  System.out.format("%04X\t%s\t%s\t%s\t%s\r\n", LOCCTR[a],token[i].label,token[i].operator,token[i].operand[0],
    					  addr[a].byte_ob);
    			  addr[a].format=1;
    		  }
    		  continue;
          }
          //LTORG�� ��� ����� literal���� �ҷ��� object code�� ���� ���
          else if(token[i].operator.equals("LTORG"))
          {
        	  System.out.format("%s\t%s\r\n", token[i].label,token[i].operator);
        	  a++;
        	  addr[a].is_r=2;
        	  addr[a].r_cnt=r_cnt;
        	  int li=0;
        	  String li_str;
        	  for(int t=0; t<lit_num; t++)
        	  {
        		  if(literal[t].sub_cnt==r_cnt)
        		  {
        			  if(literal[t].name.contains("'"))
        			  {
							li = literal[t].name.indexOf("'", 3);
							li_str = literal[t].name.substring(3, li); // ������ ����
        			  }
        			  else
        			  {
        				  li = literal[t].name.indexOf("'", 3);
						  li_str = literal[t].name.substring(1, literal[t].name.length()); // ������ ����
        			  }
            		  addr[a].byte_ob=li_str;	//������ ������ ����
            		  addr[a].r_cnt=r_cnt;
            		  addr[a].is_r=0;
            		 //character�� ��� ���̿� ���� ���
            		  if(literal[t].name.startsWith("=C'"))
            		  {
            			  if(li==6)
  						{
            				  System.out.format("%04X\t*\t%s\t\t%02X%02X%02X\r\n",LOCCTR[a],literal[t].name,
                					  (int)addr[a].byte_ob.charAt(0),(int)addr[a].byte_ob.charAt(1),(int)addr[a].byte_ob.charAt(2));
            				  addr[a].format=3;
  						}
  						else if(li==5)
  						{
  							System.out.format("%04X\t*\t%s\t\t%02X%02X\r\n",LOCCTR[a],literal[t].name,
              					  (int)addr[a].byte_ob.charAt(0),(int)addr[a].byte_ob.charAt(1));
  							addr[a].format=2;
  						}
  						else
  						{
  							System.out.format("%04X\t*\t%s\t\t%02X\r\n",LOCCTR[a],literal[t].name,
                					  (int)addr[a].byte_ob.charAt(0));
  							addr[a].format=1;
  						}
            			
            			  
            		  }
            		  else if(literal[t].name.startsWith("=X'"))	//16������ ���
            		  {
            			  System.out.format("%04X\t*\t%s\t\t%s\r\n", LOCCTR[a],literal[t].name,
            					  addr[a].byte_ob);
            			  addr[a].format=1;
            		  }
            		  else
            		  {
            			  System.out.format("%04X\t*\t%s\t\t%06X\r\n", LOCCTR[a],literal[t].name,
            					  Integer.parseInt(addr[a].byte_ob));
            			  addr[a].format=3;
            		  }
            		  a++;
        		  }
        	  }
        	  continue;
          }
          else	//operator�� ��ɾ��� ��� ���Ŀ� ���� object code�� ���ϰ� ȭ�����
          {
        	  k=search_opcode(token[i].operator);	//��ɾ��� �ε����� k�� ����
        	  if(k>=0)	//k�� -1�� ��츦 ����
        	  {
        		  switch(inst[k].format)
        		  {
        		  case 2:	//2������ ��� ���������� ���� ���Ͽ� opcode�� �Բ� ����
        		  {
        			  int r1=0;
        			  int r2=0;
        			  if(token[i].operand[3].contains(","))	//�������Ͱ� 2���� ���
        			  {
        				r1 = search_register(token[i].operand[0]);	//���� ������������ �˻�
      					r2 = search_register(token[i].operand[1]);	//���� ������������ �˻�
      					addr[a].loc = r1 * 10;
      					addr[a].loc += r2;
      					addr[a].format = 2;
      					addr[a].op=inst[k].opcode;
      					addr[a].r_cnt=r_cnt;
      					
        			  }
        			  else	//�������Ͱ� 1���� ���
        			  {
        				  r1 = search_register(token[i].operand[0]);	//���� ������������ �˻�
        				  addr[a].loc = r1 * 10;
        				  addr[a].format = 2;
        				  addr[a].op=inst[k].opcode;
        				  addr[a].r_cnt=r_cnt;
        			  }
        			  addr[a].is_r=0;
        			  //ȭ�� ���
        			  if(token[i].label==null)
        			  {
        				  System.out.format("%04X%s\t%s\t%s\t%02X%02d\r\n", LOCCTR[a],token[i].label,token[i].operator,token[i].operand[3],
            					  addr[a].op,addr[a].loc);
        			  }
        			  else
        			  {
        				  System.out.format("%04X\t%s\t%s\t%s\t%02X%02d\r\n", LOCCTR[a],token[i].label,token[i].operator,token[i].operand[3],
            					  addr[a].op,addr[a].loc);
        			  }
        			  break;
        			  
        		  }
        		  
        		  case 3:	//3������ ��� �ּҰ��� pc���� �̿��� ����ּҰ��� �����ϰ� �ּ�������Ŀ� ���� object code�� ����
        		  {
        			  if(token[i].operator.equals("RSUB"))	//RSUB�� ��� �ּҴ� 0�� ����
        			  {
        				  addr[a].loc=0;
        				  addr[a].format=3;
        				  addr[a].op=inst[k].opcode+3;
        				  addr[a].xbpe=0;
        				  addr[a].is_r=0;
        				  addr[a].r_cnt=r_cnt;
        				  
        			  }
        			  //���ͷ� ���� ����� ��� literal���̺��� ã�Ƽ� ����
        			  else if(token[i].operand[0].startsWith("="))
        			  {
        				  for(int t=0; t<lit_num; t++)
        	        	  {
								if (literal[t].name.equals(token[i].operand[0])) 
								{
									if (literal[t].sub_cnt == r_cnt) 
									{
										addr[a].loc = literal[t].addr - LOCCTR[a + 1];
										if (addr[a].loc < 0) 
										{
											addr[a].loc = addr[a].loc & 0XFFF;
										}
										addr[a].is_r=0;
										break;
									}
								}
        	        	  }
        				  addr[a].format=3;
	        			  addr[a].op=inst[k].opcode+3;
	        			  addr[a].xbpe=2;
	        			  addr[a].r_cnt=r_cnt;
        				  
        			  }
        			  //����ּ���������� ���
        			  else if(token[i].operand[0].startsWith("#"))
        			  {
        				  String im=token[i].operand[0].substring(1, token[i].operand[0].length());
        				  if(isNum(im))	//������ ��� ���� �״�� �ּҰ��� ����
        				  {
        					  addr[a].loc=Integer.parseInt(im);
        					  addr[a].is_r=0;
        					  addr[a].op=inst[k].opcode+1;
            				  addr[a].xbpe=0;
            				  addr[a].format=3;
            				  addr[a].r_cnt=r_cnt;
        				  }
        				  else	//���ڰ� �ƴ� ��� �ɺ����̺��� �˻��Ͽ� �ּҰ� ����
         				  {
        					  for(int s=0; s<sym_num; s++)
        					  {
        						  if(symbol[s].isSymbol(im, r_cnt))
        						  {
        							  addr[a].loc=symbol[s].addr-LOCCTR[a+1];
            	        			  if(addr[a].loc<0)	//������ ��� ��Ʈ�����Ͽ� ����
            	        			  {
            	        				  addr[a].loc=addr[a].loc & 0XFFF;
            	        			  }
            	        			  addr[a].is_r=0;
            	        			  break;
        						  }
        						  else
        						  {
        							  addr[a].loc=0;
        							  addr[a].is_r=1;
        						  }
        					  }
        					  addr[a].op=inst[k].opcode+1;
            				  addr[a].xbpe=2;
            				  addr[a].format=3;
            				  addr[a].r_cnt=r_cnt;
            				  
        				  }
        				  
        				  
        			  }
        			  //�����ּ���������� ��� PC���� �̿��Ͽ� ����
        			  else if(token[i].operand[0].startsWith("@"))
        			  {
        				  String im=token[i].operand[0].substring(1, token[i].operand[0].length());
        				  for(int s=0; s<sym_num; s++)
    					  {
    						  if(symbol[s].isSymbol(im, r_cnt))
    						  {
    							  addr[a].loc=symbol[s].addr-LOCCTR[a+1];
        	        			  if(addr[a].loc<0)
        	        			  {
        	        				  addr[a].loc=addr[a].loc & 0XFFF;
        	        			  }
        	        			  addr[a].is_r=0;
        	        			  break;
    						  }
    						  else
    						  {
    							  addr[a].loc=0;
    							  addr[a].is_r=1;
    						  }
    					  }
    					  addr[a].op=inst[k].opcode+2;
        				  addr[a].xbpe=2;
        				  addr[a].format=3;
        				  addr[a].r_cnt=r_cnt;
        				 
        			  }
        			  //�����ּ���������� ���
        			  else 
        			  {
        				  if(token[i].operand[3].contains(","))	//X�������͸� ����ϴ� ���
        				  {
        					  addr[a].xbpe=10;
        				  }
        				  else
        				  { 
        					  addr[a].xbpe=2;
        				  }
        				  for(int s=0; s<sym_num; s++)	//operand�� �ɺ����̺��� ã�� �ּҰ��� ����
    					  {
    						  if(symbol[s].isSymbol(token[i].operand[0], r_cnt))
    						  {
    							  addr[a].loc=symbol[s].addr-LOCCTR[a+1];
        	        			  if(addr[a].loc<0)	//������ ��� ��Ʈ�����Ͽ� ����
        	        			  {
        	        				  addr[a].loc=addr[a].loc & 0XFFF;	
        	        			  }
        	        			  addr[a].is_r=0;
        	        			  break;
    						  }
    						  else	//�ɺ����̺� ���� ��� �������� ��� �ּҰ��� 0�� ����
    						  {
    							  addr[a].loc=0;
    							  addr[a].is_r=1;
    						  }
    					  }
    					  addr[a].op=inst[k].opcode+3;
    					  addr[a].r_cnt=r_cnt;
        				  addr[a].format=3;
        				  
        			  }
        			  //������ ���� ���
        			  if(token[i].label==null)
        			  {
        				  System.out.format("%04X%s\t%s\t%s\t%02%1X%03X\r\n", LOCCTR[a],token[i].label,token[i].operator,token[i].operand[3],
            					  addr[a].op,addr[a].xbpe,addr[a].loc);
        			  }
        			  else
        			  {
        				  System.out.format("%04X\t%s\t%s\t%s\t%02X%1X%03X\r\n", LOCCTR[a],token[i].label,token[i].operator,token[i].operand[3],
            					  addr[a].op,addr[a].xbpe,addr[a].loc);
        			  }
        			  break;
        		  }
        		  //4������ ��� 3���İ� �����ϵ� �ּҰ��� 20��Ʈ�� ���(5�ڸ��� ���)
        		  case 4:
        		  {
        			  if(token[i].operand[0].startsWith("="))
        			  {
        				  for(int t=0; t<=lit_num; t++)
        	        	  {
								if (literal[t].equals(token[i].operand[0])) 
								{
									if (literal[t].sub_cnt == r_cnt) 
									{
										addr[a].loc = literal[t].addr;
										addr[a].is_r=0;
										break;
									}
								}
        	        	  }
        				  addr[a].format=4;
	        			  addr[a].op=inst[k].opcode+3;
	        			  addr[a].xbpe=1;
	        			  addr[a].r_cnt=r_cnt;
        			  }
        			  else if(token[i].operand[0].startsWith("#"))
        			  {
        				  String im=token[i].operand[0].substring(1, token[i].operand[0].length());
        				  if(isNum(im))
        				  {
        					  addr[a].loc=Integer.parseInt(im);
        					  addr[a].is_r=0;
        					  addr[a].op=inst[k].opcode+1;
            				  addr[a].xbpe=1;
            				  addr[a].format=4;
            				  addr[a].r_cnt=r_cnt;
        				  }
        				  else
        				  {
        					  for(int s=0; s<sym_num; s++)
        					  {
        						  if(symbol[s].isSymbol(im, r_cnt))
        						  {
        							  addr[a].loc=symbol[s].addr;
            	        			 
            	        			  addr[a].is_r=0;
            	        			  break;
        						  }
        						  else
        						  {
        							  addr[a].loc=0;
        							  addr[a].is_r=1;
        						  }
        					  }
        					  addr[a].op=inst[k].opcode+1;
            				  addr[a].xbpe=1;
            				  addr[a].format=4;
            				  addr[a].r_cnt=r_cnt;
        				  }
        			  }
        			  
        			  else if(token[i].operand[0].startsWith("@"))
        			  {
        				  String im=token[i].operand[0].substring(1, token[i].operand[0].length());
        				  for(int s=0; s<sym_num; s++)
    					  {
    						  if(symbol[s].isSymbol(im, r_cnt))
    						  {
    							  addr[a].loc=symbol[s].addr;
        	        			  
        	        			  addr[a].is_r=0;
        	        			  break;
    						  }
    						  else
    						  {
    							  addr[a].loc=0;
    							  addr[a].is_r=1;
    						  }
    					  }
    					  addr[a].op=inst[k].opcode+2;
        				  addr[a].xbpe=1;
        				  addr[a].format=4;
        				  addr[a].r_cnt=r_cnt;
        			  }
        			  else 
        			  {
        				  if(token[i].operand[3].contains(","))
        				  {
        					  addr[a].xbpe=9;
        				  }
        				  else
        				  { 
        					  addr[a].xbpe=1;
        				  }
        				  for(int s=0; s<sym_num; s++)
    					  {
    						  if(symbol[s].isSymbol(token[i].operand[0], r_cnt))
    						  {
    							  addr[a].loc=symbol[s].addr;
        	        			  
        	        			  addr[a].is_r=0;
        	        			  break;
    						  }
    						  else
    						  {
    							  addr[a].loc=0;
    							  addr[a].is_r=1;
    						  }
    					  }
    					  addr[a].op=inst[k].opcode+3;
    					  addr[a].r_cnt=r_cnt;
        				  addr[a].format=4;
        			  }
        			  //���� 3���İ� ���� �����ϳ� ����� ��� �ּҰ����� 5�ڸ��� ���
        			  if(token[i].label==null)
        			  {
        				  System.out.format("%04X%s\t%s\t%s\t%02%1X%05X\r\n", LOCCTR[a],token[i].label,token[i].operator,token[i].operand[3],
            					  addr[a].op,addr[a].xbpe,addr[a].loc);
        			  }
        			  else
        			  {
        				  System.out.format("%04X\t%s\t%s\t%s\t%02X%1X%05X\r\n", LOCCTR[a],token[i].label,token[i].operator,token[i].operand[3],
            					  addr[a].op,addr[a].xbpe,addr[a].loc);
        			  }
        			  break;
        		  }
        		  default :
        			  break;
        		  }
        	  }
          }
		}
		//
		//  pass 2
		//  ����� �ڵ带 ���� �ڵ�� �ٲٱ� ���� �н�2 ������ ����
		//  object program�� output_114.txt�� ����
		r_cnt=0;
		a=0;
		int a2=0;	//LTORG�� ������ ���� �Ǵٸ� ī��Ʈ����
		int a3=0;	//LTORG�� ������ ���� �Ǵٸ� ī��Ʈ����
		k=0;
		int max = 30;	//T���ڵ��� ���� �����ϱ����� ����
		int real_cnt = 0;	//T���ڵ��� ���� ����
		int count = 0;	//T���ڵ��� ������ ���� ����
		int enter = 0;	//LTROG������ T���ڵ��� ������ ���� ����
		int start_cnt = 0;	//���μ����� ���� ����
		int m_cnt = 0;	//M���ڵ��� ����
			try {
				wr = new PrintWriter("C:/Users/jinsu/workspace/Assembler_114/src/project/output_114.txt");
				for(int i=0; i<token_num; i++,a++)
				{
					if(token[i].operator.equals("START"))
					{
						r_cnt++;
						start_cnt=1;
						wr.printf("H%s\t%06X%06X\r\n", token[i].label, Integer.parseInt(token[i].operand[0]), end[r_cnt]);
						continue;
					}
					else if(token[i].operator.equals("CSECT"))
					{
						//���ǿ� �Ѿ �� ���� M���ڵ带 �߰��Ͽ� ���Ͽ� ���
						if (m_cnt != 0)
						{
							wr.println();
							for (int m = 0; m < m_cnt; m++)		//���� ���ڵ带 �־���
							{
								if (is_word[m] == 1)
								{
									wr.printf("M%06X06+%s\r\n", m_record[m], m_str[m]);
								}
								else if (is_word[m] == 2)
								{
									wr.printf("M%06X06%s\r\n", m_record[m], m_str[m]);
								}
								else if (is_word[m] == 3)
								{
									wr.printf("M%06X06%s\r\n", m_record[m], m_str[m]);
								}
								else
								{
									wr.printf("M%06X05+%s\r\n", m_record[m], m_str[m]);
								}
							}
						}
						if (r_cnt == 1)	//���μ����� ��������� E���ڵ� ���
						{
							wr.printf("E%06d\r\n\r\n", LOCCTR[0]);
							r_cnt++;
						}
						else	//���μ����� �ƴѰ�� E���ڵ� ���
						{
							wr.printf("E\r\n\r\n");
							r_cnt++;
						}
						count = 0;
						wr.printf("H%s\t%06X%06X\r\n", token[i].label, 0, end[r_cnt]);	//������ ������ ������ڵ�
						a2 = LOCCTR[a];
						start_cnt = 1;
						for (int j = 0; j < m_cnt; j++)
						{
							is_word[j] = 0;
						}
						m_cnt = 0;
						
						continue;
					}
					
					else if(token[i].operator.equals("EXTDEF"))
					{
						int loc1 = -1, loc2 = -1, loc3 = -1;
						for (int p = 0; p < sym_num; p++)
						{
							if (token[i].operand[0].equals(symbol[p].name))
							{
								loc1 = symbol[p].addr;
							}
							if (token[i].operand[1].equals(symbol[p].name))
							{
								loc2 = symbol[p].addr;
							}
							if (token[i].operand[2].equals(symbol[p].name))
							{
								loc3 = symbol[p].addr;
							}
						}

						if (token[i].operand[2] != null)
						{
							wr.printf("D%s%06X%s%06X%s%06X\r\n", token[i].operand[0], loc1, token[i].operand[1], loc2, 
									token[i].operand[2], loc3);
						}
						else if (token[i].operand[1] != null)
						{
							wr.printf("D%s%06X%s%06X\r\n", token[i].operand[0], loc1, token[i].operand[1], loc2);
						}
						else
						{
							wr.printf("D%s%06X\r\n", token[i].operand[0], loc1);
						}
						continue;
					}
					//R���ڵ� �߰�
					else if(token[i].operator.equals("EXTREF"))
					{
						if (r_cnt == 1)
						{
							wr.print("R");
							for (int r = 0; r < refer_num; r++)
							{
								if(refer[r].sub_cnt==r_cnt)
								{
								wr.print(refer[r].name+"   ");
								}
								else
								{
									break;
								}
								
							}
							wr.println();
						}
						else
						{
							wr.print("R");
							for (int r = 0; r < refer_num; r++)
							{
								if(refer[r].sub_cnt==r_cnt)
								{
								wr.print(refer[r].name);
								}
								else
								{
									;
								}
								
							}
							wr.println();
						}
						continue;
					}
					
					else if(token[i].operator.equals("RESB") || token[i].operator.equals("RESW") || token[i].operator.equals("EQU"))
					{
						continue;
					}
					//������ operator��
					else
					{
						if (addr[a].is_r == 1)	//������ object code�� ���
						{
							//������ object code�� ��ɾ word�� ���
							if (token[i].operator.equals("WORD"))
							{
								if (token[i].operand[0].contains("-"))	//-�� ��� -�� ���� �߰�
								{
									String[] str=token[i].operand[0].split("-");
									m_record[m_cnt] = LOCCTR[a];
									is_word[m_cnt] = 1;
									m_str[m_cnt]=str[0];
									m_cnt++;
									m_record[m_cnt] = LOCCTR[a];
									is_word[m_cnt] = 2;
									m_str[m_cnt]="-".concat(str[1]);
									m_cnt++;
								}
								else if (token[i].operand[0].contains("+"))	//+�� ��� +�� ���� �߰�
								{
									String[] str=token[i].operand[0].split("+");
									m_record[m_cnt] = LOCCTR[a];
									is_word[m_cnt] = 1;
									m_str[m_cnt]=str[0];
									m_cnt++;
									m_record[m_cnt] = LOCCTR[a];
									is_word[m_cnt] = 2;
									m_str[m_cnt]="+".concat(str[1]);
									m_cnt++;
								}
								else //������ �ƴ� ���
								{
									m_record[m_cnt] = LOCCTR[a];
									is_word[m_cnt] = 1;
									m_str[m_cnt]=token[i].operand[0];
									m_cnt++;
									
								}
							}
							//������ object code �߰�
							else
							{
								m_record[m_cnt] = LOCCTR[a] + 1;
								m_str[m_cnt]=token[i].operand[0];
								m_cnt++;
							}
						}
					if(count==0)	//������ ��
					{
						for (int c = a; c < a + 12; c++)
						{
							if (real_cnt + 3 > max || addr[c].is_r == 2 || addr[c].r_cnt != r_cnt)
							{
								break;
							}
							else
							{
								real_cnt += addr[c].format;	//T���ڵ��� ���̸� ���
							}
						}
						wr.print("T");
						if (start_cnt == 1)
						{
							wr.printf("%06X%02X",LOCCTR[a],real_cnt);	//T���ڵ��� ù �ּҿ� ���� ���			
							start_cnt = 0;
						}
						else
						{
							wr.printf("%06X%02X",LOCCTR[a],real_cnt);
						}
						real_cnt = 0;
					}
					//OPERATOR�� BYTE�� ���
					if (token[i].operator.equals("BYTE"))
					{
						//character�� ��� ���̿� �°� ���
						if (token[i].operand[0].startsWith("C'"))
						{
							int len=addr[a].byte_ob.length();
							if(len==3)
							{
							wr.printf("%02X%02X%02X",(int)addr[a].byte_ob.charAt(0),(int)addr[a].byte_ob.charAt(1),
									(int)addr[a].byte_ob.charAt(2));
							count += 3;
							}
							else if(len==2)
							{
								wr.printf("%02X%02X",(int)addr[a].byte_ob.charAt(0),(int)addr[a].byte_ob.charAt(1));
								count += 2;
							}
							else
							{
								wr.printf("%02X",(int)addr[a].byte_ob.charAt(0));
								count += 1;
							}			

						}
						//16������ ���
						else if (token[i].operand[0].startsWith("X'"))
						{
							wr.printf("%s", addr[a].byte_ob);
							count += 1;
						}
					}
					//OPERATOR�� word�� ���
					else if (token[i].operator.equals("WORD"))
					{
						wr.printf("%06X", addr[a].loc);
						count += 3;
					}
					//LTORG�� ���ð��
					else if (token[i].operator.equals("LTORG"))
					{
						a++;
						enter = 1;
						int lk = 0;
						for (int j = 0; j < lit_num; j++)
						{
							if (r_cnt == literal[lk].sub_cnt)
							{
								
								if (literal[lk].name.startsWith("=C"))
								{
									a2 = a;	//������ ���� ���� ����
									if(addr[a].byte_ob.length()==3)
									{
									count += 3;
									}
									else if(addr[a].byte_ob.length()==2)
									{
										count+=2;
									}
									else
									{
										count+=1;
									}
									lk++;
								}
								else if (literal[lk].name.startsWith("=X"))
								{
									a2 = a;	//������ ���� ���� ����
									count += 1;
									lk++;
								}
								else
								{
									a3 = a;	//������ ���� ���� ����
									count+=3;
									lk++;
								}
								a++;
							}
							else
							{
								lk++;
								continue;
							}
						}
					}
					else
					{
						;
					}
					k = search_opcode(token[i].operator);	//��ɾ� index ����
					if(k>=0)
					{
					switch(inst[k].format)
					{
					case 2:	//2������ ���
					{
						wr.printf("%02X%02d",addr[a].op,addr[a].loc);
						count += 2;
						break;
					}
					case 3:	//3������ ���
					{
						wr.printf("%02X%1X%03X",addr[a].op, addr[a].xbpe, addr[a].loc);
						count += 3;
						break;
					}
					case 4:	//4������ ���
					{
						wr.printf("%02X%1X%05X",addr[a].op, addr[a].xbpe, addr[a].loc);
						count += 4;
						break;
					}
					default:
					{
						break;
					}
					}
					}
					
					//LTORG�� ���ð�� T ���ڵ带 ��������
					if (enter == 1)
					{
						enter = 0;
						count = 0;
						wr.println();
						for (int c = a2; c < a2 + 12; c++)
						{
							if (real_cnt + 3 > max  || addr[c].r_cnt != r_cnt)
							{
								break;
							}
							else
							{
								real_cnt += addr[c].format;	//T���ڵ��� ���̸� ������
							}
						}
						wr.print("T");
						wr.printf("%06X%02X", LOCCTR[a2], real_cnt);	//T���ڵ��� ó�� ���� ���̸� ���
						real_cnt = 0;
						int jk = 0;
						for (int j = 0; j < lit_num; j++)
						{
							if (r_cnt == literal[jk].sub_cnt)
							{
								if (literal[jk].name.startsWith("=C"))
								{
									if(addr[a2].byte_ob.length()==3)
									{
										wr.printf("%02X%02X%02X",(int)addr[a2].byte_ob.charAt(0),(int)addr[a2].byte_ob.charAt(1),
												(int)addr[a2].byte_ob.charAt(2));
										count += 3;
									}
									else if(addr[a2].byte_ob.length()==2)
									{
										wr.printf("%02X%02X",(int)addr[a2].byte_ob.charAt(0),(int)addr[a2].byte_ob.charAt(1));
										count+=2;
									}
									else
									{
										wr.printf("%02X",(int)addr[a2].byte_ob.charAt(0));
										count+=1;
									}
									jk++;
								}
								else if (literal[jk].name.startsWith("=X"))
								{
									wr.printf("%s", addr[a2].byte_ob);
									count += 1;
									jk++;
								}
								else
								{
									wr.printf("%06X", Integer.parseInt(addr[a3].byte_ob));
									count += 1;
									jk++;
								}

							}
							else
							{
								break;
							}
						}
					}
					//T���ڵ��� ����Ʈ ���� 30�� �������
					else
					{
						if (count + 3 > max)
						{
							a2 = a;
							count = 0;
							wr.println();
							
						}
					}
					//���α׷��� ������
					if (token[i].operator.equals("END"))
					{			
						a++;
						//literal�� ���� ��� ���
						for (int j = 0; j < lit_num; j++)
						{
							if (r_cnt == literal[j].sub_cnt)
							{
								if (literal[j].name.startsWith("=C"))
								{
									if(addr[a].byte_ob.length()==3)
									{
										wr.printf("%02X%02X%02X",(int)addr[a].byte_ob.charAt(0),(int)addr[a].byte_ob.charAt(1),
												(int)addr[a].byte_ob.charAt(2));
									count += 3;
									}
									else if(addr[a].byte_ob.length()==2)
									{
										wr.printf("%02X%02X",(int)addr[a].byte_ob.charAt(0),(int)addr[a].byte_ob.charAt(1));

										count+=2;
										
									}
									else
									{
										wr.printf("%s",addr[a].byte_ob);
										count+=1;
									}
									
								}
								else if (literal[j].name.startsWith("=X"))
								{
									wr.printf("%s", addr[a].byte_ob);
									
									count += 1;
									
								}
							}
							else
							{
								continue;
							}
						}
						//������ M���ڵ� �߰�
						if (m_cnt != 0)
						{
							wr.println();
							for (int m = 0; m < m_cnt; m++)
							{
								if (is_word[m] == 1)
								{
									wr.printf("M%06X06+%s\r\n", m_record[m], m_str[m]);
								}
								else
								{
									wr.printf("M%06X05+%s\r\n", m_record[m], m_str[m]);
								}
							}
						}
						wr.print("E");
					}
					
					}
					
				}
				wr.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	//���ڿ��� �Է¹޾� ���������� number�� ��ȯ���ִ� �Լ�
	//��ȯ : ���������� number
	public static int search_register(String str)
	{
		if(str.equals("A"))
		{
			return 0;
		}
		if(str.equals("X"))
		{
			return 1;
		}
		if(str.equals("L"))
		{
			return 2;
		}
		if(str.equals("B"))
		{
			return 3;
		}
		if(str.equals("S"))
		{
			return 4;
		}
		if(str.equals("T"))
		{
			return 5;
		}
		if(str.equals("F"))
		{
			return 6;
		}
		if(str.equals("PC"))
		{
			return 8;
		}
		if(str.equals("SW"))
		{
			return 9;
		}
		return 0;
	}
	//���ڿ��� �Է¹޾� ���ڿ��� �����Ͱ� �������� �Ǻ��ϴ� �Լ�
	//��ȯ : ������ ��� : true �ƴ� ��� : false
	public static boolean isNum(String str) 
	{
		char check;
		if (str.equals("")) 
		{
			return false;
		}
		for (int i = 0; i < str.length(); i++) 
		{
			check = str.charAt(i);
			if (check < 48 || check > 58) 
			{
				return false;
			}
		}
		return true;
	}
	//���ڿ��� �Է¹޾� ��ɾ����� �������ִ� �Լ�
	//��ȯ : ��ɾ��� ��� : ��ɾ��� index �ƴ� ��� : -1
	public static int search_opcode(String str)
	{
		int is=-1;
		for(int i=0; i<inst_num; i++)
		{
			if(str.startsWith("+"))
			{
				String str2=str.substring(1,str.length());
				if(inst[i].name.equals(str2))
				{
					inst[i].format=4;
					return i;
				}
			}
			else if(inst[i].name.equals(str))
			{
				if(inst[i].format==4)
				{
				inst[i].format=3;
				}
				return i;
			}
		}
		return is;
	}
}
