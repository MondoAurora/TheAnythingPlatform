package me.giskard.dust.core.machine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.core.utils.DustUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public interface DustMachineNarrative extends DustMachineConsts {

	abstract class ChainAgent extends DustAgent {
		DustHandle hNext;

		@Override
		protected void init() throws Exception {
			hNext = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_NEXT);
		};

	}

	public class Compare extends ChainAgent {

		@Override
		protected void process() throws Exception {
			// TODO Auto-generated method stub
		}

		public static boolean isDifferent(DustHandle hDiff) {
			boolean ret = false;

			DustHandle hA = Dust.access(DustAccess.Peek, null, hDiff, HANDLE_MIND_ATT_DIFF_A);
			DustHandle hB = Dust.access(DustAccess.Peek, null, hDiff, HANDLE_MIND_ATT_DIFF_B);

			Set<DustHandle> atts = new HashSet<>();

			for (String s : DustMachineUtils.getAttNames(hA)) {
				atts.add(Dust.getHandle(s));
			}
			for (DustHandle h : DustMachineUtils.getAttHandles(hB)) {
				atts.add(h);
			}

			for (DustHandle h : atts) {
				Dust.access(DustAccess.Delete, null, hDiff, HANDLE_MISC_ATT_REFPATH);
				Dust.access(DustAccess.Insert, h, hDiff, HANDLE_MISC_ATT_REFPATH, KEY_ADD);
				ret |= isDiffValue(hDiff, hA, hB);
			}

			return ret;
		}

		private static boolean isDiffValue(DustHandle hDiff, DustHandle hA, DustHandle hB) {
			DustHandle h = Dust.access(DustAccess.Peek, null, hDiff, HANDLE_MISC_ATT_REFPATH, 0);

			Object vA = Dust.access(DustAccess.Peek, null, hA, h);
			Object vB = Dust.access(DustAccess.Peek, null, hB, h);

			if (DustUtils.isEqual(vA, vB)) {
				return false;
			}

			boolean ret = true;

			if (vA instanceof DustHandle) {
				ret = (vB instanceof DustHandle) ? DustUtils.isEqual(((DustHandle) vA).getId(), ((DustHandle) vB).getId()) : true;
			} else if (vA instanceof Set) {
				if (vB instanceof Set) {
					Set sA = new HashSet();
					Set sB = new HashSet();

					for (Object v : (Set) vA) {
						sA.add((v instanceof DustHandle) ? ((DustHandle) v).getId() : v);
					}
					for (Object v : (Set) vB) {
						sB.add((v instanceof DustHandle) ? ((DustHandle) v).getId() : v);
					}

					for (Iterator<Object> i = sA.iterator(); i.hasNext();) {
						if (sB.remove(i.next())) {
							i.remove();
						}
					}

					if (sA.isEmpty() && sB.isEmpty()) {
						ret = false;
					}
				}
			}

			if (ret) {
				Dust.log(HANDLE_MISC_TAG_LEVEL_INFO, "compare diff", vA, vB);
			}

			return ret;
		}
	}

	public class ForAll extends ChainAgent {

		@Override
		protected void init() throws Exception {
			super.init();
		}

		@Override
		protected void process() throws Exception {
			String cmd = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_CMD);
//			Map params = new HashMap();

			Collection path = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_PATH);

			if (null != path) {
//				Dust.access(DustAccess.Set, cmd, params, HANDLE_MIND_ATT_CMD);
				Dust.access(DustAccess.Set, cmd, hNext, HANDLE_MIND_ATT_CMD);

				Object[] p = path.toArray();
				Collection<DustHandle> target = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, null, HANDLE_MISC_ATT_MEMBERS);

				for (DustHandle ht : target) {
					Dust.access(DustAccess.Set, ht, hNext, HANDLE_MISC_ATT_TARGET);
//					Dust.access(DustAccess.Set, ht, params, HANDLE_MISC_ATT_TARGET);

					Object data = Dust.access(DustAccess.Peek, null, ht, p);

					DustUtils.visit(data, new DustProcessor<Object, Object>() {
						@Override
						public Object process(Object handle, Object... hints) {
							Dust.access(DustAccess.Set, handle, hNext, HANDLE_MISC_ATT_DATA);
							Dust.access(DustAccess.Commit, null, hNext);
							return null;
						}
					});

				}
			}
		}

	}

	public class Filter extends ChainAgent {

		@Override
		protected void init() throws Exception {
			super.init();
//			conditions = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, null, HANDLE_MISC_ATT_FILTER);
		}

		@Override
		protected void process() throws Exception {
			String cmd = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_CMD);

			Object data = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_DATA);

			if (null != data) {
//				Map params = new HashMap();
//				Dust.access(DustAccess.Set, cmd, params, HANDLE_MIND_ATT_CMD);
				Map<String, Object> conditions = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, null, HANDLE_MISC_ATT_FILTER);

				boolean pass = true;

				for (Map.Entry<String, Object> ce : conditions.entrySet()) {
					Object val = Dust.access(DustAccess.Peek, null, data, ce.getKey());

					Object cond = ce.getValue();

					if (cond instanceof Boolean) {
						pass = ((Boolean) cond) == (null != val);
					} else if (cond instanceof Collection) {
						pass = ((Collection) cond).contains(DustUtils.toString(val));
					}

					if (!pass) {
						break;
					}
				}

				if (pass) {
					Dust.access(DustAccess.Set, cmd, hNext, HANDLE_MIND_ATT_CMD);
					Dust.access(DustAccess.Set, data, hNext, HANDLE_MISC_ATT_DATA);
					Dust.access(DustAccess.Commit, null, hNext);
				}
			}
		}
	}
}
