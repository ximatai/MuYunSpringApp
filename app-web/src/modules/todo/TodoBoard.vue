<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { UiButton, UiInput, UiSwitch } from "@ximatai/muyun-web-app";
import { queryTodos, saveTodo, type TodoItem } from "./todoClient";

const title = ref("");
const todos = ref<TodoItem[]>([]);
const busy = ref(false);
const completedCount = computed(
  () => todos.value.filter((todo) => todo.completed).length,
);

onMounted(() => void refresh());

async function refresh() {
  busy.value = true;
  try {
    todos.value = await queryTodos();
  } finally {
    busy.value = false;
  }
}

async function createTodo() {
  const normalizedTitle = title.value.trim();
  if (!normalizedTitle) return;
  busy.value = true;
  try {
    await saveTodo({ title: normalizedTitle, completed: false });
    title.value = "";
    await refresh();
  } finally {
    busy.value = false;
  }
}

async function toggle(todo: TodoItem) {
  busy.value = true;
  try {
    await saveTodo({ ...todo, completed: !todo.completed });
    await refresh();
  } finally {
    busy.value = false;
  }
}
</script>

<template>
  <section class="todo-board">
    <header>
      <p>App 自定义业务入口</p>
      <h2>待办事项</h2>
    </header>
    <form class="create" @submit.prevent="createTodo">
      <UiInput v-model:value="title" placeholder="添加一项待办" />
      <UiButton html-type="submit" type="primary" :loading="busy"
        >新增</UiButton
      >
    </form>
    <p class="summary">已完成 {{ completedCount }} / {{ todos.length }}</p>
    <ul>
      <li v-for="todo in todos" :key="todo.id">
        <label>
          <UiSwitch
            :checked="todo.completed"
            :disabled="busy"
            @change="toggle(todo)"
          />
          <span :class="{ done: todo.completed }">{{ todo.title }}</span>
        </label>
      </li>
    </ul>
  </section>
</template>

<style scoped>
.todo-board {
  width: min(760px, 100%);
  padding: 24px;
  border: 1px solid #dbe4f0;
  border-radius: 10px;
  background: #fff;
}
.todo-board header p {
  margin: 0;
  color: #1677ff;
  font-size: 12px;
  font-weight: 700;
}
.todo-board h2 {
  margin: 8px 0 20px;
}
.todo-board .create {
  display: flex;
  gap: 10px;
}
.todo-board .create :deep(.ant-input) {
  flex: 1;
}
.todo-board .summary {
  color: #64748b;
}
.todo-board ul {
  padding: 0;
  list-style: none;
}
.todo-board li {
  padding: 12px 0;
  border-bottom: 1px solid #edf1f7;
}
.todo-board label {
  display: flex;
  gap: 10px;
  align-items: center;
}
.todo-board .done {
  color: #94a3b8;
  text-decoration: line-through;
}
</style>
