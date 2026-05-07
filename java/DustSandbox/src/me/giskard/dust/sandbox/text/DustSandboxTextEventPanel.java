package me.giskard.dust.sandbox.text;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustException;
import me.giskard.dust.core.utils.DustUtilsData;
import me.giskard.dust.core.utils.DustUtilsFactory;
import me.giskard.dust.mod.gui.swing.DustGuiSwingUtils;

public class DustSandboxTextEventPanel extends JPanel implements DustSandboxTextConsts {
	private static final long serialVersionUID = 1L;

	Dimension dimTimeline = new Dimension(3000, 30);

	JPanel pnlTimeline;
	JScrollPane scpTimeline;
	
	double begin;
	double length;
	double zoom = 1.0;

	DustSandboxTextAgent txtAgent;

	ActionListener al = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();

			switch ( cmd ) {
			case "aaa":
				break;

			default:
				break;
			}
		}
	};

	DustGuiSwingUtils.ActionControlFactory factActionControls = new DustGuiSwingUtils.ActionControlFactory(al);
	DustGuiSwingUtils.ToolbarFactory factToolbars = new DustGuiSwingUtils.ToolbarFactory(factActionControls);
	
	DustUtilsFactory<DustHandle, JLabel> factLabels = new DustUtilsFactory.Simple<DustHandle, JLabel>(false, JLabel.class);

	public DustSandboxTextEventPanel(DustSandboxTextAgent txtAgent) {
		super(new BorderLayout());

		this.txtAgent = txtAgent;

		pnlTimeline = new JPanel(null);
		
		buildGui();
		
		pnlTimeline.setSize(dimTimeline);

		scpTimeline = new JScrollPane(pnlTimeline);

		add(scpTimeline, BorderLayout.CENTER);
		add(factToolbars.get("tbMain"), BorderLayout.WEST);
	};

	void buildGui() {
		factToolbars.fillToolbar("tbMain", "Zoom In", "Zoom Out");
		updateLabels();
	}

	public void updateLabels() {
		begin = Long.MAX_VALUE;
		long end = Long.MIN_VALUE;
		
		for (DustHandle hEvt: txtAgent.events.values() ) {
			try {
				Date start = DustUtilsData.getEventDate(hEvt);
				long s = start.getTime();
				if ( s < begin ) {
					begin = s;
				}

				long duration = Dust.access(DustAccess.Peek, 0L, hEvt, TOKEN_EVENT_DURATION);
				long e = s + duration;
				if ( e > end ) {
					end = e;
				}
			} catch (Exception e) {
				DustException.swallow(e);
				continue;
			}
		}
		
		length = end - begin;
		zoom = 0.001;
		
		dimTimeline.width = (int) ( zoom * (double) length);
		pnlTimeline.setSize(dimTimeline);
		
		pnlTimeline.removeAll();
		
		for (Map.Entry<DustHandle, DustHandle> ee: txtAgent.events.entrySet() ) {
			DustHandle hEvt = ee.getValue();

			try {
				Date start = DustUtilsData.getEventDate(hEvt);
				long s = (long) (zoom * (double) (start.getTime() - (long) begin));
				long duration = Dust.access(DustAccess.Peek, 0L, hEvt, TOKEN_EVENT_DURATION);
				
				DustHandle hTxt = ee.getKey();
				String tId = hTxt.getId();
				String txt = tId; // txtAgent.accessText(DustAccess.Peek, "", tId);
				JLabel lbl = factLabels.get(hTxt);
				
				lbl.setBounds((int) s, 2, (int)(zoom * (double) duration), 26);
				lbl.setText(txt);
				
				pnlTimeline.add(lbl);
			} catch (Exception e) {
				DustException.swallow(e);
				continue;
			}
		}
		

	}
}
