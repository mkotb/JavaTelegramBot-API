package pro.zackpollard.telegrambot.api.chat;

import pro.zackpollard.telegrambot.api.user.User;

/**
 * @author Zack Pollard
 */
public interface ChatMember {

    User getUser();

    ChatMemberStatus getStatus();
}
