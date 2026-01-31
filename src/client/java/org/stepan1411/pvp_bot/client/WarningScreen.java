package org.stepan1411.pvp_bot.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WarningScreen extends Screen {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("PVP_BOT_WARNING");
    private static final Identifier WARNING_TEXTURE = Identifier.of("pvp_bot", "textures/gui/warning.png");
    
    private final Screen parent;
    private final Path warningFile;
    private CheckboxWidget dontShowAgain;
    private boolean textureLogged = false;  // Флаг для логирования только один раз
    
    public WarningScreen(Screen parent) {
        super(Text.literal("WARNING: This mod is unstable in singleplayer!"));
        this.parent = parent;
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("pvpbot");
        this.warningFile = configDir.resolve("no_singleplayer_warning.txt");
    }
    
    @Override
    protected void init() {
        super.init();
        
        if (!textureLogged) {
            LOGGER.info("Initializing warning screen");
            LOGGER.info("Texture identifier: {}", WARNING_TEXTURE);
            textureLogged = true;
        }
        
        int centerX = this.width / 2;
        
        // Кнопки внизу экрана
        int bottomY = this.height - 60;  // 60 пикселей от низа
        
        // Checkbox
        this.dontShowAgain = CheckboxWidget.builder(Text.literal("Don't show again"), this.textRenderer)
                .pos(centerX - 60, bottomY)
                .build();
        this.addDrawableChild(dontShowAgain);
        
        // OK button - под чекбоксом
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("OK"),
                button -> {
                    if (dontShowAgain.isChecked()) {
                        try {
                            Files.createDirectories(warningFile.getParent());
                            Files.writeString(warningFile, "User disabled singleplayer warning");
                            LOGGER.info("Warning disabled by user");
                        } catch (IOException ex) {
                            LOGGER.error("Failed to save warning preference", ex);
                        }
                    }
                    if (this.client != null) {
                        this.client.setScreen(parent);
                    }
                }
        ).dimensions(centerX - 40, bottomY + 30, 80, 20).build());
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Не рисуем затемнение - изображение само будет фоном
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Сначала рисуем чёрный фон
        context.fill(0, 0, this.width, this.height, 0xFF000000);
        
        // Изображение занимает всю высоту экрана и сохраняет квадратные пропорции
        int displayHeight = this.height;  // Высота = высота окна
        int displayWidth = displayHeight;  // Ширина = высота (квадрат)
        int imageX = (this.width - displayWidth) / 2;  // Центрируем по горизонтали
        int imageY = 0;  // Верх изображения = верх окна
        
        try {
            // Рисуем всю текстуру с растяжением
            // Параметры: pipeline, texture, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight
            context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                WARNING_TEXTURE,
                imageX, imageY,           // позиция на экране
                0, 0,                     // u, v (откуда брать из текстуры)
                displayWidth, displayHeight,  // размер на экране (растянутый)
                1024, 1024,               // размер региона в текстуре (вся текстура)
                1024, 1024                // полный размер текстуры
            );
        } catch (Exception e) {
            if (!textureLogged) {
                LOGGER.error("Failed to render warning texture", e);
                textureLogged = true;
            }
            // Рисуем красный прямоугольник как fallback
            context.fill(imageX, imageY, imageX + displayWidth, imageY + displayHeight, 0xFFFF0000);
            
            // Рисуем текст с ошибкой
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                "Failed to load warning image",
                this.width / 2,
                this.height / 2,
                0xFFFFFF
            );
        }
        
        // Виджеты
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
