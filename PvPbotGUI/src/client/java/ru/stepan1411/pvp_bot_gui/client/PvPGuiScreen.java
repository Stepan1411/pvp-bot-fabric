package ru.stepan1411.pvp_bot_gui.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import ru.stepan1411.pvp_bot_gui.client.network.BotPayloads;
import ru.stepan1411.pvp_bot_gui.client.network.SettingsPayloads;
import org.stepan1411.sgl.gui.BaseGuiScreen;
import org.stepan1411.sgl.gui.Notification;
import org.stepan1411.sgl.gui.Surface;
import org.stepan1411.sgl.gui.components.StyledButton;
import org.stepan1411.sgl.gui.components.StyledLabel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PvPGuiScreen {
    private static Map<String, String> cachedSettings;
    private static List<String> cachedBotList;

    public static void open() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            client.setScreen(new LoadingScreen());
            ClientPlayNetworking.send(new SettingsPayloads.SettingsRequestPayload());
            return;
        }
        client.setScreen(new ErrorScreen("§cNot connected to a server"));
    }

    public static void onSettingsReceived(Map<String, String> settings) {
        cachedSettings = new LinkedHashMap<>(settings);
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            Screen current = client.currentScreen;
            if (current instanceof LoadingScreen) {
                client.setScreen(new MainScreen());
            }
        });
    }

    public static void onBotListReceived(List<String> botNames) {
        cachedBotList = botNames;
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.currentScreen instanceof BotsScreen) {
                client.setScreen(new BotsScreen(botNames));
            }
        });
    }

    private static class MainScreen extends BaseGuiScreen {
        MainScreen() {
            super(Text.literal("PvP Bot"));
        }

        @Override
        protected void init() {
            super.init();
            int panelW = 340;
            int panelH = 280;
            int pX = (width - panelW) / 2;
            int pY = (height - panelH) / 2;

            setPanelBounds(pX, pY, panelW, panelH);
            enableScroll(false);

            int cx = pX + 30;
            int btnW = panelW - 60;
            int cy = pY + 55;
            int btnH = 32;
            int gap = 8;

            StyledButton spawnBtn = new StyledButton(cx, cy, btnW, btnH, Text.literal("§lSpawn Bot"));
            spawnBtn.surface(Surface.flat(0xFF006600));
            spawnBtn.onPress(() -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().sendChatMessage("/pvpbot spawn");
                }
                cachedBotList = null;
            });
            addDrawableChild(spawnBtn);
            cy += btnH + gap;

            StyledButton botsBtn = new StyledButton(cx, cy, btnW, btnH,
                Text.literal("§lBots" + (cachedBotList != null ? " §7(" + cachedBotList.size() + ")" : "")));
            botsBtn.surface(Surface.flat(0xFF444444));
            botsBtn.onPress(() -> {
                ClientPlayNetworking.send(new BotPayloads.BotListRequestPayload());
                client.setScreen(new BotsScreen(cachedBotList));
            });
            addDrawableChild(botsBtn);
            cy += btnH + gap;

            StyledButton settingsBtn = new StyledButton(cx, cy, btnW, btnH, Text.literal("§lSettings"));
            settingsBtn.surface(Surface.flat(0xFF444444));
            settingsBtn.onPress(() -> {
                if (cachedSettings != null) {
                    client.setScreen(new SettingsPage(cachedSettings));
                }
            });
            addDrawableChild(settingsBtn);
        }

        @Override
        protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
            int panelW = 340;
            int panelH = 280;
            int pX = (width - panelW) / 2;
            int pY = (height - panelH) / 2;

            context.fill(pX, pY, pX + panelW, pY + panelH, 0xB0303030);
            context.fill(pX, pY, pX + panelW, pY + 1, 0xFF505050);
            context.fill(pX, pY + panelH - 1, pX + panelW, pY + panelH, 0xFF505050);
            context.fill(pX, pY, pX + 1, pY + panelH, 0xFF505050);
            context.fill(pX + panelW - 1, pY, pX + panelW, pY + panelH, 0xFF505050);

            Text titleText = Text.literal("§lPvP Bot");
            context.drawText(textRenderer, titleText, (width - textRenderer.getWidth(titleText)) / 2, pY + 14, 0xFFFFAA00, true);
        }
    }

    private static class BotsScreen extends BaseGuiScreen {
        private List<String> botNames;

        BotsScreen(List<String> botNames) {
            super(Text.literal("Bots"));
            this.botNames = botNames;
        }

        @Override
        protected void init() {
            super.init();
            int panelW = 340;
            int panelH = 320;
            int pX = (width - panelW) / 2;
            int pY = (height - panelH) / 2;

            setPanelBounds(pX, pY, panelW, panelH);
            enableScroll(true);
            setScrollbarColors(0xFF222222, 0xFF999999);

            int cx = pX + 15;
            int btnW = panelW - 30;
            int cy = pY + 40;
            int btnH = 22;
            int gap = 4;

            if (botNames == null) {
                StyledLabel loading = new StyledLabel(cx, cy, btnW, 20, Text.literal("§eLoading bots..."));
                loading.color(0xFFFFAA00);
                registerScrollable(loading);
                cy += 22 + gap;
            } else if (botNames.isEmpty()) {
                StyledLabel empty = new StyledLabel(cx, cy, btnW, 20, Text.literal("§7No bots spawned"));
                empty.color(0xFF888888);
                registerScrollable(empty);
                cy += 22 + gap;
            } else {
                for (String name : botNames) {
                    StyledButton botBtn = new StyledButton(cx, cy, btnW, btnH,
                        Text.literal("§e" + name));
                    botBtn.surface(Surface.flat(0xFF2A2A2A));
                    botBtn.onPress(() -> showBotNotification(name));
                    registerScrollable(botBtn);
                    cy += btnH + gap;
                }
            }

            cy += 6;
            StyledButton backBtn = new StyledButton(cx + btnW / 2 - 40, cy, 80, btnH, Text.literal("Back"));
            backBtn.surface(Surface.flat(0xFF444444));
            backBtn.onPress(() -> client.setScreen(new MainScreen()));
            registerScrollable(backBtn);
            cy += btnH + gap;

            setContentHeight(cy - pY);
        }

        private void showBotNotification(String name) {
            showNotification(new Notification(Text.literal("§e" + name),
                new Notification.Action(Text.literal("§cKill"), () -> {
                    ClientPlayNetworking.send(new BotPayloads.BotActionPayload(name, "KILL"));
                    dismissNotification();
                }),
                new Notification.Action(Text.literal("§aHeal"), () -> {
                    ClientPlayNetworking.send(new BotPayloads.BotActionPayload(name, "HEAL"));
                    dismissNotification();
                }),
                new Notification.Action(Text.literal("§eClear Inventory"), () -> {
                    ClientPlayNetworking.send(new BotPayloads.BotActionPayload(name, "CLEAR_INVENTORY"));
                    dismissNotification();
                }),
                new Notification.Action(Text.literal("§6Give Kit"), () -> {
                    ClientPlayNetworking.send(new BotPayloads.BotActionPayload(name, "GIVE_KIT"));
                    dismissNotification();
                }),
                new Notification.Action(Text.literal("§bTP to Me"), () -> {
                    ClientPlayNetworking.send(new BotPayloads.BotActionPayload(name, "TP_TO_ME"));
                    dismissNotification();
                })
            ));
        }

        @Override
        protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
            int panelW = 340;
            int panelH = 320;
            int pX = (width - panelW) / 2;
            int pY = (height - panelH) / 2;

            context.fill(pX, pY, pX + panelW, pY + panelH, 0xB0303030);
            context.fill(pX, pY, pX + panelW, pY + 1, 0xFF505050);
            context.fill(pX, pY + panelH - 1, pX + panelW, pY + panelH, 0xFF505050);
            context.fill(pX, pY, pX + 1, pY + panelH, 0xFF505050);
            context.fill(pX + panelW - 1, pY, pX + panelW, pY + panelH, 0xFF505050);

            context.drawText(textRenderer, Text.literal("§lBots"), pX + 15, pY + 12, 0xFFFFFFFF, true);
        }
    }

    private static class SettingsPage extends BaseGuiScreen {
        private final Map<String, String> settings;

        SettingsPage(Map<String, String> settings) {
            super(Text.literal("Settings"));
            this.settings = settings;
        }

        @Override
        protected void init() {
            super.init();
            int panelW = 340;
            int panelH = 320;
            int pX = (width - panelW) / 2;
            int pY = (height - panelH) / 2;

            setPanelBounds(pX, pY, panelW, panelH);
            enableScroll(true);
            setScrollbarColors(0xFF222222, 0xFF999999);

            int cx = pX + 15;
            int cy = pY + 35;
            int btnW = panelW - 30;
            int btnH = 20;
            int gap = 4;

            if (settings != null) {
                for (Map.Entry<String, String> entry : settings.entrySet()) {
                    String key = entry.getKey();
                    String val = entry.getValue();

                    if (val.equals("true") || val.equals("false")) {
                        boolean isOn = val.equals("true");
                        String name = formatKey(key);
                        StyledButton btn = new StyledButton(cx, cy, btnW, btnH,
                            Text.literal(name + ": " + (isOn ? "§aON" : "§cOFF")));
                        btn.surface(isOn ? Surface.flat(0xFF006600) : Surface.flat(0xFF444444));
                        btn.onPress(() -> {
                            boolean newVal = !isOn;
                            cachedSettings.put(key, String.valueOf(newVal));
                            ClientPlayNetworking.send(new SettingsPayloads.SettingsUpdatePayload(key, String.valueOf(newVal)));
                            btn.setMessage(Text.literal(name + ": " + (newVal ? "§aON" : "§cOFF")));
                            btn.surface(newVal ? Surface.flat(0xFF006600) : Surface.flat(0xFF444444));
                        });
                        registerScrollable(btn);
                        cy += btnH + gap;
                    } else {
                        StyledLabel label = new StyledLabel(cx, cy, btnW, btnH,
                            Text.literal(formatKey(key) + ": " + val));
                        label.color(0xFFCCCCCC);
                        registerScrollable(label);
                        cy += btnH + gap;
                    }
                }
            }

            cy += 6;
            StyledButton backBtn = new StyledButton(cx + btnW / 2 - 40, cy, 80, btnH, Text.literal("Back"));
            backBtn.surface(Surface.flat(0xFF444444));
            backBtn.onPress(() -> client.setScreen(new MainScreen()));
            registerScrollable(backBtn);
            cy += btnH + gap;

            setContentHeight(cy - pY);
        }

        @Override
        protected void renderContent(DrawContext context, int mouseX, int mouseY, float delta) {
            int panelW = 340;
            int panelH = 320;
            int pX = (width - panelW) / 2;
            int pY = (height - panelH) / 2;

            context.fill(pX, pY, pX + panelW, pY + panelH, 0xB0303030);
            context.fill(pX, pY, pX + panelW, pY + 1, 0xFF505050);
            context.fill(pX, pY + panelH - 1, pX + panelW, pY + panelH, 0xFF505050);
            context.fill(pX, pY, pX + 1, pY + panelH, 0xFF505050);
            context.fill(pX + panelW - 1, pY, pX + panelW, pY + panelH, 0xFF505050);

            context.drawText(textRenderer, Text.literal("§lSettings"), pX + 15, pY + 12, 0xFFFFFFFF, true);
        }
    }

    private static String formatKey(String camelCase) {
        StringBuilder sb = new StringBuilder();
        for (char c : camelCase.toCharArray()) {
            if (Character.isUpperCase(c) && sb.length() > 0) sb.append(' ');
            sb.append(c);
        }
        return sb.toString();
    }

    private static class LoadingScreen extends Screen {
        private int ticks;

        protected LoadingScreen() {
            super(Text.literal("PvP Bot GUI"));
        }

        @Override
        public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
            context.fill(0, 0, width, height, 0xCC000000);
        }

        @Override
        public void tick() {
            ticks++;
            if (ticks > 100) {
                MinecraftClient.getInstance().setScreen(new ErrorScreen("§cServer not responding"));
            }
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            renderBackground(context, mouseX, mouseY, delta);
            String status = ticks < 20 ? "§eConnecting" : "§eLoading settings";
            int dots = (ticks / 10) % 4;
            Text text = Text.literal(status + ".".repeat(dots) + " ".repeat(3 - dots));
            int x = (width - textRenderer.getWidth(text)) / 2;
            context.drawText(textRenderer, text, x, height / 2 - 10, 0xFFFFFF, true);
        }
    }

    private static class ErrorScreen extends Screen {
        private final String message;

        protected ErrorScreen(String message) {
            super(Text.literal("PvP Bot GUI"));
            this.message = message;
        }

        @Override
        public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
            context.fill(0, 0, width, height, 0xCC000000);
        }

        @Override
        protected void init() {
            int y = height / 2 - 20;
            addDrawableChild(ButtonWidget.builder(Text.literal("Retry"), btn -> open())
                .dimensions(width / 2 - 50, y + 30, 100, 20).build());
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            renderBackground(context, mouseX, mouseY, delta);
            Text text = Text.literal(message);
            int x = (width - textRenderer.getWidth(text)) / 2;
            context.drawText(textRenderer, text, x, height / 2 - 20, 0xFFFFFF, true);
            super.render(context, mouseX, mouseY, delta);
        }
    }
}
