package net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.velocity;

import java.io.File;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.EaglerMOTDConfiguration;
import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.EaglerMOTDConnectionUpdater;
import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.EaglerMOTDLoggerAdapter;
import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.velocity.command.CommandMOTDReloadVelocity;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.EaglerXVelocity;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.EaglerXVelocityVersion;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.api.query.EaglerQueryHandler;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.command.EaglerCommand;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.config.EaglerListenerConfig;

/**
 * Copyright (c) 2024 lax1dude. All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
@Plugin(
		id = EaglerMOTDPluginVersion.ID,
		name = EaglerMOTDPluginVersion.NAME,
		description = EaglerMOTDPluginVersion.DESCRIPTION,
		version = EaglerMOTDPluginVersion.VERSION,
		authors = {
			EaglerMOTDPluginVersion.AUTHOR
		},
		dependencies = {
			@Dependency(
				id = EaglerXVelocityVersion.PLUGIN_ID,
				optional = false
			)
		}
)
public class EaglerMOTDPluginVelocity {

	private static EaglerMOTDPluginVelocity instance = null;
	private final ProxyServer proxy;
	private final Logger logger;
	private final Path dataDirAsPath;
	private final File dataDir;

	public final EaglerMOTDConfiguration conf = new EaglerMOTDConfiguration();
	public final List<EaglerMOTDConnectionUpdater> motdConnections = new LinkedList();
	public final EaglerMOTDLoggerAdapter loggerAdapter;
	private Timer tickTimer = null;
	private final List<String> installedQueries = new ArrayList();

	@Inject
	public EaglerMOTDPluginVelocity(ProxyServer proxyIn, Logger loggerIn, @DataDirectory Path dataDirIn) {
		instance = this;
		proxy = proxyIn;
		logger = loggerIn;
		dataDirAsPath = dataDirIn;
		dataDir = dataDirIn.toFile();
		loggerAdapter = new EaglerMOTDLoggerAdapter() {
			@Override
			public void info(String msg) {
				EaglerMOTDPluginVelocity.this.logger.info(msg);
			}

			@Override
			public void warn(String msg) {
				EaglerMOTDPluginVelocity.this.logger.warn(msg);
			}

			@Override
			public void error(String msg) {
				EaglerMOTDPluginVelocity.this.logger.error(msg);
			}
		};
	}

	public void reloadConfig() {
		if(!dataDir.isDirectory() && !dataDir.mkdirs()) {
			throw new RuntimeException("Could not create config folder!");
		}
		
		try {
			conf.reload(dataDir, loggerAdapter, getListenerNames());
		}catch(IOException ex) {
			throw new RuntimeException("Could not reload config!", ex);
		}
	}

	public void installQueryHandlers() {
		for(String str : conf.queryTypes.keySet()) {
			EaglerQueryHandler.registerQueryType(str, EaglerMOTDQueryHandlerVelocity.class);
			installedQueries.add(str);
		}
	}

	public void removeQueryHandlers() {
		for(String str : installedQueries) {
			EaglerQueryHandler.unregisterQueryType(str);
		}
		installedQueries.clear();
	}

	public Collection<String> getListenerNames() {
		Collection<EaglerListenerConfig> figs = EaglerXVelocity.getEagler().getConfig().getServerListeners();
		Collection<String> ret = new ArrayList(figs.size());
		for(EaglerListenerConfig el : figs) {
			ret.add(getListenerName(el));
		}
		return ret;
	}

	public static String getListenerName(EaglerListenerConfig listenerConf) {
		InetSocketAddress sockAddr = listenerConf.getAddress();
		if(sockAddr == null) {
			sockAddr = listenerConf.getAddressV6();
			if(sockAddr == null) {
				throw new RuntimeException("Listener doesn't have an address: " + listenerConf);
			}
		}
		InetAddress addr = sockAddr.getAddress();
		if(addr instanceof Inet6Address) {
			return "[" + addr.getHostAddress() + "]:" + sockAddr.getPort();
		}else {
			return addr.getHostAddress() + ":" + sockAddr.getPort();
		}
	}

	@Subscribe
    public void onProxyInit(ProxyInitializeEvent e) {
		reloadConfig();
		proxy.getEventManager().register(this, new EaglerMOTDListenerVelocity(this));
		registerCommand(new CommandMOTDReloadVelocity(this));
		installQueryHandlers();
		if(tickTimer == null) {
			tickTimer = new Timer("MOTD Tick Timer");
			tickTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					synchronized(motdConnections) {
						Iterator<EaglerMOTDConnectionUpdater> itr = motdConnections.iterator();
						while(itr.hasNext()) {
							EaglerMOTDConnectionUpdater c = itr.next();
							try {
								if(!c.tick()) {
									itr.remove();
								}
							}catch(Throwable t) {
								EaglerMOTDPluginVelocity.this.getLogger().error("Error ticking MOTD '" + (c.currentMessage == null ? "null" : c.currentMessage.name) + "' on listener " + c.listenerName, t);
								c.close();
								itr.remove();
							}
						}
					}
				}
				
			}, 0, 50l);
		}
	}

	@Subscribe
    public void onProxyShutdown(ProxyShutdownEvent e) {
		removeQueryHandlers();
		if(tickTimer != null) {
			tickTimer.cancel();
			tickTimer = null;
		}
	}

	private void registerCommand(EaglerCommand cmd) {
		CommandManager cmdManager = proxy.getCommandManager();
		cmdManager.register(cmdManager.metaBuilder(cmd.name).aliases(cmd.alias).plugin(this).build(), cmd);
	}

	public ProxyServer getProxy() {
		return proxy;
	}

	public Logger getLogger() {
		return logger;
	}

	public File getDataFolder() {
		return dataDir;
	}

	public static EaglerMOTDPluginVelocity getPlugin() {
		return instance;
	}

	public static ProxyServer proxy() {
		return instance.proxy;
	}

	public static Logger logger() {
		return instance.logger;
	}
}
