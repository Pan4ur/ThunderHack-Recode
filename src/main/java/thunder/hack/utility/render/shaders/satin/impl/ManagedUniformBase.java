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

import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.ShaderProgram;

import java.util.List;

public abstract class ManagedUniformBase {
    protected final String name;

    public ManagedUniformBase(String name) {
        this.name = name;
    }

    public abstract boolean findUniformTargets(List<PostEffectPass> shaders);

    public abstract boolean findUniformTarget(ShaderProgram shader);

    public String getName() {
        return name;
    }
}
