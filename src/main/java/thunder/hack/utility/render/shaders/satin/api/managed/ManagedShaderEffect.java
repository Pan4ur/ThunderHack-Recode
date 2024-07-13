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
package thunder.hack.utility.render.shaders.satin.api.managed;

import thunder.hack.utility.render.shaders.satin.api.managed.uniform.SamplerUniformV2;
import thunder.hack.utility.render.shaders.satin.api.managed.uniform.UniformFinder;
import net.minecraft.client.gl.PostEffectProcessor;

public interface ManagedShaderEffect extends UniformFinder {

    PostEffectProcessor getShaderEffect();

    void release();

    void render(float tickDelta);

    ManagedFramebuffer getTarget(String name);

    void setUniformValue(String uniformName, int value);

    void setUniformValue(String uniformName, float value);

    void setUniformValue(String uniformName, float value0, float value1);

    void setUniformValue(String uniformName, float value0, float value1, float value2);

    void setUniformValue(String uniformName, float value0, float value1, float value2, float value3);

    @Override
    SamplerUniformV2 findSampler(String samplerName);
}
