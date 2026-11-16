// Extracts every UI event source from the legacy ZK app (SIBOL-EVES_DB).
// Sources: @Listen annotations, addEventListener calls, zul on*="..." attributes, zul href="..." links.
// Commented-out lines are ignored.
import fs from 'node:fs';
import path from 'node:path';

export const LEGACY_ROOT = path.resolve(
  process.env.LEGACY_ROOT ?? path.join(import.meta.dirname, '..', '..', 'SIBOL-EVES_DB', 'src', 'main'));

function walk(dir, ext, out = []) {
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walk(p, ext, out);
    else if (p.endsWith(ext)) out.push(p);
  }
  return out;
}

/** Strip // line comments and block comments while keeping line numbers. */
function stripJavaComments(src) {
  return src
    .replace(/\/\*[\s\S]*?\*\//g, (m) => m.replace(/[^\n]/g, ' '))
    .split(/\r?\n/)
    .map((l) => l.replace(/(^|[^:"])\/\/.*$/, '$1'));
}

function stripXmlComments(src) {
  return src.replace(/<!--[\s\S]*?-->/g, (m) => m.replace(/[^\n]/g, ' ')).split(/\r?\n/);
}

/** Every .zul file (relative to webapp). */
export function listZulFiles() {
  const webRoot = path.join(LEGACY_ROOT, 'webapp');
  return walk(webRoot, '.zul').map((f) => path.relative(webRoot, f).replaceAll(path.sep, '/')).sort();
}

export function extractLegacyEvents() {
  const events = [];
  const javaRoot = path.join(LEGACY_ROOT, 'java');
  const javaFiles = walk(javaRoot, '.java');
  const baseCount = new Map();
  for (const f of javaFiles) baseCount.set(path.basename(f), (baseCount.get(path.basename(f)) ?? 0) + 1);
  for (const f of javaFiles) {
    const rel = path.relative(javaRoot, f).replaceAll(path.sep, '/');
    // key by class file name; by package path when two classes share a name
    const id = baseCount.get(path.basename(f)) > 1 ? rel : path.basename(f);
    const lines = stripJavaComments(fs.readFileSync(f, 'utf8'));
    lines.forEach((l, i) => {
      let m = l.match(/@Listen\(\s*"([^"]*)"/);
      if (m) {
        events.push({ key: `${id}:${i + 1}`, file: rel, line: i + 1, kind: 'Listen', detail: m[1].replace(/\s+/g, ' ').trim() });
      }
      m = l.match(/addEventListener\(\s*([^,]+),/);
      if (m) {
        events.push({ key: `${id}:${i + 1}`, file: rel, line: i + 1, kind: 'addEventListener', detail: l.trim().slice(0, 140) });
      }
    });
  }
  const webRoot = path.join(LEGACY_ROOT, 'webapp');
  for (const f of walk(webRoot, '.zul')) {
    const rel = path.relative(webRoot, f).replaceAll(path.sep, '/');
    const lines = stripXmlComments(fs.readFileSync(f, 'utf8'));
    lines.forEach((l, i) => {
      for (const m of l.matchAll(/\b(on[A-Z][A-Za-z]*)="([^"]*)"/g)) {
        events.push({ key: `${rel}:${i + 1}:${m[1]}`, file: rel, line: i + 1, kind: 'zulAttr', detail: `${m[1]}="${m[2]}"` });
      }
      // hrefs on clickable elements only (skip <link>/<?link>/<script>/<style> resource includes)
      if (!/<\??(link|script|style)\b/.test(l)) {
        for (const m of l.matchAll(/\bhref="([^"]*)"/g)) {
          events.push({ key: `${rel}:${i + 1}:href`, file: rel, line: i + 1, kind: 'href', detail: m[1] });
        }
      }
      // every clickable control, so buttons without any handler are inventoried too
      for (const m of l.matchAll(/<(?:n:|x:)?(button|toolbarbutton|menuitem|a)\b[^>]*?\bid="([^"]+)"/g)) {
        events.push({ key: `${rel}#${m[2]}`, file: rel, line: i + 1, kind: 'control', detail: `<${m[1]} id="${m[2]}">` });
      }
    });
  }
  // keys must be unique; disambiguate duplicates on the same line
  const seen = new Map();
  for (const e of events) {
    const n = (seen.get(e.key) ?? 0) + 1;
    seen.set(e.key, n);
    if (n > 1) e.key = `${e.key}#${n}`;
  }
  return events;
}

if (process.argv[1] === import.meta.filename) {
  const ev = extractLegacyEvents();
  if (process.argv.includes('--json')) console.log(JSON.stringify(ev, null, 1));
  else for (const e of ev) console.log(`${e.key}\t${e.kind}\t${e.detail}`);
}
