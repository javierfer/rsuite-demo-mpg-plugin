package com.rsuite.demo.mpg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.reallysi.rsuite.api.extensions.ExecutionContext;
import com.reallysi.rsuite.api.extensions.Plugin;
import com.reallysi.rsuite.api.extensions.PluginLifecycleListener;

public class PluginActivator implements PluginLifecycleListener {

	private static final Log log = LogFactory.getLog(PluginActivator.class);

	@Override
	public void start(ExecutionContext context, Plugin plugin) {

		log.info("Demo MPG plugin started.");

	}

	@Override
	public void stop(ExecutionContext context, Plugin plugin) {
		log.info("Demo MPG plugin stopped.");
	}

}
