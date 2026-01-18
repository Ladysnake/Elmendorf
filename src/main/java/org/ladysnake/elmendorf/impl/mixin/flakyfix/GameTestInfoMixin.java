/*
 * Elmendorf
 * Copyright (C) 2021-2026 Ladysnake
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ladysnake.elmendorf.impl.mixin.flakyfix;

import net.minecraft.gametest.framework.GameTestInfo;
import org.ladysnake.elmendorf.impl.FixedGameTestState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameTestInfo.class)
public abstract class GameTestInfoMixin implements FixedGameTestState {
    // Keep track of replacement states, for tests that are run multiple times
    private GameTestInfo cs$fallbackGameTest;

    @Override
    public void cs$setReplacementGameTest(GameTestInfo state) {
        this.cs$fallbackGameTest = state;
    }

    @Override
    public GameTestInfo cs$getReplacementGameTest() {
        return this.cs$fallbackGameTest == null ? (GameTestInfo) (Object) this : ((FixedGameTestState) this.cs$fallbackGameTest).cs$getReplacementGameTest();
    }
}
