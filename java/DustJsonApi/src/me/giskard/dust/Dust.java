package me.giskard.dust;

import java.io.File;
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

	private static KBUnit appUnit;
	private static KBObject appObj;

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
			KBObject aCfg = DustKBUtils.access(DustAccess.Peek, null, appObj, TOKEN_AGENTS, key);

			if (null == aCfg) {
				return DustException.wrap(null, "Missing config for agent", key);
			}
			String cn = DustKBUtils.access(DustAccess.Peek, null, aCfg, TOKEN_CLASS_NAME);
			DustAgent a = null;
			try {
				a = (DustAgent) Class.forName(cn).getConstructor().newInstance();
			} catch (Throwable e) {
				DustException.wrap(e, "Creating agent", key);
			}
			
			return a;
		}
		
		@Override
		public void initNew(DustAgent a, Object key, Object... hints) {
			KBObject aCfg = DustKBUtils.access(DustAccess.Peek, null, appObj, TOKEN_AGENTS, key);

			DustUtilsFactory<DustContext, Object> ctx = CTX;
			try {
				CTX = new DustUtilsFactory(MAP_CREATOR);
				CTX.put(DustContext.Agent, DustKBUtils.access(DustAccess.Peek, Collections.EMPTY_MAP, aCfg, TOKEN_PARAMS));
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

			for (KBObject ca : ((Collection<KBObject>) DustKBUtils.access(DustAccess.Peek, Collections.EMPTY_LIST, appObj, TOKEN_INIT))) {
				Dust.log(TOKEN_LEVEL_INFO, "MemInfo", DustDevUtils.memInfo());

				String type = DustUtils.getPostfix(ca.getType(), DUST_SEP_TOKEN);
				String an;
				boolean skip;

				switch (type) {
				case TOKEN_TYPE_AGENT:
					an = ca.getId();
					skip = DustKBUtils.access(DustAccess.Check, true, appObj, TOKEN_AGENTS, an, TOKEN_SKIP);
					if (skip) {
						continue;
					} else {
						getAgent(an);
					}
					break;
				case TOKEN_TYPE_MESSAGE:
					skip = DustKBUtils.access(DustAccess.Check, true, ca, TOKEN_SKIP);
					if (skip) {
						continue;
					} else {
						sendMessage(ca);
					}
					break;
				}
			}
		} finally {
			Dust.log(TOKEN_LEVEL_TRACE, "Dust finished", System.currentTimeMillis() - start, "msec.");
		}
	}

	public static <RetType extends DustAgent> RetType getAgent(String agentId) {
		return DustUtils.isEmpty(agentId) ? null : (RetType) AGENTS.get(agentId);
	}

	public static void log(String eventId, Object... params) {
		StringBuilder sb = DustUtils.sbAppend(null, ", ", false, DustUtils.strTime(), eventId);
		DustUtils.sbAppend(sb, ", ", false, params);
		System.out.println(sb);
	}

	public static <RetType> RetType sendMessage(Object msg) {
		String agent = DustKBUtils.access(DustAccess.Get, null, msg, TOKEN_AGENT);
		Dust.log(TOKEN_LEVEL_TRACE, "Message to agent", agent, "params", msg);
		long start = System.currentTimeMillis();
		Object ret = null;
		DustUtilsFactory<DustContext, Object> ctx = CTX;

		try {
			DustAgent a = Dust.getAgent(agent);

			CTX = new DustUtilsFactory(MAP_CREATOR);
			CTX.put(DustContext.Agent, DustKBUtils.access(DustAccess.Peek, null, appObj, TOKEN_AGENTS, agent, TOKEN_PARAMS));
			CTX.put(DustContext.Service, DustKBUtils.access(DustAccess.Peek, null, msg, TOKEN_PARAMS));

			ret = a.process(DustAction.Process);
		} catch (Throwable e) {
			DustException.wrap(e, "sendMessage failed", msg);
		} finally {
			Dust.log(TOKEN_LEVEL_TRACE, "Message processed", System.currentTimeMillis() - start, "msec.");
			CTX = ctx;
		}

		return (RetType) ret;
	}

}
