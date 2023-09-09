/*
 * Elmendorf
 * Copyright (C) 2021-2023 Ladysnake
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
package org.ladysnake.elmendorf.impl.mixin;

import net.minecraft.test.GameTestException;
import net.minecraft.test.GameTestState;
import net.minecraft.test.XmlReportingTestCompletionListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Mixin(XmlReportingTestCompletionListener.class)
public abstract class XmlReportingTestCompletionListenerMixin {
    @ModifyVariable(method = "onTestFailed", at = @At(value = "INVOKE", target = "Lorg/w3c/dom/Element;setAttribute(Ljava/lang/String;Ljava/lang/String;)V", shift = At.Shift.AFTER))
    private Element logErrorStacktrace(Element el, GameTestState test) {
        // If not a basic assertion, show the stacktrace too
        Throwable t = test.getThrowable();
        if (t != null && !(t instanceof GameTestException)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintWriter w = new PrintWriter(out);
            t.printStackTrace(w);
            w.flush();
            el.setTextContent(out.toString(StandardCharsets.UTF_8));
        }

        return el;
    }
}
