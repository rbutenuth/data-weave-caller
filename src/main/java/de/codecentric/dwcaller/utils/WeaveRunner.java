package de.codecentric.dwcaller.utils;

import java.io.File;
import java.io.OutputStream;

import org.mule.weave.v2.model.ServiceManager;
import org.mule.weave.v2.runtime.DataWeaveResult;
import org.mule.weave.v2.runtime.DataWeaveScript;
import org.mule.weave.v2.runtime.DataWeaveScriptingEngine;
import org.mule.weave.v2.runtime.ScriptingBindings;

public interface WeaveRunner {

	public DataWeaveScriptingEngine getScriptingEngine();
	
	public DataWeaveScript compile(File script, ScriptingBindings bindings);
	
	public ServiceManager getServiceManager();
	
	public DataWeaveResult runScript(DataWeaveScript script, ScriptingBindings bindings, String defaultOutputMimeType);
	
	public DataWeaveResult runScript(DataWeaveScript script, ScriptingBindings bindings, String defaultOutputMimeType, OutputStream out);
}
