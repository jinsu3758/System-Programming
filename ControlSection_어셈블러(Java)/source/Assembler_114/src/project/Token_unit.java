package project;

//
// 어셈블리 할 소스코드를 토큰단위로 나눈  클래스
//
public class Token_unit extends Assembler {
	public String label;
	public String operator;
	public String[] operand={null,null,null,null};
	public String comment;
	
	Token_unit(String l, String opr, String op, String c)
	{
		this.label=l;
		this.operator=opr;
		this.operand[0]=op;
		this.operand[1]=op;
		this.operand[2]=op;
		this.operand[3]=op;
		this.comment=c;
	}
	
	public void setLabel(String l)
	{
		this.label=l;
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public void setOperator(String o)
	{
		this.operator=o;
	}
	
	public String getOperator() 
	{
		return operator;
	}
	
	public void setOperand(String r,int k)
	{
		this.operand[k]=r;
	}
	
	public String getOperand(int k) 
	{
		return operand[k];
	}
	
	public void setComment(String c)
	{
		this.comment=c;
	}
	
	public String getComment() 
	{
		return comment;
	}
	
	public void print()
	{
		System.out.println(this.label+"\t"+this.operator+"\t"+this.operand[3]);
	}
	
	//토큰의 operand에 연산이 있을 경우 연산을 해주는 함수
	public int Math(int r_cnt)
	{
		if(operand[0].contains("+"))
		{
			String[] opline=operand[0].split("+");
			String op1_str=opline[0];
			String op2_str=opline[1];
			
			int op1=-1,op2=-1;
			if(isNum(op1_str))
			{
				op1=Integer.parseInt(op1_str);
			}
			else
			{
				for(int k=0;k<sym_num;k++)
				{
					if(symbol[k].isSymbol(operand[0], r_cnt))
					{
						op1=symbol[k].addr;
						break;
					}
				}
			}
			if(isNum(op2_str))
			{
				op2=Integer.parseInt(op2_str);
			}
			else
			{
				for(int k=0;k<sym_num;k++)
				{
					if(symbol[k].isSymbol(operand[0], r_cnt))
					{
						op2=symbol[k].addr;
						break;
					}
				}
			}
			
			if(op1==-1 || op2==-1)
			{
				
				return 0;
			}
			else
			{
				return op1+op2;
			}
			
		}
		if(operand[0].contains("-"))
		{
			String[] opline=operand[0].split("-");
			String op1_str=opline[0];
			String op2_str=opline[1];
			
			int op1=-1,op2=-1;
			if(isNum(op1_str))
			{
				op1=Integer.parseInt(op1_str);
			}
			else
			{
				for(int k=0;k<sym_num;k++)
				{
					if(symbol[k].isSymbol(op1_str, r_cnt))
					{
						op1=symbol[k].addr;
						break;
					}
				}
			}
			if(isNum(op2_str))
			{
				op2=Integer.parseInt(op2_str);
			}
			else
			{
				for(int k=0;k<sym_num;k++)
				{
					if(symbol[k].isSymbol(op2_str, r_cnt))
					{
						op2=symbol[k].addr;
						break;
					}
				}
			}
			
			if(op1==-1 || op2==-1)
			{
				return 0;
			}
			else
			{
				return op1-op2;
			}
		}
		else
		{
			return 0;
		}
				
			
			
		
	}
	
	
	
	
	


}
