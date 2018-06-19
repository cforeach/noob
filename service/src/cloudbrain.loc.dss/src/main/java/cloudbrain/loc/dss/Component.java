package cloudbrain.loc.dss;

import cloudbrain.loc.dss.task.BrowserAppTask;
import net.svr.mon.daemon.IComponent;
import net.svr.mon.daemon.IHAWorker;

public class Component implements IComponent {

	private static IHAWorker _worker;

	public static IHAWorker getWorker() {
		return _worker;
	}

	@Override
	public void setHAWorker(IHAWorker haWorker) {
		_worker = haWorker;
	}

	@Override
	public void start(String[] args) {
		if (Settings.runBrowserAppTask()) {
			BrowserAppTask.init();
		}
	}

	@Override
	public void stop(String[] args) {
	}
}
