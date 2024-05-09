# HD Head Restore

## Download: See [releases](https://github.com/tealreal/hdheadrestore/releases).

## What is this mod?
This mod does exactly what it says. It brings back HD Head / Custom Head / High Resolution Heads (or whatever else you might call it) usage on Minecraft 1.16.5 and above.
This "bug" was patched in newer versions of Minecraft where you could use a scaled up version of a skin template to create textures, like a computer for instance.

![Computer](/images/jcy43gdyiqn61.webp)

It is obviously higher quality, bigger than 16 pixels per side.

### How do you make HD Head textures?

Get a regular Minecraft skin template, and scale it up to a resolution like 4096x4096 and let your creativity run wild. Here is a sample texture of a Macbook I made: https://i.ibb.co/j3vMt5V/silver.png
You can see more examples at...:
* https://r74n.com/mc/heads
* ...

### Where do I upload it?

Originally you would abuse `education.minecraft.net` but they have fixed their site to not allow image uploads. This mod comes in, and you use an external image host like `imgbb.com` because it's not forcing ads.

Take the DIRECT image link from `imgbb.com` (Looks like `ibb.co`), and run the command `/hdheads create <url>` in game.

## Commands

Type `/hdheads` to see options.
Config commands can also be changed using Modmenu / Cloth Config, which is probably easier to use.

