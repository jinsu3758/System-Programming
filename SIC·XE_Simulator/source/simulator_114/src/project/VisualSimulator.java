package project;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SingleSelectionModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.Color;
import javax.swing.JTextArea;

//�ùķ����͸� ���۽�Ű�� ���� ������ �����Ѵ�.
//SICXE�� ���� �δ��� �����Ű��, �ε�� ������ �о� �����ش�

public class VisualSimulator extends JFrame implements ActionListener{

	static instruction[] inst_table = new instruction[256];
	static int sub_cnt;
	public JPanel contentPane;
	public JTextField text_file;	//file �ؽ�Ʈ�ʵ�
	public JTextField pro_name;		//���α׷� �̸� �ؽ�Ʈ�ʵ�
	public JTextField fir_inst_addr;	//E���ڵ��� �����ּ�
	public JTextField start_addr_obj;	//���α׷��� �����ּ�
	public JTextField pro_len;	//���α׷��� ����
	public JTextField a_dec;	//A���������� 1
	public JTextField a_hex;	//
	public JTextField x_dec;	//
	public JTextField x_hex;	//
	public JTextField l_dec;	//
	public JTextField l_hex;	//
	public JTextField b_dec;	//		�� �������͵��� �ؽ�Ʈ �ʵ�
	public JTextField b_hex;	//
	public JTextField start_addr_m;	// 	�޸��� �����ּ�
	public JTextField target;
	public JTextField s_dec;	//
	public JTextField s_hex;	//
	public JTextField t_dec;	//			�� �������͵��� �ؽ�Ʈ �ʵ�
	public JTextField t_hex;	//
	public JTextField f_dec;	//
	public JTextField pc_dec;	//
	public JTextField pc_hex;	//
	public JTextField sw_dec;	//
	public JLabel label_2;
	public JLabel label_3;
	public JLabel label_4;
	public JLabel lblinst;
	public JScrollPane scroll;
	public JTextField text_ing;	//������� ��ġ
	public JTextArea log;	//log
	public JButton btn_open;	//���� ��ư
	public JButton btn_step;	//����(step)��ư
	private JButton run_btn;	//����(all)��ư
	public JFileChooser jfc = new JFileChooser();
	public static int step=0;
	private JList list;	//��ɾ� ����Ʈ
	private Vector listData;	//��ɾ� ����Ʈ�� ���� vector
	private JButton exit_btn;	//���� ��ư


