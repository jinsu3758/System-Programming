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

	public static instruction[] inst = new instruction[256];	//명령어 객체 생성
	static Token_unit[] token = new Token_unit[500];	//토큰 객체 생성
	static Symbol_unit[] symbol = new Symbol_unit[50]; 	//심볼 객체 생성
	static Symbol_unit[] literal = new Symbol_unit[50];	//literal테이블 객체 생성 (심볼과 형식이 같음)
	static Symbol_unit[] refer = new Symbol_unit[50];	//참조값 객체 생성 (심볼과 형식이 같음)
	static Address[] addr = new Address[500];	//object code 객체 생성
	static int sym_num = 0;	//심볼의 갯수
	static int token_num = 0;	//토큰 갯수
	static int refer_num = 0;	//참조값 갯수
	static int lit_num = 0;	//literal 총 갯수
	static int loc_num = 0;	//output 명령어 라인 총 갯수
	static int inst_num=0;	//명령어 갯수
	static int LOCCTR[] = new int[5000];	//LOCCTR 배열 
	static int[] end=new int[4];	//프로그램의 길이를 재기 위한 배열
	static int[] m_record = new int[100];	//수정할 값의 주소
	static int[] is_word = new int[100];	//연산을 사용했을 경우 연산의 갯수
	static String[] m_str=new String[15];	//수정할 값의 이름
	static ArrayList<String> literals = new ArrayList<>(); //literal pool을 사용하기 위한 Arraylist

	public static void main(String [] args)
	{
		//
		// 객체 배열의 초기화
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
		//inst.data에서 기계 코드목록 파일을 읽어 기계어 목록 테이블을 생성
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
			//소스코드를 읽어와 토큰단위로 파싱
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
				if (line.length > 2) 	//null포인터가 안생기기 위해 길이에 따라 토큰테이블에 추가
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
			if(line.length==4)	//null포인터가 안생기기 위해 길이에 따라 토큰테이블에 추가
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
		//input symbol : 심볼 테이블을 저장하고 LOCCTR값도 저장
		//
		int r_cnt=0;	//각 섹션의 순서
		int loc=0;	//LOCCTR에 저장할 값
		int k=0;	//operator이 명령어인지 아닌지 구분하는 값
		for(int i=0;i<token_num;i++,loc_num++)
		{
			k=search_opcode(token[i].operator);	//명령어의 index값을 k에 저장
			if(token[i].operator.equals("START") || token[i].operator.equals("CSECT"))
			{
				//pass2에서 쓰일 각 섹션의 길이를 구하여 end배열에 저장
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
	        	//출력값에서 각 섹션의 첫 명령어 줄에 섹션의 이름을 label에 추가
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
			//EXTREF일 경우 refer테이블에 저장
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
				//LTORG를 만날 경우 literal pool을 비우고 literal테이블에 저장
				LOCCTR[loc_num]=-1;
				loc_num++;
				for(int t=0; t<literals.size(); t++)
				{
					LOCCTR[loc_num]=loc;
					if(literals.get(t).startsWith("=X"))	//16진수일 경우
					{
						literal[lit_num].setName(literals.get(t));
						literal[lit_num].setAddr(loc);
						literal[lit_num].sub_cnt=r_cnt;
						lit_num++;
						loc_num++;
						loc+=1;
					}
					else if(literals.get(t).startsWith("=C"))	//character일 경우
					{
						int cnum=0;
						cnum=literals.get(t).indexOf("'",3);
						literal[lit_num].setName(literals.get(t));
						literal[lit_num].setAddr(loc);
						literal[lit_num].sub_cnt=r_cnt;
						lit_num++;
						loc_num++;
						//character길이에 따라 LOCCTR값 증가
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
				literals.clear();	//literal pool 비우기
				continue;
				
			}
			//RESW일 경우 심볼 테이블에 저장 
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
			//RESB일 경우 심볼 테이블에 저장
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
			// EQU일 경우 그에 맞는 값을 구하여 심볼 테이블에 저장
			else if(token[i].operator.equals("EQU"))
			{
				if(token[i].operand[0].equals("*")) //현재주소값(*)을 사용하는 경우 현재 주소값 저장
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
					//연산이 있을 경우 연산한 값을 구하여 저장
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
					//연산이 없을 경우
					else
					{
						//operand가 숫자가 아닐 경우 심볼테이블에서 operand와 같은 값을 찾아 주소값을 저장하고
						//심볼테이블에 저장
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
						//숫자일 경우
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
			//WORD일 경우 심볼테이블에 저장
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
			//BYTE일 경우 16진수와 character을 구분하여 심볼테이블에 저장
			else if(token[i].operator.equals("BYTE"))
			{
				int cnum=0;
				if (token[i].operand[0].startsWith("X'"))	//16진수일 경우
				{
					symbol[sym_num].setAddr(loc);
					symbol[sym_num].setName(token[i].label);
					LOCCTR[loc_num] = loc;
					symbol[sym_num].sub_cnt=r_cnt;
					loc += 1;
					sym_num++;
					continue;
				}
				else	//character일 경우
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
			//END일 경우 리터럴 폴에 값이 있을경우 비우고 input symbol 반복문을 끝냄
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
				if(k>=0)	//operator이 명령어일 경우
				{
				if(token[i].label!=null)	//label이 있을경우 심볼테이블에 저장
				{
					symbol[sym_num].setAddr(loc);
					symbol[sym_num].setName(token[i].label);
					symbol[sym_num].sub_cnt=r_cnt;
					sym_num++;
				}
				//literal값을 사용하는 경우 literal pool에 저장
				if(token[i].operand[0].startsWith("="))
				{
					if(!literals.contains(token[i].operand[0]))
					{
						literals.add(token[i].operand[0]);
					}
					
				}
				//명령어의 형식에 따라 switch문 실행
				switch(inst[k].format)
				{
				case 2:	//명령어가 2형식일 경우
				{
					LOCCTR[loc_num]=loc;
					loc+=2;
					break;
				}
				case 3:	//명령어가 3형식일 경우
				{
					LOCCTR[loc_num]=loc;
					loc+=3;
					break;
				}
				case 4:	//4형식일 경우
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
		//  object code를 구하고 저장하고 immediate data를 화면에 출력  
		//
		r_cnt=0;
		int a=0;	//object code의 객체 순서를 위한 변수
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
          //object code가 없는 operator일 경우 LOCCTR값과 함꼐 출력
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
          //END일 경우 literal값과 함께 출력
          else if(token[i].operator.equals("END"))
          {
        	  addr[a].r_cnt=r_cnt;
        	  System.out.format("%s\t%s\t%s\r\n", token[i].label,token[i].operator,token[i].operand[0]);
        	  a++;
        	  int li=0;
        	  String li_str;
              //literal값이 있을 경우 출력
        	  for(int t=0; t<lit_num; t++)
        	  {
        		  if(literal[t].sub_cnt==r_cnt)
        		  {
        			  li=literal[t].name.indexOf("'",3);	//데이터 끝의 길이
            		  li_str=literal[t].name.substring(3, li);	//데이터를 추출
            		  addr[a].byte_ob=li_str;	//추출한 데이터를 저장
            		  addr[a].r_cnt=r_cnt;
            		  addr[a].is_r=0;
            		  //character일 경우 길이에 따라 출력
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
            		  else	//16진수일 경우
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
          //WORD일 경우 연산을 사용하는지 심볼을 사용하는지 숫자를 사용한 값인지 판단하여 출력
          else if(token[i].operator.equals("WORD"))
          {
        	  addr[a].r_cnt=r_cnt;
        	  addr[a].format=3;
        	  //연산이 있을 경우 연산한 값을 object code의 주소값에 저장
        	  if(token[i].operand[0].contains("+") || token[i].operand[0].contains("-"))
				{
					int c=token[i].Math(r_cnt);
					if(c==0)	//참조값을 사용하는 경우
					{
						addr[a].is_r=1;
					}
					addr[a].loc=c;
				}
        	  else
        	  {
        		  if(isNum(token[i].operand[0]))	//숫자일 경우
        		  {
        			  addr[a].loc=Integer.parseInt(token[i].operand[0]);
        			  addr[a].is_r = 0;
        		  
        		  }
					else //숫자가 아닐 경우 심볼테이블에서 검색하여 저장
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
        	  //저장한 값을 출력
        	  System.out.format("%04X\t%s\t%s\t%s\t%06X\r\n",LOCCTR[a],token[i].label,token[i].operator,token[i].operand[0],addr[a].loc );
        	  continue;
          }
          //BYTE일 경우 literal처럼 데이터를 추출하여 저장하고 출력
          else if(token[i].operator.equals("BYTE"))
          {
        	  int by=0;
        	  String byte_str;
        	  by=token[i].operand[0].indexOf("'",2);
    		  byte_str=token[i].operand[0].substring(2, by);	//데이터 추출
    		  addr[a].byte_ob=byte_str;	//추출한 데이터를 저장
    		  addr[a].r_cnt=r_cnt;
    		  addr[a].is_r = 0;
    		  //character일 경우 길이에 따라 출력
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
    		  else	//16진수일 경우
    		  {
    			  System.out.format("%04X\t%s\t%s\t%s\t%s\r\n", LOCCTR[a],token[i].label,token[i].operator,token[i].operand[0],
    					  addr[a].byte_ob);
    			  addr[a].format=1;
    		  }
    		  continue;
          }
          //LTORG일 경우 비워진 literal값을 불러와 object code를 구해 출력
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
							li_str = literal[t].name.substring(3, li); // 데이터 추출
        			  }
        			  else
        			  {
        				  li = literal[t].name.indexOf("'", 3);
						  li_str = literal[t].name.substring(1, literal[t].name.length()); // 데이터 추출
        			  }
            		  addr[a].byte_ob=li_str;	//추출한 데이터 저장
            		  addr[a].r_cnt=r_cnt;
            		  addr[a].is_r=0;
            		 //character일 경우 길이에 따라 출력
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
            		  else if(literal[t].name.startsWith("=X'"))	//16진수일 경우
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
          else	//operator이 명령어일 경우 형식에 따라 object code를 구하고 화면출력
          {
        	  k=search_opcode(token[i].operator);	//명령어의 인덱스를 k에 저장
        	  if(k>=0)	//k가 -1일 경우를 제외
        	  {
        		  switch(inst[k].format)
        		  {
        		  case 2:	//2형식일 경우 레지스터의 값을 구하여 opcode와 함께 저장
        		  {
        			  int r1=0;
        			  int r2=0;
        			  if(token[i].operand[3].contains(","))	//레지스터가 2개인 경우
        			  {
        				r1 = search_register(token[i].operand[0]);	//무슨 레지스터인지 검사
      					r2 = search_register(token[i].operand[1]);	//무슨 레지스터인지 검사
      					addr[a].loc = r1 * 10;
      					addr[a].loc += r2;
      					addr[a].format = 2;
      					addr[a].op=inst[k].opcode;
      					addr[a].r_cnt=r_cnt;
      					
        			  }
        			  else	//레지스터가 1개인 경우
        			  {
        				  r1 = search_register(token[i].operand[0]);	//무슨 레지스터인지 검사
        				  addr[a].loc = r1 * 10;
        				  addr[a].format = 2;
        				  addr[a].op=inst[k].opcode;
        				  addr[a].r_cnt=r_cnt;
        			  }
        			  addr[a].is_r=0;
        			  //화면 출력
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
        		  
        		  case 3:	//3형식인 경우 주소값은 pc값을 이용한 상대주소값을 저장하고 주소지정방식에 따른 object code를 구함
        		  {
        			  if(token[i].operator.equals("RSUB"))	//RSUB인 경우 주소는 0을 넣음
        			  {
        				  addr[a].loc=0;
        				  addr[a].format=3;
        				  addr[a].op=inst[k].opcode+3;
        				  addr[a].xbpe=0;
        				  addr[a].is_r=0;
        				  addr[a].r_cnt=r_cnt;
        				  
        			  }
        			  //리터럴 값을 사용한 경우 literal테이블에서 찾아서 저장
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
        			  //즉시주소지정방식인 경우
        			  else if(token[i].operand[0].startsWith("#"))
        			  {
        				  String im=token[i].operand[0].substring(1, token[i].operand[0].length());
        				  if(isNum(im))	//숫자일 경우 값을 그대로 주소값에 저장
        				  {
        					  addr[a].loc=Integer.parseInt(im);
        					  addr[a].is_r=0;
        					  addr[a].op=inst[k].opcode+1;
            				  addr[a].xbpe=0;
            				  addr[a].format=3;
            				  addr[a].r_cnt=r_cnt;
        				  }
        				  else	//숫자가 아닌 경우 심볼테이블을 검색하여 주소값 저장
         				  {
        					  for(int s=0; s<sym_num; s++)
        					  {
        						  if(symbol[s].isSymbol(im, r_cnt))
        						  {
        							  addr[a].loc=symbol[s].addr-LOCCTR[a+1];
            	        			  if(addr[a].loc<0)	//음수인 경우 비트연산하여 저장
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
        			  //간접주소지정방식인 경우 PC값을 이용하여 저장
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
        			  //직접주소지정방식인 경우
        			  else 
        			  {
        				  if(token[i].operand[3].contains(","))	//X레지스터를 사용하는 경우
        				  {
        					  addr[a].xbpe=10;
        				  }
        				  else
        				  { 
        					  addr[a].xbpe=2;
        				  }
        				  for(int s=0; s<sym_num; s++)	//operand를 심볼테이블에서 찾아 주소값을 구함
    					  {
    						  if(symbol[s].isSymbol(token[i].operand[0], r_cnt))
    						  {
    							  addr[a].loc=symbol[s].addr-LOCCTR[a+1];
        	        			  if(addr[a].loc<0)	//음수인 경우 비트연산하여 저장
        	        			  {
        	        				  addr[a].loc=addr[a].loc & 0XFFF;	
        	        			  }
        	        			  addr[a].is_r=0;
        	        			  break;
    						  }
    						  else	//심볼테이블에 없는 경우 참조값인 경우 주소값은 0을 저장
    						  {
    							  addr[a].loc=0;
    							  addr[a].is_r=1;
    						  }
    					  }
    					  addr[a].op=inst[k].opcode+3;
    					  addr[a].r_cnt=r_cnt;
        				  addr[a].format=3;
        				  
        			  }
        			  //저장한 값을 출력
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
        		  //4형식인 경우 3형식과 동일하되 주소값은 20비트를 사용(5자리로 출력)
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
        			  //위는 3형식과 거의 동일하나 출력할 경우 주소값에는 5자리로 출력
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
		//  어셈블리 코드를 기계어 코드로 바꾸기 위한 패스2 과정을 실행
		//  object program을 output_114.txt에 저장
		r_cnt=0;
		a=0;
		int a2=0;	//LTORG의 개행을 위한 또다른 카운트변수
		int a3=0;	//LTORG의 개행을 위한 또다른 카운트변수
		k=0;
		int max = 30;	//T레코드의 길이 제한하기위한 변수
		int real_cnt = 0;	//T레코드의 길이 변수
		int count = 0;	//T레코드의 개행을 위한 변수
		int enter = 0;	//LTROG만나면 T레코드의 개행을 위한 변수
		int start_cnt = 0;	//메인섹션을 위한 변수
		int m_cnt = 0;	//M레코드이 갯수
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
						//섹션에 넘어갈 때 마다 M레코드를 추가하여 파일에 출력
						if (m_cnt != 0)
						{
							wr.println();
							for (int m = 0; m < m_cnt; m++)		//수정 레코드를 넣어줌
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
						if (r_cnt == 1)	//메인섹션이 끝난경우의 E레코드 출력
						{
							wr.printf("E%06d\r\n\r\n", LOCCTR[0]);
							r_cnt++;
						}
						else	//메인섹션이 아닌경우 E레코드 출력
						{
							wr.printf("E\r\n\r\n");
							r_cnt++;
						}
						count = 0;
						wr.printf("H%s\t%06X%06X\r\n", token[i].label, 0, end[r_cnt]);	//나눠진 섹션의 헤더레코드
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
					//R레코드 추가
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
					//나머지 operator들
					else
					{
						if (addr[a].is_r == 1)	//수정할 object code일 경우
						{
							//수정할 object code의 명령어가 word일 경우
							if (token[i].operator.equals("WORD"))
							{
								if (token[i].operand[0].contains("-"))	//-일 경우 -와 같이 추가
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
								else if (token[i].operand[0].contains("+"))	//+일 경우 +와 같이 추가
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
								else //연산이 아닐 경우
								{
									m_record[m_cnt] = LOCCTR[a];
									is_word[m_cnt] = 1;
									m_str[m_cnt]=token[i].operand[0];
									m_cnt++;
									
								}
							}
							//수정할 object code 추가
							else
							{
								m_record[m_cnt] = LOCCTR[a] + 1;
								m_str[m_cnt]=token[i].operand[0];
								m_cnt++;
							}
						}
					if(count==0)	//개행할 떄
					{
						for (int c = a; c < a + 12; c++)
						{
							if (real_cnt + 3 > max || addr[c].is_r == 2 || addr[c].r_cnt != r_cnt)
							{
								break;
							}
							else
							{
								real_cnt += addr[c].format;	//T레코드의 길이를 계산
							}
						}
						wr.print("T");
						if (start_cnt == 1)
						{
							wr.printf("%06X%02X",LOCCTR[a],real_cnt);	//T레코드의 첫 주소와 길이 출력			
							start_cnt = 0;
						}
						else
						{
							wr.printf("%06X%02X",LOCCTR[a],real_cnt);
						}
						real_cnt = 0;
					}
					//OPERATOR가 BYTE일 경우
					if (token[i].operator.equals("BYTE"))
					{
						//character일 경우 길이에 맞게 출력
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
						//16진수일 경우
						else if (token[i].operand[0].startsWith("X'"))
						{
							wr.printf("%s", addr[a].byte_ob);
							count += 1;
						}
					}
					//OPERATOR가 word일 경우
					else if (token[i].operator.equals("WORD"))
					{
						wr.printf("%06X", addr[a].loc);
						count += 3;
					}
					//LTORG이 나올경우
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
									a2 = a;	//개행을 위한 변수 저장
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
									a2 = a;	//개행을 위한 변수 저장
									count += 1;
									lk++;
								}
								else
								{
									a3 = a;	//개행을 위한 변수 저장
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
					k = search_opcode(token[i].operator);	//명령어 index 저장
					if(k>=0)
					{
					switch(inst[k].format)
					{
					case 2:	//2형식일 경우
					{
						wr.printf("%02X%02d",addr[a].op,addr[a].loc);
						count += 2;
						break;
					}
					case 3:	//3형식일 경우
					{
						wr.printf("%02X%1X%03X",addr[a].op, addr[a].xbpe, addr[a].loc);
						count += 3;
						break;
					}
					case 4:	//4형식일 경우
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
					
					//LTORG가 나올경우 T 레코드를 개행해줌
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
								real_cnt += addr[c].format;	//T레코드의 길이를 구해줌
							}
						}
						wr.print("T");
						wr.printf("%06X%02X", LOCCTR[a2], real_cnt);	//T레코드의 처음 값과 길이를 출력
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
					//T레코드의 바이트 수가 30을 넘을경우
					else
					{
						if (count + 3 > max)
						{
							a2 = a;
							count = 0;
							wr.println();
							
						}
					}
					//프로그램의 마지막
					if (token[i].operator.equals("END"))
					{			
						a++;
						//literal이 있을 경우 출력
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
						//마지막 M레코드 추가
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
	//문자열을 입력받아 레지스터의 number를 반환해주는 함수
	//반환 : 레지스터의 number
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
	//문자열을 입력받아 문자열의 데이터가 숫자인지 판별하는 함수
	//반환 : 숫자일 경우 : true 아닐 경우 : false
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
	//문자열을 입력받아 명령어인지 구분해주는 함수
	//반환 : 명령어일 경우 : 명령어의 index 아닐 경우 : -1
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
