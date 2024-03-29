/**
  * @(#)server.frm.panes.OnlinePane.java  2008-8-29  
  * Copy Right Information	: Tarena
  * Project					: JavaQQ
  * JDK version used		: jdk1.6.4
  * Comments				: 在线用户类。
  * Version					: 1.0
  * Sr	Date		Modified By		Why & What is modified
  * 1.	2008-8-29 	小猪     		新建
  **/
package server.frm.panes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import server.frm.Server;
import tools.DateDeal;

import dao.UserDAOByFile;
import data.User;
import data.UserSex;
import data.UserState;

 /**
 * 在线用户面板。
 * 2008-8-29
 * @author		达内科技[Tarena Training Group]
 * @version	1.0
 * @since		JDK1.6(建议) 
 */
public class OnlinePane extends JPanel implements ActionListener,Runnable{
	/** 查询JQ用户 */
	private JLabel lblQuery = new JLabel("查询JQ用户");
	/** 输入的查询的关键字 */
	private JTextField txtQuery = new JTextField("");
	/** 查询的类型 */
	private JComboBox boxQuery = new JComboBox();
	/** 查询 */
	private JButton btnQuery = new JButton("查询");
	/** 添加新用户 */
	private JButton btnBroadCast = new JButton("广播系统消息");
	/** 刷新重新获得所有用户 */
	private JButton btnFlash = new JButton("刷新");
	
	/** 显示用户的表格 */
	private JTable table = null; //= new JTable();
	/** 表格的模型 */
	private DefaultTableModel model = null;
	
	/** 状态栏 */
	private JLabel lblInfo = new JLabel("共有JQ用户:0位");
	
	private BroadcastWindow broadcastWindow = null;
	
	public OnlinePane() {
		btnBroadCast.addActionListener(this);
		btnFlash.addActionListener(this);
		btnQuery.addActionListener(this);
		init();
	}
	
	/**
	 * 初始化添加组件。初始化面板。因为时间关系，可能写的比较戳，望见谅。
	 */
	private void init(){
		txtQuery.setPreferredSize(new Dimension(100,25));
		boxQuery.addItem(new QueryWay("按JQ号查询",0));
		boxQuery.addItem(new QueryWay("按昵称查询",1));
		
		model = new MyDefaultTableModel();
		model.addColumn("ID");
		model.addColumn("JQ号");
		model.addColumn("昵称");
		model.addColumn("姓名");
		model.addColumn("性别");
		model.addColumn("年龄");
		model.addColumn("E-mail");
		model.addColumn("状态");
		model.addColumn("注册时间");
		
		table = new JTable(model);
		TableRowSorter sorter = new TableRowSorter(model); 
		table.setRowSorter(sorter);
		TableColumn tcID = table.getColumn(model.getColumnName(0));
		tcID.setPreferredWidth(25);
		tcID.setMaxWidth(25);
		tcID.setMinWidth(25);
		TableColumn tcJQ = table.getColumn(model.getColumnName(1));
		tcJQ.setPreferredWidth(70);
		tcJQ.setMaxWidth(80);
		tcJQ.setMinWidth(60);
		TableColumn tcNick = table.getColumn(model.getColumnName(2));
		tcNick.setPreferredWidth(70);
		tcNick.setMaxWidth(80);
		tcNick.setMinWidth(60);
		TableColumn tcName = table.getColumn(model.getColumnName(3));
		tcName.setPreferredWidth(60);
		tcName.setMaxWidth(65);
		tcName.setMinWidth(55);
		TableColumn tcSex = table.getColumn(model.getColumnName(4));
		tcSex.setPreferredWidth(30);
		tcSex.setMaxWidth(30);
		tcSex.setMinWidth(30);
		TableColumn tcAge = table.getColumn(model.getColumnName(5));
		tcAge.setPreferredWidth(30);
		tcAge.setMaxWidth(30);
		tcAge.setMinWidth(30);
		TableColumn tcState = table.getColumn(model.getColumnName(7));
		tcState.setPreferredWidth(30);
		tcState.setMaxWidth(30);
		tcState.setMinWidth(30);
		
		JPanel paneNorth = new JPanel();
		paneNorth.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		paneNorth.add(lblQuery);
		paneNorth.add(txtQuery);
		paneNorth.add(boxQuery);
		paneNorth.add(btnQuery);
		paneNorth.add(btnFlash);
		paneNorth.add(btnBroadCast);
		
		JPanel paneSouth = new JPanel();
		paneSouth.add(lblInfo);
		
		setLayout(new BorderLayout());
		add(paneNorth,BorderLayout.NORTH);
		add(new JScrollPane(table),BorderLayout.CENTER);
		add(new FillWidth(5,5),BorderLayout.WEST);
		add(new FillWidth(5,5),BorderLayout.EAST);
		add(paneSouth,BorderLayout.SOUTH);
	}
	/**
	 * 广播消息按钮、刷新按钮、查询按钮的事件。
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==btnBroadCast){
			if(broadcastWindow==null)
				broadcastWindow = new BroadcastWindow();
			else
				broadcastWindow.setVisible(true);
		}
		if(e.getSource()==btnFlash){
			flushOnlineUser();
		}
		if(e.getSource()==btnQuery){
			QueryOnlineUser(txtQuery.getText(),((QueryWay)boxQuery.getSelectedItem()).getWay());
		}
	}
	
	/**
	 * 查询方式。
	 * 2008-9-27
	 * @author		达内科技[Tarena Training Group]
	 * @version	1.0
	 * @since		JDK1.6(建议) 
	 */
	private class QueryWay{
		private String name;
		private int way;
		public QueryWay(String name,int way) {
			this.name = name;
			this.way = way;
		}
		public String toString() {
			return this.name;
		}
		public int getWay() {
			return way;
		}
	}
	
