package me.giskard.dust.mod.gui.swing;

import java.awt.BorderLayout;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import gen.handles.giskard_me.DustGenHandles_dev_1;
import gen.handles.giskard_me.DustGenHandles_flow_1;
import gen.handles.giskard_me.DustGenHandles_layout_1;
import gen.handles.giskard_me.DustGenHandles_misc_1;
import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsData;
import me.giskard.dust.core.utils.DustUtilsFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustGuiSwingRendererAgent extends DustAgent implements DustGuiSwingConsts, DustGenHandles_flow_1,
		DustGenHandles_misc_1, DustGenHandles_dev_1, DustGenHandles_layout_1 {
	Object anchor = new Object();

	WeakHashMap<Object, DustHandle> backRef = new WeakHashMap<Object, DustHandle>();

	DustUtilsFactory<DustHandle, Set<DustHandle>> listeners = new DustUtilsFactory(SORTEDSET_CREATOR, true);

	boolean inUpdate = false;
	
	public DustGuiSwingRendererAgent() {
		Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, getClass().getName(), "Created");
	}

	@Override
	public void process() throws Exception {
		DustHandle cmd = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_CMD);
		DustHandle hComp = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_TARGET);

		if (HANDLE_MISC_TAG_CMD_REFRESH.equals(cmd)) {
			refreshGuiComp(hComp);
		} else {
			Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, getClass().getName(), "called with", cmd);
		}
	}

	@Override
	public void init() throws Exception {
		Dust.manageThread(DustThreadAction.SetAnchor, anchor);
		
		Collection<DustHandle> impls = Dust.access(DustAccess.Peek, Collections.EMPTY_SET, null, HANDLE_DUST_ATT_AGENT_IMPLEMENTATION, HANDLE_DUST_ATT_IMPLEMENTATIONS);
		
		Map<DustHandle, String> implClasses = new TreeMap<>();
		for (DustHandle hImpl : impls ) {
			implClasses.put(Dust.access(DustAccess.Peek, null, hImpl, HANDLE_MISC_ATT_TARGET), Dust.access(DustAccess.Peek, null, hImpl, HANDLE_MISC_ATT_KEY));
		}

		Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, implClasses);
	}

	public void refreshGuiComp(DustHandle hComp) {
		DustHandle type = hComp.getType();

		if (HANDLE_GUI_ASP_WINDOW.equals(type)) {
			JFrame frm = getWrapped(hComp);

			String title = Dust.access(DustAccess.Peek, null, hComp, HANDLE_DEV_ATT_DEVTEXT);
			frm.setTitle(title);

			Container cp = frm.getContentPane();

			layoutContainer(hComp, cp);
		}
	}

	public void layoutContainer(DustHandle h, Container container) {

		ArrayList<JComponent> compList = new ArrayList<>();

		Map<DustHandle, JComponent> comps = new HashMap<DustHandle, JComponent>();
		Collection<DustHandle> members = Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, h, HANDLE_MISC_ATT_MEMBERS);
		for (DustHandle hm : members) {
			Dust.access(DustAccess.Set, h, hm, HANDLE_GUI_ATT_PARENT_CONTAINER);
			JComponent cm = getWrapped(hm);
			Object mm = Dust.access(DustAccess.Peek, null, hm, HANDLE_MISC_ATT_MEMBERS);
			if (null != mm) {
				layoutContainer(hm, (Container) cm);
			}
			comps.put(hm, cm);
			compList.add(cm);
		}

		if (container instanceof JScrollPane) {
			((JScrollPane) container).setViewportView(compList.get(0));
		} else if (container instanceof JSplitPane) {
			JSplitPane spl = (JSplitPane) container;
			spl.setLeftComponent(compList.get(0));
			spl.setRightComponent(compList.get(1));
		} else {
			container.removeAll();

			DustHandle hLayout = Dust.access(DustAccess.Peek, null, h, HANDLE_GUI_ATT_LAYOUT_CONFIG, 0);

			if (null != hLayout) {
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

					gbc.weighty = Dust.access(DustAccess.Peek, 0.0, hl, HANDLE_LAYOUT_ATT_FILL_WEIGHT, 0);
					gbc.weightx = Dust.access(DustAccess.Peek, 0.0, hl, HANDLE_LAYOUT_ATT_FILL_WEIGHT, 1);

					gbc.fill = (0.0 == gbc.weighty)
							? (0.0 == gbc.weightx) ? GridBagConstraints.NONE : GridBagConstraints.HORIZONTAL
							: (0.0 == gbc.weightx) ? GridBagConstraints.VERTICAL : GridBagConstraints.BOTH;

					if (GridBagConstraints.NONE != gbc.fill) {
						Dust.log(HANDLE_MISC_TAG_LEVEL_TRACE, "Expansion", gbc);
					}

					JComponent comp = comps.get(hTarget);
					container.add(comp, gbc);
				}
			} else {
				if (1 == members.size()) {
					container.setLayout(new BorderLayout());
					container.add(comps.get(members.iterator().next()), BorderLayout.CENTER);
				} else {
					container.setLayout(new FlowLayout(FlowLayout.CENTER));
					for (DustHandle hm : members) {
						container.add(comps.get(hm));
					}
				}
			}
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

				if (null != h) {
					DustHandle hMsg = Dust.access(DustAccess.Peek, null, h, HANDLE_MISC_ATT_TARGET);
					DustHandle hCmd = Dust.access(DustAccess.Peek, null, h, HANDLE_MIND_ATT_CMD);
					if (null != hCmd) {
						Dust.access(DustAccess.Set, hCmd, hMsg, HANDLE_MIND_ATT_CMD);
					}
					Dust.access(DustAccess.Commit, null, hMsg);

					try {
						inUpdate = true;
						for (DustHandle hData : listeners.keys()) {
							for (DustHandle hCtrl : listeners.get(hData)) {
								if (h != hCtrl) {
									updateValue(hCtrl, hData);
								}
							}
						}
					} finally {
						inUpdate = false;
					}
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
			if (inUpdate) {
				return;
			}

			try {
				Dust.manageThread(DustThreadAction.Connect, anchor);

				Document src = e.getDocument();
				DustHandle h = backRef.get(src);

				if (null != h) {
					DustHandle hData = DustUtilsData.findRec(h, HANDLE_MISC_ATT_DATA, HANDLE_GUI_ATT_PARENT_CONTAINER);
					Object[] pp = DustUtilsData.optGetRefPath(h);

					if ((null != hData) && (null != pp)) {
						Object ctrl = Dust.access(DustAccess.Peek, null, h, HANDLE_DUST_ATT_WRAPPEDOBJECT);

						if (ctrl instanceof JScrollPane) {
							ctrl = ((JScrollPane) ctrl).getViewport().getView();
						}

						String txt = ((JTextComponent) ctrl).getText();

						Dust.access(DustAccess.Set, txt, hData, pp);

						try {
							inUpdate = true;
							for (DustHandle hCtrl : listeners.get(hData)) {
								if (h != hCtrl) {
									updateValue(hCtrl, hData);
								}
							}
						} finally {
							inUpdate = false;
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

		if (null == ret) {
			DustHandle type = h.getType();

			DustHandle hData = DustUtilsData.findRec(h, HANDLE_MISC_ATT_DATA, HANDLE_GUI_ATT_PARENT_CONTAINER);

			if (null != hData) {
				listeners.get(hData).add(h);
			}

			ret = createWrapped(h, type, hData);

			if (null != ret) {
				Dust.access(DustAccess.Set, ret, h, HANDLE_DUST_ATT_WRAPPEDOBJECT);
//				Dust.access(DustAccess.Set, h, DustContext.Agent, TOKEN_DUST_ATT_WRAPBACKREF, ret);
				backRef.put(ret, h);
			}
		}

		return (RetType) ret;
	}

	public <RetType> RetType getValue(DustHandle h, DustHandle hData) {
		Object ret = Dust.access(DustAccess.Peek, null, h, HANDLE_DEV_ATT_DEVTEXT);

		if (null != hData) {
			Object[] pp = DustUtilsData.optGetRefPath(h);
			ret = (null == pp) ? hData.toString() : Dust.access(DustAccess.Peek, null, hData, pp);
		}
		return (RetType) ret;
	}

	// -- Swing spec functions

	public Object createWrapped(DustHandle h, DustHandle type, DustHandle hData) {
		Object ret = null;
		
		String className = null;

		if (HANDLE_GUI_ASP_WINDOW.equals(type)) {
			JFrame frm = new JFrame();
			frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			frm.setBounds(100, 100, 800, 400);

			frm.setVisible(true);

			ret = frm;

		} else if (HANDLE_GUI_ASP_CONTAINER.equals(type)) {

			DustHandle containerType = Dust.access(DustAccess.Peek, null, h, HANDLE_MIND_ATT_TAGS,
					HANDLE_GUI_TAG_CONTAINER_TYPE);

			Container cont;
			if (HANDLE_GUI_TAG_SCROLL.equals(containerType)) {
				JScrollPane scp = new JScrollPane();
				cont = scp;
			} else if (HANDLE_GUI_TAG_SPLIT.equals(containerType)) {
				JSplitPane spl = new JSplitPane();
				cont = spl;
			} else {
				JPanel pnl = new JPanel(null);
				cont = pnl;
			}

			layoutContainer(h, cont);

			ret = cont;
		} else if (HANDLE_GUI_ASP_CONTROL.equals(type)) {

			DustHandle ctrlType = Dust.access(DustAccess.Peek, null, h, HANDLE_MIND_ATT_TAGS, HANDLE_GUI_TAG_CONTROL_TYPE);
			

			JTextComponent tf = null;

			if (HANDLE_GUI_TAG_LABEL.equals(ctrlType)) {

				String txt = getValue(h, hData);

				JLabel lbl = new JLabel(txt);
				ret = lbl;

			} else if (HANDLE_GUI_TAG_BUTTON.equals(ctrlType)) {
				String txt = Dust.access(DustAccess.Peek, null, h, HANDLE_DEV_ATT_DEVTEXT);

				if (DustUtils.isEmpty(txt)) {
					txt = Dust.access(DustAccess.Peek, null, h, HANDLE_MIND_ATT_CMD, HANDLE_MIND_ATT_ID);
					txt = DustUtils.getPostfix(txt, DUST_SEP_TOKEN);
				}

				JButton btn = new JButton(txt);
				btn.addActionListener(al);

				ret = btn;

			} else if (HANDLE_GUI_TAG_TEXT_SINGLE_LINE.equals(ctrlType)) {

				tf = new JTextField();

			} else if (HANDLE_GUI_TAG_TEXT_MULTI_LINE.equals(ctrlType)) {
				tf = new JTextArea();
			} else if (HANDLE_GUI_TAG_TEXT_FORMATTED.equals(ctrlType)) {
				tf = new JEditorPane();
			}

			if (null != tf) {
				hData = DustUtilsData.findRec(h, HANDLE_MISC_ATT_DATA, HANDLE_GUI_ATT_PARENT_CONTAINER);
				if (null != hData) {
					tf.setText(getValue(h, hData));
					Document doc = tf.getDocument();
					doc.addDocumentListener(df);
					backRef.put(doc, h);
				}

				tf.setPreferredSize(new Dimension(300, 20));
				ret = tf;
			}
		} else if (HANDLE_MIND_ASP_MESSAGE.equals(type)) {
			Dust.access(DustAccess.Commit, null, h);
			ret = Dust.access(DustAccess.Peek, null, h, HANDLE_DUST_ATT_WRAPPEDOBJECT);
		} else {
			ret = new JLabel("No twin creation for type " + type);
		}

		return ret;
	}

	public void updateValue(DustHandle h, DustHandle hData) {
		Object val = getValue(h, hData);

		Object ctrl = Dust.access(DustAccess.Peek, null, h, HANDLE_DUST_ATT_WRAPPEDOBJECT);

		if (ctrl instanceof JTextComponent) {
			((JTextComponent) ctrl).setText((String) val);
		} else if (ctrl instanceof JLabel) {
			((JLabel) ctrl).setText((String) val);
		}
	}

}
