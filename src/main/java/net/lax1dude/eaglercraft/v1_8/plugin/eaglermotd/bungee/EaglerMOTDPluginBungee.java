package net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.bungee;

import java.io.File;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.EaglerMOTDConfiguration;
import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.EaglerMOTDConnectionUpdater;
import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.EaglerMOTDLoggerAdapter;
import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.bungee.command.CommandMOTDReloadBungee;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_bungeecord.EaglerXBungee;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_bungeecord.api.query.EaglerQueryHandler;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_bungeecord.config.EaglerListenerConfig;
import net.md_5.bungee.api.plugin.Plugin;

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
public class EaglerMOTDPluginBungee extends Plugin {

	private static EaglerMOTDPluginBungee instance = null;

	public final EaglerMOTDConfiguration conf = new EaglerMOTDConfiguration();
	public final List<EaglerMOTDConnectionUpdater> motdConnections = new LinkedList();
	public final EaglerMOTDLoggerAdapter loggerAdapter;
	private Timer tickTimer = null;
	private final List<String> installedQueries = new ArrayList();

	public EaglerMOTDPluginBungee() {
		instance = this;
		loggerAdapter = new EaglerMOTDLoggerAdapter() {
			@Override
			public void info(String msg) {
				EaglerMOTDPluginBungee.this.getLogger().info(msg);
			}

			@Override
			public void warn(String msg) {
				EaglerMOTDPluginBungee.this.getLogger().warning(msg);
			}

			@Override
			public void error(String msg) {
				EaglerMOTDPluginBungee.this.getLogger().severe(msg);
			}
		};
	}

	@Override
	public void onLoad() {
		File dataFolder = getDataFolder();
		if(!dataFolder.isDirectory() && !dataFolder.mkdirs()) {
			throw new RuntimeException("Could not create config folder!");
		}
		
		try {
			conf.reload(dataFolder, loggerAdapter, getListenerNames());
		}catch(IOException ex) {
			throw new RuntimeException("Could not reload config!", ex);
		}
	}

	public void installQueryHandlers() {
		for(String str : conf.queryTypes.keySet()) {
			EaglerQueryHandler.registerQueryType(str, EaglerMOTDQueryHandlerBungee.class);
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
		Collection<EaglerListenerConfig> figs = EaglerXBungee.getEagler().getConfig().getServerListeners();
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

	@Override
	public void onEnable() {
		getProxy().getPluginManager().registerListener(this, new EaglerMOTDListenerBungee(this));
		getProxy().getPluginManager().registerCommand(this, new CommandMOTDReloadBungee(this));
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
								EaglerMOTDPluginBungee.this.getLogger().log(Level.SEVERE, "Error ticking MOTD '" + (c.currentMessage == null ? "null" : c.currentMessage.name) + "' on listener " + c.listenerName, t);
								c.close();
								itr.remove();
							}
						}
					}
				}
				
			}, 0, 50l);
		}
	}

	@Override
	public void onDisable() {
		getProxy().getPluginManager().unregisterListeners(this);
		getProxy().getPluginManager().unregisterCommands(this);
		removeQueryHandlers();
		if(tickTimer != null) {
			tickTimer.cancel();
			tickTimer = null;
		}
	}

	public static EaglerMOTDPluginBungee getPlugin() {
		return instance;
	}

	public static Logger logger() {
		return instance.getLogger();
	}
}
