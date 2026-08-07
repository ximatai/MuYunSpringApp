import { createModuleContext } from '@ximatai/muyun-web-app';

export interface TodoItem { id?: string; title: string; completed: boolean; version?: number }

const moduleAlias = 'demo.todo_item';

export async function queryTodos() {
  return (await todoClient().query({ page: { pageNum: 1, pageSize: 100 } })).records;
}

export async function saveTodo(todo: TodoItem) {
  if (todo.id) {
    await todoClient().update(todo.id, todo);
    return;
  }
  await todoClient().insert(todo);
}

function todoClient() {
  return createModuleContext<TodoItem>({ moduleAlias }).crud;
}
