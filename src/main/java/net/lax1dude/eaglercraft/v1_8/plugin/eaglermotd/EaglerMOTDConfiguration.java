package net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Copyright (c) 2022-2024 lax1dude. All Rights Reserved.
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
public class EaglerMOTDConfiguration {

	public final Map<String,List<MessagePoolEntry>> messages = new HashMap();
	public final Map<String,MessagePool> messagePools = new HashMap();
	public final Map<String,JsonObject> framesCache = new HashMap();
	public final Map<String,QueryType> queryTypes = new HashMap();
	public int close_socket_after = 1200;
	public int max_sockets_per_ip = 10;
	public int max_total_sockets = 256;

	public void reload(File pluginDir, EaglerMOTDLoggerAdapter logger, Collection<String> allowedListeners) throws IOException {
		messages.clear();
		messagePools.clear();
		framesCache.clear();
		queryTypes.clear();
		
		BitmapFile.bitmapCache.clear();
		QueryCache.flush();
		
		byte[] damn = new byte[4096];
		int i;
		
		File msgs = new File(pluginDir, "messages.json");
		
		if(!msgs.isFile()) {
			try(OutputStream fileNew = new FileOutputStream(msgs)) {
				try(InputStream fileDefault = EaglerMOTDConfiguration.class.getResourceAsStream("/net/lax1dude/eaglercraft/v1_8/plugin/eaglermotd/default_messages.json")) {
					while((i = fileDefault.read(damn)) != -1) {
						fileNew.write(damn, 0, i);
					}
				}
			}
			File f2 = new File(pluginDir, "frames.json");
			if(!f2.isFile()) {
				try(OutputStream fileNew = new FileOutputStream(f2)) {
					try(InputStream fileDefault = EaglerMOTDConfiguration.class.getResourceAsStream("/net/lax1dude/eaglercraft/v1_8/plugin/eaglermotd/default_frames.json")) {
						while((i = fileDefault.read(damn)) != -1) {
							fileNew.write(damn, 0, i);
						}
					}
				}
			}
			f2 = new File(pluginDir, "queries.json");
			if(!f2.isFile()) {
				try(OutputStream fileNew = new FileOutputStream(f2)) {
					try(InputStream fileDefault = EaglerMOTDConfiguration.class.getResourceAsStream("/net/lax1dude/eaglercraft/v1_8/plugin/eaglermotd/default_queries.json")) {
						while((i = fileDefault.read(damn)) != -1) {
							fileNew.write(damn, 0, i);
						}
					}
				}
			}
			f2 = new File("server-animation.png");
			if(!f2.isFile()) {
				try(OutputStream fileNew = new FileOutputStream(f2)) {
					try(InputStream fileDefault = EaglerMOTDConfiguration.class.getResourceAsStream("/net/lax1dude/eaglercraft/v1_8/plugin/eaglermotd/server-icons-test.png")) {
						while((i = fileDefault.read(damn)) != -1) {
							fileNew.write(damn, 0, i);
						}
					}
				}
			}
		}
		if(!msgs.exists()) {
			throw new NullPointerException("messages.json is missing and could not be created");
		}
		
		ByteArrayOutputStream bao;
		try(InputStream is = new FileInputStream(msgs)) {
			bao = new ByteArrayOutputStream(is.available());
			while((i = is.read(damn)) != -1) {
				bao.write(damn, 0, i);
			}
		}
		
		JsonObject msgsObj = JsonParser.parseString(new String(bao.toByteArray(), StandardCharsets.UTF_8)).getAsJsonObject();
		framesCache.put("messages", msgsObj);
		close_socket_after = optInt(msgsObj.get("close_socket_after"), 1200);
		max_sockets_per_ip = optInt(msgsObj.get("max_sockets_per_ip"), 10);
		max_total_sockets = optInt(msgsObj.get("max_total_sockets"), 256);
		msgsObj = msgsObj.get("messages").getAsJsonObject();
		
		for(String ss : msgsObj.keySet()) {
			try {
				List<MessagePoolEntry> poolEntries = new LinkedList();
				JsonArray arr = msgsObj.get(ss).getAsJsonArray();
				for(int j = 0, l = arr.size(); j < l; ++j) {
					JsonObject entry = arr.get(j).getAsJsonObject();
					List<JsonObject> frames = new LinkedList();
					JsonArray framesJSON = entry.get("frames").getAsJsonArray();
					for(int k = 0, l2 = framesJSON.size(); k < l2; ++k) {
						JsonObject frame = resolveFrame(framesJSON.get(k).getAsString(), pluginDir, logger);
						if(frame != null) {
							frames.add(frame);
						}
					}
					if(frames.size() > 0) {
						poolEntries.add(new MessagePoolEntry(optInt(entry.get("interval"), 0), optInt(entry.get("timeout"), 500), 
								optBoolean(entry.get("random"), false), optBoolean(entry.get("shuffle"), false), optFloat(entry.get("weight"), 1.0f),
								optString(entry.get("next"), null), frames, optString(entry.get("name"), null)));
					}else {
						logger.error("Message '" + ss + "' has no frames!");
					}
				}
				if(poolEntries.size() > 0) {
					List<MessagePoolEntry> existingList = messages.get(ss);
					if(existingList == null) {
						existingList = poolEntries;
						messages.put(ss, existingList);
					}else {
						existingList.addAll(poolEntries);
					}
				}
			}catch(Throwable t) {
				logger.error("Could not parse messages for '" + ss + "' " + t.toString());
			}
		}
		
		String flag = null;
		for(String s : messages.keySet()) {
			if(!s.equals("all")) {
				boolean flag2 = false;
				for(String l : allowedListeners) {
					if(s.equals(l)) {
						flag2 = true;
					}
				}
				if(!flag2) {
					flag = s;
					break;
				}
			}
		}
		
		if(flag != null) {
			logger.error("Listener '" + flag + "' does not exist!");
			String hostsString = "";
			for(String l : allowedListeners) {
				if(hostsString.length() > 0) {
					hostsString += " ";
				}
				hostsString += l;
			}
			logger.error("Listeners configured: " + hostsString);
		}
		
		for(String l : allowedListeners) {
			MessagePool m = new MessagePool(l);
			List<MessagePoolEntry> e = messages.get("all");
			if(e != null) {
				m.messagePool.addAll(e);
			}
			e = messages.get(l);
			if(e != null) {
				m.messagePool.addAll(e);
			}
			if(m.messagePool.size() > 0) {
				logger.info("Loaded " + m.messagePool.size() + " messages for " + l);
				messagePools.put(l, m);
			}
		}
		
		msgs = new File(pluginDir, "queries.json");
		if(msgs.exists()) {
			try {
				try(InputStream is = new FileInputStream(msgs)) {
					bao = new ByteArrayOutputStream(is.available());
					while((i = is.read(damn)) != -1) {
						bao.write(damn, 0, i);
					}
				}
				JsonObject queriesObject = JsonParser.parseString(new String(bao.toByteArray(), StandardCharsets.UTF_8)).getAsJsonObject();
				JsonObject queriesQueriesObject = queriesObject.get("queries").getAsJsonObject();
				for(String s : queriesQueriesObject.keySet()) {
					queryTypes.put(s.toLowerCase(), new QueryType(s, queriesQueriesObject.get(s).getAsJsonObject()));
				}
				if(queryTypes.size() > 0) {
					logger.info("Loaded " + queryTypes.size() + " query types");
				}
			}catch(Throwable t) {
				logger.error("Queries were not loaded: " + t.toString());
			}
		}
		
	}
	
