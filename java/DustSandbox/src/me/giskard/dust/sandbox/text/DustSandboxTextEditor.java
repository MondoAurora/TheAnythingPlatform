package me.giskard.dust.sandbox.text;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;
import javax.swing.text.html.HTMLDocument;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.net.DustNetConsts;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsData;
import me.giskard.dust.core.utils.DustUtilsFactory;
import me.giskard.dust.mod.gui.swing.DustGuiSwingUtils;

public class DustSandboxTextEditor extends DustAgent implements DustConsts, DustNetConsts {
	
	DustHandle hTypeDoc;
	DustHandle hTypeBlock;
	
	DustHandle hUnit;
	DustHandle hDoc;

	JFrame frm;
	JTree docStruct;
	JEditorPane docEditor;
	HTMLDocument doc;
	
	int headOffset;

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
				populateGui();
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
			default:
				Dust.log(TOKEN_LEVEL_WARNING, "Command not handled", cmd);
			}
		}
	};

	TransferHandler cbTransferHandler = new TransferHandler() {

		private static final long serialVersionUID = 1L;

		@Override
		public boolean importData(TransferSupport support) {
			boolean ret = false;

			for (DataFlavor df : support.getDataFlavors()) {
				Transferable t = support.getTransferable();
				String str;

				str = df.getPrimaryType();
				if ("text".equals(str)) {
					str = df.getSubType();
					if ("plain".equals(str)) {
						try {
							Object o = t.getTransferData(df);
							if (o instanceof String) {
								str = (String) o;

								str = str.replace("\n", "</div>\n<div>").replace("\r", "");
								Dust.log(TOKEN_LEVEL_INFO, "Data", df.getPrimaryType(), df.getSubType(), str);
								int cp = docEditor.getCaretPosition();

								StringBuilder dc = new StringBuilder(docEditor.getText());

								int off = cp + headOffset;

								dc.insert(off, str);
								docEditor.setText(dc.toString());

								return true;
							}
//							Dust.log(TOKEN_LEVEL_INFO, "DF", df.getPrimaryType(), df.getSubType(), o.getClass());

						} catch (Throwable e) {
//							Dust.log(TOKEN_LEVEL_INFO, "DF", df.getPrimaryType(), df.getSubType(), e);
						}
					}
				}

			}

			return ret;
		}
	};

	DocumentListener dl = new DocumentListener() {

		@Override
		public void removeUpdate(DocumentEvent e) {
			read();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			read();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			read();
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
		
		String id =  DustUtilsData.getNewId(hUnit, 4);
		
		hDoc = Dust.getHandle(hUnit, hTypeDoc, id, DustOptCreate.Primary);
		
//		FlatLightLaf
		
		DustGuiSwingUtils.optSetLookAndFeel();
		
		frm = new JFrame();
		
		frm.setTitle( Dust.access(DustAccess.Peek, "Text editor", null, TOKEN_NAME));

		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frm.setBounds(100, 100, 800, 400);
		
		docEditor = new JEditorPane();
		docEditor.setTransferHandler(cbTransferHandler);
		docEditor.setContentType("text/html");

		doc = (HTMLDocument) docEditor.getDocument();

		doc.addDocumentListener(dl);

		docStruct = new JTree();
		
		factToolbars.get("tbTop", BoxLayout.LINE_AXIS);

		populateGui();
		
		frm.setVisible(true);

	};

	void populateGui() {
		frm.getContentPane().removeAll();
		
		String htmlContent = "<html><head>\n" + "<style>\n" + ".emphasis {\n" + "	font-weight: bold;\n" + "}\n" + "</style>\n"
				+ "</head><body><div class=\"emphasis\">Place your text here</div></body></html>";
		docEditor.setText(htmlContent);

		String dt = docEditor.getText();
		headOffset = dt.indexOf("Place");

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

		fillToolbar("tbTop", "Rebuild", "Test 1");
		fillToolbar("tbStruct", "Struct 1", "Struct 2");
		fillToolbar("tbDoc", "Doc 1", "Doc 2");

		frm.getContentPane().revalidate();

	}

	void fillToolbar(String tb, String... cmds) {
		JComponent pnl = factToolbars.get(tb);
		pnl.removeAll();

		for (String cmd : cmds) {
			pnl.add(factActionControls.get(cmd));
		}
	}

//	public static void main(String[] args) throws Exception {
//		DustSandboxTextEditor ste = new DustSandboxTextEditor();
//
//		ste.init();
//	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
