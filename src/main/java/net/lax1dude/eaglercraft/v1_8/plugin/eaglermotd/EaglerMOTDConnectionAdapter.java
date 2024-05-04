package net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd;

import java.net.InetAddress;
import java.util.List;

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
public interface EaglerMOTDConnectionAdapter {

	boolean isClosed();
	void close();

	String getAccept();
	InetAddress getAddress();
	String getListener();
	long getConnectionTimestamp();
	long getConnectionAge();

	void sendToUser();

	String getLine1();
	String getLine2();
	List<String> getPlayerList();
	int[] getBitmap();
	int getOnlinePlayers();
	int getMaxPlayers();
	String getSubType();

	void setLine1(String p);
	void setLine2(String p);
	void setPlayerList(List<String> p);
	void setPlayerList(String... p);
	void setBitmap(int[] p);
	void setOnlinePlayers(int i);
	void setMaxPlayers(int i);
	void setKeepAlive(boolean b);

	int getDefaultMaxPlayers();
	int getDefaultOnlinePlayers();
	List<String> getDefaultOnlinePlayersList(int maxLen);

}
