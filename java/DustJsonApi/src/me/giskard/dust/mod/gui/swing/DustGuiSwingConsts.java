package me.giskard.dust.mod.gui.swing;

import java.awt.BorderLayout;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JPanel;

import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.mind.DustMindConsts;

public interface DustGuiSwingConsts extends DustConsts, DustMindConsts {
	String TOKEN_SWING_LOOKANDFEEL = DUST_UNIT_ID + DUST_SEP_TOKEN + "lookAndFeel";
	
	public abstract class JCompAgent<CompClass extends JComponent> extends DustAgent {
		protected final CompClass comp;
		
		protected JCompAgent(CompClass c) {
			this.comp = c;
		}
		
		public CompClass getComp() {
			return comp;
		}
	}
	
	public abstract class JPanelAgent extends JCompAgent<JPanel> {
		public JPanelAgent() {
			this(new BorderLayout());
		}
		
		public JPanelAgent(LayoutManager lm) {
			super(new JPanel(lm));
		}
	}

}
