package net.ximatai.muyun.app.configuration;

import net.ximatai.muyun.spring.platform.initialdata.InitialDataDeclaration;
import net.ximatai.muyun.spring.platform.initialdata.InitialDataDeclarationProvider;
import net.ximatai.muyun.spring.platform.menu.Menu;
import net.ximatai.muyun.spring.platform.menu.MenuOpenMode;
import net.ximatai.muyun.spring.platform.menu.MenuSchemeService;
import net.ximatai.muyun.spring.platform.menu.MenuService;
import net.ximatai.muyun.spring.ability.TreeAbility;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** App-owned menu composition; both entries retain the Todo module permission context. */
@Configuration(proxyBeanMethods = false)
public class TodoMenuConfiguration {
    @Bean
    InitialDataDeclarationProvider todoMenuDeclarations(MenuService menuService) {
        return new InitialDataDeclarationProvider() {
            @Override
            public String name() {
                return "app.todo-menus";
            }

            @Override
            public int order() {
                return 11;
            }

            @Override
            public List<InitialDataDeclaration<?>> declarations() {
                return List.of(
                        InitialDataDeclaration.reconcileManaged(menuService, rootMenu()),
                        InitialDataDeclaration.reconcileManaged(menuService, todoGroupMenu()),
                        InitialDataDeclaration.reconcileManaged(menuService, todoRouteMenu())
                );
            }
        };
    }

    private Menu rootMenu() {
        Menu menu = new Menu();
        menu.setId("app.menu.todo");
        menu.setSchemeId(MenuSchemeService.ADMIN_SCHEME_ID);
        menu.setParentId(TreeAbility.ROOT_ID);
        menu.setTitle("Todo 示例");
        menu.setEnabled(true);
        menu.setSortOrder(100);
        return menu;
    }

    private Menu todoRouteMenu() {
        Menu menu = new Menu();
        menu.setId("app.menu.todo.board");
        menu.setSchemeId(MenuSchemeService.ADMIN_SCHEME_ID);
        menu.setParentId("app.menu.todo.business");
        menu.setTitle("待办事项");
        menu.setModuleAlias("demo.todo_item");
        menu.setRoute("/app/todo");
        menu.setOpenMode(MenuOpenMode.TAB);
        menu.setEnabled(true);
        menu.setSortOrder(10);
        return menu;
    }

    private Menu todoGroupMenu() {
        Menu menu = new Menu();
        menu.setId("app.menu.todo.business");
        menu.setSchemeId(MenuSchemeService.ADMIN_SCHEME_ID);
        menu.setParentId("app.menu.todo");
        menu.setTitle("待办业务");
        menu.setEnabled(true);
        menu.setSortOrder(10);
        return menu;
    }
}
