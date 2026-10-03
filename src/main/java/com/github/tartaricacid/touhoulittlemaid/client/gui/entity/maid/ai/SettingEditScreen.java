package com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.ai;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.MaidAIChatManager;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.CharacterSetting;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.SettingReader;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.bean.MetaData;
import com.github.tartaricacid.touhoulittlemaid.api.client.render.MaidRenderState;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.FlatColorButton;
import com.github.tartaricacid.touhoulittlemaid.client.resource.loader.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.client.resource.pojo.MaidModelInfo;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.network.message.ai.SaveMaidAIDataPackage;
import com.github.tartaricacid.touhoulittlemaid.util.EntityCacheUtil;
import com.github.tartaricacid.touhoulittlemaid.util.migrate.ScreenUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDLProperties;
import org.lwjgl.sdl.SDL_DialogFileCallback;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static com.github.tartaricacid.touhoulittlemaid.client.resource.models.SpecialMaidModelResolver.EASTER_EGG_MODEL;
import static com.github.tartaricacid.touhoulittlemaid.util.EntityCacheUtil.clearMaidDataResidue;

public class SettingEditScreen extends Screen {
    private static final long MAX_TIP_TIME = 2000;

    private final @Nullable Screen parent;
    private final EntityMaid maid;
    private final MaidAIChatManager manager;

    private EditBox ownerName;
    private MultiLineEditBox customSetting;
    private long tipTimestamp = -1;

    // SDL 的文件对话框是异步的：过滤器数组与回调必须一直存活到原生侧调用完成，
    // 所以在这里持有，等回调触发后再一起释放（见 freeFileDialog）。
    private @Nullable SDL_DialogFileCallback fileDialogCallback;
    private @Nullable SDL_DialogFileFilter.Buffer fileDialogFilter;
    private @Nullable ByteBuffer fileDialogFilterName;
    private @Nullable ByteBuffer fileDialogFilterPattern;

    public SettingEditScreen(EntityMaid maid) {
        this(null, maid);
    }

    public SettingEditScreen(@Nullable Screen parent, EntityMaid maid) {
        super(Component.literal("Setting Edit Screen"));
        this.parent = parent;
        this.maid = maid;
        this.manager = maid.getAiChatManager();
    }

    @Override
    protected void init() {
        this.clearWidgets();

        int posX = this.width / 2 - 195;
        int boxWidth = 256;

        this.ownerName = this.addRenderableWidget(new EditBox(font, posX + 1, 30,
                boxWidth - 2, 20, Component.literal("Owner Name Box")));
        this.ownerName.setValue(manager.ownerName);
        this.ownerName.setMaxLength(128);
        this.ownerName.setResponder(s -> manager.ownerName = s);


        this.customSetting = this.addRenderableWidget(MultiLineEditBox.builder()
                .setX(posX)
                .setY(70)
                .setPlaceholder(Component.translatable("gui.touhou_little_maid.button.maid_ai_chat_config.edit_custom_setting.edit"))
                .build(font, boxWidth, this.height - 100, Component.literal("Custom Setting Box")));
        this.customSetting.setValue(manager.customSetting);
        this.customSetting.setCharacterLimit(4096);
        this.customSetting.setValueListener(s -> manager.customSetting = s);

        MutableComponent export = Component.translatable("gui.touhou_little_maid.button.maid_ai_chat_config.edit_custom_setting.export");
        this.addRenderableWidget(new FlatColorButton(posX + 265, ownerName.getY(), 128, 20, export,
                b -> exportSetting(export)));

        this.addRenderableWidget(new FlatColorButton(posX + 265, customSetting.getY(), 128, 20,
                Component.translatable("selectWorld.edit.save"), b -> {
            this.saveConfig();
            this.tipTimestamp = System.currentTimeMillis();
        }));

        MutableComponent saveQuit = Component.translatable("gui.touhou_little_maid.button.maid_ai_chat_config.edit_custom_setting.save_and_quit");
        this.addRenderableWidget(new FlatColorButton(posX + 265, customSetting.getY() + 25, 128, 20, saveQuit, b -> {
            this.saveConfig();
            this.onClose();
        }));

        this.addRenderableWidget(new FlatColorButton(posX + 265, customSetting.getY() + 50, 128, 20,
                Component.translatable("gui.back"), b -> this.onClose()));
    }

