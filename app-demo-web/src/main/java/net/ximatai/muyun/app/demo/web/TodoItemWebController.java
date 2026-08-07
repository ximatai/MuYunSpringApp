package net.ximatai.muyun.app.demo.web;

import net.ximatai.muyun.app.demo.TodoApplication;
import net.ximatai.muyun.app.demo.TodoItem;
import net.ximatai.muyun.app.demo.TodoItemService;
import net.ximatai.muyun.spring.platform.module.PlatformStaticModule;
import net.ximatai.muyun.spring.platform.web.CrudWeb;
import net.ximatai.muyun.spring.platform.web.ModuleUiDefinition;
import net.ximatai.muyun.spring.platform.web.PlatformMenu;
import net.ximatai.muyun.spring.platform.web.StaticModuleOpenApi;
import net.ximatai.muyun.spring.platform.web.StaticModuleUiContributor;
import net.ximatai.muyun.spring.platform.web.StaticRecordReadProjectionService;
import net.ximatai.muyun.spring.web.WebSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Standard HTTP projection for the Todo business module. */
@RestController
@PlatformStaticModule(application = TodoApplication.class, alias = TodoItemService.MODULE_ALIAS, title = "待办")
@PlatformMenu(parent = "app.menu.todo.business", title = "待办管理", order = 20)
@StaticModuleOpenApi
@RequestMapping("/" + TodoItemService.MODULE_ALIAS)
public class TodoItemWebController extends WebSupport<TodoItemService>
        implements CrudWeb<TodoItem, TodoItemService>, StaticModuleUiContributor {
    private StaticRecordReadProjectionService staticRecordReadProjectionService;

    @Autowired(required = false)
    void setStaticRecordReadProjectionService(StaticRecordReadProjectionService staticRecordReadProjectionService) {
        this.staticRecordReadProjectionService = staticRecordReadProjectionService;
    }

    @Override
    public StaticRecordReadProjectionService staticRecordReadProjectionService() {
        return staticRecordReadProjectionService;
    }

    @Override
    public ModuleUiDefinition moduleUiDefinition() {
        return ModuleUiDefinition.builder(TodoItemService.MODULE_ALIAS)
                .listView(list -> list
                        .title("待办列表")
                        .field("title", field -> field.label("待办事项").width("240px"))
                        .field("completed", field -> field.label("完成状态").uiType("switch")
                                .width("100px").align("center")))
                .formView(form -> form
                        .title("待办事项")
                        .field("title", field -> field.label("待办事项").required())
                        .field("completed", field -> field.label("已完成").uiType("switch")))
                .build();
    }
}
