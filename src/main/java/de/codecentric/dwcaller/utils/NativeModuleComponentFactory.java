package de.codecentric.dwcaller.utils;

import org.mule.weave.v2.interpreted.RuntimeModuleNodeCompiler;
import org.mule.weave.v2.parser.phase.CompositeModuleParsingPhasesManager;
import org.mule.weave.v2.parser.phase.ModuleLoader;
import org.mule.weave.v2.parser.phase.ModuleLoaderManager;
import org.mule.weave.v2.parser.phase.ModuleParsingPhasesManager;
import org.mule.weave.v2.runtime.ModuleComponents;
import org.mule.weave.v2.runtime.ModuleComponentsFactory;
import org.mule.weave.v2.sdk.ClassLoaderWeaveResourceResolver;
import org.mule.weave.v2.sdk.SPIBasedModuleLoaderProvider;
import org.mule.weave.v2.sdk.TwoLevelWeaveResourceResolver;
import org.mule.weave.v2.sdk.WeaveResourceResolver;

import de.codecentric.dwcaller.Main;
import scala.Function0;
import scala.Function1;
import scala.Option;
import scala.collection.Seq;
import scala.collection.mutable.MutableList;

public class NativeModuleComponentFactory implements ModuleComponentsFactory {
	@Override
	public ModuleComponents createComponents() {
		MutableList<Function0<ClassLoader>> classLoaders = new MutableList<>();
		classLoaders.appendElem(new Function0<ClassLoader>() {

			@Override
			public ClassLoader apply() {
				return Main.class.getClassLoader();
			}
		});
		
		WeaveResourceResolver resolver = new ClassLoaderWeaveResourceResolver(classLoaders);
		
		MutableList<ModuleLoader> loaders = new MutableList<>();
		loaders.appendElem(ModuleLoader.apply(resolver));
		Seq<ModuleLoader> spiModules = new SPIBasedModuleLoaderProvider(resolver).getModules();
		spiModules.toList().foreach(new Function1<ModuleLoader, Void>() {

			@Override
			public Void apply(ModuleLoader v1) {
				loaders.appendElem(v1);
				return null;
			}
		});
		
		ModuleParsingPhasesManager currentClassloader = ModuleParsingPhasesManager.apply(ModuleLoaderManager.apply(loaders));
		MutableList<ModuleParsingPhasesManager> phaseManagers = new MutableList<>();
		phaseManagers.appendElem(NativeSystemModuleComponents.systemModuleParser);
		phaseManagers.appendElem(currentClassloader);
		CompositeModuleParsingPhasesManager parser = CompositeModuleParsingPhasesManager.apply(phaseManagers);
		RuntimeModuleNodeCompiler systemModuleCompiler = RuntimeModuleNodeCompiler.apply(NativeSystemModuleComponents.systemModuleParser, Option.empty());
		RuntimeModuleNodeCompiler compiler = RuntimeModuleNodeCompiler.chain(currentClassloader, systemModuleCompiler, false);
		Function0<WeaveResourceResolver> resolverFactory = new Function0<WeaveResourceResolver>() {
			@Override
			public WeaveResourceResolver apply() {
				return resolver;
			}
		};
		TwoLevelWeaveResourceResolver tlr = new TwoLevelWeaveResourceResolver(NativeSystemModuleComponents.systemResourceResolver, resolverFactory);
	    return ModuleComponents.apply(new TwoLevelWeaveResourceResolver(NativeSystemModuleComponents.systemResourceResolver, resolverFactory), parser, compiler);
	}

}
