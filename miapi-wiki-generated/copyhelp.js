const fs = require("fs/promises");
const fss = require("fs");
const path = require("path");

const CHUNK_SIZE = 4 * 1024 * 1024; // 4MB chunks
const RETRY_DELAY = 3000;

async function sleep(ms) {
  return new Promise(r => setTimeout(r, ms));
}

async function ensureDir(p) {
  await fs.mkdir(p, { recursive: true });
}

async function copyFileResumable(src, dst) {
  await ensureDir(path.dirname(dst));

  let offset = 0;

  try {
    const stat = await fs.stat(dst);
    offset = stat.size;
  } catch {}

  console.log(`Copying: ${src} (resume at ${offset})`);

  while (true) {
    let srcFd, dstFd;

    try {
      srcFd = await fs.open(src, "r");
      dstFd = await fs.open(dst, "a");

      const buffer = Buffer.alloc(CHUNK_SIZE);

      while (true) {
        const { bytesRead } = await srcFd.read(
          buffer,
          0,
          CHUNK_SIZE,
          offset
        );

        if (bytesRead === 0) break;

        await dstFd.write(buffer, 0, bytesRead);
        offset += bytesRead;

        process.stdout.write(`\r${path.basename(src)}: ${offset} bytes`);
      }

      console.log(`\nDONE: ${src}`);
      await srcFd.close();
      await dstFd.close();
      return;

    } catch (err) {
      console.log(`\n⚠️  IO error, retrying: ${err.code}`);

      try { await srcFd?.close(); } catch {}
      try { await dstFd?.close(); } catch {}

      await sleep(RETRY_DELAY);
    }
  }
}

async function copyRecursive(srcDir, dstDir) {
  const entries = await fs.readdir(srcDir, { withFileTypes: true });

  for (const e of entries) {
    const src = path.join(srcDir, e.name);
    const dst = path.join(dstDir, e.name);

    if (e.isDirectory()) {
      await copyRecursive(src, dst);
    } else if (e.isFile()) {
      await copyFileResumable(src, dst);
    }
  }
}

async function main() {
  const [,, src, dst] = process.argv;

  if (!src || !dst) {
    console.log("Usage: node resilient-copy.js <source> <dest>");
    process.exit(1);
  }

  await copyRecursive(src, dst);
}

main().catch(console.error);
