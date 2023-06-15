# FulmiChat
FulmiChat is a simple chat plugin that allows some functionalities with given permissions to the chat.

| Table of Contents |
|-------------------|
| [Config](#Config) |
| [API](#API)       |

## Config
The configuration file is divided into four sections:

| Config Contents                     |
|-------------------------------------|
| [ModerationGUI](#ModerationGUI)     |
| [ItemPlaceholder](#ItemPlaceholder) |
| [PlayerMention](#PlayerMention)     |
| [Emojis](#Emojis)                   |

### ModerationGUI
A GUI to moderate a given player.
Users with permission <b>fulmichat.mod</b> will have access to the custom <i>check</i> in chat and to command `/moderate <player-nane>`.
Here's an example configuration:
```yaml
moderation-gui:
  # The title of the GUI. 
  # %target% will be replaced with the player name
  title: "&4%target% &cGUI"
  # The symbol that should appear next every player
  # message in chat. Can be anything.
  check: "&c[✓]"
  # A list of items to appear in the GUI.
  # The GUI is auto-resizable, meaning that it
  # if you specify more than 10 slots, it will auto-resize
  # to 18, if you specify more than 19, it will
  # auto-resize to 27 and so on...
  items:
    # The slot of the item.
    0:
      # The item type (check https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
      # for a full list).
      material: DIAMOND_AXE
      # The amount of the item.
      amount: 1
      # The display name. Set "" to disable.
      display-name: "&cBan &4%target%"
      lore: []
      # A list containing all the enchantments of
      # the item. Format: <enchant>: <level>
      enchantments:
        mending: 1
        sharpness: 5
        unbreaking: 3
        # NOTE: DON'T FORGET THIS VALUE, IT IS REQUIRED!
        value-class: java.lang.Integer
      # The command to be executed when clicking on the item.
      command: "ban %target% The ban hammer has spoken!"
      # The action to be done when clicking on the item.
      # For now you can only specify "close".
      action: "close"
    1:
      material: DIAMOND_SHOVEL
      amount: 1
      display-name: "&cMute &4%target%"
      lore: []
      enchantments: {}
      command: "mute %target% Muted by %issuer%"
      action: "close"
    8:
      material: BARRIER
      amount: 1
      display-name: "&cClose GUI"
      lore: []
      enchantments: {}
      action: "close"
```

### ItemPlaceholder
A placeholder to be replaced with the item currently hold by the player.
The placeholder can be any of the list value specified in the config.yml.
This can only happen if the player has the permission <b>fulmichat.item</b>.

### PlayerMention
When typing the name of a player in a certain format (specified in <i>player-mention</i>), this will be replaced with the format in <i>player-mention-parsed</i>.
This can only happen if the player has the permission <b>fulmichat.mention</b>.
Also, the specified player will receive a sound (see <i>player-mention-sound</i>), for a full list check https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html.
<br><b>NOTE:</b> in previous versions of Minecraft sound worked differently. If you receive an error after setting the sound, try looking for "Minecraft %version% Spigot Sound" on Google.

### Emojis
FulmiChat provides a custom emoji system to your server.<br>
Every emoji belongs to a group (if none is specified, default will be used), meaning that a player has access to the emojis of a group only if she/he has permission <b>fulmichat.group.%groupname%</b>.
The plugin looks for an emoticon in the player's message and replaces it with one of the available given emojis.
<br>Here's an example configuration:
```yaml
  # The name of the group.
  default:
    # The name of the emoji (irrelevant).
    heart:
      # The emoticons to be parsed. Can be a string or a list.
      emoticon: 
      - '<3'
      - ':heart:'
      # The emoji to be used when parsing. Can be a string or a list.
      emoji: 
      - '&a❤'
      - '&b❤'
      - '&c❤'
      - '&d❤'
      - '&e❤'
    star:
      emoticon: ':star:'
      emoji: '&6✮'
```

<br>Finally, even if it is not advised, the plugin provides a color chat permission: <b>fulmichat.colored-chat</b>.

## API
Since to work, FulmiChat replaces every chat message with a ComponentBuilder, the [AsyncPlayerChatEvent](#https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/player/AsyncPlayerChatEvent.html) will not be available to developers anymore.
Instead, it is given a FulmiChatPlayerEvent to handle this situations. Here's an example:

```java
package it.fulminazzo.testplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import it.fulminazzo.fulmichat.API.FulmiChatPlayerEvent;

public class PlayerListener implements Listener {
    
    @EventHandler
    public void onPlayerChat(FulmiChatPlayerEvent event) {
        event.setChatMessage(new TextComponent("Hello world"));
        event.getRecipients().removeIf(r -> r.getName().equals("Fulminazzo"));
        event.setCancelled(false);
    }
}
```