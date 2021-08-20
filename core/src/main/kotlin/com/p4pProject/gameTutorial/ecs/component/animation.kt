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
    MP("orb_blue"),
    SPEED_2("orb_yellow"),
    LIFE("life"),
    SHIELD("shield"),

    BOSS_UP("SkeletonWithSwordUpIdle"),
    BOSS_DOWN("SkeletonWithSwordDownIdle"),
    BOSS_LEFT("SkeletonWithSwordLeftIdle"),
    BOSS_RIGHT("SkeletonWithSwordRightIdle"),

    WARRIOR_UP("WarriorUpIdle"),
    WARRIOR_DOWN("WarriorDownIdle"),
    WARRIOR_LEFT("WarriorLeftIdle"),
    WARRIOR_RIGHT("WarriorRightIdle"),
    WARRIOR_ATTACK_RIGHT("WarriorRightAttack"),
    WARRIOR_ATTACK_LEFT("WarriorLeftAttack"),
    WARRIOR_ATTACK_UP("WarriorUpAttack"),
    WARRIOR_ATTACK_DOWN("WarriorDownAttack"),

    ARCHER_UP("GoblinSlingerUpIdle"),
    ARCHER_DOWN("GoblinSlingerDownIdle"),
    ARCHER_LEFT("GoblinSlingerLeftIdle"),
    ARCHER_RIGHT("GoblinSlingerRightIdle"),
    ARCHER_ATTACK_RIGHT("GoblinSlingerRightAttack"),
    ARCHER_ATTACK_LEFT("GoblinSlingerLeftAttack"),
    ARCHER_ATTACK_UP("GoblinSlingerUpAttack"),
    ARCHER_ATTACK_DOWN("GoblinSlingerDownAttack"),

    PRIEST_UP("NecromancerUpIdle"),
    PRIEST_DOWN("NecromancerDownIdle"),
    PRIEST_LEFT("NecromancerLeftIdle"),
    PRIEST_RIGHT("NecromancerRightIdle"),
    PRIEST_ATTACK_RIGHT("NecromancerRightAttack"),
    PRIEST_ATTACK_LEFT("NecromancerLeftAttack"),
    PRIEST_ATTACK_UP("NecromancerUpAttack"),
    PRIEST_ATTACK_DOWN("NecromancerDownAttack")
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

class WarriorAnimationComponent : Component, Pool.Poolable {
    var typeUp = AnimationType.WARRIOR_UP
    var typeDown = AnimationType.WARRIOR_DOWN
    var typeLeft = AnimationType.WARRIOR_LEFT
    var typeRight = AnimationType.WARRIOR_RIGHT
    var typeAttackRight = AnimationType.WARRIOR_ATTACK_RIGHT
    var typeAttackLeft = AnimationType.WARRIOR_ATTACK_LEFT
    var typeAttackUp = AnimationType.WARRIOR_ATTACK_UP
    var typeAttackDown = AnimationType.WARRIOR_ATTACK_DOWN
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
        val mapper = mapperFor<WarriorAnimationComponent>()
    }
}

class ArcherAnimationComponent : Component, Pool.Poolable {

    var typeUp = AnimationType.ARCHER_UP
    var typeDown = AnimationType.ARCHER_DOWN
    var typeLeft = AnimationType.ARCHER_LEFT
    var typeRight = AnimationType.ARCHER_RIGHT
    var typeAttackRight = AnimationType.ARCHER_ATTACK_RIGHT
    var typeAttackLeft = AnimationType.ARCHER_ATTACK_LEFT
    var typeAttackUp = AnimationType.ARCHER_ATTACK_UP
    var typeAttackDown = AnimationType.ARCHER_ATTACK_DOWN
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
        val mapper = mapperFor<ArcherAnimationComponent>()
    }
}

class PriestAnimationComponent : Component, Pool.Poolable {

    var typeUp = AnimationType.PRIEST_UP
    var typeDown = AnimationType.PRIEST_DOWN
    var typeLeft = AnimationType.PRIEST_LEFT
    var typeRight = AnimationType.PRIEST_RIGHT
    var typeAttackRight = AnimationType.PRIEST_ATTACK_RIGHT
    var typeAttackLeft = AnimationType.PRIEST_ATTACK_LEFT
    var typeAttackUp = AnimationType.PRIEST_ATTACK_UP
    var typeAttackDown = AnimationType.PRIEST_ATTACK_DOWN
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
        val mapper = mapperFor<PriestAnimationComponent>()
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