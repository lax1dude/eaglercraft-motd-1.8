package net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd;

import java.util.List;

import com.google.gson.JsonObject;

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
public class MessagePoolEntry implements Comparable<MessagePoolEntry> {

	public final String name;
	public final int interval;
	public final int timeout;
	public final boolean random;
	public final boolean shuffle;
	public final float weight;
	public final String next;
	public final List<JsonObject> frames;
	
	public MessagePoolEntry(int interval, int timeout, boolean random, boolean shuffle, float weight, String next, List<JsonObject> frames, String name) {
		this.interval = interval;
		this.timeout = timeout;
		this.random = random;
		this.shuffle = shuffle;
		this.weight = weight;
		this.next = next;
		this.frames = frames;
		this.name = name;
	}

	@Override
	public int compareTo(MessagePoolEntry o) {
		return Float.compare(weight, o.weight);
	}
	
}
