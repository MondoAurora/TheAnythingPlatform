package me.giskard.dust.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import me.giskard.dust.core.dev.DustDevConsts;
import me.giskard.dust.core.dev.DustDevUtils;
import me.giskard.dust.core.machine.DustMachineConsts;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.core.utils.DustUtilsFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Dust implements DustConsts, DustMachineConsts, DustDevConsts {

	private static DustHandle appUnit;
	private static DustHandle appHandle;

	private static DustMachine MACHINE;

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

			String narrativeId = access(DustAccess.Peek, null, aCfg, TOKEN_MIND_ATT_NARRATIVE, TOKEN_MIND_ATT_ID);

			DustAgent a = createInstance(getBinary(narrativeId));

			return a;
		}

		@Override
		public void initNew(DustAgent a, Object key, Object... hints) {
			DustHandle hCfg = getAgentHandle(key);

			try {
				MACHINE.notifyAgent(hCfg, DustAction.Init, null, null, null);

				if ((Boolean) access(DustAccess.Peek, false, hCfg, TOKEN_DUST_ATT_RELEASEONSHUTDOWN)) {
					registerToRelease(a);
				}
			} catch (Throwable e) {
				DustException.wrap(e, "Initialising agent", key);
			}
		}
	}, true);

	public static void main(String[] args) throws Exception {
		String appName = args[0];
		String appUnitPath = args[1];
		DustMachine.Bootloader bootLoader = createInstance(Class.forName("me.giskard.dust.core.stream.DustStreamJsonApiSerializerAgent"));
		DustMachine.StreamSource streamSource = createInstance(Class.forName("me.giskard.dust.core.stream.DustStreamSrcFileAgent"));

		start(DUST_PLATFORM_JAVA, appName, appUnitPath, bootLoader, streamSource);
	}

	public static DustHandle start(String platform, String appName, String appUnitPath, DustMachine.Bootloader bootLoader, DustMachine.StreamSource streamSource) throws Exception {
		long start = System.currentTimeMillis();

		try {
			MACHINE = createInstance(Class.forName("me.giskard.dust.core.machine.DustMachineAgent"));
			AGENTS.put(TOKEN_DUST_AGT_RUNTIME, MACHINE);

			int s = appUnitPath.lastIndexOf(".");
			appUnit = optExtAppUnit(null, appUnitPath, streamSource, bootLoader);

			String userName = System.getProperty("user.name");
			if (!DustUtils.isEmpty(userName)) {
				String userExtPath = new StringBuilder(appUnitPath).insert(s, "." + userName).toString();
				optExtAppUnit(null, userExtPath, streamSource, bootLoader);
			}

			optExtAppUnit(null, DUST_CRED_FILE, streamSource, bootLoader);

			appHandle = getHandle(appUnit, TOKEN_DUST_ASP_APP, appName, DustOptCreate.None);

			String binPath = new StringBuilder(appUnitPath).insert(s, "." + platform).toString();
			optExtAppUnit(null, binPath, streamSource, bootLoader);
			
			Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, "MemInfo before init", DustDevUtils.memInfo());

			MACHINE.init();

			for (DustHandle ca : ((Collection<DustHandle>) access(DustAccess.Peek, Collections.EMPTY_LIST, appHandle, TOKEN_MISC_ATT_INIT))) {

				String type = ca.getType().getId();
				String an = ca.getId();

				DustHandle h = (TOKEN_MIND_ASP_AGENT == type) ? getHandle(appUnit, null, an, DustOptCreate.None) : ca;
				boolean skip = access(DustAccess.Check, true, h, TOKEN_DEV_ATT_SKIP);
				if (skip) {
					continue;
				}

				switch (type) {
				case TOKEN_MIND_ASP_AGENT:
					getAgent(ca.getId());
					break;
				case TOKEN_MIND_ASP_SERVICE:
					access(DustAccess.Process, null, ca);
					break;
				}

				Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, "MemInfo after " + ca.getId(), DustDevUtils.memInfo());
			}
		} finally {
			Dust.log(TOKEN_MISC_TAG_LEVEL_TRACE, "Dust finished", System.currentTimeMillis() - start, "msec.");
		}

		return appHandle;
	}

	public static DustHandle optExtAppUnit(String root, String file, DustMachine.StreamSource streamSource, DustMachine.Bootloader bootLoader)
			throws Exception, IOException {
		try (InputStream is = streamSource.optGetStream(TOKEN_MISC_TAG_CMD_LOAD, root, file)) {
			return (null == is) ? null : MACHINE.bootLoadAppUnit(appUnit, file, is, bootLoader);
		}
	}

	public static DustHandle getHandle(DustHandle unit, Object type, String id, DustOptCreate optCreate) {
		return MACHINE.getHandle(unit, type, id, optCreate);
	}

	public static DustHandle getUnit(String unitId, boolean createIfMissing) {
		return MACHINE.getUnit(unitId, createIfMissing);
	}

	public static boolean releaseUnit(DustHandle unit) {
		return MACHINE.releaseUnit(unit);
	}

	public static <RetType> Class<RetType> getBinary(Object key) {
		String cn = access(DustAccess.Peek, null, appHandle, TOKEN_DUST_ATT_BINARY_RESOLVER, key, TOKEN_DUST_ATT_BINARY);
		try {
			return (Class<RetType>) Class.forName(cn);
		} catch (Exception e) {
			return DustException.wrap(e, "accessing class for", key);
		}
	}

	@SuppressWarnings("deprecation")
	public static <RetType> RetType createInstance(Class cc) {
		Constructor<Object> pc = null;
		try {
			Constructor<Object> bcc = cc.getConstructor();
			if (!bcc.isAccessible()) {
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
		return MACHINE.access(access, val, root, path);
	}

	@Deprecated
	public static <RetType> RetType accessCtx(DustAccess access, Object val, Object root, Object... path) {
		return MACHINE.accessCtx(access, val, root, path);
	}

	@Deprecated
	public static <RetType> RetType optGetCtx(Object in) {
		return MACHINE.optGetCtx(in);
	}

	public static DustHandle getAgentHandle(Object key) {
		return getHandle(appUnit, null, (String) key, DustOptCreate.None);
	}

	private static <RetType extends DustAgent> RetType getAgent(String agentId) {
		return DustUtils.isEmpty(agentId) ? null : (RetType) AGENTS.get(agentId);
	}

}
