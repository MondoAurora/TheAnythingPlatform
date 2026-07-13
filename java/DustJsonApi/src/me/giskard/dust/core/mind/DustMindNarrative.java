package me.giskard.dust.core.mind;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.utils.DustUtils;

@SuppressWarnings("rawtypes")
public interface DustMindNarrative extends DustMindConsts {

	abstract class ChainAgent extends DustAgent {
		DustHandle hNext;

		@Override
		protected void init() throws Exception {
			hNext = Dust.access(DustAccess.Peek, null, null, TOKEN_NEXT);
		};

	}

	public class ForAll extends ChainAgent {

		@Override
		protected void init() throws Exception {
			super.init();
		}

		@Override
		protected Object process(DustAccess access) throws Exception {
			String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);
			Map params = new HashMap();

			Collection path = Dust.access(DustAccess.Peek, null, null, TOKEN_PATH);

			if (null != path) {
				Dust.access(DustAccess.Set, cmd, params, TOKEN_CMD);

				Object[] p = path.toArray();
				Collection<DustHandle> target = Dust.access(DustAccess.Peek, Collections.EMPTY_LIST, null, TOKEN_MEMBERS);

				for (DustHandle ht : target) {
					Dust.access(DustAccess.Set, ht, params, TOKEN_TARGET);

					Object data = Dust.access(DustAccess.Peek, null, ht, p);

					DustUtils.visit(data, new DustProcessor<Object, Object>() {
						@Override
						public Object process(Object handle, Object... hints) {
							Dust.access(DustAccess.Set, handle, params, TOKEN_DATA);
							Dust.access(DustAccess.Process, params, hNext);
							return null;
						}
					});

				}
			}
			return null;
		}

	}

	public class Filter extends ChainAgent {
		
		Map<String, Object> conditions;
		
		@Override
		protected void init() throws Exception {
			super.init();
			conditions = Dust.access(DustAccess.Peek, Collections.EMPTY_MAP, null, TOKEN_FILTER);
		}

		@Override
		protected Object process(DustAccess access) throws Exception {
			String cmd = Dust.access(DustAccess.Peek, null, null, TOKEN_CMD);
			
			Object data = Dust.access(DustAccess.Peek, null, null, TOKEN_DATA);
			
			if ( null != data ) {
				Map params = new HashMap();
				Dust.access(DustAccess.Set, cmd, params, TOKEN_CMD);

				boolean pass = true;

				for ( Map.Entry<String, Object> ce : conditions.entrySet()) {
					Object val = Dust.access(DustAccess.Peek, null, data, ce.getKey());
					
					Object cond = ce.getValue();
					
					if ( cond instanceof Boolean ) {
						pass = ((Boolean) cond) == (null != val);
					} else if (cond instanceof Collection ) {
						pass = ((Collection)cond).contains( DustUtils.toString(val) );
					}
					
					if ( !pass ) {
						break;
					}
				}
				
				if ( pass ) {
					Dust.access(DustAccess.Set, data, params, TOKEN_DATA);
					Dust.access(DustAccess.Process, params, hNext);					
				}
			}
			
			return null;
		}

	}
}
