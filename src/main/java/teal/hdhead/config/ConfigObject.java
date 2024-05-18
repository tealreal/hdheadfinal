package teal.hdhead.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.IOUtils;
import org.spongepowered.include.com.google.common.base.Charsets;
import teal.hdhead.HeadClient;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public final class ConfigObject {
    private String[] blacklistSites;
    private String[] whitelistSites;
    private String[] schemes;

    private Float[] scaleInject;
    private long thresholdSize;

    private int maxDimension;
    private boolean merge;
    private boolean hash;
    private boolean shrinkHat;

    public static final Gson gson = new GsonBuilder().create();

    public void setSchemes(String[] schemes) {
        this.schemes = schemes;
    }

    public String[] getSchemes() {
        return schemes;
    }

    public void setSites(String[] sites, boolean whitelisted) {
        if (whitelisted) this.whitelistSites = sites;
        else this.blacklistSites = sites;
    }

    public String[] getSites(boolean whitelisted) {
        return whitelisted ? whitelistSites : blacklistSites;
    }

    public Float[] getScaleInject() {
        return scaleInject;
    }

    public void setScaleInject(Float[] i) {
        scaleInject = i;
    }

    public void setMerge(boolean merge) {
        this.merge = merge;
    }

    public boolean isMerge() {
        return merge;
    }

    public void setHash(boolean hash) {
        this.hash = hash;
    }

    public boolean isHash() {
        return hash;
    }

    public void setShrinkHat(boolean shrinkHat) {
        this.shrinkHat = shrinkHat;
    }

    public boolean isShrinkHat() {
        return shrinkHat;
    }

    public void setThresholdSize(long newTS) {
        if (newTS < 0) newTS = -1;
        thresholdSize = newTS;
    }

    public long getThresholdSizeInKilobytes() {
        return thresholdSize;
    }

    public void setMaxDimension(int newDimension) {
        if (newDimension < 0) newDimension = -1;
        this.maxDimension = newDimension;
    }

    public int getMaxDimension() {
        return maxDimension;
    }

    public ConfigObject() {
        // URL schemes
        schemes = new String[]{"http", "https"};
        // blacklisted
        blacklistSites = new String[]{};
        // whitelisted
        whitelistSites = new String[]{"*"};

        // scaleInject
        scaleInject = new Float[]{-1.0F, -1.0F, 1.0F};
        // merge
        merge = false;
        // hash
        hash = true;

        // max size (50 MB in bytes) saved in kilobytes
        thresholdSize = 50000;
        // max image width & height (50k max)
        maxDimension = 50000;
        // shrinks the hat layer of a head to allow for double-sided textures without gaps*.
        shrinkHat = false;
    }

    public void write() {
        try {
            Writer writer = new FileWriter(".hdheads.dat");
            gson.toJson(this, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            HeadClient.logger.error(e.toString());
        }
    }

    public static ConfigObject getConfigObject() {
        ConfigObject obj = new ConfigObject();
        try {
            try {
                FileInputStream FIS = new FileInputStream(".hdheads.dat");
                obj = gson.fromJson(IOUtils.toString(FIS, Charsets.UTF_8), ConfigObject.class);
                if (obj == null) {
                    obj = new ConfigObject();
                    throw new JsonSyntaxException("");
                }
            } catch (JsonSyntaxException e) {
                Writer writer = new FileWriter(".hdheads.dat");
                gson.toJson(new ConfigObject(), writer);
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            HeadClient.logger.error(e.toString());
        }
        return obj;
    }
}