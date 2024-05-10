package net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
public class EaglerMOTDConnectionUpdater {

	public final EaglerMOTDConfiguration conf;
	public final String listenerName;
	public final int defaultMaxPlayers;
	public final EaglerMOTDConnectionAdapter motd;
	
	public MessagePoolEntry currentMessage = null;
	public int messageTimeTimer = 0;
	public int messageIntervalTimer = 0;
	public int currentFrame = 0;
	public int ageTimer = 0;

	public BitmapFile bitmap = null;
	public int spriteX = 0;
	public int spriteY = 0;
	public boolean flipX = false;
	public boolean flipY = false;
	public int rotate = 0;
	public float[] color = new float[] { 0.0f, 0.0f, 0.0f, 0.0f };
	public float[] tint = new float[] { 0.0f, 0.0f, 0.0f, 0.0f };
	
	private Random rand = null;
	
	public EaglerMOTDConnectionUpdater(EaglerMOTDConfiguration conf, String listenerName, int defaultMaxPlayers, EaglerMOTDConnectionAdapter m) {
		this.conf = conf;
		this.motd = m;
		this.listenerName = listenerName;
		this.defaultMaxPlayers = defaultMaxPlayers;
	}
	
	public boolean execute() {
		MessagePool p = conf.messagePools.get(listenerName);
		if(p == null) {
			return false;
		}
		
		messageTimeTimer = 0;
		messageIntervalTimer = 0;
		currentMessage = p.pickDefault();
		if(currentMessage.random || currentMessage.shuffle) {
			rand = new Random();
		}
		
		currentFrame = currentMessage.random ? rand.nextInt(currentMessage.frames.size()) : 0;
		
		applyFrame(currentMessage.frames.get(currentFrame));
		if(currentMessage.interval > 0 || currentMessage.next != null) {
			this.motd.setKeepAlive(true);
			return true;
		}else {
			this.motd.setKeepAlive(false);
			return false;
		}
	}
	
	public boolean tick() {
		ageTimer++;
		if(this.motd.isClosed()) {
			System.out.println("dead");
			return false;
		}
		if(ageTimer > conf.close_socket_after) {
			this.motd.close();
			return false;
		}
		messageTimeTimer++;
		if(messageTimeTimer >= currentMessage.timeout) {
			if(currentMessage.next != null) {
				if(currentMessage.next.equalsIgnoreCase("any") || currentMessage.next.equalsIgnoreCase("random")) {
					MessagePool p = conf.messagePools.get(listenerName);
					if(p == null) {
						this.motd.close();
						return false;
					}
					if(p.messagePool.size() > 1) {
						MessagePoolEntry m;
						do {
							m = p.pickNew();
						}while(m == currentMessage);
						currentMessage = m;
					}
				}else {
					if(!changeMessageTo(listenerName, currentMessage.next)) {
						boolean flag = false;
						for(String s : conf.messages.keySet()) {
							if(!s.equalsIgnoreCase(listenerName) && changeMessageTo(s, currentMessage.next)) {
								flag = true;
								break;
							}
						}
						if(!flag) {
							this.motd.close();
							return false;
						}
					}
				}
				if(currentMessage == null) {
					this.motd.close();
					return false;
				}
				messageTimeTimer = 0;
				messageIntervalTimer = 0;
				if(rand == null && (currentMessage.random || currentMessage.shuffle)) {
					rand = new Random();
				}
				currentFrame = currentMessage.random ? rand.nextInt(currentMessage.frames.size()) : 0;
				applyFrame(currentMessage.frames.get(currentFrame));
				motd.sendToUser();
				if(currentMessage.next == null && currentMessage.interval <= 0) {
					motd.close();
					return false;
				}else {
					return true;
				}
			}else {
				this.motd.close();
				return false;
			}
		}else {
			messageIntervalTimer++;
			if(currentMessage.interval > 0 && messageIntervalTimer >= currentMessage.interval) {
				messageIntervalTimer = 0;
				if(currentMessage.frames.size() > 1) {
					if(currentMessage.shuffle) {
						int i;
						do {
							i = rand.nextInt(currentMessage.frames.size());
						}while(i == currentFrame);
						currentFrame = i;
					}else {
						++currentFrame;
						if(currentFrame >= currentMessage.frames.size()) {
							currentFrame = 0;
						}
					}
					applyFrame(currentMessage.frames.get(currentFrame));
					motd.sendToUser();
				}
			}
			if(currentMessage.next == null && currentMessage.interval <= 0) {
				motd.close();
				return false;
			}else {
				return true;
			}
		}
	}
	
