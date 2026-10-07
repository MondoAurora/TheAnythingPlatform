package me.giskard.dust.api;

import java.lang.reflect.Constructor;

import me.giskard.boot.DustGenBootConsts;
import me.giskard.dust.core.machine.DustMachineBoot;
import me.giskard.dust.core.utils.DustUtils;
import me.giskard.dust.test.DustTest01;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Dust implements DustConsts {

	private static DustMachine MACHINE;

	public static void main(String[] args) throws Exception {
		String appId = DustUtils.optGet(args, 0, "");
		String appUnitPath = DustUtils.optGet(args, 1, "");

		start(appId, appUnitPath);
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

	public static void log(DustHandle eventId, Object... params) {
		StringBuilder sb = DustUtils.sbAppend(null, ", ", false, DustUtils.strTime(), eventId);
		DustUtils.sbAppend(sb, ", ", false, params);
		System.out.println(sb);
	}

	public static DustHandle start(String appId, String appUnitPath) throws Exception {
		long start = System.currentTimeMillis();

		try {
			@SuppressWarnings("unused")
			DustMachineBoot dmb = new DustMachineBoot();
			MACHINE = createInstance(Class.forName("me.giskard.dust.core.machine.DustMachineAgent"));

			MACHINE.init();

			DustHandle hApp = Dust.getHandle(appId);

			DustHandle hAppCtx = DustMachineBoot.getTokenHandle(DustGenBootConsts.TOKEN_DUST_ATT_CTX_APP);
			DustHandle hAttNode = DustMachineBoot.getTokenHandle(DustGenBootConsts.TOKEN_DUST_ATT_NODE);

			Dust.access(DustAccess.Set, hApp, null, hAppCtx, hAttNode);

			MACHINE.begin();

			Dust.log(null, "Machine instance created.", hApp);

			DustTest01.testMsg();

		} finally {
			Dust.log(null, "Dust finished", System.currentTimeMillis() - start, "msec.");
		}

		return null;
	}

	public static DustHandle getHandle(String id) {
		return MACHINE.getHandle(id, true);
	}

	public static DustHandle getHandle(String id, boolean createIfMissing) {
		return MACHINE.getHandle(id, createIfMissing);
	}

	public static DustHandle getHandle(DustHandle unit, Object type, String id, boolean createIfMissing) {
		return MACHINE.getHandle(unit, type, id, createIfMissing);
	}

	public static <RetType> RetType access(DustAccess access, Object val, Object root, Object... path) {
		return MACHINE.access(access, val, root, path);
	}
}