	public void run() {
		btnFlash.setEnabled(false);
		if(Server.isFileWay){
			table.setAutoCreateRowSorter(false);
			model.setRowCount(0);
			int totalNum = 0;
			int onlineNum = 0;
			int hiddenNum = 0;
			int departureNum = 0;
			int busyNum = 0;
			if(ServicePane.table!=null && !ServicePane.table.isEmpty()){
				totalNum = ServicePane.table.size();
				Enumeration<Integer> onlineUser = ServicePane.table.keys();
				UserDAOByFile userDAO = new UserDAOByFile();
				while(onlineUser.hasMoreElements()){
					Integer jqnum = onlineUser.nextElement();
					User user = userDAO.findById(jqnum);
					if(user!=null){
						if(user.getState()==UserState.ONLINESTATE.getState())
							onlineNum++;
						if(user.getState()==UserState.DEPARTURESTATE.getState())
							departureNum++;
						if(user.getState()==UserState.BUSYSTATE.getState())
							busyNum++;
						if(user.getState()==UserState.HIDDENSTATE.getState())
							hiddenNum++;
					}
					addUserToTable(user);
				}
			}
			table.setAutoCreateRowSorter(true);
			lblInfo.setText("共有JQ用户:"+totalNum+"位.其中," +
					"在线用户:"+onlineNum+"位." +
					"隐身用户:"+hiddenNum+"位." +
					"离开用户:"+departureNum+"位." +
					"繁忙用户:"+busyNum+"位.");
		}else
			lblInfo.setText("注意:系统暂不支持数据库方式!请使用文件方式保存数据!");
		btnFlash.setEnabled(true);
	}
	
	/**
	 * 刷新当前在线用户。
	 */
	public void flushOnlineUser(){
		new Thread(this).start();
	}
	
