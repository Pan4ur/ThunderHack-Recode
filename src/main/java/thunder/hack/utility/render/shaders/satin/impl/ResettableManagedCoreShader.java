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

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import thunder.hack.utility.render.shaders.satin.api.managed.ManagedCoreShader;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.SamplerUniform;
import net.fabricmc.fabric.impl.client.rendering.FabricShaderProgram;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class ResettableManagedCoreShader extends ResettableManagedShaderBase<ShaderProgram> implements ManagedCoreShader {
    private final Consumer<ManagedCoreShader> initCallback;
    private final VertexFormat vertexFormat;
    private final Map<String, ManagedSamplerUniformV1> managedSamplers = new HashMap<>();

    public ResettableManagedCoreShader(Identifier location, VertexFormat vertexFormat, Consumer<ManagedCoreShader> initCallback) {
        super(location);
        this.vertexFormat = vertexFormat;
        this.initCallback = initCallback;
    }

    @Override
    protected ShaderProgram parseShader(ResourceFactory resourceManager, MinecraftClient mc, Identifier location) throws IOException {
        return new FabricShaderProgram(resourceManager, this.getLocation(), this.vertexFormat);
    }

    @Override
    public void setup(int newWidth, int newHeight) {
        Preconditions.checkNotNull(this.shader);
        for (ManagedUniformBase uniform : this.getManagedUniforms()) {
            setupUniform(uniform, this.shader);
        }
        this.initCallback.accept(this);
    }

    @Override
    public ShaderProgram getProgram() {
        return this.shader;
    }


    @Override
    protected boolean setupUniform(ManagedUniformBase uniform, ShaderProgram shader) {
        return uniform.findUniformTarget(shader);
    }

    @Override
    public SamplerUniform findSampler(String samplerName) {
        return manageUniform(this.managedSamplers, ManagedSamplerUniformV1::new, samplerName, "sampler");
    }

    @Override
    protected void logInitError(IOException e) {
        LogUtils.getLogger().error("Could not create shader program {}", this.getLocation(), e);
    }
}
