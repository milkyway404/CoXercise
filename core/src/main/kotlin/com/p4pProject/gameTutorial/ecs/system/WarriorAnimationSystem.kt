package com.p4pProject.gameTutorial.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityListener
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.GdxRuntimeException
import com.p4pProject.gameTutorial.ecs.component.*
import com.p4pProject.gameTutorial.event.GameEvent
import com.p4pProject.gameTutorial.event.GameEventManager
import com.p4pProject.gameTutorial.screen.chosenCharacterType
import com.p4pProject.gameTutorial.screen.CharacterType
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.debug
import ktx.log.error
import ktx.log.logger
import java.util.*


private val LOG = logger<WarriorAnimationSystem>()
class WarriorAnimationSystem(
    private val atlas: TextureAtlas, private val gameEventManager: GameEventManager
): IteratingSystem(allOf(PlayerComponent::class, FacingComponent::class, GraphicComponent::class, WarriorAnimationComponent::class).get()),
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

        val aniCmp = entity[WarriorAnimationComponent.mapper]
        require(aniCmp != null ){"Entity |entity| must have a WarriorAnimationComponent. entity=$entity"}

        val player = entity[PlayerComponent.mapper]
        require(player != null ){"Entity |entity| must have a PlayerComponent. entity=$entity"}

        facing.lastDirection = facing.direction


        if(player.isAttacking){
            val region =  when(facing.direction){
                FacingDirection.WEST -> animateLeftAttack(aniCmp, deltaTime)
                FacingDirection.EAST -> animateRightAttack(aniCmp, deltaTime)
                FacingDirection.NORTH -> animateUpAttack(aniCmp, deltaTime)
                FacingDirection.SOUTH -> animateDownAttack(aniCmp, deltaTime)
            }
            graphic.setSpriteRegion(region)
        }else if(player.isSpecialAttacking){
            val region =  when(facing.direction){
                FacingDirection.WEST -> animateLeftSpecialAttack(aniCmp, deltaTime, entity)
                FacingDirection.EAST -> animateRightSpecialAttack(aniCmp, deltaTime, entity)
                FacingDirection.NORTH -> animateUpSpecialAttack(aniCmp, deltaTime, entity)
                FacingDirection.SOUTH -> animateDownSpecialAttack(aniCmp, deltaTime, entity)
            }
            graphic.setSpriteRegion(region)
        }else{
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
        entity[WarriorAnimationComponent.mapper]?.let{ aniCmp ->
            aniCmp.animation = getIdleAnimation(aniCmp.typeUp)
            val frame = aniCmp.animation.getKeyFrame(aniCmp.stateTime)
            entity[GraphicComponent.mapper]?.setSpriteRegion(frame)
        }
    }

    private fun animateIdleUp(aniCmp:WarriorAnimationComponent, deltaTime: Float): TextureRegion {
        if (aniCmp.typeUp == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getIdleAnimation(aniCmp.typeUp)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateIdleDown(aniCmp:WarriorAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeDown == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getIdleAnimation(aniCmp.typeDown)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateIdleLeft(aniCmp:WarriorAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeLeft == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getIdleAnimation(aniCmp.typeLeft)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateIdleRight(aniCmp:WarriorAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeRight == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getIdleAnimation(aniCmp.typeRight)
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateRightAttack(aniCmp:WarriorAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeAttackRight == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeAttackRight)
        }

        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent((GameEvent.WarriorAttackFinishEvent))
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateLeftAttack(aniCmp:WarriorAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeAttackLeft == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeAttackLeft)
        }

        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent((GameEvent.WarriorAttackFinishEvent))
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }
    private fun animateUpAttack(aniCmp:WarriorAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeAttackUp == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeAttackUp)
        }

        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent((GameEvent.WarriorAttackFinishEvent))
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }
    private fun animateDownAttack(aniCmp:WarriorAnimationComponent, deltaTime: Float): TextureRegion {

        if (aniCmp.typeAttackDown == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeAttackDown)
        }

        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent((GameEvent.WarriorAttackFinishEvent))
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateRightSpecialAttack(aniCmp:WarriorAnimationComponent, deltaTime: Float, player:Entity): TextureRegion {

        if (aniCmp.typeSpecialAttackRight == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeSpecialAttackRight)
        }

        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent((GameEvent.WarriorSpecialAttackFinishEvent.apply {
                this.player = player
                this.damage = 0
            }))
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun animateLeftSpecialAttack(aniCmp:WarriorAnimationComponent, deltaTime: Float, player:Entity): TextureRegion {

        if (aniCmp.typeSpecialAttackLeft == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeSpecialAttackLeft)
        }

        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent((GameEvent.WarriorSpecialAttackFinishEvent.apply {
                this.player = player
                this.damage = 0
            }))
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }
    private fun animateUpSpecialAttack(aniCmp:WarriorAnimationComponent, deltaTime: Float, player:Entity): TextureRegion {

        if (aniCmp.typeSpecialAttackUp == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeSpecialAttackUp)
        }

        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent((GameEvent.WarriorSpecialAttackFinishEvent.apply {
                this.player = player
                this.damage = 0
            }))
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }
    private fun animateDownSpecialAttack(aniCmp:WarriorAnimationComponent, deltaTime: Float, player:Entity): TextureRegion {

        if (aniCmp.typeSpecialAttackDown == aniCmp.animation.type){
            // animation is correctly set -> update it
            aniCmp.stateTime += deltaTime
        }else{
            //change animation
            aniCmp.stateTime = 0f
            aniCmp.animation = getAttackAnimation(aniCmp.typeSpecialAttackDown)
        }

        if(aniCmp.animation.isAnimationFinished(aniCmp.stateTime)){
            gameEventManager.dispatchEvent((GameEvent.WarriorSpecialAttackFinishEvent.apply {
                this.player = player
                this.damage = 0
            }))
        }
        return aniCmp.animation.getKeyFrame(aniCmp.stateTime)
    }

    private fun getIdleAnimation(type : AnimationType) : Animation2D {
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