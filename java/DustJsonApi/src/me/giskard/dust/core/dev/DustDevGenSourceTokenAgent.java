package me.giskard.dust.core.dev;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts.DustAgent;
import me.giskard.dust.core.machine.DustMachineUtils;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFile;

@SuppressWarnings("rawtypes")
public class DustDevGenSourceTokenAgent extends DustAgent implements DustDevConsts {

	enum Mode {
		TAP01Tokens, TAP02Handles, TAP02BootTokens, TAP02AppNodes
	}

	Collection<String> types;
	String targetPackage;
	File root;

	String targetPackageNew;
	File rootNew;

	Map temp = new HashMap();

	@Override
	protected void init() throws Exception {
		super.init();
		types = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, null, TOKEN_MISC_ATT_MEMBERS);
		targetPackage = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, null, TOKEN_DEV_ATT_PACKAGE);
		targetPackageNew = targetPackage.replace("tokens", "handles");

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
				TOKEN_DEV_ATT_BOOT_TOKENS);
		Map<String, DustHandle> btSort = new TreeMap<String, DustHandle>();

		if (null != bootTokens) {
			String pn = "me.giskard.boot";
			String cName = "DustGenBootConsts";

			File r = new File("../TAP02/gen/me/giskard/boot");
			PrintStream ps = createSrcStream(r, pn, cName, null, Mode.TAP02BootTokens);

			for (DustHandle ht : bootTokens) {
				String key = Dust.access(DustAccess.Peek, null, ht, TOKEN_DEV_ATT_TOKEN);
				btSort.put(key, ht);
			}

			String pref = null;

			for (Map.Entry<String, DustHandle> bte : btSort.entrySet()) {
				String key = bte.getKey();

				String[] kk = key.split(DUST_SEP);
				String k = DustUtils.sbAppend(null, DUST_SEP, false, kk[0], kk[1], kk[2]).toString();
				if (!DustUtils.isEqual(k, pref)) {
					ps.println();
					pref = k;
				}

				ps.print("\tString ");
				ps.print(key);
				ps.print(" = ");
				ps.print("\"");
				ps.print(bte.getValue().getId());
				ps.print("\";");
				ps.println();

			}

			ps.println("}");
			ps.flush();
			ps.close();

		}

		Collection<DustHandle> bootNodes = Dust.access(DustAccess.Peek, null, null, TOKEN_DEV_ATT_BOOT_NODES);

		if (null != bootNodes) {
			Set<String> SKIP_KEYS = new HashSet<String>();

			SKIP_KEYS.add(TOKEN_MIND_ATT_ID);
			SKIP_KEYS.add(TOKEN_MIND_ATT_TYPE);
			SKIP_KEYS.add(TOKEN_MIND_ATT_UNIT);
			SKIP_KEYS.add(TOKEN_DEV_ATT_BOOT_TOKENS);

			String pn = "me.giskard.boot";
			String cName = "DustGenBootApp";

			File r = new File("../TAP02Test01/gen/me/giskard/boot");
			PrintStream ps = createSrcStream(r, pn, cName, null, Mode.TAP02AppNodes);

			Set<String> lines = new TreeSet<String>();
			ArrayList<String> actions = new ArrayList<String>();
			Map<String, DustHandle> ideas = new TreeMap<String, DustHandle>();
			Map<String, DustHandle> atts = new TreeMap<String, DustHandle>();
			Map<DustHandle, String> all = new HashMap<DustHandle, String>();
			Map<DustHandle, String> impl = new HashMap<DustHandle, String>();

			String kCtxApp = addToken(Dust.getHandle(TOKEN_DUST_ATT_CTX_APP), "att_", atts, all);
			String kBinRes = addToken(Dust.getHandle(TOKEN_DUST_ATT_BINARY_RESOLVER), "att_", atts, all);
			String kUnitHandler = addToken(Dust.getHandle(TOKEN_STREAM_ATT_UNIT_HANDLER), "att_", atts, all);
			String kAttNode = addToken(Dust.getHandle(TOKEN_DUST_ATT_NODE), "att_", atts, all);
			
			ps.println("\t\tswitch(appId) {");
			

			for (DustHandle hNode : bootNodes) {
				ps.println(DustUtils.sbAppend(null, "", false, "\t\tcase \"", hNode.getId(), "\" : {").toString());
				
				Collection<DustHandle> modules = Dust.access(DustAccess.Get, Collections.EMPTY_LIST, hNode,
						TOKEN_MISC_ATT_PARENT, TOKEN_DUST_ATT_MODULES);

				for (DustHandle mod : modules) {
					DustHandle mu = mod.getUnit();
					for (DustHandle h : DustMachineUtils.getUnitMembers(mu)) {
						if (TOKEN_DUST_ASP_IMPLEMENTATION.equals(h.getType().getId())) {
							DustHandle target = Dust.access(DustAccess.Get, null, h, TOKEN_MISC_ATT_TARGET);
							String key = Dust.access(DustAccess.Get, null, h, TOKEN_MISC_ATT_KEY);
							impl.put(target, key);
						}
					}
				}

				bootTokens = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, hNode, TOKEN_DEV_ATT_BOOT_TOKENS);
				for (DustHandle hToken : bootTokens) {
					addToken(hToken, "idea_", ideas, all);
				}
//				String kNode = addToken(hNode, "idea_", ideas, all);

				for (DustHandle hToken : ideas.values()) {
					actions.add("\n\t\t// " + hToken);

					if (TOKEN_MIND_ASP_NARRATIVE.equals(hToken.getType().getId())) {
//						String cn = "\"" + Dust.getClassName(hToken.getId()) + "\"";
						String cn = "\"" + impl.get(hToken) + "\"";
						actions.add(DustUtils.sbAppend(null, ", ", false, "\t\t\tDust.access(DustAccess.Set", cn, "null", kCtxApp,
								kBinRes, all.get(hToken)).toString() + ");");
					}

					for (String an : DustMachineUtils.getAttNames(hToken)) {
						if (SKIP_KEYS.contains(an)) {
							continue;
						}
						DustHandle hAtt = Dust.getHandle(an);

						Object o = Dust.access(DustAccess.Peek, null, hToken, an);
						if (o instanceof DustHandle) {
							o = all.get(o);
						} else if (o instanceof Collection) {
							for (Object o2 : (Collection) o) {
								if (o2 instanceof DustHandle) {
									o2 = all.get(o2);
								}
								optAddAction(actions, o2, all.get(hToken), hAtt, atts, all, true);
							}
							o = null;
						} else if (o instanceof String) {
							o = "\"" + o + "\"";
						}

						optAddAction(actions, o, all.get(hToken), hAtt, atts, all, false);
					}
				}

				ps.println("\n// Attributes");
				lines.clear();
				for (Map.Entry<String, DustHandle> ei : atts.entrySet()) {
					lines.add(DustUtils.sbAppend(null, "", false, "\t\t\tDustHandle ", ei.getKey(), " = Dust.getHandle(\"",
							ei.getValue().getId(), "\");").toString());
				}
				for (String l : lines) {
					ps.println(l);
				}

				ps.println("\n// Ideas");
				lines.clear();
				for (Map.Entry<String, DustHandle> ei : ideas.entrySet()) {
					lines.add(DustUtils.sbAppend(null, "", false, "\t\t\tDustHandle ", ei.getKey(), " = Dust.getHandle(\"",
							ei.getValue().getId(), "\");").toString());
				}
				for (String l : lines) {
					ps.println(l);
				}

				ps.println("\n// Graph");
				for (String l : actions) {
					ps.println(l);
				}

				ps.println("\n// Set unit handler");

				ps.println("\t\t\tDustHandle hApp = Dust.getHandle(appId);");
				ps.print(
						DustUtils.sbAppend(null, ", ", false, "\t\t\tDust.access(DustAccess.Set", "hApp", "null", kCtxApp, kAttNode));
				ps.println(");");

				String unitHandlerIdeaKey = all.get(Dust.access(DustAccess.Peek, null, hNode, TOKEN_STREAM_ATT_UNIT_HANDLER));
				ps.print(DustUtils.sbAppend(null, ", ", false, "\t\t\tDust.access(DustAccess.Set", unitHandlerIdeaKey, "null",
						kCtxApp, kAttNode, kUnitHandler));
				ps.println(");");
				ps.println("\t\t} break;");

//			DustHandle hApp = Dust.getHandle(appId);
//			Dust.access(DustAccess.Set, hApp, null, HANDLE_DUST_ATT_CTX_APP, HANDLE_DUST_ATT_NODE);
//			Dust.access(DustAccess.Set, hMsgUnitHandler, null, HANDLE_DUST_ATT_CTX_APP, HANDLE_DUST_ATT_NODE, HANDLE_STREAM_ATT_UNIT_HANDLER);
			}

			ps.println("\t\tdefault:");
			ps.println("\t\t\tDustException.wrap(null, \"Unknown appId parameter\", appId);");
			ps.println("\t\tbreak;");
			
			ps.println("\t\t}");

			ps.println("\t}");
			ps.println("}");
			ps.flush();
			ps.close();

		}

		temp.clear();

		return null;
	}

	private void optAddAction(ArrayList<String> target, Object val, String idea, DustHandle hAtt, Map<String, DustHandle> atts, 
	Map<DustHandle, String> all, boolean arr) {
		if (null == val) {
			return;
		}
		String att = addToken(hAtt, "att_", atts, all);

		target.add(DustUtils.sbAppend(null, ", ", false, "\t\t\tDust.access(DustAccess." + (arr ? "Insert" : "Set"), val,
				idea, att, arr ? "KEY_ADD" : null).toString() + ");");
	}

	public String addToken(DustHandle hToken, String prefix, Map<String, DustHandle> target,
			Map<DustHandle, String> all) {
		String key;
		key = prefix + DustUtils.getJavaId(hToken);
		target.put(key, hToken);
		all.put(hToken, key);
		return key;
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
		boolean newGen = mode != Mode.TAP01Tokens;

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
			if (mode == Mode.TAP02AppNodes) {
				ps.println("import me.giskard.dust.api.DustConsts;");
				ps.println("import me.giskard.dust.api.DustException;");
			}
		}
		ps.println();

		ps.println("// Generation timestamp " + DustUtils.strTime());
		ps.println();

		switch (mode) {
		case TAP02AppNodes:
			ps.print("public class ");
			ps.print(cName);
			ps.println(" implements DustConsts {");
			ps.print("\tpublic ");
			ps.print(cName);
			ps.print(" (String appId)");

			break;
		default:
			ps.print("public interface ");
			ps.print(cName);

			break;

		}
		ps.print(" {");
		ps.println();
		return ps;
	}

}
