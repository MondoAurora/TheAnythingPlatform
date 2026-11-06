package me.giskard.dust.mod.gui.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsData;
import me.giskard.dust.core.utils.DustUtilsFactory;
import me.giskard.handles.giskard_me.DustGenHandles_dev_1;
import me.giskard.handles.giskard_me.DustGenHandles_flow_1;
import me.giskard.handles.giskard_me.DustGenHandles_misc_1;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustGuiSwingRendererAgent extends DustAgent
		implements DustGuiSwingConsts, DustGenHandles_flow_1, DustGenHandles_misc_1, DustGenHandles_dev_1 {
	Object anchor = new Object();

	WeakHashMap<Object, DustHandle> backRef = new WeakHashMap<Object, DustHandle>();

	DustUtilsFactory<DustHandle, Set<DustHandle>> listeners = new DustUtilsFactory(SORTEDSET_CREATOR, true);

	@Override
	public void init() throws Exception {
		Dust.manageThread(DustThreadAction.SetAnchor, anchor);
	}

	@Override
	public void process() throws Exception {
		DustHandle cmd = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_CMD);
//		DustHandle h;
		DustHandle hRoot = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_TARGET);

		if ( HANDLE_MISC_TAG_CMD_REFRESH.equals(cmd) ) {

			DustHandle type = hRoot.getType();

			if ( HANDLE_GUI_ASP_WINDOW.equals(type) ) {
				JFrame frm = getWrapped(hRoot);

				String title = Dust.access(DustAccess.Peek, null, hRoot, HANDLE_DEV_ATT_DEVTEXT);
				frm.setTitle(title);

				Container cp = frm.getContentPane();

				layoutContainer(hRoot, cp);
			}

		} else {
			Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, getClass().getName(), "called with", cmd);
		}
	}

	public void layoutContainer(DustHandle h, Container container) {
		container.removeAll();

		Map<DustHandle, JComponent> comps = new HashMap<DustHandle, JComponent>();
		Iterable<DustHandle> members = Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, h, HANDLE_MISC_ATT_MEMBERS);
		for (DustHandle hm : members) {
			Dust.access(DustAccess.Set, h, hm, HANDLE_GUI_ATT_PARENT_CONTAINER);
			JComponent cm = getWrapped(hm);
			Object mm = Dust.access(DustAccess.Peek, null, hm, HANDLE_MISC_ATT_MEMBERS);
			if ( null != mm ) {
				layoutContainer(hm, (Container) cm);
			}
			comps.put(hm, cm);
		}

		DustHandle hLayout = Dust.access(DustAccess.Peek, null, h, HANDLE_GUI_ATT_LAYOUT_CONFIG, 0);

		if ( null != hLayout ) {
			Iterable<DustHandle> lms = Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, hLayout,
					HANDLE_MISC_ATT_MEMBERS);

			container.setLayout(new GridBagLayout());

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(2, 2, 2, 2);

			for (DustHandle hl : lms) {
				DustHandle hTarget = Dust.access(DustAccess.Peek, null, hl, HANDLE_MISC_ATT_TARGET);

				ArrayList<Long> loc = Dust.access(DustAccess.Peek, null, hl, HANDLE_MISC_ATT_POSITION);

				gbc.gridx = loc.get(1).intValue() - 1;
				gbc.gridy = loc.get(0).intValue() - 1;

				JComponent comp = comps.get(hTarget);
				container.add(comp, gbc);
			}
		} else {
			container.setLayout(new FlowLayout(FlowLayout.CENTER));

			for (DustHandle hm : members) {
				container.add(comps.get(hm));
			}

//			for (JComponent comp : comps.values()) {
////				DustGuiSwingUtils.setTitle(comp, "hmm");
//				container.add(comp);
//			}
		}

		container.revalidate();
		container.repaint();
	}

	ActionListener al = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {

			try {
				Dust.manageThread(DustThreadAction.Connect, anchor);

				String id = e.getActionCommand();

				JComponent src = (JComponent) e.getSource();
				Object h = backRef.get(src);

				if ( null != h ) {
					DustHandle hMsg = Dust.access(DustAccess.Peek, null, h, HANDLE_MISC_ATT_TARGET);
					DustHandle hCmd = Dust.access(DustAccess.Peek, null, h, HANDLE_MIND_ATT_CMD);
					if ( null != hCmd ) {
						Dust.access(DustAccess.Set, hCmd, hMsg, HANDLE_MIND_ATT_CMD);
					}
					Dust.access(DustAccess.Commit, null, hMsg);
				} else {
					JOptionPane.showMessageDialog((Component) src, id, "Command invocation", JOptionPane.INFORMATION_MESSAGE);
				}

				Dust.manageThread(DustThreadAction.ReleaseSuccess, anchor);
			} catch (Throwable ex) {
				Dust.manageThread(DustThreadAction.ReleaseFailure, anchor);
			}
		}
	};

	DocumentListener df = new DustGuiSwingUtils.DocumentAdapter() {
		@Override
		protected void update(DocumentEvent e) {
			try {
				Dust.manageThread(DustThreadAction.Connect, anchor);

				Document src = e.getDocument();
				DustHandle h = backRef.get(src);

				if ( null != h ) {
					DustHandle hData = DustUtilsData.findRec(h, HANDLE_MISC_ATT_DATA, HANDLE_GUI_ATT_PARENT_CONTAINER);
					Object[] pp = DustUtilsData.optGetRefPath(h);

					if ( (null != hData) && (null != pp) ) {
						Object ctrl = Dust.access(DustAccess.Peek, null, h, HANDLE_DUST_ATT_WRAPPEDOBJECT);

						if ( ctrl instanceof JScrollPane ) {
							ctrl = ((JScrollPane) ctrl).getViewport().getView();
						}

						String txt = ((JTextComponent) ctrl).getText();

						Dust.access(DustAccess.Set, txt, hData, pp);
						
						for ( DustHandle hCtrl : listeners.get(hData) ) {
							if ( h != hCtrl ) {
								updateValue(hCtrl, hData);
							}
						}
					}
				}

				Dust.manageThread(DustThreadAction.ReleaseSuccess, anchor);
			} catch (Throwable ex) {
				Dust.manageThread(DustThreadAction.ReleaseFailure, anchor);
			}
		}
	};

	<RetType> RetType getWrapped(DustHandle h) {
		Object ret = Dust.access(DustAccess.Peek, null, h, HANDLE_DUST_ATT_WRAPPEDOBJECT);

		if ( null == ret ) {

			DustHandle type = h.getType();
			JScrollPane scp = null;
			DustHandle hData = DustUtilsData.findRec(h, HANDLE_MISC_ATT_DATA, HANDLE_GUI_ATT_PARENT_CONTAINER);

			if ( null != hData ) {
				listeners.get(hData).add(h);
			}

			if ( HANDLE_GUI_ASP_WINDOW.equals(type) ) {
				JFrame frm = new JFrame();
				frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				frm.setBounds(100, 100, 800, 400);

				frm.setVisible(true);

				ret = frm;

			} else if ( HANDLE_GUI_ASP_WIDGET_LABEL.equals(type) ) {

				String txt = getValue(h, hData);

				JLabel lbl = new JLabel(txt);
				ret = lbl;

			} else if ( HANDLE_GUI_ASP_PANEL_CONTAINER.equals(type) ) {
				JPanel pnl = new JPanel(null);

				layoutContainer(h, pnl);

				ret = pnl;

			} else if ( HANDLE_GUI_ASP_WIDGET_BUTTON.equals(type) ) {
				String txt = Dust.access(DustAccess.Peek, null, h, HANDLE_DEV_ATT_DEVTEXT);

				if ( DustUtils.isEmpty(txt) ) {
					txt = Dust.access(DustAccess.Peek, null, h, HANDLE_MIND_ATT_CMD, HANDLE_MIND_ATT_ID);
					txt = DustUtils.getPostfix(txt, DUST_SEP_TOKEN);
				}

				JButton btn = new JButton(txt);
				btn.addActionListener(al);

				ret = btn;

			} else if ( HANDLE_GUI_ASP_EDIT_TEXT.equals(type) ) {
				DustHandle hMode = Dust.access(DustAccess.Peek, null, h, HANDLE_MIND_ATT_TAGS, HANDLE_GUI_TAG_TEXT_MODE);

				JTextComponent tf;

				if ( HANDLE_GUI_TAG_TEXT_MODE_MULTI_LINE == hMode ) {
					tf = new JTextArea();
					scp = new JScrollPane();
				} else if ( HANDLE_GUI_TAG_TEXT_MODE_FORMATTED == hMode ) {
					tf = new JEditorPane();
					scp = new JScrollPane();
				} else {
					tf = new JTextField();
				}

				hData = DustUtilsData.findRec(h, HANDLE_MISC_ATT_DATA, HANDLE_GUI_ATT_PARENT_CONTAINER);
				if ( null != hData ) {
					tf.setText(getValue(h, hData));
					Document doc = tf.getDocument();
					doc.addDocumentListener(df);
					backRef.put(doc, h);
				}

				if ( null != scp ) {
					scp.setPreferredSize(new Dimension(400, 60));
					scp.setViewportView(tf);
					ret = scp;
				} else {
					ret = tf;
				}
			} else if ( HANDLE_MIND_ASP_MESSAGE.equals(type) ) {
				Dust.access(DustAccess.Commit, null, h);
				ret = Dust.access(DustAccess.Peek, null, h, HANDLE_DUST_ATT_WRAPPEDOBJECT);
			} else {
				ret = new JLabel("No twin creation for type " + type);
			}

			if ( null != ret ) {
				Dust.access(DustAccess.Set, ret, h, HANDLE_DUST_ATT_WRAPPEDOBJECT);
//				Dust.access(DustAccess.Set, h, DustContext.Agent, TOKEN_DUST_ATT_WRAPBACKREF, ret);
				backRef.put(ret, h);
			}
		}

		return (RetType) ret;
	}

	public void updateValue(DustHandle h, DustHandle hData) {
		Object ctrl = Dust.access(DustAccess.Peek, null, h, HANDLE_DUST_ATT_WRAPPEDOBJECT);
		Object val = getValue(h, hData);

		if ( ctrl instanceof JScrollPane ) {
			ctrl = ((JScrollPane) ctrl).getViewport().getView();
		}

		DustHandle type = h.getType();

		if ( HANDLE_GUI_ASP_EDIT_TEXT.equals(type) ) {
			((JTextComponent) ctrl).setText((String) val);
		} else if ( HANDLE_GUI_ASP_WIDGET_LABEL.equals(type) ) {
			((JLabel) ctrl).setText((String) val);
		}
	}

	public <RetType> RetType getValue(DustHandle h, DustHandle hData) {
		Object ret = Dust.access(DustAccess.Peek, null, h, HANDLE_DEV_ATT_DEVTEXT);

		if ( null != hData ) {
			Object[] pp = DustUtilsData.optGetRefPath(h);
			ret = (null == pp) ? hData.toString() : Dust.access(DustAccess.Peek, null, hData, pp);
		}
		return (RetType) ret;
	}

}