    private void exportSetting(MutableComponent export) {
        // SDL 同一时间只允许一个文件对话框
        if (this.fileDialogCallback != null) {
            return;
        }
        String title = export.getString();
        String defaultFileName = "%s.yml".formatted(this.maid.getName().getString());
        String path = SettingReader.getSettingsFolder().resolve(defaultFileName).toString();
        String fileFilter = Component.translatable("gui.touhou_little_maid.button.maid_ai_chat_config.edit_custom_setting.export.format").getString();

        MetaData metaData = getMetaData();
        CharacterSetting setting = new CharacterSetting(metaData, this.customSetting.getValue());

        // 26.3 移除了 GLFW/tinyfd，原生文件对话框改用 SDL
        this.fileDialogFilterName = MemoryUtil.memUTF8(fileFilter);
        this.fileDialogFilterPattern = MemoryUtil.memUTF8("*.yml");
        this.fileDialogFilter = SDL_DialogFileFilter.calloc(1);
        this.fileDialogFilter.get(0).name(this.fileDialogFilterName).pattern(this.fileDialogFilterPattern);

        this.fileDialogCallback = SDL_DialogFileCallback.create((userdata, fileList, filter) -> {
            // fileList 指向的原生内存只在回调期间有效，先拷成 String 再切回客户端主线程处理
            String result = firstDialogPath(fileList);
            Minecraft.getInstance().execute(() -> {
                freeFileDialog();
                if (StringUtils.isBlank(result)) {
                    return;
                }
                try {
                    setting.save(new File(result));
                    LocalPlayer player = Minecraft.getInstance().player;
                    if (player != null) {
                        Component tip = Component.translatable("gui.touhou_little_maid.button.maid_ai_chat_config.edit_custom_setting.export.success", result)
                                .withStyle(ChatFormatting.GRAY);
                        player.sendSystemMessage(tip);
                    }
                } catch (IOException e) {
                    TouhouLittleMaid.LOGGER.error("Error saving setting", e);
                }
            });
        });

        int properties = SDLProperties.SDL_CreateProperties();
        SDLProperties.SDL_SetStringProperty(properties, SDLDialog.SDL_PROP_FILE_DIALOG_TITLE_STRING, title);
        SDLProperties.SDL_SetStringProperty(properties, SDLDialog.SDL_PROP_FILE_DIALOG_LOCATION_STRING, path);
        SDLProperties.SDL_SetNumberProperty(properties, SDLDialog.SDL_PROP_FILE_DIALOG_NFILTERS_NUMBER, 1);
        SDLProperties.SDL_SetPointerProperty(properties, SDLDialog.SDL_PROP_FILE_DIALOG_FILTERS_POINTER, this.fileDialogFilter.address());
        SDLDialog.SDL_ShowFileDialogWithProperties(SDLDialog.SDL_FILEDIALOG_SAVEFILE, this.fileDialogCallback,
                MemoryUtil.NULL, properties);
        SDLProperties.SDL_DestroyProperties(properties);
    }

    /**
     * 从 SDL 的 {@code const char * const *} 文件列表里取出第一个路径；取消时为 NULL。
     */
    private static String firstDialogPath(long fileList) {
        if (fileList == MemoryUtil.NULL) {
            return null;
        }
        long first = MemoryUtil.memGetAddress(fileList);
        return first == MemoryUtil.NULL ? null : MemoryUtil.memUTF8(first);
    }

