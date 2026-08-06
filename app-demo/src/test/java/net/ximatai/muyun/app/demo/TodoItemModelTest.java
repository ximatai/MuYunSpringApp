package net.ximatai.muyun.app.demo;

import net.ximatai.muyun.database.core.annotation.Column;
import net.ximatai.muyun.database.core.annotation.Table;
import net.ximatai.muyun.database.core.builder.ColumnType;
import net.ximatai.muyun.spring.ability.CacheAbility;
import net.ximatai.muyun.spring.ability.SoftDeleteAbility;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TodoItemModelTest {
    @Test
    void exposesAStableStaticModelContract() throws NoSuchFieldException {
        Table table = TodoItem.class.getAnnotation(Table.class);
        Column completed = TodoItem.class.getDeclaredField("completed").getAnnotation(Column.class);

        assertThat(table.name()).isEqualTo("app_demo_todo_item");
        assertThat(completed.name()).isEqualTo("completed");
        assertThat(completed.type()).isEqualTo(ColumnType.BOOLEAN);
        assertThat(completed.nullable()).isFalse();
    }

    @Test
    void composesStandardPlatformAbilities() {
        assertThat(SoftDeleteAbility.class).isAssignableFrom(TodoItemService.class);
        assertThat(CacheAbility.class).isAssignableFrom(TodoItemService.class);
        assertThat(TodoItemService.MODULE_ALIAS).isEqualTo("demo.todo_item");
    }
}
