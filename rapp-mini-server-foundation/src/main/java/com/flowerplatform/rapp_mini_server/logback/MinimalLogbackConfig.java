package com.flowerplatform.rapp_mini_server.logback;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.layout.TTLLLayout;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.spi.ContextAwareBase;

/**
 * Logback configuration used when fast startup is very important. Inspired from {@link BasicConfigurator}.
 * 
 * @author Andrei Taras
 */
public class MinimalLogbackConfig extends ContextAwareBase implements Configurator {
    public void configure(LoggerContext lc) {
        addInfo("Setting up default configuration.");
        
        ConsoleAppender<ILoggingEvent> ca = new ConsoleAppender<ILoggingEvent>();
        ca.setContext(lc);
        ca.setName("console");
        LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<ILoggingEvent>();
        encoder.setContext(lc);
        
 
        // same as 
        // PatternLayout layout = new PatternLayout();
        // layout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        TTLLLayout layout = new TTLLLayout();
 
        layout.setContext(lc);
        layout.start();
        encoder.setLayout(layout);
        
        ca.setEncoder(encoder);
        ca.start();
        
        Logger rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        
        rootLogger.addAppender(ca);
    }

	@Override
	public String toString() {
		return getClass().getName();
	}
}
