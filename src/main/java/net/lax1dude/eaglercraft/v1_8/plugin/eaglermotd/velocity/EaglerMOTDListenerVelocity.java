package net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.velocity;

import com.velocitypowered.api.event.Subscribe;

import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.EaglerMOTDConnectionUpdater;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_velocity.api.event.EaglercraftMOTDEvent;

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
public class EaglerMOTDListenerVelocity {

	private final EaglerMOTDPluginVelocity plugin;

	public EaglerMOTDListenerVelocity(EaglerMOTDPluginVelocity plugin) {
		this.plugin = plugin;
	}

	@Subscribe
	public void handleMOTDEvent(EaglercraftMOTDEvent evt) {
		if(!evt.getAccept().equalsIgnoreCase("motd") && !evt.getAccept().equalsIgnoreCase("motd.noicon")) {
			return;
		}
		EaglerMOTDConnectionUpdater con = new EaglerMOTDConnectionUpdater(plugin.conf,
				EaglerMOTDPluginVelocity.getListenerName(evt.getListener()), evt.getListener().getMaxPlayer(),
				new EaglerMOTDConnectionVelocity(evt.getConnection()));
		if(con.execute()) {
			synchronized(plugin.motdConnections) {
				if(plugin.conf.max_total_sockets > 0) {
					while(plugin.motdConnections.size() >= plugin.conf.max_total_sockets) {
						EaglerMOTDConnectionUpdater c = plugin.motdConnections.remove(plugin.motdConnections.size() - 1);
						c.close();
					}
				}
				plugin.motdConnections.add(0, con);
			}
		}
	}

}