### `create <url> [<name>]`
Creates a new head using the specified parameters:
- `<url>` is an image URL. The image should look similar to a Minecraft skin.
- `[<name>]` is the name of the head. Supports color and styling and HEX (&f, &l, &#FFFFFF) through the ampersand character. (rawsay)
- `[<watermark>]` is the "fingerprint" of the head, it must be a unique value, or it may not render.

### `decompile`
When you are holding a head, this will show the key NBT of the head including the URL, UUID, and watermark of the head.

### `config SITE <BLACKLIST/WHITELIST> <ADD/REMOVE/GET> [<sites>]`
Configures the url hosts to blacklist and whitelist with the specified parameters:
- `<BLACKLIST/WHITELIST>` is only relevant for whitelist, as any domains not detected on whitelist will be ignored. blacklist can be used to block a subdomain. Use an asterisk (*) to allow or deny all sites.
- `<ADD/REMOVE/GET>` decides how you modify the list.
- `[<sites>]` are the sites you want to add or remove, to add multiple, separate using a semicolon (;).

EXAMPLES OF A VALID SITE: `.mojang.com`, `.minecraft.net`, `education.minecraft.net`

### `config TOGGLE <CHANGE/GET>`
Toggles the functionality of HD Heads:
- `CHANGE` toggles by switching out the blacklisted and whitelisted URLs, and disables rendering of HD Heads.
- `GET` shows the status of the mod.

### `config MAXFILESIZE <CHANGE/GET> [<mb>]`
Sets the maximum allowed file size for incoming and already downloaded textures. The default is 50 MB.
- `CHANGE` will allow you to set a size in MEGABYTES, not BYTES. Put -1 to allow images of any size.
- `GET` shows the maximum size allowed for rendering HD Heads.

### `config MAXIMGSIZE <CHANGE/GET> [<size>]`
Sets the maximum allowed dimensions for incoming and already downloaded textures. The default is 50000 pixels.
- `CHANGE` will allow you to set a size in PIXELS. Put -1 to allow images of any size. This will be checked against both the image height and width.
- `GET` shows the maximum size allowed for rendering HD Heads.

### `config SCHEME <ADD/REMOVE/GET> [<schemes>]` (1.19+ Only)
***It is not recommended to use this command unless you know what you are doing.***

Configures the allowed url scheme with the specified parameters:
- `<ADD/REMOVE/GET>` decides how you modify the list. Use an asterisk (*) to allow all schemes.
- `[<schemes>]` are the schemes you want to add or remove, to add multiple, separate using a semicolon (;).

EXAMPLES OF A VALID SCHEME: `https`, `http`

### `config MERGE <CHANGE/GET>`
***It is not recommended to use this command unless you know what you are doing.***

Toggles texture merging of HD Heads. This is off by default (vanilla behavior would be on), works by moving the textures by such a small amount that you will only notice that the textures of the head won't merge into the block it's on, also fixing wall heads. In order for this to work, config TOGGLE must be on.
- `CHANGE` toggles the modification that prevents player head textures from merging with the block it is placed on.
- `GET` shows the status of texture merging.

### `config HASH <CHANGE/GET>`
***It is not recommended to use this command unless you know what you are doing.***

Modifies the file naming procedure to allow URLs with the same filename to go to different heads. In order for this to work, config TOGGLE must be on. Toggling between options will download textures you've already downloaded.
- `CHANGE` toggles what string is used for hashing: the whole URL or just the filename.
- `GET` shows the status of how textures are saved.

### `config SCALE <CHANGE/GET> [<x_scale> <y_scale> <z_scale>]`
***It is not recommended to use this command unless you know what you are doing.***

Sets the scale at which player heads are rendered, intended to be a 'fun' command, large values will negatively impact user experience. The default is -1 -1 1, affecting all heads.
- `CHANGE` sets the scale for x, y, and z respectively.
- `GET` shows the current scale factor

## Limitations / Commentary

* Possible security vulnerabilities if others make heads that link to images with malicious code(?)
* Client-side, not visible to everyone. People must have this mod or something similar to this to see your head
* Lag when initially rendering chunks with heads (Usually brief, but is expected when you load large images into memory)
* This mod contains features that limit the size (both height/width and filesize) of the HD head textures that are downloaded and rendered.
* Minecraft stores heads using the filename encoded as a SHA-1, which leads to an issue of downloading heads with different URLs with the same file name, this mod fixes it. However, this will re-download all skins that come from Minecraft's servers.
* Don't make porn heads

## What did Mojang do?

* Around 1.17, they added this line of code:

```java
// Excerpt from com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService:
private static final String[] ALLOWED_DOMAINS = {
        ".minecraft.net",
        ".mojang.com",
        };

private static final String[] BLOCKED_DOMAINS = {
        "bugs.mojang.com",
        "education.minecraft.net",
        "feedback.minecraft.net"
        };
```

And from 1.16.5:
```java
// Excerpt from com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService:
private static final String[] WHITELISTED_DOMAINS = {
        ".minecraft.net",
        ".mojang.com"
        };
```

They patched the bug to not allow users creating heads by using the Mojira, Feedback, or Education sites. 
* On top of this, 1.17 removed the function to see HD Heads (textures already downloaded) because of this line of code:

```java
// Excerpt from net.minecraft.client.texture.PlayerSkinTexture:
        int i = image.getHeight();
        int j = image.getWidth();
        if (j != 64 || i != 32 && i != 64) {
            image.close();
            LOGGER.warn("Discarding incorrectly sized ({}x{}) skin texture from {}", j, i, this.url);
            return null;
        }
        boolean bl2 = bl = i == 32;
```

...which 1.16.5 did not have. This mod fixes it via mixins.

## Texture Merge Fix

Look at the computer keyboards to see how axis fighting works.

### Before
![Before](/images/before.png)

### After
![After](/images/after.png)

## Photo Gallery

![Ariamaru Tomi PV Snapshot https://www.youtube.com/watch?v=BOIHRbnCulQ](/images/ariamarutomipv.png)
![Seggs Dungeon featuring angry red person](/images/weirdsexdungeon.png)
![The setup she tells you not to worry about](/images/setupiwishihad.png)
![The setup you have](/images/thesetupyouhave.png)
![12k res. grotesque steve head by alcoRs11](/images/12k-res-grotesque-steve-head-by-alco-rs11.png)
![kashiwagi yuki middle finger](/images/kashiwagi-yuki-middle-finger-photoshop.png)
