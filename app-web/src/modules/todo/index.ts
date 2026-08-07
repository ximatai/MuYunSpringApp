import TodoBoard from './TodoBoard.vue';
import type { BusinessModuleView } from '../types';

export const todoBusinessModule: BusinessModuleView = {
  route: '/app/todo',
  component: TodoBoard,
};
