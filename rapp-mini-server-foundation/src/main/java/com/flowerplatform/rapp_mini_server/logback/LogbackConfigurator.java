package com.flowerplatform.rapp_mini_server.logback;

import java.net.URL;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.StatusListenerConfigHelper;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * A Logback {@link Configurator} implementation, that either delegates to a very basic but custom configuration,
 * or to the regular configurator depending on a switch.
 * 
 * Important note : this configurator should get invoked ONLY once. Any other invocations will have no 
 * effect.
 *
 * Please be aware that the following (complete) list of conditions should be met, in order for this class to actually
 * get invoked/used in configuration.
 * - a file META-INF/services/ch.qos.logback.classic.spi.Configurator should be present on the classpath. Please note
 * that at the time of this writing, the file name is written wrongly in the official logback configuration docs here:
 * http://logback.qos.ch/manual/configuration.html
 * - the system property denoted by {@link ContextInitializer#CONFIG_FILE_PROPERTY} (currently logback.configurationFile)
 * should NOT be present (otherwise it will have priority over current class).
 * - a logback.groovy file should NOT be present on the classpath (otherwise it will have priority over current class).
 * - a logback-test.xml file should NOT be present on the classpath (otherwise it will have priority over current class).
 * - a logback.xml file should NOT be present on the classpath (otherwise it will have priority over current class).
 * - A successful call to {@link ConfigLocationConfiguration#configure()} should take place BEFORE anyone tries to initialize Logback (this
 * class relies on stuff in {@link ConfigLocationConfiguration} to do its work)
 *
 * If you want to use this class in order to avoid parsing logback configuration, and use a programatic configuration, then,
 * in addition to those stated above, please also make sure that :
 * - A classpath config.properties or additionally a user-home config.properties, is present, and the property denoted by
 * {@link ConfigLocationConfiguration#simpleLogPropertyName} is set to true (i.e. log.simple = true).
 * - Class {@link MinimalLogbackConfig} configures the log just the way you want it to.
 * 
 * @author Andrei Taras
 */
public class LogbackConfigurator extends ContextAwareBase implements Configurator {
	
	public static final String LOGBACK_MAIN_XML = "logback-main.xml";
	
	/**
	 * Global flag that allows us to config only once.
	 */
	private static boolean configured = false;
	
	@Override
	public void setContext(Context context) {
		synchronized (LogbackConfigurator.class) {
			// don't do anything if already configured
			if (!configured) {
				// don't set the configure flag here; rely on the configure() call to do it.
				super.setContext(context);
			}
		}
	}

	@Override
	public void configure(LoggerContext loggerContext) {
		synchronized (LogbackConfigurator.class) {
			if (!configured) {
				configured = true;
			} else {
				return;
			}
		}

		boolean logbackXml = Boolean.getBoolean(LOGBACK_MAIN_XML);
		
		StatusManager sm = loggerContext.getStatusManager();
		
		if (!logbackXml) {
			System.out.println("Logback config: simple mode (i.e. programmatic)");
			configureViaMinimalFlow();
		} else {
			System.out.println("Logback config: XML (normal) mode (i.e. via " + LOGBACK_MAIN_XML + ")");
			try {
				configureViaDefaultFlow();
			} catch (JoranException je) {
				sm.add(new ErrorStatus("Failure to configure via default flow", this, je));
			}
		}
	}
	
	private void configureViaDefaultFlow() throws JoranException {
		new CustomContextInitializer((LoggerContext)this.getContext()).autoConfig();
	}
	
	/**
	 * Installs a very basic logging configuration.
	 */
	private void configureViaMinimalFlow() {
        MinimalLogbackConfig basicConfigurator = new MinimalLogbackConfig();
        basicConfigurator.setContext(getLoggerContext());
        basicConfigurator.configure(getLoggerContext());
        
        StatusPrinter.printInCaseOfErrorsOrWarnings(getLoggerContext());
	}
	
	/**
	 * A custom {@link ContextInitializer} that AVOIDS loading configuration via service-provider loading
	 * facility (http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html).
	 * 
	 * This is because we want to avoid an infinite loop.
	 * 
	 * @author Andrei Taras
	 */
	private static class CustomContextInitializer extends ContextInitializer {

		/**
		 * Redefined in this class, because in parent class has package visibility.
		 */
		private LoggerContext loggerContext;
		
		public CustomContextInitializer(LoggerContext loggerContext) {
			super(loggerContext);
			this.loggerContext = loggerContext;
		}
		
		@Override
		public void autoConfig() throws JoranException {
			// Hijack the Logback default name
			System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, LOGBACK_MAIN_XML);
			
	        StatusListenerConfigHelper.installIfAsked(loggerContext);
	        URL url = findURLOfDefaultConfigurationFile(true);
	        if (url != null) {
	            configureByResource(url);
	        } else {
                BasicConfigurator basicConfigurator = new BasicConfigurator();
                basicConfigurator.setContext(loggerContext);
                basicConfigurator.configure(loggerContext);
	        }
		}
	}
	
	private LoggerContext getLoggerContext() {
		return (LoggerContext)this.getContext();
	}

	@Override
	public String toString() {
		return getClass().getName();
	}
}
