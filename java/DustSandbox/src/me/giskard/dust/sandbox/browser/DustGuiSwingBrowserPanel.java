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
import javax.swing.JOptionPane;
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
import javax.swing.table.DefaultTableModel;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.dev.DustDevUtils;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.utils.DustUtils;
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
		Set<String> showLinks;
		DustUtilsFactory<DustHandle, DustHandle> factNodes;

		JPanel cmpGraph = new JPanel(null) {

			@Override
			protected void paintChildren(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;

				AffineTransform at = g2d.getTransform();

				g2d.scale(zoomFactor, zoomFactor);

				for (DustHandle h : factNodes.keys()) {
					JComponent hc = comps.get(factNodes.get(h));
					Rectangle rct = hc.getBounds(null);
					Point pt1 = new Point((int) rct.getCenterX(), (int) rct.getCenterY());

					if (selected.contains(h)) {
						int sx = rct.x - 5;
						int sy = rct.y - 5;
						if (null != anchor) {
							sx += dx;
							sy += dy;
						}
						g2d.drawRoundRect(sx, sy, rct.width + 10, rct.height + 10, 4, 4);
					}

					for (String l : showLinks) {
						Object val = Dust.access(DustAccess.Peek, null, h, l);

						if (val instanceof Map) {
							val = ((Map) val).values();
						}
						if (val instanceof Collection) {
							for (Object lt : (Collection) val) {
								optDrawLine(h, l, lt, g2d, rct, pt1);
							}
						}
						if (val instanceof DustHandle) {
							optDrawLine(h, l, val, g2d, rct, pt1);
						}
					}
				}

				super.paintChildren(g);

				g2d.setTransform(at);
			}

			public void optDrawLine(DustHandle hSrc, String l, Object target, Graphics2D g2d, Rectangle rct, Point from) {
				DustHandle ht = factNodes.peek((DustHandle) target);
				JComponent tc = comps.peek(ht);

				if (null != tc) {
					String lbl = attTypes.get(l).name() + " " + l;

					if (hSrc == target) {
						g2d.drawOval(from.x - 15, from.y - 50, 30, 50);
						g2d.drawString(lbl, from.x, from.y - 50);
					} else {
						tc.getBounds(rct);
						int toX = (int) rct.getCenterX();
						int toY = (int) rct.getCenterY();
						g2d.drawLine(from.x, from.y, toX, toY);

						double d = 50.0 / from.distance(toX, toY);
						int px = from.x + (int) (d * (toX - from.x));
						int py = from.y + (int) (d * (toY - from.y));

						g2d.drawString(lbl, px, py);
					}
				}
			}
		};

		JScrollPane scpGraph = new JScrollPane(cmpGraph);

		Point anchor = null;
		int dx = 0;
		int dy = 0;

		String[] GRAPH_MODES = { "Select", "Remove Link", "Create Link" };
		JComboBox<String> cbMode = new JComboBox<String>(GRAPH_MODES);

		MouseInputAdapter ma = new MouseInputAdapter() {

			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				DustHandle h = findHandle(e);
//				int mod = e.getModifiersEx();

				if (null != h) {

					switch ((String) cbMode.getSelectedItem()) {
					case "Select":
						if (selected.contains(h)) {
							selected.remove(h);
						} else {
							if (!e.isShiftDown()) {
								selected.clear();
							}

							selected.add(h);
						}
						break;

					case "Remove Link":
						for (String l : showLinks) {
							for (DustHandle hs : selected) {
								Dust.access(DustAccess.Delete, h, hs, l);
							}
						}

						break;

					case "Create Link":
						if (1 != showLinks.size()) {
							JOptionPane.showMessageDialog(cmpGraph, "Only works with one selected link", "Create link error", JOptionPane.ERROR_MESSAGE);
							break;
						}
						String l = showLinks.iterator().next();
						DustCollType ct = attTypes.get(l);
						String key = null;
						if (ct == DustCollType.Map) {
							key = JOptionPane.showInputDialog(cmpGraph, "Key?", "Create Map link", JOptionPane.QUESTION_MESSAGE);
							if (DustUtils.isEmpty(key)) {
								break;
							}
						}
						for (DustHandle hs : selected) {
							switch (ct) {
							case Arr:
								Dust.access(DustAccess.Insert, h, hs, l, KEY_ADD);
								break;
							case Map:
								Dust.access(DustAccess.Insert, h, hs, l, key);
								break;
							case One:
								Dust.access(DustAccess.Set, h, hs, l);
								break;
							case Set:
								Dust.access(DustAccess.Insert, h, hs, l);
								break;
							}
						}

						break;
					}

					if (!e.isShiftDown()) {
						cbMode.setSelectedIndex(0);
					}
					repaintGraph();
					updatePropPanel();
				}
			};

			Point getModelPoint(Point pt) {
				int x = (int) ((double) pt.x / zoomFactor);
				int y = (int) ((double) pt.y / zoomFactor);

				return new Point(x, y);
			}

			public DustHandle findHandle(java.awt.event.MouseEvent e) {
				Point pt = getModelPoint(e.getPoint());

				Rectangle rct = null;
				for (DustHandle h : comps.keys()) {
					JComponent c = comps.get(h);
					rct = c.getBounds(rct);

					if (rct.contains(pt)) {
						h = Dust.access(DustAccess.Peek, null, h, TOKEN_TARGET);
						return h;
					}
				}

				return null;
			};

			@Override
			public void mousePressed(java.awt.event.MouseEvent e) {
				anchor = getModelPoint(e.getPoint());
			};

			@Override
			public void mouseDragged(java.awt.event.MouseEvent e) {
				Point pt = getModelPoint(e.getPoint());

				dx = pt.x - anchor.x;
				dy = pt.y - anchor.y;

				repaintGraph();
			};

			@Override
			public void mouseReleased(java.awt.event.MouseEvent e) {
				Point pt = getModelPoint(e.getPoint());

				int dx = pt.x - anchor.x;
				int dy = pt.y - anchor.y;
				anchor = null;

				for (DustHandle h : selected) {
					DustHandle hn = factNodes.peek(h);
					JComponent c = comps.peek(hn);
					if (null != c) {
						pt = c.getLocation(pt);
						c.setLocation(pt.x + dx, pt.y + dy);
					}
				}

				repaintGraph();
			};

			@Override
			public void mouseMoved(java.awt.event.MouseEvent e) {
//				DustHandle h = findHandle(e);
			}

		};

		public DustGuiSwingGraphPanel(Set<String> allLinks, Collection<DustHandle> selected) {
			this.showLinks = allLinks;
			this.selected = selected;

			cmpGraph.addMouseListener(ma);
			cmpGraph.addMouseMotionListener(ma);
			cmpGraph.addMouseWheelListener(ma);

			cmpGraph.setPreferredSize(new Dimension(2000, 1000));

			cbMode.setSelectedIndex(0);
		}

		public void setFactNodes(DustUtilsFactory<DustHandle, DustHandle> factNodes) {
			this.factNodes = factNodes;
		}

		int i = 20;

		DustUtilsFactory<DustHandle, JComponent> comps = new DustUtilsFactory(new DustCreator<JComponent>() {
			@Override
			public JComponent create(Object key, Object... hints) {
				DustHandle ht = Dust.access(DustAccess.Peek, null, key, TOKEN_TARGET);
				String txt = (null == ht) ? "??"
						: new StringBuilder("<html><center>").append(ht.toString().replace(" [", "<br/>[")).append("</center></html>").toString();
				JLabel lbl = new JLabel(txt);
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

			for (DustHandle hh : comps.keys()) {
				JComponent hc = comps.peek(hh);
				rct = hc.getBounds(rct);
				int x = rnd.nextInt(w - rct.width);
				int y = rnd.nextInt(h - rct.height);

				hc.setLocation(x, y);
			}

			repaintGraph();

		}

		public void showHandle(DustHandle hs, boolean show) {
			showHandle(hs, show, true);
		}

		public void showHandle(DustHandle hs, boolean show, boolean repaint) {
			DustUtilsFactory<DustHandle, DustHandle> gf = graphs.get(selGraph);

			if (show) {
				gf.get(hs);
			} else {
				DustHandle hn = gf.remove(hs);
				comps.remove(hn);
			}
			if (repaint) {
				repaintGraph();
			}
		};

	};

	DustHandle hMindAPI;
	Collection<String> tokenClasses;

	DustHandle hDocUnit = Dust.getUnit("graphTest1", true);

	ArrayList<DustHandle> unitArr = new ArrayList<>();
	ArrayList<DustHandle> gridArr = new ArrayList<>();
	ArrayList<String> gridCols = new ArrayList<>();

	Set<DustHandle> selected = new HashSet<>();

	Map<String, DustCollType> attTypes = new TreeMap();
	Set<String> allLinks = new HashSet<>();
	Set<String> allAtts = new HashSet<>();

	Set<String> showLinks = new HashSet<>();

	Set<DustHandle> filterUnit = new HashSet<>();
	String filterExpr;
	DustUtilsFactory<String, String> filters = new DustUtilsFactory.Simple(true, String.class);

	String selGraph = "";
	Map<DustHandle, DustHandle> nodePool = new HashMap<>();

	DustCreator<DustHandle> graphNodeCreator = new DustCreator<DustHandle>() {
		@Override
		public DustHandle create(Object key, Object... hints) {
			DustHandle hNode = nodePool.remove(key);

			if (null == hNode) {
				hNode = Dust.getHandle(hDocUnit, TOKEN_GEOMETRY_NODE, null, DustOptCreate.Primary);
				Dust.access(DustAccess.Set, key, hNode, TOKEN_TARGET);

				// set location
			}

			return hNode;
		}
	};

	DustUtilsFactory<String, DustUtilsFactory<DustHandle, DustHandle>> graphs = new DustUtilsFactory(new DustCreator<DustUtilsFactory<DustHandle, DustHandle>>() {
		@Override
		public DustUtilsFactory<DustHandle, DustHandle> create(Object key, Object... hints) {
			return new DustUtilsFactory<>(graphNodeCreator, false) {
				public DustHandle remove(DustHandle key) {
					DustHandle ret = super.remove(key);

					if (null != ret) {
						nodePool.put(key, ret);
					}

					return ret;
				};
			};
		}
	}, true);

	JFrame frm;

	JTextField tfHandle = new JTextField();
	JComboBox<String> cbFilter = new JComboBox<String>();
	JComboBox<String> cbGraph = new JComboBox<String>();

	DustGuiSwingGraphPanel graphPanel = new DustGuiSwingGraphPanel(showLinks, selected);

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

			graphPanel.showHandle(hs, Boolean.TRUE.equals(aValue));

		}
	};

	private static String[] ATT_COLS = new String[] { "Name" };

	DefaultTableModel tblmAtts = new DefaultTableModel(ATT_COLS, 0);
	DefaultTableModel tblmLinks = new DefaultTableModel(ATT_COLS, 0);

	JComboBox<DustHandle> cbData = new JComboBox<DustHandle>();

	DustHandle focused = null;
	ArrayList<String> focusedCols = new ArrayList<>();
	String focusedAtt;
	DustCollType focusedCollType = DustCollType.One;
	ArrayList<Object> focusedColData = new ArrayList<>();

	private static String[] DATA_COLS = new String[] { "Name", "Type", "Coll", "Value" };
	AbstractTableModel tblmData = new AbstractTableModel() {
		@Override
		public int getColumnCount() {
			return DATA_COLS.length;
		}

		public String getColumnName(int column) {
			return DATA_COLS[column];
		};

		public java.lang.Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return String.class;
			case 1:
				return DustValType.class;
			case 2:
				return DustCollType.class;
			case 3:
				return String.class;
			}

			return Object.class;
		};

		@Override
		public int getRowCount() {
			return focusedCols.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			String a = focusedCols.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return a;
			case 1:
				return "?";
			case 2:
				return attTypes.get(a);
			case 3:
				Object v = Dust.access(DustAccess.Peek, null, focused, a);
				return DustUtils.toString(v);
			}

			return null;
		}
	};

	private static String[] COLL_COLS = new String[] { "Key", "Value" };

	AbstractTableModel tblmColl = new AbstractTableModel() {
		@Override
		public int getColumnCount() {
			return COLL_COLS.length;
		}

		public String getColumnName(int column) {
			return COLL_COLS[column];
		};

		public java.lang.Class<?> getColumnClass(int columnIndex) {
			return String.class;
		};

		@Override
		public int getRowCount() {
			return focusedColData.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object v = focusedColData.get(rowIndex);
			switch (columnIndex) {
			case 0:
				switch (focusedCollType) {
				case Arr:
					return rowIndex;
				case Map:
					return v;
				case One:
					return "-";
				case Set:
					return "-";
				}
			case 1:
				switch (focusedCollType) {
				case Arr:
					return v;
				case Map:
					return Dust.access(DustAccess.Peek, null, focused, focusedAtt, v);
				case One:
					return v;
				case Set:
					return v;
				}
			}

			return null;
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

		tblmAtts.setRowCount(0);
		tblmLinks.setRowCount(0);

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

					if (null == attTypes.put(a, ct)) {
						if (val instanceof DustHandle) {
							allLinks.add(a);
							tblmLinks.addRow(new Object[] { a });
						} else {
							tblmAtts.addRow(new Object[] { a });
							gridCols.add(a);
						}
					}
				}
			}
		}

		gridCols.sort(null);
		gridTblModel.fireTableStructureChanged();

		tblmAtts.fireTableDataChanged();
		tblmLinks.fireTableDataChanged();
	}

	@Override
	protected void init() throws Exception {

		hMindAPI = Dust.access(DustAccess.Peek, null, null, TOKEN_TARGET);

		tokenClasses = Dust.access(DustAccess.Peek, Collections.EMPTY_SET, null, TOKEN_DEV_CLASSES);

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

		try {
			switch (cmd) {
			case "Rebuild":
				buildGui();
				break;

			case "Load Tokens":
				DustDevUtils.loadConstHandles(tokenClasses);
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

			case "Load Refs":
				for (DustHandle s : selected) {
					for (String l : showLinks) {
						DustUtils.visit(Dust.access(DustAccess.Peek, null, s, l), new DustProcessor<DustHandle, Object>() {
							@Override
							public Object process(DustHandle handle, Object... hints) {
								graphPanel.showHandle(handle, true, false);
								return null;
							}
						});
					}
				}

				graphPanel.repaintGraph();
				break;

			case "Drop Selected":
				for (DustHandle s : selected) {
					graphPanel.showHandle(s, false, false);
				}
				graphPanel.repaintGraph();
				break;
			case "Select Handle":
				focused = (DustHandle) cbData.getSelectedItem();
				focusedCols.clear();

				if (null != focused) {
					Collection<String> atts = Dust.access(DustAccess.Peek, Collections.EMPTY_SET, focused, KEY_MAP_KEYS);
					focusedCols.addAll(atts);
				}

				tblmData.fireTableDataChanged();
				break;
			default:
				Dust.log(TOKEN_LEVEL_WARNING, "execCmd() Command not handled", cmd);
			}
		} catch (Throwable t) {
			DustException.wrap(t);
		}
	}

	void buildGui() {
		tfHandle.setText("5ElemCsikung.1$000a6c1e");

		Dimension dimMin = new Dimension(150, 150);
		ListSelectionModel lsm;

		cbFilter.setEditable(true);
		cbGraph.setEditable(true);

		factToolbars.fillToolbar("tbTop", "Rebuild", "Load Tokens", null, new JLabel("Handle ID:"), tfHandle, "Load Handle", null, "Rollback", "Commit"
//				, null, new JLabel("Graph"), tfGraph, "Load Graph", "Merge"
		);
		factToolbars.fillToolbar("tbUnit", "Update Units", "Drop Unit", null, "New handle");
		factToolbars.fillToolbar("tbProp", "Update Handle", "Reload Handle");
		factToolbars.fillToolbar("tbGraph", cbGraph, null, "+", ".", "-", "Random", "Load Refs", "Drop Selected", graphPanel.cbMode);
		factToolbars.fillToolbar("tbGrid", "Whatever", "Also");
		factToolbars.fillToolbar("tbFilter", "Search", null, cbFilter, "Save filter", "Load filter", "Drop filter");

		Container cp = frm.getContentPane();
		cp.removeAll();

		JPanel pnlMain = new JPanel(new BorderLayout());
		pnlMain.add(factToolbars.get("tbTop"), BorderLayout.NORTH);

		JTable unitTable = new JTable(unitTblModel);
		new TableSelListener(unitArr, unitTable, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		unitTable.setAutoCreateRowSorter(true);

		JPanel pnlUnit = new JPanel(new BorderLayout());
		DustGuiSwingUtils.setTitle(pnlUnit, "Units");
		JScrollPane scpUnit = new JScrollPane(unitTable);
		scpUnit.setMinimumSize(dimMin);
		pnlUnit.add(scpUnit, BorderLayout.CENTER);
		pnlUnit.add(factToolbars.get("tbUnit"), BorderLayout.SOUTH);

		JPanel pnlProp = new JPanel(new BorderLayout());
		DustGuiSwingUtils.setTitle(pnlProp, "Properties");
		pnlProp.add(cbData, BorderLayout.NORTH);

		cbData.setActionCommand("Select Handle");
		cbData.addActionListener(al);

		JTable tblData = new JTable(tblmData);
		JTable tblColl = new JTable(tblmColl);

		lsm = tblData.getSelectionModel();
		lsm.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int ai = ((ListSelectionModel) e.getSource()).getLeadSelectionIndex();
					focusedColData.clear();

					if (-1 != ai) {
						focusedAtt = focusedCols.get(ai);
						focusedCollType = attTypes.get(focusedAtt);

						Object data = Dust.access(DustAccess.Peek, null, focused, focusedAtt);
						if (data instanceof Map) {
							data = ((Map) data).keySet();
							focusedCollType = DustCollType.Map;
						} else if (data instanceof Collection) {
							focusedCollType = (data instanceof Set) ? DustCollType.Set : DustCollType.Arr;
						} else {
							focusedCollType = DustCollType.One;
						}

						DustUtils.visit(data, new DustProcessor<Object, Object>() {
							@Override
							public Object process(Object value, Object... hints) {
								focusedColData.add(value);
								return null;
							}
						});
					}

					tblmColl.fireTableDataChanged();
				}
			}
		});

		JScrollPane scpData = new JScrollPane(tblData);
		scpData.setMinimumSize(dimMin);
		JScrollPane scpColl = new JScrollPane(tblColl);
		scpColl.setMinimumSize(dimMin);
		pnlProp.add(DustGuiSwingUtils.createSplit(false, scpData, scpColl, 1.0), BorderLayout.CENTER);
		pnlProp.add(factToolbars.get("tbProp"), BorderLayout.SOUTH);

		JPanel pnlLeft = new JPanel(new BorderLayout());
		pnlLeft.add(DustGuiSwingUtils.createSplit(false, pnlUnit, pnlProp, 0.0), BorderLayout.CENTER);

		JTable handleTable = new JTable(gridTblModel);
		new TableSelListener(gridArr, handleTable, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

//		lsm = handleTable.getSelectionModel();
//		lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		lsm.addListSelectionListener(lslSelector);
		JScrollPane scpTbl;

		JPanel pnlGrid = new JPanel(new BorderLayout());
		DustGuiSwingUtils.setTitle(pnlGrid, "Handle Grid");
		JPanel pnlFilter = new JPanel(new BorderLayout());
		DustGuiSwingUtils.setTitle(pnlFilter, "Filter");
		JTextArea taFilter = new JTextArea();
		taFilter.setRows(3);
		pnlFilter.add(new JScrollPane(taFilter), BorderLayout.CENTER);
		pnlFilter.add(factToolbars.get("tbFilter"), BorderLayout.SOUTH);

		pnlGrid.add(pnlFilter, BorderLayout.NORTH);
		pnlGrid.add(factToolbars.get("tbGrid"), BorderLayout.SOUTH);

		JTable tblAtts = new JTable(tblmAtts);
		scpTbl = new JScrollPane(tblAtts);
		scpTbl.setMinimumSize(dimMin);

		pnlGrid.add(DustGuiSwingUtils.createSplit(true, scpTbl, new JScrollPane(handleTable), 0.0), BorderLayout.CENTER);

		graphPanel.setFactNodes(graphs.get(selGraph));

		JPanel pnlGraph = new JPanel(new BorderLayout());
		DustGuiSwingUtils.setTitle(pnlGraph, "Handle Graph");
		pnlGraph.add(factToolbars.get("tbGraph"), BorderLayout.NORTH);
		JTable tblLinks = new JTable(tblmLinks);
		scpTbl = new JScrollPane(tblLinks);
		scpTbl.setMinimumSize(dimMin);

		lsm = tblLinks.getSelectionModel();
		lsm.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					ListSelectionModel lsm = (ListSelectionModel) e.getSource();

					showLinks.clear();

					for (int idx : lsm.getSelectedIndices()) {
						showLinks.add((String) tblLinks.getValueAt(idx, 0));
					}
					graphPanel.repaintGraph();
				}
			}
		});

		pnlGraph.add(DustGuiSwingUtils.createSplit(true, scpTbl, graphPanel.scpGraph, 0.0), BorderLayout.CENTER);

		JPanel pnlRight = new JPanel(new BorderLayout());
		pnlRight.add(DustGuiSwingUtils.createSplit(false, pnlGrid, pnlGraph, 0.0), BorderLayout.CENTER);

		pnlMain.add(DustGuiSwingUtils.createSplit(true, pnlLeft, pnlRight, 0.0), BorderLayout.CENTER);

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
		cbData.removeAllItems();
		for (DustHandle s : selected) {
			cbData.addItem(s);
		}
	}

}
