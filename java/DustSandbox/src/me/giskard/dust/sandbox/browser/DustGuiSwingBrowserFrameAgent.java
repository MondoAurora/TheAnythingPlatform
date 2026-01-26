package me.giskard.dust.sandbox.browser;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts.DustAgent;
import me.giskard.dust.gui.swing.DustGuiSwingConsts;
import me.giskard.dust.gui.swing.DustGuiSwingUtils;

//@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustGuiSwingBrowserFrameAgent extends DustAgent implements DustGuiSwingConsts {
	JFrame frm;
	
	@Override
	protected void init() throws Exception {
		DustGuiSwingUtils.optSetLookAndFeel();
		
		frm = new JFrame();
		
		frm.setTitle( Dust.access(DustAccess.Peek, "App frame", null, TOKEN_NAME));
		
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frm.setBounds(100, 100, 800, 400);

		
		JPanel pnlMain = new JPanel(new BorderLayout());

		DustGuiSwingMindBrowserPanel browser = new DustGuiSwingMindBrowserPanel();
		
		browser.init();
		
		pnlMain.add(browser.getComp(), BorderLayout.CENTER);
		
		frm.getContentPane().add(pnlMain);
		
		frm.setVisible(true);
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
		String cmd = Dust.access(DustAccess.Peek, "", null, TOKEN_CMD);
//		StringBuilder sb = null;

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