	static BufferedReader bw=null;
	static String filename = null;
	static int inst_num=0;
	static ResourceManager rmgr = new ResourceManager();
	static SICXE sic=new SICXE();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		VisualSimulator frame = new VisualSimulator();
		frame.setTitle("SIC/XE");
		frame.setVisible(true);
		
		
		for(int i=0; i<256; i++)
		{
			inst_table[i]=new instruction(null,0,0);
		}
		//
		//��ɾ inst.data���� �ҷ��� inst���̺� ����
		//
		try {
			bw = new BufferedReader(new FileReader("inst.data"));
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
			inst_table[i].setName(line[0]);
			inst_table[i].opcode=Integer.parseInt(line[1],16);
			inst_table[i].format=Integer.parseInt(line[2]);
			
			}
			bw.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	/*	EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VisualSimulator frame = new VisualSimulator();
					frame.setTitle("SIC/XE");
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});*/
		
		
	}

	
	/**
	 * Create the frame.
	 */
	public VisualSimulator() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 746, 882);
		contentPane = new JPanel();
		contentPane.setForeground(Color.PINK);
		
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel label_file = new JLabel("FileName :");
		label_file.setBounds(17, 15, 93, 21);
		contentPane.add(label_file);
		
		text_file = new JTextField();
		text_file.setBounds(120, 12, 156, 27);
		contentPane.add(text_file);
		text_file.setColumns(10);
		
		btn_open = new JButton("Open");
		btn_open.setBounds(293, 11, 125, 29);
		btn_open.addActionListener(this);
		contentPane.add(btn_open);
		
		JPanel panel_h = new JPanel();
		panel_h.setForeground(Color.PINK);
		panel_h.setBorder(new TitledBorder(null, "H (Header Record)", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
		panel_h.setBounds(27, 60, 306, 144);
		contentPane.add(panel_h);
		panel_h.setLayout(null);
		
		JLabel label_name = new JLabel("Program Name :");
		label_name.setBounds(17, 31, 134, 21);
		panel_h.add(label_name);
		
		pro_name = new JTextField();
		pro_name.setBounds(123, 28, 156, 27);
		panel_h.add(pro_name);
		pro_name.setColumns(10);
		
		JLabel label_start1 = new JLabel("Start Address of");
		label_start1.setBounds(17, 59, 144, 21);
		panel_h.add(label_start1);
		
		JLabel label_start2 = new JLabel("Object Program :");
		label_start2.setBounds(17, 78, 144, 21);
		panel_h.add(label_start2);
		
		start_addr_obj = new JTextField();
		start_addr_obj.setBounds(123, 75, 156, 27);
		panel_h.add(start_addr_obj);
		start_addr_obj.setColumns(10);
		
		JLabel label_length = new JLabel("Length of Program :");
		label_length.setBounds(17, 112, 170, 21);
		panel_h.add(label_length);
		
		pro_len = new JTextField();
		pro_len.setBounds(145, 109, 134, 27);
		panel_h.add(pro_len);
		pro_len.setColumns(10);
		
		JPanel panel_e = new JPanel();
		panel_e.setBorder(new TitledBorder(null, "E (End Record)", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
		panel_e.setBounds(368, 60, 296, 101);
		contentPane.add(panel_e);
		panel_e.setLayout(null);
		
		JLabel label_end1 = new JLabel("Address of First Instruction");
		label_end1.setBounds(17, 29, 231, 21);
		panel_e.add(label_end1);
		
		JLabel label_end2 = new JLabel("in Object Program :");
		label_end2.setBounds(17, 65, 169, 21);
		panel_e.add(label_end2);
		
		fir_inst_addr = new JTextField();
		fir_inst_addr.setBounds(135, 65, 141, 27);
		panel_e.add(fir_inst_addr);
		fir_inst_addr.setColumns(10);
		
		JPanel panel_r = new JPanel();
		panel_r.setBorder(new TitledBorder(null, "Register", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
		panel_r.setBounds(27, 220, 306, 368);
		contentPane.add(panel_r);
		panel_r.setLayout(null);
		
		JLabel lblA = new JLabel("A (#0)");
		lblA.setBounds(17, 52, 57, 21);
		panel_r.add(lblA);
		
		JLabel lblDec = new JLabel("Dec");
		lblDec.setBounds(76, 25, 78, 21);
		panel_r.add(lblDec);
		
		a_dec = new JTextField();
		a_dec.setBounds(76, 52, 92, 27);
		panel_r.add(a_dec);
		a_dec.setColumns(10);
		
		a_hex = new JTextField();
		a_hex.setColumns(10);
		a_hex.setBounds(185, 52, 92, 27);
		panel_r.add(a_hex);
		
		x_dec = new JTextField();
		x_dec.setColumns(10);
		x_dec.setBounds(76, 86, 92, 27);
		panel_r.add(x_dec);
		
		x_hex = new JTextField();
		x_hex.setColumns(10);
		x_hex.setBounds(185, 86, 92, 27);
		panel_r.add(x_hex);
		
		l_dec = new JTextField();
		l_dec.setColumns(10);
		l_dec.setBounds(76, 121, 92, 27);
		panel_r.add(l_dec);
		
		l_hex = new JTextField();
		l_hex.setColumns(10);
		l_hex.setBounds(185, 121, 92, 27);
		panel_r.add(l_hex);
		
		b_dec = new JTextField();
		b_dec.setColumns(10);
		b_dec.setBounds(76, 155, 92, 27);
		panel_r.add(b_dec);
		
		b_hex = new JTextField();
		b_hex.setColumns(10);
		b_hex.setBounds(185, 155, 92, 27);
		panel_r.add(b_hex);
		
		JLabel lblHex = new JLabel("Hex");
		lblHex.setBounds(185, 25, 78, 21);
		panel_r.add(lblHex);
		
		JLabel lblX = new JLabel("X (#1)");
		lblX.setBounds(17, 86, 57, 21);
		panel_r.add(lblX);
		
		JLabel lblL = new JLabel("L (#2)");
		lblL.setBounds(17, 121, 57, 21);
		panel_r.add(lblL);
		
		JLabel lblSw = new JLabel("SW(#9)");
		lblSw.setBounds(17, 331, 57, 21);
		panel_r.add(lblSw);
		
		JLabel label = new JLabel("B (#3)");
		label.setBounds(17, 155, 57, 21);
		panel_r.add(label);
		
		JLabel label_1 = new JLabel("S (#4)");
		label_1.setBounds(17, 191, 57, 21);
		panel_r.add(label_1);
		
		s_dec = new JTextField();
		s_dec.setColumns(10);
		s_dec.setBounds(76, 191, 92, 27);
		panel_r.add(s_dec);
		
		s_hex = new JTextField();
		s_hex.setColumns(10);
		s_hex.setBounds(185, 190, 92, 27);
		panel_r.add(s_hex);
		
		t_dec = new JTextField();
		t_dec.setColumns(10);
		t_dec.setBounds(76, 226, 92, 27);
		panel_r.add(t_dec);
		
		t_hex = new JTextField();
		t_hex.setColumns(10);
		t_hex.setBounds(185, 226, 92, 27);
		panel_r.add(t_hex);
		
		f_dec = new JTextField();
		f_dec.setColumns(10);
		f_dec.setBounds(76, 262, 201, 27);
		panel_r.add(f_dec);
		
		pc_dec = new JTextField();
		pc_dec.setColumns(10);
		pc_dec.setBounds(76, 297, 92, 27);
		panel_r.add(pc_dec);
		
		pc_hex = new JTextField();
		pc_hex.setColumns(10);
		pc_hex.setBounds(185, 297, 92, 27);
		panel_r.add(pc_hex);
		
		sw_dec = new JTextField();
		sw_dec.setColumns(10);
		sw_dec.setBounds(76, 331, 201, 27);
		panel_r.add(sw_dec);
		
		label_2 = new JLabel("T (#5)");
		label_2.setBounds(17, 226, 57, 21);
		panel_r.add(label_2);
		
		label_3 = new JLabel("F (#6)");
		label_3.setBounds(17, 262, 57, 21);
		panel_r.add(label_3);
		
		label_4 = new JLabel("PC(#8)");
		label_4.setBounds(17, 297, 57, 21);
		panel_r.add(label_4);
		
		JLabel lbl_start_addr = new JLabel("Start Address in Memory");
		lbl_start_addr.setBounds(368, 176, 213, 21);
		contentPane.add(lbl_start_addr);
		
		start_addr_m = new JTextField();
		start_addr_m.setBounds(477, 200, 156, 27);
		contentPane.add(start_addr_m);
		start_addr_m.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Target Address :");
		lblNewLabel_1.setBounds(368, 236, 137, 21);
		contentPane.add(lblNewLabel_1);
		
		target = new JTextField();
		target.setBounds(477, 233, 156, 27);
		contentPane.add(target);
		target.setColumns(10);
		
		lblinst = new JLabel("Instruction :");
		lblinst.setBounds(368, 272, 102, 21);
		contentPane.add(lblinst);
		
		listData = new Vector();
		list = new JList(listData);
		
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//inst_txt.setBounds(368, 308, 156, 277);
		//contentPane.add(inst_txt);
		
		scroll = new JScrollPane(list);
		scroll.setBounds(368, 308, 156, 277);
		contentPane.add(scroll);
		
		JLabel lblNewLabel = new JLabel("Device Name");
		lblNewLabel.setBounds(566, 308, 114, 21);
		contentPane.add(lblNewLabel);
		
		text_ing = new JTextField();
		text_ing.setBounds(554, 337, 156, 27);
		contentPane.add(text_ing);
		text_ing.setColumns(10);
		
		exit_btn = new JButton("Exit");
		exit_btn.setBounds(555, 556, 125, 29);
		
		contentPane.add(exit_btn);
		exit_btn.addActionListener(this);
		
		run_btn = new JButton("Run(All)");
		run_btn.setBounds(555, 512, 125, 29);
		contentPane.add(run_btn);
		run_btn.addActionListener(this);
		
		btn_step = new JButton("Run(1 Step)");
		btn_step.setBounds(555, 468, 125, 29);
		btn_step.addActionListener(this);
		contentPane.add(btn_step);
		
		JLabel lblLog = new JLabel("Log(instruction)");
		lblLog.setBounds(27, 592, 200, 21);
		contentPane.add(lblLog);
		
		log = new JTextArea();
		JScrollPane log_scroll = new JScrollPane(log);
		log_scroll.setBounds(27, 624, 660, 187);
		contentPane.add(log_scroll);

		jfc.setFileFilter(new FileNameExtensionFilter("txt", "txt"));
		// ���� ����
		jfc.setMultiSelectionEnabled(false);// ���� ���� �Ұ�
	}

	// �ùķ����͸� ���۽�Ű�� ���� ������ �����Ѵ�.
	// sic �ùķ����͸� ���� �δ��� �����Ű��, �ε�� ������ �о� �����־�
	// ������ ������ �� �ִ� ���·� ����� ���´�.
	public void initialize(File objFile, ResourceManager rMgr) 
	{
		sub_cnt++;
		sic.initialize(objFile, rMgr);	//�δ��� ���� rmgr�� �޸𸮿� �������и� �ʱ�ȭ
		sic.load(objFile, rMgr);	//�ε� ����
		//
		//����Ʈ����Ÿ�� ��ɾ �߰�
 		for(int i=0; i<sic.inst.size();i++)
		{
			listData.addElement(sic.inst.get(i));
			//inst_txt.append(sic.inst.get(i)+"\n");
		}
 		//��ɾ �߰��� ����Ʈ����Ÿ�� ��ɾ��Ʈ�� �߰�
		list.setListData(listData);
		
		//
		//��� �ؽ�Ʈ�ʵ带 �ʱ�ȭ�Ѵ�.
		//
		pro_name.setText(sic.name.get(0));
		start_addr_obj.setText(sic.start.get(0));
		pro_len.setText(sic.length.get(0));
		fir_inst_addr.setText(sic.start.get(0));
		start_addr_m.setText(sic.start.get(0));
		a_dec.setText("0");
		b_dec.setText("0");
		s_dec.setText("0");
		t_dec.setText("0");
		l_dec.setText("0");
		pc_dec.setText("0");
		sw_dec.setText("0");
		f_dec.setText("0");
		x_dec.setText("0");
		target.setText("000000");
		
		a_hex.setText("000000");
		b_hex.setText("000000");
		s_hex.setText("000000");
		t_hex.setText("000000");
		l_hex.setText("000000");
		pc_hex.setText("000000");
		x_hex.setText("000000");
		//log�� ��ɾ� �߰�
		for(int i=0; i<sic.inst.size();i++)
		{
			log.append(sic.inst_name.get(i) + "\n");
		}
		

	}

	// �ϳ��� ��ɾ �����ϴ� �޼ҵ�ν� sic �ùķ����Ϳ��� �۾��� �����Ѵ�.
	public void oneStep() {
		
		sic.oneStep(sub_cnt,step);
		//����̽� �̸��� ���� ��쿡�� ��ĭ�� ����
		if(rmgr.device_name==null)
		{
			text_ing.setText("");
		}
		//����̽� �̸��� ���� ��쿡�� �̸� �߰�
		else
		{
			text_ing.setText(rmgr.device_name);
		}
		//�����ƾ�� ���� �Ǹ� h���ڵ�� e���ڵ� ���� ��� ����
		if(step+1==rmgr.r_cnt[sub_cnt-1] && sub_cnt!=3)
		{
			sub_cnt++;
			pro_name.setText(sic.name.get(sub_cnt-1));
			start_addr_obj.setText(sic.r_start.get(sub_cnt-2));
			pro_len.setText(sic.length.get(sub_cnt-1));
			fir_inst_addr.setText(sic.r_start.get(sub_cnt-2));
			start_addr_m.setText(sic.r_start.get(sub_cnt-2));
		}
		//Ÿ�� �ּ� ����
		target.setText(String.format("%06X",rmgr.target[step]));
		update();	//scroll ������Ʈ
		//
		//	�������Ͱ� ����
		//
		if (rmgr.reg_a[step] != -1) 
		{
			a_dec.setText(Integer.toString(rmgr.reg_a[step]));
			a_hex.setText(String.format("%06X%n", rmgr.reg_a[step]));
		}
		if (rmgr.reg_b[step] != -1) 
		{
			b_dec.setText(Integer.toString(rmgr.reg_b[step]));
			b_hex.setText(String.format("%06X%n", rmgr.reg_b[step]));
		}
		if (rmgr.reg_x[step] != -1) 
		{
			x_dec.setText(Integer.toString(rmgr.reg_x[step]));
			x_hex.setText(String.format("%06X%n", rmgr.reg_x[step]));
		}
		if (rmgr.reg_l[step] != -1) 
		{
			l_dec.setText(Integer.toString(rmgr.reg_l[step]));
			l_hex.setText(String.format("%06X%n", rmgr.reg_l[step]));
		}
		if (rmgr.reg_s[step] != -1) 
		{
			s_dec.setText(Integer.toString(rmgr.reg_s[step]));
			s_hex.setText(String.format("%06X%n", rmgr.reg_s[step]));
		}
		if (rmgr.reg_t[step] != -1) 
		{
			t_dec.setText(Integer.toString(rmgr.reg_t[step]));
			t_hex.setText(String.format("%06X%n", rmgr.reg_t[step]));
		}
		if (rmgr.reg_f[step] != -1) 
		{
			f_dec.setText(Integer.toString(rmgr.reg_b[step]));
		}
		if (rmgr.reg_pc[step] != -1) 
		{
			pc_dec.setText(Integer.toString(rmgr.reg_pc[step]));
			pc_hex.setText(String.format("%06X%n", rmgr.reg_pc[step]));
		}
		if (rmgr.reg_sw[step] != -1) 
		{
			sw_dec.setText(Integer.toString(rmgr.reg_sw[step]));
		}
		step++;

	}

	// ���� ��ɾ ��� �����ϴ� �޼ҵ�ν� sic �ùķ����Ϳ� �۾��� ����
	public void allStep() 
	{
		sic.allStep();
		for(int i=0; i<sic.inst.size();i++)
		{
			oneStep();
		}
		
	}

	// �۾��� �Ϸ�Ǿ��� �� ��ȭ�� ����� ȭ�鿡 ������Ʈ
	public void update() {
		list.setSelectedIndex(step);
		
		if(step==12)
		{
			scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
		}
	}

	//��ư�� �����ϴ� �޼ҵ�
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		//���� ���� ��ư
		if (e.getSource() == btn_open) 
		{
			if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) 
			{
				filename = jfc.getSelectedFile().toString();
				File file = new File(filename);
				text_file.setText(filename);
				initialize(file,rmgr);
			}
		} 
		//����(step) ��ư
		else if(e.getSource()==btn_step)
		{
			oneStep();
		}
		//����(ALL) ��ư
		else if(e.getSource()==run_btn)
		{
			allStep();
		}
		//���� ��ư
		else if(e.getSource()==exit_btn)
		{
			System.exit(0);
		}
		

	}

}
