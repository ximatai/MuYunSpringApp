package net.ximatai.muyun.app.demo.web;

import net.ximatai.muyun.app.demo.TodoApplication;
import net.ximatai.muyun.app.demo.TodoItem;
import net.ximatai.muyun.app.demo.TodoItemService;
import net.ximatai.muyun.spring.platform.module.PlatformStaticModule;
import net.ximatai.muyun.spring.platform.web.CrudWeb;
import net.ximatai.muyun.spring.platform.web.StaticModuleOpenApi;
import net.ximatai.muyun.spring.web.WebSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Standard HTTP projection for the Todo business module. */
@RestController
@PlatformStaticModule(application = TodoApplication.class, alias = TodoItemService.MODULE_ALIAS, title = "待办")
@StaticModuleOpenApi
@RequestMapping("/" + TodoItemService.MODULE_ALIAS)
public class TodoItemWebController extends WebSupport<TodoItemService>
        implements CrudWeb<TodoItem, TodoItemService> {
}