	/**
	 * 根据查询类型查询在线用户
	 * @param query 查询的条件。
	 * @param type 查询的类型。
	 */
	private void QueryOnlineUser(String query,int type){
		//按jq号查询
		if(query.equals(""))
			return;
		UserDAOByFile userDAO = new UserDAOByFile();
		model.setRowCount(0);
		if(type==0){
			try {
				int jqnum = Integer.parseInt(query);
				if(ServicePane.table.containsKey(jqnum)){
					User user = userDAO.findById(jqnum);
					if(user!=null)
						addUserToTable(user);
				}
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "请确保输入的正确的JQ号码!");
				return;
			}
			return;
		}
		//按昵称号查询
		if(type==1){
				Vector<User> users= userDAO.findOnlineUserByName(query);
				for(User user:users){
					if(user!=null)
						addUserToTable(user);
				}
			return;
		}
		
	}
	
	/**
	 * 向table中添加用户
	 * @param user User对象。
	 * @see User
	 */
	private void addUserToTable(User user){
		if(user!=null){
			Vector<Object> v = new Vector<Object>();
			v.add(user.getId());
			v.add(user.getJqnum());
			v.add(user.getNickname());
			v.add(user.getRealname());
			v.add(UserSex.getSex(user.getSex()));
			v.add(user.getAge());
			v.add(user.getEmail());
			v.add(UserState.getStateName(user.getState()));
			v.add(DateDeal.getDate(user.getRegisterTime()));
			
			model.addRow(v);
		}
	}
	
	/**
	 * 自己定制的DefaultTableModel
	 * 2008-9-27
	 * @author		达内科技[Tarena Training Group]
	 * @version	1.0
	 * @since		JDK1.6(建议) 
	 */
	private class MyDefaultTableModel extends DefaultTableModel{
		
		public boolean isCellEditable(int row, int column) {
			super.isCellEditable(row, column);
			return false;
		}
	}
	
	/**
	 * 广播窗体类。
	 * 2008-9-27
	 * @author		达内科技[Tarena Training Group]
	 * @version	1.0
	 * @since		JDK1.6(建议) 
	 */
	private class BroadcastWindow extends JDialog implements WindowFocusListener,ActionListener{
		private JTextArea txt = new JTextArea();
		private JButton btnSend = new JButton("发送");
		private JButton btnClose = new JButton("关闭");
		private JButton btnClear = new JButton("清空");
		public BroadcastWindow() {
			//super(null,true);
			setTitle("广播系统消息");
			setSize(300,200);
			setResizable(false);
			Toolkit tk=Toolkit.getDefaultToolkit();
			setLocation((tk.getScreenSize().width-getSize().width)/2,(tk.getScreenSize().height-getSize().height)/2);
			
			btnSend.addActionListener(this);
			btnClear.addActionListener(this);
			btnClose.addActionListener(this);
			
			JPanel pane = new JPanel();
			pane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			pane.setPreferredSize(new Dimension(300,35));
			pane.add(btnClose);
			pane.add(btnClear);
			pane.add(btnSend);
			
			txt.setLineWrap(true);
			add(new JScrollPane(txt),BorderLayout.CENTER);
			add(pane,BorderLayout.SOUTH);
			add(new FillWidth(2,2),BorderLayout.NORTH);
			add(new FillWidth(4,4),BorderLayout.EAST);
			add(new FillWidth(4,4),BorderLayout.WEST);
			
			addWindowFocusListener(this);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setVisible(true);
		}
		public void windowGainedFocus(WindowEvent e) {}
		public void windowLostFocus(WindowEvent e) {
			dispose();
		}
		public void actionPerformed(ActionEvent e) {
			if(e.getSource()==btnClear)
				txt.setText("");
			if(e.getSource()==btnClose)
				dispose();
			if(e.getSource()==btnSend){
				String msg = txt.getText();
				if(!msg.equals("")){
					if(ServicePane.broadcast(msg)){
						txt.setText("");
						JOptionPane.showMessageDialog(null, "发送成功!");
					}				
				}
					
			}
		}
	}
}
