package me.giskard.dust.sandbox.text;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.utils.DustUtils;

public class DustSandboxTextSelectionManager implements DustSandboxTextConsts {
//	private DustHandle hUnit;
	DustSandboxTextAgent txtAgent;

	private final HTMLDocument doc;
	private DefaultMutableTreeNode rootNode;

	 Object selUpdating = null;

	private int caretPos;
	int selBegin;
	int selEnd;

	Element eFocus;
	Element eParent;

	DustHandle hfChar;
	DustHandle hfBlock;
	DustHandle hfParent;

	final ArrayList<DustHandle> hSel = new ArrayList<>();
	final Set<JComponent> managedComps = new HashSet<>();

	TreeSelectionListener treeSelListener = new TreeSelectionListener() {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			if (null == selUpdating) {
				synchronized (this) {
					try {
						selUpdating = e.getSource();

						reset();

						TreePath sp = e.getNewLeadSelectionPath();
						if (null == sp) {
							return;
						}

						DefaultMutableTreeNode n = (DefaultMutableTreeNode) sp.getLastPathComponent();

						DustHandle h = (DustHandle) n.getUserObject();
						DustHandle t = Dust.access(DustAccess.Peek, h, h, DustSandboxTextEditor.TOKEN_TARGET); // opt relay from table cell
						hfBlock = (null == t) ? h : t;

						eSelByHandle(hfBlock);

						hSel.clear();

						for (TreePath tp : ((JTree) e.getSource()).getSelectionPaths()) {
							hSel.add((DustHandle) ((DefaultMutableTreeNode) tp.getLastPathComponent()).getUserObject());
						}

						updateComps((JComponent) e.getSource());
					} finally {
						selUpdating = null;
					}
				}
			}
		}
	};

	CaretListener caretListener = new CaretListener() {

		@Override
		public void caretUpdate(CaretEvent ce) {
			if (null == selUpdating) {
				synchronized (this) {
					try {
						selUpdating = ce.getSource();

						reset();

						JTextComponent txt = (JTextComponent) ce.getSource();

						caretPos = txt.getCaretPosition();

						int b = ce.getMark();
						int e = ce.getDot();
						boolean rev = b > e;
						if (rev) {
							int a = b;
							b = e;
							e = a;
						}

						Element eb = getTopSelf(doc.getParagraphElement(b));
						Element ee = getTopSelf(doc.getParagraphElement(e));

						eFocus = ee;

//						String idThis = (String) eFocus.getAttributes().getAttribute(HTML.Attribute.ID);
//						if (DustUtils.isEmpty(idThis)) {
//							return;
//						}
//
//						hfBlock = Dust.getHandle(hUnit, null, idThis, DustOptCreate.None);

						boolean extendSel = false;

						if (eb == ee) {
							Element ec = doc.getCharacterElement(b);
							eFocus = ec;

							SimpleAttributeSet sas = (SimpleAttributeSet) ec.getAttributes().getAttribute(HTML.Tag.SPAN);
							Object name = (null == sas) ? null : sas.getAttribute(HTML.Attribute.NAME);
							
							String ii = DustSandboxTextUtils.getId(ec);
							if (!DustUtils.isEmpty(ii)) {
								hfChar = txtAgent.getNode(ii);
							}
							
							if (DustUtils.isEqual("placeholder", name)) {
								selBegin = ec.getStartOffset();
								selEnd = ec.getEndOffset() - 1;
								extendSel = true;
							}
						} else {
//							Element pe = ee.getParentElement();
							boolean in = false;
							for (int i = 0; i < eParent.getElementCount(); ++i) {
								Element ei = eParent.getElement(i);
								if (eb == ei) {
									in = true;
								}
								if (in) {
									String ii = DustSandboxTextUtils.getId(ei);
									if (!DustUtils.isEmpty(ii)) {
										DustHandle hi = txtAgent.getNode(ii);
										if (null != hi) {
											hSel.add(hi);
										}
									}
									if (ee == ei) {
										in = false;
									}
								}
							}
							selBegin = eb.getStartOffset();
							selEnd = ee.getEndOffset() - 1;
							extendSel = true;
						}

						Dust.log(DustSandboxTextEditor.TOKEN_LEVEL_TRACE, "select", b, e, selBegin, selEnd);

						updateFocus();

						if (extendSel) {
							txt.setCaretPosition(rev ? selEnd : selBegin);
							txt.moveCaretPosition(rev ? selBegin : selEnd);
						}

						updateComps(txt);

					} finally {
						selUpdating = null;
					}
				}
			}
		}
	};

	public DustSandboxTextSelectionManager(DustSandboxTextAgent txtAgent, HTMLDocument doc) {
//		this.hUnit = hUnit;
		this.txtAgent = txtAgent;
		this.doc = doc;
	}

	public void attach(JComponent c) {
		if (managedComps.add(c)) {
			if (c instanceof JTree) {
				JTree tr = (JTree) c;
				tr.addTreeSelectionListener(treeSelListener);
				rootNode = (DefaultMutableTreeNode) tr.getModel().getRoot();
			} else if (c instanceof JTextComponent) {
				JTextComponent txt = (JTextComponent) c;
				txt.addCaretListener(caretListener);
			}
		}
	}

	public int getCaretPos() {
		return caretPos;
	}

	public DustHandle getFocusedBlock() {
		return hfBlock;
	}

	public DustHandle getFocusedParent() {
		return hfParent;
	}

	void reset() {
		caretPos = selBegin = selEnd = -1;
		hfChar = hfBlock = hfParent = null;
		hSel.clear();
	}

	private boolean updateFocus() {
		eFocus = getTopSelf(eFocus);
		if (null != eFocus) {
			hfBlock = hfParent = null;
			String id = DustSandboxTextUtils.getId(eFocus);
			if (null != id) {
				hfBlock = txtAgent.getNode(id);
				eParent = getTopSelf(eFocus.getParentElement());
				if (null != eParent) {
					String pid = DustSandboxTextUtils.getId(eParent);
					hfParent = txtAgent.getNode(pid);
				}
			}
		}

		return (null != hfBlock);
	}

	private Element getTopSelf(Element e) {
		if (null == e) {
			return null;
		}

		String ei = DustSandboxTextUtils.getId(e);

		for (Element ep = e.getParentElement(); (null != ep)
				&& DustUtils.isEqual(ei, ep.getAttributes().getAttribute(HTML.Attribute.ID)); ep = e.getParentElement()) {
			e = ep;
		}

		return e;
	}

	private void eSelByHandle(DustHandle h) {
		reset();
		String id = h.getId();
		eFocus = doc.getElement(id);
		updateFocus();

		hSel.add(h);
		if (null != eFocus) {
			selBegin = eFocus.getStartOffset();
			selEnd = eFocus.getEndOffset();
		}		
	}

	JComponent updateComps(JComponent src) {
		JComponent ret = null;
//		if ( null == ret ) {
//			 return ret;
//		}
		
		for (JComponent c : managedComps) {
			if ((c != src) && (c != selUpdating)) {
				if (c instanceof JTree) {
					if (null != hfBlock) {
						JTree tr = (JTree) c;

//					String idThis = (String) eFocus.getAttributes().getAttribute(HTML.Attribute.ID);
//					if (!DustUtils.isEmpty(idThis)) {
//						DustHandle hThis = Dust.getHandle(hUnit, null, idThis, DustOptCreate.None);

						TreePath tp = null;
						for (Enumeration<TreeNode> se = rootNode.depthFirstEnumeration(); se.hasMoreElements();) {
							DefaultMutableTreeNode node = (DefaultMutableTreeNode) se.nextElement();
							if (hfBlock == node.getUserObject()) {
								tp = new TreePath(node.getPath());
								tr.setSelectionPath(tp);
								tr.scrollPathToVisible(tp);
							}
						}
					}

				} else if (c instanceof JTextComponent) {
					JTextComponent txt = (JTextComponent) c;
//					if (!txt.getText().isEmpty()) 
					{
						if (-1 == selBegin) {
							if (-1 != caretPos) {
								txt.setCaretPosition(caretPos);
							}
						} else {
//							txt.moveCaretPosition(selEnd);
							txt.setCaretPosition(selEnd);
							txt.moveCaretPosition(selBegin);
						}
					}

					if (c.isShowing()) {
						c.requestFocusInWindow();
					}

				}
			}
		}
		return ret;
	}
}