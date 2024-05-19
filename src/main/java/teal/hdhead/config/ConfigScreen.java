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
                .setTitle(Text.of("HD Heads Configuration"))
                .setSavingRunnable(config::write);
            ConfigEntryBuilder eb = builder.entryBuilder();

            // USER EXPERIENCE OPTIONS
            ConfigCategory ux = builder.getOrCreateCategory(Text.of("User Experience"));
            ux.addEntry(eb.startBooleanToggle(Text.of("Run Mod"), HeadClient.doRunMod())
                .setDefaultValue(true)
                .setTooltip(Text.of("Whether to run the mod"))
                .setSaveConsumer(HeadClient::setRunMod)
                .build()
            );
            ux.addEntry(eb.startBooleanToggle(Text.of("Texture Merging"), config.isMerge())
                .setDefaultValue(virginConfig.isMerge())
                .setTooltip(Text.of("When disabled, prevents the texture of a head from merging with a block."))
                .setSaveConsumer(config::setMerge)
                .build()
            );
            ux.addEntry(eb.startBooleanToggle(Text.of("Shrink Hats"), config.isShrinkHat())
                .setDefaultValue(virginConfig.isShrinkHat())
                .setTooltip(Text.of("Shrinks the hat layer to effectively enable double-sided textures."))
                .setSaveConsumer(config::setShrinkHat)
                .build()
            );
            ux.addEntry(eb.startBooleanToggle(Text.of("New Hashing"), config.isHash())
                .setDefaultValue(virginConfig.isHash())
                .setTooltip(Text.of("When enabled, allows heads with the same filename but of different textures to coexist."))
                .setSaveConsumer(config::setHash)
                .build()
            );
            ux.addEntry(eb.startLongField(Text.of("File Size Limit (KB)"), config.getThresholdSizeInKilobytes())
                .setDefaultValue(virginConfig.getThresholdSizeInKilobytes())
                .setTooltip(Text.of("The limit on how big a head's file size can be before being blocked. (-1 to disable)"))
                .setMin(-1)
                .setSaveConsumer(config::setThresholdSize)
                .build()
            );
            ux.addEntry(eb.startIntField(Text.of("Image Resolution Limit (Pixels)"), config.getMaxDimension())
                .setDefaultValue(virginConfig.getMaxDimension())
                .setTooltip(Text.of("The limit on how big a head's texture can be before being blocked. (-1 to disable)"))
                .setMin(-1)
                .setSaveConsumer(config::setMaxDimension)
                .build()
            );


            // SITES OPTIONS
            ConfigCategory sites = builder.getOrCreateCategory(Text.of("Sites"));
            sites.addEntry(eb.startStrList(Text.of("Blacklisted Sites"), Arrays.asList(config.getSites(false)))
                .setDefaultValue(Arrays.asList(virginConfig.getSites(false)))
                .setTooltip(Text.of("Sites to not accept heads from. Format like this: *.site.com"))
                .setSaveConsumer(s -> config.setSites(s.toArray(new String[0]), false))
                .build()
            );
            sites.addEntry(eb.startStrList(Text.of("Whitelisted Sites"), Arrays.asList(config.getSites(true)))
                .setDefaultValue(Arrays.asList(virginConfig.getSites(true)))
                .setTooltip(Text.of("Sites to accept heads from. Format like this: *.site.com"))
                .setSaveConsumer(s -> config.setSites(s.toArray(new String[0]), true))
                .build()
            );

            // MISCELLANEOUS OPTIONS
            ConfigCategory fun = builder.getOrCreateCategory(Text.of("Miscellaneous"));
            // NOT IDIOT-PROOF (List is not fixed size)
            // fun.addEntry(eb.startFloatList(Text.of("Image "), List.of(config.getScaleInject()))
            //     .setDefaultValue(List.of(virginConfig.getScaleInject()))
            //     .setTooltip(Text.of("Plays with how heads are rendered. -1 -1 1 is the default."))
            //     .setSaveConsumer(s -> config.setScaleInject(s.toArray(Float[]::new)))
            //     .build()
            // );
            SubCategoryBuilder scaling = eb.startSubCategory(Text.of("Image Scaling"));
            Float[] scaleArr = config.getScaleInject();
            // THE WETTEST CODE EVER
            scaling.add(eb.startFloatField(Text.of("X"), config.getScaleInject()[0])
                .setDefaultValue(virginConfig.getScaleInject()[0])
                .setSaveConsumer(s -> {
                    scaleArr[0] = s;
                    config.setScaleInject(scaleArr);
                })
                .build()
            );
            scaling.add(eb.startFloatField(Text.of("Y"), config.getScaleInject()[1])
                .setDefaultValue(virginConfig.getScaleInject()[1])
                .setSaveConsumer(s -> {
                    scaleArr[1] = s;
                    config.setScaleInject(scaleArr);
                })
                .build()
            );
            scaling.add(eb.startFloatField(Text.of("Z"), config.getScaleInject()[2])
                .setDefaultValue(virginConfig.getScaleInject()[2])
                .setSaveConsumer(s -> {
                    scaleArr[2] = s;
                    config.setScaleInject(scaleArr);
                })
                .build()
            );
            scaling.setTooltip(Text.of("Plays with how heads are rendered. -1 -1 1 is the default."));
            fun.addEntry(scaling.build());
            return builder.build();
        };
    }
}