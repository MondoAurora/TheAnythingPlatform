package me.giskard.dust.sandbox.text;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.imageio.ImageIO;
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
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.mind.DustMindUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFactory;
import me.giskard.dust.mod.gui.swing.DustGuiSwingUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustSandboxTextEditor extends DustAgent implements DustSandboxTextConsts {

	DustHandle hTypeDoc;
	DustHandle hTypeBlock;
	DustHandle hTagBullet;
	DustHandle hTagNumber;
	DustHandle hTagInline;

	DustHandle hTypeResp;
	DustHandle hTypeTable;
	DustHandle hTypeCell;

	DustHandle hTypeStyle;
	DustHandle hAttStyleDef;

	DustHandle hUnit;
	DustHandle hDoc;
	DustHandle hCurrentLayout;

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
			String txt = (uo instanceof DustHandle) ? getNodeText((DustHandle) uo) : DustUtils.toString(uo);

			Component r = super.getTreeCellRendererComponent(tree, txt, sel, expanded, leaf, row, hasFocus);

			return r;
		};
	};

	ListCellRenderer listRenderer = new DefaultListCellRenderer() {
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if (value instanceof DustHandle) {
				DustHandle h = (DustHandle) value;
				value = Dust.access(DustAccess.Peek, h.getId(), h, TOKEN_NAME);
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

	Map<String, String> styles = new TreeMap<>();

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

			Map<String, Object> sp = null;
			TreePath[] tps;

			DustHandle hThis = selMgr.getFocusedBlock();
			DustHandle hParent = selMgr.getFocusedParent();

			boolean refresh = false;

			try {

				switch (cmd) {
				case "Rebuild":
					buildGui();
					break;
				case "Reset":
					Dust.access(DustAccess.Reset, null, hDoc, TOKEN_MEMBERS);
					refresh = true;
					break;
				case "Layout":
					hCurrentLayout = (DustHandle) ((JComboBox) e.getSource()).getSelectedItem();
					refresh = true;
					break;
				case "Load":
					sp = new HashMap<String, Object>();
					sp.put(TOKEN_CMD, TOKEN_CMD_LOAD);
					break;
				case "Save":
					sp = new HashMap<String, Object>();
					sp.put(TOKEN_CMD, TOKEN_CMD_SAVE);
					break;

				case "<-":
					updateStruct();
					break;
				case "->":
					updateDocEditor();
					break;

				case "Delete":
					tps = docStruct.getSelectionPaths();
					if (null != tps) {
						for (TreePath tp : tps) {
							DustHandle parent = (DustHandle) ((DefaultMutableTreeNode) tp.getParentPath().getLastPathComponent()).getUserObject();
							DustHandle node = (DustHandle) ((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject();

							Dust.access(DustAccess.Delete, node, parent, TOKEN_MEMBERS, KEY_MEMBEROF);
							Dust.access(DustAccess.Insert, node, parent, TOKEN_TEXT_ORPHANS);
						}

						refresh = true;
					}
					break;
				case "UnderFirst":
					tps = docStruct.getSelectionPaths();
					if (null != tps) {
						DustHandle np = null;
						for (TreePath tp : tps) {
							DustHandle parent = (DustHandle) ((DefaultMutableTreeNode) tp.getParentPath().getLastPathComponent()).getUserObject();
							DustHandle node = (DustHandle) ((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject();

							if (null == np) {
								np = node;
							} else {
								Dust.access(DustAccess.Delete, node, parent, TOKEN_MEMBERS, KEY_MEMBEROF);
								Dust.access(DustAccess.Insert, node, np, TOKEN_MEMBERS, KEY_ADD);
							}
						}

						refresh = true;
					}

					break;
				case "Bullet": {
//				int cp = docEditor.getCaretPosition();
//
//				Element ep = doc.getParagraphElement(cp);
//				String idThis = (String) ep.getAttributes().getAttribute(HTML.Attribute.ID);
//				DustHandle hThis = Dust.getHandle(hUnit, null, idThis, DustOptCreate.None);
//
//				Dust.log(TOKEN_LEVEL_TRACE, accessText(DustAccess.Peek, "??", idThis));

					Dust.access(DustAccess.Set, hTagBullet, selMgr.getFocusedBlock(), TOKEN_TEXT_GROUP);

					refresh = true;
				}
					break;
				case "Number": {
//				int cp = docEditor.getCaretPosition();
//
//				Element ep = doc.getParagraphElement(cp);
//				String idThis = (String) ep.getAttributes().getAttribute(HTML.Attribute.ID);
//				DustHandle hThis = Dust.getHandle(hUnit, null, idThis, DustOptCreate.None);

					Dust.access(DustAccess.Set, hTagNumber, selMgr.getFocusedBlock(), TOKEN_TEXT_GROUP);

					refresh = true;
				}
					break;
				case "Table": {
					String resp = JOptionPane.showInputDialog("Table dimensions? row / col / hdrRow / hdrCol");
					if (null == resp) {
						return;
					}

					String[] dim = DustUtils.isEmpty(resp) ? new String[] {} : resp.split("/");

					Long dataRows = Long.valueOf(DustUtils.optGet(dim, 0, "3").trim());
					Long dataCols = Long.valueOf(DustUtils.optGet(dim, 1, "4").trim());
					Long headRows = Long.valueOf(DustUtils.optGet(dim, 2, "2").trim());
					Long headCols = Long.valueOf(DustUtils.optGet(dim, 3, "2").trim());
					Long cols = dataCols + headCols;
					Long rows = dataRows + headRows;

					ArrayList<Long> tblSpan = new ArrayList<>();
					tblSpan.add(rows);
					tblSpan.add(cols);

					ArrayList<Long> tblDataOffset = new ArrayList<>();
					tblDataOffset.add(headRows);
					tblDataOffset.add(headCols);

//				Element eThis = doc.getParagraphElement(docEditor.getCaretPosition());
//				String idThis = (String) eThis.getAttributes().getAttribute(HTML.Attribute.ID);
//				DustHandle hThis = Dust.getHandle(hUnit, null, idThis, DustOptCreate.None);
//
//				String idParent = idThis;
//				for (Element p = eThis.getParentElement(); DustUtils.isEqual(idThis, idParent); p = p.getParentElement()) {
//					idParent = (String) p.getAttributes().getAttribute(HTML.Attribute.ID);
//				}
//				DustHandle hParent = Dust.getHandle(hUnit, null, idParent, DustOptCreate.None);

					int idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);

					DustHandle hTbl = Dust.getHandle(hUnit, hTypeTable, null, DustOptCreate.Primary);
					Dust.access(DustAccess.Set, hCurrentLayout, hTbl, TOKEN_LAYOUT_LAYOUT);
					Dust.access(DustAccess.Set, tblSpan, hTbl, TOKEN_SPAN);
					Dust.access(DustAccess.Set, tblDataOffset, hTbl, TOKEN_POSITION);

					DustHandle hResp = Dust.getHandle(hUnit, hTypeResp, null, DustOptCreate.Primary);
					Dust.access(DustAccess.Insert, hTbl, hResp, TOKEN_OPTIONS);
					Dust.access(DustAccess.Insert, hTbl, hResp, TOKEN_MEMBERS, KEY_ADD);

					Dust.access(DustAccess.Insert, hResp, hParent, TOKEN_MEMBERS, idx + 1);

					ArrayList<Long> pos = new ArrayList<>();
					pos.add(0L);
					pos.add(0L);

					for (long c = 1; c <= dataCols; ++c) {
						pos.set(1, c);
						for (long i = 1; i <= rows; ++i) {
							pos.set(0, i);
							addCell(hResp, hTbl, pos, ((i <= headRows) ? "hdrRow " : (c <= headCols) ? "hdrCol " : "Cell ") + pos);
						}
					}

					refresh = true;
				}
					break;
				case "Local": {
					int sb = docEditor.getSelectionStart();
					int se = docEditor.getSelectionEnd();

					int eb = selMgr.eFocus.getStartOffset();
					int ee = selMgr.eFocus.getEndOffset();

					boolean inlineNow = Dust.access(DustAccess.Check, hTagInline, hParent, TOKEN_TEXT_GROUP);

					int idx;
					if (!inlineNow) {
						Dust.access(DustAccess.Set, hTagInline, hThis, TOKEN_TEXT_GROUP);
						accessText(DustAccess.Set, null, hThis.getId());
						hParent = hThis;

						hThis = Dust.getHandle(hUnit, hTypeBlock, null, DustOptCreate.Primary);
						Dust.access(DustAccess.Insert, hThis, hParent, TOKEN_MEMBERS, KEY_ADD);
						idx = 0;
					} else {
						idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);
					}

					String before = docEditor.getText(eb, sb - eb).trim();
					String txt = docEditor.getSelectedText();
					String after = docEditor.getText(se, ee - se).trim();

					int sl = txt.length();
					for (int o = 0; Character.isWhitespace(txt.charAt(o)) && (o < sl); ++o) {
						++sb;
					}
					for (int o = sl - 1; Character.isWhitespace(txt.charAt(o)) && (o >= 0); --o) {
						--se;
					}

					txt = docEditor.getText(sb, se - sb);

					if (!DustUtils.isEmpty(before)) {
						hThis = optAddText(hParent, idx++, hThis, before);
					}
					if (!DustUtils.isEmpty(txt)) {
						hThis = optAddText(hParent, idx++, hThis, txt);
					}
					if (!DustUtils.isEmpty(after)) {
						hThis = optAddText(hParent, idx++, hThis, after);
					}

					refresh = true;

				}
					break;
				case "PnlHtml":
					Dust.log(TOKEN_LEVEL_INFO, docEditor.getText());
					break;
				case "GenHtml":
					Dust.log(TOKEN_LEVEL_INFO, htmlGen.generateHtml(hDoc));
					break;
				default:
					Dust.log(TOKEN_LEVEL_WARNING, "Command not handled", cmd);
				}

				if (null != sp) {
					DustHandle app = Dust.getUnit("sandbox.1", false);
					DustHandle mind = Dust.getHandle(app, null, TOKEN_MIND, DustOptCreate.None);
					DustHandle defaultSerializer = Dust.access(DustAccess.Peek, null, mind, TOKEN_SERIALIZER);

					sp.put(TOKEN_KEY, hUnit.getId());
					sp.put(TOKEN_DATA, hUnit);
					Dust.access(DustAccess.Process, sp, defaultSerializer);
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

		private DustHandle optAddText(DustHandle hParent, int idx, DustHandle hTxt, String txt) {
			if (!DustUtils.isEmpty(txt)) {
				if (null == hTxt) {
					hTxt = Dust.getHandle(hUnit, hTypeBlock, null, DustOptCreate.Primary);
					Dust.access(DustAccess.Insert, hTxt, hParent, TOKEN_MEMBERS, idx);
				}
				accessText(DustAccess.Set, txt, hTxt.getId());
				hTxt = null;
			}

			return hTxt;
		}

		public void addCell(DustHandle hResp, DustHandle hTbl, ArrayList<Long> pos, String txt) {
			DustHandle hTxt = Dust.getHandle(hUnit, hTypeBlock, null, DustOptCreate.Primary);
			accessText(DustAccess.Set, txt, hTxt.getId());
			Dust.access(DustAccess.Insert, hTxt, hResp, TOKEN_MEMBERS, KEY_ADD);

			DustHandle hCell = Dust.getHandle(hUnit, hTypeCell, null, DustOptCreate.Primary);
			Dust.access(DustAccess.Set, new ArrayList<Long>(pos), hCell, TOKEN_POSITION);
			Dust.access(DustAccess.Set, hTxt, hCell, TOKEN_TARGET);

			Dust.access(DustAccess.Insert, hCell, hTbl, TOKEN_MEMBERS, KEY_ADD);
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
							Image image = (Image) o;

							int width = image.getWidth(null);
							int height = image.getHeight(null);
							BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
							Graphics g = bi.getGraphics();
							g.drawImage(image, 0, 0, null);
							ImageIO.write(bi, "jpg", new File("localStore/save/temp.jpg"));

							return true;
						}
					}

					if ("text".equals(pt)) {
						if ("plain".equals(st)) {
							Object o = t.getTransferData(df);
							if (o instanceof String) {
								String str = (String) o;
//								int cp = docEditor.getCaretPosition();

								if (-1 == str.indexOf("\n")) {
									doc.insertString(selMgr.getCaretPos(), str, null);
									return true;
								} else {
//									Element ep = doc.getParagraphElement(cp);
//									String idThis = (String) ep.getAttributes().getAttribute(HTML.Attribute.ID);
//									DustHandle hThis = Dust.getHandle(hUnit, null, idThis, DustOptCreate.None);
//									Element p = ep.getParentElement();
//									String idParent = (String) p.getAttributes().getAttribute(HTML.Attribute.ID);
//									DustHandle hParent = Dust.getHandle(hUnit, null, idParent, DustOptCreate.None);

									DustHandle hThis = selMgr.getFocusedBlock();
									DustHandle hParent = selMgr.getFocusedParent();

									if (null == hParent) {
										hParent = hDoc;
									}

									int len = Dust.access(DustAccess.Peek, 0, hParent, TOKEN_MEMBERS, KEY_SIZE);
									int idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);

									boolean insert = (++idx < len);
									boolean override = DustUtils.isEqual(hThis, hParent) ? false : DustUtils.isEmpty(accessText(DustAccess.Peek, null, hThis));

									String[] lines = str.split("\n");
									StringBuilder sb = new StringBuilder();
//									String eol = ".:?!";

									for (String l : lines) {
										l = l.trim();
										if (0 < l.length()) {
											DustUtils.sbAppend(sb, " ", false, l);
//											sb.append(" ").append(l);

//											char ce = l.charAt(l.length() - 1);

//											if (-1 != eol.indexOf(ce)) 
											{
												String id;

												if (override) {
													id = hThis.getId();
													override = false;
												} else {
													id = null;
												}
												DustHandle h = Dust.getHandle(hUnit, hTypeBlock, id, DustOptCreate.Primary);
												accessText(DustAccess.Set, sb.toString(), h.getId());
												sb.setLength(0);

//											appendHandle(h, sb);

												if (override) {
													override = false;
												} else {
													Dust.access(DustAccess.Insert, h, hParent, TOKEN_MEMBERS, insert ? (idx++) : KEY_ADD);
												}
											}
										}
									}

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

							int idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);

							DustHandle hNew = Dust.getHandle(hUnit, hTypeBlock, null, DustOptCreate.Primary);
							Dust.access(DustAccess.Insert, hNew, hParent, TOKEN_MEMBERS, idx + 1);

							accessText(DustAccess.Set, remain, hThis.getId());
							accessText(DustAccess.Set, move, hNew.getId());

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

//		docEditor.setSelectionStart(10);
//		docEditor.setSelectionEnd(40);
	};

	@Override
	protected void init() throws Exception {

		hTypeDoc = DustUtils.getMindMeta(TOKEN_TEXT_DOC);
		hTypeBlock = DustUtils.getMindMeta(TOKEN_TEXT_BLOCK);
		DustHandle hTag = DustUtils.getMindMeta(TOKEN_KBMETA_TAG);
		hTagBullet = Dust.getHandle(null, hTag, TOKEN_TEXT_GROUP_BULLET, DustOptCreate.Meta);
		hTagNumber = Dust.getHandle(null, hTag, TOKEN_TEXT_GROUP_NUMBER, DustOptCreate.Meta);
		hTagInline = Dust.getHandle(null, hTag, TOKEN_TEXT_GROUP_INLINE, DustOptCreate.Meta);

		hTypeResp = DustUtils.getMindMeta(TOKEN_LAYOUT_RESPONSIVE);
		hTypeTable = DustUtils.getMindMeta(TOKEN_LAYOUT_TABLE);
		hTypeCell = DustUtils.getMindMeta(TOKEN_LAYOUT_CELL);

		hCurrentLayout = Dust.access(DustAccess.Peek, null, null, TOKEN_LAYOUT_LAYOUT);

		hTypeStyle = DustUtils.getMindMeta(TOKEN_TEXT_STYLE);
		DustHandle hAtt = DustUtils.getMindMeta(TOKEN_KBMETA_ATTRIBUTE);
		hAttStyleDef = Dust.getHandle(null, hAtt, TOKEN_TEXT_STYLE_DEF, DustOptCreate.Meta);

		hUnit = Dust.getUnit("test.1", true);
		hDoc = null;
		styles.clear();

		for (DustHandle h : DustMindUtils.getUnitMembers(hUnit)) {
			DustHandle ht = h.getType();

			if (hTypeDoc == ht) {
				hDoc = h;
			} else if (hTypeStyle == ht) {
				String name = Dust.access(DustAccess.Peek, "", h, TOKEN_NAME);
				Map<String, String> def = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, h, TOKEN_TEXT_STYLE_DEF);

				if (!DustUtils.isEmpty(name) && !def.isEmpty()) {
					StringBuilder sb = null;
					for (Map.Entry<String, String> de : def.entrySet()) {
						sb = DustUtils.sbAppend(sb, "", false, de.getKey(), ": ", de.getValue(), "; ");
					}
					styles.put(name, sb.toString());
				}
			}
		}

		if (null == hDoc) {
			hDoc = Dust.getHandle(hUnit, hTypeDoc, null, DustOptCreate.Primary);
		}

		DustGuiSwingUtils.optSetLookAndFeel();

		frm = new JFrame();

		frm.setTitle(Dust.access(DustAccess.Peek, "Text editor", null, TOKEN_NAME));

		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frm.setBounds(100, 100, 1000, 800);

		docEditor = new JEditorPane();
		docEditor.setContentType("text/html");
		docEditor.setTransferHandler(cbTransferHandler);

		doc = (HTMLDocument) docEditor.getDocument();
		File f = new File("localStore");
		URL url = f.toURI().toURL();

		doc.setBase(url);
		doc.setDocumentFilter(df);

		htmlGen = new DustSandboxTextHtmlGenerator(this);

		docPreview = new JTextPane();
		docPreview.setContentType("text/html");
		docPreview.setEditable(false);
		docPreview.setDocument(doc);

		docStruct = new JTree(structModel);
		docStruct.setRootVisible(false);
		docStruct.setCellRenderer(structRenderer);

		selMgr = new DustSandboxTextSelectionManager(hUnit, doc);
		selMgr.attach(docStruct);
		selMgr.attach(docEditor);
		selMgr.attach(docPreview);

		factToolbars.get("tbTop", BoxLayout.LINE_AXIS);

		buildGui();
		updateDocEditor();
		updateStruct();

		frm.setVisible(true);
	};

	void updateDocEditor() {
		String htmlContent = htmlGen.generateHtml(hDoc);
		selMgr.reset();
		docEditor.setText(htmlContent);
		docPreview.setText(htmlContent);
	};

	public void followTextNavInStructTree(JTextComponent txt) {
//		int cp = txt.getCaretPosition();
//		Element eSel = doc.getParagraphElement(cp);
//		String idThis = (String) eSel.getAttributes().getAttribute(HTML.Attribute.ID);
//		if (DustUtils.isEmpty(idThis)) {
//			return;
//		}
//
//		DustHandle hThis = Dust.getHandle(hUnit, null, idThis, DustOptCreate.None);
		DustHandle hThis = selMgr.getFocusedBlock();

		TreePath tp = null;
		for (Enumeration<TreeNode> se = rootNode.depthFirstEnumeration(); se.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) se.nextElement();
			if (hThis == node.getUserObject()) {
				tp = new TreePath(node.getPath());
				docStruct.setSelectionPath(tp);
				docStruct.scrollPathToVisible(tp);
			}
		}
	}

	String accessText(DustAccess access, String val, Object key) {
		DustHandle hNode = Dust.getHandle(hUnit, hTypeBlock, (String) key, DustOptCreate.None);

		return Dust.access(access, val, hNode, TOKEN_TEXT_TEXT);
//		return Dust.access(access, val, texts, key);
	};

	void buildGui() {
		frm.getContentPane().removeAll();

		JPanel left = new JPanel(new BorderLayout());
		left.add(new JScrollPane(docStruct), BorderLayout.CENTER);
//		left.add(factToolbars.get("tbStruct"), BorderLayout.EAST);

		JTabbedPane tpLeft = new JTabbedPane();
		tpLeft.add("Struct", left);
		tpLeft.add("Styles", new JLabel("Style editor"));
		tpLeft.add("Resources", new JLabel("Resource editor"));

		JPanel right = new JPanel(new BorderLayout());
		JScrollPane scpDocEd = new JScrollPane(docEditor);
//		scpDocEd.getViewport().addChangeListener(docScrollListener);
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
//		fillToolbar("tbStruct", "Delete", "UnderFirst", null, "Bullet", "Number", "Local", null, "Table", "Merge", null);
		fillToolbar("tbDoc", "<-", "->", null, "Delete", "UnderFirst", null, "Bullet", "Number", "Local", null, "Table", "Merge", null, "PnlHtml", "GenHtml");

		frm.getContentPane().revalidate();

	}

	private JComboBox createCombo(String cmd, Object field) {
		Vector lv = new Vector(Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, null, field));		
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

		loadNode(rootNode, hDoc);

		structModel.nodeStructureChanged(rootNode);

	}

	public DefaultMutableTreeNode loadNode(DefaultMutableTreeNode p, DustHandle h) {
		p.setUserObject(h);

		for (DustHandle hc : (Collection<DustHandle>) Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, h, TOKEN_MEMBERS)) {
			p.add(loadNode(new DefaultMutableTreeNode(), hc));
		}

		return p;
	}

	public String getNodeText(DustHandle h) {
		DustHandle ht = h.getType();
		int c = Dust.access(DustAccess.Peek, 0, h, TOKEN_MEMBERS, KEY_SIZE);
		String ph = (0 < c) ? "Inline group" : "placeholder";
		String txt = accessText(DustAccess.Peek, (hTypeBlock == ht) ? ph : ht.getId(), h.getId());
		int maxLen = 50;
		if (txt.length() > maxLen) {
			txt = txt.substring(0, maxLen - 3) + "...";
		} else {
			txt = txt.trim();
		}
		return txt;
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
