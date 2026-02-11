package me.giskard.dust.sandbox.browser;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import me.giskard.dust.Dust;
import me.giskard.dust.gui.swing.DustGuiSwingConsts;
import me.giskard.dust.mind.DustMindUtils;
import me.giskard.dust.utils.DustUtils;

//@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustGuiSwingGridPanel extends DustGuiSwingConsts.JPanelAgent implements DustSwingBrowserConsts {
	
	DustHandle unit;
	DustProcessor<Boolean> extFilter = NO_FILTER;

	private final ArrayList<DustHandle> allData = new ArrayList<>();
	private final ArrayList<DustHandle> display = new ArrayList<>();

	private final Set<String> allAtts = new TreeSet<>();
	private final ArrayList<String> atts = new ArrayList<>();

	private boolean dataRows = true;

	class GridTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return (0 == columnIndex) ? Integer.class : Object.class;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (0 == columnIndex) {
				return rowIndex + 1;
			} else {
				--columnIndex;
			}

			int iData = dataRows ? rowIndex : columnIndex;
			int iAtt = dataRows ? columnIndex : rowIndex;

			Object ob = display.get(iData);
			Object val = Dust.access(DustAccess.Peek, null, ob, atts.get(iAtt));

			return val;
		}

		public String getColumnName(int column) {
			if (0 == column) {
				return "#";
			} else {
				--column;
				return dataRows ? DustUtils.getPostfix(atts.get(column), DUST_SEP_TOKEN) : display.get(column).getId();
			}
		};

		@Override
		public int getRowCount() {
			return dataRows ? display.size() : atts.size();
		}

		@Override
		public int getColumnCount() {
			return (dataRows ? atts.size() : display.size()) + 1;
		}

		private void updateAll() {
			fireTableStructureChanged();
		}
	};

	GridTableModel tm = new GridTableModel();

	JTable tblData;
	JScrollPane scp;
	
	JPanel colEditor;

	public DustGuiSwingGridPanel() {
		tblData = new JTable(tm);

		tblData.setAutoCreateRowSorter(true);

		scp = new JScrollPane(tblData, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tblData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		comp.add(scp, BorderLayout.CENTER);
	}
	
	public void setExtFilter(DustProcessor<Boolean> extFilter) {
		this.extFilter = (null == extFilter) ? NO_FILTER : extFilter;
	}
	
	public JPanel getColEditor() {
		if ( null == colEditor ) {
			colEditor = new JPanel(new GridBagLayout());

//			GridBagConstraints gbc = new GridBagConstraints();
		}
		return colEditor;
	}

	public void setUnit(DustHandle unit) {
		this.unit = unit;
		reload();
	}

	public void reload() {
		allData.clear();
		allAtts.clear();

		for (DustHandle h : DustMindUtils.getUnitMembers(unit)) {
			if ( !extFilter.process(h) ) {
				continue;
			}
			
			allData.add(h);
			for (String a : DustMindUtils.getAttNames(h)) {
				String p = DustUtils.getPrefix(a, DUST_SEP_TOKEN);
				if (DUST_UNIT_ID.equals(p)) {
//					continue;
				}
				allAtts.add(a);
			}
		}

		atts.clear();
		atts.addAll(allAtts);
		atts.sort(null);

		display.clear();
		display.addAll(allData);

		tm.updateAll();
	}

	@Override
	protected void init() throws Exception {
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		return null;
	}

}
