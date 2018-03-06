#include "my_assembler_114.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

FILE * rp;


int main(void)
{

	if (init_my_assembler()< 0)
	{
		printf("init_my_assembler: error\n");
		return -1;
	}
	
    make_opcode_output("output(hw4).txt");
	
	assem_pass1();
	assem_pass2();
	
	
	//동적할당 해제
	for (int i = 0; i < line_num; i++)
	{
		free(table[i]->operand[3]);
		free(table[i]->operand[2]);
		free(table[i]->operand[1]);
		free(table[i]->operand[0]);
		free(table[i]->comment);
		free(table[i]->operate);
		free(table[i]->label);
		free(table[i]);
	}
	return 0;

}

/* ----------------------------------------------------------------------------------
* 설명 : 프로그램 초기화를 위한 자료구조 생성 및 파일을 읽는 함수이다.
* 매계 : 없음
* 반환 : 정상종료 = 0 , 에러 발생 = -1
* 주의 : 각각의 명령어 테이블을 내부에 선언하지 않고 관리를 용이하게 하기
*		   위해서 파일 단위로 관리하여 프로그램 초기화를 통해 정보를 읽어 올 수 있도록
*		   구현하였다.
* ----------------------------------------------------------------------------------
*/
int init_my_assembler(void)
{
	int result = 1;

	if ((result = init_inst_file("inst.data")) < 0)
	{
		return -1;
	}
	if ((result = init_input_file("input.txt")) < 0)
	{
		return -1;
	}

	return result;
}
/* ----------------------------------------------------------------------------------
* 설명 : 머신을 위한 기계 코드목록 파일을 읽어 기계어 목록 테이블(inst_table)을
*        생성하는 함수이다.
* 매계 : 기계어 목록 파일
* 반환 : 정상종료 = 0 , 에러 < 0
* 주의 : 기계어 목록파일 형식
* || 이름 || opcode || 형식 || operand 개수 ||
* ----------------------------------------------------------------------------------
*/
int init_inst_file(char *inst_file)
{
	FILE * insp = fopen(inst_file, "rt");			//기계어 코드목록 파일
	int errno;
	int ccnt = 0;
	int str_cnt = 0;
	char op_str[4];
	char format_str[2];
	char op_cnt_str[2];
	op_str[0] = '\0';
	format_str[0] = '\0';
	op_cnt_str[0] = '\0';
	errno = 0;

	if (insp == NULL)								//파일이 없을 경우 -1 리턴
	{
		errno = -1;
		return errno;
	}

	for (int i = 0; i < MAX_INST; i++)
	{
		str_cnt = 0;
		char *stop;
		char str[20];

		if (fgets(str, 15, insp) != NULL)					//파일을 1줄씩 읽음
		{
			
			if (!feof(insp))
			{
				str[strlen(str) - 1] = '\0';				//파일 끝이 아닐 경우에는 개행 삭제
			}

			//명령어 이름을 공백이나 탭을 제거하고 inst테이블에 저장 
			for (int j = 0; j < 6; j++)
			{
				if (str[j] == '\t' || str[j] == ' ')
				{
					continue;
				}
				else
				{
					instruction[i].name[ccnt++] = str[j];
					if (str[j + 1] == '\t' || str[j + 1] == ' ')
					{
						str_cnt = j + 1;
						ccnt = 0;
						break;
					}
				}
			}

			//명령어의 opcode를 공백이나 탭을 제거하고 inst테이블에 저장 
			for (int j = str_cnt; j < 10; j++)
			{
				if (str[j] == '\t' || str[j] == ' ')
				{
					continue;
				}
				else
				{
					instruction[i].op[ccnt++] = str[j];
					if (str[j + 1] == '\t' || str[j + 1] == ' ')
					{

						str_cnt = j + 1;
						ccnt = 0;
						op_str[0] = '\0';
						break;
					}
				}
			}

			//명령어의 형식을 공백이나 탭을 제거하고 inst테이블에 저장 
			for (int j = str_cnt; j < 12; j++)
			{

				if (str[j] == '\t' || str[j] == ' ')
				{

					continue;
				}
				else
				{
					format_str[ccnt++] = str[j];
					if (str[j + 1] == '\t' || str[j + 1] == ' ' || str[j + 1] == '\0')
					{
						instruction[i].format = atoi(format_str);
						str_cnt = j + 1;
						ccnt = 0;
						format_str[0] = '\0';
						break;
					}
				}
			}

			//명령어의 operand 갯수를 공백이나 탭을 제거하고 inst테이블에 저장 
			for (int j = str_cnt; j < 15; j++)
			{

				if (str[j] == '\t' || str[j] == ' ')
				{

					continue;
				}
				else
				{
					op_cnt_str[ccnt++] = str[j];
					if (str[j + 1] == '\t' || str[j + 1] == ' ' || str[j + 1] == '\0')
					{
						instruction[i].op_cnt = atoi(op_cnt_str);
						str_cnt = j + 1;
						ccnt = 0;
						op_cnt_str[0] = '\0';
						break;
					}
				}
			}

		}
		else
		{
			break;
		}

	}


	return errno;
}

/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 할 소스코드를 읽어오는 함수이다.
* 매계 : 어셈블리할 소스파일명
* 반환 : 정상종료 = 0 , 에러 < 0
* 주의 :
* ----------------------------------------------------------------------------------
*/
int init_input_file(char *input_file)
{
	rp = fopen(input_file, "rt");
	int errno;
	errno = 0;

	//파일이 없을경우 -1 리턴
	if (rp == NULL)
	{
		errno = -1;
		return errno;
	}

	token_parsing(0);	//읽을 파일에서 첫번째 줄부터 파싱시작

	fclose(rp);
	return errno;
}

