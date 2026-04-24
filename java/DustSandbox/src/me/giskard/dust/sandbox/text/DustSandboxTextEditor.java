package me.giskard.dust.sandbox.text;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.AbstractDocument.LeafElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.mind.DustMindUtils;
import me.giskard.dust.core.net.DustNetConsts;
import me.giskard.dust.core.stream.DustStreamUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsData;
import me.giskard.dust.core.utils.DustUtilsFactory;
import me.giskard.dust.mod.gui.swing.DustGuiSwingUtils;

@SuppressWarnings({ "unchecked" })
public class DustSandboxTextEditor extends DustAgent implements DustConsts, DustNetConsts {

	DustHandle hTypeDoc;
	DustHandle hTypeBlock;
	DustHandle hTagBullet;
	DustHandle hTagNumber;

	DustHandle hTypeResp;
	DustHandle hTypeTable;
	DustHandle hTypeCell;
	DustHandle hTagLayoutPage;

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

	JEditorPane docEditor;
	JTextPane docPreview;
	HTMLDocument doc;
//	Map<Object, String> texts = new HashMap<>();

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

	TreeSelectionListener docStructSelListener = new TreeSelectionListener() {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			if (!updating) {
				TreePath sp = e.getNewLeadSelectionPath();
				if (null == sp) {
					return;
				}
				try {
					updating = true;

					DefaultMutableTreeNode n = (DefaultMutableTreeNode) sp.getLastPathComponent();
					DustHandle h = (DustHandle) n.getUserObject();

					h = Dust.access(DustAccess.Peek, h, h, TOKEN_TARGET);
					String id = h.getId();

					Element eSel = doc.getElement(id);

					docEditor.setCaretPosition(eSel.getStartOffset());
					docEditor.moveCaretPosition(eSel.getEndOffset() - 1);

					docEditor.requestFocusInWindow();
				} finally {
					updating = false;
				}
			}
		}
	};

//	ChangeListener docScrollListener = new ChangeListener() {
//		@Override
//		public void stateChanged(ChangeEvent e) {
//			if (!updating) {
//				try {
//					updating = true;
//					
//					e.get
//
//					JTextComponent txt = (JTextComponent) ((JViewport) e.getSource()).getView();
//
//					SwingUtilities.invokeLater(new Runnable() {
//						@Override
//						public void run() {
//							followTextNavInStructTree(txt);
//						}
//					});
//					
//
//				} finally {
//					updating = false;
//				}
//			}
//		}
//	};

	ActionListener al = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();

			Map<String, Object> sp = null;
			TreePath[] tps;

			boolean refresh = false;

			switch (cmd) {
			case "Rebuild":
				buildGui();
				break;
			case "Reset":
				Dust.access(DustAccess.Reset, null, hDoc, TOKEN_MEMBERS);
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
				int cp = docEditor.getCaretPosition();

				Element ep = doc.getParagraphElement(cp);
				String idThis = (String) ep.getAttributes().getAttribute(HTML.Attribute.ID);
				DustHandle hThis = Dust.getHandle(hUnit, null, idThis, DustOptCreate.None);

				Dust.log(TOKEN_LEVEL_TRACE, accessText(DustAccess.Peek, "??", idThis));

				Dust.access(DustAccess.Set, hTagBullet, hThis, TOKEN_TEXT_GROUP);

				refresh = true;
			}
				break;
			case "Number": {
				int cp = docEditor.getCaretPosition();

				Element ep = doc.getParagraphElement(cp);
				String idThis = (String) ep.getAttributes().getAttribute(HTML.Attribute.ID);
				DustHandle hThis = Dust.getHandle(hUnit, null, idThis, DustOptCreate.None);

				Dust.access(DustAccess.Set, hTagNumber, hThis, TOKEN_TEXT_GROUP);

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

				Element eThis = doc.getParagraphElement(docEditor.getCaretPosition());
				String idThis = (String) eThis.getAttributes().getAttribute(HTML.Attribute.ID);
				DustHandle hThis = Dust.getHandle(hUnit, null, idThis, DustOptCreate.None);

				String idParent = idThis;
				for (Element p = eThis.getParentElement(); DustUtils.isEqual(idThis, idParent); p = p.getParentElement()) {
					idParent = (String) p.getAttributes().getAttribute(HTML.Attribute.ID);
				}
				DustHandle hParent = Dust.getHandle(hUnit, null, idParent, DustOptCreate.None);
				int idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);

				DustHandle hTbl = Dust.getHandle(hUnit, hTypeTable, DustUtilsData.getNewId(hUnit), DustOptCreate.Primary);
				Dust.access(DustAccess.Set, hCurrentLayout, hTbl, TOKEN_LAYOUT_LAYOUT);
				Dust.access(DustAccess.Set, tblSpan, hTbl, TOKEN_SPAN);
				Dust.access(DustAccess.Set, tblDataOffset, hTbl, TOKEN_POSITION);

				DustHandle hResp = Dust.getHandle(hUnit, hTypeResp, DustUtilsData.getNewId(hUnit), DustOptCreate.Primary);
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
//							String idNew = DustUtilsData.getNewId(hUnit);
//							DustHandle hNew = Dust.getHandle(hUnit, hTypeBlock, idNew, DustOptCreate.Primary);
//							accessText(DustAccess.Set, "Cell " + pos, hNew.getId());
//							Dust.access(DustAccess.Set, new ArrayList<Long>(pos), hNew, TOKEN_POSITION);
//							Dust.access(DustAccess.Insert, hNew, hTbl, TOKEN_MEMBERS, KEY_ADD);
					}
				}

				refresh = true;
			}
				break;
			case "<-":
				updateStruct();
				break;
			case "->":
				updateDocEditor();
				break;
			case "Doc 1":
