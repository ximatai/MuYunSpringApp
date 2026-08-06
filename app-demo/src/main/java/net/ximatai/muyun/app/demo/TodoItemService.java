package net.ximatai.muyun.app.demo;

import net.ximatai.muyun.spring.ability.AbstractAbilityService;
import net.ximatai.muyun.spring.ability.CacheAbility;
import net.ximatai.muyun.spring.ability.SoftDeleteAbility;
import org.springframework.stereotype.Service;

/** Business services compose platform abilities instead of reimplementing standard flows. */
@Service
public class TodoItemService extends AbstractAbilityService<TodoItem>
        implements SoftDeleteAbility<TodoItem>, CacheAbility<TodoItem> {
    public static final String MODULE_ALIAS = "demo.todo_item";

    public TodoItemService(TodoItemDao dao) {
        super(MODULE_ALIAS, TodoItem.class, dao);
    }
}
