package org.openstreetmap.OSMZmiany;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;

import javax.swing.JFrame;

import org.openstreetmap.OSMZmiany.DataContainer.Changeset;
import org.openstreetmap.OSMZmiany.DataContainer.Node;

import javax.swing.DefaultListModel;
import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import java.awt.Color;

public class OSMZmiany extends JFrame implements ZMapWidgetListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2976479683829295126L;	
	
	public static OSMZmiany instance;
	
	private Timer refetchTimer;
	private int seqNum;
		
	public DataContainer dc;
	private SelectedDrawStyle sDS;
	
	//Widgets
	private ZMapWidget map;
	private JTextField tfURL;
	private JCheckBox cbxLiveEdit;
	private JList list;
	private DefaultListModel model = new DefaultListModel();
	
	
	private JButton btUser;
	private JButton btChangeset;
	private JButton btNode;

	public OSMZmiany() {
		dc=new DataContainer();
		this.setTitle("OSMZmiany");
		
		GridBagLayout gbl = new GridBagLayout();
		gbl.rowWeights = new double[]{1.0};
		gbl.columnWeights = new double[]{1.0};		
		
		getContentPane().setLayout(gbl);
		
		JSplitPane splitPane = new JSplitPane();
		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.gridx = 0;
		gbc_splitPane.gridy = 0;
		getContentPane().add(splitPane, gbc_splitPane);
		
		this.setSize(800, 800);
		
		//MAP
		map = new ZMapWidget(dc);
		map.setSize(400, 400);
		sDS=new SelectedDrawStyle(map);
		map.setDrawStyle(sDS);
		map.addZMapWidgetListener(this);
				
				
		splitPane.setLeftComponent(map);
		
		JPanel panel = new JPanel();
		splitPane.setRightComponent(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("General", null, panel_1, null);
		
		final JButton btnSetBox = new JButton("Set Box");
		btnSetBox.setBounds(94, 41, 129, 24);
		btnSetBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				map.setBBox();
			}
		});
		
		JButton btnRemoveBox = new JButton("Remove Box");
		btnRemoveBox.setBounds(230, 41, 118, 24);
		btnRemoveBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				map.removeBBox();
			}
		});
		panel_1.setLayout(null);
		
		JLabel lblSetBoundary = new JLabel("Set boundary:");
		lblSetBoundary.setBounds(12, 10, 101, 14);
		panel_1.add(lblSetBoundary);
		panel_1.add(btnRemoveBox);
		
		panel_1.add(btnSetBox);
		
		JLabel lblData = new JLabel("Data:");
		lblData.setBounds(12, 86, 39, 14);
		panel_1.add(lblData);
		
		JLabel lblDiffUrl = new JLabel("Diff URL");
		lblDiffUrl.setBounds(12, 117, 55, 14);
		panel_1.add(lblDiffUrl);
		
		tfURL = new JTextField();
		tfURL.setBounds(94, 112, 254, 24);
		panel_1.add(tfURL);
		tfURL.setColumns(10);
		
		JButton btnLoad = new JButton("Load");
		btnLoad.setBounds(279, 148, 69, 24);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getData(tfURL.getText());
			}
		});
		panel_1.add(btnLoad);
		
		cbxLiveEdit = new JCheckBox("Live edit diff");
		cbxLiveEdit.setBounds(94, 184, 159, 22);
		cbxLiveEdit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				getData();	
			}
		});
		
		panel_1.add(cbxLiveEdit);
		
		
		JButton btnClear = new JButton("Clear");
		btnClear.setBounds(277, 184, 71, 24);
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dc.clear();	
			}
		});
		
		
		panel_1.add(btnClear);
		panel.add(tabbedPane);
		
		JPanel panel_2 = new JPanel();
		tabbedPane.addTab("Changesets", null, panel_2, null);
		panel_2.setLayout(null);
		
		JButton btnShowSite = new JButton("Info");
		btnShowSite.setBounds(154, 5, 61, 24);
		panel_2.add(btnShowSite);
		
		list = new JList(model);
		list.setBounds(12, 703, 351, -655);
		panel_2.add(list);
		
		JPanel panel_3 = new JPanel();
		tabbedPane.addTab("Info", null, panel_3, null);
		panel_3.setLayout(null);
		
		JLabel lblNode = new JLabel("Node");
		lblNode.setBounds(12, 12, 70, 14);
		panel_3.add(lblNode);
		
		JLabel lblChangeset = new JLabel("Changeset");
		lblChangeset.setBounds(12, 38, 90, 14);
		panel_3.add(lblChangeset);
		
		JLabel lblUser = new JLabel("User");
		lblUser.setBounds(12, 64, 70, 14);
		panel_3.add(lblUser);
		
		btChangeset = new JButton("#ID");
		btChangeset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(!btChangeset.getText().equals("#ID"))
					OSMZmiany.openURL("http://www.openstreetmap.org/browse/changeset/"+btChangeset.getText());	
			}
		});
		btChangeset.setForeground(Color.BLUE);
		btChangeset.setBounds(109, 38, 120, 14);
		panel_3.add(btChangeset);
		
		btNode = new JButton("#ID");
		btNode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(!btNode.getText().equals("#ID"))
					OSMZmiany.openURL("http://www.openstreetmap.org/browse/node/"+btNode.getText());
			}
		});
		btNode.setForeground(Color.BLUE);
		btNode.setBounds(109, 12, 120, 14);
		panel_3.add(btNode);
		
		btUser = new JButton("#ID");
		btUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(!btUser.getText().equals("#ID"))
					OSMZmiany.openURL("http://www.openstreetmap.org/user/"+btUser.getText());
			}
		});
		btUser.setForeground(Color.BLUE);
		btUser.setBounds(109, 64, 120, 14);
		panel_3.add(btUser);
		
		JButton btSelectChangeset = new JButton("Select changeset");
		btSelectChangeset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Node n=sDS.getSelectedNode();
				if(n!=null){
					sDS.setSelection(dc.changesets.get(dc.changesetsIndex.get(n.changesetId)));
				}
			}
		});
		btSelectChangeset.setBounds(12, 90, 216, 24);
		panel_3.add(btSelectChangeset);
		setVisible(true);


		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		refetchTimer = new Timer();
		refetchTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if(cbxLiveEdit.isSelected()){
					initChangeStream();
					getData();
				}
			}
		}, 20000, 30000);		

	}

	private void initChangeStream() {
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(
							new BufferedInputStream(
									new URL(
											"http://planet.openstreetmap.org/minute-replicate/state.txt")
											.openStream())));
			br.readLine();
			String seqNumStr = br.readLine();
			seqNum = Integer.parseInt(seqNumStr.substring(seqNumStr
					.indexOf("=") + 1));
			br.readLine();
			br.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	public void getData() {		
			DecimalFormat myFormat = new DecimalFormat("000");
			String url = "http://planet.openstreetmap.org/minute-replicate/"
					+ myFormat.format(seqNum / 1000000) + "/"
					+ myFormat.format((seqNum / 1000) % 1000) + "/"
					+ myFormat.format(seqNum % 1000) + ".osc.gz";			
			getData(url);
			seqNum++;			
	}
	
	public void getData(String url){
		try {
			BufferedInputStream bis = new BufferedInputStream(
					new GZIPInputStream(new URL(url).openStream()));
			System.out.println("Download: "+url);
			dc.addData(bis);		
		} catch (IOException ioe) {
			if (ioe instanceof FileNotFoundException) {
			} else {
				ioe.printStackTrace();
			}
		}
	}


	public void reloadChangesets(){
		model.clear();
		Iterator<Changeset> iterator = dc.changesets.iterator();
	    while (iterator.hasNext()) {
	    	Changeset ch=iterator.next();
	    	model.add(0, ch.id+":"+dc.users.get(ch.userId).name);  	
	    }
	}
				
	public static void openURL(String url) {
		if( !java.awt.Desktop.isDesktopSupported() ) {
            System.err.println( "Desktop is not supported (fatal)" );
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {
            System.err.println( "Desktop doesn't support the browse action (fatal)" );
        }
       try {
           java.net.URI uri = new java.net.URI( url );
           desktop.browse( uri );
       }
       catch ( Exception e ) {
            System.err.println( e.getMessage() );
       }
    }
	
	public static void main(String[] args) {
		instance = new OSMZmiany();
		instance.initChangeStream();
		instance.setVisible(true);
	}

	public void nodeClicked(Node node) {
		btNode.setText(Long.toString(node.id));
		btChangeset.setText(Long.toString(node.changesetId));
		btUser.setText(dc.users.get(dc.changesets.get(dc.changesetsIndex.get(node.changesetId)).userId).name);
		sDS.setSelection(node);		
	}
}
