package me.giskard.dust.sandbox.browser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.MouseInputAdapter;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAccess;
import me.giskard.dust.core.DustConsts.DustCollType;
import me.giskard.dust.core.DustConsts.DustHandle;
import me.giskard.dust.core.DustConsts.DustOptCreate;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsConsts.DustCreator;
import me.giskard.dust.core.utils.DustUtilsFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
class DustGuiSwingGraphPanel {
	DustGuiSwingBrowserPanel browserPanel;

	double zoomFactor = 1.0;

	Map<DustHandle, DustHandle> nodePool = new HashMap<>();

	DustCreator<DustHandle> graphNodeCreator = new DustCreator<DustHandle>() {
		@Override
		public DustHandle create(Object key, Object... hints) {
			DustHandle hNode = nodePool.remove(key);

			if (null == hNode) {
				hNode = Dust.getHandle(browserPanel.hDocUnit, DustGuiSwingBrowserPanel.TOKEN_GEOMETRY_ASP_LOCATION, null, DustOptCreate.Primary);
				Dust.access(DustAccess.Set, key, hNode, DustGuiSwingBrowserPanel.TOKEN_MISC_ATT_TARGET);

				// set location
			}

			return hNode;
		}
	};

	DustUtilsFactory<DustHandle, DustHandle> factNodes = new DustUtilsFactory<DustHandle, DustHandle>(graphNodeCreator, false) {
		public DustHandle remove(DustHandle key) {
			DustHandle ret = super.remove(key);

			if (null != ret) {
				nodePool.put(key, ret);
			}

			return ret;
		};
	};

	Collection<DustHandle> selected;
	Set<String> showLinks;

	JPanel cmpGraph = new JPanel(null) {

		@Override
		protected void paintChildren(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;

			AffineTransform at = g2d.getTransform();

			g2d.scale(zoomFactor, zoomFactor);

			for (DustHandle h : factNodes.keys()) {
				JComponent hc = comps.get(factNodes.get(h));
				Rectangle rct = hc.getBounds(null);
				Point pt1 = new Point((int) rct.getCenterX(), (int) rct.getCenterY());

				if (selected.contains(h)) {
					int sx = rct.x - 5;
					int sy = rct.y - 5;
					if (null != anchor) {
						sx += dx;
						sy += dy;
					}
					
					Color c = null;
					boolean f = h == browserPanel.getFocused();
					
					if ( f ) {
						c = g2d.getColor();
						g2d.setColor(Color.GREEN);
					}
					
					g2d.drawRoundRect(sx, sy, rct.width + 10, rct.height + 10, 4, 4);
					if ( f ) {
						g2d.setColor(c);
					}
				}

				for (String l : showLinks) {
					Object val = Dust.access(DustAccess.Peek, null, h, l);

					if (val instanceof Map) {
						val = ((Map) val).values();
					}
					if (val instanceof Collection) {
						for (Object lt : (Collection) val) {
							optDrawLine(h, l, lt, g2d, rct, pt1);
						}
					}
					if (val instanceof DustHandle) {
						optDrawLine(h, l, val, g2d, rct, pt1);
					}
				}
			}

			super.paintChildren(g);

			g2d.setTransform(at);
		}

		public void optDrawLine(DustHandle hSrc, String l, Object target, Graphics2D g2d, Rectangle rct, Point from) {
			DustHandle ht = factNodes.peek((DustHandle) target);
			JComponent tc = comps.peek(ht);

			if (null != tc) {
				String lbl = browserPanel.getAttType(l).name() + " " + l;

				if (hSrc == target) {
					g2d.drawOval(from.x - 15, from.y - 50, 30, 50);
					g2d.drawString(lbl, from.x, from.y - 50);
				} else {
					tc.getBounds(rct);
					int toX = (int) rct.getCenterX();
					int toY = (int) rct.getCenterY();
					g2d.drawLine(from.x, from.y, toX, toY);

					double d = 50.0 / from.distance(toX, toY);
					int px = from.x + (int) (d * (toX - from.x));
					int py = from.y + (int) (d * (toY - from.y));

					g2d.drawString(lbl, px, py);
				}
			}
		}
	};

	JScrollPane scpGraph = new JScrollPane(cmpGraph);

	Point anchor = null;
	int dx = 0;
	int dy = 0;

	String[] GRAPH_MODES = { "Select", "Remove Link", "Create Link" };
	JComboBox<String> cbMode = new JComboBox<String>(GRAPH_MODES);

