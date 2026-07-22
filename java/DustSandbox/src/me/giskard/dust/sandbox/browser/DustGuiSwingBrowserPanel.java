package me.giskard.dust.sandbox.browser;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.dev.DustDevUtils;
import me.giskard.dust.core.mind.DustMindUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.mod.gui.swing.DustGuiSwingUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustGuiSwingBrowserPanel extends DustAgent implements DustGuiSwingBrowserConsts {

	public DustCollType getAttType(String l) {
		return attTypes.get(l);
	}

	DustHandle hMindAPI;
	DustHandle hSrcGen;
	Collection<String> tokenClasses;

	DustHandle hDocUnit = Dust.getUnit("graphTest1", true);

	ArrayList<DustHandle> unitArr = new ArrayList<>();
	ArrayList<DustHandle> gridArr = new ArrayList<>();
	ArrayList<String> gridCols = new ArrayList<>();

	Set<String> showTypes = new HashSet<>();

	Set<DustHandle> selected = new HashSet<>();

	DustHandle focused = null;
	ArrayList<String> focusedAttributes = new ArrayList<>();
	int focusedIdx = -1;
	String focusedAtt;
	DustCollType focusedCollType = DustCollType.One;
	ArrayList<Object> focusedColData = new ArrayList<>();

	Map<String, DustCollType> attTypes = new TreeMap();

	Set<String> showLinks = new HashSet<>();

	Set<DustHandle> filterUnit = new HashSet<>();
	String filterStr;

	JFrame frm;

	JTextField tfHandle = new JTextField();
	JComboBox<String> cbGraph = new JComboBox<String>();
	JTextField tfFilter = new JTextField();

	DustGuiSwingGraphPanel graphPanel = new DustGuiSwingGraphPanel(this);

	private static final String[] unitCols = { "Filter", "Type", "Identifier", "count" };
	private static final Class[] unitColTypes = { Boolean.class, String.class, String.class, Integer.class };

	AbstractTableModel tblmUnits = new AbstractTableModel() {
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
				return Dust.access(DustAccess.Peek, -1, hs, TOKEN_DUST_ATT_UNIT_REFS, KEY_SIZE);
			}

			return "???";
		}
	};

	AbstractTableModel tblmGrid = new AbstractTableModel() {

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
			return (--column) < 0 ? (null != graphPanel.factNodes.peek(hs)) : Dust.access(DustAccess.Peek, null, hs, gridCols.get(column));
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			DustHandle hs = gridArr.get(rowIndex);

			graphPanel.showHandle(hs, Boolean.TRUE.equals(aValue));

		}
	};

	private static String[] ATT_COLS = new String[] { "Name" };

	DefaultTableModel tblmTypeFilter = new DefaultTableModel(ATT_COLS, 0) {
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	DefaultTableModel tblmAtts = new DefaultTableModel(ATT_COLS, 0) {
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	DefaultTableModel tblmLinks = new DefaultTableModel(ATT_COLS, 0) {
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	private static String[] PROPERTY_COLS = new String[] { "Name", "Type", "Coll", "Value" };
	AbstractTableModel tblmProperties = new AbstractTableModel() {
		@Override
		public int getColumnCount() {
			return PROPERTY_COLS.length;
		}

		public String getColumnName(int column) {
			return PROPERTY_COLS[column];
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
			return focusedAttributes.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			String a = focusedAttributes.get(rowIndex);
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

	JTable tblData = new JTable(tblmProperties);
	JTable tblColl = new JTable(tblmColl);
	JTextArea taValue = new JTextArea();

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

		hMindAPI = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_TARGET);
		hSrcGen = Dust.access(DustAccess.Peek, null, null, TOKEN_DEV_CMD_GENSRC);

		tokenClasses = Dust.access(DustAccess.Peek, Collections.EMPTY_SET, null, TOKEN_DEV_ATT_CLASSES);

		DustGuiSwingUtils.optSetLookAndFeel();

		frm = new JFrame();

		frm.setTitle(Dust.access(DustAccess.Peek, "Text editor", null, TOKEN_MISC_ATT_NAME));

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

		execCmd("Update Units");
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

			case "Commit":
				Dust.access(DustAccess.Set, TOKEN_MISC_TAG_CMD_SAVE, params, TOKEN_MIND_ATT_CMD);
				Dust.access(DustAccess.Process, params, hMindAPI);

				break;

			case "Load Handle":
				Dust.access(DustAccess.Set, TOKEN_MIND_CMD_GETHANDLE, params, TOKEN_MIND_ATT_CMD);
				Dust.access(DustAccess.Set, tfHandle.getText(), params, TOKEN_MISC_ATT_GLOBALID);
				Dust.access(DustAccess.Process, params, hMindAPI);

				Object hRet = Dust.access(DustAccess.Peek, null, params, TOKEN_MISC_ATT_TARGET);

				if (null != hRet) {
					Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, "MindAgent responded", hRet);
					execCmd("Update Units");
				}
				break;

			case "Update Units":
				Dust.access(DustAccess.Set, TOKEN_MIND_CMD_LISTUNITS, params, TOKEN_MIND_ATT_CMD);
				Dust.access(DustAccess.Process, params, hMindAPI);

				Collection<DustHandle> units = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, params, TOKEN_MISC_ATT_TARGET);
				for (DustHandle hu : units) {
					if (!unitArr.contains(hu)) {
						refresh = true;
						unitArr.add(hu);
					}
					Dust.getUnit(hu.getId(), true);
				}

				if (refresh) {
					tblmUnits.fireTableDataChanged();
					refillGrid();
				}

				break;

			case "Load Unit":
				String unitId = JOptionPane.showInputDialog(frm, "Unit ID?", "guiTest.1");

				if (!DustUtils.isEmpty(unitId)) {
					DustHandle hLoad = Dust.getUnit(unitId, true);
					if (!unitArr.contains(hLoad)) {
						unitArr.add(hLoad);
						tblmUnits.fireTableDataChanged();
						refillGrid();
					}
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

			case "Hide Selected":
			case "Drop Selected":
				for (DustHandle s : selected) {
					graphPanel.showHandle(s, false, false);
				}
				graphPanel.repaintGraph();
				break;
			case "Show Selected":
				for (DustHandle s : selected) {
					graphPanel.showHandle(s, true, false);
				}
				graphPanel.repaintGraph();
				break;
			case "Duplicate":
				for (DustHandle s : selected) {
					String id = JOptionPane.showInputDialog(frm, "New id?", s.getId());
					if (!DustUtils.isEmpty(id)) {
						DustHandle sc = Dust.getHandle(s.getUnit(), s.getType(), id, DustOptCreate.Primary);
						DustMindUtils.loadData(sc, s, false);
						Dust.access(DustAccess.Set, id, sc, TOKEN_MIND_ATT_ID); // quick fix
						graphPanel.showHandle(sc, true, false);
						selected.add(sc);
					}
				}
				tblmGrid.fireTableDataChanged();
				tblmUnits.fireTableDataChanged();
				graphPanel.repaintGraph();

				break;
			case "New handle":
				if (1 == selected.size()) {
					DustHandle hu = selected.iterator().next();
					boolean isUnit = Dust.access(DustAccess.Check, TOKEN_MIND_ASP_UNIT, hu, TOKEN_MIND_ATT_TYPE, TOKEN_MIND_ATT_ID);

					if (isUnit) {
						String id = JOptionPane.showInputDialog(frm, "New id?", "");
						if (!DustUtils.isEmpty(id)) {
//							DustHandle sc = Dust.getHandle(hu, s.getType(), id, DustOptCreate.Primary);
//							DustMindUtils.loadData(sc, s, false);
//							Dust.access(DustAccess.Set, id, sc, TOKEN_MIND_ATT_ID); // quick fix
//							graphPanel.showHandle(sc, true, false);
//							selected.add(sc);
							tblmGrid.fireTableDataChanged();
							tblmUnits.fireTableDataChanged();
							graphPanel.repaintGraph();
						}
					}
				}
				break;
			case "Delete Selected":
				Dust.access(DustAccess.Set, TOKEN_MISC_TAG_CMD_DELETE, params, TOKEN_MIND_ATT_CMD);
				Dust.access(DustAccess.Set, selected, params, TOKEN_MISC_ATT_MEMBERS);
				Dust.access(DustAccess.Process, params, hMindAPI);

				hRet = Dust.access(DustAccess.Peek, null, params, TOKEN_MISC_ATT_TARGET);

				if (null != hRet) {
					Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, "MindAgent responded", hRet);
					refillGrid();
					graphPanel.repaintGraph();
				}
				break;
			case "Drop Att":
				if ((null != focused) && (null != focusedAtt)) {
					Dust.access(DustAccess.Delete, null, focused, focusedAtt);
					tblmProperties.fireTableDataChanged();
				}
				break;
			case "Update Value":
				if (-1 != focusedIdx) {
					String strVal = taValue.getText();
					Object val = strVal;
					switch (focusedCollType) {
					case Arr:
						Dust.access(DustAccess.Set, val, focused, focusedAtt, focusedIdx);
						break;
					case Map:
						Object mk = tblmColl.getValueAt(focusedIdx, 0);
						Dust.access(DustAccess.Set, val, focused, focusedAtt, mk);
						break;
					case One:
						Dust.access(DustAccess.Set, val, focused, focusedAtt);
						break;
					case Set:
						Object ov = tblmColl.getValueAt(focusedIdx, 1);
						Dust.access(DustAccess.Delete, ov, focused, focusedAtt);
						Dust.access(DustAccess.Insert, val, focused, focusedAtt);
						break;
					}
					if (focusedCollType != DustCollType.Map) {
						focusedColData.set(focusedIdx, val);
					}
					ListSelectionModel lsm = tblData.getSelectionModel();
					int di = lsm.getLeadSelectionIndex();
					tblmProperties.fireTableCellUpdated(di, 1);
					tblmColl.fireTableCellUpdated(focusedIdx, 1);

					tblColl.repaint();
					tblData.repaint();

//					tblmColl.fireTableDataChanged();
//					tblColl.getSelectionModel().setSelectionInterval(focusedIdx, focusedIdx);

				}
				break;
			case "Gen Src":
				if (!filterUnit.isEmpty()) {
					execCmd("Commit"); // "autosave"
				}

				Dust.access(DustAccess.Set, TOKEN_DEV_TAG_CMD_TEST, params, TOKEN_MIND_ATT_CMD);
				Dust.access(DustAccess.Set, TOKEN_DUST_ATT_UNIT_REFS, params, TOKEN_MISC_ATT_PATH, KEY_ADD);
				for (DustHandle hu : filterUnit) {
					Dust.access(DustAccess.Insert, hu, params, TOKEN_MISC_ATT_MEMBERS);
				}
				Dust.access(DustAccess.Process, params, hSrcGen);

				break;
			case "Activate":
				if (null != focused) {
					Dust.access(DustAccess.Set, TOKEN_MISC_TAG_CMD_REFRESH, params, TOKEN_MIND_ATT_CMD);
					Dust.access(DustAccess.Insert, TOKEN_MISC_TAG_SLAVE, params, TOKEN_MIND_ATT_TAGS);

					Dust.access(DustAccess.Process, params, focused);
				}
				break;
			default:
				Dust.log(TOKEN_MISC_TAG_LEVEL_WARNING, "execCmd() Command not handled", cmd);
			}
		} catch (

		Throwable t) {
			DustException.wrap(t);
		}
	}

	void buildGui() {
		tfHandle.setText("5ElemCsikung.1$000a6c1e");

		Dimension dimMin = new Dimension(150, 100);
		ListSelectionModel lsm;

		cbGraph.setEditable(true);

		factToolbars.fillToolbar("tbTop", "Rebuild", "Load Tokens", null, new JLabel("Handle ID:"), tfHandle, "Load Handle", null, "Rollback", "Commit");
		factToolbars.fillToolbar("tbUnit", "Update Units", "Load Unit", null, "Gen Src");
		factToolbars.fillToolbar("tbProp", "Drop Att", "Update Value");
		factToolbars.fillToolbar("tbGraph", new JLabel("Zoom:"), "+", ".", "-", null, "Activate", null, "Random", "Load Refs", "Drop Selected", graphPanel.cbMode);
		factToolbars.fillToolbar("tbGrid", "Show Selected", "Hide Selected", null, "New handle", "Duplicate", "Delete Selected");
		factToolbars.fillToolbar("tbFilter", new JLabel("Contains:"), tfFilter);

		Container cp = frm.getContentPane();
		cp.removeAll();

		JPanel pnlMain = new JPanel(new BorderLayout());
		pnlMain.add(factToolbars.get("tbTop"), BorderLayout.NORTH);

		// Unit + text filter

		JTable unitTable = new JTable(tblmUnits);
		ListSelectionListener slUnit = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					ListSelectionModel lsm = (ListSelectionModel) e.getSource();

					DustHandle hs = null;

					int li = lsm.getLeadSelectionIndex();

					if (-1 != li) {

						int ri = unitTable.convertRowIndexToModel(li);
						hs = unitArr.get(ri);
					}

					setFocused(hs);
				}
			}
		};
		lsm = unitTable.getSelectionModel();
		lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		lsm.addListSelectionListener(slUnit);

		unitTable.setAutoCreateRowSorter(true);

		JPanel pnlUnit = new JPanel(new BorderLayout());
		DustGuiSwingUtils.setTitle(pnlUnit, "Units");
		JScrollPane scpUnit = new JScrollPane(unitTable);
		scpUnit.setMinimumSize(dimMin);
		pnlUnit.add(scpUnit, BorderLayout.CENTER);
		pnlUnit.add(factToolbars.get("tbUnit"), BorderLayout.NORTH);

		tfFilter.getDocument().addDocumentListener(new DustGuiSwingUtils.DocumentAdapter() {
			protected void update(DocumentEvent e) {
				filterStr = tfFilter.getText().toLowerCase();
				tblmGrid.fireTableDataChanged();
			}
		});

		pnlUnit.add(factToolbars.get("tbFilter"), BorderLayout.SOUTH);

		// Properties

		JPanel pnlProp = new JPanel(new BorderLayout());
		DustGuiSwingUtils.setTitle(pnlProp, "Properties");

		lsm = tblData.getSelectionModel();
		lsm.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int ai = ((ListSelectionModel) e.getSource()).getLeadSelectionIndex();
					focusedColData.clear();

					if (-1 != ai) {
						focusedAtt = focusedAttributes.get(ai);
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

		lsm = tblColl.getSelectionModel();
		lsm.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					focusedIdx = ((ListSelectionModel) e.getSource()).getLeadSelectionIndex();

					if (-1 != focusedIdx) {
						Object v = focusedColData.get(focusedIdx);
						taValue.setText(DustUtils.toString(v));
					} else {
						taValue.setText("");
					}
				}
			}
		});

		JScrollPane scpData = new JScrollPane(tblData);
		scpData.setMinimumSize(dimMin);
		JScrollPane scpColl = new JScrollPane(tblColl);
		scpColl.setMinimumSize(dimMin);
		JScrollPane scpVal = new JScrollPane(taValue);
		scpVal.setMinimumSize(dimMin);

		JComponent dc = DustGuiSwingUtils.createSplit(false, scpColl, scpVal, 1.0);
		pnlProp.add(DustGuiSwingUtils.createSplit(false, scpData, dc, 1.0), BorderLayout.CENTER);
		pnlProp.add(factToolbars.get("tbProp"), BorderLayout.SOUTH);

		// Data grid
		JTable handleTable = new JTable(tblmGrid);
		ListSelectionListener lsHandle = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					ListSelectionModel lsm = (ListSelectionModel) e.getSource();

					selected.clear();

					int li = lsm.getLeadSelectionIndex();

					for (int idx : lsm.getSelectedIndices()) {
						int ri = handleTable.convertRowIndexToModel(idx);
						DustHandle hs = gridArr.get(ri);
						selected.add(hs);

						if (li == idx) {
							setFocused(hs);
						}
					}

