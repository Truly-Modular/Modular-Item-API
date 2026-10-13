
const sharp = require("sharp");
const fs = require("fs");

async function generateColorMap(grayPath, colorPath, outputPath) {
    const gray = await sharp(grayPath)
        .greyscale()
        .raw()
        .toBuffer({ resolveWithObject: true });

    const color = await sharp(colorPath)
        .resize(gray.info.width, gray.info.height)
        .removeAlpha()
        .raw()
        .toBuffer({ resolveWithObject: true });

    const buckets = new Map();

    for (let i = 0; i < gray.data.length; i++) {
        const value = gray.data[i];
        const j = i * 3;

        if (!buckets.has(value)) {
            buckets.set(value, [0, 0, 0, 0]);
        }

        const bucket = buckets.get(value);

        bucket[0] += color.data[j];
        bucket[1] += color.data[j + 1];
        bucket[2] += color.data[j + 2];
        bucket[3]++;
    }

    const colors = {};

    for (const [value, [r, g, b, count]] of buckets) {
        colors[value] = [r, g, b]
            .map(channel =>
                Math.round(channel / count)
                    .toString(16)
                    .padStart(2, "0")
            )
            .join("");
    }

    const result = { colors };

    fs.writeFileSync(
        outputPath,
        JSON.stringify(result, null, 4)
    );

    console.log(`Saved ${outputPath}`);
}

generateColorMap(
    "iron_layer_1.png",
    "diamond_layer_1.png",
    "color-map.json"
).catch(console.error);
