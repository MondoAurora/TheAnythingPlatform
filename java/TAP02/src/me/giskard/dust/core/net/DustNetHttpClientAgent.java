package me.giskard.dust.core.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import me.giskard.dust.api.Dust;
import me.giskard.dust.api.DustAgent;
import me.giskard.dust.api.DustException;
import me.giskard.dust.api.DustHandle;
import me.giskard.dust.core.dev.DustDevCounter;
import me.giskard.dust.core.utils.DustUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustNetHttpClientAgent extends DustAgent implements DustNetConsts {

	DustHandle[] PARAMETERS = new DustHandle[] { HANDLE_STREAM_ATT_URL, HANDLE_MISC_ATT_KEY, HANDLE_MISC_ATT_TIMEOUT,
			HANDLE_NET_ATT_SRVCALL_HEADERS, HANDLE_MISC_ATT_PATH };

	DustHandle next;
	int retryCount = 5;

	class BackgroundDownloadQueue implements Runnable {

		ArrayList<Map> queue = new ArrayList<Map>();
		Set<String> seen = new TreeSet<String>();

		DustDevCounter<String> retry = new DustDevCounter<String>(true);

		boolean stop = false;

		@Override
		public void run() {
			Dust.manageThread(DustThreadAction.Connect, this);

			while (!stop) {
				boolean change = false;

				while (!queue.isEmpty() && !stop) {
					change |= dowloadOne();
				}

				seen.clear();
				if ( change ) {
					next = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_NEXT);

					Dust.access(DustAccess.Commit, null, next);
				}

				synchronized (this) {
					try {
						wait();
					} catch (Throwable e) {
						DustException.swallow(e, "BackgroundDownloadQueue wait interrupted");
					}
				}
			}
		}

		public void optEnqueue() throws Exception {
			String key = Dust.access(DustAccess.Peek, null, null, HANDLE_MISC_ATT_KEY);

			synchronized (this) {
				if ( seen.add(key) ) {
					Map item = enqueueParams();

					queue.add(item);

					Dust.log(HANDLE_MISC_TAG_LEVEL_TRACE, "Download queue added", item, queue);
				}

				this.notifyAll();
			}
		}

		public boolean dowloadOne() {
			Map item;

			synchronized (this) {
				item = queue.remove(0);
				Dust.log(HANDLE_MISC_TAG_LEVEL_TRACE, "Download queue removed to download", item);
			}

			String urlRoot = (String) item.get(HANDLE_STREAM_ATT_URL);
			String key = (String) item.get(HANDLE_MISC_ATT_KEY);
			int timeout = (int) item.getOrDefault(HANDLE_MISC_ATT_TIMEOUT, 0);
			Collection<String> headers = (Collection<String>) item.get(HANDLE_NET_ATT_SRVCALL_HEADERS);

			String path = (String) item.get(HANDLE_MISC_ATT_PATH);

			String url = urlRoot + "/" + key;

			try {
				DustNetUtils.download(url, path, headers, timeout);
				Dust.log(HANDLE_MISC_TAG_LEVEL_TRACE, "Downloaded", item);
				return true;
			} catch (Throwable e) {
				DustException.swallow(e, "Downloading", url);

				if ( retryCount > retry.add(url) ) {
					synchronized (this) {
						queue.add(item);
						return false;
					}
				}
			}

			return true;
		}
	}

	BackgroundDownloadQueue queue;
	Thread queueLoaderThread;

	public DustNetHttpClientAgent() {
	}

	@Override
	public void init() throws Exception {
		next = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_NEXT);

//		queue = new BackgroundDownloadQueue();
//		Dust.manageThread(DustThreadAction.SetAnchor, queue);
//
//		queueLoaderThread = new Thread(queue, "TAP download");
//		queueLoaderThread.setDaemon(true);
//		queueLoaderThread.start();
	}

	@Override
	public void process() throws Exception {
		DustHandle cmd = Dust.access(DustAccess.Peek, null, null, HANDLE_MIND_ATT_CMD);

		if ( HANDLE_MISC_TAG_CMD_LOAD.equals(cmd) ) {
//			queue.optEnqueue();
			dowload(enqueueParams());
		}
	}
	
	public Map enqueueParams() {
		Map item = new TreeMap<>();

		for (DustHandle par : PARAMETERS) {
			Object val = Dust.access(DustAccess.Peek, null, null, par);
			if ( null != val ) {
				item.put(par, DustUtils.copy(val, true));
			}
		}
		return item;
	}

	public void dowload(Map item) throws Exception {
		String urlRoot = (String) item.get(HANDLE_STREAM_ATT_URL);
		String key = (String) item.get(HANDLE_MISC_ATT_KEY);
		int timeout = (int) item.getOrDefault(HANDLE_MISC_ATT_TIMEOUT, 0);
		Collection<String> headers = (Collection<String>) item.get(HANDLE_NET_ATT_SRVCALL_HEADERS);

		String path = (String) item.get(HANDLE_MISC_ATT_PATH);

		String url = urlRoot + "/" + key;

		DustNetUtils.download(url, path, headers, timeout);
	}
}