    private void freeFileDialog() {
        MemoryUtil.memFree(this.fileDialogFilterName);
        MemoryUtil.memFree(this.fileDialogFilterPattern);
        this.fileDialogFilterName = null;
        this.fileDialogFilterPattern = null;
        if (this.fileDialogFilter != null) {
            this.fileDialogFilter.free();
            this.fileDialogFilter = null;
        }
        if (this.fileDialogCallback != null) {
            this.fileDialogCallback.free();
            this.fileDialogCallback = null;
        }
    }

    @NotNull
    private MetaData getMetaData() {
        String lang = this.getMinecraft().getLanguageManager().getSelected();
        String author = "Unknown";
        if (this.getMinecraft().player != null) {
            author = this.getMinecraft().player.getScoreboardName();
        }
        String modelId = this.maid.getModelId();
        return new MetaData(0, author, Collections.singletonList(modelId), lang);
    }

    @Override
    public void resize(int pWidth, int pHeight) {
        String ownerNameValue = this.ownerName.getValue();
        String customSettingValue = this.customSetting.getValue();
        super.resize(pWidth, pHeight);
        this.ownerName.setValue(ownerNameValue);
        this.customSetting.setValue(customSettingValue);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return super.mouseReleased(event) || this.customSetting.mouseReleased(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.fillGradient(0, 0, this.width, this.height, 0xc0101010, 0xc0101010);
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

        graphics.text(font, Component.translatable("gui.touhou_little_maid.button.maid_ai_chat_config.owner_name"),
                ownerName.getX() + 2, ownerName.getY() - 12, 0xFFFFFF);
        graphics.text(font, Component.translatable("gui.touhou_little_maid.button.maid_ai_chat_config.custom_setting"),
                customSetting.getX() + 2, customSetting.getY() - 12, 0xFFFFFF);

        drawMaid(graphics, customSetting.getX() + customSetting.getWidth() + 73, customSetting.getY() + 96, maid);

        long time = System.currentTimeMillis() - this.tipTimestamp;
        if (time < MAX_TIP_TIME) {
            double value = (double) (time) / MAX_TIP_TIME * Math.PI;
            int alpha = (int) (Math.sin(value) * 0xFF);
            alpha = Mth.clamp(alpha, 15, 240);
            graphics.centeredText(font, Component.translatable("gui.touhou_little_maid.button.maid_ai_chat_config.edit_custom_setting.saved"),
                    customSetting.getX() + customSetting.getWidth() + 73, customSetting.getY() - 12, (alpha << 24) + 0xFF1111);
        }
    }

    private void drawMaid(GuiGraphicsExtractor graphics, int posX, int posY, EntityMaid rawMaid) {
        Level world = getMinecraft().level;
        if (world == null) {
            return;
        }
        Optional<MaidModelInfo> info = CustomPackLoader.MAID_MODELS.getInfo(rawMaid.getModelId());
        if (info.isEmpty()) {
            return;
        }
        MaidModelInfo modelInfo = info.get();

        EntityMaid maid = EntityCacheUtil.getMaid(world, EntitySpawnReason.COMMAND);
        maid.renderState = MaidRenderState.GUI;

        clearMaidDataResidue(maid, false);
        if (modelInfo.getEasterEgg() != null) {
            maid.setModelId(EASTER_EGG_MODEL);
        } else {
            maid.setModelId(modelInfo.getModelId().toString());
        }
        float renderItemScale = modelInfo.getRenderItemScale();
        InventoryScreen.extractEntityInInventoryFollowsMouse(
                graphics,
                posX - 45,
                posY - 45,
                posX + 45,
                posY + 55,
                (int) (25 * renderItemScale),
                0.1F,
                posX - 15,
                posY,
                maid
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Screen screen = Objects.requireNonNullElse(this.parent, new AIChatScreen(this.maid));
        ScreenUtil.setScreen(screen);
    }

    private void saveConfig() {
        ClientPacketDistributor.sendToServer(new SaveMaidAIDataPackage(this.maid.getId(), this.manager));
    }
}
