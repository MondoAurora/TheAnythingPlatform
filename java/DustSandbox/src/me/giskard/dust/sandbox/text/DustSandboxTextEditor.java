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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
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
import me.giskard.dust.core.mind.DustMindUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFactory;
import me.giskard.dust.mod.gui.swing.DustGuiSwingUtils;
import me.giskard.dust.mod.utils.DustUtilsJson;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustSandboxTextEditor extends DustAgent implements DustSandboxTextConsts {

	DustSandboxTextAgent txtAgent;

	DustHandle hCurrentLayout;
	DustHandle hLang;
	Map<String, DustHandle[]> opts = new TreeMap<String, DustHandle[]>();

	JFrame frm;

	JTextField tfUnit = new JTextField("test.1");
	JTextField tfFind = new JTextField();

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
				value = Dust.access(DustAccess.Peek, DustUtils.getPostfix(h.getId(), DUST_SEP_TOKEN), h, TOKEN_NAME);
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

	ArrayList<DustHandle> styleArr = new ArrayList<>();
	JTable styleTable;
	AbstractTableModel styleModel;
	JTextArea styleEditor;

	ArrayList<DustHandle> resArr = new ArrayList<>();
	JTable resTable;
	AbstractTableModel resModel;
	JLabel resPreview;
	DustUtilsFactory<DustHandle, Icon> resIconFactory = new DustUtilsFactory<DustHandle, Icon>(new DustCreator<Icon>() {
		@Override
		public Icon create(Object key, Object... hints) {
			ImageIcon icon = null;
			try {
				String path = Dust.access(DustAccess.Peek, null, (DustHandle) key, TOKEN_PATH);
				File f = new File(txtAgent.docPath, path);
				BufferedImage image = ImageIO.read(f);
				icon = new ImageIcon(image);
			} catch (Throwable e) {
				DustException.swallow(e, key);
			}

			return icon;
		}
	}, false);

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

			int idx;

			boolean refresh = false;

			boolean fwd = true;

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
							null, opts.get(TOKEN_LAYOUT_LAYOUT_OTPIONS), null);
					if (null != defLayout) {
						idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);
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
						for (DustHandle hl : opts.get(TOKEN_LAYOUT_LAYOUT_OTPIONS)) {
							if (hl.getId().endsWith(ln)) {
								hLayout = hl;
								break;
							}
						}
					}

					if (null == hLayout) {
						hLayout = hCurrentLayout;
					}

					DustHandle hResp;
					if (TOKEN_LAYOUT_RESPONSIVE.equals(hThis.getType().getId())) {
						hResp = hThis;
					} else {
						idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);
						hResp = txtAgent.insertNode(hParent, idx, TOKEN_LAYOUT_RESPONSIVE);
						Dust.access(DustAccess.Set, hLayout, hResp, TOKEN_LAYOUT_LAYOUT);
					}

					txtAgent.insertTable(hResp, hLayout, dataRows, dataCols, headRows, headCols);

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
				case "<- Find":
					fwd = false;
				case "Find ->":
					String find = tfFind.getText();
					if (!DustUtils.isEmpty(find)) {
						int li = doc.getLength() - 1;
						int cp = docEditor.getCaretPosition();

						String txt = doc.getText(fwd ? cp + 1 : 0, fwd ? li - cp : cp - 1).toLowerCase();
						find = find.toLowerCase();
						
						idx = fwd ? txt.indexOf(find) : txt.lastIndexOf(find);

						if (-1 == idx) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							int so = fwd ? idx + cp + 1: idx;
							docEditor.select(so, so + find.length());
						}
					}
					docEditor.requestFocusInWindow();

					break;
				case "<- Style":
					fwd = false;
				case "Style ->":
					idx = styleTable.getSelectedRow();
					if (-1 != idx) {
						DustHandle hS = styleArr.get(idx);
						Set<String> match = new HashSet<>();
						for (DustHandle h : DustMindUtils.getUnitMembers(txtAgent.hUnit)) {
							Set oo = Dust.access(DustAccess.Peek, Collections.EMPTY_SET, h, TOKEN_TEXT_STYLES);
							if (!oo.isEmpty()) {
								Dust.log(TOKEN_LEVEL_TRACE, oo);

								if ((boolean) Dust.access(DustAccess.Check, hS, h, TOKEN_TEXT_STYLES)) {
									match.add(h.getId());
								}
							}
						}

						if (match.isEmpty()) {
							Toolkit.getDefaultToolkit().beep();
						} else {
							int li = doc.getLength() - 1;
							int cp = docEditor.getCaretPosition();

							Element ep = doc.getCharacterElement(cp);
							String id = DustSandboxTextUtils.getId(ep);
							match.remove(id);

							for (int pos = fwd ? cp + 1 : cp - 1; fwd ? (pos <= li) : (0 <= pos);) {
								ep = doc.getCharacterElement(pos);
								if (null == ep) {
									pos += fwd ? 1 : -1;
								} else {
									id = DustSandboxTextUtils.getId(ep);

									int eo = ep.getEndOffset();
									if (match.contains(id)) {
										docEditor.setCaretPosition(ep.getStartOffset());
//										docEditor.moveCaretPosition(ep.getStartOffset());
//										docEditor.setCaretPosition((eo < li) ? eo - 1 : li);

										docEditor.requestFocusInWindow();
										break;
									} else {
										pos = fwd ? eo + 1 : ep.getStartOffset() - 1;
									}
								}
							}
						}

					}
					break;
				case "Apply":
					idx = styleTable.getSelectedRow();
					if (-1 != idx) {
						DustHandle hS = styleArr.get(idx);
						for (DustHandle hn : selMgr.hSel) {
							Dust.access(DustAccess.Insert, hS, hn, TOKEN_TEXT_STYLES);
							refresh = true;
						}
						if (null != hThis) {
							Dust.log(TOKEN_LEVEL_TRACE, "Setting style", hS, txtAgent.accessText(DustAccess.Peek, "", hThis.getId()));
							Dust.access(DustAccess.Insert, hS, hThis, TOKEN_TEXT_STYLES);
							refresh = true;
						}
					}
					break;
				case "Update":
					idx = styleTable.getSelectedRow();
					if (-1 != idx) {
						DustHandle hS = styleArr.get(idx);
						String txt = styleEditor.getText();
						Object def = DustUtilsJson.parseJson(txt);
						Dust.access(DustAccess.Set, def, hS, TOKEN_TEXT_STYLE_DEF);
						txtAgent.updateStyleDef(hS);
						refresh = true;
					}
					break;
				case "ResInsert":
					int sr = resTable.getSelectedRow();
					if (-1 != sr) {
						DustHandle hR = resArr.get(sr);
						idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);
						DustHandle hRef = txtAgent.insertNode(hParent, idx, TOKEN_STREAM_REF);
						Dust.access(DustAccess.Set, hR, hRef, TOKEN_TARGET);

						refresh = true;
					}
					break;
				case "PnlHtml":
					Dust.log(TOKEN_LEVEL_INFO, docEditor.getText());
					break;
				case "GenHtml":
					String html = htmlGen.generateHtml(txtAgent);
					Dust.log(TOKEN_LEVEL_INFO, html);

					try (PrintWriter out = new PrintWriter(new File(txtAgent.docPath, "test.html"))) {
						out.println(html);
						out.close();
					}
					break;
				case "ExtRes":
					for (DustHandle h : DustMindUtils.getUnitMembers(txtAgent.hUnit)) {
						String ht = h.getType().getId();

						switch (ht) {
						case TOKEN_TEXT_BLOCK:
							String txt = txtAgent.accessText(DustAccess.Peek, null, h.getId());
							if (!DustUtils.isEmpty(txt)) {
								txtAgent.accessTextExt(DustAccess.Set, hLang, txt, h.getId());
								DustHandle hNode = Dust.getHandle(txtAgent.hUnit, null, h.getId(), DustOptCreate.None);
								Dust.access(DustAccess.Delete, null, hNode, TOKEN_TEXT_TEXT);
							}
							break;
						}
					}

					txtAgent.save();
					break;
				case "Translate":
					DustHandle tLan = (DustHandle) JOptionPane.showInputDialog((Component) e.getSource(), "Default layout?", "Create responsive block",
							JOptionPane.QUESTION_MESSAGE, null, opts.get(TOKEN_LANG_SUPPORTED), null);
					if (null != tLan) {
						txtAgent.translate(tLan, hLang, selMgr.hSel);
					}
					break;
				default:
					Dust.log(TOKEN_LEVEL_WARNING, "Command not handled", cmd);
				}

				if (refresh) {
					int cp = docEditor.getCaretPosition();
					updateDocEditor();
					updateStruct();
					int l = docEditor.getText().length();
					if (cp >= l) {
						cp = l - 1;
					}
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
			boolean inserted = false;

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
							DustHandle hImg = txtAgent.insertImage(selMgr.hfParent, selMgr.hfBlock, (Image) o);
							int ri = resArr.size();
							resArr.add(hImg);
							resModel.fireTableRowsInserted(ri, ri);
							inserted = true;
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

									inserted = true;
								}
							}
						}
					}

					if (inserted) {
						updateDocEditor();
						updateStruct();

						return true;
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
			try {
				selMgr.selUpdating = docEditor;
				if (string.contains("\n")) {
					Toolkit.getDefaultToolkit().beep();
				}
				super.insertString(fb, offset, string, attr);
			} finally {
				selMgr.selUpdating = null;
			}
		}

		@Override
		public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
			try {

				selMgr.selUpdating = docEditor;
				super.remove(fb, offset, length);
			} finally {
				selMgr.selUpdating = null;
			}
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
			try {
				selMgr.selUpdating = docEditor;
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
					fb.replace(offset, length, text, attrs);

					int so = es.getStartOffset();
					int len = es.getEndOffset() - so;
					String txt = doc.getText(so, len);
					String id = DustSandboxTextUtils.getId(es);

					txtAgent.accessText(DustAccess.Set, txt, id);

					updateStruct();
				}
			} finally {
				selMgr.selUpdating = null;
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

		loadDoc(tfUnit.getText());

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

		styleArr.clear();
		for (DustHandle hr : DustMindUtils.getUnitMembers(txtAgent.hUnit)) {
			String ht = hr.getType().getId();

			switch (ht) {
			case TOKEN_TEXT_STYLE:
				styleArr.add(hr);
				break;
			}
		}

		resArr.clear();

		for (DustHandle hr : DustMindUtils.getUnitMembers(txtAgent.hRes)) {
			String ht = hr.getType().getId();

			switch (ht) {
			case TOKEN_STREAM:
				resArr.add(hr);
				break;
			}
		}
	};

	void buildGui() {
		frm.getContentPane().removeAll();

		JTabbedPane tpLeft = new JTabbedPane();

		JPanel left = new JPanel(new BorderLayout());
		left.add(new JScrollPane(docStruct), BorderLayout.CENTER);

		tpLeft.add("Struct", left);

		ListSelectionModel lsm;

		JPanel pnlStyles = new JPanel(new BorderLayout());
		styleModel = new AbstractTableModel() {
			@Override
			public int getRowCount() {
				return styleArr.size();
			}

			@Override
			public int getColumnCount() {
				return 1;
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				DustHandle hs = styleArr.get(rowIndex);
				return Dust.access(DustAccess.Peek, null, hs, TOKEN_NAME);
			}
		};
		styleTable = new JTable(styleModel);

		styleEditor = new JTextArea();

		lsm = styleTable.getSelectionModel();
		lsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lsm.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int sr = styleTable.getSelectedRow();
					Icon icon = null;
					if (-1 != sr) {
						DustHandle hStyle = styleArr.get(sr);
						Map<String, Object> def = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, hStyle, TOKEN_TEXT_STYLE_DEF);

						StringBuilder sb = null;
						for (Map.Entry<String, Object> de : def.entrySet()) {
							if (null == sb) {
								sb = new StringBuilder("{\n  ");
							} else {
								sb.append(",\n  ");
							}
							sb = DustUtils.sbAppend(sb, "", true, "\"", de.getKey(), "\": \"", de.getValue(), "\"");
						}

						String defStr = (null == sb) ? "" : sb.append("\n}").toString();

						styleEditor.setText(defStr);
					}
					resPreview.setIcon(icon);
				}
			}
		});

		pnlStyles.add(DustGuiSwingUtils.createSplit(false, new JScrollPane(styleTable), new JScrollPane(styleEditor), 0.5), BorderLayout.CENTER);

		tpLeft.add("Styles", pnlStyles);

		JPanel pnlRes = new JPanel(new BorderLayout());
		resModel = new AbstractTableModel() {
			@Override
			public int getRowCount() {
				return resArr.size();
			}

			@Override
			public int getColumnCount() {
				return 1;
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				DustHandle hs = resArr.get(rowIndex);
				return Dust.access(DustAccess.Peek, null, hs, TOKEN_PATH);
			}
		};
		resTable = new JTable(resModel);

		resPreview = new JLabel();

		lsm = resTable.getSelectionModel();
		lsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lsm.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int sr = resTable.getSelectedRow();
					Icon icon = null;
					if (-1 != sr) {
						DustHandle hRes = resArr.get(sr);
						icon = resIconFactory.get(hRes);

						for (DustHandle h : DustMindUtils.getUnitMembers(txtAgent.hUnit)) {
							if ((boolean) Dust.access(DustAccess.Check, hRes, h, TOKEN_TARGET)) {
								String id = h.getId();

								Dust.log(TOKEN_LEVEL_TRACE, "Selected image ref id", id);
								Element eRef = doc.getElement(id);

								if (null != eRef) {
									docEditor.setCaretPosition(eRef.getStartOffset());
								}
								break;
							}
						}
					}
					resPreview.setIcon(icon);
				}
			}
		});

		pnlRes.add(DustGuiSwingUtils.createSplit(false, new JScrollPane(resTable), new JScrollPane(resPreview), 0.5), BorderLayout.CENTER);

		tpLeft.add("Resources", pnlRes);

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

		cbLang.setSelectedItem(hLang);
		cbLayout.setSelectedItem(hCurrentLayout);

		fillToolbar("tbTop", "Rebuild", "Reset", null, new JLabel("Unit:"), tfUnit, "Load", "Save", null, new JLabel("Find:"),tfFind, "<- Find", "Find ->", null, cbLang, cbLayout);
		fillToolbar("tbDoc", null, "<-", "->", null, "Delete", "UnderFirst", null, "Bullet", "Number", "Local", null, "Resp", "Table", "Merge", null, "Style ->",
				"<- Style", "Apply", "Update", "New", "Drop", null, "ResInsert", null, "PnlHtml", "GenHtml", Box.createGlue(), "Translate", "ExtRes", null);

		frm.getContentPane().revalidate();

		updateDocEditor();
		updateStruct();
	}

	private JComboBox createCombo(String cmd, Object field) {
		Vector lv;
		DustHandle[] o = opts.get((String) field);

		if (null == o) {
			lv = new Vector(Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, null, field));
			o = new DustHandle[lv.size()];
			o = (DustHandle[]) lv.toArray(o);
			opts.put((String) field, o);
		} else {
			lv = new Vector(o.length);
			for ( DustHandle h : o ) {
				lv.add(h);
			}
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