	MouseInputAdapter ma = new MouseInputAdapter() {

		@Override
		public void mouseClicked(java.awt.event.MouseEvent e) {
			DustHandle h = findHandle(e);
//				int mod = e.getModifiersEx();

			if (null != h) {

				switch ((String) cbMode.getSelectedItem()) {
				case "Select":
					if (selected.contains(h)) {
						selected.remove(h);
						h = null;
					} else {
						if (!e.isShiftDown()) {
							selected.clear();
						}

						selected.add(h);
					}
					break;

				case "Remove Link":
					for (String l : showLinks) {
						for (DustHandle hs : selected) {
							Dust.access(DustAccess.Delete, h, hs, l);
						}
					}
					
					h = null;

					break;

				case "Create Link":
					if (1 != showLinks.size()) {
						JOptionPane.showMessageDialog(cmpGraph, "Only works with one selected link", "Create link error", JOptionPane.ERROR_MESSAGE);
						break;
					}
					String l = showLinks.iterator().next();
					DustCollType ct = browserPanel.getAttType(l);
					String key = null;
					if (ct == DustCollType.Map) {
						key = JOptionPane.showInputDialog(cmpGraph, "Key?", "Create Map link", JOptionPane.QUESTION_MESSAGE);
						if (DustUtils.isEmpty(key)) {
							break;
						}
					}
					for (DustHandle hs : selected) {
						switch (ct) {
						case Arr:
							Dust.access(DustAccess.Insert, h, hs, l, DustGuiSwingBrowserPanel.KEY_ADD);
							break;
						case Map:
							Dust.access(DustAccess.Insert, h, hs, l, key);
							break;
						case One:
							Dust.access(DustAccess.Set, h, hs, l);
							break;
						case Set:
							Dust.access(DustAccess.Insert, h, hs, l);
							break;
						}
					}
					
					h = null;

					break;
				}

				if (!e.isShiftDown()) {
					cbMode.setSelectedIndex(0);
				}
				browserPanel.setFocused(h);
			}
		};

		Point getModelPoint(Point pt) {
			int x = (int) ((double) pt.x / zoomFactor);
			int y = (int) ((double) pt.y / zoomFactor);

			return new Point(x, y);
		}

		public DustHandle findHandle(java.awt.event.MouseEvent e) {
			Point pt = getModelPoint(e.getPoint());

			Rectangle rct = null;
			for (DustHandle h : comps.keys()) {
				JComponent c = comps.get(h);
				rct = c.getBounds(rct);

				if (rct.contains(pt)) {
					h = Dust.access(DustAccess.Peek, null, h, DustGuiSwingBrowserPanel.TOKEN_MISC_ATT_TARGET);
					return h;
				}
			}

			return null;
		};

		@Override
		public void mousePressed(java.awt.event.MouseEvent e) {
			anchor = getModelPoint(e.getPoint());
		};

		@Override
		public void mouseDragged(java.awt.event.MouseEvent e) {
			Point pt = getModelPoint(e.getPoint());

			dx = pt.x - anchor.x;
			dy = pt.y - anchor.y;

			repaintGraph();
		};

		@Override
		public void mouseReleased(java.awt.event.MouseEvent e) {
			Point pt = getModelPoint(e.getPoint());

			int dx = pt.x - anchor.x;
			int dy = pt.y - anchor.y;
			anchor = null;

			for (DustHandle h : selected) {
				DustHandle hn = factNodes.peek(h);
				JComponent c = comps.peek(hn);
				if (null != c) {
					pt = c.getLocation(pt);
					c.setLocation(pt.x + dx, pt.y + dy);
				}
			}

			repaintGraph();
		};

		@Override
		public void mouseMoved(java.awt.event.MouseEvent e) {
//				DustHandle h = findHandle(e);
		}

	};

	public DustGuiSwingGraphPanel(DustGuiSwingBrowserPanel browserPanel) {
		this.browserPanel = browserPanel;
		this.showLinks = browserPanel.showLinks;
		this.selected = browserPanel.selected;

		cmpGraph.addMouseListener(ma);
		cmpGraph.addMouseMotionListener(ma);
		cmpGraph.addMouseWheelListener(ma);

		cmpGraph.setPreferredSize(new Dimension(2000, 1000));

		cbMode.setSelectedIndex(0);
	}

	int i = 20;

	DustUtilsFactory<DustHandle, JComponent> comps = new DustUtilsFactory(new DustCreator<JComponent>() {
		@Override
		public JComponent create(Object key, Object... hints) {
			DustHandle ht = Dust.access(DustAccess.Peek, null, key, DustGuiSwingBrowserPanel.TOKEN_MISC_ATT_TARGET);
			String txt = (null == ht) ? "??"
					: new StringBuilder("<html><center>").append(ht.toString().replace(" [", "<br/>[")).append("</center></html>").toString();
			JLabel lbl = new JLabel(txt);
			Dimension d = lbl.getPreferredSize();
			lbl.setBounds(i, i, d.width, d.height);
			i += 20;
			return lbl;
		}
	}, false);

	public void changeZoomFactor(Double d) {
		if (null == d) {
			zoomFactor = 1.0;
		} else {
			zoomFactor *= d;
		}

		repaintGraph();
	}

	public void repaintGraph() {
		cmpGraph.removeAll();
		for (DustHandle h : factNodes.keys()) {
			JComponent hc = comps.get(factNodes.get(h));
			cmpGraph.add(hc);
		}

		cmpGraph.invalidate();
		scpGraph.repaint();
	}

	public void randomize() {
		Random rnd = new Random();

		Rectangle rct = scpGraph.getViewport().getBounds(null);
		int w = rct.width;
		int h = rct.height;

		Iterable<DustHandle> k;

		if (selected.isEmpty()) {
			k = comps.keys();
		} else {
			Set<DustHandle> s = new HashSet<DustHandle>();
			for (DustHandle hs : selected) {
				DustHandle hn = factNodes.peek(hs);
				if (null != hn) {
					s.add(hn);
				}
			}
			k = s;
		}

		for (DustHandle hh : k) {
			JComponent hc = comps.peek(hh);
			rct = hc.getBounds(rct);
			int x = rnd.nextInt(w - rct.width);
			int y = rnd.nextInt(h - rct.height);

			hc.setLocation(x, y);
		}

		repaintGraph();

	}

	public void showHandle(DustHandle hs, boolean show) {
		showHandle(hs, show, true);
	}

	public void showHandle(DustHandle hs, boolean show, boolean repaint) {
		if (show) {
			factNodes.get(hs);
		} else {
			DustHandle hn = factNodes.remove(hs);
			comps.remove(hn);
		}
		if (repaint) {
			repaintGraph();
		}
	};

}