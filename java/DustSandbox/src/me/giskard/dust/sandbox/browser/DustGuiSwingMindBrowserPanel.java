package me.giskard.dust.sandbox.browser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;

import me.giskard.dust.Dust;
import me.giskard.dust.gui.swing.DustGuiSwingConsts;
import me.giskard.dust.gui.swing.DustGuiSwingUtils;

//@SuppressWarnings({ "unchecked", "rawtypes" })
@SuppressWarnings({ "unchecked" })
public class DustGuiSwingMindBrowserPanel extends DustGuiSwingConsts.JPanelAgent implements DustSwingBrowserConsts {
	JComboBox<String> cbUnit = new JComboBox<String>();

	JTree trUnitInfo = new JTree();

	JLabel lbCount = new JLabel("Item count");

	JTextArea taSearch = new JTextArea();
//	JTabbedPane tpData = new JTabbedPane();
	DustGuiSwingGridPanel grid = new DustGuiSwingGridPanel();

	ActionListener al = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case TOKEN_UNIT:
				String sel = (String) cbUnit.getSelectedItem();
				grid.loadUnit(Dust.getUnit(sel, true));
				break;
			}
		}
	};

	@Override
	protected void init() throws Exception {
		String iId = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD_INFO);
		String[] i = iId.split("\\$");

		DustObject uInfo = Dust.getUnit(i[0], true);
		DustObject oInfo = Dust.getObject(uInfo, null, iId, DustOptCreate.None);

		cbUnit.removeAllItems();
		for (Map.Entry<String, Object> eUnit : (Iterable<Map.Entry<String, Object>>) Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, oInfo,
				TOKEN_KB_KNOWNUNITS)) {
			String key = eUnit.getKey();
			if (key.contains("Meta.")) {
				continue;
			}
			cbUnit.addItem(key);
		}

		cbUnit.addActionListener(al);
		cbUnit.setActionCommand(TOKEN_UNIT);

//		String data = Dust.access(DustAccess.Peek, null, null, TOKEN_DATA);
//		DustObject uData = Dust.getUnit(data, true);
//
//		grid.loadUnit(uData);
		
		cbUnit.setSelectedIndex(0);

		JPanel pnlLeft = new JPanel(new BorderLayout());

		pnlLeft.add(cbUnit, BorderLayout.NORTH);
		pnlLeft.add(DustGuiSwingUtils.setTitle(new JScrollPane(trUnitInfo), "Unit info"), BorderLayout.CENTER);
		pnlLeft.add(lbCount, BorderLayout.SOUTH);

		JComponent cGrid = DustGuiSwingUtils.setTitle(grid.getComp(), "Data");

		JSplitPane spRight = DustGuiSwingUtils.createSplit(false, DustGuiSwingUtils.setTitle(new JScrollPane(taSearch), "Search"), cGrid, 0.1);

		JSplitPane spMain = DustGuiSwingUtils.createSplit(true, pnlLeft, spRight, 0.3);

		comp.add(spMain, BorderLayout.CENTER);
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		return null;
	}

}
