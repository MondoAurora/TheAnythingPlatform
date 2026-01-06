package me.giskard.dust;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import me.giskard.dust.dev.DustDevUtils;
import me.giskard.dust.kb.DustKBConsts;
import me.giskard.dust.kb.DustKBUtils;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Dust implements DustConsts, DustKBConsts {

	private static KBStore appStore;
	private static KBUnit appUnit;
	private static KBObject appObj;

	private static final String TYPE_AGENT = DUST_UNIT_ID + DUST_SEP_TOKEN + TOKEN_TYPE_AGENT;

//	static ThreadLocal<DustUtilsFactory<DustContext, Object>> CTX = new ThreadLocal<DustUtilsFactory<DustContext, Object>>() {
//		@Override
//		protected DustUtilsFactory<DustContext, Object> initialValue() {
//			return new DustUtilsFactory(MAP_CREATOR);
//		}
//	};

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
			KBObject aCfg = getAgentObject(key);

			if (null == aCfg) {
				return DustException.wrap(null, "Missing config for agent ", key);
			}

			Constructor<Object> pc = null;
			try {
				Constructor<Object> bcc = getBinary(key).getConstructor();
				if (!bcc.canAccess(null)) {
					bcc.setAccessible(true);
					pc = bcc;
				}
				return (DustAgent) bcc.newInstance();
			} catch (Throwable e) {
				return DustException.wrap(e, "Creating agent", key);
			} finally {
				if (null != pc) {
					pc.setAccessible(false);
				}
			}
		}

		@Override
		public void initNew(DustAgent a, Object key, Object... hints) {
			KBObject aCfg = getAgentObject(key);

			DustUtilsFactory<DustContext, Object> ctx = CTX;
			try {
				CTX = new DustUtilsFactory(MAP_CREATOR);
				CTX.put(DustContext.Agent, DustKBUtils.access(DustAccess.Peek, Collections.EMPTY_MAP, aCfg));
				a.init();

				if ((Boolean) DustKBUtils.access(DustAccess.Peek, false, aCfg, TOKEN_TYPE_RELEASEONSHUTDOWN)) {
					registerToRelease(a);
				}
			} catch (Throwable e) {
				DustException.wrap(e, "Initialising agent", key);
			} finally {
				CTX = ctx;
			}
		};

	}, true);

	public static void main(String[] args) throws Exception {

		long start = System.currentTimeMillis();

		try {
			String appName = args[0];
			String appUnitPath = args[1];

			File f = new File(appUnitPath);
			appUnit = DustKBUtils.bootLoadAppUnitJsonApi(f);

			DustKBUtils.loadExtFile(appUnit, new File(DUST_CRED_FILE));

			String userName = System.getProperty("user.name");
			if (!DustUtils.isEmpty(userName)) {
				File d = f.getAbsoluteFile().getParentFile();
				String fn = f.getName();
				int s = fn.lastIndexOf(".");
				File f2 = new File(d, fn.substring(0, s) + "." + userName + DUST_EXT_TXT);
				DustKBUtils.loadExtFile(appUnit, f2);
			}

			String appType = DUST_UNIT_ID + DUST_SEP_TOKEN + TOKEN_TYPE_APP;
			appObj = appUnit.getObject(appType, appName, KBOptCreate.None);

			int s = appUnitPath.lastIndexOf(".");
			File fBin = new File(appUnitPath.substring(0, s) + "." + DUST_PLATFORM_JAVA + appUnitPath.substring(s));
			DustKBUtils.bootLoadAppUnitJsonApi(fBin);

			appStore = Dust.getAgent(DustKBUtils.access(DustAccess.Peek, null, appObj, TOKEN_DATA));

			Dust.log(TOKEN_LEVEL_INFO, "MemInfo before init", DustDevUtils.memInfo());

			for (KBObject ca : ((Collection<KBObject>) DustKBUtils.access(DustAccess.Peek, Collections.EMPTY_LIST, appObj, TOKEN_INIT))) {

				String type = DustUtils.getPostfix(ca.getType(), DUST_SEP_TOKEN);
				String an = ca.getId();

				KBObject o = (TOKEN_TYPE_AGENT == type) ? appUnit.getObject(TYPE_AGENT, an, KBOptCreate.None) : ca;
				boolean skip = DustKBUtils.access(DustAccess.Check, true, o, TOKEN_SKIP);
				if (skip) {
					continue;
				}

				switch (type) {
				case TOKEN_TYPE_AGENT:
					getAgent(an);
					break;
				case TOKEN_TYPE_SERVICE:
					DustKBUtils.access(DustAccess.Process, null, ca);
					break;
				}

				Dust.log(TOKEN_LEVEL_INFO, "MemInfo after " + ca.getId(), DustDevUtils.memInfo());
			}
		} finally {
			Dust.log(TOKEN_LEVEL_TRACE, "Dust finished", System.currentTimeMillis() - start, "msec.");
		}
	}
	
	public static <RetType> Class<RetType> getBinary(Object key) throws Exception {
		String cn = DustKBUtils.access(DustAccess.Peek, null, appObj, TOKEN_BINARY_RESOLVER, key, TOKEN_BINARY);
		return (Class<RetType>) Class.forName(cn);
	}

	public static KBStore getStore() {
		return appStore;
	}

	public static KBObject getAgentObject(Object key) {
		return appUnit.getObject(TYPE_AGENT, (String) key, KBOptCreate.None);
	}

	public static <RetType extends DustAgent> RetType getAgent(String agentId) {
		return DustUtils.isEmpty(agentId) ? null : (RetType) AGENTS.get(agentId);
	}

	public static void log(String eventId, Object... params) {
		StringBuilder sb = DustUtils.sbAppend(null, ", ", false, DustUtils.strTime(), eventId);
		DustUtils.sbAppend(sb, ", ", false, params);
		System.out.println(sb);
	}

	public static <RetType> RetType notifyAgent(DustAccess access, KBObject listener, KBObject service, Object params) {
		String agent = listener.getId();
		Dust.log(TOKEN_LEVEL_TRACE, "Message to agent", agent, "service", service, "params", params);

		long start = System.currentTimeMillis();
		Object ret = null;
		DustUtilsFactory<DustContext, Object> ctx = CTX;

		try {
			DustAgent a = Dust.getAgent(agent);

			CTX = new DustUtilsFactory(MAP_CREATOR);
			KBObject aCfg = appUnit.getObject(TYPE_AGENT, agent, KBOptCreate.None);
			CTX.put(DustContext.Agent, DustKBUtils.access(DustAccess.Peek, null, aCfg));
			CTX.put(DustContext.Service, DustKBUtils.access(DustAccess.Peek, null, service));
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

}