/* ----------------------------------------------------------------------------------
* 설명 : 소스 코드를 읽어와 토큰단위로 분석하고 토큰 테이블을 작성하는 함수이다.
*        패스 1로 부터 호출된다.
* 매계 : 소스코드의 라인번호
* 반환 : 정상종료 = 0 , 에러 < 0
* 주의 : assembler 프로그램에서는 라인단위로 토큰 및 오브젝트 관리를 하고 있다.
* ----------------------------------------------------------------------------------
*/
int token_parsing(int index)
{
	int str_cnt = 0;
	int no_operand = 0;
	int no_comment = 0;
	int operand_cnt = 0;
	char space[1] = "";
	char m_space[1] = "";

	for (int i = 0; i < MAX_LINES; i++)
	{
		str_cnt = 0;
		operand_cnt = 0;

		char str[150];
		//구조체 포인터 배열 table을 동적할당
		table[i] = (token *)malloc(sizeof(token));
		table[i]->label = (char *)malloc(sizeof(char) * 10);
		table[i]->operate = (char *)malloc(sizeof(char) * 10);
		table[i]->comment = (char *)malloc(sizeof(char) * 100);
		table[i]->operand[0] = (char *)malloc(sizeof(char) * 20);
		table[i]->operand[1] = (char *)malloc(sizeof(char) * 10);
		table[i]->operand[2] = (char *)malloc(sizeof(char) * 10);
		table[i]->operand[3] = (char *)malloc(sizeof(char) * 30);

		//널값으로 초기화
		strcpy(table[i]->label, m_space);
		strcpy(table[i]->operate, m_space);
		strcpy(table[i]->comment, m_space);
		strcpy(table[i]->operand[0], m_space);
		strcpy(table[i]->operand[1], m_space);
		strcpy(table[i]->operand[2], m_space);
		strcpy(table[i]->operand[3], m_space);
		
		if (fgets(str, 150, rp) != NULL)					//파일에서 1줄씩 읽어옴
		{
			if (!feof(rp))
			{
				str[strlen(str) - 1] = '\0';				//파일 끝이 아닐 경우 개행제거
			}

			
			if (str[0] == '\t' || str[0] == '\0')
			{
				strcpy(table[i]->label, "\t");				//label이 없을경우 탭으로 저장
			}
			else
			{
				//공백이 아닐경우 label에 입력된 값 저장
				for (int j = 0; j < 150; j++)
				{
					if (str[j] == '\t' || str[j] == '\0')
					{
						continue;
					}
					else
					{
						//값이 없는 주소의 값에 널 값을 넣음
						*space = *(str + j);
						strncat(table[i]->label, space, 1);
						if (str[j + 1] == '\t' || str[j + 1] == '\0')
						{
							str_cnt = j + 1;
							break;
						}
					}

				}
			}

			//공백을 제거하고 label과 같이 operate(명령어) 저장
			for (int j = str_cnt; j < 150; j++)
			{
				if (str[j] == '\t' || str[j] == '\0')
				{
					continue;
				}
				else
				{
					*space = *(str + j);
					strncat(table[i]->operate, space, 1);
					if (str[j + 1] == '\t' || str[j + 1] == '\0')
					{
						str_cnt = j + 1;
						break;
					}
				}
			}

			//공백을 제거하고 위와 같이 operand값 저장(아직 구별을 하지 않으므로 operand[0]에 모두 저장)
			int k = 0;
			for (int j = str_cnt; j < 150; j++)
			{
				if (str[j] == '\t')
				{
					k++;
					//operand가 없을경우 널값으로 저장
					if (k == 2)
					{
						strcpy(table[i]->operand[3], "\0");
						strcpy(table[i]->operand[0], "\0");
						str_cnt = j + 3;
						break;
					}
					else
					{
						continue;
					}

				}
				//operand와 comment모두 없을 경우 널값으로 모두 저장
				else if (str[j] == '\0')
				{
					no_operand = 1;
					strcpy(table[i]->operand[0], "\0");
					strcpy(table[i]->operand[3], "\0");
					strcpy(table[i]->comment, "\0");
					break;
				}

				else
				{
					
						*space = *(str + j);
						strncat(table[i]->operand[3], space, 1);
						switch (operand_cnt)
						{
						case 0:
						{
							if(str[j]!=',' && str[j] != '-' && str[j] != '+')
							strncat(table[i]->operand[0], space, 1);
							break;
						}
						case 1:
						{
							if (str[j] != ',' && str[j] != '-' && str[j] != '+')
							strncat(table[i]->operand[1], space, 1);
							break;
						}
						case 2:
						{
							if (str[j] != ',' && str[j] != '-' && str[j] != '+')
							strncat(table[i]->operand[2], space, 1);
							break;
						}
						default:
							break;
						}
						if (str[j] == ',' || str[j] == '-' || str[j] == '+')
						{
							operand_cnt++;
						}
						
						
						//strncat(table[i]->operand[0], space, 1);
						if (str[j + 1] == '\t' || str[j + 1] == '\0')
						{
							str_cnt = j + 1;
							break;
						}
					
				}
			}
			k = 0;

			//위와 같이 comment도 저장
			for (int j = str_cnt; j < 150; j++)
			{
				if (no_operand)		//operand와 comment 없을 경우 넘어감
				{
					break;
				}

				if (str[j] == '\t' || str[j] == '\0')
				{
					strcpy(table[i]->comment, "\0");
					break;
				}
				else
				{
					*space = *(str + j);
					strncat(table[i]->comment, space, 1);
					if (str[j + 1] == '\t' || str[j + 1] == '\0')
					{
						break;
					}
				}
			}
		}
		else
		{
			line_num = i;
			break;
		}


	}


}
/* ----------------------------------------------------------------------------------
* 설명 : 입력 문자열이 기계어 코드인지를 검사하는 함수이다.
* 매계 : 토큰 단위로 구분된 문자열
* 반환 : 정상종료 = 기계어 테이블 인덱스, 에러 or 기계어가 아님 = -1
* ----------------------------------------------------------------------------------
*/
int search_opcode(char *str)
{
	
	int cnt = 0;
	char a[20] = "+";
	
	if (str == NULL)
	{
		return -1;
	}
	//명령어 테이블에 있는지 검사
	for (int j = 0; j < MAX_INST; j++)
	{
		strcat(a, instruction[j].name);	//4형식일 경우
		if (!strcmp(str, instruction[j].name))
		{
			if (instruction[j].format == 4)
			{
				instruction[j].format = 3;
			}
			cnt = 1;
			return j;
		}
		else if (!strcmp(str, a))
		{
			cnt = 1;
			instruction[j].format = 4;
			return j;
		}
		a[0] = '\0';
		strcat(a, "+");

	}
	if (cnt == 0)
	{
		return -1;
	}

}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 명령어 옆에 OPCODE가 기록된 표(과제 4번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
* -----------------------------------------------------------------------------------
*/
void make_opcode_output(char *file_name)
{
	char *sps = "";
	FILE * wp = fopen(file_name, "wt");
	int cnt = 0;
	int k;

	

	//파일이 NULL로 들어올 경우 표준출력
	if (wp == NULL)
	{
		fprintf(wp, "%s", stdout);
	}
	char start[10] = "START";

	//명령어 테이블에서 확인 후 opcode값을 token 테이블에 저장
	for (int i = 0; i < MAX_LINES; i++)
	{

		if (table[i]->operate[0] == NULL)
		{
			break;
		}

		if (!strcmp(table[i]->operate, start))		//start일 경우 op코드값에 널값 저장
		{
			strcpy(table[i]->op, sps);
			continue;
		}
		k = search_opcode(table[i]->operate);
		//명령어 테이블에 일치하는 명령어가 없을 경우 널값 저장
		if (k < 0)
		{
			strcpy(table[i]->op, "\0");
		}
		else
		{
			strcpy(table[i]->op, instruction[k].op);
		}

	}
	//operate가 NULL값일 때까지 ouput파일에 출력
	for (int i = 0; i < MAX_LINES; i++)
	{
		if (table[i]->operate[0] == NULL)
		{
			break;
		}

		if (!strcmp(table[i]->label, "\t"))
			fprintf(wp, "%s%s\t%s\t%s", table[i]->label, table[i]->operate, table[i]->operand[3], table[i]->op);
		else
			fprintf(wp, "%s\t%s\t%s\t%s", table[i]->label, table[i]->operate, table[i]->operand[3], table[i]->op);

		fputc('\n', wp);
	}


	fclose(wp);
}
/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 위한 패스1과정을 수행하는 함수이다.
*		   패스1에서는..
*		   1. 프로그램 소스를 스캔하여 해당하는 토큰단위로 분리하여 프로그램 라인별 토큰
*		   테이블을 생성한다.
*
* 매계 : 없음
* 반환 : 정상 종료 = 0 , 에러 = < 0
* 주의 : 현재 초기 버전에서는 에러에 대한 검사를 하지 않고 넘어간 상태이다.
*	  따라서 에러에 대한 검사 루틴을 추가해야 한다.
*
* -----------------------------------------------------------------------------------
*/

static int assem_pass1(void)
{
	input_sym();
	input_address();
	data_output();
	return 0;
}

/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 기계어 코드로 바꾸기 위한 패스2 과정을 수행하는 함수이다.
*		   패스 2에서는 프로그램을 기계어로 바꾸는 작업은 라인 단위로 수행된다.
*		   다음과 같은 작업이 수행되어 진다.
*		   1. 실제로 해당 어셈블리 명령어를 기계어로 바꾸는 작업을 수행한다.
* 매계 : 없음
* 반환 : 정상종료 = 0, 에러발생 = < 0
* 주의 :
* -----------------------------------------------------------------------------------
*/
static int assem_pass2(void)
{

	make_objectcode_output("output_114.txt");
	return 0;
}




/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 object code (프로젝트 1번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_objectcode_output(char *file_name)
{
	FILE * wp = fopen(file_name, "wt");
	int l_cnt = 0;
	int l_cnt2 = 0;
	int k = 0;
	int rr_cnt = 0;
	int max = 30;
	int real_cnt = 0;
	int count = 0;
	int format = 0;
	int enter = 0;
	int start_cnt = 0;
	int m_record[100] = { 0 };
	int is_word[100] = { 0 };
	char m_str[100][15];
	int m_cnt = 0;
	int t_cnt = 0;
	
	if (wp == NULL)
	{
		fprintf(wp, "%s", stdout);
	}

	for (int i = 0; i < loc_cnt; i++, l_cnt++)
	{
		
		if (!strcmp(table[i]->operate, "START"))
		{
			int j = 0;
			rr_cnt++;
			start_cnt = 1;
			fprintf(wp, "H%s\t%06X%06X\n", table[i]->label, 0, end[rr_cnt]);	//start의 헤더레코드
			continue;
		}
		else if (!strcmp(table[i]->operate, "CSECT"))
		{
			if (m_cnt != 0)
			{
				fputc('\n', wp);
				for (int m = 0; m < m_cnt; m++)		//수정 레코드를 넣어줌
				{
					if (is_word[m] == 1)
					{
						fprintf(wp, "M%06X06+%s\n", m_record[m], m_str[m]);
					}
					else if (is_word[m] == 2)
					{
						fprintf(wp, "M%06X06%s\n", m_record[m], m_str[m]);
					}
					else if (is_word[m] == 3)
					{
						fprintf(wp, "M%06X06%s\n", m_record[m], m_str[m]);
					}
					else
					{
						fprintf(wp, "M%06X05+%s\n", m_record[m], m_str[m]);
					}
				}
			}
			if (rr_cnt == 1)
			{
				fprintf(wp, "E%06d\n\n", LOCCTR[0]);
				rr_cnt++;
			}
			else
			{
				fprintf(wp, "E\n\n");
				rr_cnt++;
			}
			count = 0;
			fprintf(wp, "H%s\t%06X%06X\n", table[i]->label, 0, end[rr_cnt]);	//나눠진 섹션의 헤더레코드
			l_cnt2 = LOCCTR[l_cnt];
			start_cnt = 1;
			for (int j = 0; j < m_cnt; j++)
			{
				is_word[j] = 0;
			}
			m_cnt = 0;
			
			continue;
		}


		else if (!strcmp(table[i]->operate, "EXTDEF"))	//D 레코드
		{
			int loc1 = -1, loc2 = -1, loc3 = -1;
			for (int p = 0; p < sym_cnt; p++)
			{
				if (!strcmp(table[i]->operand[0], sym_table[p].symbol))
				{
					loc1 = sym_table[p].addr;
				}
				if (!strcmp(table[i]->operand[1], sym_table[p].symbol))
				{
					loc2 = sym_table[p].addr;
				}
				if (!strcmp(table[i]->operand[2], sym_table[p].symbol))
				{
					loc3 = sym_table[p].addr;
				}
			}

			if (table[i]->operand[2] != NULL)
			{
				fprintf(wp, "D%s%06X%s%06X%s%06X\n", table[i]->operand[0], loc1, table[i]->operand[1], loc2, table[i]->operand[2], loc3);
			}
			else if (table[i]->operand[1] != NULL)
			{
				fprintf(wp, "D%s%06X%s%06X\n", table[i]->operand[0], loc1, table[i]->operand[1], loc2);
			}
			else
			{
				fprintf(wp, "D%s%06X\n", table[i]->operand[0], loc1);
			}
			continue;
		}
		else if (!strcmp(table[i]->operate, "EXTREF"))			//R 레코드
		{
			if (rr_cnt == 1)
			{
				fputc('R', wp);
				for (int r = 0; r < refer_table[rr_cnt].cnt; r++)
				{
					fprintf(wp, "%s  ", refer_table[rr_cnt].refer[r]);
				}
				fputc('\n', wp);
			}
			else
			{
				fputc('R', wp);
				for (int r = 0; r < refer_table[rr_cnt].cnt; r++)
				{
					fprintf(wp, "%s", refer_table[rr_cnt].refer[r]);
				}
				fputc('\n', wp);
			}
			continue;
		}
		//object code가 없을 경우 넘어감
		else if (!strcmp(table[i]->operate, "RESB") || !strcmp(table[i]->operate, "RESW") || !strcmp(table[i]->operate, "EQU"))
		{
			
			continue;
		}
	
		
		else
		{
			t_cnt++;
			if (addr[l_cnt].is_r == 1)	//수정할 object code일 경우
			{
				//수정할 object code의 명령어가 word일 경우
				if (!strcmp(table[i]->operate, "WORD"))
				{
					if (!strcmp(table[i]->operand[1], "\0"))
					{
						m_record[m_cnt] = LOCCTR[l_cnt];
						is_word[m_cnt] = 1;
						strcpy(m_str[m_cnt], table[i]->operand[0]);
						m_cnt++;
					}
					else if (!strcmp(table[i]->operand[2], "\0"))
					{
						search_cal(table[i]->operand[3]);
						char a[20] = "";
						strcpy(a, cal[0]);
						m_record[m_cnt] = LOCCTR[l_cnt];
						is_word[m_cnt] = 1;
						strcpy(m_str[m_cnt], table[i]->operand[0]);
						m_cnt++;
						m_record[m_cnt] = LOCCTR[l_cnt];
						is_word[m_cnt] = 2;
						strcat(a, table[i]->operand[1]);
						strcpy(m_str[m_cnt], a);
						m_cnt++;
					}
					else
					{
						search_cal(table[i]->operand[3]);
						char a[20] = "";
						strcpy(a, cal[0]);
						char b[20] = "";
						strcpy(b, cal[0]);
						m_record[m_cnt] = LOCCTR[l_cnt];
						is_word[m_cnt] = 1;
						strcpy(m_str[m_cnt], table[i]->operand[0]);
						m_cnt++;
						m_record[m_cnt] = LOCCTR[l_cnt];
						is_word[m_cnt] = 2;
						strcat(a, table[i]->operand[1]);
						strcpy(m_str[m_cnt], a);
						m_cnt++;
						m_record[m_cnt] = LOCCTR[l_cnt];
						is_word[m_cnt] = 3;
						strcat(b, table[i]->operand[2]);
						strcpy(m_str[m_cnt], b);
						m_cnt++;
					}
				}
				//수정할 object code 추가
				else
				{
					m_record[m_cnt] = LOCCTR[l_cnt] + 1;
					strcpy(m_str[m_cnt], table[i]->operand[0]);
					m_cnt++;
				}
			}
			//T 레코드의 줄이 개행될 때
			if (count == 0)
			{
				for (int c = l_cnt; c < l_cnt + 12; c++)
				{
					if (real_cnt + 3 > max || addr[c].is_r == 2 || addr[c].r_cnt != rr_cnt)
					{
						break;
					}
					else
					{
						real_cnt += addr[c].format;
					}
				}
				if (!strcmp(table[i]->operate, "END"))
				{
					return;
				}
				fputc('T', wp);
				if (start_cnt == 1)
				{
					fprintf(wp, "%06X%02X", LOCCTR[l_cnt], real_cnt);
					start_cnt = 0;
				}
				else
				{
					fprintf(wp, "%06X%02X", LOCCTR[l_cnt], real_cnt);
				}
				real_cnt = 0;
			}
			//형식에 따라 object code출력
			k = search_opcode(table[i]->operate);
			format = instruction[k].format;
			
			switch (format)
			{

			case 2:
			{
				fprintf(wp, "%02X%02d", addr[l_cnt].op, addr[l_cnt].loc);
				count += 2;
				break;
			}
			case 3:
			{
				fprintf(wp, "%02X%1X%03X", addr[l_cnt].op, addr[l_cnt].xbpe, addr[l_cnt].loc);
				count += 3;
				break;
			}
			case 4:
			{
				fprintf(wp, "%02X%1X%05X", addr[l_cnt].op, addr[l_cnt].xbpe, addr[l_cnt].loc);
				count += 4;
				break;
			}
			default:
			{
				//OPERATOR가 byte일 경우
				if (!strcmp(table[i]->operate, "BYTE"))
				{
					if (table[i]->operand[0][0] == 'C')
					{
						fprintf(wp, "%02X%02X%02X", addr[l_cnt].byte[0], addr[l_cnt].byte[1], addr[l_cnt].byte[2]);
						count += 3;

					}
					else if (table[i]->operand[0][0] == 'X')
					{
						fprintf(wp, "%s", addr[l_cnt].byte);
						count += 1;
					}
				}
				//OPERATOR가 word일 경우
				else if (!strcmp(table[i]->operate, "WORD"))
				{
					fprintf(wp, "%06X", addr[l_cnt].loc);
					count += 3;
				}
				//LTORG이 나올경우
				else if (!strcmp(table[i]->operate, "LTORG"))
				{
					enter = 1;
					int lk = 0;
					for (int j = 0; j < r_lit_cnt; j++)
					{
						if (rr_cnt == lit_table[lk].sub_r_cnt)
						{
							l_cnt++;
							t_cnt++;
							l_cnt2 = l_cnt;
							if (lit_table[lk].symbol[1] == 'C')
							{
								//fprintf(wp, "%02X%02X%02X", addr[l_cnt].byte[0], addr[l_cnt].byte[1], addr[l_cnt].byte[2]);
								count += 3;
								lk++;
							}
							else if (lit_table[lk].symbol[1] == 'X')
							{
								//fprintf(wp, "%02X", addr[l_cnt].byte);
								count += 1;
								lk++;
							}

						}
						else
						{
							lk++;
							continue;
						}
					}

				}

				break;
			}


			}
			//LTORG가 나올경우 T 레코드를 개행해줌
			if (enter == 1)
			{
				enter = 0;
				
				count = 0;
				fputc('\n', wp);
				for (int c = l_cnt2; c < l_cnt2 + 12; c++)
				{
					if (real_cnt + 3 > max  || addr[c].r_cnt != rr_cnt)
					{
						break;
					}
					else
					{
						real_cnt += addr[c].format;
					}
				}
				fputc('T', wp);
				fprintf(wp, "%06X%02X", LOCCTR[l_cnt2], real_cnt);
				real_cnt = 0;
				int lk = 0;
				for (int j = 0; j < r_lit_cnt; j++)
				{
					if (rr_cnt == lit_table[lk].sub_r_cnt)
					{
						
						if (lit_table[lk].symbol[1] == 'C')
						{
							fprintf(wp, "%02X%02X%02X", addr[l_cnt2].byte[0], addr[l_cnt2].byte[1], addr[l_cnt2].byte[2]);
							count += 3;
							lk++;
						}
						else if (lit_table[lk].symbol[1] == 'X')
						{
							fprintf(wp, "%s", addr[l_cnt2].byte);
							count += 1;
							lk++;
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
					
					l_cnt2 = l_cnt;
					count = 0;
					fputc('\n', wp);
				}
			}
			//프로그램의 마지막
			if (!strcmp(table[i]->operate, "END"))
			{
				
				int lk = 0;
				for (int j = 0; j <= r_lit_cnt; j++)
				{
					if (rr_cnt == lit_table[j].sub_r_cnt)
					{
						l_cnt++;
						if (lit_table[j].symbol[1] == 'C')
						{
							fprintf(wp, "%02X%02X%02X", addr[l_cnt].byte[0], addr[l_cnt].byte[1], addr[l_cnt].byte[2]);
							count += 3;
							lk++;
						}
						else if (lit_table[j].symbol[1] == 'X')
						{
							fprintf(wp, "%s", addr[l_cnt].byte);
							count += 1;
							lk++;
						}

					}
					else
					{
						
						continue;
					}
				}
				if (m_cnt != 0)
				{
					fputc('\n', wp);
					for (int m = 0; m < m_cnt; m++)
					{
						if (is_word[m] == 1)
						{
							fprintf(wp, "M%06X06+%s\n", m_record[m], m_str[m]);
						}
						else
						{
							fprintf(wp, "M%06X05+%s\n", m_record[m], m_str[m]);
						}
					}
				}
				fprintf(wp, "E");
				return;
			}

		}
	}
	
}

/* ----------------------------------------------------------------------------------
* 설명 : Object Code를 저장하는 함수
* 매계 : 없음
* 반환 : 없음
* -----------------------------------------------------------------------------------
*/

void input_address()
{
	int l_cnt = 0;
	int sub_cnt = 0;
	int bbreak = 0;

	for (int i = 0; i < line_num; i++)
	{
		int k = 0;
		search_opcode(table[i]->operate);
		addr[l_cnt].r_cnt = sub_cnt;
		//프로그램이 끝나기 전 리터럴 폴이 있을 경우 object code를 저장
		if (!strcmp(table[i]->operate, "END"))
		{
			l_cnt++;
			addr[l_cnt].r_cnt = sub_cnt;
			for (int t = 0; t <= r_lit_cnt; t++)
			{
				if (sub_cnt == lit_table[t].sub_r_cnt)
				{
					if (lit_table[t].symbol[1] == 'C')
					{
						char space[1] = " ";
						for (int j = 1; j < strlen(lit_table[t].symbol); j++)
						{
							*space = lit_table[t].symbol[j];
							if (lit_table[t].symbol[j] != 39 && lit_table[t].symbol[j] != 'C'&& lit_table[t].symbol[j] != '=')
							{
								strncat(addr[l_cnt].byte, space, 1);
								addr[l_cnt].format = 3;
							}
						}
					}
					else if (lit_table[t].symbol[1] == 'X')
					{
						char space[1] = " ";
						for (int j = 1; j < strlen(lit_table[t].symbol); j++)
						{
							*space = lit_table[t].symbol[j];
							if (lit_table[t].symbol[j] != 39 && lit_table[t].symbol[j] != 'X'&& lit_table[t].symbol[j] != '=')
							{
								strncat(addr[l_cnt].byte, space, 1);
								addr[l_cnt].format = 1;
							}
						}
					}
					l_cnt++;
				}
				
			}
			return;
		}
		
		if(!strcmp(table[i]->operate, "START") || !strcmp(table[i]->operate, "CSECT"))
		{
			sub_cnt++;
			l_cnt++;
			continue;
		}

		
	
		if (!strcmp(table[i]->operate, "RESW") || !strcmp(table[i]->operate, "RESB") || !strcmp(table[i]->operate, "EQU") ||
		    !strcmp(table[i]->operate, "EXTDEF") || !strcmp(table[i]->operate, "EXTREF") )
		{
			l_cnt++;
			continue;
		}
		
		k = search_opcode(table[i]->operate);	//inst 목록에서 operator을 검사

		if (k < 0)									//inst 목록에 없는경우
		{
			if (!strcmp(table[i]->operate, "WORD"))	//WORD인 경우
			{
				addr[l_cnt].format = 3;
				int calt = search_cal(table[i]->operand[3]);	//operand에 연산이 있는지 검사
				
				switch (calt)
				{
					//연산이 없는 경우
				case 0:
				{
					for (int t = 0; t < 3; t++)
					{
						if (!strcmp(table[i]->operand[0], refer_table[sub_cnt].refer[t]))	//참조값인지 확인
						{
							addr[l_cnt].is_r = 1;
							addr[l_cnt++].loc = 0;
							
							bbreak = 1;
							break;
						}
					}
					if (bbreak == 1)
					{
						bbreak = 0;
						break;
					}

					addr[l_cnt].loc = atoi(table[i]->operand[0]);
					for (int t = 0; t < sym_cnt; t++)
					{
						if (!strcmp(table[i]->operand[0], sym_table[t].symbol))
						{
							if (sub_cnt == sym_table[t].r_cnt)
							{
								addr[l_cnt].loc = sym_table[t].addr;
							}
						}
					}

					l_cnt++;
					break;
				}
				//연산이 1개인 경우
				case 1:
				{
					int op1 = 0, op2 = 0, sum = 0;
					for (int t = 0; t < 3; t++)
					{
						if (!strcmp(table[i]->operand[0], refer_table[sub_cnt].refer[t]))
						{
							addr[l_cnt].is_r = 1;
							addr[l_cnt++].loc = 0;
							
							bbreak = 1;
							break;
						}
						if (!strcmp(table[i]->operand[1], refer_table[sub_cnt].refer[t]))
						{
							addr[l_cnt].is_r = 1;
							addr[l_cnt++].loc = 0;
							
							bbreak = 1;
							break;
						}
					}

					if (bbreak == 1)
					{
						bbreak = 0;
						break;
					}

					for (int t = 0; t < sym_cnt; t++)
					{
						if (!strcmp(table[i]->operand[0], sym_table[t].symbol))
						{
							if (sub_cnt == sym_table[t].r_cnt)
							{
								op1 = sym_table[t].addr;
							}
						}
						if (!strcmp(table[i]->operand[1], sym_table[t].symbol))
						{
							if (sub_cnt == sym_table[t].r_cnt)
							{
								op2 = sym_table[t].addr;
							}
						}
					}
					if (op1 == 0)
					{
						op1 = atoi(table[i]->operand[0]);
					}
					if (op2 == 0)
					{
						op2 = atoi(table[i]->operand[1]);
					}
					if (cal[0][0] == '+')
					{
						sum = op1 + op2;
					}
					else if (cal[0][0] == '-')
					{
						sum = op1 - op2;
					}
					addr[l_cnt++].loc = sum;
					break;

				}
				//연산이 2개인 경우
				case 2:
				{
					int op3=0, op1 = 0, op2 = 0, sum = 0;
					//참조값인지 검사
					for (int t = 0; t < 3; t++)
					{
						if (!strcmp(table[i]->operand[0], refer_table[sub_cnt].refer[t]))
						{
							addr[l_cnt].is_r = 1;
							addr[l_cnt++].loc = 0;
							
							bbreak = 1;
							break;
						}
						if (!strcmp(table[i]->operand[1], refer_table[sub_cnt].refer[t]))
						{
							addr[l_cnt].is_r = 1;
							addr[l_cnt++].loc = 0;
							
							bbreak = 1;
							break;
						}
						if (!strcmp(table[i]->operand[2], refer_table[sub_cnt].refer[t]))
						{
							addr[l_cnt].is_r = 1;
							addr[l_cnt++].loc = 0;
							
							bbreak = 1;
							break;
						}
					}

					if (bbreak == 1)
					{
						bbreak = 0;
						break;
					}

					for (int t = 0; t < sym_cnt; t++)
					{
						if (!strcmp(table[i]->operand[0], sym_table[t].symbol))
						{
							if (sub_cnt == sym_table[t].r_cnt)
							{
								op1 = sym_table[t].addr;
							}
						}
						if (!strcmp(table[i]->operand[1], sym_table[t].symbol))
						{
							if (sub_cnt == sym_table[t].r_cnt)
							{
								op2 = sym_table[t].addr;
							}
						}
						if (!strcmp(table[i]->operand[2], sym_table[t].symbol))
						{
							if (sub_cnt == sym_table[t].r_cnt)
							{
								op1 = sym_table[t].addr;
							}
						}
					}
					if (op1 == 0)
					{
						op1 = atoi(table[i]->operand[0]);
					}
					if (op2 == 0)
					{
						op2 = atoi(table[i]->operand[1]);
					}
					if (op3 == 0)
					{
						op3 = atoi(table[i]->operand[1]);
					}
					if (cal[0][0] == '+')
					{
						sum = op1 + op2;
					}
					else if (cal[0][0] == '-')
					{
						sum = op1 - op2;
					}

					if (cal[1][0] == '+')
					{
						sum += op3;
					}
					else if (cal[1][0] == '-')
					{
						sum -= op3;
					}
					addr[l_cnt++].loc = sum;
					break;

				}
				default :
					break;

				
				}
				continue;
			}
			//BYTE인 경우
			if (!strcmp(table[i]->operate, "BYTE"))
			{
				if (table[i]->operand[0][0] == 'C')
				{
					char space[1] = " ";
					for (int j = 0; j < strlen(table[i]->operand[0]); j++)
					{
						*space = *(table[i]->operand[0] + j);
						if (table[i]->operand[0][j] != 39 && table[i]->operand[0][j] != 'C')
						{
							strncat(addr[l_cnt].byte, space, 1);
							addr[l_cnt].format = 3;
						}
					}						
				}
				else if (table[i]->operand[0][0] == 'X')
				{
					char space[1] = " ";
					for (int j = 0; j < strlen(table[i]->operand[0]); j++)
					{
						*space = *(table[i]->operand[0] + j);
						if (table[i]->operand[0][j] != 39 && table[i]->operand[0][j] != 'X')
						{
							strncat(addr[l_cnt].byte, space, 1);
							addr[l_cnt].format = 1;
						}
					}
				}
				l_cnt++;
				continue;
			}
			//LTORG가 나올 경우 리터럴 폴 비우기
			if (!strcmp(table[i]->operate, "LTORG"))
			{
				
				l_cnt++;
				addr[l_cnt].r_cnt = sub_cnt;
				addr[l_cnt].is_r = 2;
				for (int t = 0; t <= r_lit_cnt; t++)
				{
					if (sub_cnt==lit_table[t].sub_r_cnt)
					{
						if (lit_table[t].symbol[1] == 'C')
						{
							char space[1] = " ";
							for (int j = 1; j < strlen(lit_table[t].symbol); j++)
							{
								*space = lit_table[t].symbol[j];
								if (lit_table[t].symbol[j] != 39 && lit_table[t].symbol[j] != 'C'&& lit_table[t].symbol[j] != '=')
								{
									strncat(addr[l_cnt].byte, space, 1);
									addr[l_cnt].format = 3;
								}
							}
						}
						else if (lit_table[t].symbol[1] == 'X')
						{
							char space[1] = " ";
							for (int j = 1; j < strlen(lit_table[t].symbol); j++)
							{
								*space = lit_table[t].symbol[j];
								if (lit_table[t].symbol[j] != 39 && lit_table[t].symbol[j] != 'X'&& lit_table[t].symbol[j] != '=')
								{
									strncat(addr[l_cnt].byte, space, 1);
									addr[l_cnt].format = 1;
								}
							}
						}
						l_cnt++;
					}
				}		
				
				continue;
			}

		}
		//inst에 있는경우
		else       
		{
			switch (instruction[k].format)
			{
			case 2:	//2형식일 경우
			{
				if (!strcmp(table[i]->operand[1], "\0"))		//operand가 1개일 경우
				{
					int r = search_register(table[i]->operand[0]);	//무슨 레지스터인지 검사
					addr[l_cnt].loc = r * 10;
					addr[l_cnt].format = 2;
					sscanf(table[i]->op, "%x", &addr[l_cnt].op);
					l_cnt++;
					break;
				}
				else
				{
					int r1= search_register(table[i]->operand[0]);	//무슨 레지스터인지 검사
					int r2 = search_register(table[i]->operand[1]);	//무슨 레지스터인지 검사
					addr[l_cnt].loc = r1 * 10;
					addr[l_cnt].loc += r2;
					addr[l_cnt].format = 2;
					sscanf(table[i]->op, "%x", &addr[l_cnt].op);
					l_cnt++;
					break;
				}
			}	
			case 3:	//3형식일 경우
			{
				int integer = 0;
				//RSUB인 경우
				if (!strcmp(table[i]->operate, "RSUB"))
				{
					sscanf(table[i]->op, "%x", &integer);
					addr[l_cnt].op += integer + 3;
					addr[l_cnt].loc = 0;
					addr[l_cnt].xbpe = 0;
					addr[l_cnt].format = 3;
					l_cnt++;
					break;
				}
				//리터럴 값을 사용한 경우
				if (table[i]->operand[0][0] == '=')
				{
					for (int t = 0; t <= r_lit_cnt; t++)
					{
						if (!strcmp(table[i]->operand[0], lit_table[t].symbol))
						{
							addr[l_cnt].loc = lit_table[t].addr - LOCCTR[l_cnt + 1];
							if (addr[l_cnt].loc < 0)
							{
								addr[l_cnt].loc = addr[l_cnt].loc & 0xFFF;
							}
							break;
						}
					}
					
					sscanf(table[i]->op, "%x", &integer);
					addr[l_cnt].op += integer + 3;
					addr[l_cnt].xbpe = 2;
					addr[l_cnt].format = 3;
					l_cnt++;
					break;	
				}
				//Immediate인 경우
				if (table[i]->operand[0][0] == '#')
				{
					char *im;
					im = strtok(table[i]->operand[0], "#");
					sscanf(table[i]->op, "%x", &integer);
					addr[l_cnt].loc = atoi(im);
					addr[l_cnt].op += integer + 1;
					addr[l_cnt].xbpe = 0;
					addr[l_cnt].format = 3;
					l_cnt++;
					break;
				}
				//indirect인 경우
				if (table[i]->operand[0][0] == '@')
				{
					char *ch;
					ch = strtok(table[i]->operand[0], "@");
					for (int j = 0; j< sym_cnt; j++)
					{
						if (!strcmp(table[i]->operand[0], sym_table[j].symbol))
						{
							if (sym_table[j].r_cnt == sub_cnt)
							{
								addr[l_cnt].loc = sym_table[j].addr - LOCCTR[l_cnt + 1];
								if (addr[l_cnt].loc < 0)
								{
									addr[l_cnt].loc = addr[l_cnt].loc & 0xFFF;
								}
								break;
							}
							else
							{
								addr[l_cnt].loc = 0;
								addr[l_cnt].is_r = 1;
								//break;
							}
						}
					}
					sscanf(table[i]->op, "%x", &integer);
					addr[l_cnt].op += integer + 2;
					addr[l_cnt].xbpe = 2;
					addr[l_cnt].format = 3;
					l_cnt++;
					break;
				}
				if (!strcmp(table[i]->operand[1], "\0"))		//operand가 1개일 경우
				{
					for (int j = 0; j< sym_cnt; j++)
					{
						if (!strcmp(table[i]->operand[0], sym_table[j].symbol))
						{
							if (sym_table[j].r_cnt == sub_cnt)
							{
								addr[l_cnt].loc = sym_table[j].addr - LOCCTR[l_cnt + 1];
								if (addr[l_cnt].loc < 0)
								{
									addr[l_cnt].loc = addr[l_cnt].loc & 0xFFF;
									
								}
								addr[l_cnt].is_r = 0;
								break;
							}
							else
							{
								
								addr[l_cnt].loc = 0;
								addr[l_cnt].is_r = 1;
								
							}
						}
					}
					sscanf(table[i]->op, "%x", &integer);
					addr[l_cnt].op += integer + 3;
					addr[l_cnt].xbpe = 2;
					addr[l_cnt].format = 3;
					l_cnt++;
					break;
					
				}
				else
				{
					//X 레지스터를 사용한 경우
					if (table[i]->operand[1][0] == 'X')
					{
						for (int j = 0; j< sym_cnt; j++)
						{
							if (!strcmp(table[i]->operand[0], sym_table[j].symbol))
							{
								if (sym_table[j].r_cnt == sub_cnt)
								{
									addr[l_cnt].loc = sym_table[j].addr - LOCCTR[l_cnt + 1];
									if (addr[l_cnt].loc < 0)
									{
										addr[l_cnt].loc = addr[l_cnt].loc & 0xFFF;
									}
									break;
								}
								else
								{
									addr[l_cnt].loc = 0;
									addr[l_cnt].is_r = 1;
									//break;
								}
							}
						}
						sscanf(table[i]->op, "%x", &integer);
						addr[l_cnt].op += integer + 3;
						addr[l_cnt].xbpe = 10;
						addr[l_cnt].format = 3;
						l_cnt++;
						break;
					}
				}
			}
			case 4:	//4형식인 경우
			{
				int integer = 0;
				//리터럴 값을 사용한 경우
				if (table[i]->operand[0][0] == '=')
				{
					for (int t = 0; t <= r_lit_cnt; t++)
					{
						if (!strcmp(table[i]->operand[0], lit_table[t].symbol))
						{
							addr[l_cnt].loc = lit_table[t].addr;
							break;
						}
					}
					sscanf(table[i]->op, "%x", &integer);
					addr[l_cnt].op += integer + 3;
					addr[l_cnt].xbpe = 1;
					addr[l_cnt].format = 4;
					l_cnt++;
					break;
				}
				//Immediate인 경우
				if (table[i]->operand[0][0] == '#')
				{
					char im[10];
					strcpy(im, table[i]->operand[0]);
					im[0] = " ";
					sscanf(table[i]->op, "%x", &integer);
					addr[l_cnt].loc = atoi(im);
					addr[l_cnt].op += integer + 1;
					addr[l_cnt].xbpe = 1;
					addr[l_cnt].format = 4;
					l_cnt++;
					break;
				}
				//indirect인 경우
				if (table[i]->operand[0][0] == '@')
				{
					char *ch;
					ch = strtok(table[i]->operand[0], "@");
					for (int j = 0; j< sym_cnt; j++)
					{
						if (!strcmp(table[i]->operand[0], sym_table[j].symbol))
						{
							if (sym_table[j].r_cnt == sub_cnt)
							{
								addr[l_cnt].loc = sym_table[j].addr;
								break;
							}
							else
							{
								addr[l_cnt].loc = 0;
								addr[l_cnt].is_r = 1;
								//break;
							}
						}
					}
					sscanf(table[i]->op, "%x", &integer);
					addr[l_cnt].op += integer + 2;
					addr[l_cnt].xbpe = 1;
					addr[l_cnt].format = 4;
					l_cnt++;
					break;
				}

				if (!strcmp(table[i]->operand[1], "\0"))		//operand가 1개일 경우
				{
					for (int j = 0; j< sym_cnt; j++)
					{
						if (!strcmp(table[i]->operand[0], sym_table[j].symbol))
						{
							if (sym_table[j].r_cnt == sub_cnt)
							{
								addr[l_cnt].loc = sym_table[j].addr;
								break;
							}
							else
							{
								addr[l_cnt].loc = 0;
								addr[l_cnt].is_r = 1;
								//break;
							}
						}
					}
					sscanf(table[i]->op, "%x", &integer);
					addr[l_cnt].op += integer + 3;
					addr[l_cnt].xbpe = 1;
					addr[l_cnt].format = 4;
					l_cnt++;
					break;

				}
				else
				{
					//X 레지스터를 사용한 경우
					if (table[i]->operand[1][0] == 'X')
					{
						for (int j = 0; j< sym_cnt; j++)
						{
							if (!strcmp(table[i]->operand[0], sym_table[j].symbol))
							{
								if (sym_table[j].r_cnt == sub_cnt)
								{
									addr[l_cnt].loc = sym_table[j].addr;
									break;
								}
								else
								{
									addr[l_cnt].loc = 0;
									addr[l_cnt].is_r = 1;
									//break;
								}
							}
						}
						sscanf(table[i]->op, "%x", &integer);
						addr[l_cnt].op += integer + 3;
						addr[l_cnt].xbpe = 9;
						addr[l_cnt].format = 4;
						l_cnt++;
						break;
					}
				}

			}
			default:
				break;

			}
			continue;
		}
		
		
		
	}
}

/* ----------------------------------------------------------------------------------
* 설명 : SYMTAB을 저장하는 함수
* 매계 : 없음
* 반환 : 없음
* -----------------------------------------------------------------------------------
*/
void input_sym()
{
	sym_cnt = 0;
	int lit_cnt = 0;
	r_lit_cnt = 0;
	refer_cnt = 0;
	r_cnt = 0;
	int k = 0;
	int loc = 0;
	int num = 0;
	int iscal = 0;
	char literals[30][10];
	char space[1] = "";
	loc_cnt = 0;
	



	for (int i = 0; i < line_num; i++)
	{

		if (!strcmp(table[i]->operate, "START") || !strcmp(table[i]->operate, "CSECT"))
		{
			if (!strcmp(table[i]->operate, "CSECT"))
			{
				int j = i - 1;
				for (; j > 0; j--)
				{
					if (!strcmp(table[j]->operate, "EQU"))
					{
						continue;
					}
					else
					{
						break;
					}
				}
				j += r_lit_cnt;
				end[r_cnt] = LOCCTR[j + 1];	//PASS2에서 쓰일 프로그램의 길이를 저장

				for (int jk = 0; jk < 5; jk++)
				{					
					int jjk = search_opcode(table[i + jk + 1]->operate);
					if (jjk >= 0)
					{
						strcpy(table[i + jk + 1]->label, table[i]->label);
						break;
					}
				}				
			}
			loc = 0;
			LOCCTR[loc_cnt] = loc;
			LOCCTR[++loc_cnt] = loc;
			for (int h = 0; h <= lit_cnt; h++)
			{
				strcpy(literals[h], space);
			}
			lit_cnt = 0;
			r_cnt++;
			continue;
		}

		sym_table[sym_cnt].r_cnt = r_cnt;
		//리터럴 값이 나왔을 경우 literals배열에 저장
		if (table[i]->operand[0][0] == '=')
		{
			if (strcmp(table[i]->operand[0], literals[lit_cnt - 1]))
			{
				strcpy(literals[lit_cnt], table[i]->operand[0]);
				lit_cnt++;
			}
			
		}
		//EXTDEF인 경우
		if (!strcmp(table[i]->operate, "EXTDEF"))
		{
			LOCCTR[loc_cnt] = -1;
			loc_cnt++;
			continue;
		}
		//EXTREF인 경우
		if (!strcmp(table[i]->operate, "EXTREF"))
		{
			int r = 0;
			for (r = 0; r < 3; r++)
			{
				if (strcmp(table[i]->operand[r], ""))
				{
					strcpy(refer_table[r_cnt].refer[r], table[i]->operand[r]);
					refer_cnt++;
				}
				else
				{
					
					break;
				}
			}
			refer_table[r_cnt].cnt = r + 1;
			//refer_table[r_cnt].sub_cnt = r_cnt;
			LOCCTR[loc_cnt] = -1;
			loc_cnt++;
			continue;
		}
		
		k = search_opcode(table[i]->operate);	//INST테이블의 명령어 인지 검사

		if (k < 0)	//명령어가 아닌 경우
		{
			//LTORG가 나왔을 경우 리터럴 폴 비우고 리터럴 테이블에 저장
			if (!strcmp(table[i]->operate, "LTORG"))
			{
				LOCCTR[loc_cnt] = -1;
				loc_cnt++;
				for (int p = 0; p < lit_cnt; p++)
				{
					strcpy(lit_table[r_lit_cnt].symbol, literals[p]);
					lit_table[r_lit_cnt].addr = loc;
					lit_table[r_lit_cnt].sub_r_cnt = r_cnt;
					LOCCTR[loc_cnt] = loc;
					if (lit_table[r_lit_cnt].symbol[1] == 'X')
					{
						loc += 1;
					}
					else
					{
						loc += 3;
					}
					loc_cnt++;
					LOCCTR[loc_cnt] = loc;
					r_lit_cnt++;
				}
				
			}
			//RESW인 경우
			if (!strcmp(table[i]->operate, "RESW"))
			{
				sym_table[sym_cnt].addr = loc;
				strcpy(sym_table[sym_cnt].symbol, table[i]->label);
				num = 3 * atoi(table[i]->operand[3]);
				loc += num;
				LOCCTR[++loc_cnt] = loc;

				sym_cnt++;
				
				continue;
			}
			//RESB인 경우
			if (!strcmp(table[i]->operate, "RESB"))
			{
				sym_table[sym_cnt].addr = loc;
				strcpy(sym_table[sym_cnt].symbol, table[i]->label);
				num = atoi(table[i]->operand[3]);
				loc += num;
				LOCCTR[++loc_cnt] = loc;
				sym_cnt++;
				continue;
			}
			//EQU인 경우
			if (!strcmp(table[i]->operate, "EQU"))
			{
				//현재 주소값을 사용하는 경우
				if (!strcmp(table[i]->operand[3], "*"))
				{
					sym_table[sym_cnt].addr = loc;
					strcpy(sym_table[sym_cnt].symbol, table[i]->label);
					sym_cnt++;
					LOCCTR[++loc_cnt] = loc;
					continue;
				}

				else
				{
					int op1 = 0, op2 = 0, op3 = 0, sum = 0;
					iscal = search_cal(table[i]->operand[3]);	//OPERAND가 연산이 있는지 검사
					switch(iscal)
					{
					case 2:	//연산이 2개인 경우
					{
						for (int s = 0; s < sym_cnt; s++)
						{
							if (!strcmp(table[i]->operand[0], sym_table[s].symbol))
							{
								op1 = sym_table[s].addr;
								for (int r = 0; r <= 3; r++)
								{
									if (!strcmp(table[i]->operand[0], refer_table[r_cnt].refer[r]))
									{
										op1 = -1;
									}
								}
							}
							if (!strcmp(table[i]->operand[1], sym_table[s].symbol))
							{
								op2 = sym_table[s].addr;
							}
							if (!strcmp(table[i]->operand[2], sym_table[s].symbol))
							{
								op3 = sym_table[s].addr;
							}
						}
						if (op1 == 0)
						{
							op1 = atoi(table[i]->operand[0]);
						}
						else if (op1 == -1)
						{
							sym_table[sym_cnt].addr = loc;
							strcpy(sym_table[sym_cnt].symbol, table[i]->label);
							sym_cnt++;
							LOCCTR[++loc_cnt] = loc;
							break;
						}
						
						if (op2 == 0)
						{
							op2 = atoi(table[i]->operand[1]);
						}

						if (op3 == 0)
						{
							op3 = atoi(table[i]->operand[1]);
						}

						if (cal[0][0] == '+')
						{
							sum = op1 + op2;
						}
						else if (cal[0][0] == '-')
						{
							sum = op1 - op2;
						}

						if (cal[1][0] == '+')
						{
							sum += op3;
						}
						else if (cal[1][0] == '-')
						{
							sum -= op3;
						}
						sym_table[sym_cnt].addr = sum;
						strcpy(sym_table[sym_cnt].symbol, table[i]->label);
						sym_cnt++;
						LOCCTR[loc_cnt] = sum;
						LOCCTR[++loc_cnt] = loc;
						break;
					}

					case 1:		//연산이 1개인 경우
					{
						op1 = 0, op2 = 0, op3 = 0, sum = 0;
						for (int s = 0; s < sym_cnt; s++)
						{
							if (!strcmp(table[i]->operand[0], sym_table[s].symbol))
							{
								op1 = sym_table[s].addr;
								for (int r = 0; r <= 3; r++)
								{
									if (!strcmp(table[i]->operand[0], refer_table[r_cnt].refer[r]))
									{
										op1 = -1;
									}
								}
							}
							if (!strcmp(table[i]->operand[1], sym_table[s].symbol))
							{
								op2 = sym_table[s].addr;
							}
							
						}
						if (op1 == 0)
						{
							op1 = atoi(table[i]->operand[0]);
						}
						else if (op1 == -1)
						{
							sym_table[sym_cnt].addr = loc;
							strcpy(sym_table[sym_cnt].symbol, table[i]->label);
							sym_cnt++;
							LOCCTR[++loc_cnt] = loc;
							break;
						}

						if (op2 == 0)
						{
							op2 = atoi(table[i]->operand[1]);
						}

						if (cal[0][0] == '+')
						{
							sum = op1 + op2;
						}
						else if (cal[0][0] == '-')
						{
							sum = op1 - op2;
						}

						sym_table[sym_cnt].addr = sum;
						strcpy(sym_table[sym_cnt].symbol, table[i]->label);
						sym_cnt++;
						LOCCTR[loc_cnt] = sum;
						LOCCTR[++loc_cnt] = loc;
						break;
					}

					case 0:	//연산이 없는 경우
					{
						op1 = 0;
						for (int s = 0; s < sym_cnt; s++)
						{
							if (!strcmp(table[i]->operand[0], sym_table[s].symbol))
							{			
								op1 = sym_table[s].addr;
								for (int r = 0; r <= 3; r++)
								{
									if (!strcmp(table[i]->operand[0], refer_table[r_cnt].refer[r]))
									{
										op1 = -1;
									}
								}
							}
							
						}

						if (op1 == 0)
						{
							op1 = atoi(table[i]->operand[0]);
							sym_table[sym_cnt].addr = op1;
							strcpy(sym_table[sym_cnt].symbol, table[i]->label);
							LOCCTR[++loc_cnt] = loc;
							break;
						}
						else if (op1 == -1)
						{
							sym_table[sym_cnt].addr = loc;
							strcpy(sym_table[sym_cnt].symbol, table[i]->label);
							sym_cnt++;
							LOCCTR[++loc_cnt] = loc;
							break;
						}
						else
						{
							sym_table[sym_cnt].addr = loc;
							strcpy(sym_table[sym_cnt].symbol, table[i]->label);
							sym_cnt++;
							LOCCTR[++loc_cnt] = loc;
							break;
						}

					}
					default:
						break;

					}
				}
				
			}
			//BYTE인 경우
			if (!strcmp(table[i]->operate, "BYTE"))
			{
				sym_table[sym_cnt].addr = loc;
				strcpy(sym_table[sym_cnt].symbol, table[i]->label);
				loc += 1;
				LOCCTR[++loc_cnt] = loc;
				sym_cnt++;
				continue;
			}
			//WORD인 경우
			if (!strcmp(table[i]->operate, "WORD"))
			{
				sym_table[sym_cnt].addr = loc;
				strcpy(sym_table[sym_cnt].symbol, table[i]->label);
				loc += 3;
				LOCCTR[++loc_cnt] = loc;
				sym_cnt++;
				continue;
			}
			
		}
		else //명령어인 경우
		{
			switch (instruction[k].format)
			{
			case 2:	//2형식인 경우
			{
				if (!strcmp(table[i]->label,"") || !strcmp(table[i]->label,"\t"))
				{
					loc += 2;
					LOCCTR[++loc_cnt] = loc;
				}
				else
				{
					sym_table[sym_cnt].addr = loc;
					strcpy(sym_table[sym_cnt].symbol, table[i]->label);
					loc += 2;
					LOCCTR[++loc_cnt] = loc;
					sym_cnt++;
				}
				break;
			}
			case 3:	//3형식인 경우
			{
				if (!strcmp(table[i]->label, "") || !strcmp(table[i]->label, "\t"))
				{
					loc += 3;
					LOCCTR[++loc_cnt] = loc;
				}
				else
				{
					sym_table[sym_cnt].addr = loc;
					strcpy(sym_table[sym_cnt].symbol, table[i]->label);
					loc += 3;
					LOCCTR[++loc_cnt] = loc;
					sym_cnt++;
				}
				break;
			}
			case 4:	//4형식인 경우
			{
				if (!strcmp(table[i]->label, "") || !strcmp(table[i]->label, "\t"))
				{
					loc += 4;
					LOCCTR[++loc_cnt] = loc;
				}
				else
				{
					sym_table[sym_cnt].addr = loc;
					strcpy(sym_table[sym_cnt].symbol, table[i]->label);
					loc += 4;
					LOCCTR[++loc_cnt] = loc;
					sym_cnt++;
				}
				break;
			}
			default :
				break;

			}
		}	
		//프로그램의 마지막인 경우
		if (!strcmp(table[i]->operate, "END"))
		{
			int j = i - 1;
			if (lit_cnt == 0)
			{
				for (; j > 0; j--)
				{
					if (!strcmp(table[j]->operate, "EQU"))
					{
						continue;
					}
					else
					{
						break;
					}
				}
				end[r_cnt] = LOCCTR[j + 1];	//마지막 프로그램의 길이 저장
				LOCCTR[loc_cnt] = -1;
				loc_cnt++;
				return;
			}
			//리터럴 폴에 값이 있는경우 리터럴 폴 비우고 리터럴테이블에 저장
			else
			{
				LOCCTR[loc_cnt] = -1;
				loc_cnt++;
				for (int p = 0; p < lit_cnt; p++)
				{
					strcpy(lit_table[r_lit_cnt].symbol, literals[p]);
					lit_table[r_lit_cnt].addr = loc;
					lit_table[r_lit_cnt].sub_r_cnt = r_cnt;
					LOCCTR[loc_cnt] = loc;
					if (lit_table[r_lit_cnt].symbol[1] == 'X')
					{
						loc += 1;
					}
					else
					{
						loc += 3;
					}
					loc_cnt++;
					LOCCTR[loc_cnt] = loc;
					end[r_cnt] = loc;
					r_lit_cnt++;
				}
			
			}
			r_lit_cnt--;
			LOCCTR[loc_cnt] = -1;
			loc_cnt++;
			return;
		}
	}
}

/* ----------------------------------------------------------------------------------
* 설명 : Immediate data를 화면에 출력하는 함수
* 매계 : 없음
* 반환 : 없음
* -----------------------------------------------------------------------------------
*/
void data_output()
{
	int f = 0;
	int k = 0;
	int l_cnt = 0;
	int rr_cnt = 0;
	for (int i = 0; i < loc_cnt; i++)
	{
		search_opcode(table[i]->operate);
		//섹션루틴이나 프로그램의 시작인 경우 loc값만 출력
		if (!strcmp(table[i]->operate, "START") || !strcmp(table[i]->operate, "CSECT"))
		{
			printf("%04X\t%s\t%s\n", LOCCTR[l_cnt], table[i]->label, table[i]->operate);
			
			rr_cnt++;
		}
		//LTORG가 나왔을 경우 리터럴 폴안의 값들의 object code와 함께 출력
		else if (!strcmp(table[i]->operate, "LTORG"))
		{
			if (!strcmp(table[i]->label, "\t"))
			{
				if (LOCCTR[l_cnt] == -1)
				{
					printf("%s\t%s\n",  table[i]->label, table[i]->operate);
				}
			}
			else
			{
				printf("%04X\t%s\t%s\n", LOCCTR[l_cnt], table[i]->label, table[i]->operate);
			}
			for (int j = 0; j < r_lit_cnt; j++)
			{
				if (rr_cnt == lit_table[k].sub_r_cnt)
				{
					l_cnt++;
					if (lit_table[k].symbol[1] == 'C')
					{
						printf("%04X\t%s\t\%s\t\t%02X%02X%02X\n", LOCCTR[l_cnt], "*", lit_table[k].symbol,addr[l_cnt].byte[0],
							addr[l_cnt].byte[1], addr[l_cnt].byte[2] );
						k++;
					}
					else if (lit_table[k].symbol[1] == 'X')
					{
						printf("%04X\t%s\t\%s\t\t%s\n", LOCCTR[l_cnt], "*", lit_table[k].symbol, addr[l_cnt].byte);
						k++;
					}
					
				}
				else
				{
					break;
				}
			}
		}
		//프로그램의 끝과 리터럴 값 출력
		else if (!strcmp(table[i]->operate, "END"))
		{
			if (!strcmp(table[i]->label, "\t"))
			{
				if (LOCCTR[l_cnt] == -1)
				{
					printf("%s\t%s\t%s\n", table[i]->label, table[i]->operate,table[i]->operand[3]);
				}
			}
			else
			{
				printf("%04X\t%s\t%s\n", LOCCTR[l_cnt], table[i]->label, table[i]->operate);
			}
			for (int j = 0; j < r_lit_cnt; j++)
			{
				if (rr_cnt == lit_table[k].sub_r_cnt)
				{
					l_cnt++;
					if (lit_table[k].symbol[1] == 'C')
					{
						printf("%04X\t%s\t\%s\t\t%02X%02X%02X\n", LOCCTR[l_cnt], "*", lit_table[k].symbol, addr[l_cnt].byte[0],
							addr[l_cnt].byte[1], addr[l_cnt].byte[2]);
						k++;
					}
					else if (lit_table[k].symbol[1] == 'X')
					{
						printf("%04X\t%s\t\%s\t\t%s\n", LOCCTR[l_cnt], "*", lit_table[k].symbol, addr[l_cnt].byte);
						
						k++;
					}

				}
				else
				{
					break;
				}
			}
	
			
			return;
		}
		//operator가 BYTE인 경우
		else if (!strcmp(table[i]->operate, "BYTE"))
		{
			if (table[i]->operand[0][0] == 'C')
			{
				printf("%04X\t%s\t\%s\t%s\t%02X%02X%02X\n", LOCCTR[l_cnt],  table[i]->label, table[i]->operate, table[i]->operand[0],
					addr[l_cnt].byte[0],addr[l_cnt].byte[1], addr[l_cnt].byte[2]);
				
			}
			else if (table[i]->operand[0][0] == 'X')
			{
				printf("%04X\t%s\t\%s\t%s\t%s\n", LOCCTR[l_cnt], table[i]->label, table[i]->operate,table[i]->operand[0],
					addr[l_cnt].byte);

				
			}
		}
		//operator가 WORD인 경우
		else if (!strcmp(table[i]->operate, "WORD"))
		{
			printf("%04X\t%s\t%s\t%s\t%06X\n", LOCCTR[l_cnt], table[i]->label, table[i]->operate, table[i]->operand[3], 
				addr[l_cnt].loc);
		}
		else
		{
			int jk = search_opcode(table[i]->operate);	//명령어 인지 검사
			f = instruction[jk].format;	//명령어의 format값
			switch (f)
			{
			case 2:	//2형식일 경우
			{
				if (!strcmp(table[i]->label, "\t"))
				{
					if (LOCCTR[l_cnt] == -1)
					{
						printf("%s\t%s\t%s\n", table[i]->label, table[i]->operate, table[i]->operand[3]);
					}
					else
					{
						printf("%04X%s\t%s\t%s\t%2X%02d\n", LOCCTR[l_cnt], table[i]->label, table[i]->operate, table[i]->operand[3],
							addr[l_cnt].op, addr[l_cnt].loc);
					}
				}
				else
				{
					printf("%04X\t%s\t%s\t%s\t%2X%02d\n", LOCCTR[l_cnt], table[i]->label, table[i]->operate, table[i]->operand[3],
						addr[l_cnt].op, addr[l_cnt].loc);
				}
				break;
			}
			case 3:	//3형식일 경우
			{
				if (!strcmp(table[i]->label, "\t"))
				{
					if (LOCCTR[l_cnt] == -1)
					{
						printf("%s\t%s\t%s\n", table[i]->label, table[i]->operate, table[i]->operand[3]);
					}
					else
					{
						printf("%04X%s\t%s\t%s\t%02X%1X%03X\n", LOCCTR[l_cnt], table[i]->label, table[i]->operate, table[i]->operand[3],
							addr[l_cnt].op,addr[l_cnt].xbpe, addr[l_cnt].loc);
					}
				}
				else
				{
					printf("%04X\t%s\t%s\t%s\t%02X%1X%03X\n", LOCCTR[l_cnt], table[i]->label, table[i]->operate, table[i]->operand[3],
						addr[l_cnt].op,addr[l_cnt].xbpe, addr[l_cnt].loc);
				}
				break;
			}
			case 4:	//4형식일 경우
			{
				if (!strcmp(table[i]->label, "\t"))
				{
					if (LOCCTR[l_cnt] == -1)
					{
						printf("%s\t%s\t%s\n", table[i]->label, table[i]->operate, table[i]->operand[3]);
					}
					else
					{
						printf("%04X%s\t%s\t%s\t%02X%1X%05X\n", LOCCTR[l_cnt], table[i]->label, table[i]->operate, table[i]->operand[3],
							addr[l_cnt].op, addr[l_cnt].xbpe, addr[l_cnt].loc);
					}
				}
				else
				{
					printf("%04X\t%s\t%s\t%s\t%02X%1X%05X\n", LOCCTR[l_cnt], table[i]->label, table[i]->operate, table[i]->operand[3],
						addr[l_cnt].op, addr[l_cnt].xbpe, addr[l_cnt].loc);
				}
				break;
			}
			//명령어가 아닌 경우
			default:
			{
				if (!strcmp(table[i]->label, "\t"))
				{
					if (LOCCTR[l_cnt] == -1)
					{
						printf("%s\t%s\t%s\n", table[i]->label, table[i]->operate, table[i]->operand[3]);
					}
					else
					{
						printf("%04X%s\t%s\t%s\n", LOCCTR[l_cnt], table[i]->label, table[i]->operate, table[i]->operand[3]
						);
					}
				}
				else
				{
					printf("%04X\t%s\t%s\t%s\n", LOCCTR[l_cnt], table[i]->label, table[i]->operate, table[i]->operand[3]
						);
				}
				break;
			}
			}
			
		}
		l_cnt++;
	}
	
	
}

//인자로 받은 char형 배열에 연산이 있는지 검색하는 함수 있을 경우 cal배열에 연산을 저장
//없을 경우  : 0 있을 경우 : 갯수
int search_cal(char *str)
{
	int cal_k = 0;
	for (int i = 0; i < strlen(str); i++)
	{
		if (str[i] == '+')
		{
			cal[cal_k++][0] = '+';
		}
		if (str[i] == '-')
		{
			cal[cal_k++][0] = '-';
		}
	}
	if (cal_k == 0)
	{
		return 0;
	}
	else
	{
		return cal_k;
	}
}
//인자로 받은 char형 배열 레지스터가 있는 경우
//없을 경우  : 0 있을 경우 : 레지스터의 넘버
int search_register(char *str)
{
	for (int i = 0; i < 9; i++)
	{
		if (!strcmp(reg[i].ch, str))
		{
			return reg[i].num;
		}
	}
	return 0;
}

