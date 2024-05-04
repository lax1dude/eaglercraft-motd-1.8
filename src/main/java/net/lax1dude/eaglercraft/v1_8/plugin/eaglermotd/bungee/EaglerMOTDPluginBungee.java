package net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.bungee;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.EaglerMOTDConfiguration;
import net.lax1dude.eaglercraft.v1_8.plugin.eaglermotd.EaglerMOTDConnectionUpdater;
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
	public final Timer tickTimer = new Timer("MOTD Tick Timer");
	public final List<EaglerMOTDConnectionUpdater> motdConnections = new LinkedList();

}
