import { existsSync } from 'node:fs';
import { resolve } from 'node:path';
import { execFileSync } from 'node:child_process';

const tarball = process.argv[2];
if (!tarball) {
  throw new Error('用法: npm run install:framework-local -- /path/to/ximatai-muyun-web-app-<version>.tgz');
}
const resolvedTarball = resolve(tarball);
if (!existsSync(resolvedTarball)) {
  throw new Error(`未找到前端平台 tarball: ${resolvedTarball}`);
}
execFileSync('npm', ['install', '--no-save', '--package-lock=false', resolvedTarball], {
  stdio: 'inherit',
});
