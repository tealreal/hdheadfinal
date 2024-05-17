package teal.hdhead.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.text.Text;
import teal.hdhead.HeadClient;

import java.util.Arrays;

public final class ConfigScreen implements ModMenuApi {
    private static final ConfigObject virginConfig = new ConfigObject();

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigObject config = HeadClient.getConfig();
            ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("HD Heads Configuration"));
            ConfigEntryBuilder eb = builder.entryBuilder();

            // USER EXPERIENCE OPTIONS
            ConfigCategory ux = builder.getOrCreateCategory(Text.literal("User Experience"));
            ux.addEntry(eb.startBooleanToggle(Text.literal("Run Mod"), HeadClient.doRunMod())
                .setDefaultValue(true)
                .setTooltip(Text.literal("Whether to run the mod"))
                .setSaveConsumer(HeadClient::setRunMod)
                .build()
            );
            ux.addEntry(eb.startBooleanToggle(Text.literal("Texture Merging"), config.isMerge())
                .setDefaultValue(virginConfig.isMerge())
                .setTooltip(Text.literal("When disabled, prevents the texture of a head from merging with a block."))
                .setSaveConsumer(config::setMerge)
                .build()
            );
            ux.addEntry(eb.startBooleanToggle(Text.literal("Shrink Hats"), config.isShrinkHat())
                .setDefaultValue(virginConfig.isShrinkHat())
                .setTooltip(Text.literal("Shrinks the hat layer to effectively enable double-sided textures."))
                .setSaveConsumer(config::setShrinkHat)
                .build()
            );
            ux.addEntry(eb.startBooleanToggle(Text.literal("New Hashing"), config.isHash())
                .setDefaultValue(virginConfig.isHash())
                .setTooltip(Text.literal("When enabled, allows heads with the same filename but of different textures to coexist."))
                .setSaveConsumer(config::setHash)
                .build()
            );
            ux.addEntry(eb.startLongField(Text.literal("File Size Limit (KB)"), config.getThresholdSizeInKilobytes())
                .setDefaultValue(virginConfig.getThresholdSizeInKilobytes())
                .setTooltip(Text.literal("The limit on how big a head's file size can be before being blocked. (-1 to disable)"))
                .setMin(-1)
                .setSaveConsumer(config::setThresholdSize)
                .build()
            );
            ux.addEntry(eb.startIntField(Text.literal("Image Resolution Limit (Pixels)"), config.getMaxDimension())
                .setDefaultValue(virginConfig.getMaxDimension())
                .setTooltip(Text.literal("The limit on how big a head's texture can be before being blocked. (-1 to disable)"))
                .setMin(-1)
                .setSaveConsumer(config::setMaxDimension)
                .build()
            );


            // SITES OPTIONS
            ConfigCategory sites = builder.getOrCreateCategory(Text.literal("Sites"));
            sites.addEntry(eb.startStrList(Text.literal("Blacklisted Sites"), Arrays.asList(config.getSites(false)))
                .setDefaultValue(Arrays.asList(virginConfig.getSites(false)))
                .setTooltip(Text.literal("Sites to not accept heads from. Format like this: *.site.com"))
                .setSaveConsumer(s -> config.setSites(s.toArray(new String[0]), false))
                .build()
            );
            sites.addEntry(eb.startStrList(Text.literal("Whitelisted Sites"), Arrays.asList(config.getSites(true)))
                .setDefaultValue(Arrays.asList(virginConfig.getSites(true)))
                .setTooltip(Text.literal("Sites to accept heads from. Format like this: *.site.com"))
                .setSaveConsumer(s -> config.setSites(s.toArray(new String[0]), true))
                .build()
            );
            sites.addEntry(eb.startStrList(Text.literal("Allowed Schemes"), Arrays.asList(config.getSchemes()))
                .setDefaultValue(Arrays.asList(virginConfig.getSchemes()))
                .setTooltip(Text.literal("A list of allowed URL schemes. (i.e. https, wss)"))
                .setSaveConsumer(s -> config.setSchemes(s.toArray(new String[0])))
                .build()
            );

            // MISCELLANEOUS OPTIONS
            ConfigCategory fun = builder.getOrCreateCategory(Text.literal("Miscellaneous"));
            // NOT IDIOT-PROOF (List is not fixed size)
            // fun.addEntry(eb.startFloatList(Text.literal("Image "), List.of(config.getScaleInject()))
            //     .setDefaultValue(List.of(virginConfig.getScaleInject()))
            //     .setTooltip(Text.literal("Plays with how heads are rendered. -1 -1 1 is the default."))
            //     .setSaveConsumer(s -> config.setScaleInject(s.toArray(Float[]::new)))
            //     .build()
            // );
            SubCategoryBuilder scaling = eb.startSubCategory(Text.literal("Image Scaling"));
            Float[] scaleArr = config.getScaleInject();
            // THE WETTEST CODE EVER
            scaling.add(eb.startFloatField(Text.literal("X"), config.getScaleInject()[0])
                .setDefaultValue(virginConfig.getScaleInject()[0])
                .setSaveConsumer(s -> {
                    scaleArr[0] = s;
                    config.setScaleInject(scaleArr);
                })
                .build()
            );
            scaling.add(eb.startFloatField(Text.literal("Y"), config.getScaleInject()[1])
                .setDefaultValue(virginConfig.getScaleInject()[1])
                .setSaveConsumer(s -> {
                    scaleArr[1] = s;
                    config.setScaleInject(scaleArr);
                })
                .build()
            );
            scaling.add(eb.startFloatField(Text.literal("Z"), config.getScaleInject()[2])
                .setDefaultValue(virginConfig.getScaleInject()[2])
                .setSaveConsumer(s -> {
                    scaleArr[2] = s;
                    config.setScaleInject(scaleArr);
                })
                .build()
            );
            scaling.setTooltip(Text.literal("Plays with how heads are rendered. -1 -1 1 is the default."));
            fun.addEntry(scaling.build());
            return builder.build();
        };
    }
}