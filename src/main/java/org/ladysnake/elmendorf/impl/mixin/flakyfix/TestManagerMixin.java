/*
 * Elmendorf
 * Copyright (C) 2021-2024 Ladysnake
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

import net.minecraft.test.GameTestState;
import net.minecraft.test.TestManager;
import org.ladysnake.elmendorf.impl.FixedGameTestState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(TestManager.class)
public abstract class TestManagerMixin {
    @Shadow @Final private Collection<GameTestState> tests;

    // Ensure that when a test is restarted, it keeps track of its successor state
    @Inject(method = "start", at = @At("HEAD"))
    private void linkReplacementTests(GameTestState test, CallbackInfo ci) {
        this.tests.stream().filter(t -> t.getTestFunction() == test.getTestFunction()).findFirst().ifPresent(t -> ((FixedGameTestState) t).cs$setReplacementGameTest(test));
    }
}
