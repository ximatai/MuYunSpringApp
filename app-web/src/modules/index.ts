import type { PageDescriptor } from '@ximatai/muyun-web-app';
import { todoBusinessModule } from './todo';
import type { BusinessModuleView } from './types';

const businessModules: BusinessModuleView[] = [todoBusinessModule];

export function resolveBusinessModuleView(descriptor: PageDescriptor | undefined): BusinessModuleView | undefined {
  if (descriptor?.pageType !== 'business-route') return undefined;
  return businessModules.find((module) => module.route === descriptor.target.route);
}
