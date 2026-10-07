package me.giskard.dust.test;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustHandle;

public class DustTest01 implements DustTestConsts {
	public static void testMsg() throws Exception {
		DustHandle hTest;

//		hTest = Dust.getHandle("Lorand/test01$LogTest");
//		Dust.access(DustAccess.Commit, null, hTest);
//		Dust.access(DustAccess.Commit, null, hTest);

//		hTest = Dust.getHandle("Lorand/test01$UnitHandler");

		hTest = Dust.access(DustAccess.Peek, null, null, HANDLE_DUST_ATT_NODE, HANDLE_STREAM_ATT_UNIT_HANDLER);
		
		DustHandle handle = Dust.getHandle("Lorand/Tanulas.1");
		
		try {
			Dust.access(DustAccess.Set, handle, hTest, HANDLE_MISC_ATT_TARGET);
			
			Dust.access(DustAccess.Set, HANDLE_MISC_TAG_CMD_LOAD, hTest, HANDLE_MIND_ATT_CMD);
			Dust.access(DustAccess.Commit, null, hTest);
			
			Dust.access(DustAccess.Set, HANDLE_MISC_TAG_CMD_SAVE, hTest, HANDLE_MIND_ATT_CMD);
			Dust.access(DustAccess.Commit, null, hTest);
		} finally {
			Dust.access(DustAccess.Delete, null, hTest, HANDLE_MIND_ATT_CMD);
			Dust.access(DustAccess.Delete, null, hTest, HANDLE_MISC_ATT_TARGET);
		}

//		try (FileInputStream fis = new FileInputStream("localStore/" + handle.getId() + DUST_EXT_JSON)) {
//			Dust.access(DustAccess.Set, fis, hTest, HANDLE_STREAM_ATT_INPUT);
//			Dust.access(DustAccess.Set, handle, hTest, HANDLE_MISC_ATT_TARGET);
//			Dust.access(DustAccess.Set, HANDLE_MISC_TAG_CMD_LOAD, hTest, HANDLE_MIND_ATT_CMD);
//
//			Dust.access(DustAccess.Commit, null, hTest);
//		} catch (Throwable e) {
//			DustException.wrap(e, "loading unit", handle.getId());
//		} finally {
//			Dust.access(DustAccess.Delete, null, hTest, HANDLE_STREAM_ATT_INPUT);
//			Dust.access(DustAccess.Delete, null, hTest, HANDLE_MISC_ATT_TARGET);
//		}
//
//		File f = new File("tmp/ls1/" + handle.getId() + DUST_EXT_JSON);
//		DustUtilsFile.ensureDir(f.getParent());
//		try (FileOutputStream fos = new FileOutputStream(f)) {
//
////		try (FileInputStream fis = new FileInputStream("localStore/" + handle.getId() + DUST_EXT_JSON)) {
//			Dust.access(DustAccess.Set, fos, hTest, HANDLE_STREAM_ATT_OUTPUT);
//			Dust.access(DustAccess.Set, handle, hTest, HANDLE_MISC_ATT_TARGET);
//			Dust.access(DustAccess.Set, HANDLE_MISC_TAG_CMD_SAVE, hTest, HANDLE_MIND_ATT_CMD);
//
//			Dust.access(DustAccess.Commit, null, hTest);
//		} catch (Throwable e) {
//			DustException.wrap(e, "loading unit", handle.getId());
//		} finally {
//			Dust.access(DustAccess.Delete, null, hTest, HANDLE_STREAM_ATT_OUTPUT);
//			Dust.access(DustAccess.Delete, null, hTest, HANDLE_MISC_ATT_TARGET);
//		}

	}

}
