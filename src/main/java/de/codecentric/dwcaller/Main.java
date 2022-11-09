package de.codecentric.dwcaller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mule.weave.v2.io.service.CustomWorkingDirectoryService;
import org.mule.weave.v2.io.service.WorkingDirectoryService;
import org.mule.weave.v2.model.EvaluationContext;
import org.mule.weave.v2.model.ServiceManager;
import org.mule.weave.v2.model.service.CharsetProviderService;
import org.mule.weave.v2.model.service.InMemoryLoggingService;
import org.mule.weave.v2.model.service.LoggingService;
import org.mule.weave.v2.model.service.ProtocolUrlSourceProviderResolverService;
import org.mule.weave.v2.model.service.ReadFunctionProtocolHandler;
import org.mule.weave.v2.model.service.UrlSourceProviderResolverService;
import org.mule.weave.v2.module.DataFormat;
import org.mule.weave.v2.module.DataFormatManager;
import org.mule.weave.v2.parser.ast.variables.NameIdentifier;
import org.mule.weave.v2.runtime.ArrayDataWeaveValue;
import org.mule.weave.v2.runtime.DataWeaveNameValue;
import org.mule.weave.v2.runtime.DataWeaveResult;
import org.mule.weave.v2.runtime.DataWeaveScript;
import org.mule.weave.v2.runtime.DataWeaveScriptingEngine;
import org.mule.weave.v2.runtime.DataWeaveValue;
import org.mule.weave.v2.runtime.ExecuteResult;
import org.mule.weave.v2.runtime.InputType;
import org.mule.weave.v2.runtime.ModuleComponentsFactory;
import org.mule.weave.v2.runtime.ObjectDataWeaveValue;
import org.mule.weave.v2.runtime.ParserConfiguration;
import org.mule.weave.v2.runtime.ScriptingBindings;
import org.mule.weave.v2.runtime.SimpleDataWeaveValue;
import org.mule.weave.v2.runtime.SimpleModuleComponentFactory;
import org.mule.weave.v2.sdk.WeaveResourceResolver;

import de.codecentric.dwcaller.test.Test;
import de.codecentric.dwcaller.test.TextReporter;
import de.codecentric.dwcaller.utils.DataWeaveUtils;
import de.codecentric.dwcaller.utils.NativeModuleComponentFactory;
import de.codecentric.dwcaller.utils.PathBasedResourceResolver;
import de.codecentric.dwcaller.utils.WeavePathProtocolHandler;
import scala.Function0;
import scala.Function1;
import scala.Option;
import scala.Tuple2;
import scala.collection.Set;
import scala.collection.generic.CanBuildFrom;
import scala.collection.immutable.HashMap;
import scala.collection.mutable.MutableList;

public class Main {
	
	public static void main(String[] args) {
		ScriptingBindings bindings = new ScriptingBindings();
		bindings.addBinding("payload", "{\"foo\": 42}", "application/json");
		
		ModuleComponentsFactory moduleComponentFactory = SimpleModuleComponentFactory.apply(); // apply with WeaveResourceResolver possible, too
		DataWeaveScriptingEngine scriptingEngine = new DataWeaveScriptingEngine(moduleComponentFactory, ParserConfiguration.apply(new MutableList<>(), new MutableList<>()));
		//DataWeaveScript script = weaveScriptingEngine.compile("[ 41, '42', { a: 43 }]");
		
		//scriptingEngine.compile(script, nameIdentifier, inputs.entries().map(wi => new InputType(wi, None)).toArray, defaultOutputMimeType)
		
		InputType[] inputs = bindingsToInputs(bindings);
		//DataWeaveScript script = scriptingEngine.compile("payload", NameIdentifier.apply("script.dwl", Option.empty()), inputs, "application/json");
		DataWeaveScript script = scriptingEngine.compile(new File("src/main/resources/test.dwl"), inputs);
		
		ServiceManager serviceManager = createServiceManager();
		// use script.write() instead? With some target? see NativeRuntime.scala, line 92:
		// val result: DataWeaveResult = dataWeaveScript.write(inputs, serviceManager, Option(out))
		//ExecuteResult result = script.exec(bindings, serviceManager);
		//DataWeaveValue value = result.asDWValue();
		//System.out.println("result: " + toString(value));
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Option<Object> out = Option.apply(bos);
		// write(ScriptingBindings bindings, ServiceManager serviceManager, String outputMimeType, Option<Object> target) {
		DataWeaveResult result = script.write(bindings, serviceManager, "application/java", out);
		Object content = result.getContent();
		@SuppressWarnings("unchecked")
		Test test = new Test((Map<String, Object>)content);
		System.out.println(TextReporter.test2report(test));
//		System.out.println("out: " + new String(bos.toByteArray()));
	}
	
