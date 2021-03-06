package wiresegal.wob.misc.util

import de.btobastian.javacord.entities.User
import de.btobastian.javacord.entities.channels.ServerChannel
import de.btobastian.javacord.entities.channels.TextChannel
import de.btobastian.javacord.entities.message.Message
import de.btobastian.javacord.entities.message.MessageAuthor
import de.btobastian.javacord.entities.permissions.PermissionState
import de.btobastian.javacord.entities.permissions.PermissionType
import wiresegal.wob.permissions

/**
 * @author WireSegal
 * Created at 9:41 PM on 2/15/18.
 */

enum class BotRanks {
    ADMIN,
    MANAGE_MESSAGES,
    USER
}

fun Message.checkPermissions(level: BotRanks) = checkPermissions(null, level)
fun Message.checkPermissions(id: Long?, level: BotRanks) = author.checkPermissions(id, channel, level)


fun MessageAuthor.checkPermissions(channel: TextChannel, level: BotRanks) = checkPermissions(null, channel, level)
fun MessageAuthor.checkPermissions(id: Long?, channel: TextChannel, level: BotRanks) = if (isUser) asUser().get().checkPermissions(id, channel, level) else false

fun User.checkPermissions(channel: TextChannel, level: BotRanks) = checkPermissions(null, channel, level)

fun User.checkPermissions(id: Long?, channel: TextChannel, level: BotRanks): Boolean {

    if (this.isBotOwner) return true
    if (channel !is ServerChannel) return true

    val server = channel.asServerChannel().get()
    val localPerms = server.getEffectivePermissions(this)
    val roles = server.server.getRolesOf(this)
    val channelId = server.server.id

    val managerInfo = permissions[channelId]
    val isManager = managerInfo != null && roles.any { it.id in managerInfo }
    val isAdmin = localPerms.getState(PermissionType.ADMINISTRATOR) == PermissionState.ALLOWED
    val isImplicitManager = localPerms.getState(PermissionType.MANAGE_MESSAGES) == PermissionState.ALLOWED
    val isCreator = id == this.id

    return isAdmin ||
            (level != BotRanks.ADMIN &&
                    (isManager || isImplicitManager ||
                            (level == BotRanks.USER && isCreator)))
}
