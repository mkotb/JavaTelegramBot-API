package pro.zackpollard.telegrambot.api.menu.internal;

import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.CallbackQuery;
import pro.zackpollard.telegrambot.api.event.Event;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.CallbackQueryReceivedEvent;
import pro.zackpollard.telegrambot.api.extensions.Extension;
import pro.zackpollard.telegrambot.api.menu.InlineMenu;
import pro.zackpollard.telegrambot.api.menu.InlineMenuRegistry;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

public class InlineMenuRegistryImpl implements InlineMenuRegistry {
    public static class Provider implements Extension.Provider<InlineMenuRegistry> {
        @Override
        public InlineMenuRegistry create(TelegramBot bot) {
            return new InlineMenuRegistryImpl(bot);
        }
    }

    private final AtomicInteger nextId = new AtomicInteger(0);
    private final Map<Integer, InlineMenu> menus = new ConcurrentHashMap<>();

    private InlineMenuRegistryImpl(TelegramBot bot) {
        bot.getEventsManager().register(new Listener() {
            @Event.Handler(ignoreCancelled = true, priority = Event.Priority.LOWEST)
            @Override
            public void onCallbackQueryReceivedEvent(CallbackQueryReceivedEvent event) {
                if (process(event.getCallbackQuery())) {
                    event.setCancelled(true);
                }
            }
        });
    }

    @Override
    public void register(InlineMenu menu) {
        int next = nextId.getAndIncrement();

        menus.put(next, menu);
        menu.setInternalId(next);
    }

    @Override
    public void unregister(InlineMenu menu) {
        menus.remove(menu.getInternalId());
    }

    private boolean process(CallbackQuery query) {
        String data = query.getData();
        Matcher matcher = DATA_PATTERN.matcher(data);

        if (!matcher.find()) {
            return false;
        }

        InlineMenu menu = menus.get(Integer.parseInt(matcher.group(1)));

        return menu != null &&
                menu.handle(query, Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(3)));
    }
}
