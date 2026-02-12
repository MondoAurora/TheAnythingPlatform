package me.giskard.dust.sandbox.browser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import me.giskard.dust.Dust;
import me.giskard.dust.gui.swing.DustGuiSwingConsts;
import me.giskard.dust.gui.swing.DustGuiSwingUtils;
import me.giskard.dust.mvel.DustExprMvelUtils;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsConsts;
import me.giskard.dust.utils.DustUtilsConstsJson.JsonApiFilter;
import me.giskard.dust.utils.DustUtilsFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
//@SuppressWarnings({ "unchecked" })
public class DustGuiSwingMindBrowserPanel extends DustGuiSwingConsts.JPanelAgent implements DustSwingBrowserConsts, DustUtilsConsts {

	String selUnit;

	DustUtilsFactory<String, DefaultMutableTreeNode> roots = new DustUtilsFactory<String, DefaultMutableTreeNode>(new DustCreator<DefaultMutableTreeNode>() {
		@Override
		public DefaultMutableTreeNode create(Object key, Object... hints) {
			DefaultMutableTreeNode ret = new DefaultMutableTreeNode(key);
			Map<String, Object> unitInfo = Dust.access(DustAccess.Peek, null, mindInfo, TOKEN_KB_KNOWNUNITS, key);

			for (Map.Entry<String, Object> eui : unitInfo.entrySet()) {
				DefaultMutableTreeNode k = new DefaultMutableTreeNode(eui.getKey());
				ret.add(k);

				Object val = eui.getValue();
				if (val instanceof Map) {
					for (Map.Entry<String, Object> eVal : ((Map<String, Object>) val).entrySet()) {
						DefaultMutableTreeNode v = new DefaultMutableTreeNode(eVal.getKey() + ": " + eVal.getValue());
						k.add(v);
					}
				} else if (val instanceof Collection) {
					for (Object co : (Collection) val) {
						DefaultMutableTreeNode v = new DefaultMutableTreeNode(co);
						k.add(v);
					}
				} else {
					k.setUserObject(eui.getKey() + ": " + val);
				}
			}

			return ret;
		}
	}, true);

	DustUtilsFactory<String, Set> metaFilter = new DustUtilsFactory<String, Set>(SET_CREATOR, false);
	JsonApiFilter mvelFilter = new JsonApiFilter(null);

	JComboBox<String> cbUnit = new JComboBox<String>();

	DefaultTreeModel tm = new DefaultTreeModel(null);
	JTree trUnitInfo = new JTree(tm);

	JLabel lbCount = new JLabel("Item count");

	JTextArea taSearch = new JTextArea();
//	JTabbedPane tpData = new JTabbedPane();
	DustGuiSwingGridPanel grid = new DustGuiSwingGridPanel();

	ActionListener al = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
			case TOKEN_UNIT:
				selUnit = (String) cbUnit.getSelectedItem();
				grid.setUnit(Dust.getUnit(selUnit, true));

//				trUnitInfo.setRootVisible(false);
				tm.setRoot(roots.get(selUnit));

				lbCount.setText(DustUtils.toString(Dust.access(DustAccess.Peek, "?", mindInfo, TOKEN_KB_KNOWNUNITS, selUnit, TOKEN_COUNT)));

				break;
			}
		}
	};

	private DustHandle mindInfo;

	DustProcessor<Boolean> gridFilter = new DustProcessor<Boolean>() {
		@Override
		public Boolean process(DustHandle handle, Object... hints) {
			boolean ret = true;

			Set<String> s = metaFilter.get(TOKEN_ATTRIBUTES);

			if (!s.isEmpty()) {
				ret = false;
				for (String att : s) {
					if (null != Dust.access(DustAccess.Peek, null, handle, att)) {
						ret = true;
						break;
					}
				}
			}
			
//			'11111'.equals(get('cardMeta.1$card_owner.id_doc_id'))

			if (ret && (null != mvelFilter.getCondition())) {
				mvelFilter.setHandle(handle);
				Object r = DustExprMvelUtils.eval(mvelFilter.getCondition(), mvelFilter, mvelFilter.getValues(), false);
				ret = (r instanceof Boolean) ? (Boolean) r : false;
			}

			return ret;
		}
	};

	@Override
	protected void init() throws Exception {
		String iId = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD_INFO);
		String[] i = iId.split("\\$");

		DustHandle uInfo = Dust.getUnit(i[0], true);
		mindInfo = Dust.getHandle(uInfo, null, iId, DustOptCreate.None);

		grid.setExtFilter(gridFilter);

		cbUnit.removeAllItems();
		for (Map.Entry<String, Object> eUnit : (Iterable<Map.Entry<String, Object>>) Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, mindInfo,
				TOKEN_KB_KNOWNUNITS)) {
			String key = eUnit.getKey();
			if (key.contains("Meta.")) {
//				continue;
			}
			cbUnit.addItem(key);
		}

		cbUnit.addActionListener(al);
		cbUnit.setActionCommand(TOKEN_UNIT);

		if (0 < cbUnit.getItemCount()) {
			cbUnit.setSelectedIndex(0);
		}

		trUnitInfo.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {

				for (Set s : metaFilter.values()) {
					s.clear();
				}

				if (0 < trUnitInfo.getSelectionCount()) {

					for (TreePath tp : trUnitInfo.getSelectionPaths()) {
						int tpl = tp.getPathCount();

						if (3 == tpl) {

							String str = (String) ((DefaultMutableTreeNode) tp.getPathComponent(1)).getUserObject();
							Set s = metaFilter.get(str);

							s.add(DustUtils.getPrefix((String) ((DefaultMutableTreeNode) tp.getPathComponent(2)).getUserObject(), ":"));
						}
					}
				}

				grid.reload();
			}
		});

		taSearch.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				optFilter();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				optFilter();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				optFilter();
			}
		});

		JPanel pnlLeft = new JPanel(new BorderLayout());

		pnlLeft.add(cbUnit, BorderLayout.NORTH);
		pnlLeft.add(DustGuiSwingUtils.setTitle(new JScrollPane(trUnitInfo), "Unit info"), BorderLayout.CENTER);
		pnlLeft.add(lbCount, BorderLayout.SOUTH);

		JComponent cGrid = DustGuiSwingUtils.setTitle(grid.getComp(), "Data");

		JSplitPane spRight = DustGuiSwingUtils.createSplit(false, DustGuiSwingUtils.setTitle(new JScrollPane(taSearch), "Search"), cGrid, 0.1);

		JSplitPane spMain = DustGuiSwingUtils.createSplit(true, pnlLeft, spRight, 0.3);

		comp.add(spMain, BorderLayout.CENTER);
	}

	protected void optFilter() {
		String f = taSearch.getText().trim();
		mvelFilter.setCondition(f);

		if (!f.isEmpty()) {
			try {
				DustExprMvelUtils.compile(f);
				grid.reload();
			} catch (Throwable e) {
				mvelFilter.setCondition(null);
			}
		}
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		return null;
	}

}
