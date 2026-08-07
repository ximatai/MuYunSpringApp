import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const webRoot = dirname(dirname(fileURLToPath(import.meta.url)));
const appRoot = dirname(webRoot);
const packageJson = JSON.parse(readFileSync(join(webRoot, 'package.json'), 'utf8'));
const gradleProperties = readFileSync(join(appRoot, 'gradle.properties'), 'utf8');
const gradleVersion = gradleProperties.match(/^muyunSpringVersion=(.+)$/m)?.[1];
const webVersion = packageJson.dependencies?.['@ximatai/muyun-web-app'];

if (!gradleVersion || !webVersion) {
  throw new Error('缺少 muyunSpringVersion 或 @ximatai/muyun-web-app 依赖声明。');
}
if (gradleVersion !== webVersion) {
  throw new Error(`后端平台版本 ${gradleVersion} 与前端平台包版本 ${webVersion} 必须保持一致。`);
}
