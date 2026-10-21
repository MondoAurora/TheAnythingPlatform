package me.giskard.dust.api;

import java.lang.reflect.Constructor;

//import me.giskard.dust.core.machine.DustMachineBoot;
import me.giskard.dust.core.utils.DustUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Dust implements DustConsts {

	private static DustMachine MACHINE;

	public static DustHandle getHandle(String id) {
		return MACHINE.getHandle(id, null);
	}

	public static DustHandle getHandle(String id, DustHandle type) {
		return MACHINE.getHandle(id, type);
	}

	public static <RetType> RetType access(DustAccess access, Object val, Object root, Object... path) {
		return MACHINE.access(access, val, root, path);
	}

	public static void log(DustHandle eventId, Object... params) {
		StringBuilder sb = DustUtils.sbAppend(null, ", ", false, DustUtils.strTime(), eventId);
		DustUtils.sbAppend(sb, ", ", false, params);
		System.out.println(sb);
	}

	public static void main(String[] args) throws Exception {
		String appId = DustUtils.optGet(args, 0, "");
		String appUnitPath = DustUtils.optGet(args, 1, "");

		start(appId, appUnitPath);
	}

	public static DustHandle start(String appId, String appUnitPath) throws Exception {
		long start = System.currentTimeMillis();

		try {
			DustMachine.Provider mp = createInstance(Class.forName(DUST_BOOT_SYS_CLASS));
			MACHINE = mp.getMachine();
			MACHINE.init();

			createInstance(Class.forName(DUST_BOOT_APP_CLASS), appId);

//			DustTest01.testMsg();

			MACHINE.begin();

			Dust.log(null, "Machine instance created.", appId);

//			DustTest01.testMsg();
			
			Dust.access(DustAccess.Commit, null, null);

		} finally {
			Dust.log(null, "Dust finished", System.currentTimeMillis() - start, "msec.");
		}

		return null;
	}

	@SuppressWarnings("deprecation")
	public static <RetType> RetType createInstance(Class cc, Object... args) {
		Constructor<Object> protectedConstructor = null;
		Constructor<Object> validConstructor = null;

		try {
			int al = args.length;

			if (0 == al) {
				validConstructor = cc.getConstructor();
			} else {
				for (Constructor<Object> c : cc.getConstructors()) {
					validConstructor = c;
					Class[] pt = c.getParameterTypes();
					if (al == pt.length) {
						int pi = 0;
						for (Class pcc : pt) {
							if (!pcc.isInstance(args[pi])) {
								validConstructor = null;
								break;
							}
						}
					}

					if (null != validConstructor) {
						break;
					}
				}
			}

			if (!validConstructor.isAccessible()) {
				validConstructor.setAccessible(true);
				protectedConstructor = validConstructor;
			}
			return (RetType) validConstructor.newInstance(args);

		} catch (Throwable e) {
			return DustException.wrap(e, "Creating agent", cc);
		} finally {
			if (null != protectedConstructor) {
				protectedConstructor.setAccessible(false);
			}
		}
	}

	public static void manageThread(DustThreadAction action, Object anchor) {
		MACHINE.manageThread(action, anchor);
	}
}
