/*
 * Satin
 * Copyright (C) 2019-2024 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package thunder.hack.utility.render.shaders.satin.impl;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import thunder.hack.utility.render.shaders.satin.api.managed.ManagedCoreShader;
import thunder.hack.utility.render.shaders.satin.api.managed.ManagedShaderEffect;
import thunder.hack.utility.render.shaders.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import thunder.hack.features.cmd.Command;
import thunder.hack.utility.render.WindowResizeCallback;

import java.util.Set;
import java.util.function.Consumer;

public final class ReloadableShaderEffectManager implements ShaderEffectManager {
    public static final ReloadableShaderEffectManager INSTANCE = new ReloadableShaderEffectManager();

    public ReloadableShaderEffectManager() {
        WindowResizeCallback.EVENT.register((client, window) -> {
            onResolutionChanged(window.getFramebufferWidth(), window.getFramebufferHeight());
        });
    }

    private final Set<ResettableManagedShaderBase<?>> managedShaders = new ReferenceOpenHashSet<>();

    @Override
    public ManagedShaderEffect manage(Identifier location) {
        return manage(location, s -> {
        });
    }

    @Override
    public ManagedShaderEffect manage(Identifier location, Consumer<ManagedShaderEffect> initCallback) {
        ResettableManagedShaderEffect ret = new ResettableManagedShaderEffect(location, initCallback);
        managedShaders.add(ret);
        return ret;
    }

    @Override
    public ManagedCoreShader manageCoreShader(Identifier location) {
        return manageCoreShader(location, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
    }

    @Override
    public ManagedCoreShader manageCoreShader(Identifier location, VertexFormat vertexFormat) {
        return manageCoreShader(location, vertexFormat, (s) -> {
        });
    }

    @Override
    public ManagedCoreShader manageCoreShader(Identifier location, VertexFormat vertexFormat, Consumer<ManagedCoreShader> initCallback) {
        ResettableManagedCoreShader ret = new ResettableManagedCoreShader(location, vertexFormat, initCallback);
        managedShaders.add(ret);
        return ret;
    }

    public void reload(ResourceFactory shaderResources) {
        for (ResettableManagedShaderBase<?> ss : managedShaders) {
            ss.initializeOrLog(shaderResources);
        }
    }

    public void onResolutionChanged(int newWidth, int newHeight) {
        runShaderSetup(newWidth, newHeight);
    }

    private void runShaderSetup(int newWidth, int newHeight) {
        if (!managedShaders.isEmpty()) {
            for (ResettableManagedShaderBase<?> ss : managedShaders) {
                if (ss.isInitialized()) {
                    ss.setup(newWidth, newHeight);
                }
            }
        }
    }
}
