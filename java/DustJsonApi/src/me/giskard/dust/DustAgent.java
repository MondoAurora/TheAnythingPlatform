package me.giskard.dust;

import java.util.Map;

import me.giskard.dust.kb.DustKBUtils;

@SuppressWarnings("unchecked")
public abstract class DustAgent implements DustConsts {
	private Map<String, Object> cfg;
	private ThreadLocal<Map<String, Object>> msg = new ThreadLocal<Map<String,Object>>();

	void init(Map<String, Object> cfg) throws Exception {
		this.cfg = cfg;
		init();
	}

	 <RetType> RetType agentProcess(DustAction action, Object params) throws Exception {
		msg.set((Map<String, Object>) params);
		
		return (RetType) process(action);
	}

	protected void init() throws Exception {
	}

	protected abstract Object process(DustAction action) throws Exception;

	protected void release() throws Exception {
	}

	public <RetType> RetType access(DustAccess access, Object val, Object root, Object... path) {
		Object ret = null;

		Object main = (null == root) ? msg.get() : root;
		Object def = val;
		boolean pg = false;

		switch (access) {
		case Peek:
		case Get:
			pg = true;
			def = NOT_FOUND;
		case Check:
		case Visit:
			ret = (null == root) ? NOT_FOUND : DustKBUtils.access(access, def, root, path);
			if (NOT_FOUND == ret) {
				ret = DustKBUtils.access(access, def, msg.get(), path);
			}
			if (NOT_FOUND == ret) {
				ret = DustKBUtils.access(access, def, cfg, path);
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
			DustKBUtils.access(access, val, main, path);
			break;

		}

		return (RetType) ret;
	}
}