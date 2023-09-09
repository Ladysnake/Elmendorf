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
package org.ladysnake.ripstop;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import org.ladysnake.elmendorf.ByteBufChecker;
import org.ladysnake.elmendorf.GameTestUtil;

public class RipstopCcaTestSuite implements FabricGameTest {
    @GameTest(templateName = EMPTY_STRUCTURE)
    public void testComponentSyncChecks(TestContext ctx) {
        var player = ctx.spawnServerPlayer(5, 0, 5);
        var key = RipstopComponents.TEST;
        var entity = ctx.spawnEntity(EntityType.AXOLOTL, 1, 0, 1);
        key.sync(entity);
        GameTestUtil.assertThrows("Expected " + player + " to provide component ripstop:test-component", GameTestException.class, () -> ctx.verifyConnection(player, conn -> conn.sentEntityComponentUpdate(player, key, ByteBufChecker::noMoreData)));
        ctx.verifyConnection(player, conn -> conn.sentEntityComponentUpdate(entity, key, ByteBufChecker::noMoreData));
        ctx.complete();
    }
}
