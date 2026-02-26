package me.giskard.dust.sandbox;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.mod.gui.swing.DustGuiSwingConsts;
import me.giskard.dust.mod.gui.swing.DustGuiSwingUtils;

//@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustSandboxTestAgent extends DustAgent implements DustSandboxConsts, DustGuiSwingConsts {

	@Override
	protected Object process(DustAccess access) throws Exception {
		String cmd = Dust.access(DustAccess.Peek, "", null, TOKEN_CMD);
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
