package me.giskard.dust.sandbox;

import me.giskard.dust.DustAgent;
import me.giskard.dust.gui.swing.DustGuiSwingConsts;
import me.giskard.dust.gui.swing.DustGuiSwingUtils;
import me.giskard.dust.kb.DustKBUtils;

//@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustSandboxTestAgent extends DustAgent implements DustSandboxConsts, DustGuiSwingConsts {

	@Override
	protected Object process(DustAccess access) throws Exception {
		String cmd = DustKBUtils.access(DustAccess.Peek, "", null, TOKEN_CMD);
//		StringBuilder sb = null;

		switch (cmd) {
		case "LnF":
			DustGuiSwingUtils.optSetLookAndFeel();
			break;
		default:
//			sb = new StringBuilder("Unknown command: ").append(cmd);
			break;
		}

		return null;

	}


}
