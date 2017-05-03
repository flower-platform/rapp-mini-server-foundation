package com.flowerplatform.rapp_mini_server.python;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crispico.events.IEvent;
import com.crispico.java.RunnableWithParam;

/**
 * Used to connect to <b>only one</b> python executable in order to access its
 * methods and receive the information using python's stdout.
 * 
 * @author Emanuel Antonache
 */
public class PythonRemoteControl {

	/**
	 * The bridge object responsible for creating objects and setting events.
	 */
	protected static final String BRIDGE_NAME = "remoteControllableRapp";

	/**
	 * Method used to create global objects in python.
	 */
	protected static final String CREATE_METHOD = "create";

	/**
	 * Method used to add a listener to an object in python.
	 */
	protected static final String ADD_EVENT_LISTENER_METHOD = "addEventListener";

	/**
	 * The command given to python which creates a new thread that calls loop
	 * method on every object created from java;
	 */
	protected static final String BEGIN_LOOP = "beginLoop";

	/**
	 * Used in order to avoid other messages written to Python's standard output
	 * that isn't related to our pipe.
	 */
	protected static final String PYTHON_DIRECT_TOKEN = "[Crispico Direct Message]";

	/**
	 * Used for messages received from Python that are not as a result of java
	 * writing something to Python's stdin (normally events).
	 */
	protected static final String PYTHON_EVENT_TOKEN = "[Crispico Event Message]";
	
	/**
	 * Used to describe messages that is useless for Java to parse. For example,
	 * a message saying that an object was created. This token is appended to either
	 * "direct" or "event" token, but most likely to "direct".
	 */
	protected static final String PYTHON_OPTIONAL_TOKEN = "[Crispico Optional Message]";
	
	/**
	 * Used to describe a message that was sent by python as a result of a caught
	 * exception (which probably caused python process to halt).
	 */
	protected static final String PYTHON_EXCEPTION_TOKEN = "[Crispico Exception Message]";

	/**
	 * Python executable path. If it isn't in $PATH, you should write the entire
	 * path to it here.
	 */
	protected static final String PYTHON_EXECUTABLE = "python";

	/**
	 * The package location of every event class. Is used to create the event
	 * object when a listener is added.
	 */
	protected static final String EVENT_PACKAGE = "com.crispico.events";
	
	/**
	 * Logs only transmission messages a.k.a. messages sent from java to python.
	 */
	protected static final Logger TRANSMISSION_LOG = LoggerFactory.getLogger(PythonRemoteControl.class.getName() + "_TRANSMISSION_LOG");
	
	/**
	 * Logs only known reception messages a.k.a. messages sent from python to java that java understands.
	 */
	protected static final Logger RECEPTION_KNOWN_LOG = LoggerFactory.getLogger(PythonRemoteControl.class.getName() + "_RECEPTION_KNOWN_LOG");
	
	/**
	 * Logs only unknown reception messages a.k.a. messages sent from python to java that java does not understand.
	 */
	protected static final Logger RECEPTION_UNKNOWN_LOG = LoggerFactory.getLogger(PythonRemoteControl.class.getName() + "_RECEPTION_UNKNOWN_LOG");

	/**
	 * The effective map for eventListeners, which will call the corresponding
	 * listener when an event from python comes.
	 */
	protected Map<String, RunnableWithParam<Object, IEvent>> eventListenersMap;

	/**
	 * The effective process launched by Python.
	 */
	protected Process pythonProcess;

	/**
	 * Will read from the Python standard output.
	 */
	protected BufferedReader readFromStdoutPythonProcess;

	/**
	 * Will write to Python standard input.
	 */
	protected BufferedWriter writeToStdinPythonProcess;

	/**
	 * Used to know its location and remove it at the end of this class.
	 */
	protected File execCloneFile;

	/**
	 * The command that will be launched.
	 */
	protected String executionCMD;

	/**
	 * The location to the cloned Python file.
	 */
	protected String executablePath;

	/**
	 * True if the python executable was not outside the jar and it had to be
	 * cloned outside the jar. This will trigger the file to be deleted once
	 * close() method is called.
	 */
	protected boolean executableWasCloned;

	/**
	 * Used to notify a corresponding thread when its message comes.
	 */
	protected BlockingQueue<Thread> threadsQueue = new LinkedBlockingQueue<>();

	/**
	 * Used to keep the non-event messages.
	 */
	protected BlockingQueue<String> messagesQueue = new LinkedBlockingQueue<>();
	
