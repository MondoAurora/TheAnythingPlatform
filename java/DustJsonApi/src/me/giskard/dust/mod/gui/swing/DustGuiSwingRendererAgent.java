package me.giskard.dust.mod.gui.swing;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustGuiSwingRendererAgent extends DustAgent implements DustGuiSwingConsts {

	@Override
	protected void init() throws Exception {
		DustGuiSwingUtils.optSetLookAndFeel();
	}
	
	ActionListener al = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			String id = e.getActionCommand();
			
			Object src = e.getSource();
			JOptionPane.showMessageDialog((Component) src, id, "Command invocation", JOptionPane.INFORMATION_MESSAGE);
		}
	};

	<RetType> RetType getWrapped(DustHandle h) {
		Object ret = Dust.access(DustAccess.Peek, null, h, TOKEN_DUST_ATT_WRAPPEDOBJECT);

		String id;
		JFrame frm;
		JPanel pnl = null;
		JButton btn;
		Component comp;

		if (null == ret) {
			Iterable<DustHandle> members = Dust.access(DustAccess.Visit, Collections.EMPTY_LIST, h, TOKEN_MISC_ATT_MEMBERS);

			String tid = h.getType().getId();

			switch (tid) {
			case TOKEN_GUI_ASP_PANEL_TOOLBAR:
				ret = pnl = new JPanel(new FlowLayout());
				for (DustHandle hc : members) {
					id = hc.getId();
					
					btn = new JButton(id);
					btn.setActionCommand(id);
					btn.addActionListener(al);
					pnl.add(btn);
				}

				break;
			case TOKEN_GUI_ASP_PANEL_CONTAINER:

				LayoutManager lm = new GridLayout(0, 1);

				ret = pnl = new JPanel(lm);

				for (DustHandle hm : members) {
					comp = getWrapped(hm);
					if (null != comp) {
						pnl.add(comp);
					}
				}
				
				break;
			case TOKEN_GUI_ASP_WINDOW:

				ret = frm = new JFrame();
				frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				String title = Dust.access(DustAccess.Peek, null, h, TOKEN_MISC_ATT_NAME);

				frm.setTitle(title);

				frm.setBounds(100, 100, 800, 400);

				DustHandle hContent = Dust.access(DustAccess.Peek, null, h, TOKEN_MISC_ATT_TARGET);

				if (null != hContent) {
					comp = getWrapped(hContent);
					if (null != comp) {
						frm.getContentPane().add(comp);
					}
				}

				frm.setVisible(true);

				break;
			}
			
			if ( null != pnl) {
				DustGuiSwingUtils.setTitle(pnl, h.getId());
			}

			Dust.access(DustAccess.Set, ret, h, TOKEN_DUST_ATT_WRAPPEDOBJECT);
		}

		return (RetType) ret;
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		String cmd = Dust.access(DustAccess.Peek, "", null, TOKEN_MIND_ATT_CMD);

		DustHandle hRoot = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_TARGET);

		Object wrapped = getWrapped(hRoot);

		switch (cmd) {
		case "LnF":
			break;
		default:
//			sb = new StringBuilder("Unknown command: ").append(cmd);
			break;
		}

		return null;

	}

}
