package me.giskard.dust.mod.gui.swing;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Collections;
import java.util.WeakHashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.tokens.giskard_me.DustGenTokens_dev_1;
import me.giskard.tokens.giskard_me.DustGenTokens_stream_1;

@SuppressWarnings({ "unchecked" })
//@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustGuiSwingRendererAgent extends DustAgent implements DustGuiSwingConsts, DustGenTokens_stream_1, DustGenTokens_dev_1 {

	WeakHashMap<Object, DustHandle> backRef = new WeakHashMap<Object, DustHandle>();

	@Override
	protected void init() throws Exception {
		DustGuiSwingUtils.optSetLookAndFeel();

		final Object dustSwingCtx = Dust.access(DustAccess.Peek, null, null);

		Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, "Initialising on thread", Thread.currentThread(), dustSwingCtx);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Object tCtx = Dust.access(DustAccess.Peek, null, null);
				if (null == tCtx) {
					Dust.access(DustAccess.Set, dustSwingCtx, null);
					Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, "Connecting context on thread", Thread.currentThread(), dustSwingCtx);
				}
			}
		});
	}

	ActionListener al = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			String id = e.getActionCommand();

			JComponent src = (JComponent) e.getSource();

//			Object h = src.getClientProperty(DUST_SWING_HANDLE);
//			Object h = Dust.access(DustAccess.Peek, null, DustContext.Agent, TOKEN_DUST_ATT_WRAPBACKREF, src);
			Object h = backRef.get(src);

			if (null != h) {
				DustHandle hMsg = Dust.access(DustAccess.Peek, null, h, TOKEN_MISC_ATT_TARGET);
				Dust.access(DustAccess.Process, null, hMsg);
			} else {
				JOptionPane.showMessageDialog((Component) src, id, "Command invocation", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	};

	<RetType> RetType getWrapped(DustHandle h) {
		Object ret = Dust.access(DustAccess.Peek, null, h, TOKEN_DUST_ATT_WRAPPEDOBJECT);

		String id;
		JFrame frm;
		JPanel pnl = null;
		JButton btn;
		Component comp;
		DustHandle hCmd = null;

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
			case TOKEN_MIND_ASP_MESSAGE:

				Dust.access(DustAccess.Set, TOKEN_MISC_TAG_CMD_PING, h, TOKEN_MIND_ATT_CMD);
				Dust.access(DustAccess.Process, null, h);

				ret = Dust.access(DustAccess.Peek, null, h, TOKEN_DUST_ATT_WRAPPEDOBJECT);

				break;
			case TOKEN_GUI_ASP_WIDGET_BUTTON:
				hCmd = Dust.access(DustAccess.Peek, null, h, TOKEN_MISC_ATT_TARGET, TOKEN_MIND_ATT_CMD);
				id = hCmd.getId();

				ret = btn = new JButton(id);
				btn.setActionCommand(id);
				btn.addActionListener(al);

//				btn.putClientProperty(DUST_SWING_HANDLE, h);
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

			if (null != pnl) {
				DustGuiSwingUtils.setTitle(pnl, h.getId());
			}

			if (null != ret) {
				Dust.access(DustAccess.Set, ret, h, TOKEN_DUST_ATT_WRAPPEDOBJECT);
//				Dust.access(DustAccess.Set, h, DustContext.Agent, TOKEN_DUST_ATT_WRAPBACKREF, ret);
				backRef.put(ret, h);
			}

			if (ret instanceof JButton) {
				Object hGetImg = Dust.access(DustAccess.Peek, null, null, TOKEN_GUI_ATT_IMAGE_RESOLVER);

				Dust.access(DustAccess.Set, hCmd, hGetImg, TOKEN_MISC_ATT_DATA);
				Dust.access(DustAccess.Set, h, hGetImg, TOKEN_MIND_ATT_NEXT, TOKEN_MISC_ATT_TARGET);

				Dust.access(DustAccess.Process, null, hGetImg);

			}
		}

		return (RetType) ret;
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		String cmd = Dust.access(DustAccess.Peek, "", null, TOKEN_MIND_ATT_CMD);

		DustHandle hRoot = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_TARGET);

		Object wrapped = getWrapped(hRoot);

//		Container cont = (wrapped instanceof Container) ? (Container) wrapped : null;

		switch (cmd) {
		case TOKEN_DEV_TAG_CMD_TEST:
			JButton btn = (JButton) wrapped;
			InputStream is = Dust.access(DustAccess.Peek, null, null, TOKEN_STREAM_ATT_INPUT);
			ImageIcon icon = null;
			BufferedImage image = ImageIO.read(is);
			icon = new ImageIcon(image);

			btn.setIcon(icon);

			break;
		case TOKEN_MISC_TAG_CMD_REFRESH:
//			if (wrapped instanceof JFrame) {
//				cont = ((JFrame) wrapped).getContentPane();
//			}

			break;
		case "LnF":
			break;
		default:
//			sb = new StringBuilder("Unknown command: ").append(cmd);
			break;
		}

		return null;

	}

}
