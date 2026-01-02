package me.giskard.dust.sandbox;

import me.giskard.dust.Dust;
import me.giskard.dust.DustAgent;
import me.giskard.dust.gui.swing.DustGuiSwingConsts;
import me.giskard.dust.kb.DustKBStore;
import me.giskard.dust.kb.DustKBUtils;

//@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustSandboxTestAgent extends DustAgent implements DustSandboxConsts, DustGuiSwingConsts {
	DustKBStore kb;

	@Override
	protected void init() throws Exception {
		kb = Dust.getAgent(DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_KB_KNOWLEDGEBASE));
	}

	@Override
	protected Object process(DustAction action) throws Exception {
		String cmd = DustKBUtils.access(DustAccess.Peek, "", null, TOKEN_CMD);
//		StringBuilder sb = null;

		switch (cmd) {
		case "LnF":
//			DustGuiSwingUtils.optSetLookAndFeel(cfg);
			break;
		default:
//			sb = new StringBuilder("Unknown command: ").append(cmd);
			break;
		}

		return null;

	}


}
