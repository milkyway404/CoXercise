package com.p4pProject.gameTutorial.ecs.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor
import ktx.collections.GdxArray


private const val DEFAULT_FRAME_DURATION = 1/20f

enum class AnimationType (
    val atlasKey: String,
    val playMode: Animation.PlayMode = Animation.PlayMode.LOOP,
    val speedRate: Float = 1f
        ){
    NONE(""),
    DARK_MATTER("dark_matter"),
    FIRE("fire"),
    SPEED_1("orb_blue"),
    SPEED_2("orb_yellow"),
    LIFE("life"),
    SHIELD("shield"),

    BOSS_UP("SkeletonWithSwordUpIdle"),
    BOSS_DOWN("SkeletonWithSwordDownIdle"),
    BOSS_LEFT("SkeletonWithSwordLeftIdle"),
    BOSS_RIGHT("SkeletonWithSwordRightIdle"),

    PLAYER_UP("WarriorUpIdle"),
    PLAYER_DOWN("WarriorDownIdle"),
    PLAYER_LEFT("WarriorLeftIdle"),
    PLAYER_RIGHT("WarriorRightIdle"),
    ATTACK_RIGHT("WarriorRightAttack"),
    ATTACK_LEFT("WarriorLeftAttack"),
    ATTACK_UP("WarriorUpAttack"),
    ATTACK_DOWN("WarriorDownAttack")
}

class Animation2D (
    val type:AnimationType,
    keyFrames: GdxArray<out TextureRegion>,
    playMode: PlayMode = PlayMode.LOOP,
    speedRate:Float = 1f
) : Animation<TextureRegion>((DEFAULT_FRAME_DURATION)/speedRate, keyFrames , playMode)

class AnimationComponent : Component, Pool.Poolable {
    var type = AnimationType.NONE
    var stateTime = 0f
    lateinit var animation : Animation2D

    override fun reset() {
       type = AnimationType.NONE
    }

    companion object {
        val mapper = mapperFor<AnimationComponent>()
    }
}

class PlayerAnimationComponent : Component, Pool.Poolable {
    var typeUp = AnimationType.PLAYER_UP
    var typeDown = AnimationType.PLAYER_DOWN
    var typeLeft = AnimationType.PLAYER_LEFT
    var typeRight = AnimationType.PLAYER_RIGHT
    var typeAttackRight = AnimationType.ATTACK_RIGHT
    var typeAttackLeft = AnimationType.ATTACK_LEFT
    var typeAttackUp = AnimationType.ATTACK_UP
    var typeAttackDown = AnimationType.ATTACK_DOWN
    var stateTime = 0f

    lateinit var animation : Animation2D


    override fun reset() {
         typeUp = AnimationType.NONE
         typeDown = AnimationType.NONE
         typeLeft = AnimationType.NONE
         typeRight = AnimationType.NONE
        typeAttackRight = AnimationType.NONE
        typeAttackLeft = AnimationType.NONE
        typeAttackUp = AnimationType.NONE
        typeAttackDown = AnimationType.NONE
    }

    companion object {
        val mapper = mapperFor<PlayerAnimationComponent>()
    }
}

class BossAnimationComponent : Component, Pool.Poolable {
    var typeUp = AnimationType.BOSS_UP
    var typeDown = AnimationType.BOSS_DOWN
    var typeLeft = AnimationType.BOSS_LEFT
    var typeRight = AnimationType.BOSS_RIGHT
    var stateTime = 0f

    lateinit var animation : Animation2D


    override fun reset() {
        typeUp = AnimationType.NONE
        typeDown = AnimationType.NONE
        typeLeft = AnimationType.NONE
        typeRight = AnimationType.NONE
    }

    companion object {
        val mapper = mapperFor<BossAnimationComponent>()
    }
}