package de.codecentric.dwcaller.utils;

import org.mule.weave.v2.parser.ast.variables.NameIdentifier;
import org.mule.weave.v2.parser.phase.ModuleLoader;
import org.mule.weave.v2.parser.phase.ModuleLoaderManager;
import org.mule.weave.v2.parser.phase.ModuleParsingPhasesManager;
import org.mule.weave.v2.parser.phase.ParsingContext;
import org.mule.weave.v2.sdk.ClassLoaderWeaveResourceResolver;
import org.mule.weave.v2.sdk.WeaveResourceResolver;

import scala.Option;
import scala.collection.mutable.MutableList;

public class NativeSystemModuleComponents {
	/**
	 * The system resource resolver
	 */
	// val systemResourceResolver: WeaveResourceResolver =
	// ClassLoaderWeaveResourceResolver()
	// TODO: Use constructor with Seq of classloaders instead?
	public static final WeaveResourceResolver systemResourceResolver;

	/**
	 * Handles the parsing of the modules that are on the SystemClassLoader
	 */
	public static final ModuleParsingPhasesManager systemModuleParser;

	static {
		systemResourceResolver = ClassLoaderWeaveResourceResolver.apply();
		MutableList<ModuleLoader> loaders = new MutableList<>();
		loaders.appendElem(ModuleLoader.apply(systemResourceResolver));
		systemModuleParser = ModuleParsingPhasesManager.apply(ModuleLoaderManager.apply(loaders));
		systemModuleParser.typeCheckModule(NameIdentifier.CORE_MODULE(), ParsingContext.apply(NameIdentifier.anonymous(), systemModuleParser));
		systemModuleParser.typeCheckModule(NameIdentifier.apply("dw::core::Strings", Option.empty()),ParsingContext.apply(NameIdentifier.anonymous(), systemModuleParser));
		systemModuleParser.typeCheckModule(NameIdentifier.ARRAYS_MODULE(), ParsingContext.apply(NameIdentifier.anonymous(), systemModuleParser));
		systemModuleParser.typeCheckModule(NameIdentifier.OBJECTS_MODULE(),ParsingContext.apply(NameIdentifier.anonymous(), systemModuleParser));
		systemModuleParser.typeCheckModule(NameIdentifier.RUNTIME_MODULE(),ParsingContext.apply(NameIdentifier.anonymous(), systemModuleParser));
		systemModuleParser.typeCheckModule(NameIdentifier.SYSTEM_MODULE(),ParsingContext.apply(NameIdentifier.anonymous(), systemModuleParser));

		// disabled in Scala class:
		// systemModuleParser.typeCheckModule(NameIdentifier("dw::io::http::Server"),
		// ParsingContext(NameIdentifier.anonymous, systemModuleParser))
		// systemModuleParser.typeCheckModule(NameIdentifier("dw::io::file::FileSystem"), ParsingContext(NameIdentifier.anonymous, systemModuleParser))
		// systemModuleParser.typeCheckModule(NameIdentifier("dw::deps::Deps"), ParsingContext(NameIdentifier.anonymous, systemModuleParser))
	}
}
