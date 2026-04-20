package me.giskard.dust.sandbox.text;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.text.AbstractDocument.LeafElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.DustException;
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

	DustHandle hUnit;
	DustHandle hDoc;

	JFrame frm;

	JTree docStruct;
	DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("test");
	DefaultTreeModel structModel = new DefaultTreeModel(rootNode);

	JEditorPane docEditor;
	HTMLDocument doc;
	Map<Object, String> texts = new HashMap<>();

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

			switch (cmd) {
			case "Rebuild":
				buildGui();
				break;
			case "Reset":
				Dust.access(DustAccess.Reset, null, hDoc, TOKEN_MEMBERS);
				updateDocEditor();
				updateStruct();
				break;
			case "<-":
				updateStruct();
				break;
			case "->":
				updateDocEditor();
				break;
			case "Doc 1":
				String full = docEditor.getText();
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
				Dust.log(TOKEN_LEVEL_INFO, docEditor.getText());
				break;
			default:
				Dust.log(TOKEN_LEVEL_WARNING, "Command not handled", cmd);
			}
		}
	};

	TransferHandler cbTransferHandler = new TransferHandler() {

		private static final long serialVersionUID = 1L;

		@Override
		public boolean importData(TransferSupport support) {
			Transferable t = support.getTransferable();

			for (DataFlavor df : support.getDataFlavors()) {
				if ("text".equals(df.getPrimaryType())) {
					if ("plain".equals(df.getSubType())) {
						try {
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
//									StringBuilder sb = new StringBuilder();
									for (String l : lines) {
										l = l.trim();
										if (0 < l.length()) {
											String id;

											if (override) {
												id = idThis;
												override = false;
											} else {
												id = DustUtilsData.getNewId(hUnit);
											}
											DustHandle h = Dust.getHandle(hUnit, hTypeBlock, id, DustOptCreate.Primary);
											accessText(DustAccess.Set, l, h.getId());
//											appendHandle(h, sb);

											if (override) {
												override = false;
											} else {
												Dust.access(DustAccess.Insert, h, hParent, TOKEN_MEMBERS, insert ? (idx++) : KEY_ADD);
											}
										}
									}

									updateDocEditor();
									updateStruct();

//									String htmlFragment = sb.toString();
//									String txt = docEditor.getText();

//									Element ec = doc.getCharacterElement(cp);
//
//									Object id = ep.getAttributes().getAttribute(HTML.Attribute.ID);
//
//									SimpleAttributeSet sas = (SimpleAttributeSet) ec.getAttributes().getAttribute(HTML.Tag.SPAN);
//									Object name = (null == sas) ? null : sas.getAttribute(HTML.Attribute.NAME);
//
//									Toolkit.getDefaultToolkit().beep();
//
//									if (DustUtils.isEqual("placeholder", name)) {
//										int so = ec.getStartOffset();
//										int l = ec.getEndOffset() - so;
//										doc.replace(so, l, htmlFragment, null);
//									} else {
//										doc.insertBeforeEnd(ep, htmlFragment);
//									}

								}

							}
						} catch (Throwable e) {
							DustException.wrap(e, TOKEN_LEVEL_INFO, "DF", df.getPrimaryType(), df.getSubType(), e);
						}
					}
				}
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
			String s = doc.getText(offset, length);
			super.remove(fb, offset, length);
		}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
			boolean skip = false;

			if (text.contains("\n")) {
				if (text.length() == 1) {
					int l = doc.getLength();
					int to = Math.max(0, offset - 2);
					int tl = Math.min(l - to + 1, 4);

					String s = doc.getText(to, tl);

					skip = -1 != s.indexOf("\n\n");
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

		hUnit = Dust.getUnit("test.1", true);

		hTypeDoc = DustUtilsData.getMindMeta(TOKEN_TEXT_DOC);
		hTypeBlock = DustUtilsData.getMindMeta(TOKEN_TEXT_BLOCK);

		String id = DustUtilsData.getNewId(hUnit);

		hDoc = Dust.getHandle(hUnit, hTypeDoc, id, DustOptCreate.Primary);

//		FlatLightLaf

		DustGuiSwingUtils.optSetLookAndFeel();

		frm = new JFrame();

		frm.setTitle(Dust.access(DustAccess.Peek, "Text editor", null, TOKEN_NAME));

		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frm.setBounds(100, 100, 800, 400);

		docEditor = new JEditorPane();
		docEditor.setTransferHandler(cbTransferHandler);
		docEditor.setContentType("text/html");

		doc = (HTMLDocument) docEditor.getDocument();

		doc.setDocumentFilter(df);

		docStruct = new JTree(structModel);

		factToolbars.get("tbTop", BoxLayout.LINE_AXIS);

		buildGui();
		updateDocEditor();
		updateStruct();

		frm.setVisible(true);
	};

	void updateDocEditor() {
		String htmlContent = generateHtml();
		docEditor.setText(htmlContent);
	};

	String accessText(DustAccess access, String val, Object key) {
		return Dust.access(access, val, texts, key);
	};

	String generateHtml() {
		StringBuilder sb = new StringBuilder();

		sb.append("<html><head>\n");
		sb.append("<style>");

		sb.append(".emphasis { font-weight: bold; padding-bottom: 5px; }\n");

		sb.append("</style>");

		sb.append("</head><body>\n");

		appendHandle(hDoc, sb);

		sb.append("</body>");

		sb.append("</html>");

		return sb.toString();
	};

	String placeholder = "<span name=\"placeholder\">PLACEHOLDER</span>";

	void appendHandle(DustHandle h, StringBuilder sb) {
		String id = h.getId();
		boolean empty = true;

		DustUtils.sbAppend(sb, "", false, "<div id=\"", id, "\">\n");

		String txt = accessText(DustAccess.Peek, null, id);

		if (null != txt) {
			sb.append(DustStreamUtils.escapeHTML(txt));
			empty = false;
		}

		for (DustHandle hc : (Collection<DustHandle>) Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, h, TOKEN_MEMBERS)) {
			appendHandle(hc, sb);
			empty = false;
		}

		if (empty) {
			sb.append(placeholder);
		}

		sb.append("</div>\n");
	};

	void buildGui() {
		frm.getContentPane().removeAll();

		JPanel left = new JPanel(new BorderLayout());
		left.add(DustGuiSwingUtils.setTitle(new JScrollPane(docStruct), "Structure"), BorderLayout.CENTER);
		left.add(factToolbars.get("tbStruct"), BorderLayout.EAST);

		JPanel right = new JPanel(new BorderLayout());
		right.add(DustGuiSwingUtils.setTitle(new JScrollPane(docEditor), "Document"), BorderLayout.CENTER);
		right.add(factToolbars.get("tbDoc"), BorderLayout.EAST);

		JPanel pnlMain = new JPanel(new BorderLayout());
		pnlMain.add(factToolbars.get("tbTop"), BorderLayout.NORTH);
		pnlMain.add(DustGuiSwingUtils.createSplit(true, left, right, 0.3), BorderLayout.CENTER);

		frm.getContentPane().add(pnlMain);

		fillToolbar("tbTop", "Rebuild", "Reset");
		fillToolbar("tbStruct", "<-", "->");
		fillToolbar("tbDoc", "Doc 1", "Doc 2");

		frm.getContentPane().revalidate();

		docStruct.setRootVisible(true);

	}

	void fillToolbar(String tb, String... cmds) {
		JComponent pnl = factToolbars.get(tb);
		pnl.removeAll();

		for (String cmd : cmds) {
			pnl.add(factActionControls.get(cmd));
		}
	}

	public void updateStruct() {
		rootNode.removeAllChildren();

		loadNode(rootNode, hDoc);

//		Element eRoot = doc.getElement(hDoc.getId());
//		loadElement(rootNode, eRoot);
//
//		Element eBody = null;
//		for ( int i = 0; i < eRoot.getElementCount(); ++i ) {
//			eBody = eRoot.getElement(i);
//			
//			String n = eBody.getName();
//			if ( "body".equals(n)) {
//				rootNode.removeAllChildren();
//				loadElement(rootNode, eBody);
//				structModel.nodeStructureChanged(rootNode);
//				return;
//			}
//		}

		structModel.nodeStructureChanged(rootNode);

	}

	public DefaultMutableTreeNode loadNode(DefaultMutableTreeNode p, DustHandle h) {
		String txt = accessText(DustAccess.Peek, "placeholder", h.getId());
		int maxLen = 50;
		if (txt.length() > maxLen) {
			txt = txt.substring(0, maxLen - 3) + "...";
		} else {
			txt = txt.trim();
		}
		p.setUserObject(txt);

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
