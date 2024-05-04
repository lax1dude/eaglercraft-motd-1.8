package net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.bungee;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.EaglerMOTDConnectionAdapter;
import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.EaglerMOTDUtils;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_bungeecord.api.query.MOTDConnection;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_bungeecord.config.EaglerListenerConfig;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_bungeecord.server.query.MOTDQueryHandler;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

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
public class EaglerMOTDConnectionBungee  implements EaglerMOTDConnectionAdapter {

	private final MOTDConnection con;
	private String listenerString = null;

	public EaglerMOTDConnectionBungee(MOTDConnection con) {
		this.con = con;
	}

	@Override
	public boolean isClosed() {
		return con.isClosed();
	}

	@Override
	public void close() {
		con.close();
	}

	@Override
	public String getAccept() {
		return con.getAccept();
	}

	@Override
	public InetAddress getAddress() {
		return con.getAddress();
	}

	@Override
	public String getListener() {
		if(listenerString == null) {
			EaglerListenerConfig config = con.getListener();
			if(config.getAddress() != null) {
				this.listenerString = EaglerMOTDUtils.makeListenerString(config.getAddress());
			}else if(config.getAddressV6() != null) {
				this.listenerString = EaglerMOTDUtils.makeListenerString(config.getAddressV6());
			}else {
				throw new RuntimeException("Listener does not have an address!?");
			}
		}
		return listenerString;
	}

	@Override
	public long getConnectionTimestamp() {
		return con.getConnectionTimestamp();
	}

	@Override
	public long getConnectionAge() {
		return con.getConnectionAge();
	}

	@Override
	public void sendToUser() {
		con.sendToUser();
	}

	@Override
	public String getLine1() {
		return con.getLine1();
	}

	@Override
	public String getLine2() {
		return con.getLine2();
	}

	@Override
	public List<String> getPlayerList() {
		return con.getPlayerList();
	}

	@Override
	public int[] getBitmap() {
		return con.getBitmap();
	}

	@Override
	public int getOnlinePlayers() {
		return con.getOnlinePlayers();
	}

	@Override
	public int getMaxPlayers() {
		return con.getMaxPlayers();
	}

	@Override
	public String getSubType() {
		return con.getSubType();
	}

	@Override
	public void setLine1(String p) {
		con.setLine1(p);
	}

	@Override
	public void setLine2(String p) {
		con.setLine2(p);
	}

	@Override
	public void setPlayerList(List<String> p) {
		con.setPlayerList(p);
	}

	@Override
	public void setPlayerList(String... p) {
		con.setPlayerList(p);
	}

	@Override
	public void setBitmap(int[] p) {
		con.setBitmap(p);
	}

	@Override
	public void setOnlinePlayers(int i) {
		con.setOnlinePlayers(i);
	}

	@Override
	public void setMaxPlayers(int i) {
		con.setMaxPlayers(i);
	}

	@Override
	public void setKeepAlive(boolean b) {
		// workaround for pre-1.2.0 EaglerXBungee
		((MOTDQueryHandler)con).setKeepAlive(b);
	}

	@Override
	public int getDefaultMaxPlayers() {
		return con.getListener().getMaxPlayer();
	}

	@Override
	public int getDefaultOnlinePlayers() {
		return 0;
	}

	@Override
	public List<String> getDefaultOnlinePlayersList(int maxLen) {
		Collection<ProxiedPlayer> ppl = BungeeCord.getInstance().getPlayers();
		List<String> players = new ArrayList(Math.min(ppl.size(), maxLen + 1));
		for(ProxiedPlayer pp : ppl) {
			players.add(pp.getDisplayName());
			if(players.size() >= maxLen) {
				players.add("" + ChatColor.GRAY + ChatColor.ITALIC + "(" + (ppl.size() - players.size()) + " more)");
				break;
			}
		}
		return players;
	}

}
