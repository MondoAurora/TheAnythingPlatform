package me.giskard.dust;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import me.giskard.dust.dev.DustDevUtils;
import me.giskard.dust.mind.DustMindConsts;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Dust implements DustConsts, DustMindConsts {

	private static DustHandle appUnit;
	private static DustHandle appHandle;

	private static DustMind MIND;

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

			DustAgent a = createInstance(getBinary(key));

			return a;
		}

		@Override
		public void initNew(DustAgent a, Object key, Object... hints) {
			DustHandle hCfg = getAgentHandle(key);

			try {
				MIND.notifyAgent(hCfg, DustAction.Init, null, null, null);

				if ((Boolean) access(DustAccess.Peek, false, hCfg, TOKEN_TYPE_RELEASEONSHUTDOWN)) {
					registerToRelease(a);
				}
			} catch (Throwable e) {
				DustException.wrap(e, "Initialising agent", key);
			}
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

	public static void log(String eventId, Object... params) {
		StringBuilder sb = DustUtils.sbAppend(null, ", ", false, DustUtils.strTime(), eventId);
		DustUtils.sbAppend(sb, ", ", false, params);
		System.out.println(sb);
	}

	static <RetType> RetType callAgent(DustHandle hAgent, DustAction action, DustAccess access) throws Exception {
		String id = hAgent.getId();
		DustAgent a = getAgent(id);
		return (RetType) a.process(action, access);
	}

	public static <RetType> RetType access(DustAccess access, Object val, Object root, Object... path) {
		return MIND.access(access, val, root, path);
	}

	@Deprecated
	public static <RetType> RetType accessCtx(DustAccess access, Object val, Object root, Object... path) {
		return MIND.accessCtx(access, val, root, path);
	}

	@Deprecated
	public static <RetType> RetType optGetCtx(Object in) {
		return MIND.optGetCtx(in);
	}

	public static DustHandle getAgentHandle(Object key) {
		return getHandle(appUnit, null, (String) key, DustOptCreate.None);
	}

	private static <RetType extends DustAgent> RetType getAgent(String agentId) {
		return DustUtils.isEmpty(agentId) ? null : (RetType) AGENTS.get(agentId);
	}

}
