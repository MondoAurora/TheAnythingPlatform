package me.giskard.dust;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import me.giskard.dust.dev.DustDevUtils;
import me.giskard.dust.mind.DustMindConsts;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Dust implements DustConsts, DustMindConsts {

	private static DustHandle appUnit;
	private static DustHandle appHandle;

	private static DustMind MIND;

	private static DustHandle typeAtt;

	static DustUtilsFactory<DustContext, Object> CTX = new DustUtilsFactory(MAP_CREATOR);

	public static <RetType> RetType optGetCtx(Object in) {
		return (RetType) ((in instanceof DustContext) ? CTX.get((DustContext) in) : in);
	}

	public static <RetType> RetType peekCtx(DustContext dc) {
		return (RetType) CTX.peek(dc);
	}

	private static ArrayList<DustAgent> TORELEASE;

	private static synchronized void registerToRelease(DustAgent agent) {
		if (null == TORELEASE) {
			TORELEASE = new ArrayList<DustAgent>();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					for (DustAgent a : TORELEASE) {
						try {
							a.release();
						} catch (Throwable e) {
							DustException.swallow(e, "Releasing on shutdown", a);
						}
					}
				}
			});
		}

		TORELEASE.add(0, agent);
	}

	private static DustUtilsFactory<String, DustAgent> AGENTS = new DustUtilsFactory<String, DustAgent>(new DustCreator<DustAgent>() {
		@Override
		public DustAgent create(Object key, Object... hints) {
			DustHandle aCfg = getAgentHandle(key);

			if (null == aCfg) {
				return DustException.wrap(null, "Missing config for agent ", key);
			}

			return createInstance(getBinary(key));
		}

		@Override
		public void initNew(DustAgent a, Object key, Object... hints) {
			initAgent(key, a);
		}
	}, true);

	public static void main(String[] args) throws Exception {

		long start = System.currentTimeMillis();

		try {
			MIND = createInstance(Class.forName("me.giskard.dust.mind.DustMindAgent"));
			AGENTS.put(TOKEN_MIND, MIND);

			String appName = args[0];
			String appUnitPath = args[1];

			File f = new File(appUnitPath);
			appUnit = MIND.bootLoadAppUnit(null, f);

			String userName = System.getProperty("user.name");
			if (!DustUtils.isEmpty(userName)) {
				File d = f.getAbsoluteFile().getParentFile();
				String fn = f.getName();
				int s = fn.lastIndexOf(".");
				File f2 = new File(d, fn.substring(0, s) + "." + userName + DUST_EXT_JSON);
				MIND.bootLoadAppUnit(appUnit, f2);
			}

			MIND.bootLoadAppUnit(appUnit, new File(DUST_CRED_FILE));

			DustHandle appType = DustUtils.getMindMeta(TOKEN_TYPE_APP);
			appHandle = getHandle(appUnit, appType, appName, DustOptCreate.None);

			int s = appUnitPath.lastIndexOf(".");
			File fBin = new File(appUnitPath.substring(0, s) + "." + DUST_PLATFORM_JAVA + appUnitPath.substring(s));
			MIND.bootLoadAppUnit(appUnit, fBin);

			Dust.log(TOKEN_LEVEL_INFO, "MemInfo before init", DustDevUtils.memInfo());

			MIND.init();
			typeAtt = MIND.getHandle(null, null, TOKEN_KBMETA_ATTRIBUTE, DustOptCreate.Meta);

			for (DustHandle ca : ((Collection<DustHandle>) access(DustAccess.Peek, Collections.EMPTY_LIST, appHandle, DustUtils.getMindMeta(TOKEN_INIT)))) {

				String type = ca.getType().getId();
				String an = ca.getId();

				DustHandle h = (TOKEN_TYPE_AGENT == type) ? getHandle(appUnit, null, an, DustOptCreate.None) : ca;
				boolean skip = access(DustAccess.Check, true, h, TOKEN_SKIP);
				if (skip) {
					continue;
				}

				switch (type) {
				case TOKEN_TYPE_AGENT:
					getAgent(ca.getId());
					break;
				case TOKEN_TYPE_SERVICE:
					access(DustAccess.Process, null, ca);
					break;
				}

				Dust.log(TOKEN_LEVEL_INFO, "MemInfo after " + ca.getId(), DustDevUtils.memInfo());
			}
		} finally {
			Dust.log(TOKEN_LEVEL_TRACE, "Dust finished", System.currentTimeMillis() - start, "msec.");
		}
	}

	public static DustHandle getHandle(DustHandle unit, DustHandle type, String id, DustOptCreate optCreate) {
		return MIND.getHandle(unit, type, id, optCreate);
	}

	public static DustHandle getUnit(String unitId, boolean createIfMissing) {
		return MIND.getUnit(unitId, createIfMissing);
	}

	public static boolean releaseUnit(DustHandle unit) {
		return MIND.releaseUnit(unit);
	}

	public static <RetType> Class<RetType> getBinary(Object key) {
		String cn = access(DustAccess.Peek, null, appHandle, TOKEN_BINARY_RESOLVER, key, TOKEN_BINARY);
		try {
			return (Class<RetType>) Class.forName(cn);
		} catch (Exception e) {
			return DustException.wrap(e, "accessing class for", key);
		}
	}

	private static <RetType> RetType createInstance(Class cc) {
		Constructor<Object> pc = null;
		try {
			Constructor<Object> bcc = cc.getConstructor();
			if (!bcc.canAccess(null)) {
				bcc.setAccessible(true);
				pc = bcc;
			}
			return (RetType) bcc.newInstance();
		} catch (Throwable e) {
			return DustException.wrap(e, "Creating agent", cc);
		} finally {
			if (null != pc) {
				pc.setAccessible(false);
			}
		}
	}

	private static void initAgent(Object key, DustAgent a) {
		DustHandle hCfg = getAgentHandle(key);

		DustUtilsFactory<DustContext, Object> ctx = CTX;
		try {
			CTX = new DustUtilsFactory(MAP_CREATOR);
			CTX.put(DustContext.Agent, access(DustAccess.Peek, Collections.EMPTY_MAP, hCfg));
			a.init();

			if ((Boolean) access(DustAccess.Peek, false, hCfg, TOKEN_TYPE_RELEASEONSHUTDOWN)) {
				registerToRelease(a);
			}
		} catch (Throwable e) {
			DustException.wrap(e, "Initialising agent", key);
		} finally {
			CTX = ctx;
		}
	};

	public static void log(String eventId, Object... params) {
		StringBuilder sb = DustUtils.sbAppend(null, ", ", false, DustUtils.strTime(), eventId);
		DustUtils.sbAppend(sb, ", ", false, params);
		System.out.println(sb);
	}

	private static <RetType> RetType notifyAgent(DustAccess access, DustHandle listener, DustHandle service, Object params) {
		String agent = listener.getId();
		Dust.log(TOKEN_LEVEL_TRACE, "Message to agent", agent, "service", service, "params", params);

		long start = System.currentTimeMillis();
		Object ret = null;
		DustUtilsFactory<DustContext, Object> ctx = CTX;

		try {
			DustAgent a = Dust.getAgent(agent);

			CTX = new DustUtilsFactory(MAP_CREATOR);
			DustHandle hCfg = getHandle(appUnit, null, agent, DustOptCreate.None);
			CTX.put(DustContext.Agent, access(DustAccess.Peek, null, hCfg));
			CTX.put(DustContext.Service, access(DustAccess.Peek, null, service));
			CTX.put(DustContext.Input, params);

			ret = a.process(access);
		} catch (Throwable e) {
			DustException.wrap(e, "sendMessage failed", agent, "service", service, "params", params);
		} finally {
			Dust.log(TOKEN_LEVEL_TRACE, "Message processed", System.currentTimeMillis() - start, "msec.");
			CTX = ctx;
		}

		return (RetType) ret;
	}

	public static <RetType> RetType access(DustAccess access, Object val, Object root, Object... path) {
		DustHandle agent = (DustHandle) CTX.peek(DustContext.Agent);

		if ((null == root) || (root instanceof DustContext)) {
			return accessCtx(access, agent, val, (DustContext) root, path);
		}

		Object curr = root;

		DustCollType collType = DustUtils.getCollType(root);

		Object ret = null;

		Object prev = null;
		Object lastKey = null;

		Object prevColl = null;
		DustHandle prevHandle = (curr instanceof DustHandle) ? (DustHandle) curr : null;
		DustHandle prevAtt = null;

		if (val instanceof Enum) {
			val = ((Enum) val).name();
		}

		for (Object p : path) {
			if (p instanceof Enum) {
				p = ((Enum) p).name();
//			} else if (p instanceof String) {
//				DustObject a = Dust.getObject(prevUnit, typeAtt, (String) p, DustOptCreate.Meta);
//				p = a.getId();
//			} else if (p instanceof DustObject) {
//				p = ((DustObject) p).getId();
			}

			if (curr instanceof DustHandle) {
				prevHandle = (DustHandle) curr;

				if (p instanceof DustHandle) {
					prevAtt = (DustHandle) p;
					p = ((DustHandle) p).getId();
				} else if (p instanceof String) {
					DustHandle a = Dust.getHandle(prevHandle.getUnit(), typeAtt, (String) p, DustOptCreate.Meta);
					prevAtt = a;
					p = a.getId();
				}

				curr = MIND.getContent(prevHandle);
			} else if (null == curr) {
				if (access.creator) {
					curr = (p instanceof Integer) ? new ArrayList() : new HashMap();

					if (null != prevColl) {
						if ((null != prevAtt) && (null != prevHandle)) {
							MIND.checkAccess(agent, access, prevHandle, prevAtt, curr);
						}

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
				prevHandle = null;
			}

			prev = curr;
			collType = DustUtils.getCollType(prev);
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

			if ((null != prevAtt) && (null != prevHandle)) {
				curr = MIND.checkAccess(agent, access, prevHandle, prevAtt, curr);
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
			if (collType == DustCollType.Set) {
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
				switch (DustUtils.getCollType(curr)) {
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

			Object ll = access(DustAccess.Peek, null, curr, TOKEN_LISTENERS);
			if (ll instanceof Collection) {
				for (Object l : (Collection) ll) {
					ret = Dust.notifyAgent(access, (DustHandle) l, (DustHandle) curr, val);
				}
			}

			break;
		}

		if (curr instanceof DustHandle) {
			curr = MIND.checkAccess(agent, access, (DustHandle) curr, null, curr);
		}

		return (RetType) ret;

	}

	public static <RetType> RetType accessCtx(DustAccess access, Object val, Object root, Object... path) {
		DustHandle agent = peekCtx(DustContext.Agent);
		return accessCtx(access, agent, val, root, path);
	}

	private static <RetType> RetType accessCtx(DustAccess access, DustHandle agent, Object val, Object root, Object... path) {
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
			ret = ((null != main) && (main == root)) ? access(access, def, main, path) : NOT_FOUND;
			for (DustContext dc : DustContext.values()) {
				if (NOT_FOUND != ret) {
					break;
				}
				Object ctx = Dust.peekCtx(dc);
				ret = (null == ctx) ? NOT_FOUND : access(access, def, ctx, path);
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
			// ret = access(access, val, main, path);
//			ret = ((null != main) && (main == root)) ? access(access, def, main, path) : NOT_FOUND;
			ret = (null != main) ? access(access, def, main, path) : NOT_FOUND;
			break;

		}

		if (ret instanceof DustHandle) {
			ret = MIND.checkAccess(agent, access, (DustHandle) ret, null, ret);
		}

		return (RetType) ret;
	}

	public static DustHandle getAgentHandle(Object key) {
		return getHandle(appUnit, null, (String) key, DustOptCreate.None);
	}

	private static <RetType extends DustAgent> RetType getAgent(String agentId) {
		return DustUtils.isEmpty(agentId) ? null : (RetType) AGENTS.get(agentId);
	}

}