	private static InputType[] bindingsToInputs(ScriptingBindings bindings) {
		// inputs.entries().map(wi => new InputType(wi, None)).toArray
		List<InputType> inputs = new ArrayList<>();
		scala.collection.Iterator<String> iter =  bindings.entries().iterator();
		while (iter.hasNext()) {
			inputs.add(new InputType(iter.next(), Option.empty()));
		}
		return inputs.toArray(new InputType[inputs.size()]);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ServiceManager createServiceManager() {
		// ResourceManager resourceManager = new ResourceManager();
		// WeaveServicesProvider serviceProvider = new SPIWeaveServicesProvider();
		DataWeaveUtils dataWeaveUtils = new DataWeaveUtils();
		Function0<File> directoryFunction = new Function0<File>() {
			@Override
			public File apply() {
				return dataWeaveUtils.getWorkingHome();
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
		//classOf[WorkingDirectoryService] -> new CustomWorkingDirectoryService(dataWeaveUtils.getWorkingHome(), true),

		return ServiceManager.apply(logger, customServices);
	}

	private static CharsetProviderService createCharsetProvider() {
		return new CharsetProviderService() {
			
			@Override
			public Charset defaultCharset() {
				return StandardCharsets.UTF_8;
			}
		};
	}

	public static String toString(DataWeaveValue value) {
		StringBuilder sb = new StringBuilder();
		toString(value, sb, 0);
		return sb.toString();
	}

	private static void toString(DataWeaveValue value, StringBuilder sb, int depth) {
		if (value instanceof SimpleDataWeaveValue) {
			SimpleDataWeaveValue s = (SimpleDataWeaveValue) value;
			sb.append(s.valueAsString());
		} else if (value instanceof ObjectDataWeaveValue) {
			ObjectDataWeaveValue o = (ObjectDataWeaveValue) value;
			DataWeaveNameValue[] entries = o.entries();
			indent(sb, depth);
			sb.append("{\n");
			for (int i = 0; i < entries.length; i++) {
				indent(sb, depth + 1);
				sb.append(entries[i].name().name()).append(": ");
				toString(entries[i].value(), sb, depth + 2);
				if (i < entries.length - 1) {
					sb.append(",");
				}
				sb.append("\n");
			}
			indent(sb, depth);
			sb.append("}\n");
		} else if (value instanceof ArrayDataWeaveValue) {
			ArrayDataWeaveValue a = (ArrayDataWeaveValue) value;
			DataWeaveValue[] elements = a.elements();
			indent(sb, depth);
			sb.append("[\n");
			for (int i = 0; i < elements.length; i++) {
				indent(sb, depth);
				toString(elements[i], sb, depth + 1);
				if (i < elements.length - 1) {
					sb.append(",");
				}
				sb.append("\n");
			}
			indent(sb, depth);
			sb.append("]\n");
		} else {
			throw new IllegalArgumentException("no known type: " + value);
		}
	}

	private static void indent(StringBuilder sb, int depth) {
		for (int i = 0; i < depth; i++) {
			sb.append("  ");
		}
	}

}
