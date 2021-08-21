package com.p4pProject.gameTutorial.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.p4pProject.gameTutorial.ecs.asset.BitmapFontAsset
import com.p4pProject.gameTutorial.ecs.asset.TextureAtlasAsset
import ktx.assets.async.AssetStorage
import ktx.scene2d.Scene2DSkin
import ktx.style.*

enum class SkinLabel {
    LARGE, DEFAULT
}

enum class SkinImageButton {
    PAUSE_PLAY, QUIT, SOUND_ON_OFF, WARRIOR_ATTACK, ARCHER_ATTACK, PRIEST_ATTACK
}

enum class SkinTextButton {
    DEFAULT, TRANSPARENT, LABEL, LABEL_TRANSPARENT
}

enum class SkinWindow {
    DEFAULT
}

enum class SkinScrollPane {
    DEFAULT
}

enum class SkinImage(val atlasKey: String) {
    GAME_HUD("game_hud"),
    WARNING("warning"),
    HP_BAR("life_bar"),
    MP_BAR("mp_bar"),
    SHIELD_BAR("shield_bar"),
    WARRIOR_ATTACK("warriorAttack"),
    ARCHER_ATTACK("archerAttack"),
    PRIEST_ATTACK("priestAttack"),
    PLAY("play"),
    PAUSE("pause"),
    QUIT("quit"),
    FRAME("frame"),
    FRAME_TRANSPARENT("frame_transparent"),
    SOUND_ON("sound"),
    SOUND_OFF("no_sound"),
    SCROLL_V("scroll_v"),
    SCROLL_KNOB("scroll_knob"),
    FRAME_LABEL("label_frame"),
    FRAME_LABEL_TRANSPARENT("label_frame_transparent")
}

fun createSkin(assets: AssetStorage) {
    val atlas = assets[TextureAtlasAsset.UI.descriptor]
    val bigFont = assets[BitmapFontAsset.FONT_LARGE_GRADIENT.descriptor]
    val defaultFont = assets[BitmapFontAsset.FONT_DEFAULT.descriptor]
    Scene2DSkin.defaultSkin = skin(atlas) { skin ->
        createLabelStyles(bigFont, defaultFont)
        createImageButtonStyles(skin)
        createTextButtonStyles(defaultFont, skin)
        createWindowStyles(skin, defaultFont)
        createScrollPaneStyles(skin)
        createHpBarStyles(skin, defaultFont)
    }
}

private fun Skin.createHpBarStyles(skin: Skin, defaultFont: BitmapFont) {
//    imageButton(SkinImage.HP_BAR.name) {
//        over = skin.getDrawable(SkinImage.HP_BAR.atlasKey)
//    }
//    imageButton(SkinImage.MP_BAR.name) {
//        over = skin.newDrawable(SkinImage.MP_BAR.atlasKey, Color(0f, 0f, 255f, 1f))
//        color("blue",0f,0f,255f,1f)
//    }

    textField {
        font = defaultFont
        fontColor = Color(1f,1f,1f,1f)
    }
}

private fun Skin.createScrollPaneStyles(skin: Skin) {
    scrollPane(SkinScrollPane.DEFAULT.name) {
        vScroll = skin.getDrawable(SkinImage.SCROLL_V.atlasKey)
        vScrollKnob = skin.getDrawable(SkinImage.SCROLL_KNOB.atlasKey)
    }
}

private fun Skin.createWindowStyles(
    skin: Skin,
    defaultFont: BitmapFont
) {
    window(SkinWindow.DEFAULT.name) {
        background = skin.getDrawable(SkinImage.FRAME.atlasKey)
        titleFont = defaultFont
    }
}

private fun Skin.createTextButtonStyles(
    defaultFont: BitmapFont,
    skin: Skin
) {
    textButton(SkinTextButton.DEFAULT.name) {
        font = defaultFont
        up = skin.getDrawable(SkinImage.FRAME.atlasKey)
        down = up
    }
    textButton(SkinTextButton.TRANSPARENT.name) {
        font = defaultFont
        up = skin.getDrawable(SkinImage.FRAME_TRANSPARENT.atlasKey)
        down = up
    }
    textButton(SkinTextButton.LABEL.name) {
        font = defaultFont
        up = skin.getDrawable(SkinImage.FRAME_LABEL.atlasKey)
        down = up
    }
    textButton(SkinTextButton.LABEL_TRANSPARENT.name) {
        font = defaultFont
        up = skin.getDrawable(SkinImage.FRAME_LABEL_TRANSPARENT.atlasKey)
        down = up
    }
}


// Use this to create new buttons
private fun Skin.createImageButtonStyles(skin: Skin) {

    imageButton(SkinImageButton.WARRIOR_ATTACK.name) {
        imageUp = skin.getDrawable(SkinImage.WARRIOR_ATTACK.atlasKey)
        imageDown = imageUp
    }
    imageButton(SkinImageButton.ARCHER_ATTACK.name) {
        imageUp = skin.getDrawable(SkinImage.ARCHER_ATTACK.atlasKey)
        imageDown = imageUp
    }
    imageButton(SkinImageButton.PRIEST_ATTACK.name) {
        imageUp = skin.getDrawable(SkinImage.PRIEST_ATTACK.atlasKey)
        imageDown = imageUp
    }
    imageButton(SkinImageButton.WARRIOR_ATTACK.name) {
        imageUp = skin.getDrawable(SkinImage.WARRIOR_ATTACK.atlasKey)
        imageDown = imageUp
    }

    imageButton(SkinImageButton.PAUSE_PLAY.name) {
        imageUp = skin.getDrawable(SkinImage.PAUSE.atlasKey)
        imageChecked = skin.getDrawable(SkinImage.PLAY.atlasKey)
        imageDown = imageChecked
        up = skin.getDrawable(SkinImage.FRAME.atlasKey)
        down = up
    }
    imageButton(SkinImageButton.QUIT.name) {
        imageDown = skin.getDrawable(SkinImage.QUIT.atlasKey)
        imageUp = imageDown
        up = skin.getDrawable(SkinImage.FRAME.atlasKey)
        down = up
    }
    imageButton(SkinImageButton.SOUND_ON_OFF.name) {
        imageUp = skin.getDrawable(SkinImage.SOUND_ON.atlasKey)
        imageChecked = skin.getDrawable(SkinImage.SOUND_OFF.atlasKey)
        imageDown = imageChecked
        up = skin.getDrawable(SkinImage.FRAME.atlasKey)
        down = up
    }
}

private fun Skin.createLabelStyles(
    bigFont: BitmapFont,
    defaultFont: BitmapFont
) {
    label(SkinLabel.LARGE.name) {
        font = bigFont
    }
    label(SkinLabel.DEFAULT.name) {
        font = defaultFont
    }
}