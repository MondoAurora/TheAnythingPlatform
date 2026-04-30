package me.giskard.dust.sandbox.text;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.text.AbstractDocument.LeafElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFactory;
import me.giskard.dust.mod.gui.swing.DustGuiSwingUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustSandboxTextEditor extends DustAgent implements DustSandboxTextConsts {

	DustSandboxTextAgent txtAgent;

	DustHandle hCurrentLayout;
	DustHandle hLang;
	DustHandle[] layoutOptions;

	JFrame frm;

	JTextField tfUnit = new JTextField("test.1");

	JTree docStruct;
	DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
	DustUtilsFactory<DustHandle, DefaultMutableTreeNode> structNodeFactory = new DustUtilsFactory.Simple<DustHandle, DefaultMutableTreeNode>(false,
			DefaultMutableTreeNode.class);
	DefaultTreeModel structModel = new DefaultTreeModel(rootNode);
	DefaultTreeCellRenderer structRenderer = new DefaultTreeCellRenderer() {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			Object uo = ((DefaultMutableTreeNode) value).getUserObject();
			String txt = (uo instanceof DustHandle) ? txtAgent.getShortText((DustHandle) uo, 50) : DustUtils.toString(uo);

			Component r = super.getTreeCellRendererComponent(tree, txt, sel, expanded, leaf, row, hasFocus);

			return r;
		};
	};

	ListCellRenderer listRenderer = new DefaultListCellRenderer() {
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if (value instanceof DustHandle) {
				DustHandle h = (DustHandle) value;
				value = Dust.access(DustAccess.Peek, h.getId(), h, TOKEN_NAME);
			} else if (null == value) {
				value = "<not set>";
			}
			Component ret = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			return ret;
		};
	};

	HTMLDocument doc;
	JEditorPane docEditor;
	JTextPane docPreview;

	DustSandboxTextSelectionManager selMgr;
	DustSandboxTextHtmlGenerator htmlGen;

	DustUtilsFactory<String, JComponent> factToolbars = new DustUtilsFactory<String, JComponent>(new DustCreator<JComponent>() {
		@Override
		public JComponent create(Object key, Object... hints) {
			JPanel pnl = new JPanel(null);

			int a = DustUtils.optGet(hints, 0, BoxLayout.PAGE_AXIS);
			pnl.setLayout(new BoxLayout(pnl, a));
			return pnl;
		}
	});

	DustUtilsFactory<String, JComponent> factActionControls = new DustUtilsFactory<String, JComponent>(new DustCreator<JComponent>() {
		@Override
		public JComponent create(Object key, Object... hints) {
			if (0 == hints.length) {
				return DustGuiSwingUtils.createBtn((String) key, al, JButton.class);
			}
			return null;
		}
	});

	ActionListener al = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();

			DustHandle hThis = selMgr.getFocusedBlock();
			DustHandle hParent = selMgr.getFocusedParent();

			boolean refresh = false;

			try {

				switch (cmd) {
				case "Rebuild":
					buildGui();
					break;
				case "Reset":
					txtAgent.reset();
					refresh = true;
					break;
				case "Layout":
					hCurrentLayout = (DustHandle) ((JComboBox) e.getSource()).getSelectedItem();
					txtAgent.setLayout(hCurrentLayout);
					refresh = true;
					break;
				case "Language":
					hLang = (DustHandle) ((JComboBox) e.getSource()).getSelectedItem();
					txtAgent.setLang(hLang);
					refresh = true;
					break;
				case "Load":
					loadDoc(tfUnit.getText());
					refresh = true;
					break;
				case "Save":
					txtAgent.save();
					break;

				case "<-":
					updateStruct();
					break;
				case "->":
					updateDocEditor();
					break;

				case "Delete":
					txtAgent.delete(hParent, selMgr.hSel);
					refresh = true;
					break;
				case "UnderFirst":
					txtAgent.underFirst(hParent, selMgr.hSel);
					refresh = true;
					break;
				case "Bullet":
					txtAgent.setGroupType(hThis, TOKEN_TEXT_GROUP_BULLET);
					refresh = true;
					break;
				case "Number":
					txtAgent.setGroupType(hThis, TOKEN_TEXT_GROUP_NUMBER);
					refresh = true;
					break;
				case "Resp":
					Object defLayout = JOptionPane.showInputDialog((Component) e.getSource(), "Default layout?", "Create responsive block", JOptionPane.QUESTION_MESSAGE,
							null, layoutOptions, null);
					if (null != defLayout) {
						int idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);
						DustHandle hResp = txtAgent.insertNode(hParent, idx, TOKEN_LAYOUT_RESPONSIVE);
						Dust.access(DustAccess.Set, defLayout, hResp, TOKEN_LAYOUT_LAYOUT);
					}
					refresh = true;
					break;
				case "Table":
					String resp = JOptionPane.showInputDialog("Table dimensions? row / col / hdrRow / hdrCol / layout end");
					if (null == resp) {
						return;
					}

					String[] dim = DustUtils.isEmpty(resp) ? new String[] {} : resp.split("/");

					Long dataRows = Long.valueOf(DustUtils.optGet(dim, 0, "3").trim());
					Long dataCols = Long.valueOf(DustUtils.optGet(dim, 1, "4").trim());
					Long headRows = Long.valueOf(DustUtils.optGet(dim, 2, "2").trim());
					Long headCols = Long.valueOf(DustUtils.optGet(dim, 3, "2").trim());

					String ln = DustUtils.optGet(dim, 4, "").trim();
					DustHandle hLayout = null;
					if (!DustUtils.isEmpty(ln)) {
						for (DustHandle hl : layoutOptions) {
							if (hl.getId().endsWith(ln)) {
								hLayout = hl;
								break;
							}
						}
					}

					DustHandle hResp;
					if (TOKEN_LAYOUT_RESPONSIVE.equals(hThis.getType().getId())) {
						hResp = hThis;
					} else {
						int idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);
						hResp = txtAgent.insertNode(hParent, idx, TOKEN_LAYOUT_RESPONSIVE);
					}

					txtAgent.insertTable(hResp, (null == hLayout) ? hCurrentLayout : hLayout, dataRows, dataCols, headRows, headCols);

					refresh = true;

					break;
				case "Local": {
					int sb = docEditor.getSelectionStart();
					int se = docEditor.getSelectionEnd();

					int eb = selMgr.eFocus.getStartOffset();
					int ee = selMgr.eFocus.getEndOffset();

					String origText = docEditor.getText(eb, ee - eb);
					int rsb = sb - eb;
					int rse = se - eb;

					txtAgent.splitInline(hParent, hThis, origText, rsb, rse);

					refresh = true;
				}
					break;
				case "PnlHtml":
					Dust.log(TOKEN_LEVEL_INFO, docEditor.getText());
					break;
				case "GenHtml":
					String html = htmlGen.generateHtml(txtAgent);
					Dust.log(TOKEN_LEVEL_INFO, html);
					
					try ( PrintWriter out = new PrintWriter(new File(txtAgent.docPath, "test.html")) ) {
						out.println(html);
						out.close();
					}
					break;
				default:
					Dust.log(TOKEN_LEVEL_WARNING, "Command not handled", cmd);
				}

				if (refresh) {
					int cp = docEditor.getCaretPosition();
					updateDocEditor();
					updateStruct();
					docEditor.setCaretPosition(cp);
				}
			} catch (Throwable ex) {
				DustException.swallow(ex, cmd);
			}
		}
	};

	TransferHandler cbTransferHandler = new TransferHandler() {

		private static final long serialVersionUID = 1L;

		@Override
		public boolean importData(TransferSupport support) {
			Transferable t = support.getTransferable();
			String pt = null;
			String st = null;

			try {

				for (DataFlavor df : support.getDataFlavors()) {

					pt = df.getPrimaryType();
					st = df.getSubType();

					if ("image".equals(pt)) {
						Object o = t.getTransferData(df);
						if (o instanceof Image) {
							txtAgent.insertImage(selMgr.hfParent, selMgr.hfBlock, (Image) o);
							return true;
						}
					}

					if ("text".equals(pt)) {
						if ("plain".equals(st)) {
							Object o = t.getTransferData(df);
							if (o instanceof String) {
								String str = (String) o;

								if (-1 == str.indexOf("\n")) {
									doc.insertString(selMgr.getCaretPos(), str, null);
									return true;
								} else {
									DustHandle hThis = selMgr.getFocusedBlock();
									DustHandle hParent = selMgr.getFocusedParent();

									txtAgent.insertLongText(hParent, hThis, str);

									updateDocEditor();
									updateStruct();

									return true;
								}
							}
						}
					}
				}
			} catch (Throwable e) {
				DustException.wrap(e, TOKEN_LEVEL_INFO, "DF", pt, st, e);
			}

			return false;
		}

		@Override
		public boolean canImport(TransferSupport support) {
			for (DataFlavor df : support.getDataFlavors()) {
				if ("text".equals(df.getPrimaryType())) {
					if ("plain".equals(df.getSubType())) {
						return true;
					}
				}
			}

			return false;
		}
	};

	DocumentFilter df = new DocumentFilter() {
		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
			if (string.contains("\n")) {
				Toolkit.getDefaultToolkit().beep();
			}
			super.insertString(fb, offset, string, attr);
		}

		@Override
		public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
