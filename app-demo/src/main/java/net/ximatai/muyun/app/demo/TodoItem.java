package net.ximatai.muyun.app.demo;

import net.ximatai.muyun.database.core.annotation.Column;
import net.ximatai.muyun.database.core.annotation.Default;
import net.ximatai.muyun.database.core.annotation.Table;
import net.ximatai.muyun.database.core.annotation.TrueOrFalse;
import net.ximatai.muyun.database.core.builder.ColumnType;
import net.ximatai.muyun.spring.common.model.standard.StandardTitledEntity;

/** A deliberately small static business model used as the second-development example. */
@Table(name = "app_demo_todo_item", comment = "二开示例待办")
public class TodoItem extends StandardTitledEntity {
    @Column(name = "completed", type = ColumnType.BOOLEAN, nullable = false,
            defaultVal = @Default(bool = TrueOrFalse.FALSE))
    private Boolean completed = Boolean.FALSE;

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
