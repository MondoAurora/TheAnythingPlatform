package me.giskard.dust;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import me.giskard.dust.dev.DustDevUtils;
import me.giskard.dust.kb.DustKBConsts;
import me.giskard.dust.kb.DustKBUtils;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsFactory;

@SuppressWarnings("unchecked")
public class Dust implements DustConsts, DustKBConsts {

	private static KBUnit appUnit;
	private static KBObject appObj;

	private static ArrayList<DustAgent> TORELEASE;

	private static synchronized void registerToRelease(DustAgent agent) {
		if (null == TORELEASE) {
			TORELEASE = new ArrayList<DustConsts.DustAgent>();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					for (DustAgent a : TORELEASE) {
						try {
							a.agentProcess(DustAction.Release, null);
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
			KBObject aCfg = appObj.access(KBAccess.Peek, null, TOKEN_AGENTS, key);

			if (null == aCfg) {
				return DustException.wrap(null, "Missing config for agent", key);
			}
			String cn = aCfg.access(KBAccess.Peek, null, TOKEN_CLASS_NAME);
			DustAgent a = null;
			try {
				a = (DustAgent) Class.forName(cn).getConstructor().newInstance();
				a.agentProcess(DustAction.Init, aCfg.access(KBAccess.Peek, Collections.EMPTY_MAP, TOKEN_PARAMS));

				if ((Boolean) aCfg.access(KBAccess.Peek, false, TOKEN_TYPE_RELEASEONSHUTDOWN)) {
					registerToRelease(a);
				}
			} catch (Throwable e) {
				DustException.wrap(e, "Creating agent", key);
			}

			return a;
		}
	}, true);

	public static void main(String[] args) throws Exception {

		long start = System.currentTimeMillis();

		try {
			String appName = args[0];
			String appUnitPath = args[1];

			File f = new File(appUnitPath);
			appUnit = DustKBUtils.bootLoadAppUnitJsonApi(f);

			String appType = DUST_UNIT_ID + DUST_SEP_TOKEN + TOKEN_TYPE_APP;

			appObj = appUnit.getObject(appType, appName, KBOptCreate.None);

			File cf = new File(DUST_CRED_FILE);

			if (cf.isFile()) {
				try (FileInputStream fis = new FileInputStream(cf); BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
					String line;
					while ((line = br.readLine()) != null) {
						line = line.trim();

						if (!DustUtils.isEmpty(line)) {
							String[] cred = line.split(":");

							KBObject aCfg = appObj.access(KBAccess.Peek, null, TOKEN_AGENTS, cred[0].trim());
							aCfg.access(KBAccess.Set, cred[2].trim(), (Object[]) cred[1].trim().split("\\."));
						}
					}
				}
			} else {
				Dust.log(TOKEN_LEVEL_WARNING, "No credentials file given", cf.getName());
			}

			for (KBObject ca : ((Collection<KBObject>) appObj.access(KBAccess.Peek, Collections.EMPTY_LIST, TOKEN_INIT))) {
				Dust.log(TOKEN_LEVEL_INFO, "MemInfo", DustDevUtils.memInfo());

				String type = DustUtils.getPostfix(ca.getType(), DUST_SEP_TOKEN);
				String an;
				boolean skip;

				switch (type) {
				case TOKEN_TYPE_AGENT:
					an = ca.getId();
					skip = appObj.access(KBAccess.Check, true, TOKEN_AGENTS, an, TOKEN_SKIP);
					if (skip) {
						continue;
					} else {
						getAgent(an);
					}
					break;
				case TOKEN_TYPE_MESSAGE:
					skip = ca.access(KBAccess.Check, true, TOKEN_SKIP);
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
		String agent = DustKBUtils.access(KBAccess.Get, null, msg, TOKEN_AGENT);
		Dust.log(TOKEN_LEVEL_TRACE, "Message to agent", agent, "params", msg);
		long start = System.currentTimeMillis();
		Object ret = null;

		try {
			DustAgent a = Dust.getAgent(agent);
			Map<String, Object> params = DustKBUtils.access(KBAccess.Get, null, msg, TOKEN_PARAMS);
			ret = a.agentProcess(DustAction.Process, params);
		} catch (Throwable e) {
			DustException.wrap(e, "sendMessage failed", msg);
		} finally {
			Dust.log(TOKEN_LEVEL_TRACE, "Message processed", System.currentTimeMillis() - start, "msec.");
		}

		return (RetType) ret;
	}

}
