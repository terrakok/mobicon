console.log("XXX:: WORKER SCRIPT!");

self.onmessage = async (e) => {
    console.log("XXX:: worker onmessage");
    const arr = e.data.data;
    const w = e.data.w;
    const h = e.data.h;
    var blob = new Blob([arr.buffer]);

    try {
        const t0 = performance.now();

        const bmp = await createImageBitmap(blob);

        const canvas = new OffscreenCanvas(w, h);
        const ctx = canvas.getContext("2d", { willReadFrequently: true });

        ctx.drawImage(bmp, 0, 0, w, h);
        bmp.close();

        const imgData = ctx.getImageData(0, 0, w, h);
        const totalMs = performance.now() - t0;

    console.log("XXX:: worker postMessage");
        self.postMessage(
            {
                kind: "result",
                width: canvas.width,
                height: canvas.height,
                buffer: imgData.data.buffer,
                totalMs: Math.round(totalMs),
            },
            [imgData.data.buffer]
        );
    } catch (err) {
        self.postMessage({
            kind: "error",
            message: err?.message ?? String(err),
            name: err?.name ?? "Error",
            stack: err?.stack ?? "",
        });
    }
};
