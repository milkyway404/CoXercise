package com.p4pProject.gameTutorial.ecs.system.animation

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.GdxRuntimeException
import com.p4pProject.gameTutorial.ecs.component.*
import com.p4pProject.gameTutorial.ecs.system.BOSS_ATTACK_RANGE
import com.p4pProject.gameTutorial.event.GameEvent
import com.p4pProject.gameTutorial.event.GameEventManager
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.debug
import ktx.log.error
import ktx.log.logger
import java.util.*

private val LOG = logger<BossAnimationSystem>()
class BossAnimationSystem(
    private val atlas: TextureAtlas, private val gameEventManager: GameEventManager
): IteratingSystem(allOf(BossComponent::class, FacingComponent::class, GraphicComponent::class, BossAnimationComponent::class).get()),
    EntityListener {

    private val animationCache = EnumMap<AnimationType, Animation2D>(AnimationType::class.java)

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        engine.addEntityListener(family, this)
    }

    override fun removedFromEngine(engine: Engine) {
        super.removedFromEngine(engine)
        engine.removeEntityListener(this)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val facing = entity[FacingComponent.mapper]
        require(facing != null ){"Entity |entity| must have a FacingComponent. entity=$entity"}

        val graphic = entity[GraphicComponent.mapper]
        require(graphic != null ){"Entity |entity| must have a GraphicComponent. entity=$entity"}

        val aniCmp = entity[BossAnimationComponent.mapper]
        require(aniCmp != null ){"Entity |entity| must have a BossAnimationComponent. entity=$entity"}

        val boss = entity[BossComponent.mapper]
        require(boss != null ){"Entity |entity| must have a PlayerComponent. entity=$entity"}

//        if(facing.direction == facing.lastDirection && graphic.sprite.texture!= null){
//            LOG.debug { "lol gotcha" }
//            return
//        }
        if(boss.isAttackReady){
            //Gdx.app.log("Boss Animation", "attacking...")
            boss.isAttacking = true
            facing.lastDirection = facing.direction

            val region = when(facing.direction){
                FacingDirection.WEST -> animateAttackRight(aniCmp, deltaTime, entity, boss.attackDamage)
                FacingDirection.EAST -> animateAttackLeft(aniCmp, deltaTime, entity, boss.attackDamage)
                FacingDirection.NORTH -> animateAttackUp(aniCmp, deltaTime, entity, boss.attackDamage)
                FacingDirection.SOUTH -> animateAttackDown(aniCmp, deltaTime, entity, boss.attackDamage)
            }
            graphic.setSpriteRegion(region)
            //Gdx.app.log("Boss Animation", "done...")
        }else if(boss.isHurt || boss.isStunned){
            facing.lastDirection = facing.direction
            val region = when(facing.direction){
                FacingDirection.WEST -> animateHurtLeft(aniCmp, deltaTime)
                FacingDirection.EAST -> animateHurtRight(aniCmp, deltaTime)
                FacingDirection.NORTH -> animateHurtUp(aniCmp, deltaTime)
                FacingDirection.SOUTH -> animateHurtDown(aniCmp, deltaTime)
            }
            graphic.setSpriteRegion(region)
        }else{
            facing.lastDirection = facing.direction
            val region = when(facing.direction){
                FacingDirection.WEST -> animateIdleLeft(aniCmp, deltaTime)
                FacingDirection.EAST -> animateIdleRight(aniCmp, deltaTime)
                FacingDirection.NORTH -> animateIdleUp(aniCmp, deltaTime)
                FacingDirection.SOUTH -> animateIdleDown(aniCmp, deltaTime)
            }
            graphic.setSpriteRegion(region)
        }



        /*val region = when(facing.direction){
                FacingDirection.WEST -> animateLeft(aniCmp, deltaTime)
                FacingDirection.EAST -> animateRight(aniCmp, deltaTime)
                FacingDirection.NORTH -> animateUp(aniCmp, deltaTime)
                FacingDirection.SOUTH -> animateDown(aniCmp, deltaTime)
        }



        graphic.setSpriteRegion(region)*/
    }

    override fun entityAdded(entity: Entity) {
        entity[BossAnimationComponent.mapper]?.let{ aniCmp ->
            aniCmp.animation = getAnimation(aniCmp.typeUp)
            val frame = aniCmp.animation.getKeyFrame(aniCmp.stateTime)
            entity[GraphicComponent.mapper]?.setSpriteRegion(frame)
        }
    }

    private fun animateIdleUp(aniCmp: BossAnimationComponent, deltaTime: Float): TextureRegion {
        if (aniCmp.typeUp == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAnimation(aniCmp.typeUp)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateIdleDown(aniCmp: BossAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeDown == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAnimation(aniCmp.typeDown)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateIdleLeft(aniCmp: BossAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeLeft == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAnimation(aniCmp.typeLeft)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateIdleRight(aniCmp: BossAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeRight == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAnimation(aniCmp.typeRight)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }


    private fun animateAttackUp(aniCmp: BossAnimationComponent, deltaTime: Float, entity: Entity, bossAttackDamage: Int): TextureRegion {
        val transform = entity[TransformComponent.mapper]
        require(transform != null ){"Entity |entity| must have a PlayerComponent. entity=$entity"}
        if (aniCmp.typeAttackUp == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeAttackUp)
        }
        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent(GameEvent.BossAttackFinished.apply {
                this.damage = bossAttackDamage
                this.startX = transform.position.x - BOSS_ATTACK_RANGE
                this.endX = transform.position.x + transform.size.x + BOSS_ATTACK_RANGE
                this.startY = transform.position.y - transform.size.y - BOSS_ATTACK_RANGE
                this.endY = transform.position.y + BOSS_ATTACK_RANGE
                Gdx.app.log("Boss Attack", toString())
            })
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateAttackDown(aniCmp: BossAnimationComponent, deltaTime: Float, entity: Entity, bossAttackDamage: Int): TextureRegion {
        val transform = entity[TransformComponent.mapper]
        require(transform != null ){"Entity |entity| must have a PlayerComponent. entity=$entity"}
        if (aniCmp.typeAttackDown == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeAttackDown)
        }

        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent(GameEvent.BossAttackFinished.apply {
                this.damage = bossAttackDamage
                this.startX = transform.position.x - BOSS_ATTACK_RANGE
                this.endX = transform.position.x + transform.size.x + BOSS_ATTACK_RANGE
                this.startY = transform.position.y - transform.size.y - BOSS_ATTACK_RANGE
                this.endY = transform.position.y + BOSS_ATTACK_RANGE
                Gdx.app.log("Boss Attack", toString())
            })
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateAttackLeft(aniCmp: BossAnimationComponent, deltaTime: Float, entity: Entity, bossAttackDamage: Int): TextureRegion {
        val transform = entity[TransformComponent.mapper]
        require(transform != null ){"Entity |entity| must have a PlayerComponent. entity=$entity"}
        if (aniCmp.typeAttackLeft == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeAttackLeft)
        }

        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent(GameEvent.BossAttackFinished.apply {
                this.damage = bossAttackDamage
                this.startX = transform.position.x - BOSS_ATTACK_RANGE
                this.endX = transform.position.x + transform.size.x + BOSS_ATTACK_RANGE
                this.startY = transform.position.y - transform.size.y - BOSS_ATTACK_RANGE
                this.endY = transform.position.y + BOSS_ATTACK_RANGE
                Gdx.app.log("Boss Attack", toString())
            })
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateAttackRight(aniCmp: BossAnimationComponent, deltaTime: Float, entity: Entity, bossAttackDamage: Int): TextureRegion {
        val transform = entity[TransformComponent.mapper]
        require(transform != null ){"Entity |entity| must have a PlayerComponent. entity=$entity"}
        if (aniCmp.typeAttackRight == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeAttackRight)
        }

        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent(GameEvent.BossAttackFinished.apply {
                this.damage = bossAttackDamage
                this.startX = transform.position.x - BOSS_ATTACK_RANGE
                this.endX = transform.position.x + transform.size.x + BOSS_ATTACK_RANGE
                this.startY = transform.position.y - transform.size.y - BOSS_ATTACK_RANGE
                this.endY = transform.position.y + BOSS_ATTACK_RANGE
                Gdx.app.log("Boss Attack", toString())
            })
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }


    private fun animateHurtUp(aniCmp: BossAnimationComponent, deltaTime: Float): TextureRegion {
        if (aniCmp.typeHurtUp == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeHurtUp)
        }
        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent(GameEvent.BossHitFinished)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateHurtDown(aniCmp: BossAnimationComponent, deltaTime: Float): TextureRegion {
        if (aniCmp.typeHurtDown == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeHurtDown)
        }

        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent(GameEvent.BossHitFinished)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateHurtLeft(aniCmp: BossAnimationComponent, deltaTime: Float): TextureRegion {
        if (aniCmp.typeHurtLeft == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeHurtLeft)
        }

        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent(GameEvent.BossHitFinished)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateHurtRight(aniCmp: BossAnimationComponent, deltaTime: Float): TextureRegion {
        if (aniCmp.typeHurtRight == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeHurtRight)
        }

        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent(GameEvent.BossHitFinished)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun getAnimation(type : AnimationType) : Animation2D {
        var animation = animationCache[type]
        if(animation == null){
            var regions = atlas.findRegions(type.atlasKey)
            if(regions.isEmpty){
                LOG.error { "No regions found for ${type.atlasKey}" }
                regions = atlas.findRegions("error")
                if (regions == null) throw GdxRuntimeException("There is no error region in the atlas")
            }
            animation = Animation2D(type, regions, type.playModeLoop, type.speedRate)
            animationCache[type] = animation
        }
        return animation
    }

    private fun getAttackAnimation(type : AnimationType) : Animation2D {
        var animation = animationCache[type]
        if(animation == null){
            var regions = atlas.findRegions(type.atlasKey)
            if(regions.isEmpty){
                LOG.error { "No regions found for ${type.atlasKey}" }
                regions = atlas.findRegions("error")
                if (regions == null) throw GdxRuntimeException("There is no error region in the atlas")
            }else{
                LOG.debug{"Adding animation of type $type with ${regions.size} regions"}
            }
            animation = Animation2D(type, regions, type.playModeNormal, type.speedRate)
            animationCache[type] = animation
        }
        return animation
    }

    override fun entityRemoved(entity: Entity?) = Unit


}