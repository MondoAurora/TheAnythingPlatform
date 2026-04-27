package me.giskard.dust.sandbox.text;

import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;

public class DustSandboxTextUtils implements DustSandboxTextConsts {
	public static String getId(Element e) {
		SimpleAttributeSet sas = (SimpleAttributeSet) e.getAttributes().getAttribute(HTML.Tag.SPAN);
		String id = (null == sas) ? null : (String) sas.getAttribute(HTML.Attribute.ID);

		return (null == id) ? (String) e.getAttributes().getAttribute(HTML.Attribute.ID) : id;
	}

}
