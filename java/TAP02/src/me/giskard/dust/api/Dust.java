package me.giskard.dust.api;

import java.lang.reflect.Constructor;

import me.giskard.dust.core.DustConstsBoot;
import me.giskard.dust.core.machine.DustMachineBoot;
import me.giskard.dust.core.utils.DustUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Dust implements DustConstsBoot {
	
	private static DustMachine MACHINE;

	public static void main(String[] args) throws Exception {
		String appName = DustUtils.optGet(args, 0, "");
		String appUnitPath = DustUtils.optGet(args, 1, "");

		start(DUST_PLATFORM_JAVA, appName, appUnitPath);
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


	public static DustHandle start(String platform, String appName, String appUnitPath)
			throws Exception {
		long start = System.currentTimeMillis();

		try {
			@SuppressWarnings("unused")
			DustMachineBoot dmb = new DustMachineBoot();
			MACHINE = createInstance(Class.forName("me.giskard.dust.core.machine.DustMachineNewAgent"));

			MACHINE.init();

//			DustHandle hh = Dust.getHandle(null, null, TOKEN_DUST_ATT_CTX_AGT, DustOptCreate.None);
//
//			Dust.log(TOKEN_MISC_TAG_LEVEL_INFO, "Test handle", hh);

			Dust.log(null, "Machine instance created.");

		} finally {
			Dust.log(null, "Dust finished", System.currentTimeMillis() - start, "msec.");
		}

		return null;
	}
	
	public static DustHandle getHandle(String id){
		return MACHINE.getHandle(id, true);
	}

	public static DustHandle getHandle(String id, boolean createIfMissing){
		return MACHINE.getHandle(id, createIfMissing);
	}

	public static DustHandle getHandle(DustHandle unit, Object type, String id, boolean createIfMissing) {
		return MACHINE.getHandle(unit, type, id, createIfMissing);
	}


	public static <RetType> RetType access(DustAccess access, Object val, Object root, Object... path) {
		return MACHINE.access(access, val, root, path);
	}


}
