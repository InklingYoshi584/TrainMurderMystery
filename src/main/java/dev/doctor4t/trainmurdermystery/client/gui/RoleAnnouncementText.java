package dev.doctor4t.trainmurdermystery.client.gui;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public enum RoleAnnouncementText {
    BLANK(0xFFFFFF),
    CIVILIAN(0x36E51B),
    VIGILANTE(0x1B8AE5),
    KILLER(0xC13838),
    LOOSE_END(0xC13838);

    public final int colour;
    public final Text roleText;
    public final Text titleText;
    public final Text welcomeText;
    public final Function<Integer, Text> premiseText;
    public final Function<Integer, Text> goalText;
    public final Text winText;

    RoleAnnouncementText(int colour) {
        this.colour = colour;
        this.roleText = Text.translatable("announcement.role." + this.name().toLowerCase()).withColor(this.colour);
        this.titleText = Text.translatable("announcement.title." + this.name().toLowerCase()).withColor(this.colour);
        this.welcomeText = Text.translatable("announcement.welcome", this.roleText).withColor(0xF0F0F0);
        this.premiseText = (count) -> Text.translatable(count == 1 ? "announcement.premise" : "announcement.premises", count);
        this.goalText = (count) -> Text.translatable((count == 1 ? "announcement.goal." : "announcement.goals.") + this.name().toLowerCase(), count).withColor(this.colour);
        this.winText = Text.translatable("announcement.win." + this.name().toLowerCase()).withColor(this.colour);
    }

    public Text getLoseText() {
        return this == KILLER ? CIVILIAN.winText : KILLER.winText;
    }

    public @Nullable Text getEndText(GameFunctions.@NotNull WinStatus status) {
        return switch (status) {
            case NONE -> null;
            case PASSENGERS, TIME -> this == KILLER ? this.getLoseText() : this.winText;
            case KILLERS -> this == KILLER ? this.winText : this.getLoseText();
        };
    }
}