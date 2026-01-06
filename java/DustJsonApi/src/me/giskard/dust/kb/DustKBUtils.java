package me.giskard.dust.kb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import me.giskard.dust.Dust;
import me.giskard.dust.utils.DustUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustKBUtils implements DustKBConsts {

	public static synchronized KBUnit bootLoadAppUnitJsonApi(File f) throws Exception {
		if (f.isFile()) {
			if (null == DustKBStore.appUnit) {
				String unitId = DustUtils.cutPostfix(f.getName(), ".");
				DustKBStore.appUnit = new DustKBUnit(null, unitId);
//		} else {
//			return DustException.wrap(null, "DustKBStore.appUnit already set");
			}
			DustKBSerializerJsonApi.loadFile(DustKBStore.appUnit, f);
		}

		return DustKBStore.appUnit;
	}

	public static void loadExtFile(KBUnit unit, File extFile) throws IOException, FileNotFoundException {
		if (extFile.isFile()) {
			try (FileInputStream fis = new FileInputStream(extFile); BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
				String line;
				while ((line = br.readLine()) != null) {
					line = line.trim();

					if (!DustUtils.isEmpty(line)) {
						String[] ext = line.split("\\|");

						String[] access = ext[0].trim().split("/");
						KBObject aCfg = unit.getObject(access[0], access[1], KBOptCreate.None);

						if (null == aCfg) {
							Dust.log(TOKEN_LEVEL_WARNING, "No object found for id", ext[0]);
						} else {
							String val = ext[2].trim();
							Object v = val;

							if (val.startsWith("!!")) {
								switch (val) {
								case "!!true":
									v = true;
									break;
								case "!!false":
									v = false;
									break;
								case "!!null":
									v = null;
									break;
								default:
									v = Long.valueOf(val.substring(2));
									break;
								}
							}
							access(DustAccess.Set, v, aCfg, (Object[]) ext[1].trim().split("\\."));
							Dust.log(TOKEN_LEVEL_TRACE, "change applied", line);
						}
					}
				}
			}
		} else {
			Dust.log(TOKEN_LEVEL_WARNING, "No extension file found", extFile.getName());
		}
	}

	public static <RetType> RetType accessCtx(DustAccess access, Object val, Object root, Object... path) {
		Object ret = NOT_FOUND;

		Object main = Dust.optGetCtx(root);
		Object def = val;
		boolean pg = false;

		switch (access) {
		case Peek:
		case Get:
			pg = true;
			def = NOT_FOUND;
		case Check:
		case Visit:
			ret = ((null != main) && (main == root)) ? DustKBUtils.access(access, def, main, path) : NOT_FOUND;
			for (DustContext dc : DustContext.values()) {
				if (NOT_FOUND != ret) {
					break;
				}
				Object ctx = Dust.peekCtx(dc);
				ret = (null == ctx) ? NOT_FOUND : DustKBUtils.access(access, def, ctx, path);
			}

			if (pg && (NOT_FOUND == ret)) {
				ret = val;
			}
			break;

		case Begin:
		case Process:
		case Commit:
		case Rollback:

		case Set:
		case Insert:
		case Delete:
		case Reset:
//			ret = DustKBUtils.access(access, val, main, path);
			ret = ((null != main) && (main == root)) ? DustKBUtils.access(access, def, main, path) : NOT_FOUND;
			break;

		}

		return (RetType) ret;
	}

	public static <RetType> RetType access(DustAccess access, Object val, Object root, Object... path) {
		if ((null == root) || (root instanceof DustContext)) {
			return accessCtx(access, val, (DustContext) root, path);
		}

		Object curr = root;

		KBCollType collType = KBCollType.getCollType(root);

		Object ret = null;

		Object prev = null;
		Object lastKey = null;

		Object prevColl = null;

		if (val instanceof Enum) {
			val = ((Enum) val).name();
		}

		for (Object p : path) {
			if (p instanceof Enum) {
				p = ((Enum) p).name();
			}

			if (curr instanceof KBObject) {
				curr = ((DustKBObject) curr).content;
			} else if (null == curr) {
				if (access.creator) {
					curr = (p instanceof Integer) ? new ArrayList() : new HashMap();

					if (null != prevColl) {
						switch (collType) {
						case Arr:
							DustUtils.safePut((ArrayList) prevColl, (Integer) lastKey, val, false);
							break;
						case Map:
							((Map) prevColl).put(lastKey, curr);
							break;
						case One:
							break;
						case Set:
							((Set) prevColl).add(curr);
							break;
						}
					}
				} else {
					break;
				}
			}

			prev = curr;
			collType = KBCollType.getCollType(prev);
			prevColl = (null == collType) ? null : prev;

			lastKey = p;

			if (curr instanceof ArrayList) {
				ArrayList al = (ArrayList) curr;
				Integer idx = (Integer) p;

				if ((KEY_SIZE == idx)) {
					curr = al.size();
				} else if ((KEY_ADD == idx) || (idx >= al.size())) {
					curr = null;
				} else {
					curr = al.get(idx);
				}
			} else if (curr instanceof Map) {
				curr = DustUtils.isEqual(KEY_SIZE, p) ? ((Map) curr).size()
						: DustUtils.isEqual(KEY_MAP_KEYS, p) ? new ArrayList(((Map) curr).keySet()) : ((Map) curr).get(p);
			} else {
				curr = null;
			}

		}

		switch (access) {
		case Check:
			ret = DustUtils.isEqual(val, curr);
			break;
		case Delete:
			if (curr != null) {
				switch (collType) {
				case Arr:
					((ArrayList) prevColl).remove((int) lastKey);
					break;
				case Map:
					((Map) prevColl).remove(lastKey);
					break;
				case One:
					break;
				case Set:
					((Set) prevColl).remove(curr);
					break;
				}
			}
			ret = curr;

			break;
		case Get:
			ret = (null == curr) ? val : curr;
			break;
		case Insert:
			if (!DustUtils.isEqual(curr, val) && (null != prevColl)) {
				switch (collType) {
				case Arr:
					DustUtils.safePut((ArrayList) prevColl, (Integer) lastKey, val, false);
					break;
				case Map:
					Set s = (curr instanceof Set) ? (Set) curr : new HashSet();
					ret = s.add(val);
					((Map) prevColl).put(lastKey, s);
					break;
				case One:
					break;
				case Set:
					ret = ((Set) prevColl).add(curr);
					break;
				}
			}
			break;
		case Peek:
			if (collType == KBCollType.Set) {
				Iterator is = ((Set) prevColl).iterator();
				if (is.hasNext()) {
					curr = is.next();
				}
			}
			ret = (null == curr) ? val : curr;
			break;
		case Reset:
			if (curr instanceof Map) {
				((Map) curr).clear();
			} else if (curr instanceof Collection) {
				((Collection) curr).clear();
			}
			break;
		case Set:
			ret = curr;
			if ((null != lastKey) && (null != prevColl)) {
				switch (collType) {
				case Arr:
					DustUtils.safePut((ArrayList) prevColl, (Integer) lastKey, val, true);
					break;
				case Map:
					if (!DustUtils.isEqual(curr, val)) {
						((Map) prevColl).put(lastKey, val);
					}
					break;
				case One:
					break;
				case Set:
					((Set) prevColl).add(val);
					break;
				}
			}

			break;
		case Visit:
			if (curr == null) {
				ret = NOT_FOUND;
			} else {
				switch (KBCollType.getCollType(curr)) {
				case Arr:
				case Set:
					ret = curr;
					break;
				case Map:
					ret = ((Map) curr).entrySet();
					break;
				case One:
					ret = null;
					break;
				}
			}
			break;
		case Begin:
		case Commit:
		case Rollback:
		case Process:

			Object ll = DustKBUtils.access(DustAccess.Peek, null, curr, TOKEN_LISTENERS);
			if (ll instanceof Collection) {
				for (Object l : (Collection) ll) {
					ret = Dust.notifyAgent(access, (KBObject) l, (KBObject) curr, val);
				}
			}

			break;
		}

		return (RetType) ret;

	}
}