//			String s = doc.getText(offset, length);
			super.remove(fb, offset, length);
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
			boolean skip = false;

			Element es = doc.getCharacterElement(offset);
			Element ee = doc.getCharacterElement(offset + length);

			if (ee != es) {
				skip = true;
			} else {
				if (text.contains("\n")) {
					if (text.length() == 1) {
						int dl = doc.getLength();
						int to = Math.max(0, offset - 2);
						int tl = Math.min(dl - to + 1, 4);

						String s = doc.getText(to, tl);

						if (-1 != s.indexOf("\n\n")) {
							skip = true;
						} else {
							int so = es.getStartOffset();
							String orig = doc.getText(so, es.getEndOffset() - so);
							String remain = orig.substring(0, offset - so);
							String move = orig.substring(offset - so);

							DustHandle hThis = selMgr.getFocusedBlock();
							DustHandle hParent = selMgr.getFocusedParent();

							txtAgent.split(hParent, hThis, remain, move);

							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									updateDocEditor();
									updateStruct();
									docEditor.setCaretPosition(offset);
								}
							});

							return;
						}
					}
				}
			}

			if (skip) {
				Toolkit.getDefaultToolkit().beep();
			} else {
				super.replace(fb, offset, length, text, attrs);
			}
		}
	};

	protected void read() {
		String str = docEditor.getText();

		Dust.log(TOKEN_LEVEL_TRACE, str);
	};

	@Override
	protected void init() throws Exception {
		txtAgent = new DustSandboxTextAgent();

		hCurrentLayout = Dust.access(DustAccess.Peek, null, null, TOKEN_LAYOUT_LAYOUT);
		hLang = Dust.access(DustAccess.Peek, null, null, TOKEN_TEXT_LANG);

		DustGuiSwingUtils.optSetLookAndFeel();

		frm = new JFrame();

		frm.setTitle(Dust.access(DustAccess.Peek, "Text editor", null, TOKEN_NAME));

		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frm.setBounds(100, 100, 1000, 800);

		docEditor = new JEditorPane();
		docEditor.setContentType("text/html");
		docEditor.setTransferHandler(cbTransferHandler);

		doc = (HTMLDocument) docEditor.getDocument();
		doc.setBase(txtAgent.docUrl);
		doc.setDocumentFilter(df);

		htmlGen = new DustSandboxTextHtmlGenerator();

		docPreview = new JTextPane();
		docPreview.setContentType("text/html");
		docPreview.setEditable(false);
		docPreview.setDocument(doc);

		docStruct = new JTree(structModel);
		docStruct.setRootVisible(false);
		docStruct.setCellRenderer(structRenderer);

		selMgr = new DustSandboxTextSelectionManager(txtAgent, doc);
		selMgr.attach(docStruct);
		selMgr.attach(docEditor);
		selMgr.attach(docPreview);

		factToolbars.get("tbTop", BoxLayout.LINE_AXIS);

		buildGui();

		frm.setVisible(true);
	};

	void updateDocEditor() {
		String htmlContent = htmlGen.generateHtml(txtAgent);
		selMgr.reset();
		docEditor.setText(htmlContent);
		docPreview.setText(htmlContent);
	};

	void loadDoc(String docUnit) {
		txtAgent.load(tfUnit.getText(), hCurrentLayout, hLang);
		updateDocEditor();
		updateStruct();
	};

	void buildGui() {
		frm.getContentPane().removeAll();

		JPanel left = new JPanel(new BorderLayout());
		left.add(new JScrollPane(docStruct), BorderLayout.CENTER);

		JTabbedPane tpLeft = new JTabbedPane();
		tpLeft.add("Struct", left);
		tpLeft.add("Styles", new JLabel("Style editor"));
		tpLeft.add("Resources", new JLabel("Resource editor"));

		JPanel right = new JPanel(new BorderLayout());
		JScrollPane scpDocEd = new JScrollPane(docEditor);
		right.add(scpDocEd, BorderLayout.CENTER);
		right.add(factToolbars.get("tbDoc"), BorderLayout.WEST);

		JTabbedPane tpRight = new JTabbedPane();
		tpRight.add("Edit", right);
		tpRight.add("Test", new JScrollPane(docPreview));

		JPanel pnlMain = new JPanel(new BorderLayout());
		pnlMain.add(factToolbars.get("tbTop"), BorderLayout.NORTH);
		pnlMain.add(DustGuiSwingUtils.createSplit(true, tpLeft, tpRight, 0.3), BorderLayout.CENTER);

		frm.getContentPane().add(pnlMain);

		JComboBox cbLang = createCombo("Language", TOKEN_LANG_SUPPORTED);
		JComboBox cbLayout = createCombo("Layout", TOKEN_LAYOUT_LAYOUT_OTPIONS);

		fillToolbar("tbTop", "Rebuild", "Reset", null, tfUnit, "Load", "Save", null, cbLang, cbLayout);
		fillToolbar("tbDoc", "<-", "->", null, "Delete", "UnderFirst", null, "Bullet", "Number", "Local", null, "Resp", "Table", "Merge", null, "PnlHtml",
				"GenHtml");

		frm.getContentPane().revalidate();

		loadDoc(tfUnit.getText());

		updateDocEditor();
		updateStruct();
	}

	private JComboBox createCombo(String cmd, Object field) {
		Vector lv = new Vector(Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, null, field));
		if (TOKEN_LAYOUT_LAYOUT_OTPIONS.equals(field)) {
			layoutOptions = new DustHandle[lv.size()];
			layoutOptions = (DustHandle[]) lv.toArray(layoutOptions);
		}
		JComboBox cb = new JComboBox(lv);
		cb.setActionCommand(cmd);
		cb.setRenderer(listRenderer);
		return cb;
	}

	void fillToolbar(String tb, Object... cmds) {
		JComponent pnl = factToolbars.get(tb);
		pnl.removeAll();

		for (Object o : cmds) {
			if (null == o) {
				pnl.add(Box.createRigidArea(new Dimension(10, 10)));
			} else if (o instanceof JComponent) {
				pnl.add((JComponent) o);
				if (o instanceof JComboBox) {
					((JComboBox) o).addActionListener(al);
				}
			} else if (o instanceof String) {
				pnl.add(factActionControls.get((String) o));
			}
		}
	}

	public void updateStruct() {
		rootNode.removeAllChildren();

		loadNode(rootNode, txtAgent.hDoc);

		structModel.nodeStructureChanged(rootNode);

	}

	public DefaultMutableTreeNode loadNode(DefaultMutableTreeNode p, DustHandle h) {
		p.setUserObject(h);

		for (DustHandle hc : (Collection<DustHandle>) Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, h, TOKEN_MEMBERS)) {
			p.add(loadNode(new DefaultMutableTreeNode(), hc));
		}

		return p;
	}

	public DefaultMutableTreeNode loadElement(DefaultMutableTreeNode p, Element e) {
		String s = e.toString();

		if (e instanceof LeafElement) {
			LeafElement le = (LeafElement) e;
			try {
				int start = le.getStartOffset();
				int end = le.getEndOffset();
				s = doc.getText(start, end - start);
				int maxLen = 50;
				if (s.length() > maxLen) {
					s = s.substring(0, maxLen - 3) + "...";
				} else {
					s = s.trim();
				}

				if (!DustUtils.isEmpty(s)) {
					p = new DefaultMutableTreeNode(s);
				}
			} catch (Throwable e1) {
				DustException.swallow(e1);
			}
		} else {
			for (int i = 0; i < e.getElementCount(); ++i) {
				Element eChild = e.getElement(i);
				DefaultMutableTreeNode pc = loadElement(null, eChild);
				if (null != pc) {
					if (null == p) {
						p = new DefaultMutableTreeNode(s);
					}
					p.add(pc);
				}
			}
		}

		return p;
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
