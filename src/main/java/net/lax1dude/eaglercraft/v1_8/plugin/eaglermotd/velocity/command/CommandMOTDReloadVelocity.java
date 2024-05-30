package net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.EaglerMOTDLoggerAdapter;
import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.velocity.EaglerMOTDPluginVelocity;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.command.EaglerCommand;

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
public class CommandMOTDReloadVelocity extends EaglerCommand {

	private final EaglerMOTDPluginVelocity plugin;

	public CommandMOTDReloadVelocity(EaglerMOTDPluginVelocity plugin) {
		super("motd-reload", "eaglermotd.command.reload");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSource arg0, String[] arg1) {
		try {
			plugin.removeQueryHandlers();
			plugin.conf.reload(plugin.getDataFolder(), new EaglerMOTDLoggerAdapter() {
				@Override
				public void info(String msg) {
					CommandMOTDReloadVelocity.this.plugin.getLogger().info(msg);
					if(!(arg0 instanceof ConsoleCommandSource)) {
						arg0.sendMessage(Component.text("[EaglerMOTD] " + msg, NamedTextColor.GREEN));
					}
				}

				@Override
				public void warn(String msg) {
					CommandMOTDReloadVelocity.this.plugin.getLogger().warn(msg);
					if(!(arg0 instanceof ConsoleCommandSource)) {
						arg0.sendMessage(Component.text("[EaglerMOTD] " + msg, NamedTextColor.YELLOW));
					}
				}

				@Override
				public void error(String msg) {
					CommandMOTDReloadVelocity.this.plugin.getLogger().error(msg);
					if(!(arg0 instanceof ConsoleCommandSource)) {
						arg0.sendMessage(Component.text("[EaglerMOTD] " + msg, NamedTextColor.RED));
					}
				}
			}, plugin.getListenerNames());
			plugin.installQueryHandlers();
		}catch(Throwable ex) {
			if(!(arg0 instanceof ConsoleCommandSource)) {
				arg0.sendMessage(Component.text("[EaglerMOTD] Failed to reload! " + ex.toString(), NamedTextColor.RED));
			}
			plugin.getLogger().error("Exception thrown while reloading config!", ex);
		}
	}

}
