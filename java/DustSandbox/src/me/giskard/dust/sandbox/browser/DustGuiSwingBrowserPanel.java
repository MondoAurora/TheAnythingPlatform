package me.giskard.dust.sandbox.browser;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

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
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.AbstractTableModel;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.utils.DustUtilsFactory;
import me.giskard.dust.mod.gui.swing.DustGuiSwingUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustGuiSwingBrowserPanel extends DustAgent implements DustGuiSwingBrowserConsts {

	class TableSelListener implements ListSelectionListener {
		JTable tbl;
		ArrayList<DustHandle> arr;

		public TableSelListener(ArrayList<DustHandle> arr, JTable tbl, int selMode) {
			this.tbl = tbl;
			this.arr = arr;
			ListSelectionModel lsm = tbl.getSelectionModel();
			lsm.setSelectionMode(selMode);
			lsm.addListSelectionListener(this);
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();

				selected.clear();

				for (int idx : lsm.getSelectedIndices()) {
					int ri = tbl.convertRowIndexToModel(idx);
					selected.add(arr.get(ri));
				}

				updatePropPanel();
				graphPanel.repaintGraph();
			}
		}
	};

	 class DustGuiSwingGraphPanel {
		double zoomFactor = 1.0;

		Collection<DustHandle> selected;
		Set<String> allLinks;
		DustUtilsFactory<DustHandle, DustHandle> factNodes;

		JPanel cmpGraph = new JPanel(null) {
			@Override
			protected void paintComponent(Graphics g) {
//				Graphics2D g2d = (Graphics2D) g;
//				AffineTransform at = g2d.getTransform();
				//
//				g2d.scale(zoomFactor, zoomFactor);

//				AffineTransform zoom = AffineTransform.getScaleInstance(zoomFactor, zoomFactor);
//				g2d.setTransform(zoom);

//				Dust.log(TOKEN_LEVEL_TRACE, "Paint with zoom factor", zoomFactor);


//				int i = 20;
				//
//				for (DustHandle h : graphs.get(selGraph).values()) {
//					g2d.drawString(h.getId(), i, i);
				//
//					i += 20;
//				}

//				super.paintComponent(g2d);

//				g2d.dispose();
//				g2d.setTransform(at);
			}

			@Override
			protected void paintChildren(Graphics g) {
//				super.paintChildren(g);
				Graphics2D g2d = (Graphics2D) g;

				AffineTransform at = g2d.getTransform();

				g2d.scale(zoomFactor, zoomFactor);

//				Dust.log(TOKEN_LEVEL_TRACE, "Paint with at", at);

				super.paintChildren(g);

				for (DustHandle h : factNodes.keys()) {
					JComponent hc = comps.get(factNodes.get(h));
					Rectangle rct = hc.getBounds(null);
					Point pt1 = new Point((int) rct.getCenterX(), (int) rct.getCenterY());
					
					if ( selected.contains(h)) {
						g2d.drawRoundRect(rct.x - 5, rct.y - 5, rct.width + 10, rct.height + 10 , 4, 4);
					}

					for (String l : allLinks) {
						Object val = Dust.access(DustAccess.Peek, null, h, l);

						if (val instanceof Map) {
							val = ((Map) val).values();
						}
						if (val instanceof Collection) {
							for (Object lt : (Collection) val) {
								optDrawLine(lt, g2d, rct, pt1);
							}
						}
						if (val instanceof DustHandle) {
							optDrawLine(val, g2d, rct, pt1);
						}
					}
				}

//				int i = 20;
				//
//				for (DustHandle h : graphs.get(selGraph).values()) {
//					g2d.drawString(h.getId(), i, i);
				//
//					i += 20;
//				}
				g2d.setTransform(at);
			}

			public void optDrawLine(Object target, Graphics2D g2d, Rectangle rct, Point pt1) {
				DustHandle ht = factNodes.peek((DustHandle) target);
				JComponent tc = comps.peek(ht);

				if (null != tc) {
					tc.getBounds(rct);
					g2d.drawLine(pt1.x, pt1.y, (int) rct.getCenterX(), (int) rct.getCenterY());
				}
			}
		};
		JScrollPane scpGraph = new JScrollPane(cmpGraph);
		
		Point anchor = null;

		MouseInputAdapter ma = new MouseInputAdapter() {
			
			Point getModelPoint(Point pt) {
				int x = (int) ((double) pt.x / zoomFactor);
				int y = (int) ((double) pt.y / zoomFactor);

				return new Point(x, y);
			}
			
			public void mousePressed(java.awt.event.MouseEvent e) {
				anchor = getModelPoint(e.getPoint());
			};
			
			public void mouseDragged(java.awt.event.MouseEvent e) {};
			
			public void mouseReleased(java.awt.event.MouseEvent e) {
				Point pt = getModelPoint(e.getPoint());
				
				int dx = pt.x - anchor.x;
				int dy = pt.y - anchor.y;
				anchor = null;

				for ( DustHandle h : selected ) {					
					DustHandle hn = factNodes.peek(h);
					JComponent c = comps.peek(hn);
					if (null != c) {
						pt = c.getLocation(pt);
						c.setLocation(pt.x + dx, pt.y + dy);
					}
				}
				
				repaintGraph();
			};
			
			public void mouseMoved(java.awt.event.MouseEvent e) {
				DustHandle h = findHandle(e);
				
				if ( null != h ) {
//					Dust.log(TOKEN_LEVEL_TRACE, "Over", h);				
				}
			}
			
			public DustHandle findHandle(java.awt.event.MouseEvent e) {
				Point pt = getModelPoint( e.getPoint());
				
				Rectangle rct = null;
				for ( DustHandle h : comps.keys() ) {
					JComponent c = comps.get(h);
					rct = c.getBounds(rct);
					
					if ( rct.contains(pt)) {
						h = Dust.access(DustAccess.Peek, null, h, TOKEN_TARGET);
						return h;		
					}
				}
				
				return null;
			};
			
			public void mouseClicked(java.awt.event.MouseEvent e) {
				DustHandle h = findHandle(e);
				
				if ( null != h ) {
					if ( selected.contains(h) ) {
						selected.remove(h);
					} else {
						selected.add(h);
					}
					
					repaintGraph();
					updatePropPanel();
				}
			};
		};

		public DustGuiSwingGraphPanel(Set<String> allLinks, Collection<DustHandle> selected) {
			this.allLinks = allLinks;
			this.selected = selected;

			cmpGraph.addMouseListener(ma);
			cmpGraph.addMouseMotionListener(ma);
			cmpGraph.addMouseWheelListener(ma);

			cmpGraph.setPreferredSize(new Dimension(2000, 1000));
		}

		public void setFactNodes(DustUtilsFactory<DustHandle, DustHandle> factNodes) {
			this.factNodes = factNodes;
		}

		int i = 20;

		DustUtilsFactory<DustHandle, JComponent> comps = new DustUtilsFactory(new DustCreator<JComponent>() {
			@Override
			public JComponent create(Object key, Object... hints) {
				Object txt = Dust.access(DustAccess.Peek, "??", key, TOKEN_TARGET, TOKEN_ID);
				JLabel lbl = new JLabel((String) txt);
				Dimension d = lbl.getPreferredSize();
				lbl.setBounds(i, i, d.width, d.height);
				i += 20;
				return lbl;
			}
		}, false);

		public void changeZoomFactor(Double d) {
			if (null == d) {
				zoomFactor = 1.0;
			} else {
				zoomFactor *= d;
			}
			
			repaintGraph();
		}

		public void repaintGraph() {
			cmpGraph.removeAll();
			for (DustHandle h : factNodes.keys()) {
				JComponent hc = comps.get(factNodes.get(h));
				cmpGraph.add(hc);
			}

			cmpGraph.invalidate();
			scpGraph.repaint();
		}

		public void randomize() {
			Random rnd = new Random();
			
			Rectangle rct = scpGraph.getViewport().getBounds(null);
			int w = rct.width;
			int h = rct.height;
			
			for ( DustHandle hh : comps.keys() ) {
				JComponent hc = comps.peek(hh);
				rct = hc.getBounds(rct);
				int x = rnd.nextInt(w - rct.width);
				int y = rnd.nextInt(h - rct.height);
				
				hc.setLocation(x, y);
			}

			repaintGraph();
			
		};

	};

	DustHandle hMindAPI;

	DustHandle hDocUnit = Dust.getUnit("graphTest1", true);

	ArrayList<DustHandle> unitArr = new ArrayList<>();
	ArrayList<DustHandle> gridArr = new ArrayList<>();
	ArrayList<String> gridCols = new ArrayList<>();

	Set<DustHandle> filterUnit = new HashSet<>();
	Set<String> allLinks = new HashSet<>();
	Set<DustHandle> selected = new HashSet<>();
	Map<String, DustCollType> attTypes = new TreeMap();

	String filterExpr;
	DustUtilsFactory<String, String> filters = new DustUtilsFactory.Simple(true, String.class);

	String selGraph = "";
	DustCreator<DustHandle> graphNodeCreator = new DustCreator<DustHandle>() {
		@Override
		public DustHandle create(Object key, Object... hints) {
			DustHandle hNode = Dust.getHandle(hDocUnit, TOKEN_GEOMETRY_NODE, null, DustOptCreate.Primary);

			Dust.access(DustAccess.Set, key, hNode, TOKEN_TARGET);

			// set location

			return hNode;
		}
	};

	DustUtilsFactory<String, DustUtilsFactory<DustHandle, DustHandle>> graphs = new DustUtilsFactory(new DustCreator<DustUtilsFactory<DustHandle, DustHandle>>() {
		@Override
		public DustUtilsFactory<DustHandle, DustHandle> create(Object key, Object... hints) {
			return new DustUtilsFactory<>(graphNodeCreator, false);
		}
	}, true);

	JFrame frm;

	JTextField tfHandle = new JTextField();
	JComboBox<String> cbFilter = new JComboBox<String>();
	JComboBox<String> cbGraph = new JComboBox<String>();
	JTextArea propEditor = new JTextArea();

	DustGuiSwingGraphPanel graphPanel = new DustGuiSwingGraphPanel(allLinks, selected);

	private static final String[] unitCols = { "Filter", "Type", "Identifier", "count" };
	private static final Class[] unitColTypes = { Boolean.class, String.class, String.class, Integer.class };

	AbstractTableModel unitTblModel = new AbstractTableModel() {
		@Override
		public int getColumnCount() {
			return unitCols.length;
		}

		public String getColumnName(int column) {
			return unitCols[column];
		};

		public Class<?> getColumnClass(int columnIndex) {
			return unitColTypes[columnIndex];
		};

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return 0 == columnIndex;
		};

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (Boolean.TRUE.equals(aValue)) {
				filterUnit.add(unitArr.get(rowIndex));
			} else {
				filterUnit.remove(unitArr.get(rowIndex));
			}

			refillGrid();
		};

		@Override
		public int getRowCount() {
			return unitArr.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			DustHandle hs = unitArr.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return filterUnit.contains(hs);
			case 1:
				return hs.getType().getId();
			case 2:
				return hs.getId();
			case 3:
				return Dust.access(DustAccess.Peek, -1, hs, TOKEN_UNIT_REFS, KEY_SIZE);
			}

			return "???";
		}
	};

	AbstractTableModel gridTblModel = new AbstractTableModel() {

		@Override
		public int getColumnCount() {
			return gridCols.size() + 1;
		}

		public String getColumnName(int column) {
			return (--column) < 0 ? "Show" : gridCols.get(column);
		};

		public Class<?> getColumnClass(int columnIndex) {
			return (0 == columnIndex) ? Boolean.class : Object.class;
		};

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return 0 == columnIndex;
		};

		@Override
		public int getRowCount() {
			return gridArr.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int column) {
			DustHandle hs = gridArr.get(rowIndex);
			return (--column) < 0 ? (null != graphs.get(selGraph).peek(hs)) : Dust.access(DustAccess.Peek, null, hs, gridCols.get(column));
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			DustHandle hs = gridArr.get(rowIndex);
			DustUtilsFactory<DustHandle, DustHandle> gf = graphs.get(selGraph);

			if (Boolean.TRUE.equals(aValue)) {
				gf.get(hs);
			} else {
				gf.remove(hs);
			}
			graphPanel.repaintGraph();
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

	void refillGrid() {
		boolean fu = !filterUnit.isEmpty();

		gridArr.clear();
		gridCols.clear();
		attTypes.clear();
		allLinks.clear();

		for (DustHandle hu : unitArr) {
			if (fu && !filterUnit.contains(hu)) {
				continue;
			}

			Map<String, DustHandle> members = Dust.access(DustAccess.Peek, -1, hu, TOKEN_UNIT_REFS);
			for (DustHandle h : members.values()) {
				gridArr.add(h);

				Collection<String> atts = Dust.access(DustAccess.Peek, Collections.EMPTY_SET, h, KEY_MAP_KEYS);

				for (String a : atts) {
					DustCollType ct = DustCollType.One;
					Object val = Dust.access(DustAccess.Peek, null, h, a);
					if (val instanceof Map) {
						val = ((Map) val).values();
						ct = DustCollType.Map;
					}
					if (val instanceof Collection) {
						ct = (val instanceof Set) ? DustCollType.Set : DustCollType.Arr;
						Collection c = (Collection) val;
						val = c.isEmpty() ? null : c.iterator().next();
					}

					if (val instanceof DustHandle) {
						allLinks.add(a);
					}

					attTypes.put(a, ct);
				}
			}
		}

		gridCols.addAll(attTypes.keySet());
		gridCols.sort(null);
		gridTblModel.fireTableStructureChanged();
	}

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
			for (DustHandle hu : units) {
				if (!unitArr.contains(hu)) {
					refresh = true;
					unitArr.add(hu);
				}
			}

			if (refresh) {
				unitTblModel.fireTableDataChanged();
				refillGrid();
			}

			break;

		case "+":
			graphPanel.changeZoomFactor(1.25);
			break;

		case ".":
			graphPanel.changeZoomFactor(null);
			break;

		case "-":
			graphPanel.changeZoomFactor(0.8);
			break;

		case "Random":
			graphPanel.randomize();
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
		factToolbars.fillToolbar("tbGraph", cbGraph, null, "+", ".", "-", "Random", "Align right");
		factToolbars.fillToolbar("tbGrid", "Whatever", "Also");
		factToolbars.fillToolbar("tbFilter", "Search", null, cbFilter, "Save filter", "Load filter", "Drop filter");

		Container cp = frm.getContentPane();
		cp.removeAll();

		JPanel pnlMain = new JPanel(new BorderLayout());
		pnlMain.add(factToolbars.get("tbTop"), BorderLayout.NORTH);

		propEditor.setEditable(false);

		JTable unitTable = new JTable(unitTblModel);
		new TableSelListener(unitArr, unitTable, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		unitTable.setAutoCreateRowSorter(true);
//		ListSelectionModel lsm = unitTable.getSelectionModel();
//		lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		lsm.addListSelectionListener(lslSelector);

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

		JTable handleTable = new JTable(gridTblModel);
		new TableSelListener(gridArr, handleTable, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

//		lsm = handleTable.getSelectionModel();
//		lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		lsm.addListSelectionListener(lslSelector);

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

		graphPanel.setFactNodes(graphs.get(selGraph));

		JPanel pnlGraph = new JPanel(new BorderLayout());
		DustGuiSwingUtils.setTitle(pnlGraph, "Handle Graph");
		pnlGraph.add(graphPanel.scpGraph, BorderLayout.CENTER);
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

	public void updatePropPanel() {
		propEditor.setText(selected.toString());
	}

}
