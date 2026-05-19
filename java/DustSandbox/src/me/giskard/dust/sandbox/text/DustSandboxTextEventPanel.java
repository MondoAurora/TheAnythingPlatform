package me.giskard.dust.sandbox.text;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Date;

import javax.swing.JComponent;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.utils.DustUtilsData;

public class DustSandboxTextEventPanel extends JComponent implements DustSandboxTextConsts {
	private static final long serialVersionUID = 1L;

	long zero;
	long begin;
	long length;

	DustSandboxTextAgent txtAgent;
	DustSandboxTextSelectionManager selMgr;

	public DustSandboxTextEventPanel(DustSandboxTextAgent txtAgent, DustSandboxTextSelectionManager selMgr) {
		this.txtAgent = txtAgent;
		this.selMgr = selMgr;

		zero = DustUtilsData.getEventZeroDate().getTime();

		setPreferredSize(new Dimension(800, 30));

		selMgr.attach(this);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		DustHandle hSelTxt = selMgr.hfBlock;
		if (null == hSelTxt) {
			hSelTxt = Dust.access(DustAccess.Peek, null, txtAgent.hDoc, TOKEN_MEMBERS, 0);

			if (null == hSelTxt) {
				return;
			}
		}

		long zoom = 20;

		DustHandle hSelEvt = txtAgent.events.get(hSelTxt);
		if (null != hSelEvt) {
			long start = DustUtilsData.getEventDate(hSelEvt).getTime();
			start -= zero;

			long w = getWidth() * zoom;

			start -= (w / 4);
			if (start < 0) {
				start = 0;
			}
			long end = start + w;

			int ry = 2;
			int rh = getHeight() - (2 * ry);

			for (DustHandle hEvt : txtAgent.events.values()) {
				long s = DustUtilsData.getEventDate(hEvt).getTime() - zero;
				if (s < end) {
					long duration = Dust.access(DustAccess.Peek, 0L, hEvt, TOKEN_EVENT_DURATION);
					long e = s + duration;
					if (e > start) {
						int rx = (int) ((s - start) / zoom);
						int rw = (int) (duration / zoom);

						if (hEvt == hSelEvt) {
							g2.fillRect(rx, ry, rw, rh);
						} else {
							g2.drawRect(rx, ry, rw, rh);
						}
					}
				}
			}
		}

//		g2.setPaint(new GradientPaint(0, 0, Color.red, getWidth(), getHeight(), Color.blue, true));
//		g2.fillRect(5, 5, getWidth() - 5, getHeight() - 5);
	}

	public void updateLabels() {
		begin = Long.MAX_VALUE;
		long end = Long.MIN_VALUE;

		for (DustHandle hEvt : txtAgent.events.values()) {
			Date start = DustUtilsData.getEventDate(hEvt);
			long s = start.getTime();
			if (s < begin) {
				begin = s;
			}

			long duration = Dust.access(DustAccess.Peek, 0L, hEvt, TOKEN_EVENT_DURATION);
			long e = s + duration;
			if (e > end) {
				end = e;
			}
		}

		length = end - begin;

//		long offset = begin - zero;

//		for (Map.Entry<DustHandle, DustHandle> ee : txtAgent.events.entrySet()) {
//			DustHandle hEvt = ee.getValue();
//
//			try {
//				Date start = DustUtilsData.getEventDate(hEvt);
//				long s = start.getTime() - begin;
//				long duration = Dust.access(DustAccess.Peek, 0L, hEvt, TOKEN_EVENT_DURATION);
//
//				DustHandle hTxt = ee.getKey();
//				String tId = hTxt.getId();
//				String txt = tId; // txtAgent.accessText(DustAccess.Peek, "", tId);
//				JLabel lbl = factLabels.get(hTxt);
//
//				lbl.setBounds((int) s, 2, (int) (zoom * (double) duration), 26);
//				lbl.setText(txt);
//
//				pnlTimeline.add(lbl);
//			} catch (Exception e) {
//				DustException.swallow(e);
//				continue;
//			}
//		}

	}
}
