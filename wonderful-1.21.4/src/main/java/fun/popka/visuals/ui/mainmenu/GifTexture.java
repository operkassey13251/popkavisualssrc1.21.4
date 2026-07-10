package fun.popka.visuals.ui.mainmenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GifTexture {

    private static final int DEFAULT_DELAY = 100;
    private static final int DISPOSAL_NONE = 0;
    private static final int DISPOSAL_DO_NOT_DISPOSE = 1;
    private static final int DISPOSAL_RESTORE_TO_BACKGROUND = 2;
    private static final int DISPOSAL_RESTORE_TO_PREVIOUS = 3;

    private static final long MAX_ELAPSED_MS = 100L;

    private final Identifier resourceId;
    private Identifier textureId;
    private NativeImageBackedTexture texture;
    private NativeImage frameBuffer;
    private final List<Frame> frames = new ArrayList<>();
    private boolean loaded = false;
    private long timer = 0;
    private int currentFrame = 0;
    private boolean loopForever = true;
    private int loopCount = 0;
    private int loopsDone = 0;
    private long lastUpdateTime = 0;

    public GifTexture(Identifier resourceId) {
        this.resourceId = resourceId;
        load();
    }

    private void load() {
        try (InputStream stream = MinecraftClient.getInstance().getResourceManager().getResource(resourceId).get().getInputStream()) {
            ImageInputStream imageStream = ImageIO.createImageInputStream(stream);
            ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
            reader.setInput(imageStream);

            int width = getInt(reader.getStreamMetadata(), "LogicalScreenDescriptor", "logicalScreenWidth");
            int height = getInt(reader.getStreamMetadata(), "LogicalScreenDescriptor", "logicalScreenHeight");
            if (width <= 0 || height <= 0) {
                width = 1;
                height = 1;
            }

            int frameCount = reader.getNumImages(true);
            BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            BufferedImage prevCanvas = null;
            Graphics2D canvasG = canvas.createGraphics();
            canvasG.setComposite(AlphaComposite.Src);

            for (int i = 0; i < frameCount; i++) {
                BufferedImage rawFrame = reader.read(i);
                GifFrameInfo info = parseFrameInfo(reader.getImageMetadata(i));

                if (info.disposalMethod == DISPOSAL_RESTORE_TO_PREVIOUS && prevCanvas != null) {
                    canvasG.drawImage(prevCanvas, 0, 0, null);
                }

                if (info.disposalMethod != DISPOSAL_RESTORE_TO_PREVIOUS) {
                    prevCanvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    prevCanvas.createGraphics().drawImage(canvas, 0, 0, null);
                }

                canvasG.drawImage(rawFrame, info.x, info.y, null);

                BufferedImage frameCopy = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                frameCopy.createGraphics().drawImage(canvas, 0, 0, null);
                frames.add(new Frame(frameCopy, info.delay));

                if (info.disposalMethod == DISPOSAL_RESTORE_TO_BACKGROUND) {
                    canvasG.setColor(new Color(0, 0, 0, 0));
                    canvasG.fillRect(info.x, info.y, rawFrame.getWidth(), rawFrame.getHeight());
                } else if (info.disposalMethod == DISPOSAL_RESTORE_TO_PREVIOUS) {
                    canvasG.drawImage(prevCanvas, 0, 0, null);
                }
            }

            canvasG.dispose();
            reader.dispose();
            imageStream.close();

            if (!frames.isEmpty()) {
                frameBuffer = createNativeImage(frames.get(0).image);
                texture = new NativeImageBackedTexture(frameBuffer);
                texture.setFilter(true, false);
                textureId = Identifier.of("popka", "dynamic/mainmenu_bg");
                MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, texture);
                loaded = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            loaded = false;
        }
    }

    private static NativeImage createNativeImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        NativeImage nativeImage = new NativeImage(width, height, false);
        writeFrame(image, nativeImage);
        return nativeImage;
    }

    private static void writeFrame(BufferedImage image, NativeImage target) {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                target.setColorArgb(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
    }

    public void update(float delta) {
        if (!loaded || frames.isEmpty()) return;

        long now = System.currentTimeMillis();
        if (lastUpdateTime == 0) lastUpdateTime = now;
        long elapsed = now - lastUpdateTime;
        lastUpdateTime = now;
        if (elapsed > MAX_ELAPSED_MS) elapsed = MAX_ELAPSED_MS;
        if (elapsed < 0) elapsed = 0;

        timer += elapsed;
        Frame frame = frames.get(currentFrame);
        boolean changed = false;
        while (timer >= frame.delay) {
            timer -= frame.delay;
            currentFrame++;
            if (currentFrame >= frames.size()) {
                currentFrame = loopForever ? 0 : frames.size() - 1;
            }
            frame = frames.get(currentFrame);
            changed = true;
        }

        if (changed) {
            try {
                writeFrame(frame.image, frameBuffer);
                texture.upload();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Identifier getIdentifier() {
        return textureId;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void close() {
        if (texture != null) {
            texture.close();
        }
    }

    private static int getInt(IIOMetadata metadata, String nodeName, String attrName) {
        if (metadata == null) return 0;
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree("javax_imageio_gif_stream_1.0");
        if (root == null) return 0;
        IIOMetadataNode node = (IIOMetadataNode) root.getElementsByTagName(nodeName).item(0);
        if (node == null) return 0;
        try {
            return Integer.parseInt(node.getAttribute(attrName));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static GifFrameInfo parseFrameInfo(IIOMetadata metadata) {
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree("javax_imageio_gif_image_1.0");
        IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
        IIOMetadataNode img = (IIOMetadataNode) root.getElementsByTagName("ImageDescriptor").item(0);

        int delay = DEFAULT_DELAY;
        int disposal = DISPOSAL_NONE;
        int x = 0;
        int y = 0;

        if (gce != null) {
            String delayTime = gce.getAttribute("delayTime");
            try {
                delay = Integer.parseInt(delayTime) * 10;
            } catch (NumberFormatException ignored) {
            }
            String disposalName = gce.getAttribute("disposalMethod");
            if (disposalName != null) {
                try {
                    disposal = Integer.parseInt(disposalName);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (img != null) {
            try {
                x = Integer.parseInt(img.getAttribute("imageLeftPosition"));
                y = Integer.parseInt(img.getAttribute("imageTopPosition"));
            } catch (NumberFormatException ignored) {
            }
        }

        if (delay <= 0) delay = DEFAULT_DELAY;
        return new GifFrameInfo(delay, disposal, x, y);
    }

    private record GifFrameInfo(int delay, int disposalMethod, int x, int y) {
    }

    private record Frame(BufferedImage image, int delay) {
    }
}