	/**
	 * Called at the begining of callFunction and used by a timeout thread to
	 * know if python did not respond in time (python process may have died
	 * and we cannot know otherwise).
	 */
	protected long callFunctionTimestamp = -1;

	/**
	 * Used by the timeout thread to signal to the current thread calling callFunction
	 * to throw an Exception.
	 */
	protected boolean timeoutDetected = false;

	/**
	 * The timeout thread which can awake a sleeping thread that waited too long
	 * and tells that thread to throw an Exception because of this reason.
	 */
	protected Thread timeoutThread = null;
	
	/**
	 * The PID of the python process. Only works in linux environment.
	 */
	protected Integer pythonPID = null;
	
	/**
	 * Used in order to stop sending and receiving messages to the python process.
	 */
	protected boolean isShuttingDown = false;

	/**
	 * The minimum difference between current time and the callFunctionTimestamp
	 * which causes the timeout thread to awake a sleeping thread that waited for
	 * python response for too long.
	 */
	protected static final int TIMEOUT_PERIOD_IN_MILLISECONDS = 10000;
	
	/**
	 * Specifies the number of milliseconds the timeout thread sleep before making
	 * a new check whether a thread waiting for a python function call exists
	 * and if it needs to awake him.
	 */
	protected static final int TIMEOUT_SLEEP_IN_MILLISECONDS = 1000;

	/**
	 * Makes the string easier to be identified by Python process. Mainly,
	 * whenever it sees a comma, it adds an escape separator, '\', which, in
	 * turn, will be used by Python to know whether that is a new argument or
	 * the coma is within the same argument.
	 * 
	 * @param classObjectName
	 *            the object from Python which will call the method
	 * @param classMethodName
	 *            the method that is called from Python's object
	 * @param arguments
	 *            the list of arguments given to that method
	 * @return the parsed string which will be sent to Python's standard input
	 */
	protected String parseString(String classObjectName, String classMethodName, Object[] arguments) {
		String result = classObjectName + "|" + classMethodName;
		if (arguments == null || arguments.length == 0)
			return result;
		result += "|";
		for (Object arg : arguments) {
			result += arg.toString().replace(",", "\\,") + ",";
		}
		return result.substring(0, result.length() - 1);
	}
	
	public void startPythonProcess() throws Exception {
		startPythonProcess("python/python-remote-control-bridge.py");
	}