//				String full = docEditor.getText();
				String sel = docEditor.getSelectedText();

				int begin = docEditor.getSelectionStart();
				int end = docEditor.getSelectionEnd();

				try {
					String s2 = doc.getText(begin, end - begin);

					Segment txt = new Segment();
					txt.setPartialReturn(true);
					doc.getText(begin, end - begin, txt);

					Dust.log(TOKEN_LEVEL_TRACE, "editor sel", sel, "doc sel", s2);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				break;
			case "Doc 2":
				Dust.log(TOKEN_LEVEL_INFO, generateHtml());
//				Dust.log(TOKEN_LEVEL_INFO, docEditor.getText());
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
				updateDocEditor();
				updateStruct();
			}
		}

		public void addCell(DustHandle hResp, DustHandle hTbl, ArrayList<Long> pos, String txt) {
			String idTxt = DustUtilsData.getNewId(hUnit);
			DustHandle hTxt = Dust.getHandle(hUnit, hTypeBlock, idTxt, DustOptCreate.Primary);
			accessText(DustAccess.Set, txt, hTxt.getId());
			Dust.access(DustAccess.Insert, hTxt, hResp, TOKEN_MEMBERS, KEY_ADD);

			String idCell = DustUtilsData.getNewId(hUnit);
			DustHandle hCell = Dust.getHandle(hUnit, hTypeCell, idCell, DustOptCreate.Primary);
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
								int cp = docEditor.getCaretPosition();

								if (-1 == str.indexOf("\n")) {
									doc.insertString(cp, str, null);
									return true;
								} else {
									Element ep = doc.getParagraphElement(cp);
									String idThis = (String) ep.getAttributes().getAttribute(HTML.Attribute.ID);
									DustHandle hThis = Dust.getHandle(hUnit, null, idThis, DustOptCreate.None);
									Element p = ep.getParentElement();
									String idParent = (String) p.getAttributes().getAttribute(HTML.Attribute.ID);
									DustHandle hParent = Dust.getHandle(hUnit, null, idParent, DustOptCreate.None);

									int len = Dust.access(DustAccess.Peek, 0, hParent, TOKEN_MEMBERS, KEY_SIZE);
									int idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);

									boolean insert = (++idx < len);
									boolean override = DustUtils.isEqual(idParent, idThis) ? false : DustUtils.isEmpty(accessText(DustAccess.Peek, null, hThis));

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
													id = idThis;
													override = false;
												} else {
													id = DustUtilsData.getNewId(hUnit);
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

							Element eThis = doc.getParagraphElement(offset);
							String idThis = (String) eThis.getAttributes().getAttribute(HTML.Attribute.ID);
							DustHandle hThis = Dust.getHandle(hUnit, null, idThis, DustOptCreate.None);

							String idParent = idThis;
							for (Element p = eThis.getParentElement(); DustUtils.isEqual(idThis, idParent); p = p.getParentElement()) {
								idParent = (String) p.getAttributes().getAttribute(HTML.Attribute.ID);
							}
							DustHandle hParent = Dust.getHandle(hUnit, null, idParent, DustOptCreate.None);

							int idx = Dust.access(DustAccess.Peek, hThis, hParent, TOKEN_MEMBERS, KEY_INDEXOF);
							String idNew = DustUtilsData.getNewId(hUnit);

							DustHandle hNew = Dust.getHandle(hUnit, hTypeBlock, idNew, DustOptCreate.Primary);
							Dust.access(DustAccess.Insert, hNew, hParent, TOKEN_MEMBERS, idx + 1);

							accessText(DustAccess.Set, remain, idThis);
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

	boolean updating = false;

	CaretListener cl = new CaretListener() {

		@Override
		public void caretUpdate(CaretEvent ce) {
			if (updating) {
				return;
			}

			int b = ce.getMark();
			int e = ce.getDot();
			boolean rev = b > e;

			followTextNavInStructTree(docEditor);

			if (b == e) {
				Element ec = doc.getCharacterElement(b);

				SimpleAttributeSet sas = (SimpleAttributeSet) ec.getAttributes().getAttribute(HTML.Tag.SPAN);
				Object name = (null == sas) ? null : sas.getAttribute(HTML.Attribute.NAME);
				if (DustUtils.isEqual("placeholder", name)) {
					b = ec.getStartOffset();
					e = ec.getEndOffset();

					Dust.log(TOKEN_LEVEL_TRACE, "select", b, e, ec);

				} else {
					return;
				}
			} else {
				if (rev) {
					int a = b;
					b = e;
					e = a;
				}

				Element eb = doc.getParagraphElement(b);
				Element ee = doc.getParagraphElement(e);

				if (eb != ee) {
					b = eb.getStartOffset();
					e = ee.getEndOffset() - 1;
				}

				Dust.log(TOKEN_LEVEL_TRACE, "select", b, e, eb, ee);
			}
			try {
				updating = true;
				docEditor.setCaretPosition(rev ? e : b);
				docEditor.moveCaretPosition(rev ? b : e);
//				int cp = rev ? b : e;
//				if (-1 != cp) {
//					docEditor.carsetCaretPosition(cp);
//				}
//				docEditor.select(b, e);

			} finally {
				updating = false;
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

		hTypeDoc = DustUtilsData.getMindMeta(TOKEN_TEXT_DOC);
		hTypeBlock = DustUtilsData.getMindMeta(TOKEN_TEXT_BLOCK);
		DustHandle hTag = DustUtilsData.getMindMeta(TOKEN_KBMETA_TAG);
		hTagBullet = Dust.getHandle(null, hTag, TOKEN_TEXT_GROUP_BULLET, DustOptCreate.Meta);
		hTagNumber = Dust.getHandle(null, hTag, TOKEN_TEXT_GROUP_NUMBER, DustOptCreate.Meta);

		hTypeResp = DustUtilsData.getMindMeta(TOKEN_LAYOUT_RESPONSIVE);
		hTypeTable = DustUtilsData.getMindMeta(TOKEN_LAYOUT_TABLE);
		hTypeCell = DustUtilsData.getMindMeta(TOKEN_LAYOUT_CELL);
		hTagLayoutPage = Dust.getHandle(null, hTag, TOKEN_LAYOUT_LAYOUT_PAGE, DustOptCreate.Meta);

		hUnit = Dust.getUnit("test.1", true);
		hDoc = null;

		for (DustHandle h : DustMindUtils.getUnitMembers(hUnit)) {
			if (hTypeDoc == h.getType()) {
				hDoc = h;
				break;
			}
		}

		if (null == hDoc) {
			String id = DustUtilsData.getNewId(hUnit);
			hDoc = Dust.getHandle(hUnit, hTypeDoc, id, DustOptCreate.Primary);
		}

		DustGuiSwingUtils.optSetLookAndFeel();

		frm = new JFrame();

		frm.setTitle(Dust.access(DustAccess.Peek, "Text editor", null, TOKEN_NAME));

		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frm.setBounds(100, 100, 800, 400);

		docEditor = new JEditorPane();
		docEditor.setContentType("text/html");
		docEditor.setTransferHandler(cbTransferHandler);
		docEditor.addCaretListener(cl);

		doc = (HTMLDocument) docEditor.getDocument();
		doc.setDocumentFilter(df);

		docPreview = new JTextPane();
		docPreview.setContentType("text/html");

		docStruct = new JTree(structModel);
		docStruct.setRootVisible(false);
		docStruct.setCellRenderer(structRenderer);
		docStruct.addTreeSelectionListener(docStructSelListener);

		factToolbars.get("tbTop", BoxLayout.LINE_AXIS);

		buildGui();
		updateDocEditor();
		updateStruct();

		frm.setVisible(true);
	};

	void updateDocEditor() {
		String htmlContent = generateHtml();
		docEditor.setText(htmlContent);
		docPreview.setText(htmlContent);
	};

	public void followTextNavInStructTree(JTextComponent txt) {
		int cp = txt.getCaretPosition();
		Element eSel = doc.getParagraphElement(cp);
		String idThis = (String) eSel.getAttributes().getAttribute(HTML.Attribute.ID);
		if (DustUtils.isEmpty(idThis)) {
			return;
		}

		DustHandle hThis = Dust.getHandle(hUnit, null, idThis, DustOptCreate.None);

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

	String generateHtml() {
		StringBuilder sb = new StringBuilder();

		sb.append("<html><head>\n");
		sb.append("<style>");

		sb.append("div { padding-top: 5px; }\n");

		sb.append("</style>");

		sb.append("</head><body>\n");

		appendHandle(hDoc, sb, null);

		sb.append("</body>");

		sb.append("</html>");

		return sb.toString();
	};

	String placeholder = "<span name=\"placeholder\">&lt;type statement here&gt;</span>";
	String noLayout = "<span name=\"placeholder\">&lt;no layout found&gt;</span>";
	Stack<Integer> headStack = new Stack<Integer>();
	int lp = 0;

	void appendHandle(DustHandle h, StringBuilder sb, DustHandle specGroup) {
		String id = h.getId();
		Collection<DustHandle> members = Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, h, TOKEN_MEMBERS);
		boolean empty = members.isEmpty();
		String container = "div";

		DustHandle hLayoutParent = null;

		if (hTypeResp == h.getType()) {
			Collection<DustHandle> options = Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, h, TOKEN_OPTIONS);
			for (DustHandle ho : options) {
				DustHandle hl = Dust.access(DustAccess.Peek, hCurrentLayout, ho, TOKEN_LAYOUT_LAYOUT);
				if (hCurrentLayout == hl) {
					hLayoutParent = h;
					h = ho;
					members = Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, h, TOKEN_MEMBERS);
					break;
				}
			}

			if (null == hLayoutParent) {
				return;
			}
		}

		if (hTypeTable == h.getType()) {
			container = "table";
			DustUtilsFactory<Long, Map<Long, String>> factTableRows = new DustUtilsFactory.Simple<Long, Map<Long, String>>(true,
					(Class<? extends Map<Long, String>>) TreeMap.class);

			DustUtils.sbAppend(sb, "", false, "<", container, " id=\"", id, "\"", "style=\"padding-left:", lp, "px\" >\n");

			for (DustHandle hc : members) {
				ArrayList<Long> pos = Dust.access(DustAccess.Peek, null, hc, TOKEN_POSITION);
				StringBuilder sbc = new StringBuilder();
				DustHandle ht = Dust.access(DustAccess.Peek, null, hc, TOKEN_TARGET);
				appendHandle(ht, sbc, null);
				factTableRows.get(pos.get(0)).put(pos.get(1), sbc.toString());
			}

// will be needed for missing / spanned cells
//			ArrayList<Long> tblSpan = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, h, TOKEN_SPAN);

			ArrayList<Long> tblDataOffset = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, h, TOKEN_POSITION);

			Long headOffsetRow = tblDataOffset.get(0);
			Long headOffsetCol = tblDataOffset.get(1);

			if (0 < headOffsetRow) {
				sb.append("<thead>\n");
			} else {
				sb.append("<tbody>\n");
			}

			for (Long r : factTableRows.keys()) {
				sb.append("<tr>\n");

				for (Map.Entry<Long, String> ce : factTableRows.peek(r).entrySet()) {
					String cc = (r <= headOffsetRow) || (ce.getKey() <= headOffsetCol) ? "th" : "td";
					DustUtils.sbAppend(sb, "", false, "<", cc, "> ", ce.getValue(), "</", cc, ">\n");
				}

				sb.append("</tr>\n");

				if (r == headOffsetRow) {
					sb.append("</thead>\n<tbody>\n");
				}
			}
			sb.append("</tbody>\n");
		} else {
			int depth = headStack.size();
			boolean hdr = !empty && (0 < depth);
			int mi = 0;

			DustHandle hGroup = Dust.access(DustAccess.Peek, null, h, TOKEN_TEXT_GROUP);

			if (null != hGroup) {
				hdr = false;
			}

//		if (( 0 < depth ) && ( 0 > headStack.peek() )) {
//			container = "li";
//		}

			DustUtils.sbAppend(sb, "", false, "<", container, " id=\"", id, "\"", "style=\"padding-left:", lp, "px\" >\n");

			String txt = accessText(DustAccess.Peek, null, id);

			if (null != txt) {
				if (hdr) {
					StringBuilder headNum = null;
					for (Integer i : headStack) {
						if (null == headNum) {
							headNum = new StringBuilder(i.toString());
						} else {
							headNum.append(".").append(i);
						}
					}
					DustUtils.sbAppend(sb, "", false, "<h", depth, "> ", headNum, " ");
				} else if (null != specGroup) {
					String g = specGroup.getId();

					switch (g) {
					case TOKEN_TEXT_GROUP_BULLET:
						sb.append(" - ");
						break;
					case TOKEN_TEXT_GROUP_NUMBER:
						DustUtils.sbAppend(sb, "", false, " ", headStack.peek(), ". ");
						break;
					}
				}
				sb.append(DustStreamUtils.escapeHTML(txt));
				if (hdr) {
					DustUtils.sbAppend(sb, "", false, "</h", depth, ">");
				}

				empty = false;
			}

			for (DustHandle hc : members) {
				int cc = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, hc, TOKEN_MEMBERS, KEY_SIZE);
				if ((0 < cc) || (null != hGroup)) {
					try {
						++mi;
						headStack.push(mi);
						if (null != hGroup) {
							lp += 5;
						}
						appendHandle(hc, sb, hGroup);
					} finally {
						if (null != hGroup) {
							lp -= 5;
						}
						headStack.pop();
					}
				} else {
					appendHandle(hc, sb, hGroup);
				}
				empty = false;
			}

			if (empty) {
				sb.append(placeholder);
			}
		}

		sb.append("</" + container + ">\n");
	};

	void buildGui() {
		hCurrentLayout = hTagLayoutPage;

		frm.getContentPane().removeAll();

		JPanel left = new JPanel(new BorderLayout());
		left.add(new JScrollPane(docStruct), BorderLayout.CENTER);
		left.add(factToolbars.get("tbStruct"), BorderLayout.EAST);

		JTabbedPane tpLeft = new JTabbedPane();
		tpLeft.add("Struct", left);
		tpLeft.add("Styles", new JLabel("Style editor"));
		tpLeft.add("Resources", new JLabel("Resource editor"));

		JPanel right = new JPanel(new BorderLayout());
		JScrollPane scpDocEd = new JScrollPane(docEditor);
//		scpDocEd.getViewport().addChangeListener(docScrollListener);
		right.add(scpDocEd, BorderLayout.CENTER);
		right.add(factToolbars.get("tbDoc"), BorderLayout.EAST);

		JTabbedPane tpRight = new JTabbedPane();
		tpRight.add("Edit", right);
		tpRight.add("Test", new JScrollPane(docPreview));

		JPanel pnlMain = new JPanel(new BorderLayout());
		pnlMain.add(factToolbars.get("tbTop"), BorderLayout.NORTH);
		pnlMain.add(DustGuiSwingUtils.createSplit(true, tpLeft, tpRight, 0.3), BorderLayout.CENTER);

		frm.getContentPane().add(pnlMain);

		fillToolbar("tbTop", "Rebuild", "Reset", null, tfUnit, "Load", "Save");
		fillToolbar("tbStruct", "<-", "->", null, "Delete", "UnderFirst", null, "Bullet", "Number", null, "Table", "Merge");
		fillToolbar("tbDoc", "Doc 1", "Doc 2");

		frm.getContentPane().revalidate();

	}

	void fillToolbar(String tb, Object... cmds) {
		JComponent pnl = factToolbars.get(tb);
		pnl.removeAll();

		for (Object o : cmds) {
			if (null == o) {
				pnl.add(Box.createHorizontalStrut(10));
			} else if (o instanceof JComponent) {
				pnl.add((JComponent) o);
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
		String txt = accessText(DustAccess.Peek, (hTypeBlock == ht) ? "placeholder" : ht.getId(), h.getId());
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