//					updatePropPanel();
					graphPanel.repaintGraph();
				}
			}
		};
		lsm = handleTable.getSelectionModel();
		lsm.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		lsm.addListSelectionListener(lsHandle);

		handleTable.setAutoCreateRowSorter(true);

		RowFilter rf = new RowFilter() {
			@Override
			public boolean include(Entry entry) {
				int idx = (int) entry.getIdentifier();
				DustHandle hi = gridArr.get(idx);

				if (!showTypes.isEmpty() && !showTypes.contains(hi.getType().getId())) {
					return false;
				}

				if (!DustUtils.isEmpty(filterStr)) {
					boolean found = false;
					for (String a : gridCols) {
						String av = DustUtils.toString(Dust.access(DustAccess.Peek, "", hi, a));
						found = av.toLowerCase().contains(filterStr);
						if (found) {
							break;
						}
					}

					if (!found) {
						return false;
					}
				}

				return true;
			}
		};

		((TableRowSorter) handleTable.getRowSorter()).setRowFilter(rf);
		JScrollPane scpTbl;

		JPanel pnlGrid = new JPanel(new BorderLayout());
		DustGuiSwingUtils.setTitle(pnlGrid, "Handle Grid");

//		pnlGrid.add(factToolbars.get("tbFilter"), BorderLayout.NORTH);
		pnlGrid.add(factToolbars.get("tbGrid"), BorderLayout.SOUTH);
		pnlGrid.add(new JScrollPane(handleTable), BorderLayout.CENTER);

		pnlGrid.setMinimumSize(dimMin);

		ActionListener alShowAll = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JToggleButton btn = (JToggleButton) e.getSource();
				boolean all = btn.isSelected();
				DefaultTableModel tblm = null;

				String s = btn.getText();
				switch (s) {
				case "All Types":
					tblm = tblmTypeFilter;
					break;
				case "All Atts":
					tblm = tblmAtts;
					break;
				case "All Links":
					tblm = tblmLinks;
					break;
				default:
					DustException.wrap(null, "Should not be here");
				}
			}
		};

		// Attributes

		JTable tblAtts = new JTable(tblmAtts);
		scpTbl = new JScrollPane(tblAtts);

		JPanel pnlAtts = new JPanel(new BorderLayout());
		pnlAtts.add(scpTbl, BorderLayout.CENTER);
		JToggleButton tbAllAtts = new JToggleButton("All Atts");
		tbAllAtts.addActionListener(alShowAll);
		pnlAtts.add(tbAllAtts, BorderLayout.SOUTH);
		pnlAtts.setMinimumSize(dimMin);
		DustGuiSwingUtils.setTitle(pnlAtts, "Atts");

		// Types

		JTable tblTypeFilter = new JTable(tblmTypeFilter);
		scpTbl = new JScrollPane(tblTypeFilter);
		JPanel pnlTypes = new JPanel(new BorderLayout());
		pnlTypes.add(scpTbl, BorderLayout.CENTER);
		JToggleButton tbAllTypes = new JToggleButton("All Types");
		tbAllTypes.addActionListener(alShowAll);
		pnlTypes.add(tbAllTypes, BorderLayout.SOUTH);
		pnlTypes.setMinimumSize(dimMin);
		DustGuiSwingUtils.setTitle(pnlTypes, "Types");
		lsm = tblTypeFilter.getSelectionModel();
		lsm.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					ListSelectionModel lsm = (ListSelectionModel) e.getSource();

					showTypes.clear();

					for (int idx : lsm.getSelectedIndices()) {
						showTypes.add((String) tblTypeFilter.getValueAt(idx, 0));
					}
					tblmGrid.fireTableDataChanged();
				}
			}
		});

		// Links
		JTable tblLinks = new JTable(tblmLinks);
		scpTbl = new JScrollPane(tblLinks);
		JPanel pnlLinks = new JPanel(new BorderLayout());
		pnlLinks.add(scpTbl, BorderLayout.CENTER);
		JToggleButton tbAllLinks = new JToggleButton("All Links");
		tbAllLinks.addActionListener(alShowAll);
		pnlLinks.add(tbAllLinks, BorderLayout.SOUTH);
		pnlLinks.setMinimumSize(dimMin);
		DustGuiSwingUtils.setTitle(pnlLinks, "Links");

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

		// Graph
		JPanel pnlGraph = new JPanel(new BorderLayout());
		DustGuiSwingUtils.setTitle(pnlGraph, "Handle Graph");
		pnlGraph.add(graphPanel.scpGraph, BorderLayout.CENTER);
		pnlGraph.add(factToolbars.get("tbGraph"), BorderLayout.NORTH);

		// Final build

