package me.giskard.dust.sandbox;

import javax.swing.JFrame;

import me.giskard.dust.Dust;
import me.giskard.dust.DustConsts.DustAgent;
import me.giskard.dust.gui.swing.DustGuiSwingConsts;
import me.giskard.dust.gui.swing.DustGuiSwingUtils;

//@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustSandboxAppFrameAgent extends DustAgent implements DustSandboxConsts, DustGuiSwingConsts {
	JFrame frm;
	
	@Override
	protected void init() throws Exception {
		DustGuiSwingUtils.optSetLookAndFeel();
		
		frm = new JFrame();
		
		frm.setTitle( Dust.accessCtx(DustAccess.Peek, "App frame", DustContext.Agent, TOKEN_NAME));
		
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frm.setBounds(100, 100, 800, 400);
		
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
