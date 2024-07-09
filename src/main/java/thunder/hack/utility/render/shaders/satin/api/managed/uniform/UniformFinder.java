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
package thunder.hack.utility.render.shaders.satin.api.managed.uniform;

public interface UniformFinder {
    Uniform1i findUniform1i(String uniformName);

    Uniform2i findUniform2i(String uniformName);

    Uniform3i findUniform3i(String uniformName);

    Uniform4i findUniform4i(String uniformName);

    Uniform1f findUniform1f(String uniformName);

    Uniform2f findUniform2f(String uniformName);

    Uniform3f findUniform3f(String uniformName);

    Uniform4f findUniform4f(String uniformName);

    UniformMat4 findUniformMat4(String uniformName);

    SamplerUniform findSampler(String samplerName);

}