	public JsonObject resolveFrame(String s, File pluginDir, EaglerMOTDLoggerAdapter logger) throws IOException {
		int i = s.indexOf('.');
		if(i == -1) {
			logger.error("Frame '" + s + "' cannot be found! (it does not specify a filename)");
			return null;
		}
		String f = s.substring(0, i);
		JsonObject fc = framesCache.get(f);
		if(fc == null) {
			File ff = new File(pluginDir, f + ".json");
			if(!ff.exists()) {
				logger.error("File '" + f + ".json' cannot be found!");
				return null;
			}
			try {
				ByteArrayOutputStream bao;
				try(InputStream is = new FileInputStream(ff)) {
					bao = new ByteArrayOutputStream(is.available());
					byte[] damn = new byte[4096];
					int j;
					while((j = is.read(damn)) != -1) {
						bao.write(damn, 0, j);
					}
				}
				fc = JsonParser.parseString(new String(bao.toByteArray(), StandardCharsets.UTF_8)).getAsJsonObject();
				framesCache.put(f, fc);
			}catch(Throwable t) {
				logger.error("File '" + f + ".json' could not be loaded: " + t.toString());
				return null;
			}
		}
		f = s.substring(i + 1).trim();
		if(fc.has(f)) {
			return fc.get(f).getAsJsonObject();
		}else {
			logger.error("Frame '" + s + "' cannot be found!");
			return null;
		}
	}

	public static final char COLOR_CHAR = '\u00A7';
	public static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

	public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
		char[] b = textToTranslate.toCharArray();
		for (int i = 0; i < b.length - 1; i++) {
			if (b[i] == altColorChar && ALL_CODES.indexOf(b[i + 1]) > -1) {
				b[i] = EaglerMOTDConfiguration.COLOR_CHAR;
				b[i + 1] = Character.toLowerCase(b[i + 1]);
			}
		}
		return new String(b);
	}

	private static int optInt(JsonElement el, int def) {
		if(el != null && !el.isJsonPrimitive()) {
			JsonPrimitive prim = el.getAsJsonPrimitive();
			return prim.isNumber() ? prim.getAsInt() : def;
		}else {
			return def;
		}
	}

	private static boolean optBoolean(JsonElement el, boolean def) {
		if(el != null && !el.isJsonPrimitive()) {
			JsonPrimitive prim = el.getAsJsonPrimitive();
			return prim.isBoolean() ? prim.getAsBoolean() : def;
		}else {
			return def;
		}
	}

	private static float optFloat(JsonElement el, float def) {
		if(el != null && !el.isJsonPrimitive()) {
			JsonPrimitive prim = el.getAsJsonPrimitive();
			return prim.isNumber() ? prim.getAsFloat() : def;
		}else {
			return def;
		}
	}

	private static String optString(JsonElement el, String def) {
		if(el != null && !el.isJsonPrimitive()) {
			JsonPrimitive prim = el.getAsJsonPrimitive();
			return prim.isString() ? prim.getAsString() : def;
		}else {
			return def;
		}
	}
}
