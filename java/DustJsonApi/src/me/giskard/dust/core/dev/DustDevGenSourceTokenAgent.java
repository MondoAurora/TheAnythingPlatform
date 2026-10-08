package me.giskard.dust.core.dev;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFile;

@SuppressWarnings("rawtypes")
public class DustDevGenSourceTokenAgent extends DustAgent implements DustDevConsts {

	enum Mode {
		TAP01Tokens, TAP02Handles, TAP02BootTokens
	}

	Collection<String> types;
	String targetPackage;
	File root;

	String targetPackageNew;
	File rootNew;

	Collection<DustHandle> bootTokens;

	Map temp = new HashMap();

	@Override
	protected void init() throws Exception {
		super.init();
		types = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, null, TOKEN_MISC_ATT_MEMBERS);
		targetPackage = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, null, TOKEN_DEV_ATT_PACKAGE);
		targetPackageNew = targetPackage.replace("tokens", "handles");
//		targetPackageNew = targetPackage + "_new";

		bootTokens = Dust.access(DustAccess.Peek, null, null, TOKEN_DEV_ATT_MACHINE_IMPL,
				TOKEN_DEV_ATT_MACHINE_BOOT_TOKENS);

		String projectRoot = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, null, TOKEN_MISC_ATT_PATH);
		root = new File(new File(projectRoot), targetPackage.replace('.', '/'));
		rootNew = new File(new File(projectRoot.replace("DustJsonApi", "TAP02")), targetPackageNew.replace('.', '/'));

		DustUtilsFile.ensureDir(root);
		DustUtilsFile.ensureDir(rootNew);

	}

	@Override
	protected Object begin() throws Exception {
		temp.clear();
		return super.begin();
	}

	@Override
	protected Object process(DustAccess access) throws Exception {
//		String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);

		DustHandle data = Dust.access(DustAccess.Peek, null, null, TOKEN_MISC_ATT_DATA);

		if (null != data) {
			String unit = data.getUnit().getId();
			String type = data.getType().getId();
			String name = Dust.access(DustAccess.Peek, null, data, TOKEN_MISC_ATT_NAME);

			Dust.access(DustAccess.Set, name, data, TOKEN_DEV_ATT_TOKEN);

			Dust.access(DustAccess.Set, data, temp, TOKEN_MISC_ATT_DATA, unit, type, name);
		}

		Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "Source generator received", data);

		return null;
	}

	@Override
	protected Object end(boolean commit) throws Exception {
		Map<String, Object> tokens = Dust.access(DustAccess.Peek, null, temp, TOKEN_MISC_ATT_DATA);

		Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "Generate sources from", tokens);

		for (Map.Entry<String, Object> ue : tokens.entrySet()) {
			genJava(ue, Mode.TAP01Tokens);
			genJava(ue, Mode.TAP02Handles);
		}

		Collection<DustHandle> bootTokens = Dust.access(DustAccess.Peek, null, null, TOKEN_DEV_ATT_MACHINE_IMPL,
				TOKEN_DEV_ATT_MACHINE_BOOT_TOKENS);

		if (null != bootTokens) {
			String pn = "me.giskard.boot";
			String cName = "DustGenBootConstsTest";

			File r = new File("../TAP02/gen/me/giskard/boot");
			PrintStream ps = createSrcStream(r, pn, cName, null, Mode.TAP02BootTokens);

			for (DustHandle ht : bootTokens) {
				String key = Dust.access(DustAccess.Peek, null, ht, TOKEN_DEV_ATT_TOKEN);
				ps.print("\tString ");
				ps.print(key);
				ps.print(" = ");
				ps.print("\"");
				ps.print(ht.getId());
				ps.print("\";");
				ps.println();

			}

			ps.println("}");
			ps.flush();
			ps.close();

		}
		temp.clear();

		return null;
	}

	public void genJava(Map.Entry<String, Object> ue, Mode mode) throws Exception, FileNotFoundException {
		PrintStream ps = null;
		boolean newGen = mode == Mode.TAP02Handles;
		String pn = newGen ? targetPackageNew : targetPackage;
		File r = newGen ? rootNew : root;

		String unit = ue.getKey().replace('.', '_');
		int s = unit.indexOf("/");

		String author = unit.substring(0, s);
		String cName = (newGen ? "DustGenHandles_" : "DustGenTokens_") + unit.substring(s + 1);

		for (String t : types) {
			Map<String, DustHandle> tm = Dust.access(DustAccess.Peek, null, ue.getValue(), t);
			boolean first = true;

			if (null != tm) {

				Collection<String> keys = new TreeSet<>();
				keys.addAll(tm.keySet());

				for (String key : keys) {
					if (first) {
						first = false;

						if (null == ps) {
							ps = createSrcStream(r, pn, cName, author, mode);
						} else {
							ps.println();
						}

						ps.print("// ");
						ps.print(DustUtils.getPostfix(t, DUST_SEP_TOKEN));
						ps.println("s");
					}

					ps.print(newGen ? "\tDustHandle " : "\tString ");
					ps.print(newGen ? key.replace("TOKEN_", "HANDLE_") : key);
					ps.print(" = ");
					if (newGen) {
						ps.print("Dust.getHandle(");
					}
					ps.print("\"");
					ps.print(tm.get(key).getId());
					ps.print(newGen ? "\");" : "\";");
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

	public PrintStream createSrcStream(File r, String pn, String cName, String author, Mode mode) throws Exception {
		boolean newGen = mode == Mode.TAP02Handles;

		File f = new File(r, ((null == author) ? "" : author + "/") + cName + ".java");
		DustUtilsFile.ensureDir(f.getParentFile());

		PrintStream ps;
		ps = new PrintStream(f);

		ps.print("package ");
		ps.print(pn);
		if (null != author) {
			ps.print(".");
			ps.print(author);
		}
		ps.print(";");
		ps.println();
		if (newGen) {
			ps.println();
			ps.println("import me.giskard.dust.api.Dust;");
			ps.println("import me.giskard.dust.api.DustHandle;");
		}
		ps.println();

		ps.println("// Generation timestamp " + DustUtils.strTime());
		ps.println();

		ps.print("public interface ");
		ps.print(cName);
//		if ( mode == Mode.TAP02BootTokens ) {
//			ps.print(" extends DustConsts");
//		}
		ps.print(" {");
		ps.println();
		return ps;
	}

}
