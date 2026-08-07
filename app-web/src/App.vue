<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import {
  Workbench,
  PlatformAdminOutlet,
  UiButton,
  UiInput,
  configureModuleContext,
  createHttpClient,
  createMenuClient,
  createMenuTab,
  createSessionClient,
  getMenuNavigationTarget,
  platformAdminRouteLayouts,
  platformAdminRoutePrefixes,
  provideWorkbenchNavigation,
  tabKeyOf,
} from "@ximatai/muyun-web-app";
import type {
  MenuNavigationTarget,
  MenuRecord,
  MenuTab,
  MenuTreeNode,
  PageDescriptor,
  WorkbenchStartupState,
} from "@ximatai/muyun-web-app";
import { resolveBusinessModuleView } from "./modules";
import {
  authErrorMessage,
  changeOwnPassword,
  clearAuthSession,
  isAuthenticationFailure,
  isPasswordChangeRequired,
  restoreAuthSession,
  signIn as createAuthSession,
  signOut,
  type AuthSession,
} from "./app/auth/authSession";

const username = ref("admin");
const password = ref("");
const session = ref<AuthSession>();
const startup = ref<WorkbenchStartupState>();
const activeTabKey = ref<string>();
const busy = ref(false);
const error = ref("");
const passwordChangeRequired = ref(false);
const currentPassword = ref("");
const newPassword = ref("");

const activeDescriptor = computed<PageDescriptor | undefined>(
  () =>
    startup.value?.tabs?.find((tab) => tab.key === activeTabKey.value)
      ?.pageDescriptor,
);

provideWorkbenchNavigation({
  openPage: openPlatformPage,
  replacePage: replacePlatformPage,
});

onMounted(() => {
  session.value = restoreAuthSession();
  if (session.value) void loadWorkbench();
});

async function signIn() {
  await run(async () => {
    session.value = await createAuthSession(username.value, password.value);
    passwordChangeRequired.value = session.value.passwordChangeRequired;
    if (passwordChangeRequired.value) return;
    await loadWorkbench();
  });
}

async function loadWorkbench() {
  const token = session.value?.token;
  if (!token) return;
  try {
    const http = createHttpClient({ token });
    configureModuleContext({
      httpFactory: () => createHttpClient({ token: session.value?.token }),
    });
    const [currentUser, menuResponse] = await Promise.all([
      createSessionClient(http).current(),
      createMenuClient(http).mine(),
    ]);
    const firstMenu = firstNavigableMenu(menuResponse.records);
    const firstTarget = firstMenu
      ? getMenuNavigationTarget(firstMenu)
      : undefined;
    const firstTab =
      firstMenu && firstTarget
        ? createMenuTab(firstMenu, firstTarget, resolveOptions)
        : undefined;
    startup.value = {
      session: { currentUser },
      menus: menuResponse.records,
      tabs: firstTab ? [firstTab] : [],
      activeTabKey: firstTab?.key,
    };
    activeTabKey.value = firstTab?.key;
  } catch (cause) {
    if (isPasswordChangeRequired(cause)) {
      passwordChangeRequired.value = true;
      return;
    }
    if (isAuthenticationFailure(cause)) clearLocalSession();
    throw cause;
  }
}

function selectMenu(menu: MenuRecord, target: MenuNavigationTarget) {
  if (target.openMode === "window") {
    const url =
      target.menuType === "link"
        ? target.externalUrl
        : target.menuType === "route"
          ? target.route
          : "/";
    window.open(url, "_blank", "noopener");
    return;
  }
  const tab = createMenuTab(menu, target, resolveOptions) as MenuTab;
  const tabs = startup.value?.tabs ?? [];
  const nextTabs = tabs.some((item) => item.key === tab.key)
    ? tabs
    : [...tabs, tab];
  startup.value = { ...startup.value!, tabs: nextTabs, activeTabKey: tab.key };
  activeTabKey.value = tab.key;
}

function openPlatformPage(descriptor: PageDescriptor) {
  const current = startup.value;
  if (!current) return { created: false };
  const key = tabKeyOf(descriptor);
  const tabs = current.tabs ?? [];
  const existing = tabs.find((tab) => tab.key === key);
  const nextTabs = existing
    ? tabs
    : [
        ...tabs,
        {
          key,
          title: descriptor.title ?? "平台页面",
          pageDescriptor: descriptor,
          closable: true,
        },
      ];
  startup.value = { ...current, tabs: nextTabs, activeTabKey: key };
  activeTabKey.value = key;
  return { created: !existing };
}

