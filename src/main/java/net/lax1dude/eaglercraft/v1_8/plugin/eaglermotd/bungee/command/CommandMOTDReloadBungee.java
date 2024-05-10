package net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.bungee.command;

import java.util.logging.Level;

import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.EaglerMOTDLoggerAdapter;
import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.bungee.EaglerMOTDPluginBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.command.ConsoleCommandSender;

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
public class CommandMOTDReloadBungee extends Command {

	private final EaglerMOTDPluginBungee plugin;

	public CommandMOTDReloadBungee(EaglerMOTDPluginBungee plugin) {
		super("motd-reload", "eaglermotd.command.reload");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender arg0, String[] arg1) {
		try {
			plugin.removeQueryHandlers();
			plugin.conf.reload(plugin.getDataFolder(), new EaglerMOTDLoggerAdapter() {
				@Override
				public void info(String msg) {
					CommandMOTDReloadBungee.this.plugin.getLogger().info(msg);
					if(!(arg0 instanceof ConsoleCommandSender)) {
						arg0.sendMessage(new TextComponent(ChatColor.GREEN + "[EaglerMOTD] " + msg));
					}
				}

				@Override
				public void warn(String msg) {
					CommandMOTDReloadBungee.this.plugin.getLogger().warning(msg);
					if(!(arg0 instanceof ConsoleCommandSender)) {
						arg0.sendMessage(new TextComponent(ChatColor.YELLOW + "[EaglerMOTD] " + msg));
					}
				}

				@Override
				public void error(String msg) {
					CommandMOTDReloadBungee.this.plugin.getLogger().severe(msg);
					if(!(arg0 instanceof ConsoleCommandSender)) {
						arg0.sendMessage(new TextComponent(ChatColor.RED + "[EaglerMOTD] " + msg));
					}
				}
			}, plugin.getListenerNames());
			plugin.installQueryHandlers();
		}catch(Throwable ex) {
			if(!(arg0 instanceof ConsoleCommandSender)) {
				arg0.sendMessage(new TextComponent(ChatColor.RED + "[EaglerMOTD] Failed to reload! " + ex.toString()));
			}
			plugin.getLogger().log(Level.SEVERE, "Exception thrown while reloading config!", ex);
		}
	}

}
