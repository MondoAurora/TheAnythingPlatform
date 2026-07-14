package me.giskard.dust.core.dev;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFile;

@SuppressWarnings("rawtypes")
public class DustDevGenSourceTokenAgent extends DustAgent implements DustDevConsts {

	Collection<String> types;
	String targetPackage;
	File root;

	Map temp = new HashMap();

	@Override
	protected void init() throws Exception {
		super.init();
		types = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, null, TOKEN_MEMBERS);
		targetPackage = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, null, TOKEN_DEV_PACKAGE);

		String projectRoot = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, null, TOKEN_PATH);
		root = new File(new File(projectRoot), targetPackage.replace('.', '/'));

		DustUtilsFile.ensureDir(root);
	}

	@Override
	protected Object begin() throws Exception {
		temp.clear();
		return super.begin();
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
//		String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);

		DustHandle data = Dust.access(DustAccess.Peek, null, null, TOKEN_DATA);

		if (null != data) {
			String unit = data.getUnit().getId();
			String type = data.getType().getId();
			String name = Dust.access(DustAccess.Peek, null, data, TOKEN_NAME);

			Dust.access(DustAccess.Set, data, temp, TOKEN_DATA, unit, type, name);
		}

		Dust.log(TOKEN_LEVEL_TRACE, "Source generator received", data);

		return null;
	}

	@Override
	protected Object end(boolean commit) throws Exception {
		Map<String, Object> tokens = Dust.access(DustAccess.Peek, null, temp, TOKEN_DATA);

		Dust.log(TOKEN_LEVEL_TRACE, "Generate sources from", tokens);

		for (Map.Entry<String, Object> ue : tokens.entrySet()) {
			PrintStream ps = null;

			String unit = ue.getKey().replace('.', '_');
			String cName = "DustGenTokens_" + unit;

			for (String t : types) {
				Map<String, DustHandle> tm = Dust.access(DustAccess.Peek, null, ue.getValue(), t);
				boolean first = true;

				if (null != tm) {

					for (Map.Entry<String, DustHandle> te : tm.entrySet()) {
						if (first) {
							first = false;

							if (null == ps) {
								File f = new File(root, cName + ".java");
								ps = new PrintStream(f);

								ps.print("package ");
								ps.print(targetPackage);
								ps.print(";");
								ps.println();
								ps.println();

								ps.print("public interface ");
								ps.print(cName);
								ps.print(" {");
								ps.println();
							} else {
								ps.println();
							}

							ps.print("// ");
							ps.print(DustUtils.getPostfix(t, DUST_SEP_TOKEN));
							ps.println("s");
						}

						ps.print("\tString ");
						ps.print(te.getKey());
						ps.print(" = \"");
						ps.print(te.getValue().getId());
						ps.print("\";");
						ps.println();
					}
				}
			}

			if (null != ps) {
				ps.println("}");
				ps.flush();
				ps.close();
			}
		}

		temp.clear();

		return null;
	}

}
