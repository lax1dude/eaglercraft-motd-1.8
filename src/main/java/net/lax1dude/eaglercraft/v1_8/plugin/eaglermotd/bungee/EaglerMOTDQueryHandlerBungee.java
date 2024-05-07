package net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.bungee;

import java.util.logging.Level;

import com.google.gson.JsonObject;

import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.EaglerMOTDQueryResponseAdapter;
import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.QueryType;
import net.lax1dude.eaglercraft.v1_8.plugin.gateway_bungeecord.api.query.EaglerQuerySimpleHandler;

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
public class EaglerMOTDQueryHandlerBungee extends EaglerQuerySimpleHandler implements EaglerMOTDQueryResponseAdapter {

	@Override
	protected void begin(String queryType) {
		if(this.isClosed()) {
			return;
		}
		try {
			QueryType queryHandler = EaglerMOTDPluginBungee.getPlugin().conf.queryTypes.get(queryType.toLowerCase());
			if(queryHandler != null) {
				queryHandler.doQuery(this);
			}
		}catch(Throwable t) {
			EaglerMOTDPluginBungee.logger().log(Level.SEVERE, "Failed to handle query type \"" + queryType + "\"!", t);
		}finally {
			this.close();
		}
	}

	@Override
	public void stringResponse(String type, String str) {
		this.sendStringResponse(type, str);
	}

	@Override
	public void jsonResponse(String type, JsonObject json) {
		this.sendJsonResponse(type, json);
	}

	@Override
	public void sendBinary(byte[] bin) {
		this.sendBinaryResponse(bin);
	}

}
