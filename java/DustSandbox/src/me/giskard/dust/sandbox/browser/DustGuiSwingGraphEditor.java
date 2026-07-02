package me.giskard.dust.sandbox.browser;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.DustException;
import me.giskard.dust.mod.gui.swing.DustGuiSwingUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustGuiSwingGraphEditor extends DustAgent implements DustGuiSwingBrowserConsts {

	DustHandle hMindAPI;

	Set<DustHandle> unitSel = new HashSet<>();
	ArrayList<DustHandle> unitArr = new ArrayList<>();

	JFrame frm;

	JTextField tfHandle = new JTextField();
	JComboBox<String> cbFilter = new JComboBox<String>();
	JComboBox<String> cbGraph = new JComboBox<String>();

	JComponent cmpGraph = new JComponent() {
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
		}

		@Override
		protected void paintChildren(Graphics g) {
			// TODO Auto-generated method stub
			super.paintChildren(g);
		}
	};
	
	
	AbstractTableModel unitTblModel = new AbstractTableModel() {
		@Override
		public int getRowCount() {
			return unitArr.size();
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			DustHandle hs = unitArr.get(rowIndex);
			switch ( columnIndex ) {
			case 0:
				return hs;
			}
			return Dust.access(DustAccess.Peek, null, hs, TOKEN_PATH);
		}
	};


	ActionListener al = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();

			try {
				execCmd(cmd);
			} catch (Throwable ex) {
				DustException.swallow(ex, cmd);
			}
		}
	};

	DustGuiSwingUtils.ActionControlFactory factActionControls = new DustGuiSwingUtils.ActionControlFactory(al);
	DustGuiSwingUtils.ToolbarFactory factToolbars = new DustGuiSwingUtils.ToolbarFactory(factActionControls);

	@Override
	protected void init() throws Exception {

		hMindAPI = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET);

		DustGuiSwingUtils.optSetLookAndFeel();

		frm = new JFrame();

		frm.setTitle(Dust.access(DustAccess.Peek, "Text editor", null, TOKEN_NAME));

		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frm.setBounds(100, 100, 1000, 800);

		factToolbars.get("tbTop", BoxLayout.LINE_AXIS);
		factToolbars.get("tbUnit", BoxLayout.LINE_AXIS);
		factToolbars.get("tbProp", BoxLayout.LINE_AXIS);
		factToolbars.get("tbGraph", BoxLayout.LINE_AXIS);
		factToolbars.get("tbGrid", BoxLayout.LINE_AXIS);
		factToolbars.get("tbFilter", BoxLayout.LINE_AXIS);

		buildGui();

		frm.setVisible(true);

	};

	public void execCmd(String cmd) {
		Map params = new HashMap();

		boolean refresh = true;

		switch (cmd) {
		case "Rebuild":
			buildGui();
			break;

		case "Load Handle":
			Dust.access(DustAccess.Set, TOKEN_KBMETA_CMD_GETHANDLE, params, TOKEN_CMD);
			Dust.access(DustAccess.Set, tfHandle.getText(), params, TOKEN_GLOBALID);
			Dust.access(DustAccess.Process, params, hMindAPI);

			Object hRet = Dust.access(DustAccess.Peek, null, params, TOKEN_TARGET);

			if (null != hRet) {
				Dust.log(TOKEN_LEVEL_INFO, "MindAgent responded", hRet);
				execCmd("Update Units");
			}
			break;

		case "Update Units":
			Dust.access(DustAccess.Set, TOKEN_KBMETA_CMD_LISTUNITS, params, TOKEN_CMD);
			Dust.access(DustAccess.Process, params, hMindAPI);

			Collection<DustHandle> units = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, params, TOKEN_TARGET);
			for ( DustHandle hu : units ) {
				if ( !unitArr.contains(hu)) {
					refresh = true;
					unitArr.add(hu);
				}
			}
			
			if (refresh) {
				unitTblModel.fireTableDataChanged();
			}

			break;

		default:
			Dust.log(TOKEN_LEVEL_WARNING, "execCmd() Command not handled", cmd);
		}

	}

	void buildGui() {
		tfHandle.setText("5ElemCsikung.1$000a6c1e");
		cbFilter.setEditable(true);
		cbGraph.setEditable(true);

		factToolbars.fillToolbar("tbTop", "Rebuild", null, new JLabel("Handle ID:"), tfHandle, "Load Handle", null, "Rollback", "Commit"
//				, null, new JLabel("Graph"), tfGraph, "Load Graph", "Merge"
		);
		factToolbars.fillToolbar("tbUnit", "Update Units", "Drop Unit", null, "New handle");
		factToolbars.fillToolbar("tbProp", "Update Handle", "Reload Handle");
		factToolbars.fillToolbar("tbGraph", cbGraph, null, "Align left", "Align right");
		factToolbars.fillToolbar("tbGrid", "Whatever", "Also");
		factToolbars.fillToolbar("tbFilter", "Search", null, cbFilter, "Save filter", "Load filter", "Drop filter");

		Container cp = frm.getContentPane();
		cp.removeAll();

		JPanel pnlMain = new JPanel(new BorderLayout());
		pnlMain.add(factToolbars.get("tbTop"), BorderLayout.NORTH);

		
		JTextArea propEditor = new JTextArea();
		propEditor.setEditable(false);

		ListSelectionListener lslSelector = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Object o = e.getSource();
					
					Dust.log(TOKEN_LEVEL_TRACE, "grid selection", o);
//					int sr = ((JTable) e.getSource()).getSelectedRow();
//					if (-1 != sr) {
//					}
				}
			}
		};

		JTable unitTable = new JTable(unitTblModel);
		
		ListSelectionModel lsm = unitTable.getSelectionModel();
		lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		lsm.addListSelectionListener(lslSelector);

		JPanel pnlUnit = new JPanel(new BorderLayout());
		DustGuiSwingUtils.setTitle(pnlUnit, "Units");
		JScrollPane scpUnit = new JScrollPane(unitTable);
		pnlUnit.add(scpUnit, BorderLayout.CENTER);
		pnlUnit.add(factToolbars.get("tbUnit"), BorderLayout.SOUTH);

		JScrollPane scpProp = new JScrollPane(propEditor);
		JPanel pnlProp = new JPanel(new BorderLayout());
		DustGuiSwingUtils.setTitle(pnlProp, "Properties");
		pnlProp.add(scpProp, BorderLayout.CENTER);
		pnlProp.add(factToolbars.get("tbProp"), BorderLayout.SOUTH);

		JPanel pnlLeft = new JPanel(new BorderLayout());
		pnlLeft.add(DustGuiSwingUtils.createSplit(false, pnlUnit, pnlProp, 0.5), BorderLayout.CENTER);

		ArrayList<DustHandle> handleArr = new ArrayList<>();
		TableModel handleTblModel = new AbstractTableModel() {
			@Override
			public int getRowCount() {
				return handleArr.size();
			}

			@Override
			public int getColumnCount() {
				return 1;
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				DustHandle hs = handleArr.get(rowIndex);
				return Dust.access(DustAccess.Peek, null, hs, TOKEN_PATH);
			}
		};
		JTable handleTable = new JTable(handleTblModel);

		lsm = handleTable.getSelectionModel();
		lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		lsm.addListSelectionListener(lslSelector);

		JPanel pnlGrid = new JPanel(new BorderLayout());
		DustGuiSwingUtils.setTitle(pnlGrid, "Handle Grid");
		JPanel pnlFilter = new JPanel(new BorderLayout());
		DustGuiSwingUtils.setTitle(pnlFilter, "Filter");
		JTextArea taFilter = new JTextArea();
		taFilter.setRows(3);
		pnlFilter.add(new JScrollPane(taFilter), BorderLayout.CENTER);
		pnlFilter.add(factToolbars.get("tbFilter"), BorderLayout.SOUTH);

		pnlGrid.add(pnlFilter, BorderLayout.NORTH);
		pnlGrid.add(new JScrollPane(handleTable), BorderLayout.CENTER);
		pnlGrid.add(factToolbars.get("tbGrid"), BorderLayout.SOUTH);

		cmpGraph.setPreferredSize(new Dimension(2000, 1000));

		JPanel pnlGraph = new JPanel(new BorderLayout());
		DustGuiSwingUtils.setTitle(pnlGraph, "Handle Graph");
		pnlGraph.add(new JScrollPane(cmpGraph), BorderLayout.CENTER);
		pnlGraph.add(factToolbars.get("tbGraph"), BorderLayout.NORTH);

		JPanel pnlRight = new JPanel(new BorderLayout());
		pnlRight.add(DustGuiSwingUtils.createSplit(false, pnlGrid, pnlGraph, 0.5), BorderLayout.CENTER);

		pnlMain.add(DustGuiSwingUtils.createSplit(true, pnlLeft, pnlRight, 0.3), BorderLayout.CENTER);

//		factToolbars.fillToolbar("tbDoc", null, "<-", "->", null, "Delete", "UnderFirst", null, "Bullet", "Number", "Local", null, "Resp", "Table",
//				/* "Merge", */ null, "Style ->", "<- Style", "Apply", "Update", /* "New", "Drop", */ null, "ResInsert", null, "evtToNext", "evtToPrev", "evtSplit",
//				"evtMergeNext", "evtMergePrev", "evtTranslate", null, "Magic!", "GenHtml", Box.createGlue(), "Translate", "LoadTranslate", "ExtRes", "Export", null);

		cp.add(pnlMain);
		cp.revalidate();
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
