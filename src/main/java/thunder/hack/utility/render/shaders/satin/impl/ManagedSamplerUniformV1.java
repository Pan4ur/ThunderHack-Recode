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

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.texture.AbstractTexture;

public final class ManagedSamplerUniformV1 extends ManagedSamplerUniformBase {
    public ManagedSamplerUniformV1(String name) {
        super(name);
    }

    @Override
    public void set(AbstractTexture texture) {
        this.set((Object) texture);
    }

    @Override
    public void set(Framebuffer textureFbo) {
        this.set((Object)textureFbo);
    }

    @Override
    public void set(int textureName) {
        this.set((Object)textureName);
    }

    @Override
    protected void set(Object value) {
        SamplerAccess[] targets = this.targets;
        if (targets.length > 0 && this.cachedValue != value) {
            for (SamplerAccess target : targets) {
                ((ShaderProgram) target).addSampler(this.name, value);
            }
            this.cachedValue = value;
        }
    }
}
