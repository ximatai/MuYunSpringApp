package net.ximatai.muyun.app.demo.web;

import net.ximatai.muyun.app.demo.TodoItemService;
import net.ximatai.muyun.spring.platform.module.PlatformStaticModule;
import net.ximatai.muyun.spring.platform.web.PlatformMenu;
import net.ximatai.muyun.spring.platform.web.StaticModuleOpenApi;
import net.ximatai.muyun.spring.platform.web.StaticModuleUiContributor;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.assertj.core.api.Assertions.assertThat;

class TodoItemWebControllerTest {
    @Test
    void projectsTheBusinessServiceAtItsStableModuleRoute() {
        PlatformStaticModule module = TodoItemWebController.class.getAnnotation(PlatformStaticModule.class);
        RequestMapping mapping = TodoItemWebController.class.getAnnotation(RequestMapping.class);

        assertThat(module.alias()).isEqualTo(TodoItemService.MODULE_ALIAS);
        assertThat(mapping.value()).containsExactly("/demo.todo_item");
        assertThat(TodoItemWebController.class).hasAnnotation(StaticModuleOpenApi.class);
        assertThat(TodoItemWebController.class.getAnnotation(PlatformMenu.class).parent())
                .isEqualTo("app.menu.todo.business");
        assertThat(TodoItemWebController.class).isAssignableTo(StaticModuleUiContributor.class);

        StaticModuleUiContributor contributor = new TodoItemWebController();
        assertThat(contributor.moduleUiDefinition().moduleAlias()).isEqualTo(TodoItemService.MODULE_ALIAS);
        assertThat(contributor.moduleUiDefinition().views())
                .anySatisfy(view -> assertThat(view.fields()).extracting(field -> field.fieldRef().fieldName())
                        .containsExactly("title", "completed"))
                .anySatisfy(view -> assertThat(view.fields()).extracting(field -> field.fieldRef().fieldName())
                        .containsExactly("title", "completed"));
    }
}
