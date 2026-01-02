package me.giskard.dust.sandbox;

import javax.swing.JFrame;

import me.giskard.dust.DustAgent;
import me.giskard.dust.gui.swing.DustGuiSwingConsts;
import me.giskard.dust.gui.swing.DustGuiSwingUtils;
import me.giskard.dust.kb.DustKBUtils;

//@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustSandboxAppFrameAgent extends DustAgent implements DustSandboxConsts, DustGuiSwingConsts {
	JFrame frm;
	
	@Override
	protected void init() throws Exception {
		DustGuiSwingUtils.optSetLookAndFeel();
		
		frm = new JFrame();
		
		frm.setTitle( DustKBUtils.accessCtx(DustAccess.Peek, "App frame", DustContext.Agent, TOKEN_NAME));
		
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frm.setBounds(100, 100, 800, 400);
		
		frm.setVisible(true);
	}

	@Override
	protected Object process(DustAction action) throws Exception {
		String cmd = DustKBUtils.access(DustAccess.Peek, "", null, TOKEN_CMD);
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