	private boolean changeMessageTo(String group, String s) {
		if(group == null || s == null) {
			return false;
		}
		List<MessagePoolEntry> lst = conf.messages.get(group);
		if(lst == null) {
			return false;
		}
		for(MessagePoolEntry m : lst) {
			if(m.name.equalsIgnoreCase(s)) {
				currentMessage = m;
				return true;
			}
		}
		return false;
	}
	
	public void applyFrame(JsonObject frame) {
		boolean shouldPush = false;
		JsonElement v = frame.get("online");
		if(v != null) {
			if(v.isJsonPrimitive() && ((JsonPrimitive)v).isNumber()) {
				motd.setOnlinePlayers(v.getAsInt());
			}else {
				motd.setOnlinePlayers(motd.getDefaultOnlinePlayers());
			}
			shouldPush = true;
		}
		v = frame.get("max");
		if(v != null) {
			if(v.isJsonPrimitive() && ((JsonPrimitive)v).isNumber()) {
				motd.setMaxPlayers(v.getAsInt());
			}else {
				motd.setMaxPlayers(motd.getDefaultMaxPlayers());
			}
			shouldPush = true;
		}
		v = frame.get("players");
		if(v != null) {
			if(v.isJsonArray()) {
				List<String> players = new ArrayList();
				JsonArray vv = (JsonArray) v;
				for(int i = 0, l = vv.size(); i < l; ++i) {
					players.add(EaglerMOTDConfiguration.translateAlternateColorCodes('&', vv.get(i).getAsString()));
				}
				motd.setPlayerList(players);
			}else {
				motd.setPlayerList(motd.getDefaultOnlinePlayersList(9));
			}
			shouldPush = true;
		}
		String line = optString(frame.get("text0"), optString(frame.get("text"), null));
		if(line != null) {
			int ix = line.indexOf('\n');
			if(ix != -1) {
				motd.setLine1(EaglerMOTDConfiguration.translateAlternateColorCodes('&', line.substring(0, ix)));
				motd.setLine2(EaglerMOTDConfiguration.translateAlternateColorCodes('&', line.substring(ix + 1)));
			}else {
				motd.setLine1(EaglerMOTDConfiguration.translateAlternateColorCodes('&', line));
			}
			line = optString(frame.get("text1"), null);
			if(line != null) {
				motd.setLine2(EaglerMOTDConfiguration.translateAlternateColorCodes('&', line));
			}
			shouldPush = true;
		}
		if(!this.motd.getAccept().equalsIgnoreCase("motd.noicon")) {
			boolean shouldRenderIcon = false;
			JsonElement icon = frame.get("icon");
			if(icon != null) {
				String asString = (icon.isJsonPrimitive() && ((JsonPrimitive)icon).isString()) ? icon.getAsString() : null;
				shouldRenderIcon = true;
				if(icon.isJsonNull() || asString == null || asString.equalsIgnoreCase("none") || asString.equalsIgnoreCase("default")
						|| asString.equalsIgnoreCase("null") || asString.equalsIgnoreCase("color")) {
					bitmap = null;
				}else {
					bitmap = BitmapFile.getCachedIcon(asString);
				}
				spriteX = spriteY = rotate = 0;
				flipX = flipY = false;
				color = new float[] { 0.0f, 0.0f, 0.0f, 0.0f };
				tint = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
			}
			int sprtX = optInt(frame.get("icon_spriteX"), -1) * 64;
			if(sprtX >= 0 && sprtX != spriteX) {
				shouldRenderIcon = true;
				spriteX = sprtX;
			}
			int sprtY = optInt(frame.get("icon_spriteY"), -1) * 64;
			if(sprtY >= 0 && sprtY != spriteY) {
				shouldRenderIcon = true;
				spriteY = sprtY;
			}
			sprtX = optInt(frame.get("icon_pixelX"), -1);
			if(sprtX >= 0 && sprtX != spriteX) {
				shouldRenderIcon = true;
				spriteX = sprtX;
			}
			sprtY = optInt(frame.get("icon_pixelY"), -1);
			if(sprtY >= 0 && sprtY != spriteY) {
				shouldRenderIcon = true;
				spriteY = sprtY;
			}
			JsonElement flip = frame.get("icon_flipX");
			if(flip != null) {
				shouldRenderIcon = true;
				if(flip.isJsonPrimitive() && ((JsonPrimitive)flip).isBoolean()) {
					flipX = flip.getAsBoolean();
				}else {
					flipX = false;
				}
			}
			flip = frame.get("icon_flipY");
			if(flip != null) {
				shouldRenderIcon = true;
				if(flip.isJsonPrimitive() && ((JsonPrimitive)flip).isBoolean()) {
					flipY = flip.getAsBoolean();
				}else {
					flipY = false;
				}
			}
			int rot = optInt(frame.get("icon_rotate"), -1);
			if(rot >= 0) {
				shouldRenderIcon = true;
				rotate = rot % 4;
			}
			JsonArray colorF = optJSONArray(frame.get("icon_color"));
			if(colorF != null && colorF.size() > 0) {
				shouldRenderIcon = true;
				color[0] = colorF.get(0).getAsFloat();
				color[1] = colorF.size() > 1 ? colorF.get(1).getAsFloat() : color[1];
				color[2] = colorF.size() > 2 ? colorF.get(2).getAsFloat() : color[2];
				color[3] = colorF.size() > 3 ? colorF.get(3).getAsFloat() : 1.0f;
			}
			colorF = optJSONArray(frame.get("icon_tint"));
			if(colorF != null && colorF.size() > 0) {
				shouldRenderIcon = true;
				tint[0] = colorF.get(0).getAsFloat();
				tint[1] = colorF.size() > 1 ? colorF.get(1).getAsFloat() : tint[1];
				tint[2] = colorF.size() > 2 ? colorF.get(2).getAsFloat() : tint[2];
				tint[3] = colorF.size() > 3 ? colorF.get(3).getAsFloat() : 1.0f;
			}
			if(shouldRenderIcon) {
				int[] newIcon = null;
				if(bitmap != null) {
					newIcon = bitmap.getSprite(spriteX, spriteY);
				}
				if(newIcon == null) {
					newIcon = new int[64*64];
				}
				newIcon = BitmapFile.applyTint(newIcon, tint[0], tint[1], tint[2], tint[3]);
				if(color[3] > 0.0f) {
					newIcon = BitmapFile.applyColor(newIcon, color[0], color[1], color[2], color[3]);
				}
				if(bitmap != null) {
					if(flipX) {
						newIcon = BitmapFile.flipX(newIcon);
					}
					if(flipY) {
						newIcon = BitmapFile.flipY(newIcon);
					}
					if(rotate != 0) {
						newIcon = BitmapFile.rotate(newIcon, rotate);
					}
				}
				motd.setBitmap(newIcon);
				shouldPush = true;
			}
		}
		if(shouldPush) {
			motd.sendToUser();
		}
	}
	
	public void close() {
		motd.close();
	}

	private static String optString(JsonElement el, String def) {
		return (el != null && el.isJsonPrimitive() && ((JsonPrimitive)el).isString()) ? el.getAsString() : def;
	}

	private static int optInt(JsonElement el, int def) {
		return (el != null && el.isJsonPrimitive() && ((JsonPrimitive)el).isNumber()) ? el.getAsInt() : def;
	}

	private static JsonArray optJSONArray(JsonElement el) {
		return (el != null && el instanceof JsonArray) ? (JsonArray)el : null;
	}
}
