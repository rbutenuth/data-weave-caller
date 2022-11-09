package de.codecentric.dwcaller.utils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mule.weave.v2.io.service.CustomWorkingDirectoryService;
import org.mule.weave.v2.io.service.WorkingDirectoryService;
import org.mule.weave.v2.model.ServiceManager;
import org.mule.weave.v2.model.service.CharsetProviderService;
import org.mule.weave.v2.model.service.InMemoryLoggingService;
import org.mule.weave.v2.model.service.LoggingService;
import org.mule.weave.v2.model.service.ProtocolUrlSourceProviderResolverService;
import org.mule.weave.v2.model.service.ReadFunctionProtocolHandler;
import org.mule.weave.v2.model.service.UrlSourceProviderResolverService;
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
 * Currently the builder is complete overkill, but we may add some configurability (patch etc.) later.
 */
public class WeaveRunnerBuilder {

	public WeaveRunnerBuilder() {
	}
	
	public WeaveRunner build() {
		return new WeaveRunnerImplementation();
	}
	
	private static class WeaveRunnerImplementation implements WeaveRunner {
		private DataWeaveScriptingEngine scriptingEngine;
		private ServiceManager serviceManager;

		WeaveRunnerImplementation() {
			ModuleComponentsFactory moduleComponentFactory = SimpleModuleComponentFactory.apply(); // apply with WeaveResourceResolver possible, too
			scriptingEngine = new DataWeaveScriptingEngine(moduleComponentFactory, ParserConfiguration.apply(new MutableList<>(), new MutableList<>()));
			serviceManager = createServiceManager();
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
			scala.collection.Iterator<String> iter =  bindings.entries().iterator();
			while (iter.hasNext()) {
				inputs.add(new InputType(iter.next(), Option.empty()));
			}
			return inputs.toArray(new InputType[inputs.size()]);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private ServiceManager createServiceManager() {
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
			LoggingService logger = new InMemoryLoggingService();
			HashMap<Class<?>, ?> customServices = new HashMap<>();
			MutableList<ReadFunctionProtocolHandler> handlers = new MutableList<>();
			// TODO: Missing in Sequence: UrlProtocolHandler
			PathBasedResourceResolver pathBasedResourceResolver = new PathBasedResourceResolver(Collections.emptyList());
			handlers.appendElem(new WeavePathProtocolHandler(pathBasedResourceResolver));
			ProtocolUrlSourceProviderResolverService service = new ProtocolUrlSourceProviderResolverService(handlers);
			customServices.$plus(new Tuple2(UrlSourceProviderResolverService.class, service));
			customServices.$plus(new Tuple2(WorkingDirectoryService.class, new CustomWorkingDirectoryService(directoryFunction, true)));
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
	}
}
