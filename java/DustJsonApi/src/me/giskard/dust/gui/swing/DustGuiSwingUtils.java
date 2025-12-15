package me.giskard.dust.gui.swing;

import java.util.Collections;
import java.util.List;

import javax.swing.UIManager;

import me.giskard.dust.Dust;
import me.giskard.dust.kb.DustKBUtils;

public class DustGuiSwingUtils implements DustGuiSwingConsts {
	public static String optSetLookAndFeel(Object cfg) throws Exception {
		UIManager.LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
		List<String> lnf = DustKBUtils.access(KBAccess.Peek, Collections.EMPTY_LIST, cfg, TOKEN_SWING_LOOKANDFEEL);
		
		int i = Integer.MAX_VALUE;
		String lcn = null;
		
		for (UIManager.LookAndFeelInfo look : looks) {
			String cn = look.getClassName();
			int p = lnf.indexOf(cn);
			if ( (0 <= p) && (p < i) ) {
				i = p;
				lcn = cn;
			}
		}
		
		if ( null != lcn ) {
			UIManager.setLookAndFeel(lcn);
			Dust.log(TOKEN_LEVEL_INFO, "Swing L&F selected:", lcn);
		}
		
		return lcn;
	}
}
