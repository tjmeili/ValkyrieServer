package valkyrie.server.local.data;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Singleton for executing changes to server data
public class DataExecutor{

	private ExecutorService executor;

	private DataExecutor(){
		executor = Executors.newSingleThreadExecutor();
	}

	private static class SingletonHelper{
		private static final DataExecutor INSTANCE = new DataExecutor();
	}

	public static DataExecutor getInstance(){
		return SingletonHelper.INSTANCE;
	}

	public void execute(Runnable run){
		executor.execute(run);
	}
}