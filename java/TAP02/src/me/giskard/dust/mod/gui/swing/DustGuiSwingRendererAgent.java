package me.giskard.dust.mod.gui.swing;

import java.awt.Component;
import java.awt.Container;
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
import java.util.WeakHashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustHandle;
import me.giskard.handles.giskard_me.DustGenHandles_dev_1;
import me.giskard.handles.giskard_me.DustGenHandles_flow_1;
import me.giskard.handles.giskard_me.DustGenHandles_misc_1;

//@SuppressWarnings({ "unchecked", "rawtypes" })
@SuppressWarnings({ "unchecked" })
public class DustGuiSwingRendererAgent extends DustAgent
		implements DustGuiSwingConsts, DustGenHandles_flow_1, DustGenHandles_misc_1, DustGenHandles_dev_1 {
	Object anchor = new Object();

	WeakHashMap<Object, DustHandle> backRef = new WeakHashMap<Object, DustHandle>();

	@Override
	public void init() throws Exception {
		Dust.manageThread(DustThreadAction.SetAnchor, anchor);

//		SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				Dust.manageThread(DustThreadAction.Connect, this);
//			}
//		});
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
			comps.put(hm, getWrapped(hm));
		}

		DustHandle hLayout = Dust.access(DustAccess.Peek, null, h, HANDLE_GUI_ATT_LAYOUT_CONFIG, 0);

		if ( null != hLayout ) {
			Iterable<DustHandle> lms = Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, hLayout, HANDLE_MISC_ATT_MEMBERS);
			
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
			
			for ( JComponent comp : comps.values() ) {
				container.add(comp);
			}
		}

		container.revalidate();
		container.repaint();
	}

	ActionListener al = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			Dust.manageThread(DustThreadAction.Connect, anchor);

			String id = e.getActionCommand();

			JComponent src = (JComponent) e.getSource();
			Object h = backRef.get(src);

			if ( null != h ) {
				DustHandle hMsg = Dust.access(DustAccess.Peek, null, h, HANDLE_MISC_ATT_TARGET);
				Dust.access(DustAccess.Commit, null, hMsg);
			} else {
				JOptionPane.showMessageDialog((Component) src, id, "Command invocation", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	};

	<RetType> RetType getWrapped(DustHandle h) {
		Object ret = Dust.access(DustAccess.Peek, null, h, HANDLE_DUST_ATT_WRAPPEDOBJECT);

		if ( null == ret ) {

			DustHandle type = h.getType();

			if ( HANDLE_GUI_ASP_WINDOW.equals(type) ) {
				JFrame frm = new JFrame();
				frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				frm.setBounds(100, 100, 800, 400);

				frm.setVisible(true);

				ret = frm;

			} else if ( HANDLE_GUI_ASP_WIDGET_LABEL.equals(type) ) {
				String txt = Dust.access(DustAccess.Peek, null, h, HANDLE_DEV_ATT_DEVTEXT);

				JLabel lbl = new JLabel(txt);
				ret = lbl;

			} else if ( HANDLE_GUI_ASP_PANEL_CONTAINER.equals(type) ) {
				JPanel pnl = new JPanel(null);

				layoutContainer(h, pnl);

//				Iterable<DustHandle> members = Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, h,
//						HANDLE_MISC_ATT_MEMBERS);
//
//				for (DustHandle hm : members) {
//					JComponent comp = getWrapped(hm);
//					pnl.add(comp);
//				}

				ret = pnl;

			} else if ( HANDLE_GUI_ASP_WIDGET_BUTTON.equals(type) ) {
				String txt = Dust.access(DustAccess.Peek, null, h, HANDLE_DEV_ATT_DEVTEXT);

				JButton btn = new JButton(txt);
				btn.addActionListener(al);

				ret = btn;

			} else {

			}

			if ( null != ret ) {
				Dust.access(DustAccess.Set, ret, h, HANDLE_DUST_ATT_WRAPPEDOBJECT);
//				Dust.access(DustAccess.Set, h, DustContext.Agent, TOKEN_DUST_ATT_WRAPBACKREF, ret);
				backRef.put(ret, h);
			}
		}

		return (RetType) ret;
	}

}