	/**
	 * Creates a connection with executablePath file.
	 * 
	 * @param executablePath
	 *            the relative path of the executable file to communicate with
	 * @param arguments
	 *            Python native arguments; maybe verbose, etc.
	 */
	public void startPythonProcess(String filePath, String... arguments) throws Exception {
		this.executablePath = filePath;
		eventListenersMap = new HashMap<String, RunnableWithParam<Object, IEvent>>();

		executionCMD = PYTHON_EXECUTABLE + " ";
		for (String argument : arguments) {
			executionCMD += argument + " ";
		}
		executionCMD += executablePath;

		// we check if the executable file already exists outside the jar
		// if it does not, we create its folder structure together with the file
		// and write content to it (we basically create a clone)
		File foldersLocation = null;
		if (executablePath.contains("/")) {
			foldersLocation = new File(executablePath.substring(0, executablePath.lastIndexOf("/")));
		}
		execCloneFile = new File(executablePath);

		if (!execCloneFile.exists()) {
			executableWasCloned = true;

			if (foldersLocation != null && !foldersLocation.exists()) {
				foldersLocation.mkdirs();
			}

			InputStream exeIS = this.getClass().getResourceAsStream("/" + executablePath);
			BufferedReader exeBR = new BufferedReader(new InputStreamReader(exeIS));
			StringBuilder execContent = new StringBuilder();
			String line;

			while ((line = exeBR.readLine()) != null) {
				execContent.append(line);
				execContent.append("\n");
			}
			exeBR.close();
			BufferedWriter execWriter = new BufferedWriter(new FileWriter(execCloneFile.getAbsoluteFile()));
			execWriter.write(execContent.toString());
			execWriter.close();
		}
		pythonProcess = Runtime.getRuntime().exec(executionCMD);
		readFromStdoutPythonProcess = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
		writeToStdinPythonProcess = new BufferedWriter(new OutputStreamWriter(pythonProcess.getOutputStream()));
		
		if (!pythonProcess.getClass().getName().equals("java.lang.UNIXProcess")) {
			// maybe a better check would be to look at the system properties
			throw new IllegalStateException("Unsupported OS. Unix needed.");
		}
		// get the PID on unix/linux systems
		Field f;
		f = pythonProcess.getClass().getDeclaredField("pid");
		f.setAccessible(true);
		pythonPID = f.getInt(pythonProcess);

		// this gets called when we use a signal that causes the process to shutdown gracefully
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					// we close the python connection (and maybe delete the cloned python source)
					isShuttingDown = true;
					close();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}));

		timeoutThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(TIMEOUT_SLEEP_IN_MILLISECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (callFunctionTimestamp != -1
							&& System.currentTimeMillis() - callFunctionTimestamp > TIMEOUT_PERIOD_IN_MILLISECONDS) {
						timeoutDetected = true;
						Thread toRemoveThread = threadsQueue.remove();
						synchronized (toRemoveThread) {
							toRemoveThread.notify();
						}
					}
				}

			}
		});
		timeoutThread.start();
		
		// communication thread
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					beginCommunication();
				} catch (InterruptedException | IOException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
	}

	/**
	 * Call a method from a python object with the given arguments.
	 * <p>
	 * Arguments, if any, <b>respect the structure</b>: type, value, type, value
	 * etc. For example: <b>int</b>, 2, <b>long</b>, 5555555, <b>float</b>,
	 * 5.27, <b>str</b>, john, <b>bool</b>, True
	 * <p>
	 * 
	 * <p>
	 * This method is synchronized. I.e. if there are 2 threads, the second one
	 * will wait. This means that we don't need a mechanism (e.g. w/ tokens),
	 * that would allow us to have e.g. 2 simultaneous calls to Python. And we
	 * don't want things to happen simultaneously because Python has only 1
	 * thread (cf. current design). If it would work like a server (i.e. new
	 * call => new thread), then it would have been interesting to allow this in
	 * Java as well.
	 * </p>
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public synchronized String callFunction(String objectName, String function, String... arguments) {
		String messageToPython = parseString(objectName, function, arguments);
		if (TRANSMISSION_LOG.isDebugEnabled()) {
			TRANSMISSION_LOG.debug(messageToPython);
		}

		if (isShuttingDown)
			return null;

		threadsQueue.add(Thread.currentThread());
		try {
			writeToStdinPythonProcess.write(messageToPython + "\n");
			writeToStdinPythonProcess.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		callFunctionTimestamp = System.currentTimeMillis();
		synchronized (Thread.currentThread()) {
			try {
				Thread.currentThread().wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);			}
		}
		callFunctionTimestamp = -1;
		if (timeoutDetected) {
			timeoutDetected = false;
			throw new RuntimeException("callFunction timeout");
		}
		return messagesQueue.poll();
	}

	/**
	 * Creates a python object with given name and a given type (a.k.a. class)
	 * and possible arguments in constructor.
	 * <p>
	 * The objectType <b>must</b> be a <b>fully qualified path</b> a.k.a.
	 * Adafruit_DHT.DHTSensor.DHTSensor
	 * </p>
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public void createObject(String... objectNameTypeAndArgs) throws IOException, InterruptedException {
		callFunction(BRIDGE_NAME, CREATE_METHOD, objectNameTypeAndArgs);
	}

	public void addEventListener(String objectName, String eventHandlerField,
			RunnableWithParam<Object, IEvent> callback) throws IOException, InterruptedException {
		if (eventListenersMap.containsKey(getListenersMapKey(objectName, eventHandlerField))) {
			throw new RuntimeException("A callback with object = " + objectName + " and eventHandlerField = "
					+ eventHandlerField + " was already added.");
		}

		eventListenersMap.put(getListenersMapKey(objectName, eventHandlerField), callback);

		callFunction(BRIDGE_NAME, ADD_EVENT_LISTENER_METHOD, objectName, eventHandlerField);
	}

	/**
	 * Must be called after creating all objects, not before. Calling it makes
	 * python begin to send the event messages.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public void beginPythonEventLoop() throws IOException, InterruptedException {
		callFunction(BRIDGE_NAME, BEGIN_LOOP);
	}

	protected Object runEventListener(String objectName, String eventHandlerField, IEvent event) {
		return eventListenersMap.get(getListenersMapKey(objectName, eventHandlerField)).run(event);
	}

	protected String getListenersMapKey(String objectName, String eventHandlerField) {
		return objectName + "|" + eventHandlerField;
	}

	protected String readLine() throws IOException, InterruptedException {
		while (!isShuttingDown && !readFromStdoutPythonProcess.ready()) {
			Thread.sleep(200);
		}
		if (isShuttingDown)
			return null;
		String line = readFromStdoutPythonProcess.readLine();

		while (!line.startsWith(PYTHON_DIRECT_TOKEN) && !line.startsWith(PYTHON_EVENT_TOKEN) && !line.startsWith(PYTHON_EXCEPTION_TOKEN)) {
			if (RECEPTION_UNKNOWN_LOG.isDebugEnabled()) {
				RECEPTION_UNKNOWN_LOG.debug(line);
			}
			while (!isShuttingDown && !readFromStdoutPythonProcess.ready()) {
				Thread.sleep(200);
			}
			if (isShuttingDown)
				return null;
			line = readFromStdoutPythonProcess.readLine();
		}
		if (line.endsWith("\n"))
			line = line.substring(0,  line.length() - 1);
		line = line.replace("\\n", "\n");
		if (line.endsWith("\n"))
			line = line.substring(0,  line.length() - 1);
		if (RECEPTION_KNOWN_LOG.isDebugEnabled()) {
			RECEPTION_KNOWN_LOG.debug(line);
		}
		return line;
	}

	/**
	 * <b>Must</b> be called after everything is finished, when there is no need
	 * for communication with the Python process.
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	protected void close() throws IOException, InterruptedException {
		// we delete the cloned Python source file if that is the case
		if (executableWasCloned) {
			execCloneFile.delete();
			int slashLocation;
			while ((slashLocation = executablePath.lastIndexOf("/")) != -1) {
				executablePath = executablePath.substring(0, slashLocation);
				new File(executablePath).delete();
			}
		}
		System.out.println("Shutting down gracefully. Bye!");
		
		if (pythonProcess.isAlive()) {
			Process killProcess = Runtime.getRuntime().exec("kill -2 " + pythonPID);
			killProcess.waitFor();
		}
		while (pythonProcess.isAlive()) {
			Thread.sleep(500);
		}
	}

	/**
	 * Receives events and general messages and awakes the callFunction when he
	 * received the message
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	protected void beginCommunication() throws InterruptedException, IOException {
		boolean isEvent;

		while (true) {
			String line = readLine();
			
			if (line == null) {
				return;
			}
			
			isEvent = false;

			if (line.startsWith(PythonRemoteControl.PYTHON_DIRECT_TOKEN)) {
				line = line.substring(PythonRemoteControl.PYTHON_DIRECT_TOKEN.length());
			} else if (line.startsWith(PythonRemoteControl.PYTHON_EVENT_TOKEN)) {
				line = line.substring(PythonRemoteControl.PYTHON_EVENT_TOKEN.length());
				isEvent = true;
			} else if (line.startsWith(PythonRemoteControl.PYTHON_EXCEPTION_TOKEN)) {
				throw new RuntimeException("Exception from python: " + line);
			}
			
			if (line.startsWith(PythonRemoteControl.PYTHON_OPTIONAL_TOKEN)) {
				messagesQueue.add(line);
				Thread toRemoveThread = threadsQueue.take();
				while (toRemoveThread.getState() != Thread.State.WAITING);
				synchronized (toRemoveThread) {
					toRemoveThread.notify();
				}
				continue;
			}

			if (isEvent) {
				String[] lineArgs = line.split("\\|");
				if (lineArgs.length == 2)
					throw new RejectedExecutionException(
							"No event class was found in the message received from python");
				String eventClassName = lineArgs[2].split(",")[0];
				IEvent event;
				try {
					event = (IEvent) Class.forName(EVENT_PACKAGE + "." + eventClassName).newInstance();
				} catch (InstantiationException e) {
					throw new RejectedExecutionException(
							"Event class " + eventClassName + " could not be instantiated.");
				} catch (IllegalAccessException e) {
					throw new RejectedExecutionException("Event class " + eventClassName + " could not be accessed.");
				} catch (ClassNotFoundException e) {
					throw new RejectedExecutionException("Event class " + eventClassName + " was not found.");
				}
				event.fromString(lineArgs[2]);
				runEventListener(lineArgs[0], lineArgs[1], event);
			} else {
				messagesQueue.add(line);
				Thread toRemoveThread = threadsQueue.remove();
				while (toRemoveThread.getState() != Thread.State.WAITING);
				synchronized (toRemoveThread) {
					toRemoveThread.notify();
				}
			}
		}
	}
}
