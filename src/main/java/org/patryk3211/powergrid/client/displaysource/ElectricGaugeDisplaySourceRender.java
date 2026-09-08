/*
 * Copyright 2025 patryk3211
 * Modified 2026 by chaevsfe for the unofficial Fabric / Create Fly 26.2 port.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.patryk3211.powergrid.client.displaysource;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.client.content.redstone.displayLink.source.SingleLineDisplaySourceRender;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.patryk3211.powergrid.utility.Lang;

@Environment(EnvType.CLIENT)
public class ElectricGaugeDisplaySourceRender extends SingleLineDisplaySourceRender {
    @Override
    public void initConfigurationWidgets(DisplaySource source, DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        super.initConfigurationWidgets(source, context, builder, isFirstLine);
        if(isFirstLine)
            return;

        builder.addSelectionScrollInput(0, 120, (si, l) -> si
                        .forOptions(Lang.translatedOptions("display_source.electric_gauge", "progress_bar", "absolute", "polarized", "custom"))
                        .titled(Lang.translateDirect("display_source.display_information")),
                "Mode");
    }
}
