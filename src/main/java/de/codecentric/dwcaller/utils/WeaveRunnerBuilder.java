package de.codecentric.dwcaller.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.mule.weave.v2.io.service.CustomWorkingDirectoryService;
import org.mule.weave.v2.io.service.WorkingDirectoryService;
import org.mule.weave.v2.model.ServiceManager;
import org.mule.weave.v2.model.service.CharsetProviderService;
import org.mule.weave.v2.model.service.LoggingService;
import org.mule.weave.v2.model.service.ProtocolUrlSourceProviderResolverService;
import org.mule.weave.v2.model.service.ReadFunctionProtocolHandler;
import org.mule.weave.v2.model.service.UrlSourceProviderResolverService;
import org.mule.weave.v2.runtime.DataWeaveResult;
import org.mule.weave.v2.runtime.DataWeaveScript;
import org.mule.weave.v2.runtime.DataWeaveScriptingEngine;
import org.mule.weave.v2.runtime.InputType;
import org.mule.weave.v2.runtime.ModuleComponentsFactory;
import org.mule.weave.v2.runtime.ParserConfiguration;
import org.mule.weave.v2.runtime.ScriptingBindings;
import org.mule.weave.v2.runtime.SimpleModuleComponentFactory;

import scala.Function0;
import scala.Option;
import scala.Tuple2;
import scala.collection.immutable.HashMap;
import scala.collection.mutable.MutableList;

/**
 * Builder for a {@link WeaveRunner}.
 */
public class WeaveRunnerBuilder {
	private DataWeaveScriptingEngine scriptingEngine;
	private ServiceManager serviceManager;
	private List<Pattern> ignorePatterns;
	private List<File> pathElements;
	private boolean addClassPath;

	public WeaveRunnerBuilder() {
		ignorePatterns = new ArrayList<>();
		pathElements = new ArrayList<>();
	}

	public WeaveRunnerBuilder withIgnorePattern(Pattern pattern) {
		ignorePatterns.add(pattern);
		return this;
	}
	
	public WeaveRunnerBuilder withPathDir(File dir) {
		pathElements.add(dir);
		return this;
	}


	public WeaveRunnerBuilder withClassPath() {
		addClassPath = true;
		return this;
	}
	
	public WeaveRunner build() {
		if (addClassPath) {
			addJarsToClassPath();
		}
		PathBasedResourceResolver resourceResolver = new PathBasedResourceResolver(pathElements);
		ModuleComponentsFactory moduleComponentFactory = SimpleModuleComponentFactory.apply(resourceResolver);
		scriptingEngine = new DataWeaveScriptingEngine(moduleComponentFactory,
				ParserConfiguration.apply(new MutableList<>(), new MutableList<>()));
		SimpleLoggingService logger = new SimpleLoggingService();
		for (Pattern p : ignorePatterns) {
			logger.addIgnorePattern(p);
		}
		serviceManager = createServiceManager(logger, resourceResolver);
		return new WeaveRunnerImplementation(scriptingEngine, serviceManager);
	}

	private void addJarsToClassPath() {
		String classPath = System.getProperty("java.class.path");
		String pathSep = System.getProperty("path.separator");
		StringTokenizer tok = new StringTokenizer(classPath, pathSep);
		while (tok.hasMoreTokens()) {
			String s = tok.nextToken();
			pathElements.add(new File(s));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ServiceManager createServiceManager(LoggingService logger, PathBasedResourceResolver pathBasedResourceResolver) {
		Function0<File> directoryFunction = new Function0<File>() {
			@Override
			public File apply() {
				File dwHome = new File(new File(System.getProperty("user.home")), ".dw");
				File tmpDirectory = new File(dwHome, "tmp");
				if (!tmpDirectory.exists()) {
					tmpDirectory.mkdirs();
				}
				return tmpDirectory;
			}
		};
		HashMap<Class<?>, ?> customServices = new HashMap<>();
		MutableList<ReadFunctionProtocolHandler> handlers = new MutableList<>();
		// TODO: Missing in Sequence: UrlProtocolHandler
		handlers.appendElem(new WeavePathProtocolHandler(pathBasedResourceResolver));
		ProtocolUrlSourceProviderResolverService service = new ProtocolUrlSourceProviderResolverService(handlers);
		customServices.$plus(new Tuple2(UrlSourceProviderResolverService.class, service));
		customServices.$plus(
				new Tuple2(WorkingDirectoryService.class, new CustomWorkingDirectoryService(directoryFunction, true)));
		customServices.$plus(new Tuple2(CharsetProviderService.class, createCharsetProvider()));

		return ServiceManager.apply(logger, customServices);
	}

	private CharsetProviderService createCharsetProvider() {
		return new CharsetProviderService() {
			@Override
			public Charset defaultCharset() {
				return StandardCharsets.UTF_8;
			}
		};
	}

	private static class WeaveRunnerImplementation implements WeaveRunner {
		private DataWeaveScriptingEngine scriptingEngine;
		private ServiceManager serviceManager;

		WeaveRunnerImplementation(DataWeaveScriptingEngine scriptingEngine, ServiceManager serviceManager) {
			this.scriptingEngine = scriptingEngine;
			this.serviceManager = serviceManager;
		}

		@Override
		public DataWeaveScriptingEngine getScriptingEngine() {
			return scriptingEngine;
		}

		@Override
		public ServiceManager getServiceManager() {
			return serviceManager;
		}

		@Override
		public DataWeaveScript compile(File script, ScriptingBindings bindings) {
			InputType[] inputs = bindingsToInputs(bindings);
			return scriptingEngine.compile(script, inputs);
		}

		private InputType[] bindingsToInputs(ScriptingBindings bindings) {
			List<InputType> inputs = new ArrayList<>();
			scala.collection.Iterator<String> iter = bindings.entries().iterator();
			while (iter.hasNext()) {
				inputs.add(new InputType(iter.next(), Option.empty()));
			}
			return inputs.toArray(new InputType[inputs.size()]);
		}

		@Override
		public DataWeaveResult runScript(DataWeaveScript script, ScriptingBindings bindings,
				String defaultOutputMimeType) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			return runScript(script, bindings, defaultOutputMimeType, bos);
		}

		@Override
		public DataWeaveResult runScript(DataWeaveScript script, ScriptingBindings bindings,
				String defaultOutputMimeType, OutputStream bos) {
			Option<Object> out = Option.apply(bos);
			return script.write(bindings, serviceManager, "application/java", out);
		}
	}
}
