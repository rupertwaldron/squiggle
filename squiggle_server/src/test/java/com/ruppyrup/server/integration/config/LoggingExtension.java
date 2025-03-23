package com.ruppyrup.server.integration.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.ruppyrup.server.command.NullCommand;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.slf4j.LoggerFactory;


@Slf4j
public class LoggingExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback, ParameterResolver {
  private Logger logger;

  // create and start a ListAppender
  public static final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

  @Override
  public void afterAll(final ExtensionContext context) throws Exception {
    log.info("after all :: " + context);
  }

  @Override
  public void afterEach(final ExtensionContext context) throws Exception {
    log.info("after :: " + listAppender.list);
    listAppender.stop();
    listAppender.clearAllFilters();
  }

  @Override
  public void afterTestExecution(final ExtensionContext context) throws Exception {
    log.info("after Test execution :: " + context);
  }

  @Override
  public void beforeAll(final ExtensionContext context) throws Exception {
    log.info("before :: " + context);
  }

  @Override
  public void beforeEach(final ExtensionContext context) throws Exception {
    LoggingExtensionConfig config = context.getElement().get().getAnnotation(LoggingExtensionConfig.class);
    Class<?> testClass = Class.forName(config.value());
    logger = (Logger) LoggerFactory.getLogger(testClass);
    listAppender.start();
    logger.addAppender(listAppender);
    log.info("before each :: " + context);
  }

  @Override
  public void beforeTestExecution(final ExtensionContext context) throws Exception {
    log.info("before test execution :: " + context);
  }

  @Override
  public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) throws ParameterResolutionException {
    return true;
  }

  @Override
  public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) throws ParameterResolutionException {
    log.info("resolve Parameter :: " + parameterContext);
    return null;
  }

  @Override
  public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
    log.info("postProcessTestInstance :: " + testInstance);
  }
}