function replacePlatformPage(pageKey: string, descriptor: PageDescriptor) {
  const current = startup.value;
  if (!current) return;
  startup.value = {
    ...current,
    tabs: (current.tabs ?? []).map((tab) =>
      tab.key === pageKey
        ? {
            ...tab,
            title: descriptor.title ?? tab.title,
            pageDescriptor: descriptor,
          }
        : tab,
    ),
  };
}

function closeTab(key: string) {
  if (!startup.value) return;
  const tabs = startup.value.tabs?.filter((tab) => tab.key !== key) ?? [];
  const nextActive =
    activeTabKey.value === key ? tabs.at(-1)?.key : activeTabKey.value;
  startup.value = { ...startup.value, tabs, activeTabKey: nextActive };
  activeTabKey.value = nextActive;
}

function clearLocalSession() {
  clearAuthSession();
  session.value = undefined;
  startup.value = undefined;
  activeTabKey.value = undefined;
  passwordChangeRequired.value = false;
  currentPassword.value = "";
  newPassword.value = "";
}

async function logout() {
  const current = session.value;
  clearLocalSession();
  try {
    await signOut(current);
  } catch {
    // Local session has already been cleared; the next request cannot reuse it.
  }
}

async function completePasswordChange() {
  const current = session.value;
  if (!current) return;
  await run(async () => {
    await changeOwnPassword(current, currentPassword.value, newPassword.value);
    clearLocalSession();
  });
}

async function run(action: () => Promise<void>) {
  busy.value = true;
  error.value = "";
  try {
    await action();
  } catch (cause) {
    error.value = authErrorMessage(cause);
  } finally {
    busy.value = false;
  }
}

function firstNavigableMenu(nodes: MenuTreeNode[]): MenuRecord | undefined {
  for (const node of nodes) {
    if (getMenuNavigationTarget(node.record)?.openMode === "tab")
      return node.record;
    const child = firstNavigableMenu(node.children);
    if (child) return child;
  }
}

const resolveOptions = {
  businessRoutePrefixes: ["/app", ...platformAdminRoutePrefixes],
  businessRouteLayouts: platformAdminRouteLayouts,
};
const activeBusinessModule = computed(() =>
  resolveBusinessModuleView(activeDescriptor.value),
);
</script>

<template>
  <main v-if="!session || passwordChangeRequired" class="login-page">
    <form
      v-if="passwordChangeRequired"
      class="login-panel"
      @submit.prevent="completePasswordChange"
    >
      <p>MuYunSpringApp</p>
      <h1>设置新密码</h1>
      <UiInput
        v-model:value="currentPassword"
        type="password"
        autocomplete="current-password"
        placeholder="当前密码"
      />
      <UiInput
        v-model:value="newPassword"
        type="password"
        autocomplete="new-password"
        placeholder="新密码"
      />
      <UiButton html-type="submit" type="primary" :loading="busy"
        >确认修改</UiButton
      >
      <span v-if="error" class="error">{{ error }}</span>
    </form>
    <form v-else class="login-panel" @submit.prevent="signIn">
      <p>MuYunSpringApp</p>
      <h1>应用工作台</h1>
      <UiInput
        v-model:value="username"
        autocomplete="username"
        placeholder="用户名"
      />
      <UiInput
        v-model:value="password"
        type="password"
        autocomplete="current-password"
        placeholder="密码"
      />
      <UiButton html-type="submit" type="primary" :loading="busy"
        >登录</UiButton
      >
      <small>首次登录请先按平台要求修改初始密码。</small>
      <span v-if="error" class="error">{{ error }}</span>
    </form>
  </main>
  <Workbench
    v-else
    :startup="startup"
    :loading="busy"
    :error="error || undefined"
    :active-tab-key="activeTabKey"
    @select-menu="selectMenu"
    @change-tab="activeTabKey = $event"
    @close-tab="closeTab"
    @user-command="$event === 'logout' ? void logout() : undefined"
  >
    <template #default="{ pageDescriptor }">
      <component
        v-if="activeBusinessModule"
        :is="activeBusinessModule.component"
      />
      <PlatformAdminOutlet v-else :descriptor="pageDescriptor" />
    </template>
  </Workbench>
</template>