//		pnlGrid.add(DustGuiSwingUtils.createSplit(true, new JScrollPane(handleTable), pnlAtts, 1.0), BorderLayout.CENTER);
//
//		
//		JComponent splGrid = DustGuiSwingUtils.createSplit(true, pnlTypes, pnlGrid, 0.0);
//		
//		pnlGraph.add(DustGuiSwingUtils.createSplit(true, pnlLinks, graphPanel.scpGraph, 0.0), BorderLayout.CENTER);
//
//		JPanel pnlLeft = new JPanel(new BorderLayout());
//		pnlLeft.add(DustGuiSwingUtils.createSplit(false, pnlUnit, pnlProp, 0.0), BorderLayout.CENTER);
//
//		JPanel pnlRight = new JPanel(new BorderLayout());
//		pnlRight.add(DustGuiSwingUtils.createSplit(false, splGrid, pnlGraph, 0.0), BorderLayout.CENTER);

		JPanel pnlFilter = new JPanel(new BorderLayout());
		JPanel pnlMetaFilter = new JPanel(new GridLayout(1, 3));
		pnlMetaFilter.add(pnlTypes);
		pnlMetaFilter.add(pnlAtts);
		pnlMetaFilter.add(pnlLinks);

		pnlFilter.add(DustGuiSwingUtils.createSplit(true, pnlUnit, pnlMetaFilter, 0.0), BorderLayout.CENTER);

		JPanel pnlData = new JPanel(new BorderLayout());
		JPanel pnlDataMulti = new JPanel(new BorderLayout());
		pnlDataMulti.add(DustGuiSwingUtils.createSplit(false, pnlGrid, pnlGraph, 0.2), BorderLayout.CENTER);
		pnlData.add(DustGuiSwingUtils.createSplit(true, pnlDataMulti, pnlProp, 1.0), BorderLayout.CENTER);

		pnlMain.add(DustGuiSwingUtils.createSplit(false, pnlFilter, pnlData, 0.0), BorderLayout.CENTER);

		cp.add(pnlMain);
		cp.revalidate();
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	void refillGrid() {
		boolean fu = !filterUnit.isEmpty();

		gridArr.clear();
		gridCols.clear();
		attTypes.clear();

		tblmAtts.setRowCount(0);
		tblmLinks.setRowCount(0);
		tblmTypeFilter.setRowCount(0);

		Set<DustHandle> seenTypes = new HashSet();

		for (DustHandle hu : unitArr) {
			if (fu && !filterUnit.contains(hu)) {
				continue;
			}

			Map<String, DustHandle> members = Dust.access(DustAccess.Peek, -1, hu, TOKEN_DUST_ATT_UNIT_REFS);
			for (DustHandle h : members.values()) {
				gridArr.add(h);

				DustHandle t = h.getType();
				if (seenTypes.add(t)) {
					tblmTypeFilter.addRow(new Object[] { t.getId() });
				}

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
		tblmGrid.fireTableStructureChanged();

		tblmAtts.fireTableDataChanged();
		tblmLinks.fireTableDataChanged();
		tblmTypeFilter.fireTableDataChanged();
	}

	public DustHandle getFocused() {
		return focused;
	}

	public void setFocused(DustHandle h) {
		focused = h;
		focusedAttributes.clear();

		if (null != focused) {
			Collection<String> atts = Dust.access(DustAccess.Peek, Collections.EMPTY_SET, focused, KEY_MAP_KEYS);
			focusedAttributes.addAll(atts);
			selected.add(h);
		}

		tblmProperties.fireTableDataChanged();
		graphPanel.repaintGraph();
	}

}
